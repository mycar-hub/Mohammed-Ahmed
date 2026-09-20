package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MaitreRepository
import com.example.data.local.AppPreferences
import com.example.data.local.ThemeMode
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
  currentUser: UserProfile,
  appPreferences: AppPreferences = AppPreferences(),
  onRoleSwitch: (UserRole) -> Unit,
  onRequestVerification: (licenseNumber: String) -> Unit,
  onNavigateToVerification: () -> Unit,
  onNavigateToDeposit: () -> Unit,
  onNavigateToWithdrawal: (() -> Unit)? = null,
  onThemeModeChange: (ThemeMode) -> Unit = {},
  onNotificationsToggle: (Boolean) -> Unit = {},
  onBiometricToggle: (Boolean) -> Unit = {},
  onSoundAlertsToggle: (Boolean) -> Unit = {},
  onLogout: () -> Unit,
  onBackClick: (() -> Unit)? = null,
  onNavigateToAdmin: (() -> Unit)? = null,
  onNavigateToRequests: (() -> Unit)? = null
) {
  var showEditJudicialScopeDialog by remember { mutableStateOf(false) }

  // Editable Judicial Scope State
  var editSubBarExpanded by remember { mutableStateOf(false) }
  var editSubBar by remember(currentUser) { mutableStateOf(currentUser.subBarAssociation ?: EgyptSubBarHelper.egyptianSubBars[0]) }
  var editPracticeDegrees by remember(currentUser) {
    mutableStateOf(
      if (currentUser.desiredPracticeDegrees.isNotEmpty()) {
        currentUser.desiredPracticeDegrees.toSet()
      } else {
        setOf(
          JudicialPracticeCategory.HIGH_APPEAL_AND_STATE_COUNCIL.id,
          JudicialPracticeCategory.PRIMARY_AND_MISDEMEANOR.id,
          JudicialPracticeCategory.TRAINEE_AND_SUMMARY.id
        )
      }
    )
  }
  var editGovernorates by remember(currentUser) {
    mutableStateOf(
      if (currentUser.selectedGovernorates.isNotEmpty()) currentUser.selectedGovernorates.toSet()
      else setOf(currentUser.assignedGovernorate, "الجيزة")
    )
  }
  var editCourts by remember(currentUser) {
    mutableStateOf(
      if (currentUser.selectedCourts.isNotEmpty()) currentUser.selectedCourts.toSet()
      else setOf("دار القضاء العالي (النقض والاستئناف)", "مجمع محاكم شمال القاهرة (العباسية)", "محكمة الجيزة الابتدائية (السودان)")
    )
  }
  var editDistricts by remember(currentUser) {
    mutableStateOf(
      if (currentUser.selectedDistricts.isNotEmpty()) currentUser.selectedDistricts.toSet()
      else setOf("مصر الجديدة", "الدقي", "التجمع الخامس", "مدينة نصر")
    )
  }
  var editCustomCourtInput by remember { mutableStateOf("") }
  var scopeUpdateSavedSnackbar by remember { mutableStateOf(false) }
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            when (currentUser.role) {
              UserRole.CLIENT -> "حساب الموكل وإعدادات الأمان"
              UserRole.LAWYER -> "ملف المحامي واعتماد النقابة"
              UserRole.ADMIN -> "حساب المشرف والرقابة القانونية"
            },
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        },
        navigationIcon = {
          if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
              Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
      )
    },
    containerColor = MaterialTheme.adaptiveBackground
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // =========================================================================
      // 1. Role-Specific User Profile Header
      // =========================================================================
      item {
        Surface(
          color = MaterialTheme.adaptiveSurface,
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
          shadowElevation = 1.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              // Avatar
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(
                    when (currentUser.role) {
                      UserRole.CLIENT -> NavyPrimary
                      UserRole.LAWYER -> EmeraldDark
                      UserRole.ADMIN -> NavyDark
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                if (currentUser.role == UserRole.ADMIN) {
                  Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = GoldLight,
                    modifier = Modifier.size(32.dp)
                  )
                } else {
                  Text(
                    text = currentUser.name.take(2),
                    color = if (currentUser.role == UserRole.LAWYER) Color.White else GoldLight,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = currentUser.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = MaterialTheme.adaptiveTextPrimary
                )

                when (currentUser.role) {
                  UserRole.CLIENT -> {
                    if (!currentUser.companyName.isNullOrBlank()) {
                      Text(
                        text = "جهة العمل: ${currentUser.companyName}",
                        fontSize = 12.sp,
                        color = NavyPrimary,
                        fontWeight = FontWeight.SemiBold
                      )
                    } else {
                      Text("حساب أفراد موكل موثق", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                    }
                  }
                  UserRole.LAWYER -> {
                    Text(
                      text = currentUser.barDegree.formalTitleAr,
                      fontSize = 12.sp,
                      color = EmeraldSuccess,
                      fontWeight = FontWeight.Bold
                    )
                    if (!currentUser.companyName.isNullOrBlank()) {
                      Text(
                        text = "مكتب: ${currentUser.companyName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.adaptiveTextSecondary
                      )
                    }
                  }
                  UserRole.ADMIN -> {
                    Text(
                      text = "إدارة المنصة وهيئة التحكيم العليا",
                      fontSize = 12.sp,
                      color = GoldDark,
                      fontWeight = FontWeight.Bold
                    )
                    Text("صلاحيات الإشراف والرقابة القضائية", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
                  }
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(text = currentUser.email, fontSize = 11.5.sp, color = MaterialTheme.adaptiveTextSecondary)
                  if (currentUser.email.contains("gmail.com", ignoreCase = true) || currentUser.id.contains("google")) {
                    Surface(
                      color = Color(0xFF4285F4).copy(alpha = 0.12f),
                      shape = RoundedCornerShape(4.dp)
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = null,
                          tint = Color(0xFF4285F4),
                          modifier = Modifier.size(10.dp)
                        )
                        Text(
                          text = "Google OAuth",
                          fontSize = 8.5.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFF4285F4)
                        )
                      }
                    }
                  }
                }
                Text(text = currentUser.phone, fontSize = 11.5.sp, color = MaterialTheme.adaptiveTextMuted)
              }

              // Role Badge
              Surface(
                color = when (currentUser.role) {
                  UserRole.CLIENT -> GoldContainer
                  UserRole.LAWYER -> EmeraldContainer
                  UserRole.ADMIN -> NavyDark
                },
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = when (currentUser.role) {
                    UserRole.CLIENT -> "مستخدم (موكل)"
                    UserRole.LAWYER -> "محامٍ معتمد"
                    UserRole.ADMIN -> "مشرف النظام"
                  },
                  color = when (currentUser.role) {
                    UserRole.CLIENT -> GoldDark
                    UserRole.LAWYER -> EmeraldSuccess
                    UserRole.ADMIN -> GoldSecondary
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            HorizontalDivider(color = MaterialTheme.adaptiveBorder)

            // Identity verification line tailored to role
            when (currentUser.role) {
              UserRole.CLIENT -> {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(
                      imageVector = if (currentUser.nafathVerified) Icons.Default.Verified else Icons.Default.Warning,
                      contentDescription = null,
                      tint = if (currentUser.nafathVerified) EmeraldSuccess else AmberWarning,
                      modifier = Modifier.size(18.dp)
                    )
                    Column {
                      Text(
                        text = if (currentUser.nafathVerified) "الهوية الوطنية موثقة رسمياً" else "الهوية بحاجة لتوثيق",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentUser.nafathVerified) EmeraldSuccess else AmberWarning
                      )
                      Text(
                        text = "الرقم القومي: ${currentUser.nationalIdOrCr ?: "29408151203948"}",
                        fontSize = 10.sp,
                        color = MaterialTheme.adaptiveTextMuted
                      )
                    }
                  }

                  TextButton(onClick = onNavigateToVerification) {
                    Text("تفاصيل التوثيق", fontSize = 11.5.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                  }
                }
              }

              UserRole.LAWYER -> {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(
                      imageVector = if (currentUser.isVerified) Icons.Default.VerifiedUser else Icons.Default.Pending,
                      contentDescription = null,
                      tint = if (currentUser.isVerified) EmeraldSuccess else AmberWarning,
                      modifier = Modifier.size(18.dp)
                    )
                    Column {
                      Text(
                        text = if (currentUser.isVerified) "قيد نقابة المحامين معتمد وموثق ✓" else "ملف القيد بانتظار اعتماد المشرف",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentUser.isVerified) EmeraldSuccess else AmberWarning
                      )
                      Text(
                        text = "رقم القيد: ${currentUser.licenseNumber ?: "لم يسجل"} • ${currentUser.assignedGovernorate}",
                        fontSize = 10.sp,
                        color = MaterialTheme.adaptiveTextMuted
                      )
                    }
                  }

                  TextButton(onClick = onNavigateToVerification) {
                    Text("فحص المستندات", fontSize = 11.5.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                  }
                }
              }

              UserRole.ADMIN -> {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(
                      Icons.Default.Security,
                      contentDescription = null,
                      tint = GoldDark,
                      modifier = Modifier.size(18.dp)
                    )
                    Text(
                      text = "اعتماد أمني كامل • صلاحيات التحكيم والرقابة القضائية",
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.adaptiveTextPrimary
                    )
                  }
                }
              }
            }
          }
        }
      }

      // =========================================================================
      // 2. Financial / Wallet Section (Tailored to Each Role)
      // =========================================================================
      item {
        when (currentUser.role) {
          // CLIENT: Escrow Deposit Wallet
          UserRole.CLIENT -> {
            Surface(
              color = NavyDark,
              shape = RoundedCornerShape(16.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text("رصيد محفظة الضمان المالي", fontSize = 11.sp, color = GoldLight)
                  Text(
                    "${currentUser.balance.toInt()} ج.م",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                  )
                  Text("محمية بحساب الضمان للقضايا والاستشارات", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                }

                Button(
                  onClick = onNavigateToDeposit,
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.AddCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("شحن وإيداع", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
              }
            }
          }

          // LAWYER: Professional Fees & Withdrawal
          UserRole.LAWYER -> {
            Surface(
              color = Color(0xFF064E3B), // Emerald Dark
              shape = RoundedCornerShape(16.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text("محفظة الأتعاب المهنية والأرباح المتاحة", fontSize = 11.sp, color = EmeraldLight)
                  Text(
                    "${currentUser.balance.toInt()} ج.م",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                  )
                  Text("أتعاب محررة جاهزة للسحب لحسابك البنكي أو InstaPay", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                }

                Button(
                  onClick = { onNavigateToWithdrawal?.invoke() ?: onNavigateToDeposit() },
                  colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("طلب سحب أرباح", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          // ADMIN: Platform Financial & Escrow Overview
          UserRole.ADMIN -> {
            Surface(
              color = NavyDark,
              shape = RoundedCornerShape(16.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text("الخزينة المجمعة وأموال الضمان المالي", fontSize = 11.sp, color = GoldLight)
                  Text(
                    "إدارة التدفقات المالية",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Text("رقابة وإشراف على أموال الضمان وعمولات المنصة وأحكام التحكيم", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                }

                if (onNavigateToAdmin != null) {
                  Button(
                    onClick = onNavigateToAdmin,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
                    shape = RoundedCornerShape(12.dp)
                  ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("سجلات الخزينة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      }

      // =========================================================================
      // 3. Role-Specific Middle Sections (NO OVERLAP)
      // =========================================================================

      // A. CLIENT EXCLUSIVE: My Legal Requests & Case Activity
      if (currentUser.role == UserRole.CLIENT) {
        item {
          Surface(
            color = MaterialTheme.adaptiveSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
            modifier = Modifier.fillMaxWidth()
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Default.Assignment, contentDescription = null, tint = NavyPrimary)
                  Text("قضاياي وطلباتي القانونية النشطة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.adaptiveTextPrimary)
                }
                Surface(color = GoldContainer, shape = RoundedCornerShape(6.dp)) {
                  Text("خدمات الموكل", color = GoldDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }

              Text(
                "يمكنك متابعة العروض المقدمة من المحامين المعتمدين، والتواصل الآمن وإدارة مبالغ الضمان المودعة.",
                fontSize = 11.5.sp,
                color = MaterialTheme.adaptiveTextSecondary
              )

              if (onNavigateToRequests != null) {
                OutlinedButton(
                  onClick = onNavigateToRequests,
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.fillMaxWidth(),
                  border = androidx.compose.foundation.BorderStroke(1.dp, NavyPrimary),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                ) {
                  Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("عرض كافة طلباتي وقضاياي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      // B. LAWYER EXCLUSIVE: Bar Association Accreditation & Professional Jurisdiction
      if (currentUser.role == UserRole.LAWYER) {
        item {
          Surface(
            color = MaterialTheme.adaptiveSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
            modifier = Modifier.fillMaxWidth()
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess)
                  Text("بيانات القيد بنقابة المحامين المصرية", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.adaptiveTextPrimary)
                }

                if (currentUser.isVerified) {
                  Surface(color = EmeraldContainer, shape = RoundedCornerShape(6.dp)) {
                    Text("معتمد رسمياً ✓", color = EmeraldSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                  }
                } else if (currentUser.pendingVerification) {
                  Surface(color = AmberContainer, shape = RoundedCornerShape(6.dp)) {
                    Text("قيد تدقيق المشرف ⏳", color = AmberWarning, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                  }
                }
              }

              // License Details Grid
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(MaterialTheme.adaptiveSurfaceVariant, RoundedCornerShape(10.dp))
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("رقم القيد بالنقابة:", fontSize = 11.5.sp, color = MaterialTheme.adaptiveTextSecondary)
                  Text(currentUser.licenseNumber ?: "لم يسجل", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("درجة القيد المعتمدة:", fontSize = 11.5.sp, color = MaterialTheme.adaptiveTextSecondary)
                  Text(currentUser.barDegree.formalTitleAr, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("النقابة الفرعية / المحافظة:", fontSize = 11.5.sp, color = MaterialTheme.adaptiveTextSecondary)
                  Text(currentUser.assignedGovernorate, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                }
                if (!currentUser.assignedCourtJurisdiction.isNullOrBlank()) {
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("نطاق المحاكم المعتمدة:", fontSize = 11.5.sp, color = MaterialTheme.adaptiveTextSecondary)
                    Text(currentUser.assignedCourtJurisdiction, fontSize = 10.5.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                  }
                }
              }

              // Rejection alert if any
              if (!currentUser.rejectionReason.isNullOrBlank()) {
                Surface(
                  color = CrimsonContainer,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(18.dp))
                    Column {
                      Text("تنبيه من المشرف بخصوص طلب القيد:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CrimsonError)
                      Text(currentUser.rejectionReason, fontSize = 10.5.sp, color = MaterialTheme.adaptiveTextPrimary)
                    }
                  }
                }
              }

              Button(
                onClick = onNavigateToVerification,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  if (currentUser.isVerified) "مراجعة وتحديث مستندات القيد والكارنيه" else "إرفاق وتحديث مستندات القيد للفحص",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // B.2 LAWYER EXCLUSIVE: Judicial Scope & Practice Locations Card (Editable post-registration)
        item {
          Surface(
            color = MaterialTheme.adaptiveSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldDark)
                  Text("نطاق العمل القضائي وأماكن الممارسة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.adaptiveTextPrimary)
                }
                Surface(color = GoldContainer, shape = RoundedCornerShape(6.dp)) {
                  Text("قابل للتعديل ✓", color = GoldDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }

              // 1. Sub-Bar
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(MaterialTheme.adaptiveSurfaceVariant, RoundedCornerShape(10.dp))
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Default.AccountBalance, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                  Text("النقابة الفرعية:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextSecondary)
                  Text(currentUser.subBarAssociation ?: "نقابة المحامين الفرعية بالقاهرة", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
                }
              }

              // 2. Desired Practice Categories
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("درجات وتصنيفات القضايا المرغوبة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                FlowRow(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  val activeDegreeCats = if (currentUser.desiredPracticeDegrees.isNotEmpty()) {
                    JudicialPracticeCategory.values().filter { it.id in currentUser.desiredPracticeDegrees }
                  } else {
                    JudicialPracticeCategory.values().filter { JudicialPracticeCategory.isAllowedForActualDegree(it, currentUser.barDegree) }
                  }
                  activeDegreeCats.forEach { cat ->
                    Surface(
                      color = EmeraldContainer,
                      shape = RoundedCornerShape(6.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                      ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
                        Text(cat.titleAr, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                      }
                    }
                  }
                }
              }

              // 3. Workplaces: Governorates, Courts & Districts
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("محافظات ومحاكم ومناطق العمل المختارة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                FlowRow(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  val govs = if (currentUser.selectedGovernorates.isNotEmpty()) currentUser.selectedGovernorates else listOf(currentUser.assignedGovernorate, "الجيزة")
                  val courts = if (currentUser.selectedCourts.isNotEmpty()) currentUser.selectedCourts else listOf("دار القضاء العالي", "محكمة العباسية", "محكمة الجيزة")
                  val districts = if (currentUser.selectedDistricts.isNotEmpty()) currentUser.selectedDistricts else listOf("مصر الجديدة", "الدقي", "التجمع الخامس")

                  govs.forEach { gov ->
                    Surface(color = GoldContainer, shape = RoundedCornerShape(4.dp)) {
                      Text("📍 $gov", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = GoldDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                  }
                  courts.take(4).forEach { court ->
                    Surface(color = MaterialTheme.adaptiveSurfaceVariant, shape = RoundedCornerShape(4.dp)) {
                      Text("🏛️ $court", fontSize = 9.5.sp, color = MaterialTheme.adaptiveTextPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                  }
                  districts.take(3).forEach { dist ->
                    Surface(color = MaterialTheme.adaptiveSurfaceVariant, shape = RoundedCornerShape(4.dp)) {
                      Text("🏢 $dist", fontSize = 9.5.sp, color = MaterialTheme.adaptiveTextSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                  }
                }
              }

              OutlinedButton(
                onClick = {
                  editSubBar = currentUser.subBarAssociation ?: EgyptSubBarHelper.egyptianSubBars[0]
                  editPracticeDegrees = if (currentUser.desiredPracticeDegrees.isNotEmpty()) currentUser.desiredPracticeDegrees.toSet() else setOf(JudicialPracticeCategory.HIGH_APPEAL_AND_STATE_COUNCIL.id, JudicialPracticeCategory.PRIMARY_AND_MISDEMEANOR.id)
                  editGovernorates = if (currentUser.selectedGovernorates.isNotEmpty()) currentUser.selectedGovernorates.toSet() else setOf(currentUser.assignedGovernorate, "الجيزة")
                  editCourts = if (currentUser.selectedCourts.isNotEmpty()) currentUser.selectedCourts.toSet() else setOf("دار القضاء العالي (النقض والاستئناف)", "مجمع محاكم شمال القاهرة (العباسية)")
                  editDistricts = if (currentUser.selectedDistricts.isNotEmpty()) currentUser.selectedDistricts.toSet() else setOf("مصر الجديدة", "الدقي")
                  showEditJudicialScopeDialog = true
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark)
              ) {
                Icon(Icons.Default.EditLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تعديل نطاق العمل القضائي والمحاكم", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // C. ADMIN EXCLUSIVE: Oversight & Approval Shortcuts
      if (currentUser.role == UserRole.ADMIN && onNavigateToAdmin != null) {
        item {
          Surface(
            color = MaterialTheme.adaptiveSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldDark)
                  Text("لوحة التحكم والرقابة القانونية (مصر)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.adaptiveTextPrimary)
                }
                Surface(color = NavyDark, shape = RoundedCornerShape(6.dp)) {
                  Text("مشرف معتمد", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }

              Text(
                "بصفتك مشرفاً للنظام، يمكنك مراجعة كافة طلبات تسجيل المحامين الجديدة، تدقيق بطاقات الرقم القومي وكارنيهات النقابة وش وظهر، والموافقة عليها أو رفضها مع ذكر السبب.",
                fontSize = 11.5.sp,
                color = MaterialTheme.adaptiveTextSecondary,
                lineHeight = 16.sp
              )

              Button(
                onClick = onNavigateToAdmin,
                colors = ButtonDefaults.buttonColors(containerColor = NavyDark, contentColor = GoldSecondary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("فتح لوحة المشرف وفحص طلبات التسجيل الجديدة", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
              }
            }
          }
        }
      }

      // =========================================================================
      // 4. Current System Role Info Card
      // =========================================================================
      item {
        Surface(
          color = MaterialTheme.adaptiveSurface,
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Security, contentDescription = null, tint = GoldDark)
              Text(
                "نظام الحساب الحالي",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MaterialTheme.adaptiveTextPrimary
              )
            }

            Surface(
              color = MaterialTheme.adaptiveSurfaceVariant,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(
                    imageVector = when (currentUser.role) {
                      UserRole.CLIENT -> Icons.Default.Person
                      UserRole.LAWYER -> Icons.Default.Gavel
                      UserRole.ADMIN -> Icons.Default.Shield
                    },
                    contentDescription = null,
                    tint = when (currentUser.role) {
                      UserRole.CLIENT -> NavyPrimary
                      UserRole.LAWYER -> EmeraldSuccess
                      UserRole.ADMIN -> GoldDark
                    },
                    modifier = Modifier.size(22.dp)
                  )
                  Column {
                    Text(
                      text = when (currentUser.role) {
                        UserRole.CLIENT -> "حساب الموكل (المستخدم النهائي)"
                        UserRole.LAWYER -> "حساب المحامي المعتمد (النقابة)"
                        UserRole.ADMIN -> "حساب المشرف والرقابة القانونية"
                      },
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                      color = MaterialTheme.adaptiveTextPrimary
                    )
                    Text(
                      text = "الصلاحيات محددة ومنفصلة تماماً وفق هذا الدور",
                      fontSize = 10.sp,
                      color = MaterialTheme.adaptiveTextMuted
                    )
                  }
                }

                OutlinedButton(
                  onClick = onLogout,
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                  border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError.copy(alpha = 0.5f))
                ) {
                  Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("تبديل الحساب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      // =========================================================================
      // 5. Theme and Application Preferences Card (DataStore backed)
      // =========================================================================
      item {
        Surface(
          color = MaterialTheme.adaptiveSurface,
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = NavyPrimary)
                Text("المظهر وإعدادات التفضيلات", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.adaptiveTextPrimary)
              }
              Surface(
                color = GoldContainer,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  "تفضيلات النظام",
                  color = GoldDark,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Text(
              "تفضيلات المظهر والسمات البصرية:",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.adaptiveTextSecondary
            )

            // Theme Mode Selector Chips
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              ThemeMode.values().forEach { mode ->
                val isSelected = appPreferences.themeMode == mode
                OutlinedButton(
                  onClick = { onThemeModeChange(mode) },
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) NavyPrimary else Color.Transparent,
                    contentColor = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary
                  ),
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) NavyPrimary else MaterialTheme.adaptiveBorder
                  ),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f),
                  contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) {
                  Icon(
                    imageVector = when (mode) {
                      ThemeMode.LIGHT -> Icons.Default.LightMode
                      ThemeMode.DARK -> Icons.Default.DarkMode
                      ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = when (mode) {
                      ThemeMode.LIGHT -> "فاتح"
                      ThemeMode.DARK -> "داكن"
                      ThemeMode.SYSTEM -> "تلقائي"
                    },
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                }
              }
            }

            HorizontalDivider(color = MaterialTheme.adaptiveBorder)

            // Notifications Switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                Column {
                  Text("الإشعارات الفورية للعروض والقضايا", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.adaptiveTextPrimary)
                  Text("استلام تنبيهات فورية عند وصول عروض أو أحكام جديدة", fontSize = 10.sp, color = MaterialTheme.adaptiveTextSecondary)
                }
              }
              Switch(
                checked = appPreferences.notificationsEnabled,
                onCheckedChange = onNotificationsToggle,
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = NavyPrimary
                )
              )
            }

            // Sound Alerts Switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                Column {
                  Text("التنبيهات الصوتية", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.adaptiveTextPrimary)
                  Text("تشغيل نغمات تنبيه عند قبول العقود أو تحرير الضمان", fontSize = 10.sp, color = MaterialTheme.adaptiveTextSecondary)
                }
              }
              Switch(
                checked = appPreferences.soundAlertsEnabled,
                onCheckedChange = onSoundAlertsToggle,
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = NavyPrimary
                )
              )
            }

            // Biometric Auth Switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                Column {
                  Text("حماية الجلسة بالبصمة البيومترية", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.adaptiveTextPrimary)
                  Text("طلب البصمة قبل فتح المحفظة أو سحب الأرباح", fontSize = 10.sp, color = MaterialTheme.adaptiveTextSecondary)
                }
              }
              Switch(
                checked = appPreferences.biometricAuthEnabled,
                onCheckedChange = onBiometricToggle,
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = EmeraldSuccess
                )
              )
            }
          }
        }
      }

      // =========================================================================
      // 6. Logout Action
      // =========================================================================
      item {
        OutlinedButton(
          onClick = onLogout,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
          border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Icon(Icons.Default.Logout, contentDescription = null, tint = CrimsonError)
          Spacer(modifier = Modifier.width(8.dp))
          Text("تسجيل الخروج والعودة لشاشة الدخول", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }

  // =========================================================================
  // Edit Judicial Scope Dialog (Sub-Bar, Desired Degrees, Workplaces)
  // =========================================================================
  if (showEditJudicialScopeDialog) {
    AlertDialog(
      onDismissRequest = { showEditJudicialScopeDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldDark)
          Text("تعديل نطاق العمل القضائي والمحاكم", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            "يمكنك تحديث النقابة الفرعية، درجات وتصنيفات القضايا (بما لا يتجاوز درجتك ${currentUser.barDegree.titleAr})، وأماكن المحاكم والمحافظات.",
            fontSize = 11.sp,
            color = MaterialTheme.adaptiveTextSecondary
          )

          // 1. Sub-Bar Dropdown
          Text("النقابة الفرعية:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          ExposedDropdownMenuBox(
            expanded = editSubBarExpanded,
            onExpandedChange = { editSubBarExpanded = !editSubBarExpanded },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = editSubBar,
              onValueChange = {},
              readOnly = true,
              label = { Text("اختر النقابة الفرعية") },
              leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = EmeraldSuccess) },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editSubBarExpanded) },
              modifier = Modifier.menuAnchor().fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              colors = maitreTextFieldColors()
            )
            ExposedDropdownMenu(
              expanded = editSubBarExpanded,
              onDismissRequest = { editSubBarExpanded = false },
              modifier = Modifier.heightIn(max = 240.dp)
            ) {
              EgyptSubBarHelper.egyptianSubBars.forEach { subBarName ->
                val isSelected = subBarName == editSubBar
                DropdownMenuItem(
                  text = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                      Text(subBarName, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                      if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                      }
                    }
                  },
                  onClick = {
                    editSubBar = subBarName
                    editSubBarExpanded = false
                  }
                )
              }
            }
          }

          // 2. Desired Practice Degrees (Constrained to actual degree)
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("درجات وتصنيفات القضايا المرغوبة (اختيار متعدد):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Text(
              "الحد الأقصى المتاح لك: ${currentUser.barDegree.formalTitleAr}",
              fontSize = 10.sp,
              color = EmeraldSuccess,
              fontWeight = FontWeight.SemiBold
            )

            JudicialPracticeCategory.values().forEach { cat ->
              val isAllowed = JudicialPracticeCategory.isAllowedForActualDegree(cat, currentUser.barDegree)
              val isChecked = cat.id in editPracticeDegrees

              Surface(
                color = when {
                  !isAllowed -> MaterialTheme.adaptiveSurfaceVariant.copy(alpha = 0.5f)
                  isChecked -> EmeraldContainer
                  else -> MaterialTheme.adaptiveSurface
                },
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isChecked && isAllowed) EmeraldSuccess else MaterialTheme.adaptiveBorder),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable(enabled = isAllowed) {
                    editPracticeDegrees = if (isChecked) {
                      if (editPracticeDegrees.size > 1) editPracticeDegrees - cat.id else editPracticeDegrees
                    } else {
                      editPracticeDegrees + cat.id
                    }
                  }
              ) {
                Row(
                  modifier = Modifier.padding(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Checkbox(
                    checked = isChecked && isAllowed,
                    onCheckedChange = { chk ->
                      if (isAllowed) {
                        editPracticeDegrees = if (chk) editPracticeDegrees + cat.id else (if (editPracticeDegrees.size > 1) editPracticeDegrees - cat.id else editPracticeDegrees)
                      }
                    },
                    enabled = isAllowed,
                    colors = CheckboxDefaults.colors(checkedColor = EmeraldSuccess)
                  )
                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      Text(cat.titleAr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isAllowed) MaterialTheme.adaptiveTextPrimary else MaterialTheme.adaptiveTextMuted)
                      if (!isAllowed) {
                        Text("🔒 غير متاح", fontSize = 8.5.sp, color = CrimsonError, fontWeight = FontWeight.Bold)
                      }
                    }
                    Text(cat.subtitleAr, fontSize = 9.sp, color = MaterialTheme.adaptiveTextSecondary)
                  }
                }
              }
            }
          }

          // 3. Governorates
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("المحافظات المشمولة بالعمل:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Text("(${editGovernorates.size} مختارة)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
            }
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              EgyptCourtsDirectory.governoratesList.forEach { gov ->
                val isSel = gov in editGovernorates
                FilterChip(
                  selected = isSel,
                  onClick = {
                    editGovernorates = if (isSel) {
                      if (editGovernorates.size > 1) editGovernorates - gov else editGovernorates
                    } else {
                      editGovernorates + gov
                    }
                  },
                  label = { Text(gov, fontSize = 10.sp) },
                  leadingIcon = if (isSel) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                  } else null
                )
              }
            }
          }

          val dialogCourts = remember(editGovernorates) {
            EgyptCourtsDirectory.getCourtsForGovernorates(editGovernorates)
          }
          val dialogDistricts = remember(editGovernorates) {
            EgyptCourtsDirectory.getDistrictsForGovernorates(editGovernorates)
          }

          // 4. Courts
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("المحاكم التابعة للمحافظات المختارة:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                  onClick = { editCourts = editCourts + dialogCourts },
                  contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                  Text("تحديد الكل", fontSize = 9.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                }
              }
            }
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              dialogCourts.forEach { court ->
                val isSel = court in editCourts
                FilterChip(
                  selected = isSel,
                  onClick = {
                    editCourts = if (isSel) {
                      if (editCourts.size > 1) editCourts - court else editCourts
                    } else {
                      editCourts + court
                    }
                  },
                  label = { Text(court, fontSize = 9.sp) },
                  leadingIcon = if (isSel) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                  } else null
                )
              }
            }

            // Custom court addition
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = editCustomCourtInput,
                onValueChange = { editCustomCourtInput = it },
                placeholder = { Text("إضافة محكمة أخرى", fontSize = 10.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = maitreTextFieldColors()
              )
              Button(
                onClick = {
                  if (editCustomCourtInput.isNotBlank()) {
                    editCourts = editCourts + editCustomCourtInput.trim()
                    editCustomCourtInput = ""
                  }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
              ) {
                Text("+", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          // 5. Districts
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("المناطق والأحياء التابعة للمحافظات المختارة:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                  onClick = { editDistricts = editDistricts + dialogDistricts },
                  contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                  Text("تحديد الكل", fontSize = 9.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                }
              }
            }
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              dialogDistricts.forEach { dist ->
                val isSel = dist in editDistricts
                FilterChip(
                  selected = isSel,
                  onClick = {
                    editDistricts = if (isSel) {
                      if (editDistricts.size > 1) editDistricts - dist else editDistricts
                    } else {
                      editDistricts + dist
                    }
                  },
                  label = { Text(dist, fontSize = 9.5.sp) },
                  leadingIcon = if (isSel) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                  } else null
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val scopeSummary = "محاكم: ${editCourts.take(2).joinToString("، ")} | محافظات: ${editGovernorates.joinToString("، ")}"
            MaitreRepository.updateLawyerJudicialScope(
              subBarAssociation = editSubBar,
              desiredPracticeDegrees = editPracticeDegrees.toList(),
              selectedGovernorates = editGovernorates.toList(),
              selectedCourts = editCourts.toList(),
              selectedDistricts = editDistricts.toList(),
              courtScopeSummary = scopeSummary
            )
            showEditJudicialScopeDialog = false
            scopeUpdateSavedSnackbar = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("حفظ التعديلات وتحديث النطاق القضائي", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditJudicialScopeDialog = false }) {
          Text("إلغاء", fontSize = 11.5.sp, color = MaterialTheme.adaptiveTextSecondary)
        }
      }
    )
  }
}
