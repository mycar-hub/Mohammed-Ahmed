package com.example.service

import android.content.Context
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.*

/**
 * مدير منبه وتنبيهات استقبال الطلبات الفورية للمحامي
 * (يحاكي منبه تطبيقات النقل الذكي مثل أوبر وكريم وبولت)
 */
object DispatchAlarmManager {

  private const val TAG = "DispatchAlarmManager"
  private var isPlaying = false
  private var alarmJob: Job? = null
  private var ringtone: Ringtone? = null
  private var toneGenerator: ToneGenerator? = null

  /**
   * تشغيل المنبه الصوتي ونبضات الاهتزاز معاً
   */
  fun startAlarm(context: Context) {
    if (isPlaying) return
    isPlaying = true

    val appContext = context.applicationContext

    alarmJob = CoroutineScope(Dispatchers.IO).launch {
      try {
        // 1. تشغيل رنين النظام إن أمكن
        try {
          val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

          ringtone = RingtoneManager.getRingtone(appContext, alarmUri)?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
              isLooping = true
            }
            play()
          }
        } catch (e: Exception) {
          Log.w(TAG, "Ringtone play failed, falling back to ToneGenerator", e)
        }

        // 2. إعداد مولد النغمات الاحتياطي / الإضافي لضمان سماع الصوت بوضوح
        try {
          toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 95)
        } catch (e: Exception) {
          try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 95)
          } catch (e2: Exception) {
            Log.e(TAG, "Could not initialize ToneGenerator", e2)
          }
        }

        // 3. تهيئة الاهتزاز التكراري
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
          vm?.defaultVibrator
        } else {
          @Suppress("DEPRECATION")
          appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        val pattern = longArrayOf(0, 350, 150, 350, 150, 600)

        // حلقة تكرار النغمات والاهتزاز طالما المنبه يعمل
        while (isActive && isPlaying) {
          // نبضة نغمة سريعة تحاكي وصول طلب أوبر الذكي
          toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 280)

          // تشغيل نبضات الاهتزاز
          try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
              @Suppress("DEPRECATION")
              vibrator?.vibrate(pattern, -1)
            }
          } catch (e: Exception) {
            Log.w(TAG, "Vibration failed", e)
          }

          delay(400)
          toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 220)
          delay(800)
        }
      } catch (e: CancellationException) {
        // تم الإلغاء بشكل طبيعي
      } catch (e: Exception) {
        Log.e(TAG, "Error in alarm loop", e)
      } finally {
        cleanupResources(appContext)
      }
    }
  }

  /**
   * إيقاف المنبه فوراً وتحرير كافة الموارد
   */
  fun stopAlarm(context: Context? = null) {
    isPlaying = false
    alarmJob?.cancel()
    alarmJob = null
    cleanupResources(context?.applicationContext)
  }

  private fun cleanupResources(context: Context?) {
    try {
      ringtone?.let {
        if (it.isPlaying) {
          it.stop()
        }
      }
      ringtone = null
    } catch (e: Exception) {
      Log.w(TAG, "Error stopping ringtone", e)
    }

    try {
      toneGenerator?.release()
      toneGenerator = null
    } catch (e: Exception) {
      Log.w(TAG, "Error releasing ToneGenerator", e)
    }

    try {
      if (context != null) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
          vm?.defaultVibrator
        } else {
          @Suppress("DEPRECATION")
          context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        vibrator?.cancel()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error cancelling vibration", e)
    }
  }
}
