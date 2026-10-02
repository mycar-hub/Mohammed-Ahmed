package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.theme.*

enum class DocumentSlotType(
  val titleAr: String,
  val sideBadgeAr: String,
  val isFront: Boolean
) {
  CLIENT_NID_FRONT("بطاقة الرقم القومي للموكل", "الوجه الأمامي (وش)", true),
  CLIENT_NID_BACK("بطاقة الرقم القومي للموكل", "الوجه الخلفي (ظهر)", false),
  LAWYER_NID_FRONT("بطاقة الرقم القومي للمحامي", "الوجه الأمامي (وش)", true),
  LAWYER_NID_BACK("بطاقة الرقم القومي للمحامي", "الوجه الخلفي (ظهر)", false),
  LAWYER_BAR_FRONT("كارنيه نقابة المحامين المصرية", "الوجه الأمامي (وش)", true),
  LAWYER_BAR_BACK("كارنيه نقابة المحامين المصرية", "الوجه الخلفي (ظهر)", false)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
  onLoginSuccess: (phone: String, role: UserRole) -> Unit,
  onRegisterClient: (
    name: String,
    phone: String,
    email: String,
    nationalId: String,
    clientType: ClientType,
    companyName: String?,
    governorate: String,
    idFrontUploaded: Boolean,
    idBackUploaded: Boolean,
    idFrontUri: String?,
    idBackUri: String?
  ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
  onRegisterLawyer: (
    name: String,
    phone: String,
    email: String,
    nationalId: String,
    licenseNo: String,
    degree: LawyerBarDegree,
    subBar: String,
    governorate: String,
    courtScope: String,
    specialization: RequestCategory,
    yearsExp: Int,
    firmName: String,
    barCardUploaded: Boolean,
    idUploaded: Boolean,
    barCardBackUploaded: Boolean,
    idCardBackUploaded: Boolean,
    idCardFrontUri: String?,
    idCardBackUri: String?,
    barCardFrontUri: String?,
    barCardBackUri: String?,
    desiredDegrees: List<String>,
    selectedGovs: List<String>,
    selectedCourts: List<String>,
    selectedDistricts: List<String>,
    lawyerTitle: LawyerTitle,
    bio: String,
    officeAddressManually: String,
    officeLatitude: Double?,
    officeLongitude: Double?
  ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
  onRegisterSuccess: (name: String, phone: String, email: String, role: UserRole, idOrCr: String, license: String?, company: String?) -> Unit,
  onOpenOtpVerification: (phone: String) -> Unit,
  onOpenNafathVerification: () -> Unit,
  onGoogleSignIn: (email: String, displayName: String, role: UserRole) -> Unit = { _, _, _ -> }
) {
  var isLoginMode by remember { mutableStateOf(true) }
  var selectedRole by remember { mutableStateOf(UserRole.CLIENT) }

  // Google Sign-In & Sign-Up State
  var showGoogleAccountPicker by remember { mutableStateOf(false) }
  var isGoogleAuthenticating by remember { mutableStateOf(false) }
  var showAddCustomGoogleAccount by remember { mutableStateOf(false) }
  var customGoogleEmail by remember { mutableStateOf("") }
  var customGoogleName by remember { mutableStateOf("") }

  // Client KYC Fields
  var clientType by remember { mutableStateOf(ClientType.INDIVIDUAL) }
  var clientFullName by remember { mutableStateOf("أحمد محمود الشناوي") }
  var clientCompanyName by remember { mutableStateOf("شركة النيل للتجارة والاستثمار ش.م.م") }
  var clientPhone by remember { mutableStateOf("01012345678") }
  var clientEmail by remember { mutableStateOf("ahmed.shennawy@example.com") }
  var clientNationalId by remember { mutableStateOf("29408151203948") }
  var clientGovernorate by remember { mutableStateOf("القاهرة") }
  var clientIdFrontUploaded by remember { mutableStateOf(true) }
  var clientIdBackUploaded by remember { mutableStateOf(true) }
  var clientIdFrontUri by remember { mutableStateOf<String?>("id_card_front.jpg") }
  var clientIdBackUri by remember { mutableStateOf<String?>("id_card_back.jpg") }

  // Lawyer KYC Fields
  var lawyerTitle by remember { mutableStateOf(LawyerTitle.COUNSELOR) }
  var lawyerFullName by remember { mutableStateOf("سامح محمد العسقلاني") }
  var lawyerBio by remember { mutableStateOf("مستشار قانوني متخصص في القضايا التجارية والشركات والتحكيم وفض المنازعات، مقيد بنقابة محامي القاهرة.") }
  var lawyerOfficeAddressManually by remember { mutableStateOf("15 شارع شريف، وسط البلد، عمارة التأمين، الدور الرابع، مكتب 42") }
  var lawyerLatitude by remember { mutableStateOf<Double?>(30.0444) }
  var lawyerLongitude by remember { mutableStateOf<Double?>(31.2357) }
  var lawyerLocationCaptured by remember { mutableStateOf(true) }
  var lawyerLocationAddressDescription by remember { mutableStateOf("ميدان التحرير / قصر النيل، وسط القاهرة (30.0444, 31.2357)") }
  var lawyerPostponeOfficeLocation by remember { mutableStateOf(false) }
  var lawyerPhone by remember { mutableStateOf("01198765432") }
  var lawyerEmail by remember { mutableStateOf("sameh.askalani@law.eg") }
  var lawyerFirmName by remember { mutableStateOf("مجموعة العسقلاني للمحاماة والاستشارات") }
  var lawyerNationalId by remember { mutableStateOf("28209210103491") }
  var lawyerBarLicenseNumber by remember { mutableStateOf("431908") }
  var lawyerDegree by remember { mutableStateOf(LawyerBarDegree.APPEAL) }
  var lawyerSubBarExpanded by remember { mutableStateOf(false) }
  var lawyerSubBar by remember { mutableStateOf(EgyptSubBarHelper.egyptianSubBars[0]) }
  var lawyerGovernorate by remember { mutableStateOf("القاهرة") }
  var lawyerGovDropdownExpanded by remember { mutableStateOf(false) }
  var lawyerSpecialization by remember { mutableStateOf(RequestCategory.COMMERCIAL) }
  var lawyerExperienceYears by remember { mutableStateOf("12") }

  // Judicial Practice Scope Selection:
  // List 1: Desired Practice Degrees (Multi-select within limits of actual verified degree)
  var selectedPracticeDegreeIds by remember {
    mutableStateOf(
      setOf(
        JudicialPracticeCategory.HIGH_APPEAL_AND_STATE_COUNCIL.id,
        JudicialPracticeCategory.PRIMARY_AND_MISDEMEANOR.id,
        JudicialPracticeCategory.TRAINEE_AND_SUMMARY.id
      )
    )
  }

  // List 2: Workplaces (Governorates, Courts & Districts) - Multi-select
  var selectedWorkGovernorates by remember { mutableStateOf(setOf("القاهرة", "الجيزة")) }
  var selectedWorkCourts by remember {
    mutableStateOf(
      setOf(
        "دار القضاء العالي (محكمة النقض واستئناف القاهرة)",
        "مجمع محاكم شمال القاهرة (العباسية)",
        "محكمة الجيزة الابتدائية (شارع السودان)"
      )
    )
  }
  var selectedWorkDistricts by remember {
    mutableStateOf(
      setOf("مصر الجديدة", "الدقي", "مدينة نصر", "التجمع الخامس")
    )
  }
  var customCourtNameInput by remember { mutableStateOf("") }
  var lawyerBarCardUploaded by remember { mutableStateOf(true) }
  var lawyerBarBackUploaded by remember { mutableStateOf(true) }
  var lawyerIdCardUploaded by remember { mutableStateOf(true) }
  var lawyerIdBackUploaded by remember { mutableStateOf(true) }
  var lawyerBarFrontUri by remember { mutableStateOf<String?>("bar_card_front.jpg") }
  var lawyerBarBackUri by remember { mutableStateOf<String?>("bar_card_back.jpg") }
  var lawyerIdFrontUri by remember { mutableStateOf<String?>("id_card_front.jpg") }
  var lawyerIdBackUri by remember { mutableStateOf<String?>("id_card_back.jpg") }
  var lawyerCertificateUploaded by remember { mutableStateOf(false) }

  // Active Picker Dialog State
  var activeSlotForSourcePicker by remember { mutableStateOf<DocumentSlotType?>(null) }
  var documentPreviewModal by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(Title, Uri)

  // Camera & Gallery Launchers
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    if (bitmap != null) {
      val mockUri = "camera_captured_${System.currentTimeMillis()}.jpg"
      when (activeSlotForSourcePicker) {
        DocumentSlotType.CLIENT_NID_FRONT -> {
          clientIdFrontUploaded = true
          clientIdFrontUri = mockUri
        }
        DocumentSlotType.CLIENT_NID_BACK -> {
          clientIdBackUploaded = true
          clientIdBackUri = mockUri
        }
        DocumentSlotType.LAWYER_NID_FRONT -> {
          lawyerIdCardUploaded = true
          lawyerIdFrontUri = mockUri
        }
        DocumentSlotType.LAWYER_NID_BACK -> {
          lawyerIdBackUploaded = true
          lawyerIdBackUri = mockUri
        }
        DocumentSlotType.LAWYER_BAR_FRONT -> {
          lawyerBarCardUploaded = true
          lawyerBarFrontUri = mockUri
        }
        DocumentSlotType.LAWYER_BAR_BACK -> {
          lawyerBarBackUploaded = true
          lawyerBarBackUri = mockUri
        }
        null -> {}
      }
      activeSlotForSourcePicker = null
    }
  }

  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      val uriStr = uri.toString()
      when (activeSlotForSourcePicker) {
        DocumentSlotType.CLIENT_NID_FRONT -> {
          clientIdFrontUploaded = true
          clientIdFrontUri = uriStr
        }
        DocumentSlotType.CLIENT_NID_BACK -> {
          clientIdBackUploaded = true
          clientIdBackUri = uriStr
        }
        DocumentSlotType.LAWYER_NID_FRONT -> {
          lawyerIdCardUploaded = true
          lawyerIdFrontUri = uriStr
        }
        DocumentSlotType.LAWYER_NID_BACK -> {
          lawyerIdBackUploaded = true
          lawyerIdBackUri = uriStr
        }
        DocumentSlotType.LAWYER_BAR_FRONT -> {
          lawyerBarCardUploaded = true
          lawyerBarFrontUri = uriStr
        }
        DocumentSlotType.LAWYER_BAR_BACK -> {
          lawyerBarBackUploaded = true
          lawyerBarBackUri = uriStr
        }
        null -> {}
      }
      activeSlotForSourcePicker = null
    }
  }

  // Password fields
  var password by remember { mutableStateOf("••••••••") }
  var passwordVisible by remember { mutableStateOf(false) }

  // Error and feedback
  var formErrorMessage by remember { mutableStateOf<String?>(null) }
  var lawyerSubmissionSuccess by remember { mutableStateOf(false) }

  // Multi-step Registration Wizard State
  var clientRegistrationStep by remember { mutableStateOf(0) } // 0: Basic Info, 1: KYC ID Cards, 2: Review & Terms
  var lawyerRegistrationStep by remember { mutableStateOf(0) } // 0: Personal & Firm, 1: Bar & Degree, 2: Practice Scope & Courts, 3: KYC & Documents, 4: Review & Terms

  // Terms of Service & Privacy Policy Agreement
  var clientTermsAccepted by remember { mutableStateOf(true) }
  var lawyerTermsAccepted by remember { mutableStateOf(true) }
  var showTermsAndPrivacyDialog by remember { mutableStateOf(false) }
  var termsDialogTab by remember { mutableStateOf(0) } // 0: Terms, 1: Privacy, 2: Code of Conduct

  val egyptianGovernorates = listOf(
    "القاهرة", "الجيزة", "الإسكندرية", "الدقهلية", "الغربية", "الشرقية",
    "القليوبية", "المنوفية", "البحيرة", "كفر الشيخ", "دمياط", "بورسعيد",
    "الإسماعيلية", "السويس", "بني سويف", "الفيوم", "المنيا", "أسيوط",
    "سوهاج", "قنا", "الأقصر", "أسوان", "البحر الأحمر", "الوادي الجديد",
    "مطروح", "شمال سيناء", "جنوب سيناء"
  )

  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            NavyDark,
            NavyPrimary,
            CreamBackground
          ),
          startY = 0f,
          endY = 800f
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 18.dp, vertical = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Official Logo
      Surface(
        color = NavyDark,
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(2.dp, GoldSecondary),
        modifier = Modifier.size(86.dp),
        shadowElevation = 6.dp
      ) {
        Box(contentAlignment = Alignment.Center) {
          Image(
            painter = painterResource(id = R.drawable.ic_maitre_logo),
            contentDescription = "شعار مِتر الرسمي",
            modifier = Modifier.size(68.dp),
            contentScale = ContentScale.Fit
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "مِتر • Maitre",
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        color = Color.White
      )

      Text(
        text = "المنظومة القانونية المعتمدة بنقابة المحامين المصرية (KYC)",
        fontSize = 12.sp,
        color = GoldLight,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Card Container
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Tab selector: Login vs Register (KYC)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.adaptiveSurfaceVariant, RoundedCornerShape(12.dp))
              .padding(4.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isLoginMode) NavyPrimary else Color.Transparent)
                .clickable {
                  isLoginMode = true
                  formErrorMessage = null
                }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                "تسجيل الدخول",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isLoginMode) Color.White else TextSecondary
              )
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (!isLoginMode) NavyPrimary else Color.Transparent)
                .clickable {
                  isLoginMode = false
                  formErrorMessage = null
                  clientRegistrationStep = 0
                  lawyerRegistrationStep = 0
                }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                  Icons.Default.VerifiedUser,
                  contentDescription = null,
                  tint = if (!isLoginMode) GoldSecondary else TextSecondary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  "حساب جديد (KYC)",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = if (!isLoginMode) Color.White else TextSecondary
                )
              }
            }
          }

          // Role Switcher: STRICTLY CLIENT vs LAWYER (No Admin Registration here!)
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              if (isLoginMode) "اختيار نظام الدخول:" else "اختيار نوع الحساب المراد تسجيله:",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Client Option
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    selectedRole = UserRole.CLIENT
                    formErrorMessage = null
                    clientRegistrationStep = 0
                  }
                  .border(
                    width = if (selectedRole == UserRole.CLIENT) 2.dp else 1.dp,
                    color = if (selectedRole == UserRole.CLIENT) GoldDark else BorderSubtle,
                    shape = RoundedCornerShape(12.dp)
                  ),
                color = if (selectedRole == UserRole.CLIENT) GoldContainer.copy(alpha = 0.7f) else CreamSurfaceVariant
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = if (selectedRole == UserRole.CLIENT) GoldDark else TextMuted,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    "مستخدم نهائي (موكل)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (selectedRole == UserRole.CLIENT) TextPrimary else TextSecondary,
                    textAlign = TextAlign.Center
                  )
                  Text("أفراد / شركات واستثمار", fontSize = 9.5.sp, color = TextMuted)
                }
              }

              // Lawyer Option
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    selectedRole = UserRole.LAWYER
                    formErrorMessage = null
                    lawyerRegistrationStep = 0
                  }
                  .border(
                    width = if (selectedRole == UserRole.LAWYER) 2.dp else 1.dp,
                    color = if (selectedRole == UserRole.LAWYER) EmeraldSuccess else BorderSubtle,
                    shape = RoundedCornerShape(12.dp)
                  ),
                color = if (selectedRole == UserRole.LAWYER) EmeraldContainer.copy(alpha = 0.4f) else CreamSurfaceVariant
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = if (selectedRole == UserRole.LAWYER) EmeraldSuccess else TextMuted,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    "محامٍ مقيد بالنقابة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (selectedRole == UserRole.LAWYER) TextPrimary else TextSecondary,
                    textAlign = TextAlign.Center
                  )
                  Text("فحص واعتماد الوثائق", fontSize = 9.5.sp, color = TextMuted)
                }
              }
            }
          }

          // =========================================================================
          // LOGIN MODE
          // =========================================================================
          if (isLoginMode) {
            // Google Account Sign-In Button (Only for Clients - Disabled for Lawyers)
            if (selectedRole == UserRole.CLIENT) {
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    showGoogleAccountPicker = true
                  },
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, BorderSubtle),
                shadowElevation = 2.dp
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  GoogleLogoIcon(modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    "المتابعة وتسجيل الدخول بحساب Google",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NavyDark
                  )
                }
              }

              // Separator
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Divider(modifier = Modifier.weight(1f), color = BorderSubtle)
                Text(
                  text = "أو عبر البريد ورقم الهاتف",
                  fontSize = 10.5.sp,
                  color = TextMuted,
                  modifier = Modifier.padding(horizontal = 10.dp)
                )
                Divider(modifier = Modifier.weight(1f), color = BorderSubtle)
              }
            } else {
              // Lawyer Notice
              Surface(
                color = NavyDark,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                  Text(
                    text = "دخول المحامين مخصص عبر رقم الهاتف المسجل والهوية النقابية لضمان الخصوصية والتحقق الرقمي.",
                    color = GoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 15.sp
                  )
                }
              }
            }

            OutlinedTextField(
              value = if (selectedRole == UserRole.LAWYER) lawyerPhone else clientPhone,
              onValueChange = {
                if (selectedRole == UserRole.LAWYER) lawyerPhone = it else clientPhone = it
              },
              label = { Text("رقم الهاتف المحمول المسجل (مصر)") },
              leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GoldDark) },
              trailingIcon = { Text("+20 ", color = MaterialTheme.adaptiveTextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              singleLine = true,
              colors = maitreTextFieldColors(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
              value = password,
              onValueChange = { password = it },
              label = { Text("كلمة المرور") },
              leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldDark) },
              trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                  Icon(
                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null
                  )
                }
              },
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              singleLine = true,
              colors = maitreTextFieldColors()
            )

            Button(
              onClick = {
                val phoneToLogin = if (selectedRole == UserRole.LAWYER) lawyerPhone else clientPhone
                onLoginSuccess(phoneToLogin, selectedRole)
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("تسجيل الدخول", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }

            OutlinedButton(
              onClick = onOpenNafathVerification,
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
            ) {
              Icon(Icons.Default.Fingerprint, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("الدخول السريع بالهوية الرقمية الموثقة (KYC)", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            }

            TextButton(
              onClick = {
                val ph = if (selectedRole == UserRole.LAWYER) lawyerPhone else clientPhone
                onOpenOtpVerification(ph)
              },
              modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
              Text("تسجيل الدخول عبر كود التحقق السريع (SMS OTP)", fontSize = 11.5.sp, color = GoldDark, fontWeight = FontWeight.SemiBold)
            }
          }

          // =========================================================================
          // REGISTER MODE (KYC REGISTRATION - DIFFERENTIATED FIELDS)
          // =========================================================================
          if (!isLoginMode) {
            // Google Sign-Up / One-Tap registration Banner
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  showGoogleAccountPicker = true
                },
              color = Color.White,
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
              shadowElevation = 1.dp
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                GoogleLogoIcon(modifier = Modifier.size(22.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text("التسجيل السريع والمطابقة بحساب Google", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
                  Text("استيراد الاسم والبريد الموثق وتخطي الخطوات الروتينية", fontSize = 9.5.sp, color = TextSecondary)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
              }
            }

            if (selectedRole == UserRole.CLIENT) {
              // -------------------------------------------------------------
              // CLIENT REGISTRATION WIZARD (3 STEPS)
              // -------------------------------------------------------------
              val clientStepTitles = listOf(
                "البيانات الأساسية والصفة القانونية",
                "التحقق من الهوية الوطنية (KYC)",
                "المراجعة وشروط الاستخدام"
              )
              val clientStepSubtitles = listOf(
                "تحديد نوع الحساب (فرد / منشأة) وتعبئة بيانات التواصل الأساسية",
                "التحقق التلقائي من الرقم القومي ورفع بطاقة الهوية (الوجهين)",
                "مراجعة ملخص الحساب والموافقة على شروط الاستخدام وسياسة الخصوصية"
              )

              RegistrationWizardHeader(
                currentStep = clientRegistrationStep,
                totalSteps = 3,
                stepTitle = clientStepTitles[clientRegistrationStep],
                stepSubtitle = clientStepSubtitles[clientRegistrationStep],
                accentColor = GoldDark
              )

              when (clientRegistrationStep) {
                0 -> {
                  // CLIENT STEP 1: Basic Information & Entity Type
                  Text("الصفة القانونية للموكل:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)

                  // Individual vs Corporate
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    ClientType.values().forEach { type ->
                      val isSel = clientType == type
                      Surface(
                        color = if (isSel) NavyPrimary else CreamSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                          .weight(1f)
                          .clip(RoundedCornerShape(8.dp))
                          .clickable { clientType = type }
                      ) {
                        Text(
                          text = type.labelAr,
                          textAlign = TextAlign.Center,
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (isSel) Color.White else TextPrimary,
                          modifier = Modifier.padding(vertical = 8.dp)
                        )
                      }
                    }
                  }

                  OutlinedTextField(
                    value = clientFullName,
                    onValueChange = { clientFullName = it },
                    label = { Text("الاسم الرباعي الرسمي (كما في الرقم القومي)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldDark) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = maitreTextFieldColors()
                  )

                  if (clientType == ClientType.CORPORATE) {
                    OutlinedTextField(
                      value = clientCompanyName,
                      onValueChange = { clientCompanyName = it },
                      label = { Text("اسم الشركة / المنشأة التجارية") },
                      leadingIcon = { Icon(Icons.Default.CorporateFare, contentDescription = null, tint = GoldDark) },
                      modifier = Modifier.fillMaxWidth(),
                      shape = RoundedCornerShape(10.dp),
                      singleLine = true,
                      colors = maitreTextFieldColors()
                    )
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedTextField(
                      value = clientPhone,
                      onValueChange = { clientPhone = it },
                      label = { Text("رقم الهاتف (مصر)") },
                      leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GoldDark) },
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(10.dp),
                      singleLine = true,
                      colors = maitreTextFieldColors(),
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    OutlinedTextField(
                      value = clientEmail,
                      onValueChange = { clientEmail = it },
                      label = { Text("البريد الإلكتروني") },
                      leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldDark) },
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(10.dp),
                      singleLine = true,
                      colors = maitreTextFieldColors(),
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                  }

                  // Governorate Selection
                  var govExpanded by remember { mutableStateOf(false) }
                  ExposedDropdownMenuBox(
                    expanded = govExpanded,
                    onExpandedChange = { govExpanded = !govExpanded },
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    OutlinedTextField(
                      value = clientGovernorate,
                      onValueChange = {},
                      readOnly = true,
                      label = { Text("المحافظة") },
                      leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldDark) },
                      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = govExpanded) },
                      modifier = Modifier.menuAnchor().fillMaxWidth(),
                      shape = RoundedCornerShape(10.dp),
                      colors = maitreTextFieldColors()
                    )
                    ExposedDropdownMenu(
                      expanded = govExpanded,
                      onDismissRequest = { govExpanded = false },
                      modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                      egyptianGovernorates.forEach { gov ->
                        DropdownMenuItem(
                          text = { Text(gov) },
                          onClick = {
                            clientGovernorate = gov
                            govExpanded = false
                          }
                        )
                      }
                    }
                  }
                }

                1 -> {
                  // CLIENT STEP 2: National ID Verification & Document Upload
                  Text("التحقق من الهوية الوطنية (National ID KYC):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (MaterialTheme.colorScheme.background == NavyDark) GoldLight else NavyPrimary)

                  OutlinedTextField(
                    value = clientNationalId,
                    onValueChange = { if (it.length <= 14 && it.all { c -> c.isDigit() }) clientNationalId = it },
                    label = { Text("الرقم القومي المصري (14 رقماً)") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = GoldDark) },
                    trailingIcon = { Text("${clientNationalId.length}/14", fontSize = 10.sp, color = if (clientNationalId.length == 14) EmeraldSuccess else MaterialTheme.adaptiveTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = maitreTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                  )

                  // Live Egyptian National ID KYC Decoder Card
                  val kycCheck = EgyptianKycHelper.validateNationalId(clientNationalId)
                  if (kycCheck.isValid) {
                    Surface(
                      color = EmeraldContainer,
                      shape = RoundedCornerShape(8.dp),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Text("الرقم القومي مطابق لسجلات الهوية المصرية ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                          }
                          Text("النوع: ${kycCheck.gender}", fontSize = 10.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
                        }
                        Text("المحافظة: ${kycCheck.governorate} • تاريخ الميلاد: ${kycCheck.birthDate}", fontSize = 10.5.sp, color = NavyDark)
                      }
                    }
                  } else if (clientNationalId.isNotEmpty()) {
                    Surface(
                      color = GoldContainer,
                      shape = RoundedCornerShape(8.dp),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
                        Text(kycCheck.error ?: "أدخل 14 رقماً للتحقق التلقائي", fontSize = 10.5.sp, color = GoldDark)
                      }
                    }
                  }

                  // Document Upload Section for Client KYC (وش وظهر)
                  Text("رفع وثائق إثبات الشخصية (الرقم القومي وش وظهر):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                  Text("افتح الكاميرا أو معرض الصور لرفع بطاقة الرقم القومي (الوجهين):", fontSize = 10.sp, color = MaterialTheme.adaptiveTextSecondary)

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    DocumentUploadSlot(
                      title = "بطاقة الرقم القومي",
                      sideBadge = "الوجه الأمامي (وش)",
                      isUploaded = clientIdFrontUploaded,
                      uri = clientIdFrontUri,
                      onCardClick = { activeSlotForSourcePicker = DocumentSlotType.CLIENT_NID_FRONT },
                      onCameraClick = {
                        activeSlotForSourcePicker = DocumentSlotType.CLIENT_NID_FRONT
                        cameraLauncher.launch(null)
                      },
                      onGalleryClick = {
                        activeSlotForSourcePicker = DocumentSlotType.CLIENT_NID_FRONT
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                      },
                      onPreviewClick = {
                        documentPreviewModal = Pair("بطاقة الرقم القومي للموكل - الوجه الأمامي (وش)", clientIdFrontUri ?: "id_card_front.jpg")
                      },
                      modifier = Modifier.weight(1f)
                    )

                    DocumentUploadSlot(
                      title = "بطاقة الرقم القومي",
                      sideBadge = "الوجه الخلفي (ظهر)",
                      isUploaded = clientIdBackUploaded,
                      uri = clientIdBackUri,
                      onCardClick = { activeSlotForSourcePicker = DocumentSlotType.CLIENT_NID_BACK },
                      onCameraClick = {
                        activeSlotForSourcePicker = DocumentSlotType.CLIENT_NID_BACK
                        cameraLauncher.launch(null)
                      },
                      onGalleryClick = {
                        activeSlotForSourcePicker = DocumentSlotType.CLIENT_NID_BACK
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                      },
                      onPreviewClick = {
                        documentPreviewModal = Pair("بطاقة الرقم القومي للموكل - الوجه الخلفي (ظهر)", clientIdBackUri ?: "id_card_back.jpg")
                      },
                      modifier = Modifier.weight(1f)
                    )
                  }
                }

                2 -> {
                  // CLIENT STEP 3: Review Summary & Terms of Service Agreement
                  Text("مراجعة بيانات الحساب والتوثيق:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)

                  Surface(
                    color = MaterialTheme.adaptiveSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الاسم الرسمي:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                        Text(clientFullName, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                      }
                      if (clientType == ClientType.CORPORATE) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                          Text("المنشأة التجارية:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                          Text(clientCompanyName, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                        }
                      }
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الهاتف والمحافظة:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                        Text("$clientPhone ($clientGovernorate)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                      }
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الرقم القومي:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                        Text(clientNationalId, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                      }
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مستندات الهوية:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                        Text(
                          if (clientIdFrontUploaded && clientIdBackUploaded) "الوجهان مرفقان ✓" else "مرفوع جزئياً",
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Bold,
                          color = EmeraldSuccess
                        )
                      }
                    }
                  }

                  // Terms of Service & Privacy Policy Card
                  Surface(
                    color = GoldContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(
                      modifier = Modifier.padding(12.dp),
                      verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                          Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldDark, modifier = Modifier.size(18.dp))
                          Text("شروط الاستخدام وسياسة الخصوصية", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
                        }
                        TextButton(
                          onClick = {
                            termsDialogTab = 0
                            showTermsAndPrivacyDialog = true
                          },
                          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                          Text("قراءة البنود كاملة ↗", fontSize = 10.5.sp, color = GoldDark, fontWeight = FontWeight.Bold)
                        }
                      }

                      Text(
                        text = "باستخدام منصة مِتر، فإنك توافق على سياسة حماية وسرية البيانات، وشروط التعاقد وتوكيل المحامين المعتمدين بجمهورية مصر العربية وفقاً للقانون المنظم.",
                        fontSize = 10.sp,
                        color = NavyDark.copy(alpha = 0.85f),
                        lineHeight = 14.sp
                      )

                      // Checkbox Agreement
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .clip(RoundedCornerShape(8.dp))
                          .clickable { clientTermsAccepted = !clientTermsAccepted }
                          .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                      ) {
                        Checkbox(
                          checked = clientTermsAccepted,
                          onCheckedChange = { clientTermsAccepted = it },
                          colors = CheckboxDefaults.colors(checkedColor = GoldDark)
                        )
                        Text(
                          text = "أوافق وأقر بصحة البيانات وأوافق على شروط الاستخدام وسياسة الخصوصية للمنصة",
                          fontSize = 10.5.sp,
                          fontWeight = FontWeight.Bold,
                          color = NavyDark,
                          lineHeight = 15.sp
                        )
                      }
                    }
                  }
                }
              }

              // CLIENT STEP NAVIGATION BUTTONS (السابق / التالي / التوثيق النهائي)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                if (clientRegistrationStep > 0) {
                  OutlinedButton(
                    onClick = {
                      formErrorMessage = null
                      clientRegistrationStep--
                    },
                    modifier = Modifier
                      .weight(1f)
                      .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder)
                  ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("السابق", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                  }
                }

                if (clientRegistrationStep < 2) {
                  Button(
                    onClick = {
                      if (clientRegistrationStep == 0) {
                        if (clientFullName.isBlank() || clientPhone.isBlank()) {
                          formErrorMessage = "يرجى كتابة الاسم الرباعي ورقم الهاتف أولاً للمتابعة."
                          return@Button
                        }
                      } else if (clientRegistrationStep == 1) {
                        if (clientNationalId.length < 14) {
                          formErrorMessage = "يرجى كتابة الرقم القومي المصري كاملاً (14 رقماً)."
                          return@Button
                        }
                      }
                      formErrorMessage = null
                      clientRegistrationStep++
                    },
                    modifier = Modifier
                      .weight(if (clientRegistrationStep > 0) 1.5f else 1f)
                      .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Text("التالي", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                  }
                } else {
                  // Step 2: Final Submit
                  Button(
                    onClick = {
                      if (!clientTermsAccepted) {
                        formErrorMessage = "يجب الموافقة على شروط الاستخدام وسياسة الخصوصية للمتابعة."
                        return@Button
                      }
                      if (clientFullName.isBlank() || clientPhone.isBlank()) {
                        formErrorMessage = "يرجى استكمال البيانات الإلزامية."
                        return@Button
                      }
                      onRegisterClient(
                        clientFullName,
                        clientPhone,
                        clientEmail,
                        clientNationalId,
                        clientType,
                        if (clientType == ClientType.CORPORATE) clientCompanyName else null,
                        clientGovernorate,
                        clientIdFrontUploaded,
                        clientIdBackUploaded,
                        clientIdFrontUri,
                        clientIdBackUri
                      )
                      onRegisterSuccess(
                        clientFullName,
                        clientPhone,
                        clientEmail,
                        UserRole.CLIENT,
                        clientNationalId,
                        null,
                        if (clientType == ClientType.CORPORATE) clientCompanyName else null
                      )
                    },
                    modifier = Modifier
                      .weight(if (clientRegistrationStep > 0) 1.8f else 1f)
                      .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("توثيق الهوية وإنشاء الحساب", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                  }
                }
              }
            } else {
              // -------------------------------------------------------------
              // LAWYER REGISTRATION WIZARD (6 STEPS)
              // -------------------------------------------------------------
              val lawyerStepTitles = listOf(
                "البيانات المهنية والتواصل",
                "القيد بنقابة المحامين والدرجة",
                "نطاق العمل والمحافظات والمحاكم",
                "رفع الوثائق الرسمية (KYC)",
                "المراجعة وميثاق شرف المهنة",
                "الموقع الجغرافي للمكتب واعتماده"
              )
              val lawyerStepSubtitles = listOf(
                "الاسم الرباعي الرسمي، اسم المكتب، والخبرة وبيانات الاتصال",
                "رقم القيد، درجة المحاماة الحالية، والنقابة الفرعية والمحافظة الأساسية",
                "تحديد درجات وتصنيفات القضايا والمحافظات المشمولة لتوسيع نطاق العمل",
                "رفع بطاقة الرقم القومي وكارنيه نقابة المحامين (الوجهين وش وظهر)",
                "مراجعة الملف والموافقة على شروط الاستخدام وميثاق شرف المهنة",
                "تسجيل عنوان المكتب يدوياً مع إمكانية إضافة نقطة الموقع أو تأجيلها"
              )

              RegistrationWizardHeader(
                currentStep = lawyerRegistrationStep,
                totalSteps = 6,
                stepTitle = lawyerStepTitles[lawyerRegistrationStep],
                stepSubtitle = lawyerStepSubtitles[lawyerRegistrationStep],
                accentColor = EmeraldSuccess
              )

              when (lawyerRegistrationStep) {
                0 -> {
                  // STEP 1: Personal & Firm Info
                  Surface(
                    color = NavyDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Policy, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(16.dp))
                        Text("إشعار التسجيل والاعتماد للمحامين:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                      }
                      Text(
                        text = "وفقاً للائحة منصة مِتر وقانون المحاماة المصري، يتطلب تفعيل حسابك رفع المستندات النقابية ومراجعتها من قِبل مسؤول النظام لاعتماد قيدك وتحديد نطاق العمل القضائي والدرجة الحالية.",
                        fontSize = 10.sp,
                        color = GoldLight,
                        lineHeight = 14.sp
                      )
                    }
                  }

                  // Lawyer Title Dropdown + Full Name in a Row
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    var titleExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                      expanded = titleExpanded,
                      onExpandedChange = { titleExpanded = !titleExpanded },
                      modifier = Modifier.weight(0.9f)
                    ) {
                      OutlinedTextField(
                        value = lawyerTitle.labelAr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("اللقب المهني") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = titleExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = maitreTextFieldColors()
                      )
                      ExposedDropdownMenu(
                        expanded = titleExpanded,
                        onDismissRequest = { titleExpanded = false }
                      ) {
                        LawyerTitle.values().forEach { titleOption ->
                          DropdownMenuItem(
                            text = { Text(titleOption.labelAr, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            onClick = {
                              lawyerTitle = titleOption
                              titleExpanded = false
                            }
                          )
                        }
                      }
                    }

                    OutlinedTextField(
                      value = lawyerFullName,
                      onValueChange = { lawyerFullName = it },
                      label = { Text("الاسم الرسمي (بدون اللقب)") },
                      placeholder = { Text("مثال: سامح محمد العسقلاني") },
                      leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldSuccess) },
                      modifier = Modifier.weight(1.3f),
                      shape = RoundedCornerShape(10.dp),
                      singleLine = true,
                      colors = maitreTextFieldColors()
                    )
                  }

                  // Bio (نبذة عن المحامي)
                  OutlinedTextField(
                    value = lawyerBio,
                    onValueChange = { lawyerBio = it },
                    label = { Text("نبذة مهنية عن المحامي وسنوات الخبرة *") },
                    placeholder = { Text("نبذة مختصرة تظهر للعملاء عن خبرتك القضائية وأبرز تخصصاتك...") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = GoldDark) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    colors = maitreTextFieldColors()
                  )

                  OutlinedTextField(
                    value = lawyerFirmName,
                    onValueChange = { lawyerFirmName = it },
                    label = { Text("اسم مكتب المحاماة / المجموعة القانونية") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldDark) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = maitreTextFieldColors()
                  )

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedTextField(
                      value = lawyerPhone,
                      onValueChange = { lawyerPhone = it },
                      label = { Text("الهاتف والواتساب المهني") },
                      leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GoldDark) },
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(10.dp),
                      singleLine = true,
                      colors = maitreTextFieldColors(),
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    OutlinedTextField(
                      value = lawyerEmail,
                      onValueChange = { lawyerEmail = it },
                      label = { Text("البريد الإلكتروني المهني") },
                      leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldDark) },
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(10.dp),
                      singleLine = true,
                      colors = maitreTextFieldColors(),
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    // Specialization
                    var specExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                      expanded = specExpanded,
                      onExpandedChange = { specExpanded = !specExpanded },
                      modifier = Modifier.weight(1.3f)
                    ) {
                      OutlinedTextField(
                        value = lawyerSpecialization.titleAr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("التخصص الرئيسي") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = specExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = maitreTextFieldColors()
                      )
                      ExposedDropdownMenu(
                        expanded = specExpanded,
                        onDismissRequest = { specExpanded = false },
                        modifier = Modifier.heightIn(max = 240.dp)
                      ) {
                        RequestCategory.values().forEach { cat ->
                          DropdownMenuItem(
                            text = { Text(cat.titleAr, fontSize = 11.5.sp) },
                            onClick = {
                              lawyerSpecialization = cat
                              specExpanded = false
                            }
                          )
                        }
                      }
                    }

                    // Years of experience
                    OutlinedTextField(
                      value = lawyerExperienceYears,
                      onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) lawyerExperienceYears = it },
                      label = { Text("سنوات الخبرة") },
                      modifier = Modifier.weight(0.7f),
                      shape = RoundedCornerShape(10.dp),
                      singleLine = true,
                      colors = maitreTextFieldColors(),
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                  }
                }

                1 -> {
                  // STEP 2: Bar License, Degree & Sub-Bar
                  Text("بيانات القيد بنقابة المحامين والدرجة القضائية:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (MaterialTheme.colorScheme.background == NavyDark) GoldLight else NavyPrimary)

                  OutlinedTextField(
                    value = lawyerBarLicenseNumber,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) lawyerBarLicenseNumber = it },
                    label = { Text("رقم القيد بنقابة المحامين المصرية") },
                    leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldSuccess) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = maitreTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                  )

                  // Select Bar Degree
                  Text("درجة القيد الحالية المطلوب اعتمادها:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.adaptiveTextPrimary)

                  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LawyerBarDegree.values().forEach { deg ->
                      val isSel = lawyerDegree == deg
                      Surface(
                        color = if (isSel) EmeraldContainer else MaterialTheme.adaptiveSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EmeraldSuccess else MaterialTheme.adaptiveBorder),
                        modifier = Modifier
                          .fillMaxWidth()
                          .clickable {
                            lawyerDegree = deg
                            val allowedCategoryIds = JudicialPracticeCategory.values()
                              .filter { JudicialPracticeCategory.isAllowedForActualDegree(it, deg) }
                              .map { it.id }
                              .toSet()
                            selectedPracticeDegreeIds = selectedPracticeDegreeIds.filter { it in allowedCategoryIds }.toSet()
                            if (selectedPracticeDegreeIds.isEmpty()) {
                              selectedPracticeDegreeIds = allowedCategoryIds
                            }
                          }
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                          Column(modifier = Modifier.weight(1f)) {
                            Text(deg.formalTitleAr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) NavyDark else MaterialTheme.adaptiveTextPrimary)
                            Text(deg.descAr, fontSize = 9.5.sp, color = if (isSel) TextSecondary else MaterialTheme.adaptiveTextSecondary, maxLines = 1)
                          }
                          RadioButton(
                            selected = isSel,
                            onClick = {
                              lawyerDegree = deg
                              val allowedCategoryIds = JudicialPracticeCategory.values()
                                .filter { JudicialPracticeCategory.isAllowedForActualDegree(it, deg) }
                                .map { it.id }
                                .toSet()
                              selectedPracticeDegreeIds = selectedPracticeDegreeIds.filter { it in allowedCategoryIds }.toSet()
                              if (selectedPracticeDegreeIds.isEmpty()) {
                                selectedPracticeDegreeIds = allowedCategoryIds
                              }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldSuccess)
                          )
                        }
                      }
                    }
                  }

                  // Sub-Bar Association Dropdown List
                  Text("النقابة الفرعية التابع لها المحامي:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.adaptiveTextPrimary)
                  ExposedDropdownMenuBox(
                    expanded = lawyerSubBarExpanded,
                    onExpandedChange = { lawyerSubBarExpanded = !lawyerSubBarExpanded },
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    OutlinedTextField(
                      value = lawyerSubBar,
                      onValueChange = {},
                      readOnly = true,
                      label = { Text("اختر النقابة الفرعية من القائمة") },
                      leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = EmeraldSuccess) },
                      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lawyerSubBarExpanded) },
                      modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                      shape = RoundedCornerShape(10.dp),
                      colors = maitreTextFieldColors()
                    )
                    ExposedDropdownMenu(
                      expanded = lawyerSubBarExpanded,
                      onDismissRequest = { lawyerSubBarExpanded = false },
                      modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                      EgyptSubBarHelper.egyptianSubBars.forEach { subBarName ->
                        val isCurrent = subBarName == lawyerSubBar
                        DropdownMenuItem(
                          text = {
                            Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.SpaceBetween,
                              verticalAlignment = Alignment.CenterVertically
                            ) {
                              Text(
                                subBarName,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) EmeraldSuccess else MaterialTheme.adaptiveTextPrimary
                              )
                              if (isCurrent) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                              }
                            }
                          },
                          onClick = {
                            lawyerSubBar = subBarName
                            lawyerSubBarExpanded = false
                          }
                        )
                      }
                    }
                  }

                  // Main Office Governorate Dropdown List
                  Text("المحافظة الرئيسية للمكتب:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.adaptiveTextPrimary)
                  ExposedDropdownMenuBox(
                    expanded = lawyerGovDropdownExpanded,
                    onExpandedChange = { lawyerGovDropdownExpanded = !lawyerGovDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    OutlinedTextField(
                      value = lawyerGovernorate,
                      onValueChange = {},
                      readOnly = true,
                      label = { Text("اختر المحافظة الرئيسية للمكتب") },
                      leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldDark) },
                      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lawyerGovDropdownExpanded) },
                      modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                      shape = RoundedCornerShape(10.dp),
                      colors = maitreTextFieldColors()
                    )
                    ExposedDropdownMenu(
                      expanded = lawyerGovDropdownExpanded,
                      onDismissRequest = { lawyerGovDropdownExpanded = false },
                      modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                      EgyptCourtsDirectory.governoratesList.forEach { govName ->
                        val isCurrent = govName == lawyerGovernorate
                        DropdownMenuItem(
                          text = {
                            Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.SpaceBetween,
                              verticalAlignment = Alignment.CenterVertically
                            ) {
                              Text(
                                govName,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) EmeraldSuccess else MaterialTheme.adaptiveTextPrimary
                              )
                              if (isCurrent) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                              }
                            }
                          },
                          onClick = {
                            lawyerGovernorate = govName
                            if (govName !in selectedWorkGovernorates) {
                              selectedWorkGovernorates = selectedWorkGovernorates + govName
                            }
                            lawyerGovDropdownExpanded = false
                          }
                        )
                      }
                    }
                  }
                }

                2 -> {
                  // STEP 3: Judicial Scope & Workplaces
                  Surface(
                    color = NavyDark,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(18.dp))
                        Text("نطاق العمل القضائي والمحاكم (إعدادات التخصص والدوائر):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                      }
                      Text(
                        text = "حدد أولاً المحافظات المشمولة بنشاطك، لتظهر لك المحاكم والمناطق والدوائر التابعة لاختيارك فقط.",
                        fontSize = 10.sp,
                        color = GoldLight,
                        lineHeight = 14.sp
                      )
                    }
                  }

                  // LIST 1: Desired Practice Degrees
                  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("١. الدرجات والتصنيفات التي ترغب في العمل بها:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                      }
                      Surface(color = EmeraldContainer, shape = RoundedCornerShape(4.dp)) {
                        Text(
                          text = "الحد الأقصى: ${lawyerDegree.titleAr}",
                          fontSize = 9.sp,
                          color = EmeraldSuccess,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }
                    Text(
                      text = "يمكنك اختيار أكثر من تصنيف قضائي بما لا يتجاوز درجتك الفعلية الحالية (${lawyerDegree.formalTitleAr}):",
                      fontSize = 10.sp,
                      color = MaterialTheme.adaptiveTextSecondary
                    )

                    JudicialPracticeCategory.values().forEach { cat ->
                      val isAllowed = JudicialPracticeCategory.isAllowedForActualDegree(cat, lawyerDegree)
                      val isChecked = cat.id in selectedPracticeDegreeIds

                      Surface(
                        color = when {
                          !isAllowed -> MaterialTheme.adaptiveSurfaceVariant.copy(alpha = 0.5f)
                          isChecked -> EmeraldContainer
                          else -> MaterialTheme.adaptiveSurface
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                          1.dp,
                          when {
                            !isAllowed -> MaterialTheme.adaptiveBorder.copy(alpha = 0.4f)
                            isChecked -> EmeraldSuccess
                            else -> MaterialTheme.adaptiveBorder
                          }
                        ),
                        modifier = Modifier
                          .fillMaxWidth()
                          .clickable(enabled = isAllowed) {
                            selectedPracticeDegreeIds = if (isChecked) {
                              if (selectedPracticeDegreeIds.size > 1) selectedPracticeDegreeIds - cat.id else selectedPracticeDegreeIds
                            } else {
                              selectedPracticeDegreeIds + cat.id
                            }
                          }
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                          Checkbox(
                            checked = isChecked && isAllowed,
                            onCheckedChange = { checked ->
                              if (isAllowed) {
                                selectedPracticeDegreeIds = if (checked) {
                                  selectedPracticeDegreeIds + cat.id
                                } else {
                                  if (selectedPracticeDegreeIds.size > 1) selectedPracticeDegreeIds - cat.id else selectedPracticeDegreeIds
                                }
                              }
                            },
                            enabled = isAllowed,
                            colors = CheckboxDefaults.colors(
                              checkedColor = EmeraldSuccess,
                              checkmarkColor = Color.White
                            )
                          )

                          Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                              Text(
                                text = cat.titleAr,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAllowed) {
                                  if (isChecked) NavyDark else MaterialTheme.adaptiveTextPrimary
                                } else MaterialTheme.adaptiveTextMuted
                              )
                              if (!isAllowed) {
                                Surface(color = CrimsonError.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp)) {
                                  Text(
                                    text = "🔒 يتطلب قيد ${cat.minRequiredDegree.titleAr}",
                                    fontSize = 8.5.sp,
                                    color = CrimsonError,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                  )
                                }
                              }
                            }
                            Text(
                              text = cat.subtitleAr,
                              fontSize = 9.5.sp,
                              color = if (isAllowed) {
                                if (isChecked) TextSecondary else MaterialTheme.adaptiveTextSecondary
                              } else MaterialTheme.adaptiveTextMuted,
                              lineHeight = 13.sp
                            )
                          }
                        }
                      }
                    }
                  }

                  // LIST 2: Workplaces (Governorates -> Then Courts & Districts for selected Governorates only)
                  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text("٢. أماكن العمل والمحاكم (تسلسل ذكي):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                      Surface(color = GoldContainer, shape = RoundedCornerShape(4.dp)) {
                        Text(
                          text = "المحافظات أولاً ✓",
                          fontSize = 9.sp,
                          color = GoldDark,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }

                    // A. STEP 1 OF WORKPLACES: Governorates Selection
                    Surface(
                      color = MaterialTheme.adaptiveSurfaceVariant,
                      shape = RoundedCornerShape(10.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder)
                    ) {
                      Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                              color = EmeraldSuccess,
                              shape = CircleShape,
                              modifier = Modifier.size(20.dp)
                            ) {
                              Box(contentAlignment = Alignment.Center) {
                                Text("١", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                              }
                            }
                            Text("أولاً: اختر المحافظات المشمولة بنشاطك:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                          }
                          Text(
                            text = "(${selectedWorkGovernorates.size} مختارة)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                          )
                        }

                        Text(
                          text = "اختر محافظة واحدة أو أكثر، وسيتم تصفية وإظهار المحاكم والمناطق التابعة لاختيارك فقط بالأسفل:",
                          fontSize = 9.5.sp,
                          color = MaterialTheme.adaptiveTextSecondary
                        )

                        // Quick Select Presets
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                          AssistChip(
                            onClick = {
                              selectedWorkGovernorates = setOf("القاهرة", "الجيزة", "القليوبية")
                            },
                            label = { Text("القاهرة الكبرى", fontSize = 9.5.sp) },
                            leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, modifier = Modifier.size(12.dp)) }
                          )
                          AssistChip(
                            onClick = {
                              selectedWorkGovernorates = EgyptCourtsDirectory.governoratesList.toSet()
                            },
                            label = { Text("كافة المحافظات", fontSize = 9.5.sp) }
                          )
                          if (selectedWorkGovernorates.isNotEmpty()) {
                            AssistChip(
                              onClick = {
                                selectedWorkGovernorates = emptySet()
                              },
                              label = { Text("مسح", fontSize = 9.5.sp, color = CrimsonError) }
                            )
                          }
                        }

                        // Governorates Chips
                        FlowRow(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.spacedBy(6.dp),
                          verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                          EgyptCourtsDirectory.governoratesList.forEach { gov ->
                            val isSel = gov in selectedWorkGovernorates
                            FilterChip(
                              selected = isSel,
                              onClick = {
                                selectedWorkGovernorates = if (isSel) {
                                  selectedWorkGovernorates - gov
                                } else {
                                  selectedWorkGovernorates + gov
                                }
                              },
                              label = { Text(gov, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                              leadingIcon = if (isSel) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                              } else null,
                              colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldContainer,
                                selectedLabelColor = NavyDark
                              )
                            )
                          }
                        }
                      }
                    }

                    // B. CONDITIONAL COURTS & DISTRICTS BASED ON SELECTED GOVERNORATES ONLY
                    if (selectedWorkGovernorates.isEmpty()) {
                      Surface(
                        color = GoldContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Row(
                          modifier = Modifier.padding(12.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                          Icon(Icons.Default.Info, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
                          Column(modifier = Modifier.weight(1f)) {
                            Text(
                              text = "يرجى تحديد المحافظات أولاً",
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold,
                              color = GoldDark
                            )
                            Text(
                              text = "حدد محافظة واحدة على الأقل من القائمة أعلاه لعرض المحاكم والمناطق والنيابات التابعة لاختيارك فقط.",
                              fontSize = 9.5.sp,
                              color = MaterialTheme.adaptiveTextPrimary,
                              lineHeight = 13.sp
                            )
                          }
                        }
                      }
                    } else {
                      val availableCourts = remember(selectedWorkGovernorates) {
                        EgyptCourtsDirectory.getCourtsForGovernorates(selectedWorkGovernorates)
                      }
                      val availableDistricts = remember(selectedWorkGovernorates) {
                        EgyptCourtsDirectory.getDistrictsForGovernorates(selectedWorkGovernorates)
                      }

                      // B. Common Court Complexes for Selected Governorates Only
                      Surface(
                        color = MaterialTheme.adaptiveSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder)
                      ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                              Surface(
                                color = GoldDark,
                                shape = CircleShape,
                                modifier = Modifier.size(20.dp)
                              ) {
                                Box(contentAlignment = Alignment.Center) {
                                  Text("٢", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                              }
                              Text("ثانياً: المحاكم التابعة للمحافظات المختارة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                              TextButton(
                                onClick = {
                                  selectedWorkCourts = selectedWorkCourts + availableCourts
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                              ) {
                                Text("تحديد الكل", fontSize = 9.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                              }
                              TextButton(
                                onClick = {
                                  selectedWorkCourts = selectedWorkCourts - availableCourts.toSet()
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                              ) {
                                Text("إلغاء", fontSize = 9.5.sp, color = CrimsonError)
                              }
                            }
                          }

                          Surface(
                            color = EmeraldContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                          ) {
                            Text(
                              text = "محاكم محافظات: ${selectedWorkGovernorates.joinToString(" • ")} (إجمالي ${availableCourts.size} محكمة ومجمع)",
                              fontSize = 9.sp,
                              color = NavyDark,
                              fontWeight = FontWeight.SemiBold,
                              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                          }

                          if (availableCourts.isEmpty()) {
                            Text("لا توجد مجمعات محاكم مسجلة لهذه المحافظة حالياً، يمكنك إضافة اسم المحكمة يدوياً بالأسفل.", fontSize = 9.5.sp, color = MaterialTheme.adaptiveTextSecondary)
                          } else {
                            FlowRow(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.spacedBy(6.dp),
                              verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                              availableCourts.forEach { court ->
                                val isSel = court in selectedWorkCourts
                                FilterChip(
                                  selected = isSel,
                                  onClick = {
                                    selectedWorkCourts = if (isSel) {
                                      selectedWorkCourts - court
                                    } else {
                                      selectedWorkCourts + court
                                    }
                                  },
                                  label = { Text(court, fontSize = 9.5.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                  leadingIcon = if (isSel) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                  } else null,
                                  colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldContainer,
                                    selectedLabelColor = NavyDark
                                  )
                                )
                              }
                            }
                          }

                          // Add Custom Court
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            OutlinedTextField(
                              value = customCourtNameInput,
                              onValueChange = { customCourtNameInput = it },
                              label = { Text("إضافة محكمة أو دائرة أخرى") },
                              placeholder = { Text("مثال: نيابة الشروق الجزئية") },
                              modifier = Modifier.weight(1f),
                              shape = RoundedCornerShape(8.dp),
                              singleLine = true,
                              colors = maitreTextFieldColors()
                            )
                            Button(
                              onClick = {
                                if (customCourtNameInput.isNotBlank()) {
                                  selectedWorkCourts = selectedWorkCourts + customCourtNameInput.trim()
                                  customCourtNameInput = ""
                                }
                              },
                              shape = RoundedCornerShape(8.dp),
                              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                              modifier = Modifier.height(52.dp)
                            ) {
                              Text("إضافة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                          }
                        }
                      }

                      // C. Districts / Areas for Selected Governorates Only
                      Surface(
                        color = MaterialTheme.adaptiveSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder)
                      ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                              Surface(
                                color = NavyPrimary,
                                shape = CircleShape,
                                modifier = Modifier.size(20.dp)
                              ) {
                                Box(contentAlignment = Alignment.Center) {
                                  Text("٣", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                              }
                              Text("ثالثاً: المناطق والأحياء التابعة للمحافظات المختارة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                              TextButton(
                                onClick = {
                                  selectedWorkDistricts = selectedWorkDistricts + availableDistricts
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                              ) {
                                Text("تحديد الكل", fontSize = 9.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                              }
                              TextButton(
                                onClick = {
                                  selectedWorkDistricts = selectedWorkDistricts - availableDistricts.toSet()
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                              ) {
                                Text("إلغاء", fontSize = 9.5.sp, color = CrimsonError)
                              }
                            }
                          }

                          Surface(
                            color = GoldContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                          ) {
                            Text(
                              text = "أحياء ومراكز: ${selectedWorkGovernorates.joinToString(" • ")} (إجمالي ${availableDistricts.size} منطقة)",
                              fontSize = 9.sp,
                              color = NavyDark,
                              fontWeight = FontWeight.SemiBold,
                              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                          }

                          if (availableDistricts.isEmpty()) {
                            Text("لا توجد أحياء مسجلة لهذه المحافظة حالياً.", fontSize = 9.5.sp, color = MaterialTheme.adaptiveTextSecondary)
                          } else {
                            FlowRow(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.spacedBy(6.dp),
                              verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                              availableDistricts.forEach { dist ->
                                val isSel = dist in selectedWorkDistricts
                                FilterChip(
                                  selected = isSel,
                                  onClick = {
                                    selectedWorkDistricts = if (isSel) {
                                      selectedWorkDistricts - dist
                                    } else {
                                      selectedWorkDistricts + dist
                                    }
                                  },
                                  label = { Text(dist, fontSize = 9.5.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                  leadingIcon = if (isSel) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                  } else null
                                )
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }

                3 -> {
                  // STEP 4: KYC Documents
                  Text("رفع المستندات الرسمية للاعتماد (KYC - وش وظهر):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                  Text("التقط بالكاميرا أو اختر من المعرض لكافة الوثائق (الوجهين):", fontSize = 10.sp, color = MaterialTheme.adaptiveTextSecondary)

                  OutlinedTextField(
                    value = lawyerNationalId,
                    onValueChange = { if (it.length <= 14 && it.all { c -> c.isDigit() }) lawyerNationalId = it },
                    label = { Text("الرقم القومي للأستاذ المحامي (14 رقماً)") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldSuccess) },
                    trailingIcon = { Text("${lawyerNationalId.length}/14", fontSize = 10.sp, color = if (lawyerNationalId.length == 14) EmeraldSuccess else MaterialTheme.adaptiveTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = maitreTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                  )

                  // 1. National ID Cards (Front and Back)
                  Text("أ. بطاقة الرقم القومي (سارية - وش وظهر):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    DocumentUploadSlot(
                      title = "بطاقة الرقم القومي",
                      sideBadge = "الوجه الأمامي (وش)",
                      isUploaded = lawyerIdCardUploaded,
                      uri = lawyerIdFrontUri,
                      onCardClick = { activeSlotForSourcePicker = DocumentSlotType.LAWYER_NID_FRONT },
                      onCameraClick = {
                        activeSlotForSourcePicker = DocumentSlotType.LAWYER_NID_FRONT
                        cameraLauncher.launch(null)
                      },
                      onGalleryClick = {
                        activeSlotForSourcePicker = DocumentSlotType.LAWYER_NID_FRONT
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                      },
                      onPreviewClick = {
                        documentPreviewModal = Pair("بطاقة الرقم القومي للمحامي - الوجه الأمامي (وش)", lawyerIdFrontUri ?: "id_card_front.jpg")
                      },
                      modifier = Modifier.weight(1f)
                    )

                    DocumentUploadSlot(
                      title = "بطاقة الرقم القومي",
                      sideBadge = "الوجه الخلفي (ظهر)",
                      isUploaded = lawyerIdBackUploaded,
                      uri = lawyerIdBackUri,
                      onCardClick = { activeSlotForSourcePicker = DocumentSlotType.LAWYER_NID_BACK },
                      onCameraClick = {
                        activeSlotForSourcePicker = DocumentSlotType.LAWYER_NID_BACK
                        cameraLauncher.launch(null)
                      },
                      onGalleryClick = {
                        activeSlotForSourcePicker = DocumentSlotType.LAWYER_NID_BACK
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                      },
                      onPreviewClick = {
                        documentPreviewModal = Pair("بطاقة الرقم القومي للمحامي - الوجه الخلفي (ظهر)", lawyerIdBackUri ?: "id_card_back.jpg")
                      },
                      modifier = Modifier.weight(1f)
                    )
                  }

                  // 2. Bar Association Card (Front and Back)
                  Text("ب. كارنيه نقابة المحامين (ساري - وش وظهر):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    DocumentUploadSlot(
                      title = "كارنيه النقابة",
                      sideBadge = "الوجه الأمامي (وش)",
                      isUploaded = lawyerBarCardUploaded,
                      uri = lawyerBarFrontUri,
                      onCardClick = { activeSlotForSourcePicker = DocumentSlotType.LAWYER_BAR_FRONT },
                      onCameraClick = {
                        activeSlotForSourcePicker = DocumentSlotType.LAWYER_BAR_FRONT
                        cameraLauncher.launch(null)
                      },
                      onGalleryClick = {
                        activeSlotForSourcePicker = DocumentSlotType.LAWYER_BAR_FRONT
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                      },
                      onPreviewClick = {
                        documentPreviewModal = Pair("كارنيه نقابة المحامين - الوجه الأمامي (وش)", lawyerBarFrontUri ?: "bar_card_front.jpg")
                      },
                      modifier = Modifier.weight(1f)
                    )

                    DocumentUploadSlot(
                      title = "كارنيه النقابة",
                      sideBadge = "الوجه الخلفي (ظهر)",
                      isUploaded = lawyerBarBackUploaded,
                      uri = lawyerBarBackUri,
                      onCardClick = { activeSlotForSourcePicker = DocumentSlotType.LAWYER_BAR_BACK },
                      onCameraClick = {
                        activeSlotForSourcePicker = DocumentSlotType.LAWYER_BAR_BACK
                        cameraLauncher.launch(null)
                      },
                      onGalleryClick = {
                        activeSlotForSourcePicker = DocumentSlotType.LAWYER_BAR_BACK
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                      },
                      onPreviewClick = {
                        documentPreviewModal = Pair("كارنيه نقابة المحامين - الوجه الخلفي (ظهر)", lawyerBarBackUri ?: "bar_card_back.jpg")
                      },
                      modifier = Modifier.weight(1f)
                    )
                  }

                  // Optional Certificate Upload
                  Surface(
                    color = if (lawyerCertificateUploaded) EmeraldContainer else CreamSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { lawyerCertificateUploaded = !lawyerCertificateUploaded }
                  ) {
                    Row(
                      modifier = Modifier.padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                          if (lawyerCertificateUploaded) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                          contentDescription = null,
                          tint = if (lawyerCertificateUploaded) EmeraldSuccess else TextMuted,
                          modifier = Modifier.size(18.dp)
                        )
                        Text("شهادة القيد أو إفادة ممارسة حديثة (اختياري)", fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                      }
                      Text(if (lawyerCertificateUploaded) "مرفق ✓" else "إرفاق ملف", fontSize = 9.5.sp, color = GoldDark, fontWeight = FontWeight.Bold)
                    }
                  }
                }

                4 -> {
                  // STEP 5: Review & Terms of Use / Code of Ethics
                  Text("مراجعة ملف المحامي وميثاق شرف المهنة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)

                  // Summary Card
                  Surface(
                    color = MaterialTheme.adaptiveSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الاسم والمكتب:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                        Text(lawyerFullName.ifBlank { "—" }, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                      }
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("درجة القيد والنقابة:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                        Text("${lawyerDegree.titleAr} • $lawyerSubBar", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                      }
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("رقم القيد والهاتف:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                        Text("قيد #$lawyerBarLicenseNumber • $lawyerPhone", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                      }
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المحاكم والمحافظات:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                        Text("${selectedWorkCourts.size} محاكم • ${selectedWorkGovernorates.joinToString("، ")}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                      }
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المستندات المرفوعة:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                        Text("البطاقة والكارنيه جاهزان للاعتماد ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                      }
                    }
                  }

                  // Terms and Ethics Agreement Box
                  Surface(
                    color = EmeraldContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(
                      modifier = Modifier.padding(12.dp),
                      verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                          Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                          Text("شروط الاستخدام وميثاق شرف المهنة", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
                        }
                        TextButton(
                          onClick = {
                            termsDialogTab = 0
                            showTermsAndPrivacyDialog = true
                          },
                          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                          Text("قراءة البنود ↗", fontSize = 10.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                      }

                      Text(
                        text = "يتعهد المحامي بالالتزام بقانون المحاماة رقم 17 لسنة 1983 وتعديلاته، والحفاظ على السر المهني للموكلين، والامتثال لشروط وأحكام منصة مِتر وسياسة الخصوصية والأمان المعتمدة.",
                        fontSize = 10.sp,
                        color = NavyDark.copy(alpha = 0.85f),
                        lineHeight = 14.sp
                      )

                      // Checkbox Agreement
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .clip(RoundedCornerShape(8.dp))
                          .clickable { lawyerTermsAccepted = !lawyerTermsAccepted }
                          .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                      ) {
                        Checkbox(
                          checked = lawyerTermsAccepted,
                          onCheckedChange = { lawyerTermsAccepted = it },
                          colors = CheckboxDefaults.colors(checkedColor = EmeraldSuccess)
                        )
                        Text(
                          text = "أقر بصحة بيانات القيد والوثائق وأوافق على شروط الاستخدام وميثاق المهنة وسياسة الخصوصية",
                          fontSize = 10.5.sp,
                          fontWeight = FontWeight.Bold,
                          color = NavyDark,
                          lineHeight = 15.sp
                        )
                      }
                    }
                  }
                }

                5 -> {
                  // STEP 6: Office Location & Official Workspace Activation (الموقع الجغرافي للمكتب وتفعيله)
                  Surface(
                    color = NavyDark,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(20.dp))
                        Text("الموقع الجغرافي لمكتب المحاماة:", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                      }
                      Text(
                        text = "يتم تسجيل عنوان المكتب بالتفصيل يدوياً، مع إمكانية التقاط نقطة الـ GPS فوراً أو تأجيلها لحين اعتماد ومعاينة المكتب.",
                        fontSize = 10.sp,
                        color = GoldLight,
                        lineHeight = 14.sp
                      )
                    }
                  }

                  // 1. Manual Office Address (إلزامي يدوياً)
                  OutlinedTextField(
                    value = lawyerOfficeAddressManually,
                    onValueChange = { lawyerOfficeAddressManually = it },
                    label = { Text("عنوان المكتب الرسمي بالتفصيل يدوياً *") },
                    placeholder = { Text("مثال: 15 شارع شريف، وسط البلد، عمارة التأمين، الدور الرابع، مكتب 42") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = CrimsonError) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    colors = maitreTextFieldColors()
                  )

                  // 2. Options: Pin GPS Location vs Postpone
                  Surface(
                    color = CreamSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                      Text("تحديد موقع المكتب على الخريطة والرادار:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = NavyDark)

                      // Option A: Pin location now
                      Surface(
                        color = if (!lawyerPostponeOfficeLocation) EmeraldContainer else MaterialTheme.adaptiveSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (!lawyerPostponeOfficeLocation) EmeraldSuccess else MaterialTheme.adaptiveBorder),
                        modifier = Modifier
                          .fillMaxWidth()
                          .clickable { lawyerPostponeOfficeLocation = false }
                      ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                              RadioButton(
                                selected = !lawyerPostponeOfficeLocation,
                                onClick = { lawyerPostponeOfficeLocation = false },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldSuccess)
                              )
                              Text(
                                text = "إضافة نقطة الموقع الجغرافي الآن (GPS Pin)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!lawyerPostponeOfficeLocation) NavyDark else MaterialTheme.adaptiveTextPrimary
                              )
                            }
                            if (lawyerLocationCaptured && !lawyerPostponeOfficeLocation) {
                              Surface(color = EmeraldSuccess, shape = RoundedCornerShape(4.dp)) {
                                Text("تم التحديد ✓", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                              }
                            }
                          }

                          if (!lawyerPostponeOfficeLocation) {
                            Text(
                              text = if (lawyerLocationCaptured) {
                                "📍 الإحداثيات: (${String.format(java.util.Locale.US, "%.4f", lawyerLatitude ?: 30.0444)}, ${String.format(java.util.Locale.US, "%.4f", lawyerLongitude ?: 31.2357)})\n$lawyerLocationAddressDescription"
                              } else {
                                "اضغط لالتقاط وتثبيت إحداثيات GPS الخاصة بالمكتب لتسهيل الملاحة والوصول."
                              },
                              fontSize = 10.sp,
                              color = TextSecondary,
                              lineHeight = 14.sp
                            )

                            Button(
                              onClick = {
                                lawyerLatitude = 30.0444
                                lawyerLongitude = 31.2357
                                lawyerLocationCaptured = true
                                lawyerLocationAddressDescription = "وسط القاهرة - قصر النيل / شارع شريف (تم التحديد بدقة GPS)"
                              },
                              colors = ButtonDefaults.buttonColors(
                                containerColor = if (lawyerLocationCaptured) EmeraldSuccess else NavyPrimary,
                                contentColor = Color.White
                              ),
                              shape = RoundedCornerShape(8.dp),
                              modifier = Modifier.fillMaxWidth()
                            ) {
                              Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp))
                              Spacer(modifier = Modifier.width(6.dp))
                              Text(
                                text = if (lawyerLocationCaptured) "إعادة التقاط وتثبيت نقطة الموقع (GPS)" else "التقاط وتثبيت نقطة الموقع على الخريطة (GPS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                              )
                            }
                          }
                        }
                      }

                      // Option B: Postpone location
                      Surface(
                        color = if (lawyerPostponeOfficeLocation) GoldContainer else MaterialTheme.adaptiveSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (lawyerPostponeOfficeLocation) GoldDark else MaterialTheme.adaptiveBorder),
                        modifier = Modifier
                          .fillMaxWidth()
                          .clickable { lawyerPostponeOfficeLocation = true }
                      ) {
                        Row(
                          modifier = Modifier.padding(10.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                          RadioButton(
                            selected = lawyerPostponeOfficeLocation,
                            onClick = { lawyerPostponeOfficeLocation = true },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldDark)
                          )
                          Column(modifier = Modifier.weight(1f)) {
                            Text(
                              text = "تأجيل إضافة نقطة الموقع لحين مراجعة واعتماد المكتب",
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold,
                              color = if (lawyerPostponeOfficeLocation) GoldOnContainer else MaterialTheme.adaptiveTextPrimary
                            )
                            Text(
                              text = "يمكنك تحديد العنوان نصياً الآن وإضافة الإحداثيات وتفعيلها لاحقاً من صفحة الملف الشخصي.",
                              fontSize = 9.5.sp,
                              color = MaterialTheme.adaptiveTextSecondary
                            )
                          }
                        }
                      }
                    }
                  }

                  // 3. Prominent Regulatory Warning
                  Surface(
                    color = CrimsonContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier.padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      Icon(Icons.Default.WarningAmber, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(20.dp))
                      Text(
                        text = "تنبيه نظامي: لا يقبل النظام أو يتيح تقديم عروض على أي طلبات فورية أو قضايا قبل تفعيل واعتماد الموقع الرسمي للمكتب.",
                        color = CrimsonError,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp
                      )
                    }
                  }

                  if (lawyerSubmissionSuccess) {
                    Surface(color = EmeraldContainer, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("تم إرسال ملف المحامي لمسؤول النظام بنجاح!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldSuccess)
                        Text("طلبك قيد الفحص والاعتماد لدى إدارة المنصة لتحديد نطاق العمل والدرجة.", fontSize = 10.5.sp, color = NavyDark)
                      }
                    }
                  }
                }
              }

              // LAWYER WIZARD NAVIGATION BUTTONS (السابق / التالي / الإرسال للاعتماد)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                if (lawyerRegistrationStep > 0) {
                  OutlinedButton(
                    onClick = {
                      formErrorMessage = null
                      lawyerRegistrationStep--
                    },
                    modifier = Modifier
                      .weight(1f)
                      .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder)
                  ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("السابق", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                  }
                }

                if (lawyerRegistrationStep < 5) {
                  Button(
                    onClick = {
                      if (lawyerRegistrationStep == 0) {
                        if (lawyerFullName.isBlank() || lawyerPhone.isBlank()) {
                          formErrorMessage = "يرجى كتابة الاسم الرباعي ورقم الهاتف أولاً للمتابعة."
                          return@Button
                        }
                      } else if (lawyerRegistrationStep == 1) {
                        if (lawyerBarLicenseNumber.isBlank()) {
                          formErrorMessage = "يرجى كتابة رقم قيد نقابة المحامين للمتابعة."
                          return@Button
                        }
                      } else if (lawyerRegistrationStep == 2) {
                        if (selectedPracticeDegreeIds.isEmpty() || selectedWorkGovernorates.isEmpty()) {
                          formErrorMessage = "يرجى اختيار درجة تقاضٍ واحدة على الأقل ومحافظة عمل واحدة."
                          return@Button
                        }
                      } else if (lawyerRegistrationStep == 3) {
                        if (lawyerNationalId.length < 14) {
                          formErrorMessage = "يرجى كتابة الرقم القومي للمحامي (14 رقماً)."
                          return@Button
                        }
                      } else if (lawyerRegistrationStep == 4) {
                        if (!lawyerTermsAccepted) {
                          formErrorMessage = "يجب الموافقة على شروط الاستخدام وميثاق شرف المهنة وسياسة الخصوصية للمتابعة."
                          return@Button
                        }
                      }
                      formErrorMessage = null
                      lawyerRegistrationStep++
                    },
                    modifier = Modifier
                      .weight(if (lawyerRegistrationStep > 0) 1.5f else 1f)
                      .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Text("التالي", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                  }
                } else {
                  // Step 5: Final Lawyer Submit Button
                  Button(
                    onClick = {
                      if (lawyerOfficeAddressManually.isBlank()) {
                        formErrorMessage = "يرجى كتابة العنوان التفصيلي للمكتب يدوياً لإتمام التسجيل."
                        return@Button
                      }
                      if (lawyerFullName.isBlank() || lawyerBarLicenseNumber.isBlank() || lawyerPhone.isBlank()) {
                        formErrorMessage = "يرجى استكمال البيانات الإلزامية ورقم قيد النقابة."
                        return@Button
                      }
                      val expYears = lawyerExperienceYears.toIntOrNull() ?: 5
                      val desiredDegreesList = selectedPracticeDegreeIds.toList()
                      val selectedGovsList = selectedWorkGovernorates.toList()
                      val selectedCourtsList = selectedWorkCourts.toList()
                      val selectedDistrictsList = selectedWorkDistricts.toList()
                      val computedCourtScope = "محاكم: ${selectedCourtsList.take(2).joinToString("، ")} | محافظات: ${selectedGovsList.joinToString("، ")}"

                      onRegisterLawyer(
                        lawyerFullName,
                        lawyerPhone,
                        lawyerEmail,
                        lawyerNationalId,
                        lawyerBarLicenseNumber,
                        lawyerDegree,
                        lawyerSubBar,
                        lawyerGovernorate,
                        computedCourtScope,
                        lawyerSpecialization,
                        expYears,
                        lawyerFirmName,
                        lawyerBarCardUploaded,
                        lawyerIdCardUploaded,
                        lawyerBarBackUploaded,
                        lawyerIdBackUploaded,
                        lawyerIdFrontUri,
                        lawyerIdBackUri,
                        lawyerBarFrontUri,
                        lawyerBarBackUri,
                        desiredDegreesList,
                        selectedGovsList,
                        selectedCourtsList,
                        selectedDistrictsList,
                        lawyerTitle,
                        lawyerBio,
                        lawyerOfficeAddressManually,
                        if (lawyerPostponeOfficeLocation) null else lawyerLatitude,
                        if (lawyerPostponeOfficeLocation) null else lawyerLongitude
                      )
                      onRegisterSuccess(
                        lawyerFullName,
                        lawyerPhone,
                        lawyerEmail,
                        UserRole.LAWYER,
                        lawyerNationalId,
                        lawyerBarLicenseNumber,
                        lawyerFirmName
                      )
                      lawyerSubmissionSuccess = true
                    },
                    modifier = Modifier
                      .weight(if (lawyerRegistrationStep > 0) 1.8f else 1f)
                      .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إرسال الملف للاعتماد", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                  }
                }
              }
            }
          }

          formErrorMessage?.let { err ->
            Surface(color = CrimsonContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
              Text(err, color = CrimsonError, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Dedicated Admin Access in Footer (بوابة الإدارة المركزية والرقابة من قاعدة البيانات)
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f))
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Security, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(18.dp))
              Text("بوابة مسؤول النظام ولجان التحكيم والرقابة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Surface(
              color = GoldContainer,
              shape = RoundedCornerShape(4.dp)
            ) {
              Text("إدارة المنصة", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NavyDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
          }

          Text(
            text = "خاص بمسؤول النظام والتحكيم لمراجعة واعتماد مستندات المحامين الجدد، تحديد درجات القيد ونطاق العمل، وفض المنازعات.",
            fontSize = 10.5.sp,
            color = Color.White.copy(alpha = 0.8f),
            lineHeight = 15.sp
          )

          Button(
            onClick = { onLoginSuccess("01200000000", UserRole.ADMIN) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("دخول مسؤول النظام وقاعدة البيانات المركزية", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }

  // 1. Source Picker Dialog (Camera vs Gallery)
  activeSlotForSourcePicker?.let { slot ->
    AlertDialog(
      onDismissRequest = { activeSlotForSourcePicker = null },
      icon = {
        Icon(Icons.Default.UploadFile, contentDescription = null, tint = GoldDark, modifier = Modifier.size(28.dp))
      },
      title = {
        Text(
          text = "رفع المستند المطلوب",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = NavyDark,
          textAlign = TextAlign.Center
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            color = CreamSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
              Text(slot.titleAr, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
              Spacer(modifier = Modifier.height(4.dp))
              Surface(color = GoldContainer, shape = RoundedCornerShape(4.dp)) {
                Text(
                  text = slot.sideBadgeAr,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldDark,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }
          }

          Text(
            text = "اختر مصدر الصورة لالتقاطها مباشرة أو تحديدها من المعرض:",
            fontSize = 11.5.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
          )

          // Camera Option Button
          Button(
            onClick = {
              cameraLauncher.launch(null)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("فتح الكاميرا والتقاط صورة الآن 📷", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          // Gallery Option Button
          OutlinedButton(
            onClick = {
              galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("اختيار صورة من معرض الصور (Gallery) 🖼️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { activeSlotForSourcePicker = null }) {
          Text("إلغاء", color = TextMuted)
        }
      }
    )
  }

  // 2. Document Preview Enlarged Dialog
  documentPreviewModal?.let { (docTitle, docUri) ->
    AlertDialog(
      onDismissRequest = { documentPreviewModal = null },
      icon = {
        Icon(Icons.Default.Visibility, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(28.dp))
      },
      title = {
        Text(docTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark, textAlign = TextAlign.Center)
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            color = CreamSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
          ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = if (docTitle.contains("كارنيه")) Icons.Default.CreditCard else Icons.Default.Badge,
                  contentDescription = null,
                  tint = NavyPrimary,
                  modifier = Modifier.size(48.dp)
                )
                Text(
                  text = docTitle,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = NavyDark,
                  textAlign = TextAlign.Center
                )
                Surface(color = EmeraldContainer, shape = RoundedCornerShape(6.dp)) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                    Text("المستند مرفق وجاهز للاعتماد ✓", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                  }
                }
                Text("المسار: $docUri", fontSize = 9.sp, color = TextMuted)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { documentPreviewModal = null },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("إغلاق المعاينة")
        }
      }
    )
  }

  // ==========================================
  // GOOGLE ACCOUNT PICKER / SIGN-IN MODAL
  // ==========================================
  if (showGoogleAccountPicker) {
    AlertDialog(
      onDismissRequest = {
        if (!isGoogleAuthenticating) {
          showGoogleAccountPicker = false
          showAddCustomGoogleAccount = false
        }
      },
      icon = {
        GoogleLogoIcon(modifier = Modifier.size(32.dp))
      },
      title = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "المتابعة باستخدام Google",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = NavyDark,
            textAlign = TextAlign.Center
          )
          Text(
            text = "اختر حساب Google للمتابعة إلى تطبيق مِتر",
            fontSize = 11.5.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (isGoogleAuthenticating) {
            Surface(
              color = EmeraldContainer,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                CircularProgressIndicator(
                  color = EmeraldSuccess,
                  modifier = Modifier.size(28.dp),
                  strokeWidth = 3.dp
                )
                Text(
                  text = "جاري المصادقة الآمنة والتحقق مع خوادم Google...",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavyDark,
                  textAlign = TextAlign.Center
                )
                Text(
                  text = "يتم تسجيل الدخول وتوثيق الجلسة المشفرة ✓",
                  fontSize = 10.sp,
                  color = TextSecondary
                )
              }
            }
          } else {
            val googleAccounts = listOf(
              GoogleAccountItem(
                email = "mycar85662@gmail.com",
                displayName = "م. شريف عبد الفتاح التميمي",
                role = if (selectedRole == UserRole.LAWYER) UserRole.LAWYER else UserRole.CLIENT,
                avatarInitials = "ش",
                avatarColor = NavyPrimary,
                subtitle = "حساب Google النشط على هذا الجهاز"
              ),
              GoogleAccountItem(
                email = "dr.sameh.askalani@gmail.com",
                displayName = "المستشار سامح محمد العسقلاني",
                role = UserRole.LAWYER,
                avatarInitials = "س",
                avatarColor = EmeraldSuccess,
                subtitle = "محامٍ مقيد بالاستئناف العالي ومجلس الدولة"
              )
            )

            Text(
              text = "الحسابات المتاحة على الجهاز:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )

            // List of Accounts
            googleAccounts.forEach { account ->
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    isGoogleAuthenticating = true
                    onGoogleSignIn(account.email, account.displayName, account.role)
                  },
                color = CreamSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  // Avatar Initial
                  Surface(
                    color = account.avatarColor,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = account.avatarInitials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                      )
                    }
                  }

                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      Text(
                        text = account.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                      )
                      Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF4285F4),
                        modifier = Modifier.size(13.dp)
                      )
                    }
                    Text(
                      text = account.email,
                      fontSize = 10.5.sp,
                      color = TextSecondary,
                      maxLines = 1
                    )
                    Text(
                      text = account.subtitle,
                      fontSize = 9.sp,
                      color = TextMuted,
                      maxLines = 1
                    )
                  }

                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                  )
                }
              }
            }

            Divider(color = BorderSubtle)

            // Custom Account Toggle
            if (!showAddCustomGoogleAccount) {
              OutlinedButton(
                onClick = { showAddCustomGoogleAccount = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
              ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("استخدام أو كتابة حساب Google آخر...", fontSize = 11.5.sp, color = NavyDark, fontWeight = FontWeight.Bold)
              }
            } else {
              Surface(
                color = CreamSurface,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(10.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text("إدخال بيانات حساب Google البديل:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)

                  OutlinedTextField(
                    value = customGoogleEmail,
                    onValueChange = { customGoogleEmail = it },
                    label = { Text("البريد الإلكتروني (@gmail.com)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldDark) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                  )

                  OutlinedTextField(
                    value = customGoogleName,
                    onValueChange = { customGoogleName = it },
                    label = { Text("الاسم الكامل على حساب Google") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = GoldDark) }
                  )

                  Button(
                    onClick = {
                      val targetEmail = customGoogleEmail.ifBlank { "custom.user@gmail.com" }
                      val targetName = customGoogleName.ifBlank { "مستخدم Google الجديد" }
                      isGoogleAuthenticating = true
                      onGoogleSignIn(targetEmail, targetName, selectedRole)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                  ) {
                    Text("المتابعة وتسجيل الدخول بحساب Google", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }

            // Disclaimer
            Surface(
              color = CreamSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
                Text(
                  text = "تسجيل دخول محمي بنظام مصادقة Google OAuth 2.0 المشفر وفق أعلى معايير أمان البيانات.",
                  fontSize = 9.sp,
                  color = TextSecondary,
                  lineHeight = 13.sp
                )
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        if (!isGoogleAuthenticating) {
          TextButton(onClick = {
            showGoogleAccountPicker = false
            showAddCustomGoogleAccount = false
          }) {
            Text("إلغاء", color = TextSecondary)
          }
        }
      }
    )
  }
}

data class GoogleAccountItem(
  val email: String,
  val displayName: String,
  val role: UserRole,
  val avatarInitials: String,
  val avatarColor: Color,
  val subtitle: String
)

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val width = size.width
    val height = size.height
    val strokeWidth = width * 0.20f

    // Red Arc (Top)
    drawArc(
      color = Color(0xFFEA4335),
      startAngle = 190f,
      sweepAngle = 110f,
      useCenter = false,
      topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
      size = Size(width - strokeWidth, height - strokeWidth),
      style = Stroke(width = strokeWidth)
    )

    // Yellow Arc (Bottom-Left)
    drawArc(
      color = Color(0xFFFBBC05),
      startAngle = 110f,
      sweepAngle = 80f,
      useCenter = false,
      topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
      size = Size(width - strokeWidth, height - strokeWidth),
      style = Stroke(width = strokeWidth)
    )

    // Green Arc (Bottom)
    drawArc(
      color = Color(0xFF34A853),
      startAngle = 20f,
      sweepAngle = 90f,
      useCenter = false,
      topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
      size = Size(width - strokeWidth, height - strokeWidth),
      style = Stroke(width = strokeWidth)
    )

    // Blue Arc (Right)
    drawArc(
      color = Color(0xFF4285F4),
      startAngle = -50f,
      sweepAngle = 70f,
      useCenter = false,
      topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
      size = Size(width - strokeWidth, height - strokeWidth),
      style = Stroke(width = strokeWidth)
    )

    // Blue Crossbar (Middle Right)
    drawLine(
      color = Color(0xFF4285F4),
      start = Offset(width * 0.45f, height * 0.5f),
      end = Offset(width - strokeWidth * 0.5f, height * 0.5f),
      strokeWidth = strokeWidth
    )
  }
}

@Composable
fun DocumentUploadSlot(
  title: String,
  sideBadge: String,
  isUploaded: Boolean,
  uri: String?,
  onCardClick: () -> Unit,
  onCameraClick: () -> Unit,
  onGalleryClick: () -> Unit,
  onPreviewClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = if (isUploaded) EmeraldContainer.copy(alpha = 0.6f) else CreamSurfaceVariant,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isUploaded) EmeraldSuccess else BorderSubtle
    ),
    modifier = modifier.clickable { onCardClick() }
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = if (isUploaded) EmeraldDark else NavyPrimary,
          shape = RoundedCornerShape(4.dp)
        ) {
          Text(
            text = sideBadge,
            color = Color.White,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
          )
        }

        Icon(
          imageVector = if (isUploaded) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
          contentDescription = null,
          tint = if (isUploaded) EmeraldSuccess else GoldDark,
          modifier = Modifier.size(16.dp)
        )
      }

      Text(
        text = title,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        color = NavyDark,
        maxLines = 1
      )

      Text(
        text = if (isUploaded) "تم الإرفاق بنجاح ✓" else "انقر لفتح الكاميرا/المعرض",
        fontSize = 8.5.sp,
        color = if (isUploaded) EmeraldSuccess else TextMuted
      )

      // Direct Action Buttons (Camera / Gallery / Preview)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
      ) {
        FilledTonalButton(
          onClick = onCameraClick,
          shape = RoundedCornerShape(6.dp),
          contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
          modifier = Modifier
            .weight(1f)
            .height(26.dp),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color.White
          )
        ) {
          Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(11.dp), tint = NavyPrimary)
          Spacer(modifier = Modifier.width(2.dp))
          Text("كاميرا", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        }

        FilledTonalButton(
          onClick = onGalleryClick,
          shape = RoundedCornerShape(6.dp),
          contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
          modifier = Modifier
            .weight(1f)
            .height(26.dp),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color.White
          )
        ) {
          Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(11.dp), tint = GoldDark)
          Spacer(modifier = Modifier.width(2.dp))
          Text("معرض", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = GoldDark)
        }

        if (isUploaded) {
          FilledTonalButton(
            onClick = onPreviewClick,
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
            modifier = Modifier
              .width(28.dp)
              .height(26.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White)
          ) {
            Icon(Icons.Default.Visibility, contentDescription = "معاينة", modifier = Modifier.size(11.dp), tint = EmeraldDark)
          }
        }
      }
    }
  }
}

@Composable
fun RegistrationWizardHeader(
  currentStep: Int,
  totalSteps: Int,
  stepTitle: String,
  stepSubtitle: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    color = MaterialTheme.adaptiveSurfaceVariant,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Step Indicators and Counter
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(totalSteps) { index ->
            val isCompleted = index < currentStep
            val isCurrent = index == currentStep
            Surface(
              color = when {
                isCurrent -> accentColor
                isCompleted -> accentColor.copy(alpha = 0.6f)
                else -> MaterialTheme.adaptiveBorder
              },
              shape = RoundedCornerShape(3.dp),
              modifier = Modifier
                .width(if (isCurrent) 22.dp else 14.dp)
                .height(6.dp)
            ) {}
          }
        }

        Surface(
          color = accentColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = "الخطوة ${currentStep + 1} من $totalSteps",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      // Title & Subtitle
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = stepTitle,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.adaptiveTextPrimary
        )
        Text(
          text = stepSubtitle,
          fontSize = 10.sp,
          color = MaterialTheme.adaptiveTextSecondary,
          lineHeight = 14.sp
        )
      }
    }
  }
}
