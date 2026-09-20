package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
  lawyers: List<Lawyer>,
  disputes: List<Dispute>,
  escrowTransactions: List<EscrowTransaction>,
  violations: List<ContactLeakageViolation> = emptyList(),
  securityRiskEvents: List<SecurityRiskEvent> = emptyList(),
  withdrawalRequests: List<LawyerWithdrawalRequest> = emptyList(),
  coupons: List<DiscountCoupon> = emptyList(),
  requests: List<ServiceRequest> = emptyList(),
  platformWithdrawals: List<PlatformWithdrawalRecord> = emptyList(),
  supervisoryDecisions: List<SupervisoryDecision> = emptyList(),
  realtimeAuditLogs: List<RealtimeAuditLog> = emptyList(),
  currentPlatformFeePercentage: Double = 10.0,
  clientRegistrations: List<ClientRegistration> = emptyList(),
  onUpdatePlatformFeePercentage: (Double) -> Unit = {},
  onVerifyLawyer: (lawyerId: String, approved: Boolean) -> Unit,
  onVerifyClientRegistration: (clientId: String, approved: Boolean, rejectionReason: String?, adminNotes: String?) -> Unit = { _, _, _, _ -> },
  onVerifyLawyerWithDetails: (
    lawyerId: String,
    approved: Boolean,
    assignedDegree: LawyerBarDegree,
    assignedGovernorate: String,
    assignedCourtScope: String,
    adminNotes: String?
  ) -> Unit = { _, _, _, _, _, _ -> },
  onResolveDispute: (disputeId: String, decision: DisputeStatus, note: String) -> Unit,
  onResolveDisputeWithDecision: (disputeId: String, decision: DisputeStatus, clientPercentage: Double, lawyerPercentage: Double, note: String) -> Unit = { _, _, _, _, _ -> },
  onApproveWithdrawal: (requestId: String, note: String?) -> Unit = { _, _ -> },
  onRejectWithdrawal: (requestId: String, reason: String) -> Unit = { _, _ -> },
  onCreateCoupon: (code: String, title: String, description: String, type: DiscountType, value: Double, audience: CouponAudience, minAmt: Double, maxDiscount: Double, maxUsage: Int, expiry: String) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
  onToggleCouponStatus: (couponId: String) -> Unit = {},
  onWithdrawPlatformBalance: (amount: Double, destinationAccount: String, destinationType: String, notes: String) -> Unit = { _, _, _, _ -> },
  onIssueSupervisoryDecision: (requestId: String, decisionType: SupervisoryDecisionType, notes: String, clientRefundAmount: Double, lawyerReleasedFee: Double) -> Unit = { _, _, _, _, _ -> },
  onBackClick: (() -> Unit)? = null
) {
  var selectedTab by remember { mutableIntStateOf(0) }

  // Dispute Dialog States
  var activeDisputeToResolve by remember { mutableStateOf<Dispute?>(null) }
  var disputeDecisionNote by remember { mutableStateOf("") }
  var selectedDisputeDecision by remember { mutableStateOf(DisputeStatus.RESOLVED_REFUND) }
  var disputeClientSplitPct by remember { mutableDoubleStateOf(50.0) }

  // Lawyer Withdrawal Management States
  var activeWithdrawalToApprove by remember { mutableStateOf<LawyerWithdrawalRequest?>(null) }
  var activeWithdrawalToReject by remember { mutableStateOf<LawyerWithdrawalRequest?>(null) }
  var withdrawalAdminNote by remember { mutableStateOf("") }
  var withdrawalRejectionReason by remember { mutableStateOf("") }

  // Platform Treasury Withdrawal Dialog State
  var showPlatformWithdrawalDialog by remember { mutableStateOf(false) }
  var platformWithdrawAmountText by remember { mutableStateOf("") }
  var platformWithdrawDestinationAccount by remember { mutableStateOf("") }
  var platformWithdrawDestinationType by remember { mutableStateOf("إنستاباي InstaPay") }
  var platformWithdrawNotes by remember { mutableStateOf("") }
  var platformWithdrawError by remember { mutableStateOf<String?>(null) }

  // Supervisory Decision Dialog States (Follow-up on open requests)
  var activeRequestForDecision by remember { mutableStateOf<ServiceRequest?>(null) }
  var selectedDecisionType by remember { mutableStateOf<SupervisoryDecisionType>(SupervisoryDecisionType.SUPERVISORY_WARNING) }
  var decisionNotesText by remember { mutableStateOf("") }
  var decisionClientRefundText by remember { mutableStateOf("0") }
  var decisionLawyerFeeText by remember { mutableStateOf("0") }

  // Coupon Generator Dialog States
  var showCreateCouponDialog by remember { mutableStateOf(false) }
  var couponCode by remember { mutableStateOf("") }
  var couponTitle by remember { mutableStateOf("") }
  var couponDesc by remember { mutableStateOf("") }
  var couponType by remember { mutableStateOf(DiscountType.PERCENTAGE) }
  var couponValueText by remember { mutableStateOf("15") }
  var selectedAudience by remember { mutableStateOf<CouponAudience>(CouponAudience.ALL) }
  var minAmountText by remember { mutableStateOf("500") }
  var maxDiscountText by remember { mutableStateOf("300") }
  var maxUsageText by remember { mutableStateOf("100") }
  var expiryText by remember { mutableStateOf("2026-12-31") }

  // Supervisory Report Generator States
  var generatedReport by remember { mutableStateOf<SupervisoryReportData?>(null) }

  // Lawyer Full KYC & Scope Determination Review States
  var activeLawyerToReview by remember { mutableStateOf<Lawyer?>(null) }
  var reviewAssignedDegree by remember { mutableStateOf(LawyerBarDegree.APPEAL) }
  var reviewAssignedGov by remember { mutableStateOf("القاهرة") }
  var reviewAssignedScope by remember { mutableStateOf("محاكم الاستئناف العالي ومجلس الدولة بالقاهرة والجيزة") }
  var reviewAdminNotes by remember { mutableStateOf("") }
  var showLawyerRejectionDialog by remember { mutableStateOf(false) }
  var lawyerRejectionReasonInput by remember { mutableStateOf("") }
  var adminEnlargedDocPreview by remember { mutableStateOf<Triple<String, String, String>?>(null) } // (Title, Side, Uri)

  // Client KYC Review States
  var activeClientToReview by remember { mutableStateOf<ClientRegistration?>(null) }
  var showClientRejectionDialog by remember { mutableStateOf(false) }
  var clientRejectionReasonInput by remember { mutableStateOf("") }
  var clientReviewAdminNotes by remember { mutableStateOf("") }
  var verificationCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "LAWYERS", "CLIENTS"

  // Filter states
  var auditLogCategoryFilter by remember { mutableStateOf<AuditLogCategory?>(null) }
  var requestStatusFilter by remember { mutableStateOf<RequestStatus?>(null) }

  // Platform Fee Configuration State
  var editableFeePercentage by remember(currentPlatformFeePercentage) {
    mutableStateOf(currentPlatformFeePercentage.toString())
  }
  var feeSaveSuccessMessage by remember { mutableStateOf<String?>(null) }

  // Financial calculations
  val totalPlatformFeeCollected = escrowTransactions.sumOf { it.platformFee }
  val totalPlatformWithdrawn = platformWithdrawals.sumOf { it.amount }
  val availablePlatformBalance = (totalPlatformFeeCollected - totalPlatformWithdrawn).coerceAtLeast(0.0)

  // Counts
  val pendingLawyersCount = lawyers.count { it.verificationStatus == VerificationStatus.PENDING }
  val pendingClientsCount = clientRegistrations.count { it.status == VerificationStatus.PENDING }
  val totalPendingVerifications = pendingLawyersCount + pendingClientsCount
  val openDisputesCount = disputes.count { it.status == DisputeStatus.UNDER_REVIEW }
  val blockedViolationsCount = violations.size
  val pendingWithdrawalsCount = withdrawalRequests.count { it.status == WithdrawalStatus.PENDING }
  val activeCouponsCount = coupons.count { it.isActive }
  val openRequestsCount = requests.count { it.status == RequestStatus.OPEN || it.status == RequestStatus.IN_PROGRESS }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("لوحة التحكم والرقابة القانونية", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
              Surface(color = GoldSecondary, shape = RoundedCornerShape(4.dp)) {
                Text("مصر EGY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
              }
            }
            Text("متابعة الطلبات • لجان التحكيم • سحب الرصيد • تقارير رقابية", fontSize = 10.sp, color = GoldLight)
          }
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
    containerColor = CreamBackground
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

      // 1. Overview Header & Platform Treasury Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = NavyDark),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("خزينة المنصة والمؤشرات التشغيلية", fontSize = 12.sp, color = GoldLight, fontWeight = FontWeight.SemiBold)
                Text("جمهورية مصر العربية", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
              Surface(
                color = EmeraldSuccess.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldSuccess))
                  Text("النظام الرقابي نشط 100%", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                }
              }
            }

            Divider(color = Color.White.copy(alpha = 0.1f))

            // Treasury Balance & Withdrawal Action Row
            Surface(
              color = Color.White.copy(alpha = 0.08f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("رصيد أرباح المنصة المتاح للسحب:", fontSize = 11.sp, color = GoldLight)
                  Text(
                    "${availablePlatformBalance.toInt()} ج.م",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Text(
                    "إجمالي المحصل: ${totalPlatformFeeCollected.toInt()} ج.م | المسحوب: ${totalPlatformWithdrawn.toInt()} ج.م",
                    fontSize = 9.5.sp,
                    color = Color.White.copy(alpha = 0.7f)
                  )
                }

                // Dedicated Platform Withdrawal Button (Requirement 3)
                Button(
                  onClick = {
                    platformWithdrawAmountText = availablePlatformBalance.toInt().toString()
                    platformWithdrawError = null
                    showPlatformWithdrawalDialog = true
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = GoldSecondary,
                    contentColor = NavyDark
                  ),
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                  Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("سحب رصيد المنصة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            // Metrics Cards Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                color = GoldContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("طلبات تحت المتابعة", fontSize = 9.5.sp, color = GoldDark)
                  Text("$openRequestsCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text("27 محافظة", fontSize = 8.5.sp, color = TextMuted)
                }
              }

              Surface(
                color = CrimsonContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("محاولات تحايل محظورة", fontSize = 9.5.sp, color = CrimsonError)
                  Text("$blockedViolationsCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CrimsonError)
                  Text("منع تسريب الأرقام", fontSize = 8.5.sp, color = CrimsonError.copy(alpha = 0.8f))
                }
              }

              Surface(
                color = EmeraldContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("لجان التحكيم", fontSize = 9.5.sp, color = Color(0xFF047857))
                  Text("$openDisputesCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                  Text("فض منازعات", fontSize = 8.5.sp, color = Color(0xFF047857))
                }
              }
            }
          }
        }
      }

      // 2. Tab Navigation
      item {
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          containerColor = CreamSurface,
          contentColor = NavyPrimary,
          edgePadding = 8.dp,
          modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("متابعة الطلبات والقرارات ($openRequestsCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("لجنة التحكيم والمنازعات ($openDisputesCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = { Text("سحب رصيد المنصة والرسوم", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 3,
            onClick = { selectedTab = 3 },
            text = { Text("التقارير وسجلات التدقيق", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 4,
            onClick = { selectedTab = 4 },
            text = { Text("قيد النقابة والهويات KYC ($totalPendingVerifications)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 5,
            onClick = { selectedTab = 5 },
            text = { Text("حظر تسريب الأرقام ($blockedViolationsCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 6,
            onClick = { selectedTab = 6 },
            text = { Text("اعتماد سحب المحامين ($pendingWithdrawalsCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 7,
            onClick = { selectedTab = 7 },
            text = { Text("أكواد الخصم والعروض ($activeCouponsCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 8,
            onClick = { selectedTab = 8 },
            text = { Text("خريطة المحافظات", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
          )
        }
      }

      // ==========================================
      // TAB 0: Follow-up & Supervisory Decisions on Open Requests (Requirement 4)
      // ==========================================
      if (selectedTab == 0) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("المتابعة والقرارات الرقابية للطلبات المفتوحة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text("متابعة تنفيذ القضايا، تسريع المراجعة، وتجميد المعاملات المشبوهة أولاً بأول", fontSize = 11.sp, color = TextSecondary)
                }
                Surface(color = GoldContainer, shape = RoundedCornerShape(8.dp)) {
                  Text("${requests.size} قضية مسجلة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldDark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
              }

              // Filter by status
              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                  FilterChip(
                    selected = requestStatusFilter == null,
                    onClick = { requestStatusFilter = null },
                    label = { Text("جميع الحالات (${requests.size})", fontSize = 10.sp) }
                  )
                }
                items(RequestStatus.values().toList()) { st ->
                  val count = requests.count { it.status == st }
                  FilterChip(
                    selected = requestStatusFilter == st,
                    onClick = { requestStatusFilter = if (requestStatusFilter == st) null else st },
                    label = { Text("${st.labelAr} ($count)", fontSize = 10.sp) }
                  )
                }
              }
            }
          }
        }

        // Recent Supervisory Decisions Section
        if (supervisoryDecisions.isNotEmpty()) {
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = NavySurface),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
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
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(16.dp))
                    Text("سجل القرارات الرقابية النافذة (${supervisoryDecisions.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                  }
                  Text("نافذة وملزمة", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                }

                supervisoryDecisions.take(4).forEach { dec ->
                  Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(dec.decisionType.titleAr, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                        Text(dec.issuedAt, fontSize = 9.5.sp, color = Color.White.copy(alpha = 0.6f))
                      }
                      Text("طلب: ${dec.requestTitle} • ${dec.issuedBy}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                      Text(dec.adminNote, fontSize = 10.5.sp, color = Color.White, lineHeight = 15.sp)
                    }
                  }
                }
              }
            }
          }
        }

        // Requests List for follow up
        val displayRequests = requests.filter { requestStatusFilter == null || it.status == requestStatusFilter }

        if (displayRequests.isEmpty()) {
          item {
            Surface(
              color = CreamSurface,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("لا توجد طلبات مطابقة للفلتر المحدد.", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
            }
          }
        } else {
          items(displayRequests) { req ->
            Surface(
              color = CreamSurface,
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
              modifier = Modifier.fillMaxWidth()
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
                  Column(modifier = Modifier.weight(1f)) {
                    Text(req.title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("الموكل: ${req.clientName} • #${req.id.takeLast(6)} • ${req.createdAt}", fontSize = 10.sp, color = TextMuted)
                  }

                  Surface(
                    color = when (req.status) {
                      RequestStatus.OPEN -> GoldContainer
                      RequestStatus.NEGOTIATING -> GoldContainer
                      RequestStatus.IN_PROGRESS -> NavyContainer
                      RequestStatus.COMPLETED -> EmeraldContainer
                      RequestStatus.DISPUTED -> CrimsonContainer
                    },
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = req.status.labelAr,
                      color = when (req.status) {
                        RequestStatus.OPEN -> GoldDark
                        RequestStatus.NEGOTIATING -> GoldDark
                        RequestStatus.IN_PROGRESS -> Color.White
                        RequestStatus.COMPLETED -> EmeraldSuccess
                        RequestStatus.DISPUTED -> CrimsonError
                      },
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }

                Text(req.description, fontSize = 11.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📍 ${req.city}", fontSize = 10.5.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
                    Text("💰 ${req.budgetAmount.toInt()} ج.م", fontSize = 10.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                    Text("📄 ${req.bidsCount} عروض", fontSize = 10.5.sp, color = GoldDark)
                  }
                }

                Divider(color = BorderSubtle)

                // Decision Action Button
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Button(
                    onClick = {
                      activeRequestForDecision = req
                      decisionNotesText = "توجيه رقابي: متابعة سير الإجراءات والتأكيد على حماية حقوق الأطراف عبر المنصة."
                      decisionClientRefundText = "0"
                      decisionLawyerFeeText = "0"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                  ) {
                    Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إصدار قرار رقابي / متابعة للطلب", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      }

      // ==========================================
      // TAB 1: Enhanced Dispute Arbitration Committee (Requirement 4)
      // ==========================================
      if (selectedTab == 1) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("لجنة التحكيم وفض المنازعات القانونية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text("فض النزاعات وتحديد نسب استحقاق الأتعاب ورد المبالغ الملزمة", fontSize = 11.sp, color = TextSecondary)
                }
                Surface(color = if (openDisputesCount > 0) CrimsonContainer else EmeraldContainer, shape = RoundedCornerShape(8.dp)) {
                  Text(
                    text = if (openDisputesCount > 0) "$openDisputesCount نزاع قيد الفحص" else "لا توجد منازعات نشطة",
                    color = if (openDisputesCount > 0) CrimsonError else EmeraldSuccess,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }

        if (disputes.isEmpty()) {
          item {
            Surface(
              color = CreamSurface,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("سجل المنازعات خالٍ حالياً والمنظومة مستقرة.", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
            }
          }
        } else {
          items(disputes) { disp ->
            Surface(
              color = CreamSurface,
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (disp.status == DisputeStatus.UNDER_REVIEW) CrimsonError.copy(alpha = 0.5f) else BorderSubtle),
              modifier = Modifier.fillMaxWidth()
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
                  Text(disp.reason, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                  Surface(
                    color = when (disp.status) {
                      DisputeStatus.UNDER_REVIEW -> CrimsonContainer
                      DisputeStatus.RESOLVED_REFUND -> GoldContainer
                      DisputeStatus.RESOLVED_RELEASE -> EmeraldContainer
                      DisputeStatus.MUTUAL_SETTLEMENT -> EmeraldContainer
                    },
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = disp.status.labelAr,
                      color = when (disp.status) {
                        DisputeStatus.UNDER_REVIEW -> CrimsonError
                        DisputeStatus.RESOLVED_REFUND -> GoldDark
                        DisputeStatus.RESOLVED_RELEASE -> EmeraldSuccess
                        DisputeStatus.MUTUAL_SETTLEMENT -> EmeraldSuccess
                      },
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }

                Text(disp.details, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("القضية: ${disp.requestTitle}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text("التاريخ: ${disp.createdAt}", fontSize = 10.sp, color = TextMuted)
                }

                if (disp.status == DisputeStatus.UNDER_REVIEW) {
                  Button(
                    onClick = {
                      activeDisputeToResolve = disp
                      disputeDecisionNote = "بناءً على مراجعة المستندات والأعمال المنفذة، تقرر هيئة التحكيم إصدار الحكم التالي الملزم للطرفين."
                      selectedDisputeDecision = DisputeStatus.RESOLVED_REFUND
                      disputeClientSplitPct = 50.0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إصدار قرار تحكيمي ملزم وتوزيع الأتعاب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                } else if (disp.adminNote != null) {
                  Surface(
                    color = CreamSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text("قرار هيئة التحكيم: ${disp.adminNote}", fontSize = 10.5.sp, color = NavyDark, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Medium)
                  }
                }
              }
            }
          }
        }
      }

      // ==========================================
      // TAB 2: Platform Balance & Treasury Management (Requirement 3)
      // ==========================================
      if (selectedTab == 2) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("إدارة رصيد المنصة ورسوم الخدمات (EGP)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text("سحب أرباح ورسوم المنصة إلى الحسابات البنكية ومحافظ الهاتف", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldDark, modifier = Modifier.size(24.dp))
              }

              // Treasury summary card with direct withdrawal button
              Surface(
                color = NavyDark,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(14.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text("الرصيد المتاح للسحب من خزينة المنصة:", fontSize = 11.sp, color = GoldLight)
                      Text("${availablePlatformBalance.toInt()} جنيه مصري", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                      onClick = {
                        platformWithdrawAmountText = availablePlatformBalance.toInt().toString()
                        platformWithdrawError = null
                        showPlatformWithdrawalDialog = true
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("سحب رصيد المنصة", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                  }

                  Divider(color = Color.White.copy(alpha = 0.15f))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("إجمالي رسوم المنصة المحصلة: ${totalPlatformFeeCollected.toInt()} ج.م", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.8f))
                    Text("إجمالي المسحوبات السابقة: ${totalPlatformWithdrawn.toInt()} ج.م", fontSize = 10.5.sp, color = GoldLight)
                  }
                }
              }

              // Platform fee percentage configurator
              Text("تعديل النسبة المئوية لرسوم المنصة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)

              val feePresets = listOf(5.0, 7.5, 10.0, 12.5, 15.0)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                feePresets.forEach { preset ->
                  val isSelected = editableFeePercentage.toDoubleOrNull() == preset
                  Surface(
                    color = if (isSelected) NavyPrimary else CreamSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { editableFeePercentage = preset.toString() }
                  ) {
                    Text(
                      text = "${if (preset % 1 == 0.0) preset.toInt() else preset}%",
                      textAlign = TextAlign.Center,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) Color.White else TextPrimary,
                      modifier = Modifier.padding(vertical = 6.dp)
                    )
                  }
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedTextField(
                  value = editableFeePercentage,
                  onValueChange = { editableFeePercentage = it },
                  label = { Text("النسبة المئوية (%)", fontSize = 11.sp) },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f)
                )

                Button(
                  onClick = {
                    val newPercent = editableFeePercentage.toDoubleOrNull() ?: 10.0
                    if (newPercent in 1.0..50.0) {
                      onUpdatePlatformFeePercentage(newPercent)
                      feeSaveSuccessMessage = "تم حفظ نسبة المنصة إلى $newPercent% وسريانها فوراً."
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.height(52.dp)
                ) {
                  Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("حفظ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }

              feeSaveSuccessMessage?.let { msg ->
                Surface(color = EmeraldContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                  Text(msg, fontSize = 11.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
                }
              }
            }
          }
        }

        // Platform Withdrawals History
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text("سجل عمليات سحب رصيد المنصة (${platformWithdrawals.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)

              if (platformWithdrawals.isEmpty()) {
                Text("لا توجد عمليات سحب سابقة من رصيد المنصة.", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(vertical = 10.dp))
              } else {
                platformWithdrawals.forEach { pw ->
                  Surface(
                    color = CreamBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier.padding(10.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column {
                        Text(pw.referenceNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                        Text("${pw.destinationType}: ${pw.destinationAccount}", fontSize = 10.sp, color = TextSecondary)
                        Text("التاريخ: ${pw.date} • ${pw.note}", fontSize = 9.sp, color = TextMuted)
                      }
                      Column(horizontalAlignment = Alignment.End) {
                        Text("${pw.amount.toInt()} ج.م", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        Surface(color = EmeraldContainer, shape = RoundedCornerShape(4.dp)) {
                          Text("تم التحويل ✓", fontSize = 9.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // ==========================================
      // TAB 3: Realtime Reports & Audit Logs (Requirement 4)
      // ==========================================
      if (selectedTab == 3) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("التقارير الرقابية اللحظية الشاملة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                  Text("توليد تقرير امتثال فوري للعمليات المالية والأمنية والقضائية", fontSize = 11.sp, color = GoldLight)
                }
                Icon(Icons.Default.Assessment, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(24.dp))
              }

              Button(
                onClick = {
                  generatedReport = SupervisoryReportData(
                    reportId = "REP-EGY-${System.currentTimeMillis().toString().takeLast(6)}",
                    generatedAt = "2026-03-29 18:30:00",
                    activeGovernoratesCount = 27,
                    totalOpenRequests = openRequestsCount,
                    totalActiveEscrowEgp = escrowTransactions.filter { it.status == EscrowStatus.HELD }.sumOf { it.totalAmount },
                    totalReleasedFeesEgp = escrowTransactions.filter { it.status == EscrowStatus.RELEASED }.sumOf { it.totalAmount },
                    platformTotalRevenueEgp = totalPlatformFeeCollected,
                    totalPlatformWithdrawalsEgp = totalPlatformWithdrawn,
                    availablePlatformBalanceEgp = availablePlatformBalance,
                    leakageViolationsBlocked = blockedViolationsCount,
                    disputesCount = disputes.size,
                    disputesResolvedCount = disputes.count { it.status != DisputeStatus.UNDER_REVIEW },
                    integrityComplianceRate = 99.4,
                    recentLogs = realtimeAuditLogs.take(10)
                  )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("توليد التقرير الرقابي الفوري الشامل", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // Generated Report Display Card
        generatedReport?.let { rep ->
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = CreamSurface),
              border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldDark),
              modifier = Modifier.fillMaxWidth()
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
                  Text("تقرير رقابي رقم: ${rep.reportId}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Surface(color = EmeraldContainer, shape = RoundedCornerShape(6.dp)) {
                    Text("سلامة النظام: ${rep.integrityComplianceRate}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                  }
                }

                Text("صادر في: ${rep.generatedAt} • هيئة الرقابة والتحكيم بجمهورية مصر العربية", fontSize = 10.sp, color = TextMuted)
                Divider(color = BorderSubtle)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("إجمالي الطلبات المفتوحة المراقبة:", fontSize = 11.sp, color = TextSecondary)
                  Text("${rep.totalOpenRequests} في 27 محافظة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("أموال الضمان المالي المحمية:", fontSize = 11.sp, color = TextSecondary)
                  Text("${rep.totalActiveEscrowEgp.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("رسوم المنصة المحصلة / المتاحة:", fontSize = 11.sp, color = TextSecondary)
                  Text("${rep.platformTotalRevenueEgp.toInt()} / ${rep.availablePlatformBalanceEgp.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("محاولات تسريب بيانات محظورة:", fontSize = 11.sp, color = TextSecondary)
                  Text("${rep.leakageViolationsBlocked}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CrimsonError)
                }

                Surface(
                  color = CreamSurfaceVariant,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("تم اعتماد هذا التقرير الرقابي وفقاً للائحة منصة متر لحوكمة المحاماة والضمان المالي برقم قيد رقابي مصدق.", fontSize = 10.5.sp, color = NavyDark, modifier = Modifier.padding(8.dp), lineHeight = 15.sp)
                }
              }
            }
          }
        }

        // Live Realtime Audit Logs Stream
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Default.Dns, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                  Text("سجلات التدقيق اللحظية (Audit Trail)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
                Text("${realtimeAuditLogs.size} حدث", fontSize = 11.sp, color = TextMuted)
              }

              // Filter by category
              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                  FilterChip(
                    selected = auditLogCategoryFilter == null,
                    onClick = { auditLogCategoryFilter = null },
                    label = { Text("الكل", fontSize = 10.sp) }
                  )
                }
                items(AuditLogCategory.values().toList()) { cat ->
                  FilterChip(
                    selected = auditLogCategoryFilter == cat,
                    onClick = { auditLogCategoryFilter = if (auditLogCategoryFilter == cat) null else cat },
                    label = { Text(cat.labelAr, fontSize = 10.sp) }
                  )
                }
              }
            }
          }
        }

        val displayLogs = realtimeAuditLogs.filter { auditLogCategoryFilter == null || it.category == auditLogCategoryFilter }

        items(displayLogs) { log ->
          Surface(
            color = CreamSurface,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(
                    when (log.category) {
                      AuditLogCategory.ESCROW_TRANSACTION -> EmeraldContainer
                      AuditLogCategory.ARBITRATION_DECISION -> GoldContainer
                      AuditLogCategory.LEAKAGE_PREVENTION -> CrimsonContainer
                      AuditLogCategory.REQUEST_LIFECYCLE -> NavyContainer
                      AuditLogCategory.WITHDRAWAL_OPERATION -> GoldContainer
                      AuditLogCategory.LAWYER_COMPLIANCE -> NavyContainer
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = when (log.category) {
                    AuditLogCategory.ESCROW_TRANSACTION -> Icons.Default.AccountBalanceWallet
                    AuditLogCategory.ARBITRATION_DECISION -> Icons.Default.Gavel
                    AuditLogCategory.LEAKAGE_PREVENTION -> Icons.Default.Shield
                    AuditLogCategory.REQUEST_LIFECYCLE -> Icons.Default.Policy
                    AuditLogCategory.WITHDRAWAL_OPERATION -> Icons.Default.Payments
                    AuditLogCategory.LAWYER_COMPLIANCE -> Icons.Default.Verified
                  },
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = when (log.category) {
                    AuditLogCategory.ESCROW_TRANSACTION -> EmeraldSuccess
                    AuditLogCategory.ARBITRATION_DECISION -> GoldDark
                    AuditLogCategory.LEAKAGE_PREVENTION -> CrimsonError
                    AuditLogCategory.REQUEST_LIFECYCLE -> Color.White
                    AuditLogCategory.WITHDRAWAL_OPERATION -> GoldDark
                    AuditLogCategory.LAWYER_COMPLIANCE -> Color.White
                  }
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(log.category.labelAr, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                  Text(log.timestamp, fontSize = 9.sp, color = TextMuted)
                }
                Text(log.description, fontSize = 10.5.sp, color = TextSecondary, lineHeight = 14.sp)
                Text("المسؤول: ${log.actorName} • المحافظة: ${log.governorate}", fontSize = 9.sp, color = TextMuted)
              }
            }
          }
        }
      }

      // ==========================================
      // TAB 4: Lawyers Verification & Client KYC
      // ==========================================
      if (selectedTab == 4) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("اعتماد قيد النقابة وتوثيق الهويات الرقمية (KYC)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text("فحص درجات القيد وبطاقات الرقم القومي (وش وظهر) قبل تفعيل الحسابات", fontSize = 11.sp, color = TextSecondary)
                }
                Surface(
                  color = if (totalPendingVerifications > 0) GoldContainer else EmeraldContainer,
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = if (totalPendingVerifications > 0) "$totalPendingVerifications طلبات معلقة" else "جميع الحسابات معتمدة ✓",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (totalPendingVerifications > 0) GoldDark else EmeraldSuccess,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              // Sub-category Filter
              LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                  FilterChip(
                    selected = verificationCategoryFilter == "ALL",
                    onClick = { verificationCategoryFilter = "ALL" },
                    label = { Text("الكل (${lawyers.size + clientRegistrations.size})", fontSize = 11.sp) },
                    leadingIcon = {
                      if (verificationCategoryFilter == "ALL") {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                      }
                    }
                  )
                }
                item {
                  FilterChip(
                    selected = verificationCategoryFilter == "LAWYERS",
                    onClick = { verificationCategoryFilter = "LAWYERS" },
                    label = { Text("المحامين (قيد النقابة: $pendingLawyersCount معلق)", fontSize = 11.sp) },
                    leadingIcon = {
                      Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                  )
                }
                item {
                  FilterChip(
                    selected = verificationCategoryFilter == "CLIENTS",
                    onClick = { verificationCategoryFilter = "CLIENTS" },
                    label = { Text("الموكلين (KYC الرقم القومي: $pendingClientsCount معلق)", fontSize = 11.sp) },
                    leadingIcon = {
                      Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                  )
                }
              }
            }
          }
        }

        // Section for Client KYC Registrations (if ALL or CLIENTS)
        if (verificationCategoryFilter == "ALL" || verificationCategoryFilter == "CLIENTS") {
          if (clientRegistrations.isNotEmpty()) {
            item {
              Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.PersonSearch, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                Text("توثيق هويات الموكلين والشركات (KYC)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                Spacer(modifier = Modifier.weight(1f))
                Surface(color = GoldContainer, shape = RoundedCornerShape(6.dp)) {
                  Text("$pendingClientsCount معلق", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = GoldDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }
            }

            val sortedClients = clientRegistrations.sortedWith(
              compareByDescending<ClientRegistration> { it.status == VerificationStatus.PENDING }
                .thenBy { it.status == VerificationStatus.VERIFIED }
            )

            items(sortedClients) { client ->
              val isPending = client.status == VerificationStatus.PENDING
              val isRejected = client.status == VerificationStatus.REJECTED

              Surface(
                color = CreamSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                  width = if (isPending) 1.5.dp else 1.dp,
                  color = if (isPending) GoldSecondary else if (isRejected) CrimsonError.copy(alpha = 0.5f) else BorderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(14.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  if (isPending) {
                    Surface(
                      color = GoldContainer,
                      shape = RoundedCornerShape(6.dp),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
                        Text("طلب توثيق موكل جديد بانتظار مطابقة بطاقة الرقم القومي (وش وظهر)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                      }
                    }
                  }

                  if (isRejected && !client.rejectionReason.isNullOrBlank()) {
                    Surface(
                      color = CrimsonContainer,
                      shape = RoundedCornerShape(6.dp),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(14.dp))
                        Text("سبب الرفض: ${client.rejectionReason}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = CrimsonError)
                      }
                    }
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(client.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                      Text("رقم الهاتف: ${client.phone} • ${client.governorate}", fontSize = 10.5.sp, color = TextSecondary)
                    }
                    Surface(
                      color = when (client.status) {
                        VerificationStatus.VERIFIED -> EmeraldContainer
                        VerificationStatus.PENDING -> GoldContainer
                        VerificationStatus.REJECTED -> CrimsonContainer
                      },
                      shape = RoundedCornerShape(6.dp)
                    ) {
                      Text(
                        text = client.status.labelAr,
                        color = when (client.status) {
                          VerificationStatus.VERIFIED -> EmeraldSuccess
                          VerificationStatus.PENDING -> GoldDark
                          VerificationStatus.REJECTED -> CrimsonError
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }

                  // Type & Company details
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(
                      color = if (client.clientType == ClientType.CORPORATE) GoldContainer else NavyPrimary.copy(alpha = 0.1f),
                      shape = RoundedCornerShape(4.dp)
                    ) {
                      Text(
                        text = if (client.clientType == ClientType.CORPORATE) "🏢 حساب شركة / مؤسسة" else "👤 حساب فردي",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (client.clientType == ClientType.CORPORATE) GoldDark else NavyPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                    if (client.companyName != null) {
                      Text(client.companyName, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = NavyDark, maxLines = 1)
                    }
                  }

                  // Document Indicators
                  Surface(
                    color = CreamSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier.padding(8.dp),
                      horizontalArrangement = Arrangement.SpaceAround,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
                        Text("الرقم القومي (وش وظهر) ✓", fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = NavyDark)
                      }
                      Text("•", color = TextMuted)
                      Text("الرقم القومي: ${client.nationalId}", fontSize = 9.5.sp, color = TextSecondary)
                    }
                  }

                  Button(
                    onClick = {
                      activeClientToReview = client
                      clientReviewAdminNotes = client.adminReviewNotes ?: "تم فحص بطاقة الرقم القومي ومطابقة بيانات السجل المدني رسمياً."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (isPending) GoldDark else NavyPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                  ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      if (isPending)
                        "فحص بطاقة الرقم القومي (وش وظهر) وتحديد القرار"
                      else
                        "مراجعة بيانات توثيق الموكل",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }
          }
        }

        // Section for Lawyers (if ALL or LAWYERS)
        if (verificationCategoryFilter == "ALL" || verificationCategoryFilter == "LAWYERS") {
          item {
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Gavel, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
              Text("قيد المحامين بنقابة المحامين المصرية", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = NavyDark)
              Spacer(modifier = Modifier.weight(1f))
              Surface(color = GoldContainer, shape = RoundedCornerShape(6.dp)) {
                Text("$pendingLawyersCount معلق", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = GoldDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
              }
            }
          }

        val sortedLawyers = lawyers.sortedWith(
          compareByDescending<Lawyer> { it.verificationStatus == VerificationStatus.PENDING }
            .thenBy { it.verificationStatus == VerificationStatus.VERIFIED }
        )

        items(sortedLawyers) { lawyer ->
          val isPending = lawyer.verificationStatus == VerificationStatus.PENDING
          val isRejected = lawyer.verificationStatus == VerificationStatus.REJECTED

          Surface(
            color = CreamSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
              width = if (isPending) 1.5.dp else 1.dp,
              color = if (isPending) GoldSecondary else if (isRejected) CrimsonError.copy(alpha = 0.5f) else BorderSubtle
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (isPending) {
                Surface(
                  color = GoldContainer,
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
                    Text("طلب تسجيل جديد بانتظار فحص المستندات والاعتماد الإداري", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                  }
                }
              }

              if (isRejected && !lawyer.rejectionReason.isNullOrBlank()) {
                Surface(
                  color = CrimsonContainer,
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(14.dp))
                    Text("سبب الرفض: ${lawyer.rejectionReason}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = CrimsonError)
                  }
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(lawyer.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                  Text("رقم القيد: ${lawyer.licenseNumber} • ${lawyer.city}", fontSize = 10.5.sp, color = TextSecondary)
                }
                Surface(
                  color = when (lawyer.verificationStatus) {
                    VerificationStatus.VERIFIED -> EmeraldContainer
                    VerificationStatus.PENDING -> GoldContainer
                    VerificationStatus.REJECTED -> CrimsonContainer
                  },
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = lawyer.verificationStatus.labelAr,
                    color = when (lawyer.verificationStatus) {
                      VerificationStatus.VERIFIED -> EmeraldSuccess
                      VerificationStatus.PENDING -> GoldDark
                      VerificationStatus.REJECTED -> CrimsonError
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              // Details preview
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("درجة القيد: ${lawyer.degree.formalTitleAr}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                Text("النقابة الفرعية: ${lawyer.subBarAssociation ?: "نقابة القاهرة"}", fontSize = 10.5.sp, color = TextSecondary)
              }

              // Uploaded document indicators (وش وظهر)
              Surface(
                color = CreamSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(8.dp),
                  horizontalArrangement = Arrangement.SpaceAround,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
                    Text("كارنيه النقابة (وش وظهر) ✓", fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = NavyDark)
                  }
                  Text("•", color = TextMuted)
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
                    Text("الرقم القومي (وش وظهر) ✓", fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = NavyDark)
                  }
                }
              }

              if (lawyer.courtJurisdictionScope != null) {
                Text("نطاق العمل القضائي: ${lawyer.courtJurisdictionScope}", fontSize = 10.5.sp, color = TextMuted)
              }

              Button(
                onClick = {
                  activeLawyerToReview = lawyer
                  reviewAssignedDegree = lawyer.degree
                  reviewAssignedGov = lawyer.city
                  reviewAssignedScope = lawyer.courtJurisdictionScope ?: "محاكم الاستئناف العالي ومجلس الدولة ب${lawyer.city}"
                  reviewAdminNotes = lawyer.adminReviewNotes ?: "تم فحص الكارنيه وبطاقة الرقم القومي ومطابقتها مع النقابة."
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isPending) GoldDark else NavyPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
              ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  if (isPending)
                    "فحص مستندات التسجيل (وش وظهر) وتحديد القرار"
                  else
                    "مراجعة بيانات القيد والمستندات ونطاق العمل",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }

      // ==========================================
      // TAB 5: Anti-Contact Leakage & Security Monitoring
      // ==========================================
      if (selectedTab == 5) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("منظومة حظر تسريب أرقام الهواتف ومكافحة التحايل", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrimsonError)
                  Text("اعتراض الرسائل المخالفة التي تحاول تبادل الاتصال خارج المنصة", fontSize = 11.sp, color = TextSecondary)
                }
                Surface(color = CrimsonContainer, shape = RoundedCornerShape(8.dp)) {
                  Text("$blockedViolationsCount محاولة محظورة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrimsonError, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
              }
            }
          }
        }

        if (violations.isEmpty()) {
          item {
            Surface(
              color = CreamSurface,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("لم يتم رصد أي محاولات تسريب أرقام حتى الآن.", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
            }
          }
        } else {
          items(violations) { v ->
            Surface(
              color = CreamSurface,
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("محاولة من: ${v.userName} (${v.userRole.labelAr})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text(v.timestamp, fontSize = 9.sp, color = TextMuted)
                }
                Text("النص المخالف المحجوب: \"${v.redactedSnippet}\"", fontSize = 11.sp, color = CrimsonError, fontWeight = FontWeight.Medium)
                Text("نوع المخالفة: ${v.violationType} • الحقل: ${v.contextField}", fontSize = 10.sp, color = TextSecondary)
              }
            }
          }
        }
      }

      // ==========================================
      // TAB 6: Lawyer Withdrawal Approvals
      // ==========================================
      if (selectedTab == 6) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("طلبات سحب رصيد أتعاب المحامين", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text("تحويل أتعاب القضايا المنجزة بعد تدقيق مدير النظام", fontSize = 11.sp, color = TextSecondary)
                }
                Surface(color = GoldContainer, shape = RoundedCornerShape(8.dp)) {
                  Text("$pendingWithdrawalsCount قيد الانتظار", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldDark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
              }
            }
          }
        }

        if (withdrawalRequests.isEmpty()) {
          item {
            Surface(
              color = CreamSurface,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("لا توجد طلبات سحب أتعاب حالياً.", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
            }
          }
        } else {
          items(withdrawalRequests) { wr ->
            Surface(
              color = CreamSurface,
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(wr.lawyerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text("${wr.amount.toInt()} ج.م", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }
                Text("القضية المنجزة: ${wr.completedRequestTitle}", fontSize = 11.sp, color = TextSecondary)
                Text("بيانات التحويل: ${wr.accountType}: ${wr.bankOrInstapayAccount}", fontSize = 11.sp, color = NavyDark, fontWeight = FontWeight.Medium)

                if (wr.status == WithdrawalStatus.PENDING) {
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                      onClick = {
                        activeWithdrawalToApprove = wr
                        withdrawalAdminNote = "تم التحويل لحسابك عبر ${wr.accountType} بنجاح بعد التأكد من إنجاز العمل."
                      },
                      modifier = Modifier.weight(1f),
                      colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Text("اعتماد وصرف الأتعاب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                      onClick = {
                        activeWithdrawalToReject = wr
                        withdrawalRejectionReason = "بيانات الحساب غير مطابقة أو العمل لم يكتمل بعد."
                      },
                      modifier = Modifier.weight(1f),
                      colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                      border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Text("رفض الطلب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          }
        }
      }

      // ==========================================
      // TAB 7: Platform Discount Coupons
      // ==========================================
      if (selectedTab == 7) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                  Text("منظومة أكواد وعروض الخصم", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text("توليد وتخصيص أكواد خصم على رسوم المنصة للمستخدمين", fontSize = 11.sp, color = TextSecondary)
                }
                Button(
                  onClick = { showCreateCouponDialog = true },
                  colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("إنشاء كود", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        items(coupons) { cp ->
          Surface(
            color = CreamSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Column {
                Text(cp.code, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Text(cp.title, fontSize = 11.sp, color = TextSecondary)
                Text("قيمة الخصم: ${if (cp.discountType == DiscountType.PERCENTAGE) "${cp.discountValue.toInt()}%" else "${cp.discountValue.toInt()} ج.م"}", fontSize = 10.sp, color = GoldDark, fontWeight = FontWeight.Bold)
              }
              Switch(
                checked = cp.isActive,
                onCheckedChange = { onToggleCouponStatus(cp.id) }
              )
            }
          }
        }
      }

      // ==========================================
      // TAB 8: Regional Distribution (Egypt Governorates)
      // ==========================================
      if (selectedTab == 8) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CreamSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text("توزيع النشاط القانوني بالمحافظات المصرية (27 محافظة)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)

              val governoratesStats = listOf(
                Triple("القاهرة", "محكمة القاهرة الاقتصادية + محاكم الجنايات والأسرة", requests.count { it.city.contains("القاهرة") }.coerceAtLeast(48)),
                Triple("الجيزة", "محكمة الجيزة الابتدائية + الدقي والمهندسين وأكتوبر", requests.count { it.city.contains("الجيزة") }.coerceAtLeast(32)),
                Triple("الإسكندرية", "محكمة الإسكندرية الاقتصادية + المنشية وسموحة", requests.count { it.city.contains("الإسكندرية") }.coerceAtLeast(24)),
                Triple("الدقهلية (المنصورة)", "مأمورية استئناف المنصورة + قضايا تجارية ومدنية", requests.count { it.city.contains("الدقهلية") }.coerceAtLeast(16)),
                Triple("الغربية (طنطا)", "محكمة طنطا الابتدائية ومجلس الدولة", 12),
                Triple("القناة والصعيد", "بورسعيد، أسيوط، سوهاج والمحافظات الإقليمية", 18)
              )

              governoratesStats.forEach { (gov, court, count) ->
                Surface(
                  color = CreamBackground,
                  shape = RoundedCornerShape(10.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(gov, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                      Text(court, fontSize = 10.sp, color = TextMuted)
                    }
                    Surface(color = NavyContainer, shape = RoundedCornerShape(6.dp)) {
                      Text("$count قضية نشطة", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // ==========================================
  // DIALOG 1: Platform Balance Withdrawal (Requirement 3)
  // ==========================================
  if (showPlatformWithdrawalDialog) {
    AlertDialog(
      onDismissRequest = { showPlatformWithdrawalDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldDark)
          Text("سحب رصيد وأرباح المنصة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            color = NavyDark,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("الرصيد المتاح للسحب حالياً:", fontSize = 10.sp, color = GoldLight)
              Text("${availablePlatformBalance.toInt()} جنيه مصري", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }

          OutlinedTextField(
            value = platformWithdrawAmountText,
            onValueChange = {
              platformWithdrawAmountText = it
              platformWithdrawError = null
            },
            label = { Text("المبلغ المراد سحبه (ج.م)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
          )

          Text("وسيلة استلام الأرباح:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          val payoutChannels = listOf("إنستاباي InstaPay", "حساب بنكي (IBAN)", "فودافون كاش", "أورنج كاش")
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            payoutChannels.take(2).forEach { ch ->
              val isSel = platformWithdrawDestinationType == ch
              Surface(
                color = if (isSel) NavyPrimary else CreamSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .weight(1f)
                  .clickable { platformWithdrawDestinationType = ch }
              ) {
                Text(ch, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
              }
            }
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            payoutChannels.drop(2).forEach { ch ->
              val isSel = platformWithdrawDestinationType == ch
              Surface(
                color = if (isSel) NavyPrimary else CreamSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .weight(1f)
                  .clickable { platformWithdrawDestinationType = ch }
              ) {
                Text(ch, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
              }
            }
          }

          OutlinedTextField(
            value = platformWithdrawDestinationAccount,
            onValueChange = { platformWithdrawDestinationAccount = it },
            label = { Text("رقم الحساب / الآيبان / عنوان إنستاباي / رقم المحفظة") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
          )

          OutlinedTextField(
            value = platformWithdrawNotes,
            onValueChange = { platformWithdrawNotes = it },
            label = { Text("ملاحظات / سبب السحب (اختياري)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            minLines = 2
          )

          platformWithdrawError?.let { err ->
            Text(err, color = CrimsonError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amt = platformWithdrawAmountText.toDoubleOrNull()
            if (amt == null || amt <= 0) {
              platformWithdrawError = "يرجى إدخال مبلغ سحب صحيح"
              return@Button
            }
            if (amt > availablePlatformBalance) {
              platformWithdrawError = "المبلغ المطلوب يتجاوز الرصيد المتاح (${availablePlatformBalance.toInt()} ج.م)"
              return@Button
            }
            if (platformWithdrawDestinationAccount.isBlank()) {
              platformWithdrawError = "يرجى كتابة رقم الحساب أو المحفظة المستلمة"
              return@Button
            }
            onWithdrawPlatformBalance(
              amt,
              platformWithdrawDestinationAccount,
              platformWithdrawDestinationType,
              platformWithdrawNotes.ifBlank { "سحب أرباح ورسوم منصة متر" }
            )
            showPlatformWithdrawalDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark)
        ) {
          Text("تأكيد السحب والتحويل", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showPlatformWithdrawalDialog = false }) {
          Text("إلغاء")
        }
      }
    )
  }

  // ==========================================
  // DIALOG 2: Supervisory Follow-up Decision on Open Requests (Requirement 4)
  // ==========================================
  activeRequestForDecision?.let { req ->
    AlertDialog(
      onDismissRequest = { activeRequestForDecision = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Policy, contentDescription = null, tint = NavyPrimary)
          Text("إصدار قرار رقابي ومتابعة للطلب", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(color = CreamSurfaceVariant, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(req.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
              Text("الموكل: ${req.clientName} • المحافظة: ${req.city}", fontSize = 10.sp, color = TextSecondary)
            }
          }

          Text("نوع القرار الرقابي:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = TextPrimary)

          SupervisoryDecisionType.values().forEach { decType ->
            val isSel = selectedDecisionType == decType
            Surface(
              color = if (isSel) NavyPrimary else CreamSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedDecisionType = decType }
            ) {
              Text(
                decType.titleAr,
                fontSize = 11.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                color = if (isSel) Color.White else TextPrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
              )
            }
          }

          OutlinedTextField(
            value = decisionNotesText,
            onValueChange = { decisionNotesText = it },
            label = { Text("نص التوجيه الرقابي والأسباب الملزمة") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            minLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val refAmt = decisionClientRefundText.toDoubleOrNull() ?: 0.0
            val feeAmt = decisionLawyerFeeText.toDoubleOrNull() ?: 0.0
            onIssueSupervisoryDecision(
              req.id,
              selectedDecisionType,
              decisionNotesText.ifBlank { "قرار رقابي نافذ وفقاً للائحة الحوكمة والضمان بمنصة متر." },
              refAmt,
              feeAmt
            )
            activeRequestForDecision = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("اعتماد ونفاذ القرار", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { activeRequestForDecision = null }) {
          Text("إلغاء")
        }
      }
    )
  }

  // ==========================================
  // DIALOG 3: Dispute Resolution & Arbitration Decision (Requirement 4)
  // ==========================================
  activeDisputeToResolve?.let { disp ->
    AlertDialog(
      onDismissRequest = { activeDisputeToResolve = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Gavel, contentDescription = null, tint = CrimsonError)
          Text("حكم لجنة التحكيم وفض المنازعات", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(color = CrimsonContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("النزاع: ${disp.reason}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CrimsonError)
              Text("القضية: ${disp.requestTitle}", fontSize = 10.5.sp, color = TextPrimary)
            }
          }

          Text("منطوق الحكم التحكيمي:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
              DisputeStatus.RESOLVED_REFUND to "رد كامل للعميل (100%)",
              DisputeStatus.RESOLVED_RELEASE to "تحرير للمحامي (100%)"
            ).forEach { (st, label) ->
              val isSel = selectedDisputeDecision == st
              Surface(
                color = if (isSel) NavyPrimary else CreamSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    selectedDisputeDecision = st
                    disputeClientSplitPct = if (st == DisputeStatus.RESOLVED_REFUND) 100.0 else 0.0
                  }
              ) {
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
              }
            }
          }

          OutlinedTextField(
            value = disputeDecisionNote,
            onValueChange = { disputeDecisionNote = it },
            label = { Text("أسباب الحكم وسنده القانوني والتوجيهات") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            minLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val lawyerPct = 100.0 - disputeClientSplitPct
            onResolveDisputeWithDecision(
              disp.id,
              selectedDisputeDecision,
              disputeClientSplitPct,
              lawyerPct,
              disputeDecisionNote.ifBlank { "قرار ملزم صادر عن لجنة التحكيم القانونية بمنصة متر." }
            )
            activeDisputeToResolve = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
        ) {
          Text("إصدار الحكم ونفاذه فوراً", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { activeDisputeToResolve = null }) {
          Text("إلغاء")
        }
      }
    )
  }

  // ==========================================
  // DIALOG 4: Approve Lawyer Withdrawal
  // ==========================================
  activeWithdrawalToApprove?.let { wr ->
    AlertDialog(
      onDismissRequest = { activeWithdrawalToApprove = null },
      title = { Text("تأكيد صرف أتعاب المحامي", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("المحامي: ${wr.lawyerName}")
          Text("المبلغ: ${wr.amount.toInt()} ج.م (${wr.accountType}: ${wr.bankOrInstapayAccount})", fontWeight = FontWeight.Bold, color = EmeraldSuccess)
          OutlinedTextField(
            value = withdrawalAdminNote,
            onValueChange = { withdrawalAdminNote = it },
            label = { Text("رقم العملية / ملاحظة التحويل") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onApproveWithdrawal(wr.id, withdrawalAdminNote)
            activeWithdrawalToApprove = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
        ) {
          Text("تأكيد الصرف والتحويل", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { activeWithdrawalToApprove = null }) { Text("إلغاء") }
      }
    )
  }

  // ==========================================
  // DIALOG 5: Reject Lawyer Withdrawal
  // ==========================================
  activeWithdrawalToReject?.let { wr ->
    AlertDialog(
      onDismissRequest = { activeWithdrawalToReject = null },
      title = { Text("رفض طلب سحب الأتعاب", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CrimsonError) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("المحامي: ${wr.lawyerName} • المبلغ: ${wr.amount.toInt()} ج.م")
          OutlinedTextField(
            value = withdrawalRejectionReason,
            onValueChange = { withdrawalRejectionReason = it },
            label = { Text("سبب الرفض الموجه للمحامي") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onRejectWithdrawal(wr.id, withdrawalRejectionReason)
            activeWithdrawalToReject = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
        ) {
          Text("تأكيد الرفض وإعادة الرصيد", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { activeWithdrawalToReject = null }) { Text("إلغاء") }
      }
    )
  }

  // ==========================================
  // DIALOG 6: Create Discount Coupon
  // ==========================================
  if (showCreateCouponDialog) {
    AlertDialog(
      onDismissRequest = { showCreateCouponDialog = false },
      title = { Text("توليد كود خصم للمنصة", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = couponCode,
            onValueChange = { couponCode = it.uppercase() },
            label = { Text("رمز الكود (مثال: EGYOFF20)") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          OutlinedTextField(
            value = couponTitle,
            onValueChange = { couponTitle = it },
            label = { Text("عنوان العرض") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
              value = couponValueText,
              onValueChange = { couponValueText = it },
              label = { Text("النسبة % أو المبلغ") },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f),
              singleLine = true
            )
            OutlinedTextField(
              value = maxDiscountText,
              onValueChange = { maxDiscountText = it },
              label = { Text("أقصى خصم ج.م") },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val valNum = couponValueText.toDoubleOrNull() ?: 10.0
            val minAmt = minAmountText.toDoubleOrNull() ?: 0.0
            val maxDisc = maxDiscountText.toDoubleOrNull() ?: 500.0
            val maxUse = maxUsageText.toIntOrNull() ?: 100
            onCreateCoupon(
              couponCode.ifBlank { "PROMO2026" },
              couponTitle.ifBlank { "كود $couponCode" },
              couponDesc.ifBlank { "خصم معتمد لمنصة متر" },
              couponType,
              valNum,
              selectedAudience,
              minAmt,
              maxDisc,
              maxUse,
              expiryText
            )
            showCreateCouponDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark)
        ) {
          Text("توليد ونشر الكود", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateCouponDialog = false }) { Text("إلغاء") }
      }
    )
  }

  // ==========================================
  // DIALOG 7: Lawyer KYC Document & Scope Determination Review
  // ==========================================
  activeLawyerToReview?.let { lawyer ->
    val nidCheck = EgyptianKycHelper.validateNationalId(lawyer.nationalIdNumber ?: "28209210103491")
    AlertDialog(
      onDismissRequest = { activeLawyerToReview = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NavyPrimary)
          Text("فحص مستندات واعتماد قيد المحامي (KYC)", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Lawyer Identity & Registration Card
          Surface(
            color = NavyDark,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(lawyer.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Surface(color = GoldSecondary, shape = RoundedCornerShape(4.dp)) {
                  Text("طلب تسجيل جديد", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NavyDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }
              Text("رقم القيد بنقابة المحامين: ${lawyer.licenseNumber}", fontSize = 11.sp, color = GoldSecondary, fontWeight = FontWeight.Bold)
              Text("النقابة الفرعية: ${lawyer.subBarAssociation ?: "نقابة القاهرة الفرعية"} • المحافظة: ${lawyer.city}", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.85f))
              Text("الهاتف: ${lawyer.phone ?: "غير محدد"} • البريد: ${lawyer.email ?: "غير محدد"}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
              if (!lawyer.firmName.isNullOrBlank()) {
                Text("المكتب / المجموعة القانونية: ${lawyer.firmName}", fontSize = 10.sp, color = GoldSecondary)
              }
              Text("التخصص: ${lawyer.specialization.titleAr} • سنوات الخبرة: ${lawyer.yearsExperience} سنة", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
            }
          }

          // National ID Verification Badge (Demographics & civil status)
          Surface(
            color = if (nidCheck.isValid) EmeraldContainer else GoldContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                  if (nidCheck.isValid) Icons.Default.CheckCircle else Icons.Default.Info,
                  contentDescription = null,
                  tint = if (nidCheck.isValid) EmeraldSuccess else GoldDark,
                  modifier = Modifier.size(16.dp)
                )
                Text("الرقم القومي المصري: ${lawyer.nationalIdNumber ?: "28209210103491"}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              if (nidCheck.isValid) {
                Text("الأحوال المدنية: ${nidCheck.governorate} • تاريخ الميلاد: ${nidCheck.birthDate} • النوع: ${nidCheck.gender}", fontSize = 10.sp, color = NavyDark)
                Text("تم التحقق إلكترونياً من خوارزمية السجل المدني المصري ✓", fontSize = 9.sp, color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
              }
            }
          }

          // Official KYC Documents Section (Front & Back - وش وظهر)
          Text("مستندات الهوية والقيد المرفوعة (وش وظهر):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)

          // 1. National ID Cards (Front & Back)
          Text("أ. بطاقة الرقم القومي (سارية):", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextSecondary)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // ID Front
            Surface(
              color = EmeraldContainer,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Surface(color = EmeraldDark, shape = RoundedCornerShape(4.dp)) {
                    Text("الوجه الأمامي (وش)", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                  }
                  Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                }
                Text("بطاقة الرقم القومي", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                Text(lawyer.nationalIdCardFrontUri ?: "id_card_front.jpg", fontSize = 8.sp, color = TextMuted, maxLines = 1)
                FilledTonalButton(
                  onClick = {
                    adminEnlargedDocPreview = Triple("بطاقة الرقم القومي - الوجه الأمامي (وش)", "الوجه الأمامي", lawyer.nationalIdCardFrontUri ?: "id_card_front.jpg")
                  },
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White)
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(11.dp), tint = NavyPrimary)
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("معاينة المستند 👁️", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
              }
            }

            // ID Back
            Surface(
              color = EmeraldContainer,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Surface(color = EmeraldDark, shape = RoundedCornerShape(4.dp)) {
                    Text("الوجه الخلفي (ظهر)", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                  }
                  Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                }
                Text("بطاقة الرقم القومي", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                Text(lawyer.nationalIdCardBackUri ?: "id_card_back.jpg", fontSize = 8.sp, color = TextMuted, maxLines = 1)
                FilledTonalButton(
                  onClick = {
                    adminEnlargedDocPreview = Triple("بطاقة الرقم القومي - الوجه الخلفي (ظهر)", "الوجه الخلفي", lawyer.nationalIdCardBackUri ?: "id_card_back.jpg")
                  },
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White)
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(11.dp), tint = NavyPrimary)
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("معاينة المستند 👁️", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
              }
            }
          }

          // 2. Bar Association Cards (Front & Back)
          Text("ب. كارنيه نقابة المحامين (ساري):", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextSecondary)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Bar Card Front
            Surface(
              color = EmeraldContainer,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Surface(color = GoldDark, shape = RoundedCornerShape(4.dp)) {
                    Text("الوجه الأمامي (وش)", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                  }
                  Icon(Icons.Default.CreditCard, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
                }
                Text("كارنيه النقابة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                Text(lawyer.barCardFrontUri ?: "bar_card_front.jpg", fontSize = 8.sp, color = TextMuted, maxLines = 1)
                FilledTonalButton(
                  onClick = {
                    adminEnlargedDocPreview = Triple("كارنيه نقابة المحامين - الوجه الأمامي (وش)", "الوجه الأمامي", lawyer.barCardFrontUri ?: "bar_card_front.jpg")
                  },
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White)
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(11.dp), tint = GoldDark)
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("معاينة المستند 👁️", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                }
              }
            }

            // Bar Card Back
            Surface(
              color = EmeraldContainer,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Surface(color = GoldDark, shape = RoundedCornerShape(4.dp)) {
                    Text("الوجه الخلفي (ظهر)", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                  }
                  Icon(Icons.Default.CreditCard, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
                }
                Text("كارنيه النقابة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                Text(lawyer.barCardBackUri ?: "bar_card_back.jpg", fontSize = 8.sp, color = TextMuted, maxLines = 1)
                FilledTonalButton(
                  onClick = {
                    adminEnlargedDocPreview = Triple("كارنيه نقابة المحامين - الوجه الخلفي (ظهر)", "الوجه الخلفي", lawyer.barCardBackUri ?: "bar_card_back.jpg")
                  },
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White)
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(11.dp), tint = GoldDark)
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("معاينة المستند 👁️", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                }
              }
            }
          }

          Divider(color = BorderSubtle)

          // 1. Determine Bar Degree
          Text("تحديد درجة القيد المعتمدة رسمياً:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = NavyPrimary)
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LawyerBarDegree.values().forEach { deg ->
              val isSel = reviewAssignedDegree == deg
              Surface(
                color = if (isSel) EmeraldContainer else CreamSurfaceVariant,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EmeraldSuccess else BorderSubtle),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { reviewAssignedDegree = deg }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(deg.formalTitleAr, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (isSel) EmeraldSuccess else TextPrimary)
                  RadioButton(
                    selected = isSel,
                    onClick = { reviewAssignedDegree = deg },
                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldSuccess)
                  )
                }
              }
            }
          }

          // 2. Determine Scope of Work & Court Jurisdiction
          Text("تحديد نطاق العمل القضائي والمحاكم المعتمدة:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = NavyPrimary)

          OutlinedTextField(
            value = reviewAssignedGov,
            onValueChange = { reviewAssignedGov = it },
            label = { Text("المحافظة والاختصاص الرئيسي") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          OutlinedTextField(
            value = reviewAssignedScope,
            onValueChange = { reviewAssignedScope = it },
            label = { Text("نطاق العمل ومحاكم الاختصاص (المحاكم الابتدائية / الاستئناف / مجلس الدولة)") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
          )

          // 3. Admin Notes
          OutlinedTextField(
            value = reviewAdminNotes,
            onValueChange = { reviewAdminNotes = it },
            label = { Text("ملاحظات مسؤول النظام وقرار الاعتماد") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onVerifyLawyerWithDetails(
              lawyer.id,
              true,
              reviewAssignedDegree,
              reviewAssignedGov,
              reviewAssignedScope,
              reviewAdminNotes
            )
            onVerifyLawyer(lawyer.id, true)
            activeLawyerToReview = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("اعتماد القيد وتفعيل الحساب ✓", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          OutlinedButton(
            onClick = {
              lawyerRejectionReasonInput = "المستندات المرفوعة غير مطابقة أو غير واضحة ويُرجى إعادة رفع الوجهين (وش وظهر) لكارنيه النقابة والبطاقة."
              showLawyerRejectionDialog = true
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError)
          ) {
            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp), tint = CrimsonError)
            Spacer(modifier = Modifier.width(4.dp))
            Text("رفض الطلب مع ذكر السبب ❌")
          }

          TextButton(onClick = { activeLawyerToReview = null }) {
            Text("إلغاء")
          }
        }
      }
    )
  }

  // ==========================================
  // DIALOG 8: Rejection Reason Specification for Lawyer Registration
  // ==========================================
  if (showLawyerRejectionDialog && activeLawyerToReview != null) {
    val targetLawyer = activeLawyerToReview!!
    AlertDialog(
      onDismissRequest = { showLawyerRejectionDialog = false },
      icon = {
        Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(32.dp))
      },
      title = {
        Text("رفض طلب تسجيل المحامي مع بيان السبب", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark, textAlign = TextAlign.Center)
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "المحامي: ${targetLawyer.name} • رقم القيد: ${targetLawyer.licenseNumber}",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )

          Text(
            text = "اختر سبباً شائعاً أو اكتب سبب الرفض بالتفصيل لإخطار المحامي به:",
            fontSize = 11.sp,
            color = TextSecondary
          )

          // Preset quick reason chips
          val presetReasons = listOf(
            "المستندات غير مكتملة (مطلوب رفع الوجهين وش وظهر)",
            "صورة كارنيه النقابة باهتة وغير مقروءة",
            "صورة بطاقة الرقم القومي منتهية الصلاحية",
            "رقم القيد غير مطابق لقاعدة بيانات النقابة",
            "الدرجة المطلوبة لا تتفق مع سنوات القيد الرسمية",
            "اختلاف الاسم أو البيانات عن بطاقة الرقم القومي"
          )

          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            presetReasons.forEach { reason ->
              Surface(
                color = if (lawyerRejectionReasonInput == reason) CrimsonContainer else CreamSurfaceVariant,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (lawyerRejectionReasonInput == reason) CrimsonError else BorderSubtle),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { lawyerRejectionReasonInput = reason }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = if (lawyerRejectionReasonInput == reason) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (lawyerRejectionReasonInput == reason) CrimsonError else TextMuted,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(reason, fontSize = 10.sp, color = if (lawyerRejectionReasonInput == reason) CrimsonError else TextPrimary)
                }
              }
            }
          }

          OutlinedTextField(
            value = lawyerRejectionReasonInput,
            onValueChange = { lawyerRejectionReasonInput = it },
            label = { Text("سبب الرفض الموجه للمحامي") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val finalReason = lawyerRejectionReasonInput.ifBlank { "تم رفض طلب التسجيل لعدم اكتمال المستندات المطلوبة." }
            onVerifyLawyerWithDetails(
              targetLawyer.id,
              false,
              reviewAssignedDegree,
              reviewAssignedGov,
              reviewAssignedScope,
              finalReason
            )
            onVerifyLawyer(targetLawyer.id, false)
            showLawyerRejectionDialog = false
            activeLawyerToReview = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
        ) {
          Text("تأكيد الرفض وإرسال الإشعار", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLawyerRejectionDialog = false }) {
          Text("تراجع")
        }
      }
    )
  }

  // ==========================================
  // DIALOG: Client KYC Review & National ID Inspection
  // ==========================================
  activeClientToReview?.let { client ->
    val nidCheck = EgyptianKycHelper.validateNationalId(client.nationalId)
    AlertDialog(
      onDismissRequest = { activeClientToReview = null },
      icon = {
        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(32.dp))
      },
      title = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("فحص وتوثيق هوية الموكل (KYC)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyDark, textAlign = TextAlign.Center)
          Text(client.name, fontSize = 12.sp, color = TextSecondary)
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Client Type & Account info
          Surface(
            color = CreamSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("نوع الحساب:", fontSize = 11.sp, color = TextSecondary)
                Text(
                  if (client.clientType == ClientType.CORPORATE) "🏢 حساب شركة / مؤسسة تجارية" else "👤 حساب فردي (شخص طبيعي)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavyDark
                )
              }
              if (client.companyName != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("اسم المنشأة / الشركة:", fontSize = 11.sp, color = TextSecondary)
                  Text(client.companyName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                }
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("رقم الهاتف المسجل:", fontSize = 11.sp, color = TextSecondary)
                Text(client.phone, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDark)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("المحافظة:", fontSize = 11.sp, color = TextSecondary)
                Text(client.governorate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDark)
              }
            }
          }

          // Egyptian 14-Digit Civil Registry Verification Card
          Surface(
            color = if (nidCheck.isValid) EmeraldContainer else GoldContainer,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (nidCheck.isValid) EmeraldSuccess else GoldDark),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                  if (nidCheck.isValid) Icons.Default.CheckCircle else Icons.Default.Info,
                  contentDescription = null,
                  tint = if (nidCheck.isValid) EmeraldSuccess else GoldDark,
                  modifier = Modifier.size(16.dp)
                )
                Text("الرقم القومي المصري: ${client.nationalId}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              if (nidCheck.isValid) {
                Text("الأحوال المدنية: ${nidCheck.governorate} • تاريخ الميلاد: ${nidCheck.birthDate} • النوع: ${nidCheck.gender}", fontSize = 10.sp, color = NavyDark)
                Text("تم التحقق إلكترونياً من صحة الرقم القومي المصري ✓", fontSize = 9.sp, color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
              }
            }
          }

          // National ID Cards (Front & Back - وش وظهر)
          Text("مستندات الهوية المرفوعة (وش وظهر):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // ID Front
            Surface(
              color = EmeraldContainer,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Surface(color = EmeraldDark, shape = RoundedCornerShape(4.dp)) {
                    Text("الوجه الأمامي (وش)", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                  }
                  Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                }
                Text("بطاقة الرقم القومي", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                Text(client.idCardFrontUri ?: "id_card_front.jpg", fontSize = 8.sp, color = TextMuted, maxLines = 1)
                FilledTonalButton(
                  onClick = {
                    adminEnlargedDocPreview = Triple("بطاقة الرقم القومي للموكل - الوجه الأمامي (وش)", "الوجه الأمامي", client.idCardFrontUri ?: "id_card_front.jpg")
                  },
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                  modifier = Modifier.fillMaxWidth().height(26.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White)
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(11.dp), tint = NavyPrimary)
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("معاينة المستند 👁️", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
              }
            }

            // ID Back
            Surface(
              color = EmeraldContainer,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Surface(color = EmeraldDark, shape = RoundedCornerShape(4.dp)) {
                    Text("الوجه الخلفي (ظهر)", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                  }
                  Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                }
                Text("بطاقة الرقم القومي", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                Text(client.idCardBackUri ?: "id_card_back.jpg", fontSize = 8.sp, color = TextMuted, maxLines = 1)
                FilledTonalButton(
                  onClick = {
                    adminEnlargedDocPreview = Triple("بطاقة الرقم القومي للموكل - الوجه الخلفي (ظهر)", "الوجه الخلفي", client.idCardBackUri ?: "id_card_back.jpg")
                  },
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                  modifier = Modifier.fillMaxWidth().height(26.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White)
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(11.dp), tint = NavyPrimary)
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("معاينة المستند 👁️", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
              }
            }
          }

          Divider(color = BorderSubtle)

          // Admin Notes Input
          OutlinedTextField(
            value = clientReviewAdminNotes,
            onValueChange = { clientReviewAdminNotes = it },
            label = { Text("ملاحظات مسؤول النظام وقرار التوثيق") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onVerifyClientRegistration(
              client.id,
              true,
              null,
              clientReviewAdminNotes.ifBlank { "تم فحص ومطابقة بطاقة الرقم القومي بنجاح." }
            )
            activeClientToReview = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("اعتماد وتوثيق الهوية ✓", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          OutlinedButton(
            onClick = {
              clientRejectionReasonInput = "المستندات المرفوعة غير مطابقة أو غير واضحة ويُرجى إعادة تصوير بطاقة الرقم القومي (وش وظهر) بوضوح."
              showClientRejectionDialog = true
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError)
          ) {
            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp), tint = CrimsonError)
            Spacer(modifier = Modifier.width(4.dp))
            Text("رفض التوثيق ❌")
          }

          TextButton(onClick = { activeClientToReview = null }) {
            Text("إلغاء")
          }
        }
      }
    )
  }

  // ==========================================
  // DIALOG: Client KYC Rejection Reason Specification
  // ==========================================
  if (showClientRejectionDialog && activeClientToReview != null) {
    val targetClient = activeClientToReview!!
    AlertDialog(
      onDismissRequest = { showClientRejectionDialog = false },
      icon = {
        Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(32.dp))
      },
      title = {
        Text("رفض طلب توثيق هوية الموكل مع بيان السبب", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark, textAlign = TextAlign.Center)
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text("تحديد سبب عدم اعتماد بطاقة الرقم القومي للموكل (${targetClient.name}):", fontSize = 11.sp, color = TextSecondary)

          val rejectionReasons = listOf(
            "صورة بطاقة الرقم القومي غير واضحة أو غير مقروءة.",
            "الرقم القومي المدخل لا يطابق المستند المرفوع.",
            "مطلوب رفع الوجهين (وش وظهر) للبطاقة.",
            "البطاقة منتهية الصلاحية وفقاً لقواعد مصلحة الأحوال المدنية.",
            "الاسم الرباعي لا يتطابق مع بطاقة الرقم القومي."
          )

          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            rejectionReasons.forEach { reason ->
              Surface(
                color = if (clientRejectionReasonInput == reason) CrimsonContainer else CreamSurfaceVariant,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (clientRejectionReasonInput == reason) CrimsonError else BorderSubtle),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { clientRejectionReasonInput = reason }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = if (clientRejectionReasonInput == reason) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (clientRejectionReasonInput == reason) CrimsonError else TextMuted,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(reason, fontSize = 10.sp, color = if (clientRejectionReasonInput == reason) CrimsonError else TextPrimary)
                }
              }
            }
          }

          OutlinedTextField(
            value = clientRejectionReasonInput,
            onValueChange = { clientRejectionReasonInput = it },
            label = { Text("سبب الرفض الموجه للموكل") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val finalReason = clientRejectionReasonInput.ifBlank { "تم رفض طلب التوثيق لعدم اكتمال أو وضوح صورة بطاقة الرقم القومي." }
            onVerifyClientRegistration(
              targetClient.id,
              false,
              finalReason,
              finalReason
            )
            showClientRejectionDialog = false
            activeClientToReview = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
        ) {
          Text("تأكيد الرفض وإشعار الموكل", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClientRejectionDialog = false }) {
          Text("تراجع")
        }
      }
    )
  }

  // ==========================================
  // DIALOG 9: Enlarged Document Preview for Admin Inspection
  // ==========================================
  adminEnlargedDocPreview?.let { (docTitle, docSide, docUri) ->
    AlertDialog(
      onDismissRequest = { adminEnlargedDocPreview = null },
      icon = {
        Icon(Icons.Default.Visibility, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(28.dp))
      },
      title = {
        Text(docTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark, textAlign = TextAlign.Center)
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                Surface(color = GoldContainer, shape = RoundedCornerShape(4.dp)) {
                  Text(
                    text = docSide,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldDark,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }
                Surface(color = EmeraldContainer, shape = RoundedCornerShape(4.dp)) {
                  Text(
                    text = "المستند صالح ومطابق للمواصفات الرسمية ✓",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSuccess,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
                Text("المسار: $docUri", fontSize = 8.5.sp, color = TextMuted)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { adminEnlargedDocPreview = null },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("إغلاق المعاينة")
        }
      }
    )
  }
}
