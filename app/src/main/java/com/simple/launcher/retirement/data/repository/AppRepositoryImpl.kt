package com.simple.launcher.retirement.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telecom.TelecomManager
import android.provider.Telephony
import android.util.Log
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.inputmethod.InputMethodManager
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

        /**
         * Tập hợp tất cả package names hệ thống / OEM đã biết.
         * Dùng HashSet để lookup O(1), khởi tạo 1 lần duy nhất trong companion.
         * Được kiểm tra ĐẦU TIÊN trong resolveIsDefaultApp() để short-circuit
         * trước khi thực hiện bất kỳ intent resolve / binder IPC nào.
         */
        private val KNOWN_SYSTEM_PACKAGES: HashSet<String> = hashSetOf(
            // ── Core Android system ──
            "com.android.systemui",
            "com.android.settings",
            "com.android.providers.settings",
            "com.android.providers.contacts",
            "com.android.providers.telephony",
            "com.android.providers.media",
            "com.android.providers.downloads",
            "com.android.providers.calendar",
            "com.android.bluetooth",
            "com.android.nfc",
            "com.android.shell",
            "com.android.inputdevices",
            "com.android.location.fused",
            "com.android.wallpaper",
            "com.android.wallpapercropper",
            "com.android.printspooler",
            "com.android.vpndialogs",
            "com.android.htmlviewer",
            "com.android.certinstaller",
            "com.android.packageinstaller",
            "com.android.backupconfirm",
            "com.android.managedprovisioning",
            "com.android.storagemanager",
            "com.android.vending",              // Google Play Store
            "com.android.providers.userdictionary",
            "com.android.emergency",            // Emergency Information

            // ── Samsung ──
            "com.samsung.android.incallui",
            "com.samsung.android.dialer",
            "com.samsung.android.messaging",
            "com.samsung.android.contacts",
            "com.samsung.android.calendar",
            "com.samsung.android.app.camera",
            "com.samsung.android.gallery",
            "com.samsung.android.sm",
            "com.samsung.android.lool",
            "com.samsung.android.app.notes",
            "com.samsung.android.app.reminder",
            "com.samsung.android.app.clockpack",
            "com.samsung.android.app.calculator",
            "com.samsung.android.app.sbrowseredge",
            "com.samsung.android.themestore",
            "com.samsung.android.app.spage",
            "com.samsung.android.bixby.agent",
            "com.samsung.android.visionintelligence",
            "com.samsung.android.spay",
            "com.samsung.android.svoiceime",
            "com.samsung.android.app.soundpicker",
            "com.samsung.android.app.myfiles",
            "com.samsung.android.app.phone",
            "com.samsung.android.forest",
            "com.samsung.android.app.watchmanager",
            "com.samsung.android.weather",
            "com.samsung.android.voicerecorder",

            // ── Google ──
            "com.google.android.apps.messaging",
            "com.google.android.dialer",
            "com.google.android.contacts",
            "com.google.android.calendar",
            "com.google.android.apps.photos",
            "com.google.android.apps.maps",
            "com.google.android.apps.nbu.files",
            "com.google.android.calculator",
            "com.google.android.deskclock",
            "com.google.android.apps.walletnfcrel",
            "com.google.android.apps.wellbeing",
            "com.google.android.apps.recorder",
            "com.google.android.keep",
            "com.google.android.apps.docs",
            "com.google.android.gm",
            "com.google.android.googlequicksearchbox",
            "com.google.android.apps.googleassistant",
            "com.google.android.inputmethod.latin",
            "com.google.android.apps.healthdata",
            "com.google.android.apps.fitness",
            "com.google.android.apps.safetyhub",
            "com.google.android.settings.intelligence",
            "com.google.android.apps.weather",
            "com.google.android.tts",

            // ── Xiaomi / MIUI ──
            "com.miui.securitycenter",
            "com.miui.weather2",
            "com.miui.calculator",
            "com.miui.notes",
            "com.miui.compass",
            "com.miui.player",
            "com.miui.gallery",
            "com.miui.fm",
            "com.miui.screenrecorder",
            "com.miui.videoplayer",
            "com.miui.cleanmaster",
            "com.miui.voiceassist",
            "com.xiaomi.scanner",
            "com.xiaomi.camera",

            // ── OPPO / Realme / ColorOS ──
            "com.coloros.calculator",
            "com.coloros.weather",
            "com.coloros.filemanager",
            "com.coloros.compass2",
            "com.coloros.note",
            "com.coloros.gallery3d",
            "com.coloros.soundrecorder",
            "com.heytap.browser",
            "com.heytap.music",

            // ── Vivo / FuntouchOS ──
            "com.vivo.weather",
            "com.vivo.calculator",
            "com.vivo.compass",
            "com.vivo.gallery",
            "com.vivo.filemanager",
            "com.vivo.note",

            // ── Huawei / HarmonyOS ──
            "com.huawei.camera",
            "com.huawei.systemmanager",
            "com.huawei.health",
            "com.huawei.wallet",
            "com.huawei.calculator",
            "com.huawei.notepad",
            "com.huawei.calendar",
            "com.huawei.compass",
            "com.huawei.appmarket",
            "com.huawei.android.totemweather",

            // ── OnePlus / OxygenOS ──
            "com.oneplus.camera",
            "com.oneplus.gallery",
            "com.oneplus.filemanager",
            "com.oneplus.calculator",
            "com.oneplus.note",
            "com.oneplus.weather",

            // ── LG ──
            "com.lge.camera",
            "com.lge.clock",
            "com.lge.calculator",

            // ── Sony ──
            "com.sonymobile.camera",
            "com.sonymobile.album"
        )
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
                val type = TypeToken.getParameterized(List::class.java, String::class.java).type
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

        // ── Fast path: O(1) HashSet lookup trước khi resolve bất kỳ intent nào ──
        if (packageName in KNOWN_SYSTEM_PACKAGES) return true

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

        // Default Camera
        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val cameraPkg = pm.resolveActivity(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (cameraPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Calendar
        try {
            val calendarIntent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
            val calendarPkg = pm.resolveActivity(calendarIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (calendarPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Contacts
        try {
            val contactsIntent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
            val contactsPkg = pm.resolveActivity(contactsIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (contactsPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Gallery / Photos
        try {
            val galleryIntent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse("content://media/external/images/media/1"), "image/*")
            val galleryPkg = pm.resolveActivity(galleryIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (galleryPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Maps / Navigation
        try {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"))
            val mapPkg = pm.resolveActivity(mapIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (mapPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Calculator
        try {
            val calcIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)
            val calcPkg = pm.resolveActivity(calcIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (calcPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Clock / Alarm
        try {
            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            val clockPkg = pm.resolveActivity(clockIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (clockPkg == packageName) return true
        } catch (_: Exception) {}

        // Default File Manager
        try {
            val filesIntent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse("content://com.android.externalstorage.documents/root/primary"), "vnd.android.document/root")
            val filesPkg = pm.resolveActivity(filesIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (filesPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Music Player
        try {
            val musicIntent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse("content://media/external/audio/media/1"), "audio/*")
            val musicPkg = pm.resolveActivity(musicIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (musicPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Digital Assistant
        try {
            val assistIntent = Intent(Intent.ACTION_ASSIST)
            val assistPkg = pm.resolveActivity(assistIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (assistPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Voice Recognition
        try {
            val voiceIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            val voicePkg = pm.resolveActivity(voiceIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (voicePkg == packageName) return true
        } catch (_: Exception) {}

        // Default TTS Engine
        try {
            val ttsIntent = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
            val ttsResolves = pm.queryIntentActivities(ttsIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (ttsResolves.any { it.activityInfo.packageName == packageName }) return true
        } catch (_: Exception) {}

        // Default Voice Input (alternative — some OEMs register this)
        try {
            val voiceInputIntent = Intent("android.speech.action.VOICE_SEARCH_RESULTS")
            val voiceInputPkg = pm.resolveActivity(voiceInputIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (voiceInputPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Settings
        try {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS)
            val settingsPkg = pm.resolveActivity(settingsIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (settingsPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Video Player
        try {
            val videoIntent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse("content://media/external/video/media/1"), "video/*")
            val videoPkg = pm.resolveActivity(videoIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (videoPkg == packageName) return true
        } catch (_: Exception) {}

        // Default PDF / Document Viewer
        try {
            val pdfIntent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse("content://com.android.providers.downloads.documents/document/1"), "application/pdf")
            val pdfPkg = pm.resolveActivity(pdfIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (pdfPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Voice Recorder
        try {
            val recorderIntent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
            val recorderPkg = pm.resolveActivity(recorderIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (recorderPkg == packageName) return true
        } catch (_: Exception) {}

        // Default Keyboard (IME)
        try {
            val currentIme = android.provider.Settings.Secure.getString(
                context.contentResolver, android.provider.Settings.Secure.DEFAULT_INPUT_METHOD
            )
            if (currentIme != null) {
                // DEFAULT_INPUT_METHOD format: "com.package.name/.ClassName"
                val imePkg = currentIme.substringBefore("/")
                if (imePkg == packageName) return true
            }
        } catch (_: Exception) {}

        // Default NFC Payment / Wallet
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val nfcPaymentIntent = Intent("android.nfc.cardemulation.action.ACTION_CHANGE_DEFAULT")
                val nfcPayPkg = pm.resolveActivity(nfcPaymentIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    ?.activityInfo?.packageName
                if (nfcPayPkg == packageName) return true
            }
        } catch (_: Exception) {}

        // Default Notes / Memo
        try {
            val noteIntent = Intent(Intent.ACTION_CREATE_NOTE)
            val notePkg = pm.resolveActivity(noteIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            if (notePkg == packageName) return true
        } catch (_: Exception) {}

        return false
    }
}
