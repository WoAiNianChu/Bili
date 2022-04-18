package xyz.keriteal.bili.models

import com.google.gson.annotations.SerializedName

data class BiliAccountInfo(
    val name: String,
    val sex: String,
    val face: String,
    val sign: String,
    val rank: Int,
    val level: Int,
    val joinTime: Int,
    val coins: Int
) {
    data class BiliVipInfo(
        val type: Int,
        val state: Int,
        @SerializedName("due_date") val dueDate: Int
    )

    data class BiliVipLabel(
        val path: String,
        val text: String,
        val textColor: String
    )
}