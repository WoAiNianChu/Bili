package xyz.keriteal.bili.ui.player

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.keriteal.bili.constants.ServiceConstants
import xyz.keriteal.bili.databinding.ActivityPlayerBinding
import xyz.keriteal.bili.enums.RequestClientType
import xyz.keriteal.bili.service.VideoService
import xyz.keriteal.bili.utils.RetrofitUtils
import xyz.keriteal.bili.utils.ScreenRotateUtils
import xyz.keriteal.videoplayer.VideoDataSource
import xyz.keriteal.videoplayer.VideoPlayer
import xyz.keriteal.videoplayer.utils.logd
import java.net.URI

class PlayerActivity : AppCompatActivity() {
    companion object {
        const val TAG = "PlayerActivity"
    }

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var player: VideoPlayer
    var avid: Int = 0
    var bvid: String? = null
    var cid: Int = 0
    var qn: Int = 0
    var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        player = binding.player

        if (intent != null && intent.extras != null) {
            avid = intent.extras!!.getInt("avid")
            bvid = intent.extras!!.getString("bvid")
            cid = intent.extras!!.getInt("cid")
            qn = intent.extras!!.getInt("qn")
        }
//        ScreenRotateUtils.getInstance(this.applicationContext).setOrientationChangeListener(this)
        loadVideo()
    }

    private fun loadVideo() {
        val queryMap = mapOf<String, String>(
            "avid" to avid.toString(),
            "bvid" to (bvid ?: ""),
            "cid" to cid.toString(),
        )
        CoroutineScope(Dispatchers.Main).launch {
            val urls = VideoService.instance
                .getVideoInformation(
                    RetrofitUtils.generateAuthorizedQueryMap(
                        queryMap,
                        RequestClientType.WEB
                    )
                ).data.parts
            urls.forEach { v ->
                logd(TAG, hashCode(), "Url: ${v.url}")
            }
            val url = urls[0].url

            val dataSource = VideoDataSource(
                urls[0].url, "test title",
                headers = mapOf(
                    "Host" to URI.create(url).host + ":443",
                    "Cache-Control" to "no-cache",
                    "referer" to "https://www.bilibili.com",
                    "Connection" to "keep-alive",
                    ServiceConstants.Headers.USER_AGENT to ServiceConstants.HeaderDefaults.DEFAULT_USER_AGENT
                )
            )
            player.setup(dataSource)
        }
    }

    override fun onBackPressed() {
//        if (Jzvd.backPress()) {
//            return
//        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        ScreenRotateUtils.getInstance(this).start(this)
//        Jzvd.goOnPlayOnPause()
    }

    override fun onResume() {
        super.onResume()
        ScreenRotateUtils.getInstance(this).stop()
//        Jzvd.goOnPlayOnResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        ScreenRotateUtils.getInstance(this.applicationContext).setOrientationChangeListener(null)
    }
//
//    override fun orientationChange(orientation: Int) {
//        if (orientation in 45..315 && player.screen == Jzvd.SCREEN_NORMAL) {
//            changeScreenFullLandscape(ScreenRotateUtils.orientationDirection)
//        } else if ((orientation in 0..44 || orientation > 315) && player.screen == Jzvd.SCREEN_FULLSCREEN) {
//            changeScreenNormal()
//        }
//    }
//
//    private fun changeScreenFullLandscape(x: Float) {
//        //从竖屏状态进入横屏
//        if (player.screen != Jzvd.SCREEN_FULLSCREEN) {
//            if (System.currentTimeMillis() - Jzvd.lastAutoFullscreenTime > 2000) {
//                player.autoFullscreen(x)
//                Jzvd.lastAutoFullscreenTime = System.currentTimeMillis()
//            }
//        }
//    }
//
//    /**
//     * 竖屏并退出全屏
//     */
//    private fun changeScreenNormal() {
//        if (player.screen == Jzvd.SCREEN_FULLSCREEN) {
//            player.autoQuitFullscreen()
//        }
//    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}