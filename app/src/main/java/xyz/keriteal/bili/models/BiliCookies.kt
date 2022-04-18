package xyz.keriteal.bili.models

data class BiliCookies(
    val name: String,
    val value: String,
    val httpOnly: Int,
    val expires: Int
)
