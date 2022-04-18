package xyz.keriteal.bili.utils

import android.accounts.OperationCanceledException
import xyz.keriteal.bili.constants.ServiceConstants
import xyz.keriteal.bili.enums.RequestClientType
import java.security.MessageDigest

fun Map<String, String>.generateAuthorizedQueryMap(
    requestClientType: RequestClientType,
    needToken: Boolean = false
): MutableMap<String, String> {
    return RetrofitUtils.generateAuthorizedQueryMap(this, requestClientType, needToken)
}

object RetrofitUtils {
    fun generateAuthorizedQueryMap(
        queryParameters: Map<String, String>,
        requestClientType: RequestClientType,
        needToken: Boolean = false
    ): MutableMap<String, String> {
        val clonedQueryParameters = queryParameters.toMutableMap()
        clonedQueryParameters["build"] = ServiceConstants.buildNumber
        when (requestClientType) {
            RequestClientType.IOS -> {
                clonedQueryParameters[ServiceConstants.Query.APP_KEY] =
                    ServiceConstants.Keys.IOS_KEY
                clonedQueryParameters[ServiceConstants.Query.MOBILE_APP] = "iphone"
                clonedQueryParameters[ServiceConstants.Query.PLATFORM] = "ios"
                clonedQueryParameters[ServiceConstants.Query.TIMESTAMP] =
                    (System.currentTimeMillis() / 1000).toString()
            }
            RequestClientType.ANDROID -> {
                clonedQueryParameters[ServiceConstants.Query.APP_KEY] =
                    ServiceConstants.Keys.ANDROID_KEY
                clonedQueryParameters[ServiceConstants.Query.MOBILE_APP] = "android"
                clonedQueryParameters[ServiceConstants.Query.PLATFORM] = "android"
                clonedQueryParameters[ServiceConstants.Query.TIMESTAMP] =
                    (System.currentTimeMillis() / 1000).toString()
            }
            RequestClientType.WEB -> {
                clonedQueryParameters[ServiceConstants.Query.APP_KEY] =
                    ServiceConstants.Keys.WEB_KEY
                clonedQueryParameters[ServiceConstants.Query.TIMESTAMP] =
                    (System.currentTimeMillis() / 1000).toString()
            }
            RequestClientType.LOGIN -> {
                clonedQueryParameters[ServiceConstants.Query.APP_KEY] =
                    ServiceConstants.Keys.LOGIN_KEY
                clonedQueryParameters[ServiceConstants.Query.TIMESTAMP] =
                    (System.currentTimeMillis() / 1000).toString()
            }
        }
        var token = ""
        if (TokenUtils.validateToken()) {
            token = TokenUtils.tokenInfo!!.accessToken
        } else if (needToken) {
            token = TokenUtils.getToken()
        }
        if (token.isNotEmpty()) {
            clonedQueryParameters[ServiceConstants.Query.ACCESS_KEY] = token
        } else if (needToken) {
            throw OperationCanceledException();
        }
        val sign = generateSign(clonedQueryParameters)
        clonedQueryParameters[ServiceConstants.Query.SIGN] = sign
        return clonedQueryParameters
    }

    fun generateAuthorizedQueryString(
        queryParameters: Map<String, String>,
        requestClientType: RequestClientType,
        needToken: Boolean = false
    ): String {
        val parameters = generateAuthorizedQueryMap(queryParameters, requestClientType, needToken)
        val queryList = parameters.toList().map { "${it.first}=${it.second}" }.sorted()
        return queryList.joinToString("&")
    }

    private fun generateSign(queryParameters: Map<String, String>): String {
        val queryList = queryParameters.toList().map { "${it.first}=${it.second}" }
            .sorted()
        val secret = when (queryParameters[ServiceConstants.Query.APP_KEY]) {
            ServiceConstants.Keys.IOS_KEY -> ServiceConstants.Keys.IOS_SECRET
            ServiceConstants.Keys.ANDROID_KEY -> ServiceConstants.Keys.ANDROID_SECRET
            ServiceConstants.Keys.LOGIN_KEY -> ServiceConstants.Keys.LOGIN_SECRET
            else -> ServiceConstants.Keys.WEB_SECRET
        }

        val query = queryList.joinToString("&")
        val signedQuery = query + secret
        println("signedQuery:${signedQuery}")
        return signedQuery.md5()
    }

    fun String.md5(): String {
        val strBytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        return strBytes.joinToString("") { "%02x".format(it) }
    }
}

