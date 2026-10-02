package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.*
import com.example.ui.theme.*

/**
 * Authentic stylized QR Code Canvas drawing finder patterns, timing patterns,
 * deterministic matrix bits based on token hash, and an elegant center emblem.
 */
@Composable
fun QrCodeCanvas(
  token: String,
  modifier: Modifier = Modifier,
  matrixSize: Int = 21,
  darkColor: Color = NavyDark,
  lightColor: Color = Color.White
) {
  Box(
    modifier = modifier
      .background(lightColor, RoundedCornerShape(12.dp))
      .padding(12.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth = size.width
      val canvasHeight = size.height
      val cellSize = canvasWidth / matrixSize

      // Draw background
      drawRect(color = lightColor, size = size)

      // Deterministic pseudo-random generation based on token
      val hash = token.hashCode().toLong()

      // Function to check if cell is inside finder patterns (top-left, top-right, bottom-left)
      fun isInFinder(r: Int, c: Int): Boolean {
        if (r < 7 && c < 7) return true // Top-left
        if (r < 7 && c >= matrixSize - 7) return true // Top-right
        if (r >= matrixSize - 7 && c < 7) return true // Bottom-left
        return false
      }

      // Draw standard 7x7 Finder Pattern at (startR, startC)
      fun drawFinder(startR: Int, startC: Int) {
        val x = startC * cellSize
        val y = startR * cellSize
        val finderWidth = 7 * cellSize

        // Outer black 7x7 square
        drawRoundRect(
          color = darkColor,
          topLeft = Offset(x, y),
          size = Size(finderWidth, finderWidth),
          cornerRadius = CornerRadius(cellSize * 0.8f, cellSize * 0.8f)
        )
        // Middle white 5x5 square
        drawRoundRect(
          color = lightColor,
          topLeft = Offset(x + cellSize, y + cellSize),
          size = Size(finderWidth - 2 * cellSize, finderWidth - 2 * cellSize),
          cornerRadius = CornerRadius(cellSize * 0.5f, cellSize * 0.5f)
        )
        // Inner black 3x3 square
        drawRoundRect(
          color = darkColor,
          topLeft = Offset(x + 2 * cellSize, y + 2 * cellSize),
          size = Size(finderWidth - 4 * cellSize, finderWidth - 4 * cellSize),
          cornerRadius = CornerRadius(cellSize * 0.4f, cellSize * 0.4f)
        )
      }

      // Draw the three standard finder patterns
      drawFinder(0, 0)
      drawFinder(0, matrixSize - 7)
      drawFinder(matrixSize - 7, 0)

      // Draw Timing patterns
      for (i in 7 until matrixSize - 7) {
        if (i % 2 == 0) {
          drawRect(color = darkColor, topLeft = Offset(6 * cellSize, i * cellSize), size = Size(cellSize, cellSize))
          drawRect(color = darkColor, topLeft = Offset(i * cellSize, 6 * cellSize), size = Size(cellSize, cellSize))
        }
      }

      // Fill data matrix deterministically
      for (r in 0 until matrixSize) {
        for (c in 0 until matrixSize) {
          if (isInFinder(r, c) || (r == 6 && c in 7 until matrixSize - 7) || (c == 6 && r in 7 until matrixSize - 7)) {
            continue
          }
          // Center area reserved for logo
          if (r in (matrixSize / 2 - 2)..(matrixSize / 2 + 2) && c in (matrixSize / 2 - 2)..(matrixSize / 2 + 2)) {
            continue
          }
          val bit = ((hash * (r + 13) * (c + 17) + r * 31 + c * 37) % 100) > 42
          if (bit) {
            drawRoundRect(
              color = darkColor,
              topLeft = Offset(c * cellSize + cellSize * 0.05f, r * cellSize + cellSize * 0.05f),
              size = Size(cellSize * 0.9f, cellSize * 0.9f),
              cornerRadius = CornerRadius(cellSize * 0.2f, cellSize * 0.2f)
            )
          }
        }
      }
    }

    // Center Emblem (Legal Scales badge)
    Surface(
      modifier = Modifier.size(36.dp),
      shape = RoundedCornerShape(8.dp),
      color = GoldDark,
      shadowElevation = 4.dp,
      border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = Icons.Default.Gavel,
          contentDescription = "شعار منصة متر للتحقق",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

/**
 * Lawyer's Screen: Displays the QR code for meeting confirmation and starting the service.
 */
@Composable
fun LawyerMeetingQrCard(
  request: ServiceRequest,
  modifier: Modifier = Modifier
) {
  val clipboard = LocalClipboardManager.current
  var copied by remember { mutableStateOf(false) }
  val token = request.meetingQrToken ?: "MTR-MEET-${request.id.takeLast(3)}-9481"

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = if (request.isMeetingConfirmed) EmeraldContainer.copy(alpha = 0.6f) else CreamSurfaceVariant,
    border = androidx.compose.foundation.BorderStroke(
      width = 1.5.dp,
      color = if (request.isMeetingConfirmed) EmeraldSuccess else GoldSecondary
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = if (request.isMeetingConfirmed) EmeraldSuccess else GoldSecondary,
            modifier = Modifier.size(34.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (request.isMeetingConfirmed) Icons.Default.CheckCircle else Icons.Default.QrCode2,
                contentDescription = null,
                tint = if (request.isMeetingConfirmed) Color.White else NavyDark,
                modifier = Modifier.size(20.dp)
              )
            }
          }
          Column {
            Text(
              text = "رمز QR لتأكيد المقابلة وبدء الخدمة",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = NavyDark
            )
            Text(
              text = if (request.isMeetingConfirmed) "تم توثيق الالتقاء وبدء الخدمة بنجاح" else "لا يتفعل إلا بمسح كاميرا العميل",
              fontSize = 11.sp,
              color = if (request.isMeetingConfirmed) EmeraldSuccess else GoldDark,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (request.isMeetingConfirmed) EmeraldSuccess else GoldContainer,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (request.isMeetingConfirmed) EmeraldSuccess else GoldSecondary
          )
        ) {
          Text(
            text = if (request.isMeetingConfirmed) "مؤكد ومفعل ✓" else "بانتظار العميل ⏳",
            color = if (request.isMeetingConfirmed) Color.White else GoldDark,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Divider(color = BorderSubtle)

      if (!request.isMeetingConfirmed) {
        // Active QR Code to be scanned
        Text(
          text = "يُرجى إظهار هذا الرمز للموكل عند الالتقاء الشخصي أو في مقر المحكمة/المكتب، ليقوم بمسحه عبر زر (كاميرا بدء العمل) بتطبيقه لتأكيد بدء الخدمة وتوثيق الحضور رسمياً.",
          fontSize = 12.sp,
          color = TextPrimary,
          lineHeight = 18.sp,
          textAlign = TextAlign.Center
        )

        Box(
          modifier = Modifier
            .size(210.dp)
            .border(2.dp, GoldSecondary, RoundedCornerShape(14.dp))
            .padding(6.dp),
          contentAlignment = Alignment.Center
        ) {
          QrCodeCanvas(
            token = token,
            modifier = Modifier.fillMaxSize()
          )
        }

        // Token code pill with copy action
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = NavyDark.copy(alpha = 0.08f),
          modifier = Modifier.clickable {
            clipboard.setText(AnnotatedString(token))
            copied = true
          }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "كود التحقق: $token",
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = NavyDark
            )
            Icon(
              imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
              contentDescription = "نسخ",
              tint = if (copied) EmeraldSuccess else TextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      } else {
        // Already Confirmed View
        Surface(
          color = Color.White,
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(22.dp))
              Text("تم إثبات المقابلة والالتقاء رسمياً بنجاح", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldSuccess)
            }
            Text(
              text = "• تاريخ ووقت المسح: ${request.meetingConfirmedAt ?: "اليوم"}",
              fontSize = 12.sp,
              color = TextPrimary
            )
            Text(
              text = "• وسيلة التوثيق: تم المسح المباشر بكاميرا العميل للرمز الحضوري المعتمد.",
              fontSize = 12.sp,
              color = TextSecondary
            )
            Text(
              text = "• حالة العمل: الخدمة القضائية قيد التنفيذ المباشر ومحفظة الضمان مفعلة.",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }
        }
      }
    }
  }
}

