package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.EscrowStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscrowScreen(
  currentUser: UserProfile,
  transactions: List<EscrowTransaction>,
  requests: List<ServiceRequest> = emptyList(),
  deposits: List<DepositRecord> = emptyList(),
  withdrawalRequests: List<LawyerWithdrawalRequest> = emptyList(),
  onBackClick: (() -> Unit)? = null,
  onNavigateToDeposit: (() -> Unit)? = null,
  onFundEscrow: ((requestId: String, total: Double, lawyerFee: Double, legalExpenses: Double, platformFee: Double, method: String) -> Unit)? = null,
  onReleaseEscrow: ((requestId: String, stars: Int, comment: String) -> Unit)? = null,
  onOpenDispute: ((requestId: String, reason: String, details: String) -> Unit)? = null,
  onRequestWithdrawal: ((amount: Double, completedRequestId: String, account: String, accountType: String) -> Unit)? = null,
  onRequestClick: ((requestId: String) -> Unit)? = null,
  onOpenWorkspace: ((requestId: String) -> Unit)? = null
) {
  val isLawyer = currentUser.role == UserRole.LAWYER
  var selectedTab by remember { mutableStateOf(0) }
  var searchQuery by remember { mutableStateOf("") }

  // Dialog states
  var showDepositModal by remember { mutableStateOf(false) }
  var showWithdrawalModal by remember { mutableStateOf(false) }
  var showReleaseModal by remember { mutableStateOf(false) }
  var selectedTxForRelease by remember { mutableStateOf<EscrowTransaction?>(null) }
  var showCertificateModal by remember { mutableStateOf(false) }
  var selectedTxForCert by remember { mutableStateOf<EscrowTransaction?>(null) }
  var showDisputeModal by remember { mutableStateOf(false) }
  var selectedTxForDispute by remember { mutableStateOf<EscrowTransaction?>(null) }

  val clipboardManager = LocalClipboardManager.current
  val totalHeldAmount = transactions.filter { it.status == EscrowStatus.HELD }.sumOf { it.totalAmount }
  val totalReleasedAmount = transactions.filter { it.status == EscrowStatus.RELEASED }.sumOf { it.lawyerAmount }
  val totalFrozenAmount = transactions.filter { it.status == EscrowStatus.FROZEN_FOR_DISPUTE }.sumOf { it.totalAmount }
  val lawyerWithdrawals = withdrawalRequests.filter { if (isLawyer) it.lawyerId == currentUser.id else true }

  // Completed requests eligible for withdrawal
  val completedRequests = requests.filter { it.status == RequestStatus.COMPLETED }

  val filteredTransactions = remember(transactions, selectedTab, searchQuery) {
    val tabFiltered = when (selectedTab) {
      1 -> transactions.filter { it.status == EscrowStatus.HELD }
      2 -> transactions.filter { it.status == EscrowStatus.RELEASED }
      3 -> transactions.filter { it.status == EscrowStatus.FROZEN_FOR_DISPUTE || it.status == EscrowStatus.REFUNDED }
      else -> transactions
    }
    if (searchQuery.isBlank()) {
      tabFiltered
    } else {
      tabFiltered.filter {
        it.referenceNumber.contains(searchQuery, ignoreCase = true) ||
          it.paymentMethod.contains(searchQuery, ignoreCase = true) ||
          (requests.find { r -> r.id == it.requestId }?.title?.contains(searchQuery, ignoreCase = true) == true)
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Shield, contentDescription = null, tint = GoldLight, modifier = Modifier.size(20.dp))
              Text(
                text = "منظومة الضمان المالي لمتر (Escrow)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
            Text(
              text = "حجز أتعاب آمن • تحرير مشروط برضا العميل • خاضع للبنك المركزي",
              fontSize = 10.sp,
              color = GoldLight.copy(alpha = 0.9f)
            )
          }
        },
        navigationIcon = {
          if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
              Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
            }
          }
        },
        actions = {
          IconButton(onClick = { showDepositModal = true }) {
            Icon(Icons.Default.AddCard, contentDescription = "إيداع جديد", tint = GoldLight)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showDepositModal = true },
        containerColor = EmeraldSuccess,
        contentColor = Color.White,
        icon = { Icon(Icons.Default.Security, contentDescription = null) },
        text = { Text("إيداع وتأمين قضية جديدة", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
      )
    },
    containerColor = MaterialTheme.adaptiveBackground
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Interactive Financial Escrow Wallet Summary Card
      item {
        Surface(
          color = NavyDark,
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f)),
          shadowElevation = 4.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("الرصيد المتاح بالمحفظة", color = GoldLight, fontSize = 11.sp)
                Text(
                  text = "${currentUser.balance.toInt()} ج.م",
                  color = Color.White,
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Black
                )
              }

              Surface(
                color = EmeraldSuccess.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.6f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                  Text(
                    text = "محفظة مشفرة ومؤمنة",
                    color = EmeraldSuccess,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // Quick Stats Grid
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Held in Escrow
              Surface(
                modifier = Modifier.weight(1f),
                color = NavySurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.3f))
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GoldSecondary))
                    Text("المحتجز بالضمان", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text("${totalHeldAmount.toInt()} ج.م", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                }
              }

              // Released to Lawyers
              Surface(
                modifier = Modifier.weight(1f),
                color = NavySurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f))
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldSuccess))
                    Text("المحرر للمحامين", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text("${totalReleasedAmount.toInt()} ج.م", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }
              }
            }

            Divider(color = Color.White.copy(alpha = 0.12f), thickness = 0.8.dp)

            // Escrow Deposit / Withdrawal Action Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (isLawyer) {
                // Lawyer Withdrawal Button (سحب رصيد المحامي عن الطلبات التي أتمها)
                Button(
                  onClick = { showWithdrawalModal = true },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark)
                ) {
                  Icon(Icons.Default.PriceCheck, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("سحب رصيد الأتعاب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                  onClick = { selectedTab = 5 },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                  border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary)
                ) {
                  Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("سجل السحوبات المعتمدة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              } else {
                Button(
                  onClick = { showDepositModal = true },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                  Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("إيداع وتأمين خدمة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                  onClick = { onNavigateToDeposit?.invoke() ?: run { showDepositModal = true } },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                  border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary)
                ) {
                  Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("شحن رصيد المحفظة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      // 2. Guarantee & Protection Accordion / Banner
      item {
        Surface(
          color = CreamSurface,
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GoldContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
              Text("ضمان مالي ثلاثي الأركان بمصر", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
              Text(
                "الأتعاب محتجزة بحساب الضمان ولا تُصرف للمحامي إلا بعد موافقتك على مخرجات العمل القانوني.",
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 14.sp
              )
            }
          }
        }
      }

      // 3. Filter Tabs
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            val tabs = mutableListOf(
              "الكل (${transactions.size})" to 0,
              "المحتجزة بالضمان (${transactions.count { it.status == EscrowStatus.HELD }})" to 1,
              "المحررة للمحامي (${transactions.count { it.status == EscrowStatus.RELEASED }})" to 2,
              "نزاع وتحكيم (${transactions.count { it.status == EscrowStatus.FROZEN_FOR_DISPUTE }})" to 3,
              "سجل الإيداع (${deposits.size})" to 4
            )
            if (isLawyer || currentUser.role == UserRole.ADMIN) {
              tabs.add("طلبات السحب (${lawyerWithdrawals.size})" to 5)
            }
            items(tabs) { (title, index) ->
              val isSelected = selectedTab == index
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { selectedTab = index }
                  .border(
                    1.dp,
                    if (isSelected) NavyPrimary else MaterialTheme.adaptiveBorder,
                    RoundedCornerShape(10.dp)
                  ),
                color = if (isSelected) NavyPrimary else MaterialTheme.adaptiveSurface
              ) {
                Text(
                  text = title,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else MaterialTheme.adaptiveTextSecondary
                )
              }
            }
          }

          // Search Field
          if (selectedTab != 4 && selectedTab != 5) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("بحث بالرقم المرجعي أو اسم القضية أو طريقة السداد...", fontSize = 11.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.adaptiveTextMuted, modifier = Modifier.size(18.dp)) },
              trailingIcon = {
                if (searchQuery.isNotBlank()) {
                  IconButton(onClick = { searchQuery = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.adaptiveTextMuted)
                  }
                }
              },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = maitreTextFieldColors(),
              singleLine = true
            )
          }
        }
      }

      // 4. Content based on Selected Tab
      if (selectedTab == 5) {
        // Render Lawyer Withdrawal Records
        if (lawyerWithdrawals.isEmpty()) {
          item {
            EmptyEscrowState(message = "لا توجد طلبات سحب أتعاب مسجلة بعد.")
          }
        } else {
          items(lawyerWithdrawals) { withdrawal ->
            LawyerWithdrawalItemCard(withdrawal = withdrawal)
          }
        }
      } else if (selectedTab == 4) {
        // Render Deposit Records
        if (deposits.isEmpty()) {
          item {
            EmptyEscrowState(message = "لا توجد عمليات إيداع بنكية مسجلة بعد.")
          }
        } else {
          items(deposits) { dep ->
            DepositItemCard(
              deposit = dep,
              onCopyRef = { ref ->
                clipboardManager.setText(AnnotatedString(ref))
              }
            )
          }
        }
      } else {
        // Render Escrow Transactions
        if (filteredTransactions.isEmpty()) {
          item {
            EmptyEscrowState(
              message = if (searchQuery.isNotBlank()) "لم يتم العثور على عمليات مطابقة لبحثك." else "لا توجد عمليات ضمان مالي في هذا التبويب."
            )
          }
        } else {
          items(filteredTransactions) { tx ->
            val linkedRequest = requests.find { it.id == tx.requestId }
            EscrowTransactionCard(
              transaction = tx,
              request = linkedRequest,
              isClient = currentUser.role == UserRole.CLIENT || currentUser.role == UserRole.ADMIN,
              onReleaseClick = {
                selectedTxForRelease = tx
                showReleaseModal = true
              },
              onViewCertificate = {
                selectedTxForCert = tx
                showCertificateModal = true
              },
              onDisputeClick = {
                selectedTxForDispute = tx
                showDisputeModal = true
              },
              onRequestClick = {
                onRequestClick?.invoke(tx.requestId)
              },
              onOpenWorkspace = {
                onOpenWorkspace?.invoke(tx.requestId)
              }
            )
          }
        }
      }
    }
  }

  // DIALOG 1: Direct Fund Escrow / Case Deposit Modal
  if (showDepositModal) {
    FundEscrowModal(
      currentUser = currentUser,
      requests = requests.filter { it.status == RequestStatus.OPEN || it.status == RequestStatus.IN_PROGRESS },
      onDismiss = { showDepositModal = false },
      onConfirmDeposit = { reqId, total, lawyerFee, legalExpenses, platFee, method ->
        onFundEscrow?.invoke(reqId, total, lawyerFee, legalExpenses, platFee, method)
        showDepositModal = false
      }
    )
  }

  // DIALOG 2: Release Escrow to Lawyer Confirmation Modal
  if (showReleaseModal && selectedTxForRelease != null) {
    val tx = selectedTxForRelease!!
    val linkedRequest = requests.find { it.id == tx.requestId }
    ReleaseEscrowModal(
      transaction = tx,
      requestTitle = linkedRequest?.title ?: "القضية القانونية",
      onDismiss = {
        showReleaseModal = false
        selectedTxForRelease = null
      },
      onConfirmRelease = { stars, comment ->
        onReleaseEscrow?.invoke(tx.requestId, stars, comment)
        showReleaseModal = false
        selectedTxForRelease = null
      }
    )
  }

  // DIALOG 3: Official Digital Escrow Certificate & Receipt
  if (showCertificateModal && selectedTxForCert != null) {
    val tx = selectedTxForCert!!
    val linkedRequest = requests.find { it.id == tx.requestId }
    DigitalEscrowCertificateModal(
      transaction = tx,
      request = linkedRequest,
      clientName = currentUser.name,
      onDismiss = {
        showCertificateModal = false
        selectedTxForCert = null
      },
      onCopyRef = { ref ->
        clipboardManager.setText(AnnotatedString(ref))
      }
    )
  }

  // DIALOG 4: Dispute / Freeze Escrow Modal
  if (showDisputeModal && selectedTxForDispute != null) {
    val tx = selectedTxForDispute!!
    val linkedRequest = requests.find { it.id == tx.requestId }
    FreezeEscrowDisputeModal(
      transaction = tx,
      requestTitle = linkedRequest?.title ?: "القضية القانونية",
      onDismiss = {
        showDisputeModal = false
        selectedTxForDispute = null
      },
      onConfirmDispute = { reason, details ->
        onOpenDispute?.invoke(tx.requestId, reason, details)
        showDisputeModal = false
        selectedTxForDispute = null
      }
    )
  }

  // DIALOG 5: Lawyer Withdrawal Request Modal (طلب سحب رصيد المحامي بعد اعتماد مدير النظام)
  if (showWithdrawalModal) {
    LawyerWithdrawalModal(
      currentBalance = currentUser.balance,
      completedRequests = completedRequests,
      onDismiss = { showWithdrawalModal = false },
      onConfirmWithdrawal = { amount, completedReqId, account, accountType ->
        onRequestWithdrawal?.invoke(amount, completedReqId, account, accountType)
        showWithdrawalModal = false
      }
    )
  }
}

