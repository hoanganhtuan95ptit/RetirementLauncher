package com.simple.launcher.retirement.presentation.emergency

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.databinding.ActivityEmergencyAlertBinding
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseActivity
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import kotlin.math.max

/**
 * Man hinh canh bao an toan SOS (Screen A).
 *
 * Hien thi khi EmergencyWorker phat hien nguoi dung khong hoat dong den nguong.
 * Nguoi dung co [COUNTDOWN_MILLIS] de bam nut "Toi an toan"; het gio se kich hoat
 * cuoc goi khan cap qua worker.
 *
 * - Chuong bao dong (STREAM_ALARM) + rung lien tuc khi hien.
 * - Show when locked + turn screen on + keep screen on.
 * - Chan back button.
 * - launchMode=singleInstance trong manifest => tranh tao nhieu instance khi worker
 *   phat hien lai timeout truoc khi activity kip xu ly.
 */
class EmergencyAlertActivity : BaseActivity<ActivityEmergencyAlertBinding>() {

    private var countDownTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    /** Da phat event ket qua chua? Tranh phat 2 lan neu nguoi dung bam nut sat luc het gio. */
    private var resultDispatched = false

    override fun inflateBinding(inflater: LayoutInflater) = ActivityEmergencyAlertBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {

        // Cac flag phai set truoc super.onCreate de dam bao activity co the hien khi khoa man.
        applyShowWhenLockedFlags()
        super.onCreate(savedInstanceState)
    }

    override fun setupViews(savedInstanceState: Bundle?) {

        val binding = binding ?: return

        binding.pbCountdown.max = COUNTDOWN_MAX_PROGRESS
        binding.pbCountdown.progress = COUNTDOWN_MAX_PROGRESS
        updateCountdownLabel(COUNTDOWN_MILLIS)

        binding.btnImSafe.setOnSafeClickListener {

            dispatchSafeAndFinish()
        }

        // Chan back button — nguoi dung bat buoc bam "Toi an toan".
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {

                // Khong lam gi ca — nuot su kien.
                logDebug("Back button ignored on SOS alert")
            }
        })

        startCountdown()
        startAlarmSound()
        startVibration()
    }

    override fun onDestroy() {

        stopCountdown()
        stopAlarmSound()
        stopVibration()
        super.onDestroy()
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private fun applyShowWhenLockedFlags() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {

            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {

            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startCountdown() {

        stopCountdown()
        countDownTimer = object : CountDownTimer(COUNTDOWN_MILLIS, COUNTDOWN_TICK_MILLIS) {

            override fun onTick(millisUntilFinished: Long) {

                val binding = binding ?: return
                val progress = ((millisUntilFinished.toFloat() / COUNTDOWN_MILLIS) * COUNTDOWN_MAX_PROGRESS)
                    .toInt()
                    .coerceIn(0, COUNTDOWN_MAX_PROGRESS)
                binding.pbCountdown.progress = progress
                updateCountdownLabel(millisUntilFinished)
            }

            override fun onFinish() {

                val binding = binding ?: return
                binding.pbCountdown.progress = 0
                updateCountdownLabel(0L)
                dispatchTimeoutAndFinish()
            }
        }.also { it.start() }
    }

    private fun stopCountdown() {

        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun updateCountdownLabel(millisUntilFinished: Long) {

        val binding = binding ?: return
        val totalSeconds = max(0L, millisUntilFinished / 1000L)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        binding.tvCountdown.text = String.format("%d:%02d", minutes, seconds)
    }

    private fun startAlarmSound() {

        try {

            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: return

            // USAGE_ALARM => phat qua STREAM_ALARM, van keu ca khi may o che do im lang
            // (giong bao thuc). Khong can chinh setStreamVolume — tranh chinh vinh vien
            // volume nguoi dung dat.
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            mediaPlayer = MediaPlayer().apply {

                setAudioAttributes(attributes)
                setDataSource(this@EmergencyAlertActivity, alarmUri)
                isLooping = true
                setOnPreparedListener { it.start() }
                prepareAsync()
            }
        } catch (exception: Exception) {

            logDebug("Unable to start alarm sound: ${exception.message}")
        }
    }

    private fun stopAlarmSound() {

        try {

            mediaPlayer?.apply {

                if (isPlaying) stop()
                release()
            }
        } catch (exception: Exception) {

            logDebug("Error while stopping alarm sound: ${exception.message}")
        } finally {

            mediaPlayer = null
        }
    }

    private fun startVibration() {

        try {

            vibrator = resolveVibrator() ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val effect = VibrationEffect.createWaveform(VIBRATION_PATTERN, VIBRATION_REPEAT_INDEX)
                vibrator?.vibrate(effect)
            } else {

                @Suppress("DEPRECATION")
                vibrator?.vibrate(VIBRATION_PATTERN, VIBRATION_REPEAT_INDEX)
            }
        } catch (exception: Exception) {

            logDebug("Unable to start vibration: ${exception.message}")
        }
    }

    private fun stopVibration() {

        try {

            vibrator?.cancel()
        } catch (exception: Exception) {

            logDebug("Error while stopping vibration: ${exception.message}")
        } finally {

            vibrator = null
        }
    }

    private fun resolveVibrator(): Vibrator? {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {

            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun dispatchSafeAndFinish() {

        if (resultDispatched) return
        resultDispatched = true

        // Cap nhat timestamp de EmergencyWorker khong lap tuc phat hien lai timeout.
        PreferenceRepository.instance.setLastUserActivity(System.currentTimeMillis())
        AppEventBus.post(AppEvent.EmergencyAlertConfirmedSafe)
        logDebug("User confirmed safe")
        finish()
    }

    private fun dispatchTimeoutAndFinish() {

        if (resultDispatched) return
        resultDispatched = true

        AppEventBus.post(AppEvent.EmergencyAlertTimedOut)
        logDebug("Countdown expired, escalating to emergency call")
        finish()
    }

    private fun logDebug(message: String) {

        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    companion object {

        private const val TAG = "EmergencyAlertActivity"

        // 5 phut cho nguoi dung bam "Toi an toan" truoc khi thuc su goi.
        // Debug: 30s de test nhanh.
        private val COUNTDOWN_MILLIS = if (BuildConfig.DEBUG) 30_000L else 5 * 60 * 1000L
        private const val COUNTDOWN_TICK_MILLIS = 500L
        private const val COUNTDOWN_MAX_PROGRESS = 1000

        // 500ms rung, 500ms nghi, lap lai tu index 0.
        private val VIBRATION_PATTERN = longArrayOf(0L, 500L, 500L)
        private const val VIBRATION_REPEAT_INDEX = 0

        fun createLaunchIntent(context: Context): Intent {

            return Intent(context, EmergencyAlertActivity::class.java).apply {

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
        }
    }
}
