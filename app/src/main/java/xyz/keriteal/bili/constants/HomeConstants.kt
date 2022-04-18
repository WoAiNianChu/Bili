package xyz.keriteal.bili.constants

object HomeConstants {
    /**
     * 推荐视频
     */
    const val RECOMMEND_API = BaseConstants.APP_BASE + "/x/v2/feed/index"

    /**
     * 排行榜
     */
    const val RANKING_API = BaseConstants.API_BASE + "/x/web-interface/ranking/v2"

    /**
     * 排行榜 gRPC
     */
    const val RANKING_GRPC_API = BaseConstants.GRPC_BASE + "/bilibili.app.show.v1.Rank/RankRegion"
}