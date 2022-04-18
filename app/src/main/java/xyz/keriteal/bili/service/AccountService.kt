package xyz.keriteal.bili.service

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap
import xyz.keriteal.bili.constants.AccountConstants
import xyz.keriteal.bili.models.BiliAccountInfo
import xyz.keriteal.bili.models.BiliDataModel
import xyz.keriteal.bili.utils.RetrofitFactory

interface AccountService {
    companion object {
        val instance by lazy {
            RetrofitFactory.getService(AccountService::class.java)
        }
    }

    @GET(AccountConstants.ACCOUNT_INFO)
    suspend fun getSpaceInfo(@Query("mid") userId: String): BiliDataModel<BiliAccountInfo>

    @GET(AccountConstants.SPACE)
    fun getSpace(@QueryMap parameterMap: Map<String, String>): Call<String>

    @GET(AccountConstants.MULTIPLE_INFO + "?attributes[]=info")
    suspend fun getMultipleUserInfo(@Query("uids[]") uids: List<String>): BiliDataModel<JsonObject>
}