package xyz.keriteal.bili.models

data class BiliDataModel<T>(
    val code:String,
    val message: String,
    val data: T
)
