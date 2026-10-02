package com.example.service

import android.content.Context
import android.media.AudioAttributes
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
 * يتعامل مع الصوت والاهتزاز بشكل آمن وبدون تسريب موارد أو أخطاء AudioTrack/ToneGenerator
 */
object DispatchAlarmManager {

  private const val TAG = "DispatchAlarmManager"
  @Volatile private var isPlaying = false
  private var alarmJob: Job? = null
  private var ringtone: Ringtone? = null
  private var toneGenerator: ToneGenerator? = null
  private val lock = Any()

  /**
   * تشغيل المنبه الصوتي ونبضات الاهتزاز معاً
   */
  fun startAlarm(context: Context) {
    synchronized(lock) {
      if (isPlaying) return
      isPlaying = true
    }

    val appContext = context.applicationContext

    alarmJob = CoroutineScope(Dispatchers.Default).launch {
      try {
        // 1. تشغيل رنين النظام المخصص للتنبيهات أو المنبه
        try {
          val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

          ringtone = RingtoneManager.getRingtone(appContext, alarmUri)?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
              isLooping = false
            }
            play()
          }
        } catch (e: Exception) {
          Log.w(TAG, "Ringtone playback handled gracefully", e)
        }

        // 2. تهيئة مولد النغمات بشكل آمن لإنتاج صافرة التنبيه المتقطعة
        synchronized(lock) {
          try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
          } catch (e: Exception) {
            try {
              toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            } catch (e2: Exception) {
              Log.w(TAG, "ToneGenerator not available on this device", e2)
              toneGenerator = null
            }
          }
        }

        // 3. تهيئة الاهتزاز التكراري
        val vibrator = try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
          } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
          }
        } catch (e: Exception) {
          null
        }

        val pattern = longArrayOf(0, 300, 200, 300, 200, 500)

        // حلقة تكرار النغمات والاهتزاز طالما المنبه يعمل
        while (isActive && isPlaying) {
          // تشغيل نبضة نغمة قصيرة بأمان
          synchronized(lock) {
            if (isPlaying) {
              try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
              } catch (e: Exception) {
                Log.w(TAG, "Safe tone start caught", e)
              }
            }
          }

          // تشغيل نبضات الاهتزاز
          try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
              @Suppress("DEPRECATION")
              vibrator?.vibrate(pattern, -1)
            }
          } catch (e: Exception) {
            Log.w(TAG, "Safe vibration caught", e)
          }

          delay(900)
        }
      } catch (e: CancellationException) {
        // إلغاء طبيعي
      } catch (e: Exception) {
        Log.w(TAG, "Alarm loop caught exception", e)
      } finally {
        cleanupResources(appContext)
      }
    }
  }

  /**
   * إيقاف المنبه فوراً وتحرير كافة الموارد بأمان
   */
  fun stopAlarm(context: Context? = null) {
    synchronized(lock) {
      isPlaying = false
    }
    alarmJob?.cancel()
    alarmJob = null
    cleanupResources(context?.applicationContext)
  }

  private fun cleanupResources(context: Context?) {
    synchronized(lock) {
      try {
        ringtone?.let {
          if (it.isPlaying) {
            it.stop()
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Safe ringtone cleanup", e)
      } finally {
        ringtone = null
      }

      try {
        toneGenerator?.let { tg ->
          try {
            tg.stopTone()
          } catch (e: Exception) {
            // Ignored
          }
          tg.release()
        }
      } catch (e: Exception) {
        Log.w(TAG, "Safe ToneGenerator cleanup", e)
      } finally {
        toneGenerator = null
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
        Log.w(TAG, "Safe vibrator cleanup", e)
      }
    }
  }
}
