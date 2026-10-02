package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.CategoryChip
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

/**
 * شاشة تتبع حالة الطلبات والقضايا القانونية
 * (قيد المراجعة، جاري العمل، تم الانتهاء، نزاع) مع شريط تقدم بصري ومسار القضية
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestTrackerScreen(
  currentUser: UserProfile,
  requests: List<ServiceRequest>,
  bids: List<Bid> = emptyList(),
  lawyers: List<Lawyer> = emptyList(),
  escrowTransactions: List<EscrowTransaction> = emptyList(),
  disputes: List<Dispute> = emptyList(),
  onBackClick: (() -> Unit)? = null,
  onRequestClick: (String) -> Unit,
  onOpenWorkspace: (String) -> Unit,
  onOpenEscrow: (String) -> Unit,
  onOpenDispute: (requestId: String, reason: String, details: String) -> Unit,
  onUpdateStatus: ((requestId: String, newStatus: RequestStatus) -> Unit)? = null,
  onNewRequestClick: () -> Unit
) {
  // Tabs: 0: الكل, 1: قيد المراجعة, 2: جاري العمل, 3: تم الانتهاء, 4: نزاع
  var selectedTab by remember { mutableIntStateOf(0) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategoryFilter by remember { mutableStateOf<RequestCategory?>(null) }

  // Modal dialog states
  var selectedRequestForTimeline by remember { mutableStateOf<ServiceRequest?>(null) }
  var showDisputeModalForRequest by remember { mutableStateOf<ServiceRequest?>(null) }
  var showUpdateStatusModalForRequest by remember { mutableStateOf<ServiceRequest?>(null) }

  // Filter requests based on User Role, Tab, Category, and Search
  val userRole = currentUser.role
  val userScopedRequests = remember(requests, userRole, currentUser.id, currentUser.assignedGovernorate) {
    requests.filter { req ->
      when (userRole) {
        UserRole.CLIENT -> req.clientId == currentUser.id || req.clientId.contains("user_client")
        UserRole.LAWYER -> {
          // Lawyer sees accepted cases + cases within his geographic jurisdiction
          val isAssignedLawyer = bids.any { it.requestId == req.id && it.lawyerId == currentUser.id && it.status == BidStatus.ACCEPTED }
          val isWithinGov = currentUser.isWithinLawyerJurisdiction(req.city, req.courtLocation)
          isAssignedLawyer || isWithinGov
        }
        UserRole.ADMIN -> true
      }
    }
  }

  // Counts for each status tab
  val countUnderReview = userScopedRequests.count { it.status == RequestStatus.OPEN || it.status == RequestStatus.NEGOTIATING }
  val countInProgress = userScopedRequests.count { it.status == RequestStatus.IN_PROGRESS }
  val countCompleted = userScopedRequests.count { it.status == RequestStatus.COMPLETED }
  val countDisputed = userScopedRequests.count { it.status == RequestStatus.DISPUTED }
  val countTotal = userScopedRequests.size

  val filteredRequests = remember(userScopedRequests, selectedTab, selectedCategoryFilter, searchQuery) {
    userScopedRequests.filter { req ->
      val matchesTab = when (selectedTab) {
        0 -> true // الكل
        1 -> req.status == RequestStatus.OPEN || req.status == RequestStatus.NEGOTIATING // قيد المراجعة
        2 -> req.status == RequestStatus.IN_PROGRESS // جاري العمل
        3 -> req.status == RequestStatus.COMPLETED // تم الانتهاء
        4 -> req.status == RequestStatus.DISPUTED // نزاع
        else -> true
      }

      val matchesCat = selectedCategoryFilter == null || req.category == selectedCategoryFilter

      val matchesSearch = if (searchQuery.isBlank()) true else {
        req.title.contains(searchQuery, ignoreCase = true) ||
          req.id.contains(searchQuery, ignoreCase = true) ||
          req.city.contains(searchQuery, ignoreCase = true) ||
          req.clientName.contains(searchQuery, ignoreCase = true) ||
          (req.courtLocation?.courtJurisdiction?.contains(searchQuery, ignoreCase = true) == true)
      }

      matchesTab && matchesCat && matchesSearch
    }
  }

  Scaffold(
    containerColor = CreamBackground,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                tint = GoldSecondary,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "تتبع مسار وحالة القضايا",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = "شريط التقدم البصري والمراحل الإجرائية المعتمدة",
              color = GoldLight.copy(alpha = 0.85f),
              fontSize = 10.5.sp
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
          if (currentUser.role != UserRole.LAWYER) {
            IconButton(onClick = onNewRequestClick) {
              Icon(Icons.Default.AddCircleOutline, contentDescription = "طلب جديد", tint = GoldSecondary)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = NavyPrimary,
          titleContentColor = Color.White
        )
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(bottom = 90.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Overview Summary Cards
      item {
        TrackerMetricsOverview(
          totalCount = countTotal,
          underReviewCount = countUnderReview,
          inProgressCount = countInProgress,
          completedCount = countCompleted,
          disputedCount = countDisputed,
          onSelectTab = { selectedTab = it }
        )
      }

      // 2. Search & Filter Bar
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("ابحث برقم القضية، العنوان، المحكمة، أو المحافظة...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color.Gray)
                }
              }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.adaptiveSurface,
              unfocusedContainerColor = MaterialTheme.adaptiveSurface,
              focusedBorderColor = NavyPrimary,
              unfocusedBorderColor = MaterialTheme.adaptiveBorder
            ),
            singleLine = true
          )

          // Filter Tabs (الكل، قيد المراجعة، جاري العمل، تم الانتهاء، نزاع)
          ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = NavyPrimary,
            edgePadding = 0.dp,
            divider = {}
          ) {
            val tabs = listOf(
              Triple(0, "الكل", countTotal),
              Triple(1, "قيد المراجعة", countUnderReview),
              Triple(2, "جاري العمل", countInProgress),
              Triple(3, "تم الانتهاء", countCompleted),
              Triple(4, "نزاع", countDisputed)
            )

            tabs.forEach { (index, title, count) ->
              val isSelected = selectedTab == index
              val tabColor = when (index) {
                1 -> Color(0xFFD97706) // Amber
                2 -> Color(0xFF0284C7) // Blue/In Progress
                3 -> Color(0xFF059669) // Emerald/Completed
                4 -> CrimsonError     // Crimson/Disputed
                else -> NavyPrimary
              }

              Tab(
                selected = isSelected,
                onClick = { selectedTab = index },
                text = {
                  Surface(
                    color = if (isSelected) tabColor else MaterialTheme.adaptiveSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) tabColor else MaterialTheme.adaptiveBorder)
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                      Text(
                        text = title,
                        color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                      )
                      Surface(
                        color = if (isSelected) Color.White.copy(alpha = 0.25f) else tabColor.copy(alpha = 0.15f),
                        shape = CircleShape
                      ) {
                        Text(
                          text = count.toString(),
                          color = if (isSelected) Color.White else tabColor,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                      }
                    }
                  }
                }
              )
            }
          }

          // Category Chips Row
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
          ) {
            item {
              FilterChip(
                selected = selectedCategoryFilter == null,
                onClick = { selectedCategoryFilter = null },
                label = { Text("كافة التخصصات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NavyPrimary,
                  selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
              )
            }
            items(RequestCategory.entries) { cat ->
              FilterChip(
                selected = selectedCategoryFilter == cat,
                onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                label = { Text(cat.titleAr, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NavyPrimary,
                  selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }
        }
      }

      // 3. Requests List with Visual Stepper Progress Bar
      if (filteredRequests.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
            border = BorderStroke(1.dp, MaterialTheme.adaptiveBorder)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = NavyPrimary.copy(alpha = 0.08f)
              ) {
                Icon(
                  imageVector = Icons.Default.SearchOff,
                  contentDescription = null,
                  tint = NavyPrimary,
                  modifier = Modifier
                    .padding(14.dp)
                    .fillMaxSize()
                )
              }

              Text(
                text = "لا توجد طلبات أو قضايا مطابقة",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.adaptiveTextPrimary
              )

              Text(
                text = when (selectedTab) {
                  1 -> "لا توجد طلبات مفتوحة قيد مراجعة عروض الأسعار حالياً."
                  2 -> "لا توجد قضايا جاري العمل عليها ومباشرة إجراءاتها الآن."
                  3 -> "لا توجد قضايا منتهية بعد. القضايا المكتملة ستظهر هنا مع المخالصات."
                  4 -> "سجل النزاعات نظيف تماماً! لا توجد قضايا محل نزاع أو تحكيم."
                  else -> "لم يتم العثور على أي قضايا مطابقة لبحثك أو تصنيفك."
                },
                fontSize = 12.sp,
                color = MaterialTheme.adaptiveTextSecondary,
                textAlign = TextAlign.Center
              )

              if (currentUser.role != UserRole.LAWYER) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                  onClick = onNewRequestClick,
                  colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("طرح طلب قانوني جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      } else {
        items(filteredRequests, key = { it.id }) { req ->
          val acceptedBid = bids.find { it.id == req.acceptedBidId } ?: bids.find { it.requestId == req.id && it.status == BidStatus.ACCEPTED }
          val assignedLawyer = if (acceptedBid != null) lawyers.find { it.id == acceptedBid.lawyerId } else null
          val escrow = escrowTransactions.find { it.requestId == req.id }
          val dispute = disputes.find { it.requestId == req.id }

          CaseTrackingCard(
            request = req,
            acceptedBid = acceptedBid,
            assignedLawyer = assignedLawyer,
            escrow = escrow,
            dispute = dispute,
            currentUserRole = currentUser.role,
            onRequestClick = { onRequestClick(req.id) },
            onOpenWorkspace = { onOpenWorkspace(req.id) },
            onOpenEscrow = { onOpenEscrow(req.id) },
            onViewFullTimeline = { selectedRequestForTimeline = req },
            onOpenDisputeClick = { showDisputeModalForRequest = req },
            onUpdateStatusClick = { showUpdateStatusModalForRequest = req }
          )
        }
      }
    }
  }

  // Dialog 1: Detailed Timeline Modal
  if (selectedRequestForTimeline != null) {
    val req = selectedRequestForTimeline!!
    val acceptedBid = bids.find { it.id == req.acceptedBidId } ?: bids.find { it.requestId == req.id && it.status == BidStatus.ACCEPTED }
    val assignedLawyer = if (acceptedBid != null) lawyers.find { it.id == acceptedBid.lawyerId } else null
    val escrow = escrowTransactions.find { it.requestId == req.id }
    val dispute = disputes.find { it.requestId == req.id }

    CaseDetailedTimelineDialog(
      request = req,
      acceptedBid = acceptedBid,
      assignedLawyer = assignedLawyer,
      escrow = escrow,
      dispute = dispute,
      onDismiss = { selectedRequestForTimeline = null }
    )
  }

  // Dialog 2: Open Dispute Modal
  if (showDisputeModalForRequest != null) {
    val req = showDisputeModalForRequest!!
    var disputeReason by remember { mutableStateOf("") }
    var disputeDetails by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showDisputeModalForRequest = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.ReportProblem, contentDescription = null, tint = CrimsonError)
          Text("إحالة القضية للجنة التحكيم وفض النزاع", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
            text = "سيتم تجميد الضمان المالي فوراً وإحالة ملف القضية (#${req.id}) للمستشار المشرف على لجان فض المنازعات والتحكيم للفصل النهائي خلال 24 ساعة.",
            fontSize = 11.5.sp,
            color = MaterialTheme.adaptiveTextSecondary
          )

          OutlinedTextField(
            value = disputeReason,
            onValueChange = { disputeReason = it },
            label = { Text("سبب النزاع الرئيسي") },
            placeholder = { Text("مثال: تأخر في تسليم المذكرة، عدم حضور الجلسة...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )

          OutlinedTextField(
            value = disputeDetails,
            onValueChange = { disputeDetails = it },
            label = { Text("تفاصيل الشكوى والوقائع") },
            placeholder = { Text("اشرح بالتفصيل ما تم وما ترغب فيه من هيئة التحكيم...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(10.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (disputeReason.isNotBlank()) {
              onOpenDispute(req.id, disputeReason, disputeDetails)
              showDisputeModalForRequest = null
            }
          },
          enabled = disputeReason.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
        ) {
          Text("تأكيد وتجميد الضمان ⚖️", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDisputeModalForRequest = null }) {
          Text("إلغاء")
        }
      }
    )
  }

  // Dialog 3: Update Case Status Modal (For testing/simulation & Lawyers/Admins)
  if (showUpdateStatusModalForRequest != null && onUpdateStatus != null) {
    val req = showUpdateStatusModalForRequest!!
    var newStatusSelection by remember { mutableStateOf(req.status) }

    AlertDialog(
      onDismissRequest = { showUpdateStatusModalForRequest = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.EditCalendar, contentDescription = null, tint = NavyPrimary)
          Text("تحديث مسار وحالة القضية", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text("اختر المرحلة الجديدة للقضية #${req.id}:", fontSize = 12.sp, color = MaterialTheme.adaptiveTextSecondary)

          val statuses = listOf(
            RequestStatus.OPEN to "قيد المراجعة وتلقي العروض ⏳",
            RequestStatus.IN_PROGRESS to "جاري العمل ومباشرة الإجراءات ⚡",
            RequestStatus.COMPLETED to "تم الانتهاء وتحرير المخالصة ✓",
            RequestStatus.DISPUTED to "نزاع قيد هيئة التحكيم ⚖️"
          )

          statuses.forEach { (st, label) ->
            Surface(
              color = if (newStatusSelection == st) NavyPrimary.copy(alpha = 0.12f) else MaterialTheme.adaptiveSurface,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, if (newStatusSelection == st) NavyPrimary else MaterialTheme.adaptiveBorder),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { newStatusSelection = st }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
              ) {
                RadioButton(
                  selected = newStatusSelection == st,
                  onClick = { newStatusSelection = st }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, fontSize = 13.sp, fontWeight = if (newStatusSelection == st) FontWeight.Bold else FontWeight.Normal)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onUpdateStatus(req.id, newStatusSelection)
            showUpdateStatusModalForRequest = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("حفظ التحديث ✓")
        }
      },
      dismissButton = {
        TextButton(onClick = { showUpdateStatusModalForRequest = null }) {
          Text("إلغاء")
        }
      }
    )
  }
}

/**
 * شريط المقاييس والإحصائيات العلوية للتتبع
 */