// -----------------------------------------------------------------------------------------
// Escrow Transaction Card Component
// -----------------------------------------------------------------------------------------
@Composable
fun EscrowTransactionCard(
  transaction: EscrowTransaction,
  request: ServiceRequest?,
  isClient: Boolean,
  onReleaseClick: () -> Unit,
  onViewCertificate: () -> Unit,
  onDisputeClick: () -> Unit,
  onRequestClick: () -> Unit,
  onOpenWorkspace: () -> Unit
) {
  Surface(
    color = MaterialTheme.adaptiveSurface,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      when (transaction.status) {
        EscrowStatus.HELD -> GoldSecondary.copy(alpha = 0.8f)
        EscrowStatus.RELEASED -> EmeraldSuccess.copy(alpha = 0.5f)
        EscrowStatus.FROZEN_FOR_DISPUTE -> CrimsonError.copy(alpha = 0.5f)
        EscrowStatus.REFUNDED -> NavyPrimary.copy(alpha = 0.3f)
      }
    ),
    shadowElevation = 2.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Top Row: Reference + Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
          Text(
            text = "سند ضمان: ${transaction.referenceNumber}",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = NavyPrimary
          )
        }
        EscrowStatusBadge(status = transaction.status)
      }

      // Case Details Row
      if (request != null) {
        Surface(
          color = CreamSurfaceVariant,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onRequestClick() }
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldDark, modifier = Modifier.size(18.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = request.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "الاختصاص: ${request.courtLocation?.courtJurisdiction ?: "المحاكم المصرية المختصة"}",
                fontSize = 10.sp,
                color = TextMuted
              )
            }
            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
          }
        }
      }

      // Financial Breakdown Box
      Surface(
        color = NavyContainer.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("أتعاب ومصاريف المحاماة المحتجزة:", fontSize = 11.sp, color = TextSecondary)
            Text("${transaction.lawyerAmount.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          }
          if (transaction.platformFee > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("رسم حماية الضمان وإدارة القضية:", fontSize = 10.sp, color = TextMuted)
              Text("${transaction.platformFee.toInt()} ج.م", fontSize = 10.sp, color = TextMuted)
            }
          }
          Divider(color = BorderSubtle, thickness = 0.5.dp)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("إجمالي المبلغ المودع والمضمون:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Text("${transaction.totalAmount.toInt()} ج.م", fontSize = 13.sp, fontWeight = FontWeight.Black, color = NavyPrimary)
          }
        }
      }

      // Payment Method & Date
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Icon(Icons.Default.Payment, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
          Text(text = transaction.paymentMethod, fontSize = 10.sp, color = TextSecondary)
        }
        Text(text = transaction.date, fontSize = 10.sp, color = TextMuted)
      }

      // Live Milestone Stepper Bar
      EscrowMilestoneProgressBar(status = transaction.status)

      Divider(color = BorderSubtle, thickness = 0.6.dp)

      // Interactive Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // 1. Digital Certificate Button
        OutlinedButton(
          onClick = onViewCertificate,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
        ) {
          Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(14.dp), tint = NavyPrimary)
          Spacer(modifier = Modifier.width(4.dp))
          Text("سند الضمان", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        }

        // 2. Chat / Workspace Button
        OutlinedButton(
          onClick = onOpenWorkspace,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
        ) {
          Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = GoldDark)
          Spacer(modifier = Modifier.width(4.dp))
          Text("مساحة العمل", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // 3. Conditional Action: Release or Dispute
        if (transaction.status == EscrowStatus.HELD && isClient) {
          Button(
            onClick = onReleaseClick,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1.2f),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Text("تحرير الأتعاب", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        } else if (transaction.status == EscrowStatus.HELD) {
          // Lawyer view or dispute
          OutlinedButton(
            onClick = onDisputeClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.WarningAmber, contentDescription = null, modifier = Modifier.size(14.dp), tint = CrimsonError)
            Spacer(modifier = Modifier.width(4.dp))
            Text("تجميد ونزاع", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------------------
// Escrow Milestone Progress Stepper
// -----------------------------------------------------------------------------------------
@Composable
fun EscrowMilestoneProgressBar(status: EscrowStatus) {
  val currentStep = when (status) {
    EscrowStatus.HELD -> 2
    EscrowStatus.RELEASED -> 4
    EscrowStatus.FROZEN_FOR_DISPUTE -> 3
    EscrowStatus.REFUNDED -> 4
  }

  val steps = listOf(
    "1. إيداع وحجز" to (currentStep >= 1),
    "2. مباشرة العمل" to (currentStep >= 2),
    "3. تسليم المخرجات" to (currentStep >= 3),
    "4. تحرير الأتعاب" to (currentStep >= 4 && status != EscrowStatus.FROZEN_FOR_DISPUTE)
  )

  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      steps.forEachIndexed { index, (label, isPassed) ->
        val isCurrent = (index + 1) == currentStep
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(
                if (isPassed && status == EscrowStatus.RELEASED) EmeraldSuccess
                else if (isPassed) GoldSecondary
                else CreamSurfaceVariant
              )
              .border(
                1.dp,
                if (isCurrent) NavyPrimary else BorderSubtle,
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            if (isPassed) {
              Icon(Icons.Default.Check, contentDescription = null, tint = NavyDark, modifier = Modifier.size(11.dp))
            } else {
              Text("${index + 1}", fontSize = 9.sp, color = TextMuted)
            }
          }
          Text(
            text = label,
            fontSize = 8.sp,
            color = if (isPassed) TextPrimary else TextMuted,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------------------
// Deposit Item Card
// -----------------------------------------------------------------------------------------
@Composable
fun DepositItemCard(deposit: DepositRecord, onCopyRef: (String) -> Unit) {
  Surface(
    color = CreamSurface,
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(EmeraldContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
      }

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = deposit.paymentMethod, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
          Text(text = "+${deposit.amount.toInt()} ج.م", fontWeight = FontWeight.Black, fontSize = 14.sp, color = EmeraldSuccess)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "رقم العملية: ${deposit.referenceNumber}", fontSize = 10.sp, color = TextSecondary)
          Text(text = deposit.date, fontSize = 10.sp, color = TextMuted)
        }
      }

      IconButton(onClick = { onCopyRef(deposit.referenceNumber) }) {
        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = TextMuted, modifier = Modifier.size(16.dp))
      }
    }
  }
}

// -----------------------------------------------------------------------------------------
// DIALOG 1: Direct Fund Escrow / Deposit Modal
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundEscrowModal(
  currentUser: UserProfile,
  requests: List<ServiceRequest>,
  onDismiss: () -> Unit,
  onConfirmDeposit: (requestId: String, total: Double, lawyerFee: Double, legalExpenses: Double, platFee: Double, method: String) -> Unit
) {
  var selectedReqId by remember { mutableStateOf(requests.firstOrNull()?.id ?: "") }
  var lawyerFeeInput by remember { mutableStateOf("4000") }
  var legalExpensesInput by remember { mutableStateOf("500") }
  var selectedPaymentMethod by remember { mutableStateOf("إنستاباي InstaPay") }

  val chosenRequest = requests.find { it.id == selectedReqId }
  val feeVal = lawyerFeeInput.toDoubleOrNull() ?: 0.0
  val expVal = legalExpensesInput.toDoubleOrNull() ?: 0.0
  val platFeeVal = (feeVal + expVal) * 0.10
  val grandTotal = feeVal + expVal + platFeeVal

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess)
        Text("إيداع وتأمين خدمة قانونية بحساب الضمان", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          "اختر القضية أو الخدمة المراد إيداع وحجز أتعابها بأمان، حتى يباشر المحامي العمل فوراً.",
          fontSize = 11.sp,
          color = TextSecondary
        )

        // Request Selector
        if (requests.isNotEmpty()) {
          Text("حدد طلب الخدمة القانونية:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            requests.take(3).forEach { req ->
              val isSelected = selectedReqId == req.id
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable {
                    selectedReqId = req.id
                    lawyerFeeInput = (req.budgetAmount * 0.9).toInt().toString()
                    legalExpensesInput = (req.budgetAmount * 0.1).toInt().toString()
                  }
                  .border(1.dp, if (isSelected) NavyPrimary else BorderSubtle, RoundedCornerShape(8.dp)),
                color = if (isSelected) NavyContainer else CreamSurfaceVariant
              ) {
                Row(
                  modifier = Modifier.padding(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    req.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else TextPrimary,
                    modifier = Modifier.weight(1f)
                  )
                  Text(
                    "${req.budgetAmount.toInt()} ج.م",
                    fontSize = 11.sp,
                    color = if (isSelected) GoldLight else GoldDark,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }

        // Financial Amounts Inputs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Column(modifier = Modifier.weight(1f)) {
            Text("أتعاب المحامي (ج.م) *", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
              value = lawyerFeeInput,
              onValueChange = { lawyerFeeInput = it },
              shape = RoundedCornerShape(8.dp),
              colors = maitreTextFieldColors(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text("المصاريف القضائية (ج.م)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
              value = legalExpensesInput,
              onValueChange = { legalExpensesInput = it },
              shape = RoundedCornerShape(8.dp),
              colors = maitreTextFieldColors(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true
            )
          }
        }

        // Breakdown Box
        Surface(
          color = GoldContainer.copy(alpha = 0.5f),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary)
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("أتعاب المحاماة المحتجزة:", fontSize = 11.sp)
              Text("${feeVal.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("المصاريف القضائية المقدرة:", fontSize = 11.sp)
              Text("${expVal.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("رسم خدمة وحماية الضمان (10%):", fontSize = 10.sp, color = TextMuted)
              Text("${platFeeVal.toInt()} ج.م", fontSize = 10.sp, color = TextMuted)
            }
            Divider(color = GoldSecondary, thickness = 0.5.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("الإجمالي المودع والمحمي بالضمان:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Text("${grandTotal.toInt()} ج.م", fontSize = 13.sp, fontWeight = FontWeight.Black, color = NavyPrimary)
            }
          }
        }

        // Payment Method Picker
        Text("طريقة الإيداع والسداد المباشر:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        val paymentMethods = listOf(
          "إنستاباي InstaPay (التحويل اللحظي المعتمد)",
          "فودافون كاش / محافظ إلكترونية",
          "بطاقة بنكية (ميزة / فيزا / ماستركارد)",
          "رصيد المحفظة المتاح (${currentUser.balance.toInt()} ج.م)"
        )
        paymentMethods.forEach { method ->
          val isSelected = selectedPaymentMethod.startsWith(method.take(8))
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable { selectedPaymentMethod = method }
              .border(1.dp, if (isSelected) GoldDark else BorderSubtle, RoundedCornerShape(8.dp)),
            color = if (isSelected) GoldContainer.copy(alpha = 0.4f) else CreamSurfaceVariant
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(method, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
              if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val reqId = selectedReqId.ifBlank { "req_auto_${System.currentTimeMillis() % 1000}" }
          onConfirmDeposit(reqId, grandTotal, feeVal, expVal, platFeeVal, selectedPaymentMethod)
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
      ) {
        Text("تأكيد الإيداع والحجز بالضمان")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    }
  )
}

// -----------------------------------------------------------------------------------------
// DIALOG 2: Release Escrow Modal
// -----------------------------------------------------------------------------------------
@Composable
fun ReleaseEscrowModal(
  transaction: EscrowTransaction,
  requestTitle: String,
  onDismiss: () -> Unit,
  onConfirmRelease: (stars: Int, comment: String) -> Unit
) {
  var stars by remember { mutableStateOf(5) }
  var commentInput by remember { mutableStateOf("تم إنجاز العمل القانوني ومراجعته بالكامل على الوجه الأكمل.") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess)
        Text("اعتماد الخدمة وتحرير الأتعاب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "أنت على وشك تحرير مبلغ الأتعاب المحتجز (${transaction.lawyerAmount.toInt()} ج.م) لحساب المحامي بعد اكتمال القضية: \"$requestTitle\".",
          fontSize = 12.sp,
          color = TextSecondary,
          lineHeight = 16.sp
        )

        Surface(
          color = EmeraldContainer.copy(alpha = 0.4f),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
            Text(
              "عند الضغط على تأكيد، يتم إصدار مخالصة رسمية إلكترونية فورية وتحويل الأتعاب لحساب المحامي.",
              fontSize = 11.sp,
              color = Color(0xFF065F46)
            )
          }
        }

        // Lawyer Rating Stars
        Text("تقييمك لأداء المحامي في هذه القضية:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center
        ) {
          for (i in 1..5) {
            IconButton(onClick = { stars = i }) {
              Icon(
                imageVector = if (i <= stars) Icons.Default.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = GoldSecondary,
                modifier = Modifier.size(28.dp)
              )
            }
          }
        }

        OutlinedTextField(
          value = commentInput,
          onValueChange = { commentInput = it },
          label = { Text("ملاحظاتك وتقييمك النهائي") },
          minLines = 2,
          shape = RoundedCornerShape(8.dp),
          colors = maitreTextFieldColors(),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmRelease(stars, commentInput) },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
      ) {
        Text("تأكيد تحرير الأتعاب الآن")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    }
  )
}

// -----------------------------------------------------------------------------------------
// DIALOG 3: Official Digital Escrow Certificate & Receipt Modal
// -----------------------------------------------------------------------------------------
@Composable
fun DigitalEscrowCertificateModal(
  transaction: EscrowTransaction,
  request: ServiceRequest?,
  clientName: String,
  onDismiss: () -> Unit,
  onCopyRef: (String) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldDark)
        Text("سند الضمان المالي المعتمد", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Formal Certificate Canvas Box
        Surface(
          color = Color(0xFFFCFDFE),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(2.dp, GoldSecondary),
          shadowElevation = 3.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Header
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("منظومة مِتر للخدمات القانونية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                Text("جمهورية مصر العربية", fontSize = 10.sp, color = TextMuted)
              }
              Surface(color = GoldContainer, shape = RoundedCornerShape(6.dp)) {
                Text("سند رسمي موثق", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldDark, modifier = Modifier.padding(4.dp))
              }
            }

            Divider(color = GoldSecondary.copy(alpha = 0.5f))

            // Reference & Timestamp
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("الرقم المرجعي الموحد:", fontSize = 10.sp, color = TextMuted)
              Text(transaction.referenceNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, fontFamily = FontFamily.Monospace)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("تاريخ القيد والإيداع:", fontSize = 10.sp, color = TextMuted)
              Text(transaction.date, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("حالة السند المالي:", fontSize = 10.sp, color = TextMuted)
              Text(
                when (transaction.status) {
                  EscrowStatus.HELD -> "محتجز بحساب الضمان (قيد التنفيذ)"
                  EscrowStatus.RELEASED -> "تم الصرف والتحرير للمحامي"
                  EscrowStatus.FROZEN_FOR_DISPUTE -> "مجمد لوجود تحكيم"
                  EscrowStatus.REFUNDED -> "تم الاسترداد للعميل"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.status == EscrowStatus.RELEASED) EmeraldSuccess else GoldDark
              )
            }

            Divider(color = BorderSubtle)

            // Legal Task Info
            Text("بيانات الخدمة القانونية المضمونة:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Text(request?.title ?: "دعوى واستشارة قانونية مقيدة بالمنصة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("الطرف المودع: $clientName", fontSize = 10.sp, color = TextSecondary)
            Text("جهة الاختصاص: ${request?.courtLocation?.courtJurisdiction ?: "المحاكم المصرية"}", fontSize = 10.sp, color = TextMuted)

            Divider(color = BorderSubtle)

            // Financial Breakdown
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("أتعاب ومصاريف المحاماة:", fontSize = 10.sp, color = TextSecondary)
              Text("${transaction.lawyerAmount.toInt()} ج.م", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("إجمالي المبلغ المشفر بالضمان:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Text("${transaction.totalAmount.toInt()} ج.م", fontSize = 12.sp, fontWeight = FontWeight.Black, color = NavyPrimary)
            }

            Divider(color = GoldSecondary.copy(alpha = 0.5f))

            // Legal Clause Guarantee
            Text(
              "تعهد قانوني: المبالغ المذكورة أعلاه محتجزة بأمان في حساب الضمان المصرفي طبقاً لأحكام القانون المدني المصري رقم 131 لسنة 1948 وقانون التجارة، ولا تُحرر إلا بموافقة العميل الصريحة أو قرار تحكيمي واجب النفاذ.",
              fontSize = 9.sp,
              color = TextMuted,
              lineHeight = 13.sp
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onCopyRef(transaction.referenceNumber)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("نسخ رقم السند المرجعي")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إغلاق")
      }
    }
  )
}

// -----------------------------------------------------------------------------------------
// DIALOG 4: Freeze Escrow & Open Dispute Modal
// -----------------------------------------------------------------------------------------
@Composable
fun FreezeEscrowDisputeModal(
  transaction: EscrowTransaction,
  requestTitle: String,
  onDismiss: () -> Unit,
  onConfirmDispute: (reason: String, details: String) -> Unit
) {
  var disputeReason by remember { mutableStateOf("عدم الالتزام ببنود العمل أو التأخر غير المبرر") }
  var disputeDetails by remember { mutableStateOf("") }

  val reasons = listOf(
    "عدم الالتزام ببنود العمل أو التأخر غير المبرر",
    "عدم الحضور في جلسة المحكمة / النيابة المحددة",
    "وجود أخطاء جوهرية في الصياغة أو المستندات",
    "خلاف على المصاريف القضائية الفعلية"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Gavel, contentDescription = null, tint = CrimsonError)
        Text("طلب تجميد الضمان والتحكيم", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          "سيتم تجميد مبلغ الضمان (${transaction.totalAmount.toInt()} ج.م) فوراً وإحالة ملف القضية: \"$requestTitle\" لهيئة التحكيم القضائي بمتر.",
          fontSize = 11.sp,
          color = TextSecondary
        )

        Text("اختر سبب طلب التحكيم:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        reasons.forEach { r ->
          val isSelected = disputeReason == r
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable { disputeReason = r }
              .border(1.dp, if (isSelected) CrimsonError else BorderSubtle, RoundedCornerShape(8.dp)),
            color = if (isSelected) CrimsonError.copy(alpha = 0.08f) else CreamSurfaceVariant
          ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
              Text(r, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
          }
        }

        OutlinedTextField(
          value = disputeDetails,
          onValueChange = { disputeDetails = it },
          label = { Text("شرح تفصيلي للمشكلة والمستندات المؤيدة") },
          minLines = 3,
          shape = RoundedCornerShape(8.dp),
          colors = maitreTextFieldColors(),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val det = if (disputeDetails.isBlank()) "طلب تحكيم بخصوص: $disputeReason" else disputeDetails
          onConfirmDispute(disputeReason, det)
        },
        colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
      ) {
        Text("تجميد الضمان ورفع الشكوى")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    }
  )
}

