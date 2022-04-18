package xyz.keriteal.bili.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.preference.PreferenceManager
import java.io.*

object SPUtils {
    const val fileName = ""

    fun put(context: Context, key: String, obj: Any?) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        when (obj) {
            null -> editor.remove(key)
            is String -> editor.putString(key, obj)
            is Int -> editor.putInt(key, obj)
            is Boolean -> editor.putBoolean(key, obj)
            is Float -> editor.putFloat(key, obj)
            is Long -> editor.putLong(key, obj)
        }
        editor.apply()
    }

    fun <T> get(context: Context, key: String, defaultValue: T): T? {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return when (defaultValue) {
            is String -> sp.getString(key, defaultValue)
            is Int -> sp.getInt(key, defaultValue)
            is Boolean -> sp.getBoolean(key, defaultValue)
            is Float -> sp.getFloat(key, defaultValue)
            else -> null
        } as T
    }

    fun putSerializable(context: Context, key: String, obj: Serializable) {
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(obj)
        val objectStr = String(Base64.encode(baos.toByteArray(), Base64.DEFAULT))
        baos.close()
        oos.close()
        put(context, key, objectStr)
    }

    fun <T : Serializable> getSerializable(context: Context, key: String, defaultObj: T): T? {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val wordBase = sp.getString(key, "")
        if (wordBase == "") {
            return null
        }
        val objBytes = Base64.decode(wordBase?.toByteArray(), Base64.DEFAULT)
        val bais = ByteArrayInputStream(objBytes)
        val ois = ObjectInputStream(bais)
        val obj = ois.readObject() as T
        bais.close()
        ois.close()
        return obj
    }

    fun contains(context: Context, key: String): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).contains(key)

    fun remove(context: Context, key: String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.remove(key)
        editor.apply()
    }
}
