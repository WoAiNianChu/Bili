package xyz.keriteal.videoplayer.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable
import java.lang.Exception

object KLog {
    var logD: Boolean = true
    var logE: Boolean = false


    fun tryLog(
        tag: String,
        subTag: Int,
        message: String,
        throws: Boolean = false,
        body: () -> Unit
    ) {
        try {
            body.invoke()
        } catch (e: Exception) {
            Log.e(tag, "[$subTag] $message", e)
            if (throws) {
                throw e
            }
        }
    }

    fun toastShort(context: Context, message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun toastLong(context: Context, message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_LONG)
                .show()
        }
    }
}

fun <T : Serializable> logd(tag: String, subTag: T, message: String) {
    if (KLog.logD) {
        Log.d(tag, "[$subTag] $message")
    }
}

fun <T : Serializable> loge(tag: String, subTag: T, message: String, error: Exception? = null) {
    Log.e(tag, "[$subTag] $message", error)
}