// -----------------------------------------------------------------------------------------
// Empty State
// -----------------------------------------------------------------------------------------
@Composable
fun EmptyEscrowState(message: String) {
  Surface(
    color = CreamSurface,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 20.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
      Text(
        text = message,
        fontSize = 13.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center
      )
    }
  }
}

// -----------------------------------------------------------------------------------------
// Lawyer Withdrawal Modal & Card Components
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LawyerWithdrawalModal(
  currentBalance: Double,
  completedRequests: List<ServiceRequest>,
  onDismiss: () -> Unit,
  onConfirmWithdrawal: (amount: Double, completedRequestId: String, account: String, accountType: String) -> Unit
) {
  var amountText by remember { mutableStateOf(if (currentBalance > 0) currentBalance.toInt().toString() else "1000") }
  var selectedReqId by remember {
    mutableStateOf(completedRequests.firstOrNull()?.id ?: "req_completed_general")
  }
  var selectedAccountType by remember { mutableStateOf("إنستاباي InstaPay") }
  var accountIdentifier by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val accountTypes = listOf("إنستاباي InstaPay", "محفظة فودافون كاش", "حساب بنكي IBAN")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.PriceCheck, contentDescription = null, tint = GoldDark)
        Text("طلب سحب رصيد الأتعاب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Notice banner regarding Admin Approval
        Surface(
          color = GoldContainer.copy(alpha = 0.5f),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
            Text(
              text = "يتم صرف أتعاب الطلبات المكتملة بعد تدقيق واعتماد مدير النظام للتحويل البنكي لحسابك.",
              fontSize = 11.sp,
              color = NavyDark,
              lineHeight = 16.sp
            )
          }
        }

        // Available balance display
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("رصيدك المتاح للسحب:", fontSize = 12.sp, color = TextSecondary)
          Text("${currentBalance.toInt()} ج.م", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
        }

        OutlinedTextField(
          value = amountText,
          onValueChange = {
            amountText = it.filter { char -> char.isDigit() }
            errorMessage = null
          },
          label = { Text("المبلغ المراد سحبه (ج.م)") },
          keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
          shape = RoundedCornerShape(8.dp),
          colors = maitreTextFieldColors(),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        // Select Completed Request (الطلب المنجز المرتبط بالسحب)
        Text("الطلب المنجز المرتبط بالسحب:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        if (completedRequests.isNotEmpty()) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            completedRequests.forEach { req ->
              val isSelected = selectedReqId == req.id
              Surface(
                color = if (isSelected) GoldContainer else CreamSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldSecondary else BorderSubtle),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { selectedReqId = req.id }
              ) {
                Row(
                  modifier = Modifier.padding(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(req.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("الموكل: ${req.clientName} • مكتملة بنجاح", fontSize = 10.sp, color = TextSecondary)
                  }
                  if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
                  }
                }
              }
            }
          }
        } else {
          Surface(
            color = CreamSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "سيتم ربط السحب برصيد الأتعاب المنجزة المتوفر بمحفظتك.",
              fontSize = 11.sp,
              color = TextMuted,
              modifier = Modifier.padding(8.dp)
            )
          }
        }

        // Account Type
        Text("طريقة استلام الأتعاب:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          accountTypes.forEach { type ->
            val isSelected = selectedAccountType == type
            Surface(
              color = if (isSelected) NavyPrimary else CreamSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable { selectedAccountType = type }
            ) {
              Text(
                text = type,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
              )
            }
          }
        }

        // Account Input
        OutlinedTextField(
          value = accountIdentifier,
          onValueChange = {
            accountIdentifier = it
            errorMessage = null
          },
          label = {
            Text(
              when (selectedAccountType) {
                "إنستاباي InstaPay" -> "عنوان إنستاباي (مثال: username@instapay أو رقم الهاتف)"
                "محفظة فودافون كاش" -> "رقم محفظة كاش (010/011/012/015)"
                else -> "رقم الحساب البنكي أو الآيبان (IBAN المصري)"
              }
            )
          },
          shape = RoundedCornerShape(8.dp),
          colors = maitreTextFieldColors(),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        if (errorMessage != null) {
          Text(errorMessage!!, color = CrimsonError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountText.toDoubleOrNull() ?: 0.0
          if (amt <= 0) {
            errorMessage = "يرجى إدخال مبلغ صحيح أكبر من الصفر."
            return@Button
          }
          if (amt > currentBalance) {
            errorMessage = "المبلغ المطلوب يتجاوز رصيدك المتاح (${currentBalance.toInt()} ج.م)."
            return@Button
          }
          if (accountIdentifier.isBlank()) {
            errorMessage = "يرجى كتابة رقم الحساب أو معرف التحويل."
            return@Button
          }
          onConfirmWithdrawal(amt, selectedReqId, accountIdentifier.trim(), selectedAccountType)
        },
        colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark)
      ) {
        Text("إرسال طلب السحب للمدير", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    }
  )
}

