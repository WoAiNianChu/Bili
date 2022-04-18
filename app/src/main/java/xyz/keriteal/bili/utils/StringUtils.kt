package xyz.keriteal.bili.utils

import okio.ByteString.Companion.decodeBase64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

fun String.rsa(publicKeyStr: String): String {
    val keyFactory = KeyFactory.getInstance("RSA")
    val encodedKeySpec = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyStr))
    val publicKey = keyFactory.generatePublic(encodedKeySpec)

    val cipher = Cipher.getInstance("RSA")
    publicKey.toString().chainPrint()
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val encrypt = cipher.doFinal(this.toByteArray(StandardCharsets.UTF_8))
    return String(Base64.getEncoder().encode(encrypt))
}

fun String.chainPrint(prepend: String = "", append: String = ""): String {
    println("$prepend$this$append")
    return this
}