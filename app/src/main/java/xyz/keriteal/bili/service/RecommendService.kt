package xyz.keriteal.bili.service

import retrofit2.http.GET
import retrofit2.http.QueryMap
import xyz.keriteal.bili.constants.HomeConstants
import xyz.keriteal.bili.models.BiliDataModel
import xyz.keriteal.bili.models.home.RecommendDataModel
import xyz.keriteal.bili.utils.RetrofitFactory

interface RecommendService {
    companion object {
        val instance by lazy {
            RetrofitFactory.getService(RecommendService::class.java)
        }
    }

    @GET(HomeConstants.RECOMMEND_API)
    suspend fun getRecommend(
        @QueryMap parameterMap: Map<String, String>
    ): BiliDataModel<RecommendDataModel>
}