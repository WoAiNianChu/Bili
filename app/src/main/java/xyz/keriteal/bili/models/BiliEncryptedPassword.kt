package xyz.keriteal.bili.models

data class BiliEncryptedPassword(
    val hash: String,
    val key: String
)