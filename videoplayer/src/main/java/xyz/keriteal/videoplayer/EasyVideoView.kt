package xyz.keriteal.videoplayer

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import cn.jzvd.JZTextureView
import cn.jzvd.JZUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.keriteal.videoplayer.utils.logd
import java.util.*

/**
 * 视频播放器核心（不包括控件）
 * 仅用于显示视频内容
 */
@Suppress("MemberVisibilityCanBePrivate")
open class EasyVideoView : FrameLayout {
    constructor(
        context: Context,
        mediaPlayerInterfaceClass: Class<IMediaBackend<*>>
    ) : super(
        context
    ) {
        mMediaPlayerInterfaceClass = mediaPlayerInterfaceClass
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private var mProgressListener: OnProgressListener = InnerProgressListener()
    var progressListener: OnProgressListener? = null
    private var mPlayerStateListener: OnPlayerStateListener = InnerPlayerStateListener()
    var playerStateListener: OnPlayerStateListener? = null
    private var mBufferedListener: OnBufferedListener = InnerBufferedListener()
    var bufferedStateListener: OnBufferedListener? = null
    private var mDataSourceChangeListener: OnDataSourceChangeListener =
        InnerDataSourceChangedListener()
    var dataSourceChangeListener: OnDataSourceChangeListener? = null

    private var mTextureView: JZTextureView? = null
    val textureView get() = mTextureView
    private var mDataSource: VideoDataSource? = null
    private var mMediaPlayerInterfaceClass: Class<out IMediaBackend<out MediaOptions>>? =
        null
    var mediaInterfaceClass: Class<out IMediaBackend<out MediaOptions>>
        get() = mMediaPlayerInterfaceClass ?: DEFAULT_MEDIA_INTERFACE_CLASS
        set(value) {
            mMediaPlayerInterfaceClass = value
        }
    private var mMediaPlayerInterface: IMediaBackend<*>? = null
    private var mProgressTimer: Timer? = null
    private var mProgressTimerTask: ProgressTimerTask? = null

    private var mPlayerState: Int = PLAYER_STATE_IDLE
    private var mRotation: Float = 0f
    private var mVideoSpeed: Float = 1f
    private var mVideoDuration: Long = 0
    private var mVideoPosition: Long = 0
    private var mBufferedPercent: Int = 0

    var pauseOnNavigating: Boolean = false
    var autoPlayOnMobileData: Boolean = false

    var videoSpeed
        get() = mVideoSpeed
        set(value) {
            mMediaPlayerInterface?.let { inter ->
                inter.setSpeed(value)
                mVideoSpeed = value
            }
        }
    var dataSource
        get() = mDataSource
        set(value) {
            if (value != null) {
                mDataSource = value
                mDataSourceChangeListener.onDataSourceChanged(value)
            }
        }

    val realtimePosition get() = mMediaPlayerInterface!!.getCurrentPosition()

    /**
     * 播放器状态 具体的值查看 PLAYER_STATE_ 开头的常量
     */
    val playerState get() = mPlayerState
    var videoRotation
        get() = mRotation
        set(value) {
            mRotation = value
            CoroutineScope(Dispatchers.Main).launch {
                mTextureView?.rotation = rotation
            }
        }

    /**
     * 播放/暂停
     */
    fun startPause() {
        Log.d(TAG, "[${hashCode()}] Performing startPause")
        if (mDataSource?.currentUrl.isNullOrBlank()) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, resources.getText(R.string.no_url), Toast.LENGTH_LONG)
                    .show()
            }
            return
        }
        when (mPlayerState) {
            PLAYER_STATE_IDLE -> {
                if (mDataSource?.currentUrl?.startsWith("file") == false ||
                    mDataSource?.currentUrl?.startsWith("/") == false ||
                    JZUtils.isWifiConnected(context)
                ) {
                    initVideo()
                }
            }
            PLAYER_STATE_PLAYING -> {
                mMediaPlayerInterface?.pause().also {
                    updatePlayerStateInternal(PLAYER_STATE_PAUSED)
                }
            }
            PLAYER_STATE_PAUSED -> {
                mMediaPlayerInterface?.start().also {
                    updatePlayerStateInternal(PLAYER_STATE_PLAYING)
                }
            }
            PLAYER_STATE_FINISHED -> {

            }
        }
        mMediaPlayerInterface?.start()
    }

    /**
     * 重置播放器
     */
    fun reset() {
        cancelProgressTimer()
        removeAllViews()
        mMediaPlayerInterface?.release()
        updatePlayerStateInternal(PLAYER_STATE_IDLE)
    }

    /**
     * 初始化视频
     */
    fun initVideo() {
        Log.d(TAG, "[${hashCode()}] InitVideo")
        if (mPlayerState != PLAYER_STATE_IDLE) {
            Log.e(
                TAG, "[${hashCode()}] Trying to init video on a busy video core",
                IllegalStateException()
            )
        } else {
            if (mMediaPlayerInterface == null) {
                try {
                    val constructor = mediaInterfaceClass.getConstructor()
                    val mediaPlayerInterface = constructor.newInstance()
                    mMediaPlayerInterface = mediaPlayerInterface
                    mediaPlayerInterface.setDataSource(mDataSource!!)
                    attachTextureView(mediaPlayerInterface)
                } catch (e: Exception) {
                    Log.e(TAG, "[${hashCode()}] Create MediaInterface", e)
                }
            }
            // 设置状态为准备中
            updatePlayerStateInternal(PLAYER_STATE_PREPARING)
        }
    }

    /**
     * 附加TextureView（如果已经有了则删除)
     */
    private fun attachTextureView(mediaPlayerInterface: IMediaBackend<*>) {
        logd(TAG, hashCode(), "Attaching JzTextureView")
        if (mTextureView != null)
            this.removeView(mTextureView)
        val textureView = JZTextureView(context)
        textureView.surfaceTexture
        mTextureView = textureView
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture, width: Int, height: Int
            ) {
                mediaPlayerInterface.setSurface(Surface(textureView.surfaceTexture))
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture, width: Int, height: Int
            ) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }
        }
        val layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        mediaPlayerInterface.prepare()
        CoroutineScope(Dispatchers.Main).launch {
            addView(mTextureView, layoutParams)
        }
        logd(TAG, hashCode(), "Attached JzTextureView")
    }

    private fun startProgressTimer() {
        cancelProgressTimer()
        mProgressTimerTask = ProgressTimerTask().also {
            mProgressTimer = Timer().also {
                it.schedule(mProgressTimerTask, 0, 300)
            }
        }
    }

    private fun cancelProgressTimer() {
        mProgressTimer?.cancel()
        mProgressTimerTask?.cancel()
    }

    /**
     * 仅用于更新状态
     */
    private fun updatePlayerStateInternal(newState: Int) {
        val oldState = mPlayerState
        mPlayerState = newState
        mPlayerStateListener.onPlayerStateChanged(oldState, newState)
    }


    interface OnPlayerStateListener {
        fun onPlayerStateChanged(oldState: Int, newState: Int)
    }

    interface OnPreparedListener {
        fun onVideoPrepared()
    }

    interface OnErrorListener {
        fun onVideoError()
    }

    interface OnProgressListener {
        fun onProgress(position: Long, duration: Long, bufferedPercent: Int)
    }

    interface OnBufferedListener {
        fun onBuffered(percent: Int)
    }

    interface OnDataSourceChangeListener {
        fun onDataSourceChanged(dataSource: VideoDataSource)
    }


    private inner class InnerMediaPlayerInterfaceListener :
        IMediaBackend.OnStateListener {

        override fun onSeekComplete() {
            TODO("Not yet implemented")
        }

        override fun onError() {
            TODO("Not yet implemented")
        }

        override fun onFinished() {
            TODO("Not yet implemented")
        }

        override fun onInfo(what: Int, extra: Int) {
            TODO("Not yet implemented")
        }

        override fun onPrepared() {

            updatePlayerStateInternal(PLAYER_STATE_PREPARED)
        }

        override fun onStarted() {
            TODO("Not yet implemented")
        }

        override fun onPaused() {
            TODO("Not yet implemented")
        }

        override fun onPlayComplated() {
            TODO("Not yet implemented")
        }

        override fun onVideoSizeChanged(videoWidth: Int, videoHeight: Int) {
            Log.d(TAG, "[${hashCode()}] Video size changed")
            if (videoRotation != 0f) {
                mTextureView?.rotation = videoRotation
            }
            mTextureView?.setVideoSize(videoWidth, videoHeight)
        }

        override fun onBufferingUpdate(percent: Int) {
            mBufferedListener.onBuffered(percent)
        }
    }

    private inner class ProgressTimerTask : TimerTask() {
        override fun run() {
            if (mPlayerState == PLAYER_STATE_PLAYING
                || mPlayerState == PLAYER_STATE_PAUSED
                || mPlayerState == PLAYER_STATE_PREPARING
                || mPlayerState == PLAYER_STATE_BUFFERING
            ) {
                val position = (mMediaPlayerInterface?.getCurrentPosition()) ?: 0
                val duration = (mMediaPlayerInterface?.getDuration()) ?: 0
                val buffered = mBufferedPercent
                mProgressListener.onProgress(position, duration, buffered)
            }
        }
    }

    private inner class InnerProgressListener : OnProgressListener {
        override fun onProgress(position: Long, duration: Long, bufferedPercent: Int) {
            progressListener?.onProgress(position, duration, bufferedPercent)
        }
    }

    private inner class InnerPlayerStateListener : OnPlayerStateListener {
        override fun onPlayerStateChanged(oldState: Int, newState: Int) {
            playerStateListener?.onPlayerStateChanged(oldState, newState)
        }
    }

    private inner class InnerBufferedListener : OnBufferedListener {
        override fun onBuffered(percent: Int) {
            bufferedStateListener?.onBuffered(percent)
        }
    }

    private inner class InnerDataSourceChangedListener : OnDataSourceChangeListener {
        override fun onDataSourceChanged(dataSource: VideoDataSource) {
            dataSourceChangeListener?.onDataSourceChanged(dataSource)
        }
    }


    companion object {
        const val TAG = "VideoPlayer"
        val DEFAULT_MEDIA_INTERFACE_CLASS = IjkMediaBackend::class.java

        /**
         * 空闲
         */
        const val PLAYER_STATE_IDLE = -3

        /**
         * 准备中
         */
        const val PLAYER_STATE_PREPARING = -2

        /**
         * 准备完毕
         */
        const val PLAYER_STATE_PREPARED = -1

        /**
         * 播放中
         */
        const val PLAYER_STATE_PLAYING = 0

        /**
         * 暂停
         */
        const val PLAYER_STATE_PAUSED = 1

        /**
         * 缓冲中
         */
        const val PLAYER_STATE_BUFFERING = 2

        /**
         * 播放完毕
         */
        const val PLAYER_STATE_FINISHED = 3
    }
}