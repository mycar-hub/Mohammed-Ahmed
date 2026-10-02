package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ServiceRequest
import com.example.service.DispatchAlarmManager
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * نافذة إشعار الطلب الفوري المنبثقة للمحامي
 * تحاكي شاشات تنبيه رحلات تطبيقات النقل الذكي (مثل أوبر وكريم)
 * تظهر لمدة 20 ثانية مع منبه صوتي Alarm واهتزاز متواصل
 */
@Composable
fun LawyerDispatchPopup(
  request: ServiceRequest,
  onAccept: (ServiceRequest) -> Unit,
  onDecline: () -> Unit,
  onTimeout: () -> Unit
) {
  val context = LocalContext.current
  val totalDurationSeconds = 20
  var millisLeft by remember { mutableLongStateOf(totalDurationSeconds * 1000L) }

  // تشغيل المنبه الصوتي والاهتزاز التكراري طوال فترة الـ 20 ثانية
  LaunchedEffect(request.id) {
    DispatchAlarmManager.startAlarm(context)
    val startTime = System.currentTimeMillis()
    val endTime = startTime + (totalDurationSeconds * 1000L)

    while (true) {
      val now = System.currentTimeMillis()
      val remaining = endTime - now
      if (remaining <= 0) {
        millisLeft = 0
        DispatchAlarmManager.stopAlarm(context)
        onTimeout()
        break
      }
      millisLeft = remaining
      delay(50) // تحديث سلس لمؤشر التقدم الدائري
    }
  }

  // إيقاف المنبه الصوتي وتنظيف الموارد عند إغلاق النافذة
  DisposableEffect(Unit) {
    onDispose {
      DispatchAlarmManager.stopAlarm(context)
    }
  }

  val secondsLeft = ((millisLeft + 999) / 1000).toInt().coerceIn(0, totalDurationSeconds)
  val progressFraction = (millisLeft.toFloat() / (totalDurationSeconds * 1000L)).coerceIn(0f, 1f)

  // نبض حركي لأيقونة الإنذار والصوت
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.12f,
    animationSpec = infiniteRepeatable(
      animation = tween(450, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  Dialog(
    onDismissRequest = {
      DispatchAlarmManager.stopAlarm(context)
      onDecline()
    },
    properties = DialogProperties(
      dismissOnBackPress = false,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.82f))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .clip(RoundedCornerShape(26.dp))
          .border(2.dp, Brush.horizontalGradient(listOf(EmeraldSuccess, GoldSecondary)), RoundedCornerShape(26.dp)),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // 1. شريط الإنذار والعد التنازلي مع النبض الصوتي
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              color = CrimsonError.copy(alpha = 0.2f),
              shape = RoundedCornerShape(20.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError.copy(alpha = 0.6f))
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Campaign,
                  contentDescription = "إنذار طلب فوري",
                  tint = CrimsonError,
                  modifier = Modifier
                    .size(18.dp)
                    .scale(pulseScale)
                )
                Text(
                  text = "طلب فوري وارد • ALARM",
                  color = Color.White,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Surface(
              color = EmeraldSuccess.copy(alpha = 0.2f),
              shape = RoundedCornerShape(20.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.MyLocation,
                  contentDescription = null,
                  tint = EmeraldSuccess,
                  modifier = Modifier.size(13.dp)
                )
                Text(
                  text = "نطاقك الجغرافي",
                  color = EmeraldSuccess,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          // 2. مؤشر العداد الدائري لـ 20 ثانية (مثل شاشة قبول كباتن النقل الذكي)
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(100.dp)
          ) {
            CircularProgressIndicator(
              progress = { 1f },
              modifier = Modifier.fillMaxSize(),
              color = Color.White.copy(alpha = 0.12f),
              strokeWidth = 7.dp
            )
            CircularProgressIndicator(
              progress = { progressFraction },
              modifier = Modifier.fillMaxSize(),
              color = if (secondsLeft <= 5) CrimsonError else EmeraldSuccess,
              strokeWidth = 7.dp,
              strokeCap = StrokeCap.Round
            )
            Column(
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "$secondsLeft",
                color = if (secondsLeft <= 5) CrimsonError else Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
              )
              Text(
                text = "ثانية متبقية",
                color = GoldLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          // 3. كارت تفاصيل القضية والموقع الجغرافي
          Surface(
            color = NavyContainer,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // اسم العميل والتصنيف
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(GoldSecondary.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Person,
                      contentDescription = null,
                      tint = GoldSecondary,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Text(
                    text = request.clientName,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                Surface(
                  color = AmberContainer,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = request.category.titleAr,
                    color = Color(0xFFB45309),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Text(
                text = request.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )

              // الموقع والاختصاص القضائي
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = CrimsonError,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "${request.courtLocation?.city ?: request.city} • ${request.courtLocation?.district ?: "نطاق المحكمة"}",
                  color = GoldLight,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "• ضمن نطاقك الجغرافي المعتمد",
                  color = EmeraldSuccess,
                  fontSize = 10.5.sp
                )
              }

              if (request.courtLocation != null && request.courtLocation.courtJurisdiction.isNotBlank()) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = GoldSecondary,
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = request.courtLocation.courtJurisdiction,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              Divider(color = Color.White.copy(alpha = 0.15f), thickness = 0.8.dp)

              // الأتعاب المقدرة
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "الأتعاب المقترحة للطلب:",
                  color = Color.White.copy(alpha = 0.8f),
                  fontSize = 12.sp
                )
                Text(
                  text = "${request.budgetAmount.toInt()} ج.م",
                  color = EmeraldSuccess,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
          }

          // 4. أزرار اتخاذ القرار (قبول فوري أو رفض مثل أوبر)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // زر الرفض / التجاهل
            OutlinedButton(
              onClick = {
                DispatchAlarmManager.stopAlarm(context)
                onDecline()
              },
              modifier = Modifier
                .weight(1f)
                .height(52.dp),
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
              Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("رفض / تجاهل", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }

            // زر القبول الفوري السريع
            Button(
              onClick = {
                DispatchAlarmManager.stopAlarm(context)
                onAccept(request)
              },
              modifier = Modifier
                .weight(1.6f)
                .height(52.dp),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldSuccess,
                contentColor = Color.White
              ),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
              Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "قبول الطلب ($secondsLeft ث)",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Text(
            text = "🔊 صوت المنبه والاهتزاز نشطان • سيتم تحويل الطلب لمحامٍ آخر بعد 20 ثانية",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 9.5.sp,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}
