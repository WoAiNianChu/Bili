package xyz.keriteal.videoplayer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.keriteal.videoplayer.databinding.VideoPlayerStdBinding

class StandardVideoPlayer : VideoPlayer {
    companion object {
        const val TAG = "StandardVideoPlayer"

        const val GESTURE_NONE = 0
        const val GESTURE_PROGRESS = 1
        const val GESTURE_BRIGHTNESS = 2
        const val GESTURE_VOLUME = 3
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private val mClickListener: OnClickListener = InnerClickListener()

    private var mSeeking: Boolean = false
    private var mGestureType: Int = 0

    private var binding: VideoPlayerStdBinding =
        VideoPlayerStdBinding.inflate(LayoutInflater.from(context), this)
    override var mVideoView: EasyVideoView = binding.videoCore
    override var mGestureView: View = binding.gestureOverlay

    init {
        mLockStateListener = InnerLockListener()
        mGestureListener = InnerGestureListener()

        binding.gestureOverlay.setOnTouchListener(GestureLayoutTouchTouchListener())
        binding.replayBtn.setOnClickListener(mClickListener)
        binding.retryBtn.setOnClickListener(mClickListener)
        binding.startPauseBtn.setOnClickListener(mClickListener)
    }

    override fun showControls() {
        CoroutineScope(Dispatchers.Main).launch {
            if (locked) {
                binding.lockBtn.visibility = VISIBLE
                binding.videoTopContainer.visibility = INVISIBLE
                binding.videoBottomContainer.visibility = INVISIBLE
            } else {
                binding.videoTopContainer.visibility = VISIBLE
                binding.videoBottomContainer.visibility = VISIBLE
                mControlsState = CONTROLS_SHOWING
            }
        }
    }

    override fun setup(
        dataSource: VideoDataSource,
        mediaInterfaceClass: Class<out IMediaBackend<out MediaOptions>>
    ) {
        super.setup(dataSource, mediaInterfaceClass)
        binding.videoTitleView.text = dataSource.title
    }

    override fun dismissControls() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.videoTopContainer.visibility = INVISIBLE
            binding.videoBottomContainer.visibility = INVISIBLE
            binding.videoLeftContainer.visibility = INVISIBLE
            binding.startPauseBtn.visibility = INVISIBLE
            binding.retryBtn.visibility = INVISIBLE
            binding.replayBtn.visibility = INVISIBLE
            mControlsState = CONTROLS_HIDING
        }
    }

    private inner class GestureLayoutTouchTouchListener : VideoGestureTouchListener() {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (super.onTouch(v, event)) {
                return true
            }
            if (!locked) {
                when (v?.id) {
                    R.id.video_progress_seek_bar -> onVideoProgressSeekBarTouch(event)
                    R.id.progress_seek_cancel_view -> onCancelSeekingView(event)
                }
                if (mSeeking) {
                    onSeeking(event)
                }
            } else {
                onLocked(event)
            }
            return false
        }

        /**
         * 触摸的视图为进度条
         */
        private fun onVideoProgressSeekBarTouch(event: MotionEvent?) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    mSeeking = true
                    cancelDismissControlsTimer()
                }
            }
        }

        /**
         * 正在通过进度条调整时间
         */
        private fun onSeeking(event: MotionEvent?) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    cancelDismissControlsTimer()
                }
                MotionEvent.ACTION_UP -> {
                    mSeeking = false
                    startDismissControlsTimer()
                }
            }
        }

        /**
         * 正在锁定状态
         */
        private fun onLocked(event: MotionEvent?) {

        }

        private fun onCancelSeekingView(event: MotionEvent?) {
            when (event?.action) {

            }
        }
    }

    private inner class InnerClickListener : OnClickListener {
        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.gesture_overlay -> {
                    mGestureListener?.onSingleClicked()
                }
                R.id.replay_btn -> {

                }
                R.id.retry_btn -> {

                }
                R.id.screenshot_btn -> {

                }
                R.id.start_pause_btn -> {
                    mVideoView.startPause()
                }
                R.id.lock_btn -> {
                    locked = !locked
                    if (locked) {
                        mLockStateListener?.onLocked()
                    } else {
                        mLockStateListener?.onUnlocked()
                    }
                }
            }
        }
    }

    private inner class InnerGestureListener : OnGestureListener {
        override fun onLongPressingStart(positionX: Float, positionY: Float) {
            mVideoView.videoSpeed = 3f
        }

        override fun onLongPressingReleased(positionX: Float, positionY: Float) {
            mVideoView.videoSpeed = 1f
        }

        override fun onSecondPressing() {
            if (locked) {
                mVideoView.startPause()
            }
        }

        override fun onDoubleClicked(): Boolean {
            return false
        }

        override fun onSingleClicked() {
            switchControls()
        }

        override fun onValidVerticalSwiping(startY: Float, endY: Float) {
            TODO("Not yet implemented")
        }

        override fun onValidHorizontalSwiping(startX: Float, endX: Float) {
            TODO("Not yet implemented")
        }

        override fun onSwipe(start: Float, end: Float, gestureType: Int) {
            TODO("Not yet implemented")
        }
    }

    private inner class InnerLockListener : OnLockListener {
        override fun onLocked() {
            dismissControls()
        }

        override fun onUnlocked() {
            showControls()
        }
    }
}