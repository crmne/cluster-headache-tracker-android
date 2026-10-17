package me.paolino.clusterheadachetracker

import android.content.Context
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

/**
 * The prefix the web app reads to recognise the app (`ClusterHeadacheTracker; platform=android; version=…; build=…;`).
 * Builds Google Play installed add `store=play;`: the web app then leaves updates to Google Play instead of offering
 * the GitHub APK, which Google Play forbids.
 */
object AppUserAgent {
    const val GOOGLE_PLAY_INSTALLER = "com.android.vending"

    fun prefix(context: Context): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return prefix(
            versionName = packageInfo.versionName,
            versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
            installer = installer(context),
        )
    }

    fun prefix(versionName: String?, versionCode: Long, installer: String?): String {
        val store = if (installer == GOOGLE_PLAY_INSTALLER) " store=play;" else ""
        return "ClusterHeadacheTracker; platform=android; version=$versionName; build=$versionCode;$store"
    }

    private fun installer(context: Context): String? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
    }.getOrNull()
}