@Composable
private fun TrackerMetricsOverview(
  totalCount: Int,
  underReviewCount: Int,
  inProgressCount: Int,
  completedCount: Int,
  disputedCount: Int,
  onSelectTab: (Int) -> Unit
) {
  LazyRow(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 12.dp),
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      MetricOverviewCard(
        title = "إجمالي القضايا",
        count = totalCount,
        icon = Icons.Default.Folder,
        bgColor = NavyDark,
        accentColor = GoldSecondary,
        onClick = { onSelectTab(0) }
      )
    }
    item {
      MetricOverviewCard(
        title = "قيد المراجعة",
        count = underReviewCount,
        icon = Icons.Default.HourglassTop,
        bgColor = Color(0xFF78350F),
        accentColor = AmberContainer,
        onClick = { onSelectTab(1) }
      )
    }
    item {
      MetricOverviewCard(
        title = "جاري العمل",
        count = inProgressCount,
        icon = Icons.Default.Gavel,
        bgColor = Color(0xFF0C4A6E),
        accentColor = Color(0xFF38BDF8),
        onClick = { onSelectTab(2) }
      )
    }
    item {
      MetricOverviewCard(
        title = "تم الانتهاء",
        count = completedCount,
        icon = Icons.Default.CheckCircle,
        bgColor = Color(0xFF064E3B),
        accentColor = EmeraldContainer,
        onClick = { onSelectTab(3) }
      )
    }
    item {
      MetricOverviewCard(
        title = "نزاع وتحكيم",
        count = disputedCount,
        icon = Icons.Default.ReportProblem,
        bgColor = Color(0xFF7F1D1D),
        accentColor = CrimsonContainer,
        onClick = { onSelectTab(4) }
      )
    }
  }
}

