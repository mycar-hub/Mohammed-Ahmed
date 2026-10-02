package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
 *
 * (العميل يطلب الخدمة بدون سعر محدد - المحامي يحدد أتعابه والمصاريف القانونية
 * والبرنامج يحسب الإجمالي شاملاً أتعاب المنصة ويعرضه على العميل للموافقة أو الرفض)
 */
@Composable
fun LawyerDispatchPopup(
  request: ServiceRequest,
  clientFeePercentage: Double = 5.0,
  onAccept: (request: ServiceRequest, lawyerFee: Double, legalExpenses: Double) -> Unit,
  onDecline: () -> Unit,
  onTimeout: () -> Unit
) {
  val context = LocalContext.current
  val totalDurationSeconds = 20
  var millisLeft by remember { mutableLongStateOf(totalDurationSeconds * 1000L) }

  // خانات تسعير المحامي
  var lawyerFeeInput by remember { mutableStateOf("3000") }
  var legalExpensesInput by remember { mutableStateOf("500") }

  val feeVal = lawyerFeeInput.toDoubleOrNull() ?: 0.0
  val expVal = legalExpensesInput.toDoubleOrNull() ?: 0.0
  val baseTotal = feeVal + expVal
  val platformFeeAmount = if (clientFeePercentage > 0.0) baseTotal * (clientFeePercentage / 100.0) else 0.0
  val grandTotalAmount = baseTotal + platformFeeAmount

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
        .background(Color.Black.copy(alpha = 0.85f))
        .padding(14.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .clip(RoundedCornerShape(24.dp))
          .border(2.dp, Brush.horizontalGradient(listOf(EmeraldSuccess, GoldSecondary)), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    .size(17.dp)
                    .scale(pulseScale)
                )
                Text(
                  text = "طلب فوري وارد • ALARM",
                  color = Color.White,
                  fontSize = 11.sp,
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
            modifier = Modifier.size(86.dp)
          ) {
            CircularProgressIndicator(
              progress = { 1f },
              modifier = Modifier.fillMaxSize(),
              color = Color.White.copy(alpha = 0.12f),
              strokeWidth = 6.dp
            )
            CircularProgressIndicator(
              progress = { progressFraction },
              modifier = Modifier.fillMaxSize(),
              color = if (secondsLeft <= 5) CrimsonError else EmeraldSuccess,
              strokeWidth = 6.dp,
              strokeCap = StrokeCap.Round
            )
            Column(
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "$secondsLeft",
                color = if (secondsLeft <= 5) CrimsonError else Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold
              )
              Text(
                text = "ثانية متبقية",
                color = GoldLight,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          // 3. كارت تفاصيل القضية والموقع الجغرافي
          Surface(
            color = NavyContainer,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
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
                      .size(24.dp)
                      .clip(CircleShape)
                      .background(GoldSecondary.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Person,
                      contentDescription = null,
                      tint = GoldSecondary,
                      modifier = Modifier.size(14.dp)
                    )
                  }
                  Text(
                    text = "الموكل: ${request.clientName}",
                    color = Color.White,
                    fontSize = 12.sp,
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
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Text(
                text = request.title,
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )

              // الموقع والاختصاص القضائي
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = CrimsonError,
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = "${request.courtLocation?.city ?: request.city} • ${request.courtLocation?.district ?: "نطاق المحكمة"}",
                  color = GoldLight,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "• ضمن نطاقك المعتمد",
                  color = EmeraldSuccess,
                  fontSize = 10.sp
                )
              }

              if (request.courtLocation != null && request.courtLocation.courtJurisdiction.isNotBlank()) {
                Text(
                  text = "المحكمة: ${request.courtLocation.courtJurisdiction}",
                  color = Color.White.copy(alpha = 0.8f),
                  fontSize = 10.5.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          // 4. تسعير المحامي الفوري (أتعاب + مصاريف قانونية) وحساب أتعاب المنصة تلقائياً
          Surface(
            color = Color.White.copy(alpha = 0.06f),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(16.dp))
                Text(
                  text = "حدد تسعيرك القانوني للطلب (العميل لم يحدد سعراً مسبقاً):",
                  color = GoldLight,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              // A. سعر الأتعاب
              Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "1. سعر الأتعاب (ج.م) *",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = "${feeVal.toInt()} ج.م",
                    color = EmeraldSuccess,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                OutlinedTextField(
                  value = lawyerFeeInput,
                  onValueChange = { lawyerFeeInput = it },
                  placeholder = { Text("أدخل أتعابك (مثال: 3000)", color = Color.Gray, fontSize = 11.sp) },
                  singleLine = true,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth(),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldSecondary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                  )
                )

                // Quick selector chips for fee
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(listOf(1500, 2500, 3000, 4500, 6000, 8000)) { opt ->
                    Surface(
                      color = if (feeVal.toInt() == opt) GoldSecondary else Color.White.copy(alpha = 0.12f),
                      shape = RoundedCornerShape(6.dp),
                      modifier = Modifier.clickable { lawyerFeeInput = opt.toString() }
                    ) {
                      Text(
                        text = "$opt ج.م",
                        color = if (feeVal.toInt() == opt) NavyDark else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
              }

              // B. سعر المصاريف القانونية التقديرية
              Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "2. سعر المصاريف القانونية التقديرية (ج.م)",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = "${expVal.toInt()} ج.م",
                    color = GoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                OutlinedTextField(
                  value = legalExpensesInput,
                  onValueChange = { legalExpensesInput = it },
                  placeholder = { Text("رسوم قيد/أمانات خبير/انتقال (0 إذا لم توجد)", color = Color.Gray, fontSize = 11.sp) },
                  singleLine = true,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth(),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldSecondary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                  )
                )

                // Quick selector chips for expenses
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(listOf(0, 300, 500, 1000, 1500)) { opt ->
                    Surface(
                      color = if (expVal.toInt() == opt) GoldSecondary else Color.White.copy(alpha = 0.12f),
                      shape = RoundedCornerShape(6.dp),
                      modifier = Modifier.clickable { legalExpensesInput = opt.toString() }
                    ) {
                      Text(
                        text = if (opt == 0) "بدون مصاريف (0)" else "$opt ج.م",
                        color = if (expVal.toInt() == opt) NavyDark else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
              }

              Divider(color = Color.White.copy(alpha = 0.15f), thickness = 0.8.dp)

              // C. الحساب التلقائي الحي الشامل لرسوم المنصة
              Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("المجموع الأساسي (أتعاب + مصاريف):", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                  Text("${baseTotal.toInt()} ج.م", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("أتعاب المنصة المضافة (${clientFeePercentage.toInt()}%):", color = GoldLight, fontSize = 11.sp)
                  Text("+ ${platformFeeAmount.toInt()} ج.م", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Surface(
                  color = EmeraldSuccess.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "السعر النهائي الشامل المعروض للعميل:",
                      color = Color.White,
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "${grandTotalAmount.toInt()} ج.م",
                      color = EmeraldSuccess,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.ExtraBold
                    )
                  }
                }
              }
            }
          }

          // 5. أزرار اتخاذ القرار (إرسال التسعير وقبول الطلب أو الرفض)
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
                .height(48.dp),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
              Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("رفض / تجاهل", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            // زر إرسال التسعير وقبول الطلب
            Button(
              onClick = {
                DispatchAlarmManager.stopAlarm(context)
                val finalFee = if (feeVal <= 0.0) 2500.0 else feeVal
                onAccept(request, finalFee, expVal)
              },
              modifier = Modifier
                .weight(1.8f)
                .height(48.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldSuccess,
                contentColor = Color.White
              ),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
              Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "إرسال التسعير ($secondsLeft ث)",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Text(
            text = "🔊 صوت المنبه نشط • سيعرض السعر النهائي (${grandTotalAmount.toInt()} ج.م) للعميل للموافقة أو الرفض",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 9.sp,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}
