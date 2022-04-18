package xyz.keriteal.bili.utils

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.keriteal.bili.network.interceptor.BiliInterceptor
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object RetrofitFactory {
    private var serviceMap = mutableMapOf<String, Any>()

    private val retrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor { message ->
                println("RetrofitLog: $message")
            }.setLevel(HttpLoggingInterceptor.Level.BODY))
            .addNetworkInterceptor(BiliInterceptor())
            .callTimeout(20, TimeUnit.SECONDS)
//            .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 8888)))
            .build()
        Retrofit.Builder()
            .baseUrl("https://api.bilibili.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private fun <T> create(clazz: Class<T>): T {
        return retrofit.create(clazz)
    }

    @SuppressWarnings("unchecked")
    fun <T> getService(clazz: Class<T>): T {
        val key = clazz.name
        if (serviceMap[clazz.name] == null) {
            serviceMap[key] = create(clazz) as Any
        }
        return serviceMap[key] as T
    }
}