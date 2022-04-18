package xyz.keriteal.videoplayer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.keriteal.videoplayer.IMediaBackend.Companion.STATE_FINISHED
import xyz.keriteal.videoplayer.IMediaBackend.Companion.STATE_IDLE
import xyz.keriteal.videoplayer.IMediaBackend.Companion.STATE_PAUSED
import xyz.keriteal.videoplayer.IMediaBackend.Companion.STATE_PLAY_COMPLETED
import xyz.keriteal.videoplayer.IMediaBackend.Companion.STATE_PREPARED
import xyz.keriteal.videoplayer.IMediaBackend.Companion.STATE_STARTED
import xyz.keriteal.videoplayer.utils.KLog
import xyz.keriteal.videoplayer.utils.logd
import xyz.keriteal.videoplayer.utils.loge
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseMediaBackend<OP : MediaOptions>(
    private val mediaOptions: OP
) : IMediaBackend<OP> {
    companion object {
        const val TAG = "MediaBackend"
    }

    private var _state = STATE_IDLE
    private var _bufferPercent = 0
    private var _dataSource: VideoDataSource? = null
    protected var mState
        get() = _state
        set(value) {
            submitState(value)
        }
    protected var mStateListener: IMediaBackend.OnStateListener? = null
    fun setStateListener(listener: IMediaBackend.OnStateListener) {
        mStateListener = listener
    }

    protected var mStateChangedListener: IMediaBackend.OnStateChangedListener? = null
    fun setStatechangedListener(listener: IMediaBackend.OnStateChangedListener) {
        mStateChangedListener = listener
    }

    protected var mErrorListener: IMediaBackend.OnErrorListener? = null
    fun setErrorListener(listener: IMediaBackend.OnErrorListener) {
        mErrorListener = listener
    }

    protected var mSpeedListener: IMediaBackend.OnSpeedChangedListener? = null
    fun setSpeedListener(listener: IMediaBackend.OnSpeedChangedListener) {
        mSpeedListener = listener
    }

    protected var mSizeListener: IMediaBackend.OnSizeListener? = null
    fun setSizeListener(listener: IMediaBackend.OnSizeListener) {
        mSizeListener = listener
    }

    protected var mFrameworkErrorListener: IMediaBackend.OnFrameworkErrorListener? = null
    fun setFrameErrorListener(listener: IMediaBackend.OnFrameworkErrorListener) {
        mFrameworkErrorListener = listener
    }

    protected var mDataSourceListener: IMediaBackend.OnDataSourceListener? = null
    fun setDataSourceListener(listener: IMediaBackend.OnDataSourceListener) {
        mDataSourceListener = listener
    }

    protected val mInfoMessageConsumer: MutableMap<Int, MutableMap<InfoMessageSubscriber, () -> Unit>> =
        mutableMapOf()
    private val mInfoMessageSubscriberMap: MutableMap<InfoMessageSubscriber, Int> = mutableMapOf()

    protected fun releaseAllListener() {
        mStateListener = null
        mStateChangedListener = null
        mErrorListener = null
        mSpeedListener = null
        mSizeListener = null
        mFrameworkErrorListener = null
    }

    val backendState get() = mState

    private fun submitState(state: Int) {
        if (state !in -2..5) {
            throw IllegalArgumentException("Illegal state input")
        }
        _state = state
        /**
         * 异步执行State相关Listener，因为它们的结果与本类无关且容易造成阻塞
         */
        CoroutineScope(Dispatchers.Default).launch {
            val stateChangeResult = try {
                mStateChangedListener?.onState(state) == true
            } catch (e: Exception) {
                false
            }
            if (!stateChangeResult)
                return@launch
            KLog.tryLog(
                IMediaBackend.OnStateListener.TAG,
                mStateListener.hashCode(),
                "Listener Error"
            ) {
                when (state) {
                    STATE_FINISHED -> mStateListener?.onFinished()
                    STATE_PREPARED -> {
                        if (mediaOptions.autoPlay) {
                            start()
                        }
                        mStateListener?.onPrepared()
                    }
                    STATE_STARTED -> mStateListener?.onStarted()
                    STATE_PAUSED -> mStateListener?.onPaused()
                    STATE_PLAY_COMPLETED -> mStateListener?.onPlayComplated()
                }
            }
        }
    }

    protected fun submitError(error: Int, e: Exception? = null) {
        if (e != null) {
            loge(TAG, hashCode(), "onError $error", e)
            mErrorListener?.onError(error)
        }
    }

    protected fun submitSpeed(speed: Float) {
        mSpeedListener?.onSpeed(speed)
    }

    protected fun submitSize(width: Int, height: Int) {
        mSizeListener?.onSizeChanged(width, height)
    }

    protected fun notifySeek() {
        CoroutineScope(Dispatchers.Default).launch {
            mStateListener?.onSeekComplete()
        }
    }

    protected fun notifyBufferingUpdate(percent: Int) {
        _bufferPercent = percent
        CoroutineScope(Dispatchers.Default).launch {
            logd(TAG, hashCode(), "Buffering Update to $percent%")
            mStateListener?.onBufferingUpdate(percent)
        }
    }


    protected fun notifyFrameworkError(error: Int, implError: Int, e: Exception? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            loge(TAG, hashCode(), "Framework Error: $error", e)
            mFrameworkErrorListener?.onError(error, implError)
        }
    }

    protected fun submitInfo(info: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            mInfoMessageConsumer[info]?.forEach { (_, u) ->
                launch {
                    u.invoke()
                }
            }
        }
    }

    fun subscribeInfoMessage(info: Int, handler: () -> Unit) {
        val newSubscriber = UUID.randomUUID().toString()
        if (!mInfoMessageConsumer.containsKey(info)) {
            mInfoMessageConsumer[info] = mutableMapOf(
                newSubscriber to handler
            )
        } else {
            mInfoMessageConsumer[info]!![newSubscriber] = handler
        }
        mInfoMessageSubscriberMap[newSubscriber] = info
    }

    fun unsubscribeInfoMessage(subscriber: InfoMessageSubscriber) {
        val infoCode = mInfoMessageSubscriberMap[subscriber]
            ?: throw IllegalStateException("handler does not match")
        mInfoMessageConsumer[infoCode]?.remove(subscriber)
    }

    final override fun getState(): Int {
        return mState
    }

    final override fun startPauseResume() {
        when (getState()) {
            STATE_PREPARED -> start()
            STATE_PAUSED -> resume()
            STATE_STARTED -> pause()
        }
    }

    final override fun getBufferPercent(): Int {
        return _bufferPercent
    }

    final override fun isPaused(): Boolean {
        return mState == STATE_PAUSED
    }

    final override fun getDataSource(): VideoDataSource? {
        return _dataSource
    }

    final override fun setDataSource(source: VideoDataSource) {
        _dataSource?.keyChangedListener = null
        _dataSource = source
        source.keyChangedListener = object : VideoDataSource.OnKeyChangedListener {
            override fun onKeyChanged(dataSource: VideoDataSource) {
                mDataSourceListener?.onDataSourceKeyChanged(dataSource)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            mDataSourceListener?.onDataSourceChanged(source)
        }
        if (mState in arrayOf(STATE_PREPARED, STATE_STARTED, STATE_PAUSED, STATE_FINISHED))
            prepare()
    }
}

typealias InfoMessageSubscriber = String