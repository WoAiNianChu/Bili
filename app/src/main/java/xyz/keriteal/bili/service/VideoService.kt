package xyz.keriteal.bili.service

import retrofit2.http.GET
import retrofit2.http.QueryMap
import xyz.keriteal.bili.constants.VideoConstants
import xyz.keriteal.bili.models.BiliDataModel
import xyz.keriteal.bili.models.video.BiliVideo
import xyz.keriteal.bili.utils.RetrofitFactory

interface VideoService {
    companion object {
        val instance by lazy {
            RetrofitFactory.getService(VideoService::class.java)
        }
    }

    @GET(VideoConstants.PLAY_INFORMATION)
    suspend fun getVideoInformation(@QueryMap queryMap: Map<String, String>): BiliDataModel<BiliVideo>
}