package xyz.keriteal.bili.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.util.Log
import android.widget.Toast
import androidx.collection.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import xyz.keriteal.bili.BiliApplication
import xyz.keriteal.bili.R
import xyz.keriteal.bili.utils.RetrofitUtils.md5
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URL
import kotlin.math.max

object BitmapUtils {
    private val mCache: LruCache<String, Bitmap> by lazy {
        LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 8).toInt())
    }

    /**
     * 从网络加载并保存到硬盘
     */
    private fun loadFromNetwork(
        context: Context,
        url: String,
        width: Int,
        height: Int,
        saveToDisk: Boolean = true,
        savedDirectory: String = "",
        savedName: String? = null
    ): Bitmap {
        val uri = URL(url)
        val conn = uri.openConnection()
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        var bitmap = BitmapFactory.decodeStream(conn.getInputStream())
        // 存到硬盘
        if (saveToDisk) {
            val fileName = savedName ?: FilenameUtils.getName(uri.file)
            saveToDisk(context, savedDirectory, fileName, bitmap!!)
        }
        if (width > 0 && height > 0) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height)
        }
        // 存到内存
        putToMemory(savedName ?: url.md5(), bitmap)
        return bitmap
    }

    /**
     * 从硬盘加载
     */
    private fun loadFromDisk(
        context: Context,
        url: String,
        width: Int,
        height: Int,
        saveToDisk: Boolean = true,
        savedDirectory: String = "",
        savedName: String? = null
    ): Bitmap {
        val fileName = savedName ?: FilenameUtils.getName(URL(url).path)
        var bitmap = if (saveToDisk) {
            val file = File(context.cacheDir, savedDirectory + "/" + (savedName ?: fileName))
            if (!file.exists()) {
                return loadFromNetwork(
                    context,
                    url,
                    width,
                    height,
                    saveToDisk,
                    savedDirectory,
                    savedName
                )
            }
            val fis = FileInputStream(file)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            BitmapFactory.decodeFileDescriptor(fis.fd) ?: loadFromNetwork(
                context,
                url,
                width,
                height,
                saveToDisk,
                savedDirectory,
                savedName
            )
        } else {
            loadFromNetwork(context, url, width, height, saveToDisk, savedDirectory, savedName)
        }
        if (width > 0 && height > 0) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height)
        }
        putToMemory(savedName ?: url.md5(), bitmap)
        return bitmap
    }

    private fun putToMemory(key: String, bitmap: Bitmap) {
        mCache.put(key, bitmap)
    }

    /**
     * 从内存加载
     */
    suspend fun loadBitmap(
        context: Context,
        url: String,
        fallbackResourceId: Int,
        width: Int,
        height: Int,
        saveToDisk: Boolean = true,
        savedDirectory: String = "",
        savedName: String? = null
    ): Bitmap =
        withContext(Dispatchers.IO) {
            try {
                val securedUrl = url.replace("http://", "https://")
                val bitmap = mCache.get(savedName ?: securedUrl.md5())
                bitmap ?: loadFromDisk(
                    context,
                    securedUrl,
                    width,
                    height,
                    saveToDisk,
                    savedDirectory,
                    savedName
                )
            } catch (e: Exception) {
                Log.e("loadBitmap", e.message ?: "No message")
                Log.e("loadBitmap", e.stackTraceToString())
                BitmapFactory.decodeResource(context.resources, fallbackResourceId)
            }
        }

    private fun saveToDisk(context: Context, directory: String, fileName: String, bitmap: Bitmap) {
        val dir = File(context.cacheDir, directory)
        if (!dir.exists()) {
            dir.mkdir()
        }
        val file = File(dir, fileName)
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, fos)
        fos.flush()
        fos.close()
    }

    fun scaleBitmap(bitmap: Bitmap, viewWidth: Int, viewHeight: Int): Bitmap {
        val originWidth = bitmap.width
        val originHeight = bitmap.height
        val scaleHeight = viewHeight / originHeight * 1f
        val scaleWidth = viewWidth / originWidth * 1f
        val scale = max(scaleHeight, scaleWidth)
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}