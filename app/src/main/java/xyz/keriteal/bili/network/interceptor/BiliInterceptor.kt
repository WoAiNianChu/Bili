package xyz.keriteal.bili.network.interceptor

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http.HttpMethod
import retrofit2.Retrofit
import xyz.keriteal.bili.constants.ServiceConstants
import xyz.keriteal.bili.utils.RetrofitUtils
import java.util.logging.Logger

class BiliInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val request = originRequest.newBuilder()
            .header("Accept", "*/*")
            .header(
                ServiceConstants.Headers.USER_AGENT,
                "Mozilla/5.0 BiliDroid/5.0.0 (bbcallen@gmail.com)"
            )
            .header("Accept-Encoding", "gzip, deflate")
            .build()
        println(request.url)
        return chain.proceed(request)
    }
}