@Composable
private fun MetricOverviewCard(
  title: String,
  count: Int,
  icon: ImageVector,
  bgColor: Color,
  accentColor: Color,
  onClick: () -> Unit
) {
  Surface(
    color = bgColor,
    shape = RoundedCornerShape(14.dp),
    shadowElevation = 2.dp,
    modifier = Modifier
      .width(130.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
        Text(text = count.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
      }
      Text(
        text = title,
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

/**
 * بطاقة تتبع مسار القضية مع شريط التقدم البصري المتدرج
 */
@Composable
fun CaseTrackingCard(
  request: ServiceRequest,
  acceptedBid: Bid?,
  assignedLawyer: Lawyer?,
  escrow: EscrowTransaction?,
  dispute: Dispute?,
  currentUserRole: UserRole,
  onRequestClick: () -> Unit,
  onOpenWorkspace: () -> Unit,
  onOpenEscrow: () -> Unit,
  onViewFullTimeline: () -> Unit,
  onOpenDisputeClick: () -> Unit,
  onUpdateStatusClick: () -> Unit
) {
  var isExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(
      1.dp,
      when (request.status) {
        RequestStatus.DISPUTED -> CrimsonError.copy(alpha = 0.5f)
        RequestStatus.IN_PROGRESS -> GoldSecondary.copy(alpha = 0.5f)
        RequestStatus.COMPLETED -> EmeraldSuccess.copy(alpha = 0.5f)
        else -> MaterialTheme.adaptiveBorder
      }
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. Header: ID, Date, Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            color = NavyPrimary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "#${request.id}",
              color = NavyPrimary,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          CategoryChip(category = request.category)
        }

        StatusBadge(status = request.status)
      }

      // 2. Request Title & Jurisdiction
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = request.title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.adaptiveTextPrimary,
          lineHeight = 20.sp
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldDark, modifier = Modifier.size(13.dp))
          Text(
            text = "${request.city} • ${request.courtLocation?.courtJurisdiction ?: "المحكمة المختصة"}",
            fontSize = 11.sp,
            color = MaterialTheme.adaptiveTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // 3. Visual Progress Bar & Milestones Stepper (شريط التقدم البصري)
      VisualCaseProgressStepper(
        requestStatus = request.status,
        hasBids = request.bidsCount > 0,
        hasAcceptedBid = acceptedBid != null,
        isEscrowFunded = escrow != null,
        isDisputed = request.status == RequestStatus.DISPUTED
      )

      // 4. Financial & Assigned Lawyer Info Pill
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(NavyPrimary.copy(alpha = 0.04f))
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Financial summary
        Column {
          Text(
            text = if (acceptedBid != null) "قيمة التعاقد والأمانة" else "حالة العروض المالية",
            fontSize = 10.sp,
            color = MaterialTheme.adaptiveTextSecondary
          )
          Text(
            text = if (acceptedBid != null) "${acceptedBid.grandTotalAmount.toInt()} ج.م" else if (request.bidsCount > 0) "${request.bidsCount} عروض مقدمة" else "بانتظار العروض",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (acceptedBid != null) EmeraldSuccess else NavyPrimary
          )
        }

        // Assigned Lawyer or Bids info
        if (assignedLawyer != null) {
          Column(horizontalAlignment = Alignment.End) {
            Text(text = "المحامي المباشر", fontSize = 10.sp, color = MaterialTheme.adaptiveTextSecondary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = assignedLawyer.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.adaptiveTextPrimary,
                maxLines = 1
              )
              Surface(
                color = EmeraldContainer,
                shape = CircleShape
              ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(10.dp))
              }
            }
          }
        } else {
          Column(horizontalAlignment = Alignment.End) {
            Text(text = "عروض المحامين", fontSize = 10.sp, color = MaterialTheme.adaptiveTextSecondary)
            Text(
              text = if (request.bidsCount > 0) "${request.bidsCount} عروض مقدمة" else "بانتظار العروض",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (request.bidsCount > 0) GoldDark else MaterialTheme.adaptiveTextSecondary
            )
          }
        }
      }

      // 5. Dispute Alert Box (if disputed)
      if (request.status == RequestStatus.DISPUTED && dispute != null) {
        Surface(
          color = CrimsonContainer.copy(alpha = 0.5f),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.4f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Gavel, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(15.dp))
              Text(
                text = "القضية مجمدة ومحالة لهيئة التحكيم وفض المنازعات",
                color = CrimsonError,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = "السبب: ${dispute.reason}",
              color = MaterialTheme.adaptiveTextPrimary,
              fontSize = 11.sp
            )
            if (!dispute.adminNote.isNullOrBlank()) {
              Text(
                text = "قرار التحكيم: ${dispute.adminNote}",
                color = NavyDark,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }

      // 6. Action Buttons Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Primary Action: View Details
        Button(
          onClick = onRequestClick,
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("تفاصيل القضية", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        }

        // Secondary Action: Chat/Workspace if active, or Escrow
        if (request.status == RequestStatus.IN_PROGRESS || request.status == RequestStatus.COMPLETED) {
          OutlinedButton(
            onClick = onOpenWorkspace,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
            border = BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.4f))
          ) {
            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("مساحة العمل", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
        } else {
          OutlinedButton(
            onClick = onOpenEscrow,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
            border = BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.4f))
          ) {
            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("الضمان", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
        }

        // More options dropdown / icon button
        IconButton(
          onClick = onViewFullTimeline,
          modifier = Modifier.size(38.dp)
        ) {
          Icon(Icons.Default.Timeline, contentDescription = "مسار الإجراءات", tint = GoldDark)
        }

        // Dispute or update options
        if (request.status == RequestStatus.IN_PROGRESS) {
          IconButton(
            onClick = onOpenDisputeClick,
            modifier = Modifier.size(38.dp)
          ) {
            Icon(Icons.Default.ReportProblem, contentDescription = "فتح نزاع", tint = CrimsonError)
          }
        }

        // Admin/Lawyer status update tool for simulation & live progress
        if (currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.LAWYER) {
          IconButton(
            onClick = onUpdateStatusClick,
            modifier = Modifier.size(38.dp)
          ) {
            Icon(Icons.Default.EditCalendar, contentDescription = "تحديث المرحلة", tint = NavyPrimary)
          }
        }
      }
    }
  }
}

