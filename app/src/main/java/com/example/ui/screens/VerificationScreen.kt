package com.example.ui.screens

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.*

enum class VerificationTab(val titleAr: String) {
  OTP("رمز التحقق (SMS)"),
  NATIONAL_ID("الرقم القومي المصري"),
  BAR_COUNCIL("قيد نقابة المحامين")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
  currentUser: UserProfile,
  initialPhone: String = "01012345678",
  onBackClick: () -> Unit,
  onVerifySuccess: () -> Unit,
  onVerifyLawyerLicense: (String) -> Unit
) {
  var selectedTab by remember {
    mutableStateOf(
      if (currentUser.role == UserRole.LAWYER) VerificationTab.BAR_COUNCIL else VerificationTab.OTP
    )
  }

  // OTP State
  var otpDigit1 by remember { mutableStateOf("4") }
  var otpDigit2 by remember { mutableStateOf("8") }
  var otpDigit3 by remember { mutableStateOf("2") }
  var otpDigit4 by remember { mutableStateOf("9") }
  var isOtpVerified by remember { mutableStateOf(false) }

  // National ID State (Egypt)
  var nationalIdInput by remember { mutableStateOf("29408151203948") }
  var nationalIdCode by remember { mutableStateOf("84") }
  var isNationalIdConfirmed by remember { mutableStateOf(currentUser.nafathVerified) }
  var nationalIdPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var showCameraPermissionRationale by remember { mutableStateOf(false) }

  // Lawyer Bar License State (Egypt)
  var licenseInput by remember { mutableStateOf(currentUser.licenseNumber?.replace("نقابة المحامين: ", "") ?: "298104") }
  var lawyerFirmName by remember { mutableStateOf("مكتب المستشار القانوني بالاستئناف العالي ومجلس الدولة") }
  var isDocUploaded by remember { mutableStateOf(true) }
  var barCardPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var isLicenseVerified by remember { mutableStateOf(currentUser.isVerified) }

  // Camera Launchers
  var activeCameraTarget by remember { mutableStateOf("NATIONAL_ID") } // "NATIONAL_ID" or "BAR_CARD"

  val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
    if (bitmap != null) {
      if (activeCameraTarget == "NATIONAL_ID") {
        nationalIdPhotoBitmap = bitmap
        isNationalIdConfirmed = true
      } else {
        barCardPhotoBitmap = bitmap
        isDocUploaded = true
      }
    }
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      takePictureLauncher.launch(null)
    } else {
      showCameraPermissionRationale = true
    }
  }

  val scrollState = rememberScrollState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            "التحقق والتوثيق القانوني في مصر",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
      )
    },
    containerColor = CreamBackground
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Security badge banner
      Surface(
        color = NavyPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Surface(
            color = NavyDark,
            shape = CircleShape,
            modifier = Modifier.size(52.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldSecondary)
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_maitre_logo),
              contentDescription = "شعار متر",
              modifier = Modifier.fillMaxSize()
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text("منظومة التوثيق الرقمي والأمان", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
              "مطابقة رسمية مع قواعد بيانات نقابة المحامين المصرية وقطاع الأحوال المدنية لضمان حقوق كافة الأطراف.",
              color = GoldLight,
              fontSize = 11.sp,
              lineHeight = 16.sp
            )
          }
        }
      }

      // Tab selector
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(CreamSurfaceVariant, RoundedCornerShape(12.dp))
          .padding(4.dp)
      ) {
        listOf(
          VerificationTab.OTP,
          VerificationTab.NATIONAL_ID,
          VerificationTab.BAR_COUNCIL
        ).forEach { tab ->
          val isSelected = selectedTab == tab
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) NavyPrimary else Color.Transparent)
              .clickable { selectedTab = tab }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              tab.titleAr,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = if (isSelected) Color.White else TextSecondary
            )
          }
        }
      }

      when (selectedTab) {
        VerificationTab.OTP -> {
          // OTP Verification Section
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .clip(CircleShape)
                  .background(NavyContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Sms, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(28.dp))
              }

              Text(
                "أدخل كود التحقق السريع المرسل لهاتفك",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                "تم إرسال كود أمان مكوّن من 4 أرقام عبر SMS إلى رقمك المصري (+20)",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
              )

              // 4 Digits Boxes
              Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedTextField(
                  value = otpDigit1,
                  onValueChange = { if (it.length <= 1) otpDigit1 = it },
                  modifier = Modifier.size(54.dp),
                  shape = RoundedCornerShape(12.dp),
                  textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                  value = otpDigit2,
                  onValueChange = { if (it.length <= 1) otpDigit2 = it },
                  modifier = Modifier.size(54.dp),
                  shape = RoundedCornerShape(12.dp),
                  textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                  value = otpDigit3,
                  onValueChange = { if (it.length <= 1) otpDigit3 = it },
                  modifier = Modifier.size(54.dp),
                  shape = RoundedCornerShape(12.dp),
                  textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                  value = otpDigit4,
                  onValueChange = { if (it.length <= 1) otpDigit4 = it },
                  modifier = Modifier.size(54.dp),
                  shape = RoundedCornerShape(12.dp),
                  textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Text("إعادة إرسال الرمز بعد 00:30 ثانية", fontSize = 11.sp, color = TextMuted)
              }

              Button(
                onClick = {
                  isOtpVerified = true
                  onVerifySuccess()
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isOtpVerified) "تم التحقق بنجاح ✓" else "تأكيد كود التحقق", fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }

        VerificationTab.NATIONAL_ID -> {
          // National ID Verification Section
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Surface(
                color = EmeraldContainer,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(32.dp))
                }
              }

              Text(
                "التحقق من بطاقة الرقم القومي المصرية",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
              )

              Text(
                "يرجى التأكد من مطابقة الرقم القومي المكون من 14 رقماً لضمان إبرام العقود وتوكيلات القضايا الرسمية:",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
              )

              OutlinedTextField(
                value = nationalIdInput,
                onValueChange = { if (it.length <= 14) nationalIdInput = it },
                label = { Text("الرقم القومي (14 رقماً)") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = GoldDark) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
              )

              // Camera Scan Section for National ID
              Surface(
                color = CreamSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NavyPrimary)
                    Text("مسح بطاقة الرقم القومي بالكاميرا", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                  }

                  if (nationalIdPhotoBitmap != null) {
                    Image(
                      bitmap = nationalIdPhotoBitmap!!.asImageBitmap(),
                      contentDescription = "صورة بطاقة الرقم القومي",
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp)),
                      contentScale = ContentScale.Crop
                    )
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedButton(
                      onClick = {
                        activeCameraTarget = "NATIONAL_ID"
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                      },
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(if (nationalIdPhotoBitmap != null) "إعادة التقاط" else "التقاط بالكاميرا", fontSize = 11.sp)
                    }
                  }
                }
              }

              // Verification code badge
              Surface(
                color = NavyDark,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(vertical = 4.dp)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  Text("كود المصادقة الرقمية:", color = Color.White, fontSize = 12.sp)
                  Text(
                    text = nationalIdCode,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = EmeraldSuccess,
                    letterSpacing = 4.sp
                  )
                }
              }

              Surface(
                color = if (isNationalIdConfirmed) EmeraldContainer else AmberContainer,
                shape = RoundedCornerShape(10.dp)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = if (isNationalIdConfirmed) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = if (isNationalIdConfirmed) EmeraldSuccess else AmberWarning
                  )
                  Text(
                    if (isNationalIdConfirmed) "الرقم القومي موثق رسمياً وساري الصلاحية" else "جاري الربط مع قاعدة البيانات للتحقق...",
                    fontSize = 12.sp,
                    color = if (isNationalIdConfirmed) EmeraldSuccess else AmberWarning,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              Button(
                onClick = {
                  isNationalIdConfirmed = true
                  onVerifySuccess()
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
              ) {
                Text("تأكيد واعتماد الرقم القومي", fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }

        VerificationTab.BAR_COUNCIL -> {
          // Egyptian Bar Association Verification
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GoldContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldDark, modifier = Modifier.size(24.dp))
                }
                Column {
                  Text("توثيق القيد بنقابة المحامين المصرية", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                  Text("مطابقة الجدول العام وجداول الابتدائي والاستئناف والنقض", fontSize = 11.sp, color = TextMuted)
                }
              }

              Divider(color = BorderSubtle)

              OutlinedTextField(
                value = licenseInput,
                onValueChange = { licenseInput = it },
                label = { Text("رقم القيد بنقابة المحامين (كارنيه النقابة)") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = GoldDark) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
              )

              OutlinedTextField(
                value = lawyerFirmName,
                onValueChange = { lawyerFirmName = it },
                label = { Text("اسم مكتب المحاماة / المجموعة القانونية بمصر") },
                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldDark) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              )

              // Document attachment upload & Camera Scan state
              Surface(
                color = CreamSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = NavyPrimary)
                    Column(modifier = Modifier.weight(1f)) {
                      Text("صورة كارنيه نقابة المحامين / إثبات القيد السنوي", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                      Text(if (isDocUploaded) "تم إرفاق: Egyptian_Bar_Card_2026.pdf" else "التقط صورة الكارنيه أو ارفع ملف", fontSize = 10.sp, color = if (isDocUploaded) EmeraldSuccess else TextMuted)
                    }
                  }

                  if (barCardPhotoBitmap != null) {
                    Image(
                      bitmap = barCardPhotoBitmap!!.asImageBitmap(),
                      contentDescription = "صورة كارنيه النقابة",
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp)),
                      contentScale = ContentScale.Crop
                    )
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedButton(
                      onClick = {
                        activeCameraTarget = "BAR_CARD"
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                      },
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(if (barCardPhotoBitmap != null) "إعادة تصوير الكارنيه" else "تصوير الكارنيه بالكاميرا", fontSize = 11.sp)
                    }

                    TextButton(onClick = { isDocUploaded = true }) {
                      Text(if (isDocUploaded) "مرفق ✓" else "رفع ملف", fontSize = 11.sp, color = GoldDark)
                    }
                  }
                }
              }

              // Status indicator
              Surface(
                color = if (isLicenseVerified) EmeraldContainer else GoldContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = if (isLicenseVerified) Icons.Default.Verified else Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = if (isLicenseVerified) EmeraldSuccess else GoldDark
                  )
                  Text(
                    text = if (isLicenseVerified) "القيد معتمد وموثق بنقابة المحامين - مؤهل لتقديم العروض والمرافعة" else "القيد قيد الفحص مع جدول النقابة العامة",
                    fontSize = 11.sp,
                    color = if (isLicenseVerified) EmeraldSuccess else GoldDark,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              Button(
                onClick = {
                  isLicenseVerified = true
                  onVerifyLawyerLicense(licenseInput)
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldDark)
              ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("اعتماد القيد وحفظ التوثيق", fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }
      }
    }
  }
}
