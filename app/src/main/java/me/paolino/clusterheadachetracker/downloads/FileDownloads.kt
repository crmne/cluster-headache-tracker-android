package me.paolino.clusterheadachetracker.downloads

import android.app.Activity
import android.app.PendingIntent
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.chooser.ChooserAction
import android.util.Log
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import dev.hotwire.core.config.Hotwire
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.paolino.clusterheadachetracker.AppConfig
import me.paolino.clusterheadachetracker.R
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads files the WebView can't display (PDF reports, CSV exports) with the signed-in session's
 * cookies, then offers to open or share them. Files stay in the app's cache; nothing is written to
 * shared storage.
 */
object FileDownloads {
    private val downloadExtensions = setOf("pdf", "csv")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun isDownloadLocation(location: String): Boolean {
        val path = location.substringBefore('#').substringBefore('?')
        return path.substringAfterLast('/').substringAfterLast('.', "").lowercase() in downloadExtensions
    }

    /** Only the app's own server gets the session cookies. */
    fun isAppLocation(location: String): Boolean {
        val uri = location.toUri()
        val base = AppConfig.BASE_URL.toUri()
        return uri.scheme == base.scheme && uri.host == base.host && uri.port == base.port
    }

    fun download(
        activity: Activity,
        url: String,
        contentDisposition: String? = null,
        mimeType: String? = null,
        title: String? = null,
    ) {
        if (!isAppLocation(url)) {
            Toast.makeText(activity, R.string.download_failed, Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(activity, R.string.download_started, Toast.LENGTH_SHORT).show()

        scope.launch {
            val file = withContext(Dispatchers.IO) { fetch(activity, url, contentDisposition, mimeType) }
            if (activity.isFinishing || activity.isDestroyed) return@launch

            if (file == null) {
                Toast.makeText(activity, R.string.download_failed, Toast.LENGTH_LONG).show()
            } else {
                openOrShare(activity, file.file, file.mimeType, title)
            }
        }
    }

    private data class DownloadedFile(val file: File, val mimeType: String)

    private fun fetch(context: Context, url: String, contentDisposition: String?, mimeType: String?): DownloadedFile? {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            CookieManager.getInstance().getCookie(url)?.let { connection.setRequestProperty("Cookie", it) }
            connection.setRequestProperty("User-Agent", Hotwire.config.userAgentWithWebViewDefault(context))
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS

            if (connection.responseCode !in HTTP_SUCCESS) return null

            val type = mimeType?.takeUnless { it.isBlank() || it == GENERIC_MIME_TYPE }
                ?: connection.contentType?.substringBefore(';')?.trim()
                ?: guessMimeType(url)
            val name = URLUtil.guessFileName(
                url,
                contentDisposition ?: connection.getHeaderField("Content-Disposition"),
                type,
            )
            val file = File(downloadsDirectory(context), name)
            connection.inputStream.use { input -> file.outputStream().use { input.copyTo(it) } }
            DownloadedFile(file, type)
        } catch (e: IOException) {
            Log.w(TAG, "Download failed: $url", e)
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun openOrShare(activity: Activity, file: File, mimeType: String, title: String?) {
        val uri = FileProvider.getUriForFile(activity, authority(activity), file)

        val share = Intent(Intent.ACTION_SEND)
            .setType(mimeType)
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val view = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        share.clipData = ClipData.newRawUri(file.name, uri)
        val shareChooser = Intent.createChooser(share, null)

        if (activity.packageManager.queryIntentActivities(view, 0).isEmpty()) {
            activity.startActivity(shareChooser)
            return
        }

        val chooser = Intent.createChooser(view, title ?: file.name)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        chooser.clipData = ClipData.newRawUri(file.name, uri)
        addShareOption(activity, chooser, shareChooser)
        activity.startActivity(chooser)
    }

    /** Offers "Share" next to the PDF viewers: a native chooser action on Android 14+, a labeled entry before. */
    private fun addShareOption(activity: Activity, chooser: Intent, shareChooser: Intent) {
        val label = activity.getString(R.string.download_share)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                activity,
                0,
                shareChooser,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val icon = Icon.createWithResource(activity, R.drawable.ic_share)
            val action = ChooserAction.Builder(icon, label, pendingIntent).build()
            chooser.putExtra(Intent.EXTRA_CHOOSER_CUSTOM_ACTIONS, arrayOf(action))
        } else {
            val option = LabeledIntent(shareChooser, activity.packageName, label, R.drawable.ic_share)
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(option))
        }
    }

    private fun downloadsDirectory(context: Context) = File(context.cacheDir, DIRECTORY).apply { mkdirs() }

    private fun authority(context: Context) = "${context.packageName}.downloads"

    private fun guessMimeType(url: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: GENERIC_MIME_TYPE
    }

    private const val DIRECTORY = "downloads"
    private const val GENERIC_MIME_TYPE = "application/octet-stream"
    private const val TIMEOUT_MS = 30_000
    private const val TAG = "FileDownloads"
    private val HTTP_SUCCESS = HttpURLConnection.HTTP_OK until HttpURLConnection.HTTP_MULT_CHOICE
}