/**
 * شريط التقدم البصري لمراحل القضية (Visual Progress Bar Stepper)
 */
@Composable
fun VisualCaseProgressStepper(
  requestStatus: RequestStatus,
  hasBids: Boolean,
  hasAcceptedBid: Boolean,
  isEscrowFunded: Boolean,
  isDisputed: Boolean
) {
  // Determine step active & completed states
  // Steps:
  // 1: تقديم الطلب وتدقيق المستندات
  // 2: المراجعة وتلقي العروض
  // 3: قبول العرض وإيداع الضمان
  // 4: مباشرة العمل والإجراءات القضائية
  // 5: إنجاز المهمة والاعتماد النهائي / فض النزاع

  val (progressFraction, activeStepIndex, currentStageLabel, currentStageDesc) = when (requestStatus) {
    RequestStatus.OPEN -> {
      if (hasBids) {
        Quadruple(0.35f, 2, "المرحلة ٢: تلقي وفحص عروض المحامين", "تم استلام عروض أسعار من محامين معتمدين لاختيار الأنسب وتأمين الأتعاب.")
      } else {
        Quadruple(0.20f, 1, "المرحلة ١: مراجعة الطلب والاختصاص الجغرافي", "تم تسجيل طلبك وتوثيق الهوية الرقمية وبانتظار تقديم المحامين لعروضهم.")
      }
    }
    RequestStatus.NEGOTIATING -> {
      Quadruple(0.40f, 2, "المرحلة ٢: التفاوض ومراجعة الاشتراطات", "جاري دراسة تفاصيل العروض والمصاريف القضائية تمهيداً للاعتماد.")
    }
    RequestStatus.IN_PROGRESS -> {
      Quadruple(0.75f, 4, "المرحلة ٤: جاري مباشرة العمل والإجراءات القضائية", "الأمانة محفوظة بحساب الضمان المالي والمحامي يباشر كتابة المذكرات وحضور الجلسات.")
    }
    RequestStatus.COMPLETED -> {
      Quadruple(1.0f, 5, "المرحلة ٥: تم الانتهاء بنجاح وتحرير المخالصة", "تم استلام الخدمة القانونية كاملة وتحرير أتعاب المحامي وإصدار الشهادة الرسمية.")
    }
    RequestStatus.DISPUTED -> {
      Quadruple(0.70f, 4, "المرحلة ٤: نزاع قيد التحكيم والتجميد", "تم تجميد الضمان المالي لحين صدور قرار ملزم من هيئة التحكيم وفض المنازعات.")
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // 1. Progress Bar percentage header & Current Stage badge
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(
              if (isDisputed) CrimsonError
              else if (requestStatus == RequestStatus.COMPLETED) EmeraldSuccess
              else GoldSecondary
            )
        )
        Text(
          text = currentStageLabel,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = if (isDisputed) CrimsonError else NavyPrimary
        )
      }

      Surface(
        color = if (isDisputed) CrimsonContainer else if (requestStatus == RequestStatus.COMPLETED) EmeraldContainer else GoldContainer,
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(
          text = if (isDisputed) "⚠️ مجمد" else "${(progressFraction * 100).toInt()}% مكتمل",
          fontSize = 10.5.sp,
          fontWeight = FontWeight.Bold,
          color = if (isDisputed) CrimsonError else if (requestStatus == RequestStatus.COMPLETED) Color(0xFF047857) else GoldDark,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
      }
    }

    // 2. Continuous Graphical Progress Bar Line
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(MaterialTheme.adaptiveBorder)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(progressFraction)
          .fillMaxHeight()
          .clip(RoundedCornerShape(3.dp))
          .background(
            if (isDisputed) Brush.horizontalGradient(listOf(CrimsonError, Color(0xFFE11D48)))
            else if (requestStatus == RequestStatus.COMPLETED) Brush.horizontalGradient(listOf(EmeraldSuccess, Color(0xFF10B981)))
            else Brush.horizontalGradient(listOf(GoldSecondary, GoldDark, NavyPrimary))
          )
      )
    }

    // 3. Multi-stage visual step nodes (5 nodes)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      val stepNames = listOf(
        "التقديم",
        "العروض",
        "الضمان",
        "التنفيذ",
        if (isDisputed) "التحكيم" else "الإنجاز"
      )

      stepNames.forEachIndexed { index, name ->
        val stepNum = index + 1
        val isCompleted = stepNum < activeStepIndex || (stepNum == 5 && requestStatus == RequestStatus.COMPLETED)
        val isCurrent = stepNum == activeStepIndex && requestStatus != RequestStatus.COMPLETED
        val isDisputedNode = isDisputed && stepNum >= 4

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Surface(
            modifier = Modifier.size(22.dp),
            shape = CircleShape,
            color = when {
              isDisputedNode -> CrimsonError
              isCompleted -> EmeraldSuccess
              isCurrent -> GoldSecondary
              else -> MaterialTheme.adaptiveBorder.copy(alpha = 0.5f)
            },
            border = if (isCurrent) BorderStroke(2.dp, NavyPrimary) else null
          ) {
            Box(contentAlignment = Alignment.Center) {
              when {
                isDisputedNode -> Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                isCompleted -> Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                isCurrent -> Text(text = "$stepNum", color = NavyDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                else -> Text(text = "$stepNum", color = Color.Gray, fontSize = 9.sp)
              }
            }
          }

          Text(
            text = name,
            fontSize = 9.5.sp,
            fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) NavyPrimary else if (isCompleted) Color(0xFF047857) else MaterialTheme.adaptiveTextSecondary
          )
        }
      }
    }

    // 4. Current Stage Descriptive Note
    Surface(
      color = MaterialTheme.adaptiveBorder.copy(alpha = 0.25f),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = currentStageDesc,
        fontSize = 10.5.sp,
        color = MaterialTheme.adaptiveTextSecondary,
        lineHeight = 15.sp,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
      )
    }
  }
}

