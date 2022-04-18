package xyz.keriteal.bili.utils

import cn.hutool.crypto.asymmetric.KeyType
import cn.hutool.crypto.asymmetric.RSA
import org.apache.commons.lang3.StringUtils
import xyz.keriteal.bili.constants.ServiceConstants
import xyz.keriteal.bili.enums.RequestClientType
import xyz.keriteal.bili.exception.UnauthorizedException
import xyz.keriteal.bili.models.TokenInfo
import xyz.keriteal.bili.service.PassportService
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*

object TokenUtils {
    var tokenInfo: TokenInfo? = null
    var lastAuthorizeTime: OffsetDateTime? = null

    /**
     * 完成 IsTokenValidAsync
     */
    fun validateToken(isNetworkVerify: Boolean = false): Boolean {
        val isLocalValid = tokenInfo != null &&
                tokenInfo!!.accessToken.isNotBlank() &&
                lastAuthorizeTime != null &&
                ChronoUnit.SECONDS.between(
                    OffsetDateTime.now(),
                    lastAuthorizeTime
                ) < tokenInfo!!.expiresIn
        return if (isLocalValid && isNetworkVerify) {
            networkValidateToken()
        } else {
            isLocalValid
        }
    }

    /**
     * AuthorizeProvider.Extension.cs::NetworkVerifyTokenAsync
     */
    fun networkValidateToken(): Boolean {
        if (tokenInfo!!.accessToken.isNotEmpty()) {
            return try {
                val queryParameters =
                    mapOf(ServiceConstants.Query.ACCESS_TOKEN to tokenInfo!!.accessToken)
                PassportService.instance
                    .checkToken(queryParameters)
                true
            } catch (_: Exception) {
                false
            }
        }
        return false
    }

    private fun refreshToken(): TokenInfo? {
        if (StringUtils.isNotBlank(tokenInfo?.refreshToken)) {
            val queryParameter = mutableMapOf(
                ServiceConstants.Query.ACCESS_TOKEN to tokenInfo!!.accessToken,
                ServiceConstants.Query.REFRESH_TOKEN to tokenInfo!!.refreshToken
            )
            return PassportService.instance
                .sso(queryParameter)
                .execute()
                .body()!!
                .data
        }
        return null
    }

    fun getToken(): String {
        if (tokenInfo != null) {
            if (validateToken()) {
                return tokenInfo!!.accessToken
            } else {
                val tokenInfo = refreshToken()
                if (tokenInfo != null) {
                    this.tokenInfo = tokenInfo
                    this.lastAuthorizeTime = OffsetDateTime.now()
                    return tokenInfo.accessToken
                }
            }
        }
        throw UnauthorizedException()
    }

    /**
     * https://www.linkle.top/2020/02/24/Bilibili%E7%99%BB%E5%BD%95%E6%B5%81%E7%A8%8B%E5%88%86%E6%9E%90/
     * AuthorizeProvider.Extension.cs::EncryptedPasswordAsync
     */
    fun encryptedPassword(password: String): String {
        val parameterMap =
            RetrofitUtils.generateAuthorizedQueryMap(mapOf(), RequestClientType.ANDROID)
        val resp = PassportService.instance
            .encryptPassword(parameterMap)
            .execute()
            .body()!!.data
        val hash = resp.hash
        val key = resp.key
        hash.chainPrint("Hash: ")
        key.chainPrint("Key: ")
        val pubKey = key.replaceFirst("-----BEGIN PUBLIC KEY-----", "")
            .replaceFirst("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
            .trim().chainPrint("PubKey: ")
        val rsa = RSA(null, pubKey)
        val encryptedData = rsa.encrypt("$hash$password", StandardCharsets.UTF_8, KeyType.PublicKey)
        return Base64.getEncoder().encodeToString(encryptedData)
    }
}