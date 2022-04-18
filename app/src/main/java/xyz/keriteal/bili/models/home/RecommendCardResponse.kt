package xyz.keriteal.bili.models.home

import com.google.gson.annotations.SerializedName
import xyz.keriteal.bili.models.BiliMask

/**
 *
 */
data class RecommendCardResponse(
    val cardType: String,
    val cardGoto: String,
    val goto: String,
    val param: String,
    val cover: String,
    val title: String,
    val uri: String,
    val args: RecommendCardArgs,
    @SerializedName("player_args") val playerArgs: PlayerArgs,
    val mask: BiliMask?,
    @SerializedName("cover_left_text_2") val watched: String?,
    @SerializedName("cover_left_1_content_description") val watchedDescription: String?,
    @SerializedName("cover_left_text_3") val danmaku: String?,
    @SerializedName("cover_left_2_content_description") val danmakuDescription: String?,
    @SerializedName("cover_right_text") val length: String
) {
    data class RecommendCardArgs(
        @SerializedName("up_id") val upId: Int,
        @SerializedName("up_name") val upName: String,

        /**
         * 分区id
         */
        @SerializedName("rid") val partitionId: Int,
        @SerializedName("rname") val partitionName: String,

        /**
         * 子分区id
         */
        @SerializedName("tid") val subPartitionId: Int,
        @SerializedName("tname") val subPartitionName: String,

        @SerializedName("aid") val aid: Int
    )

    /**
     * 播放器参数
     */
    data class PlayerArgs(
        val aid: Int?,
        val cid: Int,
        val bid: String?,
        val type: String,
        /**
         * 视频时长
         */
        val duration: Int
    )
}
