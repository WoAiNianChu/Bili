package xyz.keriteal.bili.models.login

data class BiliCaptcha(
    val type: String,
    val token: String,
    val geetest: GeeTest
) {
    data class GeeTest(
        val challenge: String,
        val gt: String
    )
}
