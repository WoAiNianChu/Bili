package xyz.keriteal.bili

import cn.hutool.crypto.asymmetric.KeyType
import cn.hutool.crypto.asymmetric.RSA
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import xyz.keriteal.bili.enums.RequestClientType
import xyz.keriteal.bili.service.AccountService
import xyz.keriteal.bili.service.PassportService
import xyz.keriteal.bili.utils.*
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testHash() {
        val testStr = "this is a test string"
        val resultMd5 = MessageDigest.getInstance("MD5").digest(testStr.toByteArray())
            .joinToString("") { "%02x".format(it) }
        assertEquals("486eb65274adb86441072afa1e2289f3", resultMd5)
    }

    @Test
    fun testEncryptPassword() {
        val testPassword = "put your password here"
        println("encrypted password:" + TokenUtils.encryptedPassword(testPassword))
    }

    @Test
    fun testAccountInfo() {
        val queryMap = mapOf("vmid" to "16725605")
        print(
            RetrofitFactory.getService(AccountService::class.java)
                .getSpace(
                    RetrofitUtils.generateAuthorizedQueryMap(
                        queryMap,
                        RequestClientType.ANDROID
                    )
                )
                .execute().body()
        )
    }

    @Test
    fun testKey() {
        val parameterMap =
            RetrofitUtils.generateAuthorizedQueryMap(mapOf(), RequestClientType.ANDROID)
        val resp = PassportService.instance
            .encryptPassword(parameterMap)
            .execute()
            .body()!!.data
        val hash = resp.hash
        val key = resp.key
        println(key)
    }

    @Test
    fun testLogin() {
        val username = "18667210106"
        val password = "ER4kPmGqC6txAs"
        val geeType = "10"
        val encryptedPassword = TokenUtils.encryptedPassword(password)
        val queryMap = mutableMapOf<String, String>(
            "username" to URLEncoder.encode(username),
            "password" to URLEncoder.encode(encryptedPassword),
            "gee_type" to "10"
        )
        val authorizedQueryMap = RetrofitUtils.generateAuthorizedQueryMap(
            queryMap,
            RequestClientType.LOGIN
        )
        authorizedQueryMap["username"] = username
        authorizedQueryMap["password"] = encryptedPassword
        PassportService.instance.login(
            authorizedQueryMap
        ).execute().body()!!.chainPrint()
    }

    @Test
    fun testRSA() {
        val pubKey =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdnS/LflJjuj4M9V63SPe3YUx0uFD/oVoPRwG2p1HXNSp6x+h7ImVAns9Bx5aKKthTH6V70W0TAdreCwS7WyzakvnHu4zWhGX77JM7/dEgdU5LSK8sI7YLz4bKhCpjBQGXJiKj/3InDPidWzw6w53Ce207HUzrYgR71rM3/OfewwIDAQAB"
        val str = "1234abcd5678"
        val result =
            "i9YbxGll3KUQNsNF71NtXKCgZhZd7tpJjTIXW1sg3avxOv0tZ84UO9eheWwBTF4ptOW4tjXKxva7" +
                    "H2cRSkMknaZ0zGI7cGm/TANevR5cdYVdxa7IBIevDRvj+Lnklo8HQg0QgSqLAQeJ1d38lQkcXoXO" +
                    "i5Qdol2RFTkC7zIPKdc="

        val rsa = RSA(null, pubKey)
        val resultStr = rsa.encrypt(str, KeyType.PublicKey)
        Assert.assertEquals(result, String(Base64.getEncoder().encode(resultStr)))
    }
}
