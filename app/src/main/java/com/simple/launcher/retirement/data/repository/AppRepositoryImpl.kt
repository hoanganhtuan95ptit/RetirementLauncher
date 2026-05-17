package com.simple.launcher.retirement.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import android.provider.Telephony
import android.util.Log
import com.google.gson.Gson
import com.simple.launcher.retirement.BuildConfig
import com.google.gson.reflect.TypeToken
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class AppRepositoryImpl(private val context: Context) : AppRepository {

    private val sharedPrefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_SELECTED_APPS = "selected_apps"
    }

    // Trigger để home data (app + contact) phát lại khi có thay đổi
    private val _dataTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }

    override fun homeDataFlow(): Flow<Unit> = _dataTrigger

    // In-memory cache cho getSelectedPackages() — tránh Gson deserialize mỗi lần đọc.
    // Được invalidate khi saveSelectedPackages() được gọi.
    private var cachedSelectedPackages: List<String>? = null

    // Cache kết quả isDefaultApp() — mỗi lần check tốn 6-7 binder IPC calls.
    // Default apps gần như không đổi trong một session; cache tránh gọi lại mỗi 500ms.
    private val defaultAppCache = HashMap<String, Boolean>(16)

    override fun getInstalledApps(): List<AppEntity> {
        val pm = context.packageManager
        val apps = mutableListOf<AppEntity>()
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        val allApps = pm.queryIntentActivities(i, 0)
        for (ri in allApps) {
            apps.add(
                AppEntity(
                    ri.loadLabel(pm).toString(),
                    ri.activityInfo.packageName,
                    ri.activityInfo.loadIcon(pm)
                )
            )
        }
        return apps.sortedBy { it.label.lowercase() }
    }

    override fun getCurrentApp(): AppEntity {
        val pm = context.packageManager
        val info = context.applicationInfo
        return AppEntity(
            label = info.loadLabel(pm).toString(),
            packageName = context.packageName,
            icon = info.loadIcon(pm)
        )
    }

    override fun getSelectedPackages(): List<String> {
        // Trả về cache nếu đã có, tránh Gson.fromJson() mỗi lần gọi
        cachedSelectedPackages?.let { return it }

        val result = when (val data = sharedPrefs.all[KEY_SELECTED_APPS]) {
            is String -> try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(data, type)
            } catch (_: Exception) {
                emptyList()
            }
            is Set<*> -> data.filterIsInstance<String>()
            else -> emptyList()
        }
        cachedSelectedPackages = result
        return result
    }

    override fun saveSelectedPackages(packages: List<String>) {
        val json = gson.toJson(packages)
        sharedPrefs.edit().putString(KEY_SELECTED_APPS, json).apply()
        cachedSelectedPackages = packages  // cập nhật cache ngay, không cần đọc lại
        _dataTrigger.tryEmit(Unit)
    }

    override fun isDefaultApp(packageName: String): Boolean {
        // Trả về cache ngay nếu đã kiểm tra trước đó — tránh 6-7 binder IPC calls mỗi 500ms
        defaultAppCache[packageName]?.let { return it }

        val result = resolveIsDefaultApp(packageName)
        defaultAppCache[packageName] = result
        return result
    }

    private fun resolveIsDefaultApp(packageName: String): Boolean {
        val pm = context.packageManager
        val tag = "DefaultAppCheck"

        if (BuildConfig.DEBUG) Log.d(tag, "--- Checking: $packageName ---")

        // Default Launcher
        try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PackageManager.MATCH_ALL
            } else {
                PackageManager.MATCH_DEFAULT_ONLY
            }
            val launcherPkg = pm.resolveActivity(intent, flags)?.activityInfo?.packageName
            if (launcherPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Dialer
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            val dialerPkg = telecomManager?.defaultDialerPackage
            if (dialerPkg == packageName) return true

            // sharedUserId check: Samsung tách dialer + incallui thành 2 package
            // nhưng cùng sharedUserId → đều thuộc nhóm "phone default"
            if (dialerPkg != null) {
                try {
                    val dialerSharedUid = pm.getPackageInfo(dialerPkg, 0).sharedUserId
                    val targetSharedUid = pm.getPackageInfo(packageName, 0).sharedUserId
                    if (dialerSharedUid != null && dialerSharedUid == targetSharedUid) return true
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        // In-Call UI
        try {
            val inCallIntent = Intent("android.telecom.InCallService")
            val inCallPackages = pm.queryIntentServices(inCallIntent, PackageManager.MATCH_ALL)
                .map { it.serviceInfo.packageName }
            if (inCallPackages.contains(packageName)) return true
        } catch (_: Exception) {}

        // Phone process check (android.uid.phone)
        try {
            val sharedUid = pm.getPackageInfo(packageName, 0).sharedUserId
            if (sharedUid == "android.uid.phone") return true
        } catch (_: Exception) {}

        // Default SMS
        try {
            if (Telephony.Sms.getDefaultSmsPackage(context) == packageName) return true
        } catch (_: Exception) {}

        // Default Browser
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"))
            val browserPkg = pm.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (browserPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Email
        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            val emailPkg = pm.resolveActivity(emailIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (emailPkg == packageName) return true
        } catch (_: Exception) {}

        return false
    }
}
