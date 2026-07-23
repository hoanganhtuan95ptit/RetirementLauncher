package com.simple.launcher.retirement.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Telephony
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simple.launcher.retirement.BuildConfig
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
            "android",
            "android.keyguard",
            "com.android.systemui",
            "com.android.settings",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "com.google.android.gms",
            "com.google.android.gsf",
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
//            "com.android.packageinstaller",
            "com.android.intentresolver",          // System share sheet / intent chooser
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
            "com.sec.android.app.launcher",

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
            "com.google.android.apps.nexuslauncher",
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
            "com.miui.home",
            "com.xiaomi.scanner",
            "com.xiaomi.camera",

            // ── OPPO / Realme / ColorOS ──
            "com.oppo.launcher",
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
            "com.huawei.android.launcher",
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
    private val _dataTrigger = MutableSharedFlow<Unit>(replay = 1).also { 

        it.tryEmit(Unit) 
    }

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
        cachedSelectedPackages?.let { 

            return it 
        }

        val result = when (val data = sharedPrefs.all[KEY_SELECTED_APPS]) {

            is String -> parseSelectedPackages(data)
            is Set<*> -> data.filterIsInstance<String>()
            else -> emptyList()
        }
        cachedSelectedPackages = result
        return result
    }

    private fun parseSelectedPackages(data: String): List<String> {

        return try {

            val type = TypeToken.getParameterized(List::class.java, String::class.java).type
            gson.fromJson(data, type)
        } catch (_: Exception) {

            emptyList()
        }
    }

    override fun saveSelectedPackages(packages: List<String>) {

        val json = gson.toJson(packages)
        sharedPrefs.edit { 

            putString(KEY_SELECTED_APPS, json) 
        }
        cachedSelectedPackages = packages  // cập nhật cache ngay, không cần đọc lại
        _dataTrigger.tryEmit(Unit)
    }

    override fun isDefaultApp(packageName: String): Boolean {

        // Trả về cache ngay nếu đã kiểm tra trước đó — tránh 6-7 binder IPC calls mỗi 500ms
        defaultAppCache[packageName]?.let { 

            return it 
        }

        val result = resolveIsDefaultApp(packageName)
        defaultAppCache[packageName] = result
        return result
    }

    private fun resolveIsDefaultApp(packageName: String): Boolean {

        val pm = context.packageManager
        val tag = "DefaultAppCheck"

        if (BuildConfig.DEBUG) {

            Log.d(tag, "--- Checking: $packageName ---")
        }

        // ── Fast path: O(1) HashSet lookup trước khi resolve bất kỳ intent nào ──
        if (packageName in KNOWN_SYSTEM_PACKAGES) return true

        // Default Launcher
        if (checkDefaultLauncher(pm, packageName)) return true

        // Default Dialer
        if (checkDefaultDialer(pm, packageName)) return true

        // In-Call UI
        if (checkInCallUI(pm, packageName)) return true

        // Phone process check (android.uid.phone)
        if (checkPhoneSharedUid(pm, packageName)) return true

        // Default SMS
        if (checkDefaultSms(packageName)) return true

        // Default Browser
        if (checkDefaultBrowser(pm, packageName)) return true

        // Default Email
        if (checkDefaultEmail(pm, packageName)) return true

        // Default Camera
        if (checkDefaultCamera(pm, packageName)) return true

        // Default Calendar
        if (checkDefaultCalendar(pm, packageName)) return true

        // Default Contacts
        if (checkDefaultContacts(pm, packageName)) return true

        // Default Gallery / Photos
        if (checkDefaultGallery(pm, packageName)) return true

        // Default Maps / Navigation
        if (checkDefaultMaps(pm, packageName)) return true

        // Default Calculator
        if (checkDefaultCalculator(pm, packageName)) return true

        // Default Clock / Alarm
        if (checkDefaultClock(pm, packageName)) return true

        // Default File Manager
        if (checkDefaultFileManager(pm, packageName)) return true

        // Default Music Player
        if (checkDefaultMusicPlayer(pm, packageName)) return true

        // Default Digital Assistant
        if (checkDefaultAssistant(pm, packageName)) return true

        // Default Voice Recognition
        if (checkVoiceRecognition(pm, packageName)) return true

        // Default TTS Engine
        if (checkTtsEngine(pm, packageName)) return true

        // Default Voice Input (alternative — some OEMs register this)
        if (checkVoiceInput(pm, packageName)) return true

        // Default Settings
        if (checkDefaultSettings(pm, packageName)) return true

        // Default Video Player
        if (checkDefaultVideoPlayer(pm, packageName)) return true

        // Default PDF / Document Viewer
        if (checkDefaultPdfViewer(pm, packageName)) return true

        // Default Voice Recorder
        if (checkDefaultVoiceRecorder(pm, packageName)) return true

        // Default Keyboard (IME)
        if (checkDefaultIme(packageName)) return true

        // Default NFC Payment / Wallet
        if (checkDefaultNfcPayment(pm, packageName)) return true

        // Default Notes / Memo
        if (checkDefaultNotes(pm, packageName)) return true

        return false
    }

    private fun checkDefaultLauncher(pm: PackageManager, packageName: String): Boolean {

        return try {

            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                PackageManager.MATCH_ALL
            } else {

                PackageManager.MATCH_DEFAULT_ONLY
            }
            val launcherPkg = pm.resolveActivity(intent, flags)?.activityInfo?.packageName
            launcherPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultDialer(pm: PackageManager, packageName: String): Boolean {

        return try {

            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            val dialerPkg = telecomManager?.defaultDialerPackage
            if (dialerPkg == packageName) return true

            checkSharedUid(pm, dialerPkg, packageName)
        } catch (_: Exception) {

            false
        }
    }

    private fun checkSharedUid(pm: PackageManager, dialerPkg: String?, packageName: String): Boolean {

        val dialer = dialerPkg ?: return false
        return try {

            val dialerSharedUid = pm.getPackageInfo(dialer, 0).sharedUserId
            val targetSharedUid = pm.getPackageInfo(packageName, 0).sharedUserId
            dialerSharedUid != null && dialerSharedUid == targetSharedUid
        } catch (_: Exception) {

            false
        }
    }

    private fun checkInCallUI(pm: PackageManager, packageName: String): Boolean {

        return try {

            val inCallIntent = Intent("android.telecom.InCallService")
            pm.queryIntentServices(inCallIntent, PackageManager.MATCH_ALL)
                .any { it.serviceInfo.packageName == packageName }
        } catch (_: Exception) {

            false
        }
    }

    private fun checkPhoneSharedUid(pm: PackageManager, packageName: String): Boolean {

        return try {

            val sharedUid = pm.getPackageInfo(packageName, 0).sharedUserId
            sharedUid == "android.uid.phone"
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultSms(packageName: String): Boolean {

        return try {

            Telephony.Sms.getDefaultSmsPackage(context) == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultBrowser(pm: PackageManager, packageName: String): Boolean {

        return try {

            val browserIntent = Intent(Intent.ACTION_VIEW, "https://www.example.com".toUri())
            val browserPkg = pm.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            browserPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultEmail(pm: PackageManager, packageName: String): Boolean {

        return try {

            val emailIntent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri())
            val emailPkg = pm.resolveActivity(emailIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            emailPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultCamera(pm: PackageManager, packageName: String): Boolean {

        return try {

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val cameraPkg = pm.resolveActivity(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            cameraPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultCalendar(pm: PackageManager, packageName: String): Boolean {

        return try {

            val calendarIntent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
            val calendarPkg = pm.resolveActivity(calendarIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            calendarPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultContacts(pm: PackageManager, packageName: String): Boolean {

        return try {

            val contactsIntent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
            val contactsPkg = pm.resolveActivity(contactsIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            contactsPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultGallery(pm: PackageManager, packageName: String): Boolean {

        return try {

            val galleryIntent = Intent(Intent.ACTION_VIEW)
                .setDataAndType("content://media/external/images/media/1".toUri(), "image/*")
            val galleryPkg = pm.resolveActivity(galleryIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            galleryPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultMaps(pm: PackageManager, packageName: String): Boolean {

        return try {

            val mapIntent = Intent(Intent.ACTION_VIEW, "geo:0,0".toUri())
            val mapPkg = pm.resolveActivity(mapIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            mapPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultCalculator(pm: PackageManager, packageName: String): Boolean {

        return try {

            val calcIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)
            val calcPkg = pm.resolveActivity(calcIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            calcPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultClock(pm: PackageManager, packageName: String): Boolean {

        return try {

            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            val clockPkg = pm.resolveActivity(clockIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            clockPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultFileManager(pm: PackageManager, packageName: String): Boolean {

        return try {

            val filesIntent = Intent(Intent.ACTION_VIEW)
                .setDataAndType("content://com.android.externalstorage.documents/root/primary".toUri(), "vnd.android.document/root")
            val filesPkg = pm.resolveActivity(filesIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            filesPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultMusicPlayer(pm: PackageManager, packageName: String): Boolean {

        return try {

            val musicIntent = Intent(Intent.ACTION_VIEW)
                .setDataAndType("content://media/external/audio/media/1".toUri(), "audio/*")
            val musicPkg = pm.resolveActivity(musicIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            musicPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultAssistant(pm: PackageManager, packageName: String): Boolean {

        return try {

            val assistIntent = Intent(Intent.ACTION_ASSIST)
            val assistPkg = pm.resolveActivity(assistIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            assistPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkVoiceRecognition(pm: PackageManager, packageName: String): Boolean {

        return try {

            val voiceIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            val voicePkg = pm.resolveActivity(voiceIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            voicePkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkTtsEngine(pm: PackageManager, packageName: String): Boolean {

        return try {

            val ttsIntent = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
            val ttsResolves = pm.queryIntentActivities(ttsIntent, PackageManager.MATCH_DEFAULT_ONLY)
            ttsResolves.any { it.activityInfo.packageName == packageName }
        } catch (_: Exception) {

            false
        }
    }

    private fun checkVoiceInput(pm: PackageManager, packageName: String): Boolean {

        return try {

            val voiceInputIntent = Intent("android.speech.action.VOICE_SEARCH_RESULTS")
            val voiceInputPkg = pm.resolveActivity(voiceInputIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            voiceInputPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultSettings(pm: PackageManager, packageName: String): Boolean {

        return try {

            val settingsIntent = Intent(Settings.ACTION_SETTINGS)
            val settingsPkg = pm.resolveActivity(settingsIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            settingsPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultVideoPlayer(pm: PackageManager, packageName: String): Boolean {

        return try {

            val videoIntent = Intent(Intent.ACTION_VIEW)
                .setDataAndType("content://media/external/video/media/1".toUri(), "video/*")
            val videoPkg = pm.resolveActivity(videoIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            videoPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultPdfViewer(pm: PackageManager, packageName: String): Boolean {

        return try {

            val pdfIntent = Intent(Intent.ACTION_VIEW)
                .setDataAndType("content://com.android.providers.downloads.documents/document/1".toUri(), "application/pdf")
            val pdfPkg = pm.resolveActivity(pdfIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            pdfPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultVoiceRecorder(pm: PackageManager, packageName: String): Boolean {

        return try {

            val recorderIntent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
            val recorderPkg = pm.resolveActivity(recorderIntent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            recorderPkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultIme(packageName: String): Boolean {

        return try {

            val currentIme = android.provider.Settings.Secure.getString(
                context.contentResolver, android.provider.Settings.Secure.DEFAULT_INPUT_METHOD
            )
            val imePkg = currentIme?.substringBefore("/")
            imePkg == packageName
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultNfcPayment(pm: PackageManager, packageName: String): Boolean {

        return try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val nfcPaymentIntent = Intent("android.nfc.cardemulation.action.ACTION_CHANGE_DEFAULT")
                val nfcPayPkg = pm.resolveActivity(nfcPaymentIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    ?.activityInfo?.packageName
                nfcPayPkg == packageName
            } else {

                false
            }
        } catch (_: Exception) {

            false
        }
    }

    private fun checkDefaultNotes(pm: PackageManager, packageName: String): Boolean {

        return try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {

                val noteIntent = Intent(Intent.ACTION_CREATE_NOTE)
                val notePkg = pm.resolveActivity(noteIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    ?.activityInfo?.packageName
                notePkg == packageName
            } else {

                false
            }
        } catch (_: Exception) {

            false
        }
    }
}
