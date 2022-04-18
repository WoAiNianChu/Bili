package xyz.keriteal.bili.models.home

data class RecommendCardDataModel(
    val coverUrl: String,
    val upName: String,
    val upAvatarUrl: String,
    val title: String,
    val watched: String?,
    val danmaku: String?,
    val avId: Int,
    val bvId: String,
    val cId: Int
)