/**
 * نافذة سجل المراحل التفصيلية للقضية (Audit Trail & Milestones Modal)
 */
@Composable
fun CaseDetailedTimelineDialog(
  request: ServiceRequest,
  acceptedBid: Bid?,
  assignedLawyer: Lawyer?,
  escrow: EscrowTransaction?,
  dispute: Dispute?,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Icon(Icons.Default.Timeline, contentDescription = null, tint = GoldSecondary)
          Text("مسار وسجل إجراءات القضية", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = "طلب #${request.id} • ${request.title}", fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary, maxLines = 1)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Timeline Items
        TimelineStepItem(
          stepNumber = "١",
          title = "تقديم الطلب والتدقيق الرقمي",
          subtitle = "تم تسجيل الطلب وتوثيق الهوية الرقمية (KYC) وتحديد الاختصاص المكاني بمحافظة ${request.city}.",
          date = request.createdAt,
          isCompleted = true,
          statusIcon = Icons.Default.CheckCircle,
          statusColor = EmeraldSuccess
        )

        TimelineStepItem(
          stepNumber = "٢",
          title = "فحص الطلب وتلقي عروض المحامين",
          subtitle = if (request.bidsCount > 0) "تم استلام ${request.bidsCount} عروض أسعار من محامين مقيدين ومطابقين لاختصاص المحكمة." else "بانتظار تقديم عروض الأسعار من المحامين المعتمدين.",
          date = if (request.bidsCount > 0) "مكتمل" else "جاري",
          isCompleted = request.bidsCount > 0 || acceptedBid != null,
          statusIcon = if (request.bidsCount > 0) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
          statusColor = if (request.bidsCount > 0) EmeraldSuccess else GoldDark
        )

        TimelineStepItem(
          stepNumber = "٣",
          title = "قبول العرض وتأمين الضمان المالي",
          subtitle = if (acceptedBid != null) "تم قبول عرض ${acceptedBid.lawyerName} وتأمين مبلغ ${acceptedBid.grandTotalAmount.toInt()} ج.م بحساب الضمان (المرجع: ${escrow?.referenceNumber ?: "MTR-ESC-9102"})." else "لم يتم اعتماد أي عرض بعد.",
          date = acceptedBid?.createdAt ?: "قيد الانتظار",
          isCompleted = acceptedBid != null && escrow != null,
          statusIcon = if (escrow != null) Icons.Default.VerifiedUser else Icons.Default.LockClock,
          statusColor = if (escrow != null) EmeraldSuccess else Color.Gray
        )

        TimelineStepItem(
          stepNumber = "٤",
          title = "مباشرة الإجراءات والعمل القضائي",
          subtitle = when (request.status) {
            RequestStatus.IN_PROGRESS -> "جاري صياغة المذكرات وحضور الجلسات ومتابعة القضية بمحكمة ${request.courtLocation?.courtJurisdiction ?: request.city}."
            RequestStatus.COMPLETED -> "تم إنجاز كافة الإجراءات القانونية والمرافعات بنجاح."
            RequestStatus.DISPUTED -> "تم تجميد القضية وإحالتها للجنة التحكيم لوجود خلاف إجرائي."
            else -> "تبدأ هذه المرحلة فور إيداع الضمان المالي."
          },
          date = if (request.status == RequestStatus.IN_PROGRESS || request.status == RequestStatus.COMPLETED) "مباشرة مستمرة" else "--",
          isCompleted = request.status == RequestStatus.COMPLETED,
          statusIcon = if (request.status == RequestStatus.COMPLETED) Icons.Default.CheckCircle else if (request.status == RequestStatus.DISPUTED) Icons.Default.ReportProblem else Icons.Default.AccountBalance,
          statusColor = if (request.status == RequestStatus.COMPLETED) EmeraldSuccess else if (request.status == RequestStatus.DISPUTED) CrimsonError else NavyPrimary
        )

        TimelineStepItem(
          stepNumber = "٥",
          title = "التسليم والاعتماد النهائي والمخالصة",
          subtitle = if (request.status == RequestStatus.COMPLETED) "تم استلام الخدمة وتحرير الأتعاب بنجاح وإصدار شهادة المخالصة الرسمية." else "تُحرر الأتعاب وتصدر المخالصة بموافقة العميل التامة على الإنجاز.",
          date = if (request.status == RequestStatus.COMPLETED) "تم الإنجاز ✓" else "قيد الانتظار",
          isCompleted = request.status == RequestStatus.COMPLETED,
          statusIcon = if (request.status == RequestStatus.COMPLETED) Icons.Default.EmojiEvents else Icons.Default.RadioButtonUnchecked,
          statusColor = if (request.status == RequestStatus.COMPLETED) EmeraldSuccess else Color.Gray,
          isLast = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
      ) {
        Text("إغلاق")
      }
    }
  )
}

@Composable
private fun TimelineStepItem(
  stepNumber: String,
  title: String,
  subtitle: String,
  date: String,
  isCompleted: Boolean,
  statusIcon: ImageVector,
  statusColor: Color,
  isLast: Boolean = false
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Surface(
        modifier = Modifier.size(28.dp),
        shape = CircleShape,
        color = statusColor.copy(alpha = 0.15f),
        border = BorderStroke(1.5.dp, statusColor)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
        }
      }

      if (!isLast) {
        Box(
          modifier = Modifier
            .width(2.dp)
            .height(42.dp)
            .background(if (isCompleted) statusColor.copy(alpha = 0.4f) else MaterialTheme.adaptiveBorder)
        )
      }
    }

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = title, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
        Text(text = date, fontSize = 10.sp, color = GoldDark, fontWeight = FontWeight.Medium)
      }
      Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary, lineHeight = 15.sp)
    }
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