/**
 * Client's Screen: Displays the "Start Work Camera" banner/button or verified confirmation.
 */
@Composable
fun ClientStartWorkCameraBanner(
  request: ServiceRequest,
  onOpenScanner: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = if (request.isMeetingConfirmed) EmeraldContainer.copy(alpha = 0.7f) else NavyDark,
    border = androidx.compose.foundation.BorderStroke(
      width = 1.5.dp,
      color = if (request.isMeetingConfirmed) EmeraldSuccess else GoldSecondary
    ),
    shadowElevation = 3.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = if (request.isMeetingConfirmed) EmeraldSuccess else GoldSecondary,
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (request.isMeetingConfirmed) Icons.Default.CheckCircle else Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = if (request.isMeetingConfirmed) Color.White else NavyDark,
                modifier = Modifier.size(20.dp)
              )
            }
          }
          Column {
            Text(
              text = if (request.isMeetingConfirmed) "تم تأكيد المقابلة وبدء الخدمة ✓" else "كاميرا بدء العمل (تأكيد المقابلة)",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = if (request.isMeetingConfirmed) Color(0xFF065F46) else Color.White
            )
            Text(
              text = if (request.isMeetingConfirmed) "الخدمة مفعلة وجاري العمل" else "امسح رمز QR الخاص بالمحامي عند الالتقاء",
              fontSize = 11.sp,
              color = if (request.isMeetingConfirmed) EmeraldSuccess else GoldLight
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (request.isMeetingConfirmed) EmeraldSuccess else GoldSecondary.copy(alpha = 0.25f)
        ) {
          Text(
            text = if (request.isMeetingConfirmed) "مكتمل التوثيق" else "إجراء مطلوب",
            color = if (request.isMeetingConfirmed) Color.White else GoldLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      if (!request.isMeetingConfirmed) {
        Text(
          text = "للتأكد من تمام الالتقاء بمحاميك وبدء الخدمة وتوثيق الحضور رسمياً، اضغط الزر أدناه لفتح الكاميرا ومسح رمز الـ QR المعروض على شاشة المحامي.",
          fontSize = 12.sp,
          color = Color.White.copy(alpha = 0.9f),
          lineHeight = 18.sp
        )

        Button(
          onClick = onOpenScanner,
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldSecondary,
            contentColor = NavyDark
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "فتح كاميرا بدء العمل لمسح رمز المحامي",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      } else {
        Text(
          text = "تم مسح الرمز بنجاح وتوثيق بدء الخدمة رسمياً في (${request.meetingConfirmedAt ?: "اليوم"}). كافة الإجراءات القانونية محمية تحت مظلة منصة مِتر وضمان الأتعاب.",
          fontSize = 12.sp,
          color = TextPrimary,
          lineHeight = 18.sp
        )
      }
    }
  }
}

/**
 * Camera Scanner Dialog simulating camera viewfinder with animated laser line,
 * torch toggle, and verification confirmation.
 */
@Composable
fun MeetingQrScannerDialog(
  request: ServiceRequest,
  onDismiss: () -> Unit,
  onConfirmMeeting: () -> Unit
) {
  var isTorchOn by remember { mutableStateOf(false) }
  var isSuccessScanned by remember { mutableStateOf(false) }
  var manualCodeInput by remember { mutableStateOf("") }
  var showManualInput by remember { mutableStateOf(false) }

  // Laser scanner animation
  val infiniteTransition = rememberInfiniteTransition()
  val laserOffsetRatio by infiniteTransition.animateFloat(
    initialValue = 0.1f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    )
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black),
      color = Color.Black
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        // Simulated Camera Viewfinder feed
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          // Top bar with header and flash toggle
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .statusBarsPadding()
              .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
              Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
            }

            Text(
              text = "كاميرا بدء العمل القانوني",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )

            IconButton(
              onClick = { isTorchOn = !isTorchOn },
              modifier = Modifier
                .background(
                  if (isTorchOn) GoldSecondary else Color.White.copy(alpha = 0.2f),
                  CircleShape
                )
            ) {
              Icon(
                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "فلاش",
                tint = if (isTorchOn) NavyDark else Color.White
              )
            }
          }

          // Case Info Badge
          Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.Security, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(16.dp))
              Text(
                text = "وجّه الكاميرا نحو شاشة هاتف المحامي",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          // Viewfinder Target Frame
          Box(
            modifier = Modifier
              .size(260.dp)
              .clip(RoundedCornerShape(20.dp))
              .background(Color.White.copy(alpha = 0.05f))
              .border(
                width = 2.dp,
                color = if (isSuccessScanned) EmeraldSuccess else GoldSecondary,
                shape = RoundedCornerShape(20.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            if (!isSuccessScanned) {
              // Target Frame Corners & Animated Laser
              Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cornerLength = 32.dp.toPx()
                val cornerStroke = 4.dp.toPx()
                val laserY = h * laserOffsetRatio

                // Draw Scanner Laser line
                drawLine(
                  color = EmeraldSuccess,
                  start = Offset(16.dp.toPx(), laserY),
                  end = Offset(w - 16.dp.toPx(), laserY),
                  strokeWidth = 3.dp.toPx()
                )

                // Corner indicators
                // Top-Left
                drawLine(color = GoldSecondary, start = Offset(0f, 0f), end = Offset(cornerLength, 0f), strokeWidth = cornerStroke)
                drawLine(color = GoldSecondary, start = Offset(0f, 0f), end = Offset(0f, cornerLength), strokeWidth = cornerStroke)

                // Top-Right
                drawLine(color = GoldSecondary, start = Offset(w, 0f), end = Offset(w - cornerLength, 0f), strokeWidth = cornerStroke)
                drawLine(color = GoldSecondary, start = Offset(w, 0f), end = Offset(w, cornerLength), strokeWidth = cornerStroke)

                // Bottom-Left
                drawLine(color = GoldSecondary, start = Offset(0f, h), end = Offset(cornerLength, h), strokeWidth = cornerStroke)
                drawLine(color = GoldSecondary, start = Offset(0f, h), end = Offset(0f, h - cornerLength), strokeWidth = cornerStroke)

                // Bottom-Right
                drawLine(color = GoldSecondary, start = Offset(w, h), end = Offset(w - cornerLength, h), strokeWidth = cornerStroke)
                drawLine(color = GoldSecondary, start = Offset(w, h), end = Offset(w, h - cornerLength), strokeWidth = cornerStroke)
              }
            } else {
              // Success Feedback inside frame
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = EmeraldSuccess,
                  modifier = Modifier.size(60.dp)
                )
                Text(
                  text = "تم المسح والتحقق بنجاح!",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              }
            }
          }

          // Bottom Controls & Confirmation
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .navigationBarsPadding()
              .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (!isSuccessScanned) {
              if (showManualInput) {
                Surface(
                  color = Color.Black.copy(alpha = 0.8f),
                  shape = RoundedCornerShape(12.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedTextField(
                      value = manualCodeInput,
                      onValueChange = { manualCodeInput = it.uppercase() },
                      placeholder = { Text("أدخل الكود (مثال: ${request.meetingQrToken ?: "MTR-MEET-101-8492"})", fontSize = 11.sp) },
                      modifier = Modifier.weight(1f),
                      singleLine = true,
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldSecondary,
                        unfocusedBorderColor = Color.Gray
                      )
                    )
                    Button(
                      onClick = {
                        isSuccessScanned = true
                        onConfirmMeeting()
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark)
                    ) {
                      Text("تأكيد", fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }

              // Main simulated scan confirmation button
              Button(
                onClick = {
                  isSuccessScanned = true
                  onConfirmMeeting()
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = EmeraldSuccess,
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
              ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "تأكيد مسح الرمز من شاشة المحامي الآن 📷",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
              }

              TextButton(onClick = { showManualInput = !showManualInput }) {
                Text(
                  text = if (showManualInput) "العودة للمسح بالكاميرا" else "أو إدخال رمز التحقق يدوياً",
                  color = GoldLight,
                  fontSize = 12.sp
                )
              }
            } else {
              Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
              ) {
                Text("تم - متابعة تنفيذ القضية", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              }
            }
          }
        }
      }
    }
  }
}
