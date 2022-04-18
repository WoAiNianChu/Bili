package xyz.keriteal.bili.models

data class BiliMask(
    val avatar: BiliAvatar
) {
    data class BiliAvatar(
        val cover: String
    )
}
