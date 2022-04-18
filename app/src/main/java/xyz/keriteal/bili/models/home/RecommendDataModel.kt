package xyz.keriteal.bili.models.home

data class RecommendDataModel(
    val items: List<RecommendCardResponse>,
    val config: RecommendConfig
)