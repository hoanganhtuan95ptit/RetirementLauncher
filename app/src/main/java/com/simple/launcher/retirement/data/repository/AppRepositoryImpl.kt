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
        val data = sharedPrefs.all[KEY_SELECTED_APPS]
        if (data is String) {
            val type = object : TypeToken<List<String>>() {}.type
            return try {
                gson.fromJson(data, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else if (data is Set<*>) {
            return data.filterIsInstance<String>()
        }
        return emptyList()
    }

    override fun saveSelectedPackages(packages: List<String>) {
        val json = gson.toJson(packages)
        sharedPrefs.edit().putString(KEY_SELECTED_APPS, json).apply()
        _dataTrigger.tryEmit(Unit)
    }

    override fun isDefaultApp(packageName: String): Boolean {
        val pm = context.packageManager
        val tag = "DefaultAppCheck"

        Log.d(tag, "--- Checking: $packageName ---")

        // Default Launcher
        try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PackageManager.MATCH_ALL
            } else {
                PackageManager.MATCH_DEFAULT_ONLY
            }
            val resolveInfo = pm.resolveActivity(intent, flags)
            val launcherPkg = resolveInfo?.activityInfo?.packageName
            Log.d(tag, "  Launcher default = $launcherPkg")
            if (launcherPkg == packageName) {
                Log.d(tag, "  => MATCH: Launcher")
                return true
            }
        } catch (e: Exception) {
            Log.e(tag, "  Launcher check failed: ${e.message}")
        }

        // Default Dialer
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            val dialerPkg = telecomManager?.defaultDialerPackage
            Log.d(tag, "  Dialer default = $dialerPkg")
            if (dialerPkg == packageName) {
                Log.d(tag, "  => MATCH: Dialer")
                return true
            }

            // sharedUserId check: Samsung tách dialer + incallui thành 2 package
            // nhưng cùng sharedUserId → đều thuộc nhóm "phone default"
            if (dialerPkg != null) {
                try {
                    val dialerInfo = pm.getPackageInfo(dialerPkg, 0)
                    val targetInfo = pm.getPackageInfo(packageName, 0)
                    val dialerSharedUid = dialerInfo.sharedUserId
                    val targetSharedUid = targetInfo.sharedUserId
                    Log.d(tag, "  Dialer sharedUserId = $dialerSharedUid, target sharedUserId = $targetSharedUid")
                    if (dialerSharedUid != null && dialerSharedUid == targetSharedUid) {
                        Log.d(tag, "  => MATCH: same sharedUserId as default dialer")
                        return true
                    }
                } catch (e: Exception) {
                    Log.e(tag, "  sharedUserId check failed: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "  Dialer check failed: ${e.message}")
        }

        // In-Call UI — query tất cả InCallService implementations (không chỉ lấy cái đầu)
        try {
            val inCallIntent = Intent("android.telecom.InCallService")
            val inCallServices = pm.queryIntentServices(inCallIntent, PackageManager.MATCH_ALL)
            val inCallPackages = inCallServices.map { it.serviceInfo.packageName }
            Log.d(tag, "  All InCall services = $inCallPackages")
            if (inCallPackages.contains(packageName)) {
                Log.d(tag, "  => MATCH: InCall UI")
                return true
            }
        } catch (e: Exception) {
            Log.e(tag, "  InCall UI check failed: ${e.message}")
        }

        // Phone process check — package chạy trong android.uid.phone là phone system app
        // (bắt com.samsung.android.incallui và các package tương tự)
        try {
            val targetInfo = pm.getPackageInfo(packageName, 0)
            val sharedUid = targetInfo.sharedUserId
            Log.d(tag, "  Package sharedUserId = $sharedUid")
            if (sharedUid == "android.uid.phone") {
                Log.d(tag, "  => MATCH: phone system package (android.uid.phone)")
                return true
            }
        } catch (e: Exception) {
            Log.e(tag, "  Phone UID check failed: ${e.message}")
        }

        // Default SMS
        try {
            val smsPkg = Telephony.Sms.getDefaultSmsPackage(context)
            Log.d(tag, "  SMS default = $smsPkg")
            if (smsPkg == packageName) {
                Log.d(tag, "  => MATCH: SMS")
                return true
            }
        } catch (e: Exception) {
            Log.e(tag, "  SMS check failed: ${e.message}")
        }

        // Default Browser
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"))
            val browserInfo = pm.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
            val browserPkg = browserInfo?.activityInfo?.packageName
            Log.d(tag, "  Browser default = $browserPkg")
            if (browserPkg == packageName) {
                Log.d(tag, "  => MATCH: Browser")
                return true
            }
        } catch (e: Exception) {
            Log.e(tag, "  Browser check failed: ${e.message}")
        }

        // Default Email
        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            val emailInfo = pm.resolveActivity(emailIntent, PackageManager.MATCH_DEFAULT_ONLY)
            val emailPkg = emailInfo?.activityInfo?.packageName
            Log.d(tag, "  Email default = $emailPkg")
            if (emailPkg == packageName) {
                Log.d(tag, "  => MATCH: Email")
                return true
            }
        } catch (e: Exception) {
            Log.e(tag, "  Email check failed: ${e.message}")
        }

        Log.d(tag, "  => NO MATCH for $packageName")
        return false
    }
}
