package xyz.keriteal.bili.service

import retrofit2.Call
import retrofit2.http.*
import xyz.keriteal.bili.constants.PassportConstants
import xyz.keriteal.bili.models.BiliDataModel
import xyz.keriteal.bili.models.BiliEncryptedPassword
import xyz.keriteal.bili.models.TokenInfo
import xyz.keriteal.bili.utils.RetrofitFactory

interface PassportService {
    companion object {
        val instance by lazy {
            RetrofitFactory.getService(PassportService::class.java)
        }
    }

    @GET(PassportConstants.CHECK_TOKEN)
    fun checkToken(@QueryMap() params: Map<String, String>)

    @GET(PassportConstants.SSO)
    fun sso(@QueryMap() params: Map<String, String>): Call<BiliDataModel<TokenInfo>>

    @POST(PassportConstants.PASSWORD_ENCRYPT)
    @FormUrlEncoded
    fun encryptPassword(@FieldMap params: Map<String, String>): Call<BiliDataModel<BiliEncryptedPassword>>

    @POST(PassportConstants.LOGIN)
    @FormUrlEncoded
    fun login(@FieldMap params: Map<String, String>) : Call<String>
}