@Composable
fun LawyerWithdrawalItemCard(withdrawal: LawyerWithdrawalRequest) {
  val (statusBg, statusFg, statusText) = when (withdrawal.status) {
    WithdrawalStatus.PENDING -> Triple(GoldContainer, GoldDark, "قيد مراجعة واعتماد مدير النظام")
    WithdrawalStatus.APPROVED -> Triple(EmeraldContainer, EmeraldSuccess, "معتمد وتم التحويل البنكي ✓")
    WithdrawalStatus.REJECTED -> Triple(CrimsonContainer, CrimsonError, "مرفوض وتم إعادة الرصيد")
  }

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
        Column {
          Text("سحب أتعاب قضية", fontSize = 11.sp, color = TextSecondary)
          Text("${withdrawal.amount.toInt()} ج.م", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        }
        Surface(
          color = statusBg,
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = statusText,
            color = statusFg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Divider(color = BorderSubtle)

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("الطلب المنجز:", fontSize = 11.sp, color = TextSecondary)
        Text(withdrawal.completedRequestTitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("طريقة التحويل:", fontSize = 11.sp, color = TextSecondary)
        Text("${withdrawal.accountType} (${withdrawal.bankOrInstapayAccount})", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Medium)
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("تاريخ الطلب:", fontSize = 10.sp, color = TextMuted)
        Text(withdrawal.requestDate, fontSize = 10.sp, color = TextMuted)
      }

      if (withdrawal.adminNote != null) {
        Surface(
          color = CreamSurfaceVariant,
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "إفادة الإدارة: ${withdrawal.adminNote}",
            fontSize = 10.sp,
            color = NavyDark,
            modifier = Modifier.padding(6.dp)
          )
        }
      }
    }
  }
}
