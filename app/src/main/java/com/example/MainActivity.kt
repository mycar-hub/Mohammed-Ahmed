package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FirestoreChatService
import com.example.data.MaitreRepository
import com.example.data.local.AppPreferences
import com.example.data.local.ThemeMode
import com.example.model.*
import com.example.ui.components.LawyerDispatchPopup
import com.example.ui.components.MaitreBottomNavigation
import com.example.ui.components.MaitreTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    MaitreRepository.initDatabase(applicationContext)
    setContent {
      val appPreferences by MaitreRepository.appPreferences.collectAsStateWithLifecycle()
      val isDark = when (appPreferences.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
      }

      MyApplicationTheme(darkTheme = isDark) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          MaitreApp(appPreferences = appPreferences)
        }
      }
    }
  }
}

@Composable
fun MaitreApp(appPreferences: AppPreferences = AppPreferences()) {
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  // Reactive State from Repository with lifecycle awareness
  val currentUser by MaitreRepository.currentUser.collectAsStateWithLifecycle()
  val requests by MaitreRepository.requests.collectAsStateWithLifecycle()
  val lawyers by MaitreRepository.lawyers.collectAsStateWithLifecycle()
  val bids by MaitreRepository.bids.collectAsStateWithLifecycle()
  val escrowTransactions by MaitreRepository.escrowTransactions.collectAsStateWithLifecycle()
  val disputes by MaitreRepository.disputes.collectAsStateWithLifecycle()
  val messages by MaitreRepository.messages.collectAsStateWithLifecycle()
  val notifications by MaitreRepository.notifications.collectAsStateWithLifecycle()
  val deposits by MaitreRepository.deposits.collectAsStateWithLifecycle()
  val violations by MaitreRepository.violations.collectAsStateWithLifecycle()
  val securityRiskEvents by MaitreRepository.securityRiskEvents.collectAsStateWithLifecycle()
  val platformFeePercentage by MaitreRepository.platformFeePercentage.collectAsStateWithLifecycle()
  val clientPlatformFeePercentage by MaitreRepository.clientPlatformFeePercentage.collectAsStateWithLifecycle()
  val lawyerPlatformFeePercentage by MaitreRepository.lawyerPlatformFeePercentage.collectAsStateWithLifecycle()
  val withdrawalRequests by MaitreRepository.withdrawalRequests.collectAsStateWithLifecycle()
  val coupons by MaitreRepository.coupons.collectAsStateWithLifecycle()
  val platformWithdrawals by MaitreRepository.platformWithdrawals.collectAsStateWithLifecycle()
  val supervisoryDecisions by MaitreRepository.supervisoryDecisions.collectAsStateWithLifecycle()
  val realtimeAuditLogs by MaitreRepository.realtimeAuditLogs.collectAsStateWithLifecycle()
  val clientRegistrations by MaitreRepository.clientRegistrations.collectAsStateWithLifecycle()
  val isLawyerAvailable by MaitreRepository.isLawyerAvailable.collectAsStateWithLifecycle()
  val incomingDispatchRequest by MaitreRepository.incomingDispatchRequest.collectAsStateWithLifecycle()

  // Navigation State
  var currentRoute by remember { mutableStateOf("home") }
  var selectedRequestId by remember { mutableStateOf<String?>(null) }
  var selectedLawyerId by remember { mutableStateOf<String?>(null) }
  var selectedTemplateId by remember { mutableStateOf<String?>(null) }
  var pendingLocation by remember { mutableStateOf<GeoLocation?>(null) }

  val unreadNotificationsCount = notifications.count { !it.isRead }

  // Check if User is Logged In
  if (!currentUser.isLoggedIn) {
    AuthScreen(
      onLoginSuccess = { phone, role ->
        MaitreRepository.login(phone, role)
        currentRoute = "home"
        coroutineScope.launch {
          snackbarHostState.showSnackbar("مرحباً بك مجدداً في منصة مِتر!")
        }
      },
      onRegisterClient = { name, phone, email, nationalId, clientType, compName, gov, fUp, bUp, fUri, bUri ->
        MaitreRepository.registerClientWithKyc(name, phone, email, nationalId, clientType, compName, gov, fUp, bUp, fUri, bUri)
        currentRoute = "home"
        coroutineScope.launch {
          snackbarHostState.showSnackbar("تم تسجيل بيانات الهوية وحساب العميل بنجاح ✓")
        }
      },
      onRegisterLawyer = { name, phone, email, nationalId, licenseNo, degree, subBar, gov, courtScope, spec, expYears, firmName, barUp, idUp, barBackUp, idBackUp, idFrontUri, idBackUri, barFrontUri, barBackUri, desiredDegrees, selectedGovs, selectedCourts, selectedDistricts, lawyerTitle, bio, manualAddress, officeLat, officeLng ->
        MaitreRepository.registerLawyerWithKyc(
          name = name,
          phone = phone,
          email = email,
          nationalId = nationalId,
          licenseNumber = licenseNo,
          proposedDegree = degree,
          subBarAssociation = subBar,
          governorate = gov,
          courtJurisdictionScope = courtScope,
          specialization = spec,
          yearsExperience = expYears,
          firmName = firmName,
          barCardUploaded = barUp,
          idCardUploaded = idUp,
          barCardBackUploaded = barBackUp,
          idCardBackUploaded = idBackUp,
          idCardFrontUri = idFrontUri,
          idCardBackUri = idBackUri,
          barCardFrontUri = barFrontUri,
          barCardBackUri = barBackUri,
          desiredPracticeDegrees = desiredDegrees,
          selectedGovernorates = selectedGovs,
          selectedCourts = selectedCourts,
          selectedDistricts = selectedDistricts,
          lawyerTitle = lawyerTitle,
          bio = bio,
          officeAddressManually = manualAddress,
          officeLatitude = officeLat,
          officeLongitude = officeLng
        )
        currentRoute = "home"
        coroutineScope.launch {
          snackbarHostState.showSnackbar("تم إرسال مستندات المحامي وملف KYC لمسؤول النظام للمراجعة والاعتماد ⚖️")
        }
      },
      onRegisterSuccess = { name, phone, email, role, nationalId, licenseNo, firmName ->
        MaitreRepository.register(name, phone, email, role, nationalId, licenseNo, firmName)
        currentRoute = "home"
        coroutineScope.launch {
          snackbarHostState.showSnackbar("تم إنشاء حسابك وتوثيقه بنجاح!")
        }
      },
      onOpenOtpVerification = { phone ->
        currentRoute = "verification"
      },
      onOpenNafathVerification = {
        currentRoute = "verification"
      },
      onGoogleSignIn = { googleEmail, googleName, role ->
        MaitreRepository.loginWithGoogle(googleEmail, googleName, role)
        currentRoute = "home"
        coroutineScope.launch {
          snackbarHostState.showSnackbar("تم تسجيل الدخول بنجاح عبر حساب Google ($googleEmail) ✓")
        }
      }
    )
    return
  }

  val isTopLevelScreen = currentRoute in listOf("home", "tracker", "lawyers", "escrow", "profile", "admin", "lawyer_requests")

  Scaffold(
    containerColor = MaterialTheme.adaptiveBackground,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      if (isTopLevelScreen) {
        MaitreTopAppBar(
          currentUser = currentUser,
          unreadNotificationsCount = unreadNotificationsCount,
          onRoleSwitch = { role ->
            MaitreRepository.switchRole(role)
            coroutineScope.launch {
              snackbarHostState.showSnackbar("تم التبديل إلى: ${if (role == UserRole.LAWYER) "المحامي المعتمد" else if (role == UserRole.ADMIN) "مشرف المنصة والتحكيم" else "المستخدم النهائي"}")
            }
          },
          onNotificationsClick = { currentRoute = "notifications" },
          onWalletClick = { currentRoute = "deposit" }
        )
      }
    },
    bottomBar = {
      if (isTopLevelScreen) {
        MaitreBottomNavigation(
          currentScreen = currentRoute,
          userRole = currentUser.role,
          onNavigate = { destination ->
            currentRoute = destination
          }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentRoute) {
        "home" -> {
          HomeScreen(
            currentUser = currentUser,
            requests = requests,
            lawyers = lawyers,
            onNewRequestClick = {
              if (currentUser.role == UserRole.LAWYER) {
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("تنبيه: لا يجوز للمحامي طلب تقديم خدمة. دورك هو متلقٍ للطلبات في إطار اختصاصك الجغرافي.")
                }
              } else if (currentUser.role == UserRole.ADMIN) {
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("تنبيه: حساب الإدارة والرقابة مخصص للإشراف والمتابعة فقط.")
                }
              } else {
                selectedTemplateId = null
                currentRoute = "create_request"
              }
            },
            onSelectTemplateClick = { templateId ->
              if (currentUser.role == UserRole.LAWYER || currentUser.role == UserRole.ADMIN) {
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("طلب الخدمات متاح للموكلين والعملاء فقط.")
                }
              } else {
                selectedTemplateId = templateId
                currentRoute = "create_request"
              }
            },
            onRequestClick = { reqId ->
              selectedRequestId = reqId
              currentRoute = "request_detail"
            },
            onLawyerClick = { lawyerId ->
              selectedLawyerId = lawyerId
              currentRoute = "lawyer_detail"
            },
            onBrowseLawyersClick = { currentRoute = "lawyers" },
            onEscrowClick = { currentRoute = "deposit" },
            onTrackerClick = { currentRoute = "tracker" },
            onAdminClick = { currentRoute = "admin" },
            isLawyerAvailable = isLawyerAvailable,
            onToggleLawyerAvailability = { available ->
              MaitreRepository.setLawyerAvailable(available)
              coroutineScope.launch {
                snackbarHostState.showSnackbar(
                  if (available) "أنت الآن متاح لتلقي الطلبات الفورية بنطاقك الجغرافي ✓"
                  else "أنت الآن متوقف عن تلقي الطلبات والإشعارات مؤقتاً."
                )
              }
            },
            onNavigateToLawyerRequests = { currentRoute = "lawyer_requests" },
            onSimulateIncomingRequest = {
              val simReq = MaitreRepository.simulateIncomingRequestForLawyer()
              coroutineScope.launch {
                snackbarHostState.showSnackbar("ورد طلب فوري مطابق لاختصاصك الجغرافي (${simReq.city} - ${simReq.courtLocation?.district})!")
              }
            }
          )
        }

        "tracker" -> {
          RequestTrackerScreen(
            currentUser = currentUser,
            requests = requests,
            bids = bids,
            lawyers = lawyers,
            escrowTransactions = escrowTransactions,
            disputes = disputes,
            onBackClick = null,
            onRequestClick = { reqId ->
              selectedRequestId = reqId
              currentRoute = "request_detail"
            },
            onOpenWorkspace = { reqId ->
              selectedRequestId = reqId
              currentRoute = "chat"
            },
            onOpenEscrow = { reqId ->
              currentRoute = "escrow"
            },
            onOpenDispute = { reqId, reason, details ->
              MaitreRepository.openDispute(reqId, reason, details)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم تسجيل النزاع وتجميد الضمان وإحالة القضية للجنة التحكيم ⚖️")
              }
            },
            onUpdateStatus = { reqId, newStatus ->
              MaitreRepository.updateRequestStatus(reqId, newStatus)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم تحديث مسار القضية بنجاح ✓")
              }
            },
            onNewRequestClick = {
              selectedTemplateId = null
              currentRoute = "create_request"
            }
          )
        }

        "lawyers" -> {
          if (currentUser.role == UserRole.LAWYER) {
            currentRoute = "home"
            coroutineScope.launch {
              snackbarHostState.showSnackbar("صفحة دليل المحامين غير متاحة لحساب المحامي.")
            }
          } else {
            LawyersListScreen(
              lawyers = lawyers,
              currentUserRole = currentUser.role,
              assignedGovernorate = currentUser.assignedGovernorate,
              onBackClick = null,
              onLawyerClick = { lawyerId ->
                selectedLawyerId = lawyerId
                currentRoute = "lawyer_detail"
              },
              onRequestConsultation = {
                selectedTemplateId = null
                currentRoute = "create_request"
              }
            )
          }
        }

        "lawyer_requests" -> {
          if (currentUser.role != UserRole.LAWYER) {
            currentRoute = "home"
          } else {
            LawyerRequestsScreen(
              currentUser = currentUser,
              isAvailable = isLawyerAvailable,
              requests = requests,
              bids = bids,
              clientFeePercentage = clientPlatformFeePercentage,
              onToggleAvailability = { available ->
                MaitreRepository.setLawyerAvailable(available)
                coroutineScope.launch {
                  snackbarHostState.showSnackbar(
                    if (available) "أنت الآن متاح لتلقي الطلبات الفورية بنطاقك الجغرافي ✓"
                    else "أنت الآن متوقف عن تلقي الطلبات والإشعارات مؤقتاً."
                  )
                }
              },
              onSimulateIncomingRequest = {
                val simReq = MaitreRepository.simulateIncomingRequestForLawyer()
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("ورد طلب فوري مطابق لاختصاصك الجغرافي (${simReq.city} - ${simReq.courtLocation?.district})!")
                }
              },
              onRequestClick = { reqId ->
                selectedRequestId = reqId
                currentRoute = "request_detail"
              },
              onSubmitBid = { reqId, fee, exp, days, note ->
                val res = MaitreRepository.addBid(
                  requestId = reqId,
                  lawyerFee = fee,
                  legalExpenses = exp,
                  proposedDays = days,
                  proposalNote = note
                )
                if (res.isSuccess) {
                  selectedRequestId = reqId
                  coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تقديم عرضك بنجاح وحساب السعر النهائي للعميل!")
                  }
                  currentRoute = "request_detail"
                }
              },
              onOpenWorkspace = { reqId ->
                selectedRequestId = reqId
                currentRoute = "chat"
              }
            )
          }
        }

        "escrow" -> {
          EscrowScreen(
            currentUser = currentUser,
            transactions = escrowTransactions,
            requests = requests,
            deposits = deposits,
            withdrawalRequests = withdrawalRequests,
            onRequestWithdrawal = { amount, completedReqId, account, accountType ->
              val result = MaitreRepository.requestLawyerWithdrawal(amount, completedReqId, account, accountType)
              coroutineScope.launch {
                result.onSuccess {
                  snackbarHostState.showSnackbar("تم إرسال طلب سحب مبلغ ${amount.toInt()} ج.م لمدير النظام للاعتماد ✓")
                }.onFailure { err ->
                  snackbarHostState.showSnackbar(err.message ?: "تعذر تقديم طلب السحب: تأكد من كفاية الرصيد واكتمال القضية.")
                }
              }
            },
            onBackClick = null,
            onNavigateToDeposit = { currentRoute = "deposit" },
            onFundEscrow = { reqId, total, lawyerFee, legalExpenses, platFee, method ->
              MaitreRepository.fundCaseEscrow(reqId, total, lawyerFee, legalExpenses, platFee, method)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم إيداع وتأمين مبلغ ${total.toInt()} ج.م بحساب الضمان المالي بنجاح ✓")
              }
            },
            onReleaseEscrow = { reqId, stars, comment ->
              MaitreRepository.releaseEscrowWithReview(reqId, stars, comment)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم تحرير الأتعاب للمحامي وإصدار المخالصة الرسمية بنجاح ✓")
              }
            },
            onOpenDispute = { reqId, reason, details ->
              MaitreRepository.openDispute(reqId, reason, details)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم تجميد الضمان وإحالة القضية للجنة التحكيم وفض المنازعات ⚖️")
              }
            },
            onRequestClick = { reqId ->
              selectedRequestId = reqId
              currentRoute = "request_detail"
            },
            onOpenWorkspace = { reqId ->
              selectedRequestId = reqId
              currentRoute = "chat_workspace"
            }
          )
        }

        "deposit" -> {
          DepositScreen(
            currentUser = currentUser,
            deposits = deposits,
            onBackClick = { currentRoute = "escrow" },
            onDepositConfirm = { amount, method, iban ->
              MaitreRepository.depositFunds(amount, method, iban)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم سداد مبلغ ${amount.toInt()} ج.م رسم خدمة المنصة بنجاح ✓")
              }
              currentRoute = "escrow"
            }
          )
        }

        "verification" -> {
          VerificationScreen(
            currentUser = currentUser,
            onBackClick = { currentRoute = "profile" },
            onVerifySuccess = {
              MaitreRepository.verifyNafath()
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم توثيق بطاقة الرقم القومي المصري بنجاح ✓")
              }
            },
            onVerifyLawyerLicense = { licenseNumber ->
              MaitreRepository.verifyLawyerBarLicense(licenseNumber)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم اعتماد قيد نقابة المحامين المصرية بنجاح ✓")
              }
            }
          )
        }

        "location_picker" -> {
          LocationPickerScreen(
            initialLocation = pendingLocation,
            onBackClick = { currentRoute = "create_request" },
            onLocationSelected = { loc ->
              pendingLocation = loc
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم اعتماد الاختصاص القضائي: ${loc.courtJurisdiction}")
              }
              currentRoute = "create_request"
            }
          )
        }

        "profile" -> {
          ProfileScreen(
            currentUser = currentUser,
            appPreferences = appPreferences,
            onRoleSwitch = { role ->
              MaitreRepository.switchRole(role)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم التبديل إلى: ${if (role == UserRole.LAWYER) "المحامي المعتمد" else if (role == UserRole.ADMIN) "مشرف المنصة" else "المستخدم النهائي"}")
              }
            },
            onRequestVerification = { lic ->
              MaitreRepository.requestLawyerVerification(lic)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم رفع الترخيص رقم $lic للفحص والاعتماد")
              }
            },
            onNavigateToVerification = { currentRoute = "verification" },
            onNavigateToDeposit = { currentRoute = "deposit" },
            onThemeModeChange = { mode ->
              MaitreRepository.setThemeMode(mode)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم تغيير المظهر إلى: ${mode.labelAr}")
              }
            },
            onNotificationsToggle = { enabled ->
              MaitreRepository.setNotificationsEnabled(enabled)
            },
            onBiometricToggle = { enabled ->
              MaitreRepository.setBiometricAuthEnabled(enabled)
              coroutineScope.launch {
                snackbarHostState.showSnackbar(if (enabled) "تم تفعيل المصادقة بالبصمة" else "تم إلغاء المصادقة بالبصمة")
              }
            },
            onSoundAlertsToggle = { enabled ->
              MaitreRepository.setSoundAlertsEnabled(enabled)
            },
            onLogout = {
              MaitreRepository.logout()
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم تسجيل الخروج بنجاح")
              }
            },
            onBackClick = null,
            onNavigateToAdmin = { currentRoute = "admin" },
            onNavigateToWithdrawal = { currentRoute = "escrow" },
            onNavigateToRequests = { currentRoute = "home" },
            onNavigateToTracker = { currentRoute = "tracker" }
          )
        }

        "admin" -> {
          AdminDashboardScreen(
            lawyers = lawyers,
            disputes = disputes,
            escrowTransactions = escrowTransactions,
            violations = violations,
            securityRiskEvents = securityRiskEvents,
            withdrawalRequests = withdrawalRequests,
            coupons = coupons,
            requests = requests,
            platformWithdrawals = platformWithdrawals,
            supervisoryDecisions = supervisoryDecisions,
            realtimeAuditLogs = realtimeAuditLogs,
            currentPlatformFeePercentage = platformFeePercentage,
            clientPlatformFeePercentage = clientPlatformFeePercentage,
            lawyerPlatformFeePercentage = lawyerPlatformFeePercentage,
            clientRegistrations = clientRegistrations,
            onUpdateDualPlatformFees = { clientFee, lawyerFee ->
              MaitreRepository.updateDualPlatformFees(clientFee, lawyerFee)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم حفظ وتحديث نسبتي المنصة (عميل: $clientFee% - محامي: $lawyerFee%) بنجاح ✓")
              }
            },
            onUpdatePlatformFeePercentage = { newPercentage ->
              MaitreRepository.updatePlatformFeePercentage(newPercentage)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم تعديل نسبة المنصة إلى $newPercentage% بنجاح")
              }
            },
            onVerifyLawyer = { lawyerId, approved ->
              MaitreRepository.verifyLawyer(lawyerId, approved)
              coroutineScope.launch {
                snackbarHostState.showSnackbar(if (approved) "تم اعتماد قيد المحامي بنقابة المحامين" else "تم رفض وثيقة القيد")
              }
            },
            onVerifyClientRegistration = { clientId, approved, reason, notes ->
              MaitreRepository.verifyClientRegistration(clientId, approved, reason, notes)
              coroutineScope.launch {
                snackbarHostState.showSnackbar(if (approved) "تم اعتماد وتوثيق هوية الموكل بنجاح ✓" else "تم رفض طلب التوثيق")
              }
            },
            onVerifyLawyerWithDetails = { lawyerId, approved, degree, gov, scope, notes ->
              MaitreRepository.verifyLawyerWithDetails(lawyerId, approved, degree, gov, scope, notes)
              coroutineScope.launch {
                snackbarHostState.showSnackbar(if (approved) "تم اعتماد المحامي وتحديد درجة ${degree.titleAr} ونطاق اختصاص $gov بنجاح ⚖️" else "تم رفض وثائق المحامي")
              }
            },
            onResolveDispute = { disputeId, decision, note ->
              MaitreRepository.resolveDispute(disputeId, decision, note)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم إصدار ونفاذ قرار هيئة التحكيم")
              }
            },
            onResolveDisputeWithDecision = { disputeId, decision, clientPct, lawyerPct, note ->
              MaitreRepository.resolveDisputeWithDecision(disputeId, decision, clientPct, lawyerPct, note)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم إصدار حكم هيئة التحكيم وتنفيذه على الضمان المالي ✓")
              }
            },
            onApproveWithdrawal = { reqId, note ->
              MaitreRepository.approveLawyerWithdrawal(reqId, note)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم اعتماد طلب السحب وتحويل المبلغ لحساب المحامي بنجاح ✓")
              }
            },
            onRejectWithdrawal = { reqId, reason ->
              MaitreRepository.rejectLawyerWithdrawal(reqId, reason)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم رفض طلب السحب وإعادة الرصيد لمحفظة المحامي.")
              }
            },
            onWithdrawPlatformBalance = { amount, destAccount, destType, notes ->
              val result = MaitreRepository.withdrawPlatformBalance(amount, destAccount, destType, notes)
              if (result.isSuccess) {
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("تم سحب ${amount.toInt()} ج.م من رصيد المنصة عبر $destType بنجاح ✓")
                }
              } else {
                val err = result.exceptionOrNull()?.message ?: "فشل سحب الرصيد"
                coroutineScope.launch {
                  snackbarHostState.showSnackbar(err)
                }
              }
            },
            onIssueSupervisoryDecision = { reqId, decType, notes, refAmt, feeAmt ->
              val decResult = MaitreRepository.issueSupervisoryDecision(reqId, decType, notes, refAmt, feeAmt)
              val decTitle = decResult.getOrNull()?.decisionType?.titleAr ?: decType.titleAr
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم إصدار القرار الرقابي ($decTitle) ونفاذه فوراً ✓")
              }
            },
            onCreateCoupon = { code, title, desc, type, value, audience, minAmt, maxDisc, maxUsage, expiry ->
              MaitreRepository.createDiscountCoupon(code, title, desc, type, value, audience, minAmt, maxDisc, maxUsage, expiry)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم توليد ونشر كود الخصم $code بنجاح ✓")
              }
            },
            onToggleCouponStatus = { couponId ->
              MaitreRepository.toggleCouponStatus(couponId)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم تحديث حالة كود الخصم")
              }
            },
            onBackClick = { currentRoute = "home" }
          )
        }

        "create_request" -> {
          if (currentUser.role == UserRole.LAWYER || currentUser.role == UserRole.ADMIN) {
            currentRoute = "home"
            coroutineScope.launch {
              val msg = if (currentUser.role == UserRole.ADMIN) {
                "تنبيه نظامي: حساب الإدارة والرقابة مخصص للإشراف والمتابعة فقط ولا يطلب خدمات."
              } else {
                "تنبيه نظامي: لا يجوز للمحامي طلب تقديم خدمة. المحامي متلقٍ للطلبات فقط ضمن نطاق اختصاصه الجغرافي (${currentUser.assignedGovernorate})."
              }
              snackbarHostState.showSnackbar(msg)
            }
          } else {
            CreateRequestScreen(
              onBackClick = { currentRoute = "home" },
              onOpenLocationPicker = { currentRoute = "location_picker" },
              chosenLocation = pendingLocation,
              initialTemplateId = selectedTemplateId,
              onSubmitRequest = { title, cat, desc, city, budget, urgency, courtLocation, templateId ->
                val result = MaitreRepository.createRequest(title, cat, desc, city, budget, urgency, courtLocation, templateId)
                if (result.isSuccess) {
                  val created = result.getOrNull()
                  selectedRequestId = created?.id
                  pendingLocation = null
                  selectedTemplateId = null
                  coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم طرح طلبك القانوني بنجاح! ستصلك عروض المحامين قريباً")
                  }
                  currentRoute = "request_detail"
                } else {
                  val errorMsg = result.exceptionOrNull()?.message ?: "تعذر إنشاء الطلب"
                  coroutineScope.launch {
                    snackbarHostState.showSnackbar("تنبيه أمني: $errorMsg")
                  }
                }
              }
            )
          }
        }

        "request_detail" -> {
          val req = requests.find { it.id == selectedRequestId } ?: requests.firstOrNull()
          if (req != null) {
            val reqBids = bids.filter { it.requestId == req.id }
            val reqEscrow = escrowTransactions.find { it.requestId == req.id }

            RequestDetailScreen(
              request = req,
              bids = reqBids,
              escrow = reqEscrow,
              currentUser = currentUser,
              clientFeePercentage = clientPlatformFeePercentage,
              lawyerFeePercentage = lawyerPlatformFeePercentage,
              platformFeePercentage = platformFeePercentage,
              onBackClick = { currentRoute = "home" },
              onAcceptBid = { bidId, paymentMethod ->
                MaitreRepository.acceptBid(req.id, bidId, paymentMethod)
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("تم سداد رسم المنصة وفتح بيانات الاتصال ومساحة العمل بنجاح (CONTACT_UNLOCKED)")
                }
              },
              onSubmitBid = { lawyerFee, legalExpenses, days, note ->
                val bidResult = MaitreRepository.addBid(req.id, lawyerFee, legalExpenses, days, note)
                if (bidResult.isSuccess) {
                  coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم إرسال عرض السعر إلى العميل بنجاح")
                  }
                } else {
                  val errorMsg = bidResult.exceptionOrNull()?.message ?: "تعذر إرسال العرض"
                  coroutineScope.launch {
                    snackbarHostState.showSnackbar("تنبيه أمني: $errorMsg")
                  }
                }
              },
              onOpenChat = { currentRoute = "chat" },
              onReleaseEscrow = {
                MaitreRepository.releaseEscrow(req.id)
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("تم تأكيد إتمام العمل القانوني بنجاح")
                }
              },
              onOpenDispute = { currentRoute = "dispute" },
              onRateLawyer = { lawyerId ->
                selectedLawyerId = lawyerId
                currentRoute = "rate_lawyer"
              }
            )
          } else {
            currentRoute = "home"
          }
        }

        "chat" -> {
          val req = requests.find { it.id == selectedRequestId } ?: requests.firstOrNull()
          if (req != null) {
            val chatMessages by MaitreRepository.observeCaseMessages(req.id).collectAsStateWithLifecycle(
              initialValue = messages.filter { it.requestId == req.id }
            )
            val typingUsers by MaitreRepository.observeTypingUsers(req.id).collectAsStateWithLifecycle(
              initialValue = emptyList()
            )
            ChatWorkspaceScreen(
              request = req,
              messages = chatMessages,
              currentUser = currentUser,
              typingUsers = typingUsers,
              isCloudSyncActive = FirestoreChatService.isCloudConnected(),
              onTypingChanged = { isTyping ->
                MaitreRepository.setTypingStatus(req.id, isTyping)
              },
              onBackClick = { currentRoute = "request_detail" },
              onSendMessage = { text, attachment ->
                val msgResult = MaitreRepository.sendMessage(req.id, text, attachment)
                if (msgResult.isSuccess) {
                  if (currentUser.role == UserRole.CLIENT) {
                    coroutineScope.launch {
                      delay(1200)
                      MaitreRepository.simulateLawyerResponse(
                        req.id,
                        "أهلاً بك يا أخي الكريم. تم الاطلاع وجارٍ العمل على إضافة التعديل المطلوب لضمان كامل مصالحك القانونية."
                      )
                    }
                  }
                } else {
                  val errorMsg = msgResult.exceptionOrNull()?.message ?: "تعذر إرسال الرسالة"
                  coroutineScope.launch {
                    snackbarHostState.showSnackbar("تنبيه أمني: $errorMsg")
                  }
                }
              },
              onReleaseEscrow = {
                MaitreRepository.releaseEscrow(req.id)
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("تم تأكيد إتمام العمل القانوني بنجاح")
                }
              },
              onOpenDispute = { currentRoute = "dispute" }
            )
          } else {
            currentRoute = "request_detail"
          }
        }

        "lawyer_detail" -> {
          val lawyer = lawyers.find { it.id == selectedLawyerId } ?: lawyers.firstOrNull()
          if (lawyer != null) {
            val lawyerReviews = MaitreRepository.getReviewsForLawyer(lawyer.id)
            LawyerProfileDetailScreen(
              lawyer = lawyer,
              reviews = lawyerReviews,
              currentUserRole = currentUser.role,
              onBackClick = { currentRoute = "lawyers" },
              onRequestConsultation = {
                if (currentUser.role == UserRole.LAWYER) {
                  coroutineScope.launch {
                    snackbarHostState.showSnackbar("لا يجوز للمحامي طلب تقديم خدمة.")
                  }
                } else {
                  selectedTemplateId = null
                  currentRoute = "create_request"
                }
              }
            )
          } else {
            currentRoute = "lawyers"
          }
        }

        "dispute" -> {
          DisputeScreen(
            requestId = selectedRequestId,
            disputes = disputes,
            onBackClick = {
              currentRoute = if (selectedRequestId != null) "request_detail" else "home"
            },
            onSubmitDispute = { reqId, reason, details ->
              MaitreRepository.openDispute(reqId, reason, details)
              coroutineScope.launch {
                snackbarHostState.showSnackbar("تم فتح تذكرة النزاع وإحالتها للجنة التحكيم")
              }
            }
          )
        }

        "rate_lawyer" -> {
          val lawyer = lawyers.find { it.id == selectedLawyerId }
          RateLawyerScreen(
            lawyer = lawyer,
            onBackClick = { currentRoute = "home" },
            onSubmitRating = { stars, comment, tags ->
              if (selectedLawyerId != null) {
                MaitreRepository.submitReview(selectedLawyerId!!, stars, comment, tags)
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("شكراً لك! تم اعتماد تقييمك بنجاح")
                }
              }
            }
          )
        }

        "notifications" -> {
          NotificationsScreen(
            notifications = notifications,
            onBackClick = { currentRoute = "home" },
            onNotificationClick = { notif ->
              MaitreRepository.markNotificationRead(notif.id)
              if (notif.relatedRequestId != null) {
                selectedRequestId = notif.relatedRequestId
                currentRoute = "request_detail"
              }
            }
          )
        }
      }

      // نافذة إشعار الطلب الفوري المنبثقة لمدة 20 ثانية مع صوت المنبه (مثل تطبيقات النقل الذكي)
      if (currentUser.role == UserRole.LAWYER && isLawyerAvailable && incomingDispatchRequest != null) {
        val activeReq = incomingDispatchRequest!!
        LawyerDispatchPopup(
          request = activeReq,
          clientFeePercentage = clientPlatformFeePercentage,
          onAccept = { req, lawyerFee, legalExpenses ->
            val acceptRes = MaitreRepository.acceptIncomingDispatchRequest(
              requestId = req.id,
              lawyerFee = lawyerFee,
              legalExpenses = legalExpenses
            )
            selectedRequestId = req.id
            coroutineScope.launch {
              snackbarHostState.showSnackbar("تم تقديم عرضك الفوري (${(lawyerFee + legalExpenses).toInt()} ج.م + رسوم المنصة) وإرساله للموكل بنجاح!")
            }
            currentRoute = "request_detail"
          },
          onDecline = {
            MaitreRepository.dismissIncomingDispatchRequest()
            coroutineScope.launch {
              snackbarHostState.showSnackbar("تم تجاهل الطلب.")
            }
          },
          onTimeout = {
            MaitreRepository.dismissIncomingDispatchRequest()
            coroutineScope.launch {
              snackbarHostState.showSnackbar("انتهت مهلة الـ 20 ثانية - تم توجيه الطلب لمحامٍ آخر في النطاق الجغرافي.")
            }
          }
        )
      }
    }
  }
}
