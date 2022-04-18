package xyz.keriteal.bili.constants

object VideoConstants {
    /**
     * 视频详情
     */
    const val DETAIL = BaseConstants.APP_BASE + "/bilibili.app.view.v1.View/View"

    /**
     * 在线人数
     */
    const val ONLINE = BaseConstants.APP_BASE + "/x/v2/view/video/online"
    const val PLAY_INFORMATION = BaseConstants.API_BASE + "/x/player/playurl"
    const val PLAY_CONFIG = BaseConstants.APP_BASE + "/bilibili.app.playurl.v1.PlayURL/PlayConf"
    const val DANMAKU_METADATA = BaseConstants.GRPC_BASE + "/bilibili.community.service.dm.v1.DM/DmView"
    const val DANMAKU_SEGMENT = BaseConstants.GRPC_BASE + "/bilibili.community.service.dm.v1.DM/DmSegMobile"
    const val PROGRESS = BaseConstants.API_BASE + "/x/v2/history/report"
    const val LIKE = BaseConstants.APP_BASE + "/x/v2/view/like"
    const val COIN = BaseConstants.APP_BASE + "/x/v2/view/coin/add"
    const val FAVORITE = BaseConstants.API_BASE + "/x/v3/fav/resource/deal"
    const val TRIPLE = BaseConstants.APP_BASE + "/x/v2/view/like/triple"
    const val DANMAKU_SEND = BaseConstants.API_BASE + "/x/v2/dm/post"
    const val SUBTITLE = BaseConstants.API_BASE + "/x/player.so"
    const val INTERACTION_EDGE = BaseConstants.API_BASE + "/x/stein/edgeinfo_v2"

    /**
     * 视频参数
     */
    const val STAT = BaseConstants.API_BASE + "/x/web-interface/archive/stat"
}