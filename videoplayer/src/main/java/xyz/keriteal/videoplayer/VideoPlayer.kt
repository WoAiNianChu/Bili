package xyz.keriteal.videoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.absoluteValue

/**
 * 封装了基本的手势相关的操作
 */
@Suppress("LeakingThis", "MemberVisibilityCanBePrivate")
abstract class VideoPlayer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.defaultStyleAttr,
    defStyleRes: Int = R.style.DefaultVideoStyle
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        const val TAG = "VideoPlayer"
        const val GESTURE_NONE = 0
        const val GESTURE_VERTICAL = 1
        const val GESTURE_HORIZONTAL = 2
        const val GESTURE_PRESSING = 3

        const val CONTROLS_SHOWING = 0
        const val CONTROLS_HIDING = 1
    }

    private val _gestureListener: OnGestureListener = InnerGestureListener()

    protected var mLockStateListener: OnLockListener? = null
    protected var mGestureListener: OnGestureListener? = null
    private val mGestureDetector: GestureDetector

    private var _dismissControlsTimer: Timer? = null
    private var _dismissControlsTimerTask: DismissControlsTask? = null

    private var _screenState: Int = 0
    protected var mControlsState: Int = 0
    protected var mSwiping: Boolean = false

    private var _locked: Boolean = false
    var locked
        get() = _locked
        protected set(value) {
            _locked = value
            if (value) {
                mLockStateListener?.onLocked()
            } else {
                mLockStateListener?.onUnlocked()
            }
        }

    protected abstract var mVideoView: EasyVideoView
    protected abstract var mGestureView: View

    val playerState: Int get() = mVideoView.playerState

    var gestureSensitivityProgress: Float = 1f
    var gestureSensitivityBrightness: Float = 1f
    var gestureSensitivityVolume: Float = 1f
    var gestureSwipingThresholdVertical: Int = 30
    var gestureSwipingThresholdHorizontal: Int = 50
    var doubleClickDuration: Int = 1000
    var longPressingThreshold: Long = 1000
    var autoPlay: Boolean = false
    var dismissControlsDuration: Long = 3000
    var fastForwardSpeed = 3f

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.VideoPlayer, defStyleAttr, defStyleRes
        )
        setBackgroundColor(
            typedArray.getColor(R.styleable.VideoPlayer_backgroundColor, Color.BLACK)
        )
        autoPlay = typedArray.getBoolean(R.styleable.VideoPlayer_autoPlay, false)
        dismissControlsDuration =
            typedArray.getInt(
                R.styleable.VideoPlayer_dismissControlsDuration, dismissControlsDuration.toInt()
            ).toLong()
        fastForwardSpeed =
            typedArray.getFloat(R.styleable.VideoPlayer_fastForwardSpeed, fastForwardSpeed)
        gestureSensitivityBrightness =
            typedArray.getFloat(
                R.styleable.VideoPlayer_gestureSensitivityBrightness,
                gestureSensitivityBrightness
            )
        gestureSensitivityVolume =
            typedArray.getFloat(
                R.styleable.VideoPlayer_gestureSensitivityVolume,
                gestureSensitivityVolume
            )
        gestureSensitivityProgress =
            typedArray.getFloat(
                R.styleable.VideoPlayer_gestureSensitivityProgress,
                gestureSensitivityProgress
            )
        gestureSwipingThresholdHorizontal =
            typedArray.getInt(
                R.styleable.VideoPlayer_gestureSwipingThresholdHorizontal,
                gestureSwipingThresholdHorizontal
            )
        gestureSwipingThresholdVertical =
            typedArray.getInt(
                R.styleable.VideoPlayer_gestureSwipingThresholdVertical,
                gestureSwipingThresholdVertical
            )
        doubleClickDuration =
            typedArray.getInt(R.styleable.VideoPlayer_doubleClickDuration, doubleClickDuration)
        longPressingThreshold =
            typedArray.getInt(
                R.styleable.VideoPlayer_longPressingThreshold, longPressingThreshold.toInt()
            ).toLong()

        typedArray.recycle()

        val gestureDetector =
            GestureDetector(context, GestureDetector.SimpleOnGestureListener())
        gestureDetector.setOnDoubleTapListener(InnerDoubleTapListener())
        mGestureDetector = gestureDetector
    }

    open fun setup(
        dataSource: VideoDataSource,
        mediaInterfaceClass: Class<out IMediaBackend<out MediaOptions>> = IjkMediaBackend::class.java
    ) {
        mVideoView.dataSource = dataSource
        mVideoView.mediaInterfaceClass = mediaInterfaceClass
        mGestureView.setOnTouchListener(VideoGestureTouchListener())
        if (autoPlay) {
            mVideoView.initVideo()
        }
    }

    open fun start() {
        if (mVideoView.dataSource == null) {
            Log.e(
                TAG, "$[${hashCode()}] start failed",
                IllegalArgumentException("dataSource not set")
            )
        }
        mVideoView.startPause()
    }

    open fun pause() {

    }

    fun cancelDismissControlsTimer() {
        _dismissControlsTimer?.cancel()
        _dismissControlsTimerTask?.cancel()
    }

    fun startDismissControlsTimer() {
        cancelDismissControlsTimer()
        _dismissControlsTimer = Timer().also { timer ->
            _dismissControlsTimerTask = DismissControlsTask().also { timerTask ->
                timer.schedule(timerTask, dismissControlsDuration)
            }
        }
    }

    abstract fun dismissControls()
    abstract fun showControls()

    /**
     * 切换控件状态
     */
    fun switchControls() {
        Log.d(TAG, "[${hashCode()}] Switching controls")
        when (mControlsState) {
            CONTROLS_SHOWING -> dismissControls()
            CONTROLS_HIDING -> showControls()
        }
    }

    private inner class DismissControlsTask : TimerTask() {
        override fun run() {
            dismissControls()
        }
    }

    /**
     * 播放器手势
     */
    interface OnGestureListener {
        // <editor-fold defaultstate="collapsed" desc="Click Gestures">
        fun onDoubleClicked(): Boolean
        fun onSingleClicked()
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Swiping Gestures">
        fun onValidVerticalSwiping(startY: Float, endY: Float)
        fun onValidHorizontalSwiping(startX: Float, endX: Float)
        fun onSwipe(start: Float, end: Float, gestureType: Int)
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Long Pressing Gestures">
        fun onLongPressingStart(positionX: Float, positionY: Float)
        fun onLongPressingReleased(positionX: Float, positionY: Float)
        fun onSecondPressing()
        // </editor-fold>
    }

    /**
     * 锁定状态的变更
     */
    interface OnLockListener {
        fun onLocked()
        fun onUnlocked()
    }

    protected open inner class VideoGestureTouchListener : OnTouchListener {
        // <editor-fold defaultstate="collapsed" desc="手势信息">
        protected var mStartX: Float? = null
        protected var mStartY: Float? = null
        protected var mPressDownTime: Long = 0
        protected var mGestureType: Int = 0
        private var mLongPressingTimer: Timer? = null
        private var mLongPressingTimerTask: TimerTask? = null
        // </editor-fold>

        private fun startLongPressingTimer(x: Float, y: Float) {
            canceLongPressinglTimer()
            val timer = Timer()
            val timerTask = object : TimerTask() {
                override fun run() {
                    if (mGestureType == GESTURE_PRESSING)
                        _gestureListener.onLongPressingStart(x, y)
                }
            }
            timer.schedule(timerTask, longPressingThreshold)
            mLongPressingTimer = timer
            mLongPressingTimerTask = timerTask
        }

        private fun canceLongPressinglTimer() {
            mLongPressingTimer?.cancel()
            mLongPressingTimerTask?.cancel()
            mLongPressingTimer = null
            mLongPressingTimerTask = null
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (!locked) {
                when (v?.id) {
                    mGestureView.id -> onGestureOverlayTouch(event)
                }
            }
            return true
        }

        // <editor-fold defaultstate="collapsed" desc="Touch event of mGestureView">
        private fun onGestureOverlayTouch(event: MotionEvent?) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mSwiping) {
                        return
                    }
                    mPressDownTime = System.currentTimeMillis()
                    mStartX = event.x
                    mStartY = event.y
                    startLongPressingTimer(event.x, event.y)
                    mGestureType = GESTURE_PRESSING
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentX = event.x
                    val currentY = event.y
                    val startX = mStartX!!
                    val startY = mStartY!!
                    // 只有超过阈值才认为是拖动
                    if ((currentY - startY).absoluteValue >= gestureSwipingThresholdVertical
                        && mGestureType != GESTURE_VERTICAL
                    ) {
                        mGestureType = GESTURE_VERTICAL
                        CoroutineScope(Dispatchers.Default).launch {
                            _gestureListener.onValidVerticalSwiping(startY, currentY)
                        }
                    } else if ((currentX - startX).absoluteValue >= gestureSwipingThresholdHorizontal
                        && mGestureType != GESTURE_HORIZONTAL
                    ) {
                        mGestureType = GESTURE_HORIZONTAL
                        CoroutineScope(Dispatchers.Default).launch {
                            _gestureListener.onValidHorizontalSwiping(startX, currentX)
                        }
                    } else if (mGestureType == GESTURE_PRESSING) {
                        canceLongPressinglTimer()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val currentX = event.x
                    val currentY = event.y
                    canceLongPressinglTimer()
                    if (System.currentTimeMillis() - mPressDownTime >= longPressingThreshold) {
                        when (mGestureType) {
                            GESTURE_VERTICAL ->
                                _gestureListener.onSwipe(mStartY!!, currentY, GESTURE_VERTICAL)

                            GESTURE_HORIZONTAL ->
                                _gestureListener.onSwipe(mStartX!!, currentX, GESTURE_HORIZONTAL)

                            GESTURE_PRESSING ->
                                _gestureListener.onLongPressingReleased(currentX, currentY)
                        }
                        return
                    }
                }
            }
            mGestureDetector.onTouchEvent(event)
        }
        // </editor-fold>
    }

    private inner class InnerGestureListener : OnGestureListener {
        override fun onDoubleClicked(): Boolean {
            Log.d(TAG, "[${VideoPlayer.hashCode()}] GestureView DoubleClicked")
            if (mGestureListener?.onDoubleClicked() != true) {
                mVideoView.startPause()
            }
            return true
        }

        override fun onSingleClicked() {
            Log.d(TAG, "[${VideoPlayer.hashCode()}] GestureView SingleClicked")
        }

        override fun onValidVerticalSwiping(startY: Float, endY: Float) {
            Log.d(TAG, "[${VideoPlayer.hashCode()}] GestureView VerticalSwiping")
            mGestureListener?.onValidVerticalSwiping(startY, endY)
        }

        override fun onValidHorizontalSwiping(startX: Float, endX: Float) {
            Log.d(TAG, "[${VideoPlayer.hashCode()}] GestureView Horizontal Swiping")
        }

        override fun onSwipe(start: Float, end: Float, gestureType: Int) {
            Log.d(TAG, "[${VideoPlayer.hashCode()}] GestureView Swipe Finished")
        }

        override fun onLongPressingStart(positionX: Float, positionY: Float) {
            Log.d(TAG, "[${VideoPlayer.hashCode()}] LongPressingStarted")
        }

        override fun onLongPressingReleased(positionX: Float, positionY: Float) {
            Log.d(TAG, "[${VideoPlayer.hashCode()}] GestureView LongPressingReleased")
        }

        override fun onSecondPressing() {
            CoroutineScope(Dispatchers.Default).launch {
                _gestureListener.onSecondPressing()
            }
        }
    }

    private inner class InnerDoubleTapListener :
        GestureDetector.OnDoubleTapListener {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            _gestureListener.onSingleClicked()
            return false
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
            if (e?.action == MotionEvent.ACTION_UP) {
                CoroutineScope(Dispatchers.Default).launch {
                    _gestureListener.onDoubleClicked()
                }
            } else if (e?.action == MotionEvent.ACTION_DOWN) {
                _gestureListener.onSecondPressing()
            }
            return true
        }
    }
}