package xyz.keriteal.videoplayer

import android.content.Context
import android.os.HandlerThread
import android.view.Surface
import android.view.SurfaceHolder

interface IMediaBackend<OP : MediaOptions>
//    : TextureView.SurfaceTextureListener
{
    companion object {
        const val ERROR_INIT = 1

        const val STATE_ERROR = -2
        const val STATE_FINISHED = -1
        const val STATE_IDLE = 0
        const val STATE_INITIALIZED = 1
        const val STATE_PREPARED = 2
        const val STATE_STARTED = 3
        const val STATE_PAUSED = 4
        const val STATE_PLAY_COMPLETED = 5

        val mMediaHandlerThread: HandlerThread = HandlerThread("VIDEO_PLAYER")
    }

    fun prepare()
    fun prepare(mediaOptions: OP, context: Context? = null)

    fun start()
    fun pause()
    fun resume()
    fun startPauseResume()
    fun stop()
    fun reset()

    /**
     * 释放
     */
    fun release()

    fun isPaused(): Boolean
    fun isPlaying(): Boolean

    fun getCurrentPosition(): Long
    fun getBufferPercent(): Int
    fun getState(): Int

    /**
     * 视频总长度
     */
    fun getDuration(): Long


    ////////////////////////////////////////////////////////////////
    /**
     * 跳转到指定位置
     */
    fun seekTo(time: Long)


    /**
     * 设置音量
     */
    fun setVolume(leftVolume: Float, rightVolume: Float)

    /**
     * 设置速度
     */
    fun setSpeed(speed: Float)
    fun setDisplay(surfaceHolder: SurfaceHolder)
    fun setSurface(surface: Surface)

    fun setDataSource(source: VideoDataSource)
    fun getDataSource(): VideoDataSource?

    interface OnStateListener {
        companion object {
            const val TAG = "MediaBackend.OnStateListener"
        }

        /**
         * 手动进度加载完毕
         */
        fun onError()
        fun onFinished()
        fun onSeekComplete()
        fun onInfo(what: Int, extra: Int)
        fun onPrepared()
        fun onStarted()
        fun onPaused()
        fun onPlayComplated()

        /**
         * 视频尺寸变更
         */
        fun onVideoSizeChanged(videoWidth: Int, videoHeight: Int)

        /**
         * 缓存进度更新
         */
        fun onBufferingUpdate(percent: Int)
    }

    interface OnSpeedChangedListener {
        fun onSpeed(speed: Float)
    }

    interface OnStateChangedListener {
        fun onState(state: Int): Boolean
    }

    interface OnErrorListener {
        fun onError(error: Int)
    }

    interface OnFrameworkErrorListener {
        fun onError(error: Int, implError: Int)
    }

    interface OnSizeListener {
        fun onSizeChanged(width: Int, height: Int)
    }

    interface OnDataSourceListener {
        fun onDataSourceChanged(dataSource: VideoDataSource)
        fun onDataSourceKeyChanged(dataSource: VideoDataSource)
    }
}