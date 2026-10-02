package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.data.MaitreRepository
import com.example.ui.components.EscrowStatusBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.LawyerMeetingQrCard
import com.example.ui.components.ClientStartWorkCameraBanner
import com.example.ui.components.MeetingQrScannerDialog
import com.example.ui.components.DisbursementSection
import com.example.ui.components.RequestDisbursementDialog
import com.example.ui.components.ModifyDisbursementDialog
import com.example.ui.components.RejectDisbursementDialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
  request: ServiceRequest,
  bids: List<Bid>,
  escrow: EscrowTransaction?,
  currentUser: UserProfile,
  clientFeePercentage: Double = 5.0,
  lawyerFeePercentage: Double = 5.0,
  platformFeePercentage: Double = clientFeePercentage + lawyerFeePercentage,
  onBackClick: () -> Unit,
  onAcceptBid: (bidId: String, paymentMethod: String) -> Unit,
  onSubmitBid: (lawyerFee: Double, legalExpenses: Double, days: Int, note: String) -> Unit,
  onOpenChat: () -> Unit,
  onReleaseEscrow: () -> Unit,
  onOpenDispute: () -> Unit,
  onRateLawyer: (lawyerId: String) -> Unit
) {
  var showAcceptPaymentDialog by remember { mutableStateOf(false) }
  var selectedBidToAccept by remember { mutableStateOf<Bid?>(null) }
  var selectedPaymentMethod by remember { mutableStateOf("إنستاباي InstaPay") }

  var showLawyerBidDialog by remember { mutableStateOf(false) }
  var lawyerProposedFee by remember { mutableStateOf("3000") }
  var lawyerProposedExpenses by remember { mutableStateOf("500") }
  var lawyerProposedDays by remember { mutableStateOf("4") }
  var lawyerProposalNote by remember { mutableStateOf("") }

  var showReleaseConfirmDialog by remember { mutableStateOf(false) }
  var showRatingDialog by remember { mutableStateOf(false) }

  var showMeetingScannerDialog by remember { mutableStateOf(false) }
  var showDisbursementDialog by remember { mutableStateOf(false) }
  var modifyingDisbursement by remember { mutableStateOf<DisbursementRequest?>(null) }
  var rejectingDisbursement by remember { mutableStateOf<DisbursementRequest?>(null) }

  val allDisbursements by MaitreRepository.disbursementRequests.collectAsState()
  val caseDisbursements = allDisbursements.filter { it.requestId == request.id }

  val lawyersList by MaitreRepository.lawyers.collectAsState()
  val reviewsList by MaitreRepository.reviews.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "تفاصيل القضية والعروض",
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
        actions = {
          if (request.status == RequestStatus.IN_PROGRESS || request.status == RequestStatus.COMPLETED) {
            IconButton(onClick = onOpenChat) {
              Icon(Icons.Default.Chat, contentDescription = "مساحة العمل والدردشة", tint = GoldLight)
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
      // 1. Case Status & Escrow Alert
      item {
        Surface(
          color = MaterialTheme.adaptiveSurface,
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
          shadowElevation = 1.dp,
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
              StatusBadge(status = request.status)
              Surface(
                color = NavyContainer,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = request.category.titleAr,
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Text(
              text = request.title,
              color = TextPrimary,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = request.city, color = TextSecondary, fontSize = 12.sp)
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = request.createdAt, color = TextSecondary, fontSize = 12.sp)
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = request.urgency.labelAr, color = TextSecondary, fontSize = 12.sp)
              }
            }

            Divider(color = BorderSubtle, thickness = 0.8.dp)

            Text(
              text = "شرح القضية والوقائع:",
              color = TextPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )

            Text(
              text = request.description,
              color = TextSecondary,
              fontSize = 13.sp,
              lineHeight = 20.sp
            )

            // Court and Incident/Case location info
            request.courtLocation?.let { loc ->
              Surface(
                color = CreamBackground,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(18.dp))
                    Text(
                      text = "${loc.locationPurpose}: ${loc.city} - ${loc.district}",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = NavyDark
                    )
                  }

                  if (loc.specificLandmark.isNotBlank()) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                      modifier = Modifier.padding(start = 26.dp)
                    ) {
                      Icon(Icons.Default.Place, contentDescription = null, tint = GoldDark, modifier = Modifier.size(13.dp))
                      Text(
                        text = "المقر المحدد: ${loc.specificLandmark}",
                        fontSize = 11.sp,
                        color = TextPrimary
                      )
                    }
                  }

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(start = 26.dp)
                  ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(13.dp))
                    Text(
                      text = "الجهة القضائية / النيابة: ${loc.courtJurisdiction}",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Medium,
                      color = TextSecondary
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 2. Escrow Status Card (if IN_PROGRESS or COMPLETED)
      if (escrow != null && (request.status == RequestStatus.IN_PROGRESS || request.status == RequestStatus.COMPLETED)) {
        item {
          Surface(
            color = if (escrow.status == EscrowStatus.HELD) GoldContainer else EmeraldContainer,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (escrow.status == EscrowStatus.HELD) GoldSecondary else EmeraldSuccess),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(
                    imageVector = if (escrow.status == EscrowStatus.HELD) Icons.Default.Lock else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (escrow.status == EscrowStatus.HELD) GoldDark else EmeraldSuccess
                  )
                  Text(
                    text = "محفظة الضمان المالي لمِتر",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (escrow.status == EscrowStatus.HELD) GoldOnContainer else Color(0xFF047857)
                  )
                }
                EscrowStatusBadge(status = escrow.status)
              }

              Text(
                text = if (escrow.status == EscrowStatus.HELD)
                  "المبلغ الإجمالي (${escrow.totalAmount.toInt()} ج.م) محتجز بأمان في حساب الضمان، وسيتم تحرير أتعاب المحامي بعد مراجعة واعتماد إنجاز العمل."
                else
                  "تم تحرير الأتعاب بنجاح لحساب المحامي بعد إتمام المهمة القانونية بالكامل.",
                fontSize = 12.sp,
                color = TextPrimary,
                lineHeight = 18.sp
              )

              if (escrow.status == EscrowStatus.HELD && currentUser.role == UserRole.CLIENT) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Button(
                    onClick = { showReleaseConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تحرير الأتعاب", fontSize = 12.sp)
                  }

                  OutlinedButton(
                    onClick = onOpenDispute,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نزاع", fontSize = 12.sp)
                  }
                }
              } else if (request.status == RequestStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(6.dp))
                val acceptedBid = bids.find { it.id == request.acceptedBidId }
                if (acceptedBid != null && currentUser.role == UserRole.CLIENT) {
                  Button(
                    onClick = { showRatingDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تقييم المحامي وكتابة الملاحظات", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                }
              }
            }
          }
        }
      }

      // 2.5 Post-Service Client Rating & Feedback Component (Stored in Firestore)
      if (request.status == RequestStatus.COMPLETED && currentUser.role == UserRole.CLIENT) {
        val acceptedBid = bids.find { it.id == request.acceptedBidId }
        if (acceptedBid != null) {
          val assignedLawyer = lawyersList.find { it.id == acceptedBid.lawyerId }
          val existingReview = reviewsList.find { it.lawyerId == acceptedBid.lawyerId && (it.clientName == currentUser.name || it.lawyerId == acceptedBid.lawyerId) }

          item {
            PostServiceRatingCard(
              lawyer = assignedLawyer,
              existingReview = existingReview,
              onSubmitRating = { stars, comment, tags ->
                MaitreRepository.submitReview(
                  lawyerId = acceptedBid.lawyerId,
                  stars = stars,
                  comment = comment,
                  tags = tags,
                  requestId = request.id
                )
              }
            )
          }
        }
      }

      // 2.7 Meeting Confirmation via QR & Start Work Camera (Requirement #2)
      if (request.status == RequestStatus.IN_PROGRESS || request.status == RequestStatus.COMPLETED || request.acceptedBidId != null) {
        // بطاقة إظهار بيانات المحامي الكاملة (الاسم الكامل، العنوان، الهاتف، الموقع الجغرافي) عند الاتفاق
        val acceptedBid = bids.find { it.id == request.acceptedBidId }
        val assignedLawyer = lawyersList.find { it.id == (acceptedBid?.lawyerId ?: "") }
        if (acceptedBid != null) {
          item {
            Surface(
              color = EmeraldContainer.copy(alpha = 0.35f),
              shape = RoundedCornerShape(16.dp),
              border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldSuccess),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldSuccess)
                    Text(
                      text = "بيانات المحامي الموكل (تم التعاقد والاتفاق ✓)",
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.5.sp,
                      color = EmeraldSuccess
                    )
                  }
                  Surface(color = EmeraldSuccess, shape = RoundedCornerShape(6.dp)) {
                    Text(
                      text = "متاح للتواصل",
                      color = Color.White,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }

                Divider(color = EmeraldSuccess.copy(alpha = 0.25f))

                // 1. الاسم بالكامل
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                  Column {
                    Text("الاسم الكامل للمحامي:", fontSize = 10.sp, color = TextSecondary)
                    Text(
                      text = assignedLawyer?.getFullDisplayName() ?: acceptedBid.getFullDisplayName(),
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      color = TextPrimary
                    )
                  }
                }

                // 2. الهاتف والواتساب
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Default.Phone, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                  Column {
                    Text("رقم الهاتف المباشر والتواصل المهني:", fontSize = 10.sp, color = TextSecondary)
                    Text(
                      text = assignedLawyer?.phone ?: "+20 111 987 6543",
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = TextPrimary
                    )
                  }
                }

                // 3. عنوان المكتب يدوياً
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(18.dp))
                  Column {
                    Text("عنوان المكتب المعتمد:", fontSize = 10.sp, color = TextSecondary)
                    Text(
                      text = assignedLawyer?.officeAddressManually ?: "القاهرة - مصر الجديدة - شارع الأهرام - برج مِتر القانوني",
                      fontWeight = FontWeight.Medium,
                      fontSize = 12.sp,
                      color = TextPrimary
                    )
                  }
                }

                // 4. الموقع الجغرافي (GPS Location)
                val lat = assignedLawyer?.officeLatitude ?: 30.0911
                val lng = assignedLawyer?.officeLongitude ?: 31.3253
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Default.Place, contentDescription = null, tint = GoldDark, modifier = Modifier.size(18.dp))
                  Column {
                    Text("الموقع الجغرافي للمكتب (الإحداثيات):", fontSize = 10.sp, color = TextSecondary)
                    Text(
                      text = "خط العرض: ${String.format(java.util.Locale.US, "%.4f", lat)} • خط الطول: ${String.format(java.util.Locale.US, "%.4f", lng)}",
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 11.sp,
                      color = NavyPrimary
                    )
                  }
                }
              }
            }
          }
        }

        item {
          if (currentUser.role == UserRole.LAWYER) {
            LawyerMeetingQrCard(request = request)
          } else if (currentUser.role == UserRole.CLIENT) {
            ClientStartWorkCameraBanner(
              request = request,
              onOpenScanner = { showMeetingScannerDialog = true }
            )
          }
        }
      }

      // 2.8 Partial Payment & Judicial Expenses (Requirement #3)
      if (request.status == RequestStatus.IN_PROGRESS || request.status == RequestStatus.COMPLETED) {
        item {
          DisbursementSection(
            disbursements = caseDisbursements,
            currentUser = currentUser,
            isRequestActive = request.status == RequestStatus.IN_PROGRESS,
            onRequestDisbursementClick = { showDisbursementDialog = true },
            onAcceptDisbursement = { disbId -> MaitreRepository.acceptDisbursement(disbId) },
            onModifyDisbursementClick = { disb -> modifyingDisbursement = disb },
            onRejectDisbursementClick = { disb -> rejectingDisbursement = disb },
            onLawyerAcceptCounter = { disbId -> MaitreRepository.lawyerAcceptCounterDisbursement(disbId) }
          )
        }
      }

      // 3. Lawyer Submit Bid Action (if in Lawyer mode and request is OPEN)
      if (currentUser.role == UserRole.LAWYER && request.status == RequestStatus.OPEN) {
        item {
          Surface(
            color = NavyPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldLight)
                Text("هل ترغب في تولي هذه القضية؟", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              }
              Text(
                text = "بصفتك محامياً مقيداً (${currentUser.barDegree.formalTitleAr})، يمكنك تقديم عرض مفصل يشمل أتعابك والمصاريف القانونية المقدرة.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp
              )
              Button(
                onClick = { showLawyerBidDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تقديم عرض سعر قانوني مفصل", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // 4. Bids List Section (Requirement #1: Hide other lawyers' bids in lawyer screen)
      val isLawyer = currentUser.role == UserRole.LAWYER
      val lawyerOwnBids = if (isLawyer) bids.filter { it.lawyerId == currentUser.id || it.lawyerName == currentUser.name } else bids

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isLawyer) "عرضك القانوني المقدم" else "عروض المحامين المقدمة (${bids.size})",
            color = MaterialTheme.adaptiveTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )

          if (isLawyer) {
            Surface(
              color = GoldContainer,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "🔒 سرية المنافسة مفعلة",
                color = GoldDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          } else if (request.status == RequestStatus.OPEN) {
            Surface(
              color = GoldContainer,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "تنافس شفاف معتمد",
                color = GoldDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      if (isLawyer) {
        // Lawyer view: ONLY show own bid, never other lawyers' bids
        if (lawyerOwnBids.isEmpty()) {
          item {
            Surface(
              color = CreamSurfaceVariant,
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Default.Lock, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
                  Text(
                    text = "سرية العروض بين المحامين",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NavyDark
                  )
                }
                Text(
                  text = "احتراماً لأخلاقيات مهنة المحاماة وقواعد المنافسة الشريفة، عروض الزملاء الآخرين سرية بالكامل ومحجوبة عن شاشات المحامين المنافسين.",
                  fontSize = 12.sp,
                  color = TextPrimary,
                  lineHeight = 16.sp
                )
                Text(
                  text = "• إجمالي العروض المستلمة من الموكل حتى الآن: ${bids.size} عروض (بدون كشف تفاصيل المنافسين).",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = TextSecondary
                )
                if (request.status == RequestStatus.OPEN) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Button(
                    onClick = { showLawyerBidDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تقديم عرضك القانوني لهذه القضية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        } else {
          // Lawyer has submitted a bid: display only their bid
          items(lawyerOwnBids) { bid ->
            BidCardItem(
              bid = bid,
              isRequestOpen = request.status == RequestStatus.OPEN,
              isClient = false,
              onAccept = {}
            )
          }

          item {
            Surface(
              color = NavyDark.copy(alpha = 0.05f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
                Text(
                  text = "عروض الزملاء الآخرين محجوبة وسرية بالكامل طبقاً للائحة الخصوصية والمنافسة المهنية.",
                  fontSize = 11.sp,
                  color = TextSecondary
                )
              }
            }
          }
        }
      } else {
        // Client / Admin view: show all bids received
        if (bids.isEmpty()) {
          item {
            Surface(
              color = CreamSurface,
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Outlined.Gavel, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                Text("لم يتم تقديم أي عروض بعد", color = TextSecondary, fontSize = 13.sp)
                Text("سيتم إشعارك فور قيام أحد المحامين المعتمدين بتقديم عرضه.", color = TextMuted, fontSize = 11.sp)
              }
            }
          }
        } else {
          items(bids) { bid ->
            BidCardItem(
              bid = bid,
              isRequestOpen = request.status == RequestStatus.OPEN,
              isClient = currentUser.role == UserRole.CLIENT,
              onAccept = {
                selectedBidToAccept = bid
                showAcceptPaymentDialog = true
              }
            )
          }
        }
      }
    }
  }

  // DIALOG: Lawyer Submit Bid (Requirement #3)
  if (showLawyerBidDialog) {
    val feeVal = lawyerProposedFee.toDoubleOrNull() ?: 0.0
    val expVal = lawyerProposedExpenses.toDoubleOrNull() ?: 0.0
    val baseTotal = feeVal + expVal
    val clientFeeVal = if (clientFeePercentage > 0.0) baseTotal * (clientFeePercentage / 100.0) else 0.0
    val grandTotal = baseTotal + clientFeeVal

    AlertDialog(
      onDismissRequest = { showLawyerBidDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Gavel, contentDescription = null, tint = NavyPrimary)
          Text("تقديم عرض سعر قانوني مفصل", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "درجة القيد الخاصة بك: ${currentUser.barDegree.formalTitleAr}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )

          request.courtLocation?.let { loc ->
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
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(16.dp))
                Column {
                  Text(
                    text = "مكان الواقعة/الاختصاص: ${loc.city} - ${loc.district}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                  )
                  Text(
                    text = "${loc.courtJurisdiction}${if (loc.specificLandmark.isNotBlank()) " • ${loc.specificLandmark}" else ""}",
                    fontSize = 10.sp,
                    color = TextSecondary
                  )
                }
              }
            }
          }

          // 1. Lawyer Fee Input (خانة أتعاب المحامي)
          Column {
            Text("1. خانة أتعاب المحامي (ج.م) *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = lawyerProposedFee,
              onValueChange = { lawyerProposedFee = it },
              placeholder = { Text("مثال: 3000") },
              shape = RoundedCornerShape(8.dp),
              colors = maitreTextFieldColors(),
              modifier = Modifier.fillMaxWidth()
            )
          }

          // 2. Legal Expenses Input (خانة المصاريف القانونية المقدرة)
          Column {
            Text("2. خانة المصاريف القانونية المقدرة إن وجدت (ج.م)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
            Text("تشمل رسوم أمانات المحكمة، الشهر العقاري، انتقال المحضرين، والإعلانات القضائية", fontSize = 10.sp, color = MaterialTheme.adaptiveTextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = lawyerProposedExpenses,
              onValueChange = { lawyerProposedExpenses = it },
              placeholder = { Text("مثال: 500 (أو 0 إذا لم توجد)") },
              shape = RoundedCornerShape(8.dp),
              colors = maitreTextFieldColors(),
              modifier = Modifier.fillMaxWidth()
            )
          }

          // 3. Execution Days
          Column {
            Text("3. مدة الإنجاز المتوقعة (بالأيام) *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = lawyerProposedDays,
              onValueChange = { lawyerProposedDays = it },
              shape = RoundedCornerShape(8.dp),
              colors = maitreTextFieldColors(),
              modifier = Modifier.fillMaxWidth()
            )
          }

          // 4. Scope Note
          Column {
            Text("4. شرح نطاق التمثيل وخطة العمل *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = lawyerProposalNote,
              onValueChange = { lawyerProposalNote = it },
              placeholder = { Text("مثال: دراسة القضية، صياغة المذكرة، حضور الجلسات ومراجعة العقد...", fontSize = 12.sp) },
              minLines = 3,
              shape = RoundedCornerShape(8.dp),
              colors = maitreTextFieldColors(),
              modifier = Modifier.fillMaxWidth()
            )
          }

          // Live Calculation Breakdown Box
          Surface(
            color = GoldContainer.copy(alpha = 0.6f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("📊 تفصيل العرض المالي:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GoldOnContainer)
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("أتعاب المحامي:", fontSize = 11.sp, color = TextPrimary)
                Text("${feeVal.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }
              if (expVal > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("المصاريف القضائية المقدرة:", fontSize = 11.sp, color = TextPrimary)
                  Text("${expVal.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
              }
              Divider(color = GoldSecondary.copy(alpha = 0.5f), thickness = 0.8.dp)
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("إجمالي المبلغ المعروض للعميل:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Text("${grandTotal.toInt()} ج.م", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val fee = lawyerProposedFee.toDoubleOrNull() ?: 3000.0
            val exp = lawyerProposedExpenses.toDoubleOrNull() ?: 0.0
            val days = lawyerProposedDays.toIntOrNull() ?: 5
            val note = if (lawyerProposalNote.isBlank()) "أتشرف بتقديم العرض القانوني الشامل." else lawyerProposalNote
            onSubmitBid(fee, exp, days, note)
            showLawyerBidDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("إرسال العرض للعميل")
        }
      },
      dismissButton = {
        TextButton(onClick = { showLawyerBidDialog = false }) {
          Text("إلغاء")
        }
      }
    )
  }

  // DIALOG: Escrow Deposit & Bid Acceptance
  if (showAcceptPaymentDialog && selectedBidToAccept != null) {
    val bid = selectedBidToAccept!!

    AlertDialog(
      onDismissRequest = { showAcceptPaymentDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldSuccess)
          Text("إيداع الأتعاب بمحفظة الضمان", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "أنت على وشك قبول عرض المحامي: ${bid.lawyerName} (${bid.lawyerDegree.formalTitleAr}) وبدء تنفيذ القضية تحت حماية محفظة متر.",
            fontSize = 12.sp,
            color = TextSecondary
          )

          Surface(
            color = CreamSurfaceVariant,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("أتعاب المحامي:", fontSize = 12.sp, color = TextSecondary)
                Text("${bid.lawyerFee.toInt()} ج.م", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
              if (bid.legalExpenses > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("المصاريف القانونية والقضائية المقدرة:", fontSize = 11.sp, color = TextSecondary)
                  Text("${bid.legalExpenses.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
              }
              Divider(color = BorderSubtle)
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("المجموع الإجمالي المعروض للعميل:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Text("${bid.grandTotalAmount.toInt()} ج.م", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              }
            }
          }

          // Coupon Code Input Section (إتاحة كود خصم في المنصة)
          var couponInput by remember { mutableStateOf("") }
          var appliedCoupon by remember { mutableStateOf<DiscountCoupon?>(null) }
          var couponDiscountAmount by remember { mutableDoubleStateOf(0.0) }
          var couponError by remember { mutableStateOf<String?>(null) }
          var couponSuccessMessage by remember { mutableStateOf<String?>(null) }

          Surface(
            color = CreamSurface,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("هل لديك كود خصم أو بروموكود؟", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedTextField(
                  value = couponInput,
                  onValueChange = {
                    couponInput = it.uppercase()
                    couponError = null
                  },
                  placeholder = { Text("رمز الكود (مثال: WELCOME2026)", fontSize = 10.sp) },
                  shape = RoundedCornerShape(8.dp),
                  colors = maitreTextFieldColors(),
                  modifier = Modifier.weight(1f),
                  singleLine = true,
                  textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )

                Button(
                  onClick = {
                    if (couponInput.isBlank()) {
                      couponError = "يرجى كتابة رمز الكود"
                      return@Button
                    }
                    val res = MaitreRepository.validateAndApplyCoupon(
                      code = couponInput.trim(),
                      amount = bid.grandTotalAmount,
                      userRole = currentUser.role
                    )
                    res.onSuccess { (coupon, discount) ->
                      appliedCoupon = coupon
                      couponDiscountAmount = discount
                      couponSuccessMessage = "تم تطبيق الخصم بنجاح! وفرت ${discount.toInt()} ج.م"
                      couponError = null
                    }.onFailure { err ->
                      appliedCoupon = null
                      couponDiscountAmount = 0.0
                      couponSuccessMessage = null
                      couponError = err.message ?: "الكوبون غير صالح"
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("تطبيق", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              if (couponSuccessMessage != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                  Text(couponSuccessMessage!!, fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("الإجمالي بعد الخصم:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text("${(bid.grandTotalAmount - couponDiscountAmount).coerceAtLeast(0.0).toInt()} ج.م", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }
              }

              if (couponError != null) {
                Text(couponError!!, fontSize = 10.sp, color = CrimsonError, fontWeight = FontWeight.SemiBold)
              }
            }
          }

          Text("اختر وسيلة الدفع الآمنة:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          val methods = listOf("إنستاباي InstaPay", "فودافون كاش / محافظ إلكترونية", "بطاقة ميزة / بنكية Visa/MC", "رصيد المحفظة المتاح")
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            methods.forEach { method ->
              Surface(
                color = if (selectedPaymentMethod == method) GoldContainer else CreamSurface,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPaymentMethod == method) GoldSecondary else BorderSubtle),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { selectedPaymentMethod = method }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = method, fontSize = 12.sp, fontWeight = if (selectedPaymentMethod == method) FontWeight.Bold else FontWeight.Normal)
                  if (selectedPaymentMethod == method) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
                  }
                }
              }
            }
          }

          Text(
            text = "🛡️ لا يتم تحويل الأتعاب للمحامي إلا بعد موافقتك على استلام العمل أو بقرار من لجنة التحكيم.",
            fontSize = 11.sp,
            color = Color(0xFF047857)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onAcceptBid(bid.id, selectedPaymentMethod)
            showAcceptPaymentDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("تأكيد الإيداع وبدء العمل")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAcceptPaymentDialog = false }) {
          Text("تراجع")
        }
      }
    )
  }

  // DIALOG: Confirm Release of Escrow
  if (showReleaseConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showReleaseConfirmDialog = false },
      title = {
        Text("تأكيد تحرير الأتعاب للمحامي", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      },
      text = {
        Text(
          text = "هل استلمت الخدمة القانونية أو الاستشارة كاملة ووفق الاتفاق؟ عند التأكيد سيتم تحويل الأتعاب المحتجزة لحساب المحامي فوراً.",
          fontSize = 13.sp,
          color = TextSecondary,
          lineHeight = 18.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onReleaseEscrow()
            showReleaseConfirmDialog = false
            showRatingDialog = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
        ) {
          Text("نعم، حرر الأتعاب للمحامي")
        }
      },
      dismissButton = {
        TextButton(onClick = { showReleaseConfirmDialog = false }) {
          Text("إلغاء")
        }
      }
    )
  }

  // DIALOG: Post-Service Rating & Feedback to Firestore
  if (showRatingDialog) {
    val acceptedBid = bids.find { it.id == request.acceptedBidId }
    val lawyerName = acceptedBid?.lawyerName ?: "المحامي المعتمد"
    val lawyerId = acceptedBid?.lawyerId ?: ""

    ServiceCompletionRatingDialog(
      lawyerName = lawyerName,
      requestTitle = request.title,
      onDismiss = { showRatingDialog = false },
      onSubmitRating = { stars, comment, tags ->
        if (lawyerId.isNotBlank()) {
          MaitreRepository.submitReview(
            lawyerId = lawyerId,
            stars = stars,
            comment = comment,
            tags = tags,
            requestId = request.id
          )
        }
        showRatingDialog = false
      }
    )
  }

  // DIALOG: Meeting QR Scanner for Client (Requirement #2)
  if (showMeetingScannerDialog) {
    MeetingQrScannerDialog(
      request = request,
      onDismiss = { showMeetingScannerDialog = false },
      onConfirmMeeting = {
        MaitreRepository.confirmMeetingWithQr(request.id)
      }
    )
  }

  // DIALOG: Lawyer Request Disbursement / Judicial Expenses (Requirement #3)
  if (showDisbursementDialog) {
    RequestDisbursementDialog(
      onDismiss = { showDisbursementDialog = false },
      onSubmit = { type, amount, reason, receiptRef ->
        MaitreRepository.requestDisbursement(
          requestId = request.id,
          type = type,
          amount = amount,
          reason = reason,
          receiptOrRef = receiptRef
        )
        showDisbursementDialog = false
      }
    )
  }

  // DIALOG: Client Modify Disbursement (Requirement #3)
  modifyingDisbursement?.let { item ->
    ModifyDisbursementDialog(
      item = item,
      onDismiss = { modifyingDisbursement = null },
      onSubmitModification = { counterAmount, note ->
        MaitreRepository.modifyDisbursement(
          disbursementId = item.id,
          counterAmount = counterAmount,
          clientNote = note
        )
        modifyingDisbursement = null
      }
    )
  }

  // DIALOG: Client Reject Disbursement (Requirement #3)
  rejectingDisbursement?.let { item ->
    RejectDisbursementDialog(
      item = item,
      onDismiss = { rejectingDisbursement = null },
      onSubmitRejection = { reason ->
        MaitreRepository.rejectDisbursement(
          disbursementId = item.id,
          reason = reason
        )
        rejectingDisbursement = null
      }
    )
  }
}

@Composable
fun BidCardItem(
  bid: Bid,
  isRequestOpen: Boolean,
  isClient: Boolean,
  onAccept: () -> Unit
) {
  Surface(
    color = CreamSurface,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (bid.status == BidStatus.ACCEPTED) EmeraldSuccess else BorderSubtle
    ),
    shadowElevation = 1.dp,
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
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(NavyPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = GoldLight)
          }

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              val displayName = if (!isClient || bid.status == BidStatus.ACCEPTED) {
                bid.getFullDisplayName()
              } else {
                bid.getMaskedDisplayName()
              }
              Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
              Spacer(modifier = Modifier.width(4.dp))
              Icon(Icons.Default.Verified, contentDescription = "مرخص", tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
            }
            
            // Lawyer Bar Degree Badge (ابتدائي / استئناف / نقض)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.padding(top = 2.dp)
            ) {
              Surface(
                color = when (bid.lawyerDegree) {
                  LawyerBarDegree.CASSATION -> CrimsonContainer
                  LawyerBarDegree.APPEAL -> GoldContainer
                  LawyerBarDegree.PRIMARY -> EmeraldContainer
                  LawyerBarDegree.GENERAL_TABLE -> NavyContainer
                },
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = bid.lawyerDegree.formalTitleAr,
                  color = when (bid.lawyerDegree) {
                    LawyerBarDegree.CASSATION -> CrimsonError
                    LawyerBarDegree.APPEAL -> GoldDark
                    LawyerBarDegree.PRIMARY -> EmeraldSuccess
                    LawyerBarDegree.GENERAL_TABLE -> NavyPrimary
                  },
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
              Text(text = "• ⭐ ${bid.lawyerRating}", fontSize = 11.sp, color = TextMuted)
            }
          }
        }

        Surface(
          color = when (bid.status) {
            BidStatus.ACCEPTED -> EmeraldContainer
            BidStatus.PENDING -> GoldContainer
            BidStatus.REJECTED -> CrimsonContainer
          },
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = bid.status.labelAr,
            color = when (bid.status) {
              BidStatus.ACCEPTED -> Color(0xFF047857)
              BidStatus.PENDING -> GoldDark
              BidStatus.REJECTED -> CrimsonError
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Text(
        text = bid.proposalNote,
        fontSize = 12.sp,
        color = TextSecondary,
        lineHeight = 18.sp
      )

      // Transparent Financial Breakdown Card (Requirement #3)
      Surface(
        color = CreamBackground,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("أتعاب المحامي:", fontSize = 11.sp, color = TextSecondary)
            Text("${bid.lawyerFee.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          }

          if (bid.legalExpenses > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("المصاريف القضائية المقدرة:", fontSize = 11.sp, color = TextSecondary)
              Text("${bid.legalExpenses.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
          }

          Divider(color = BorderSubtle, thickness = 0.6.dp)

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("المبلغ الإجمالي المعروض للعميل:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Text("${bid.grandTotalAmount.toInt()} ج.م", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "مدة التنفيذ المقترحة:", color = TextMuted, fontSize = 10.sp)
          Text(
            text = "${bid.proposedDays} أيام عمل",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        if (isRequestOpen && isClient && bid.status == BidStatus.PENDING) {
          Button(
            onClick = onAccept,
            colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("قبول بالضمان (${bid.grandTotalAmount.toInt()} ج.م)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
