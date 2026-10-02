package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.CategoryChip
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

/**
 * شاشة طلبات المحامي الفورية
 * مزودة بزر "متاح أو متوقف" (Available / Offline)
 * ونظام تسعير الأتعاب والمصاريف القانونية التقديرية وحساب السعر النهائي للعميل
 */
@Composable
fun LawyerRequestsScreen(
  currentUser: UserProfile,
  isAvailable: Boolean,
  requests: List<ServiceRequest>,
  bids: List<Bid>,
  clientFeePercentage: Double = 5.0,
  onToggleAvailability: (Boolean) -> Unit,
  onSimulateIncomingRequest: () -> Unit,
  onRequestClick: (String) -> Unit,
  onSubmitBid: (requestId: String, lawyerFee: Double, legalExpenses: Double, days: Int, note: String) -> Unit,
  onOpenWorkspace: (String) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: طلبات واردة في النطاق, 1: عروضي المقدمة, 2: قيد التنفيذ
  var selectedCategory by remember { mutableStateOf<RequestCategory?>(null) }

  // Modal dialog للتسعير الفوري
  var pricingRequest by remember { mutableStateOf<ServiceRequest?>(null) }

  // الطلبات المطابقة للنطاق الجغرافي للمحامي
  val matchingGeographicRequests = requests.filter { req ->
    currentUser.isWithinLawyerJurisdiction(req.city, req.courtLocation)
  }

  // فلترة حسب التبويب
  val displayedRequests = matchingGeographicRequests.filter { req ->
    val matchesTab = when (selectedTab) {
      0 -> req.status == RequestStatus.OPEN
      1 -> bids.any { it.requestId == req.id && (it.lawyerId == currentUser.id || it.lawyerName.contains(currentUser.name)) }
      2 -> (req.status == RequestStatus.IN_PROGRESS || req.status == RequestStatus.COMPLETED) &&
           (req.acceptedBidId != null || bids.any { it.requestId == req.id && it.status == BidStatus.ACCEPTED })
      else -> true
    }
    val matchesCat = selectedCategory == null || req.category == selectedCategory
    matchesTab && matchesCat
  }

  // نبض مؤشر "متاح" الأخضر الحي
  val infiniteTransition = rememberInfiniteTransition(label = "onlinePulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CreamBackground),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. بطاقة زر "متاح أو متوقف" البارزة (Uber / Careem driver status bar)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isAvailable) NavyPrimary else Color(0xFF242730)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // نقطة الحالة المضيئة
              Box(
                modifier = Modifier
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(
                    if (isAvailable) EmeraldSuccess.copy(alpha = pulseAlpha) else CrimsonError
                  )
              )
              Column {
                Text(
                  text = if (isAvailable) "حالة المحامي: مـتـاح (متصل)" else "حالة المحامي: مـتـوقـف (غير متصل)",
                  color = Color.White,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (isAvailable) "جاهز لاستقبال الطلبات الفورية بإنذار صوتي 20 ثانية" else "تم إيقاف استقبال الطلبات والإشعارات الفورية مؤقتاً",
                  color = if (isAvailable) EmeraldSuccess else Color.White.copy(alpha = 0.7f),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            // زر التبديل المباشر متاح / متوقف
            Switch(
              checked = isAvailable,
              onCheckedChange = { onToggleAvailability(it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EmeraldSuccess,
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color.DarkGray
              )
            )
          }

          Divider(color = Color.White.copy(alpha = 0.15f), thickness = 0.8.dp)

          // إفادة النطاق الجغرافي للمحامي
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.LocationSearching,
              contentDescription = null,
              tint = GoldSecondary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "نطاق الاختصاص المعتمد: ${currentUser.assignedGovernorate} • دائرة ${currentUser.assignedDistrict}",
              color = GoldLight,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // توضيح آلية التسعير
          Surface(
            color = Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "💡 آلية التسعير الذكية: العميل يطلب الخدمة بدون سعر مسبق. تقدم أتعابك ومصاريفك التقديرية، ويحسب النظام السعر النهائي الشامل أتعاب المنصة (${clientFeePercentage.toInt()}%) ليعرضه على العميل للموافقة أو الرفض.",
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 10.5.sp,
              lineHeight = 15.sp,
              modifier = Modifier.padding(10.dp)
            )
          }

          // زر المحاكاة الفورية لتجربة الإشعار الـ 20 ثانية مع الـ Alarm
          Button(
            onClick = onSimulateIncomingRequest,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isAvailable) GoldSecondary else Color.Gray,
              contentColor = NavyDark
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            enabled = isAvailable
          ) {
            Icon(
              imageVector = Icons.Default.Campaign,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "⚡ محاكاة وصول طلب مطابق (تنبيه 20 ثانية مع Alarm)",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 2. تنبيه عند التوقف عن استقبال الطلبات
    if (!isAvailable) {
      item {
        Surface(
          color = CrimsonContainer,
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.PauseCircle,
              contentDescription = null,
              tint = CrimsonError,
              modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "أنت في وضع \"متوقف\"",
                color = CrimsonError,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "لن تظهر لك نوافذ التنبيه المنبثقة للطلبات الجديدة في منطقتك حتى تعود للوضع \"متاح\".",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
            TextButton(
              onClick = { onToggleAvailability(true) }
            ) {
              Text("تفعيل الآن", color = CrimsonError, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 3. شريط التبويبات الثلاثة
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val openCount = matchingGeographicRequests.count { it.status == RequestStatus.OPEN }
        val myBidsCount = bids.count { it.lawyerId == currentUser.id || it.lawyerName.contains(currentUser.name) }

        FilterChip(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          label = {
            Text(
              text = "طلبات واردة ($openCount)",
              fontSize = 11.5.sp,
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
            )
          },
          leadingIcon = {
            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(14.dp))
          },
          modifier = Modifier.weight(1f)
        )

        FilterChip(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          label = {
            Text(
              text = "عروضي ($myBidsCount)",
              fontSize = 11.5.sp,
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
            )
          },
          leadingIcon = {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
          },
          modifier = Modifier.weight(1f)
        )

        FilterChip(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          label = {
            Text(
              text = "قيد التنفيذ",
              fontSize = 11.5.sp,
              fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
            )
          },
          leadingIcon = {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
          },
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 4. تصفية التصنيفات
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          color = if (selectedCategory == null) NavyPrimary else MaterialTheme.adaptiveSurface,
          shape = RoundedCornerShape(12.dp),
          border = if (selectedCategory == null) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
          modifier = Modifier.clickable { selectedCategory = null }
        ) {
          Text(
            text = "الكل",
            color = if (selectedCategory == null) Color.White else MaterialTheme.adaptiveTextPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }

        RequestCategory.values().take(3).forEach { cat ->
          CategoryChip(
            category = cat,
            isSelected = selectedCategory == cat,
            onClick = {
              selectedCategory = if (selectedCategory == cat) null else cat
            }
          )
        }
      }
    }

    // 5. قائمة الطلبات المعروضة
    if (displayedRequests.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.SearchOff,
              contentDescription = null,
              tint = NavyDark.copy(alpha = 0.4f),
              modifier = Modifier.size(48.dp)
            )
            Text(
              text = if (!isAvailable)
                "أنت في وضع \"متوقف\". قم بالتبديل إلى \"متاح\" لبدء تلقي الطلبات الفورية."
              else
                "لا توجد طلبات جديدة حالياً ضمن نطاق ${currentUser.assignedGovernorate} - ${currentUser.assignedDistrict}.",
              color = MaterialTheme.adaptiveTextPrimary,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (isAvailable) {
              Button(
                onClick = onSimulateIncomingRequest,
                colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("محاكاة طلب فوري الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    } else {
      items(displayedRequests, key = { it.id }) { req ->
        val myBid = bids.find { it.requestId == req.id && (it.lawyerId == currentUser.id || it.lawyerName.contains(currentUser.name)) }
        LawyerRequestOrderCard(
          request = req,
          currentUser = currentUser,
          myBid = myBid,
          clientFeePercentage = clientFeePercentage,
          onClick = { onRequestClick(req.id) },
          onOpenQuoteDialog = { pricingRequest = req },
          onOpenWorkspace = { onOpenWorkspace(req.id) }
        )
      }
    }
  }

  // نافذة تسعير الطلب وتقديم الأتعاب والمصاريف القانونية
  pricingRequest?.let { req ->
    LawyerQuickPricingDialog(
      request = req,
      clientFeePercentage = clientFeePercentage,
      onDismiss = { pricingRequest = null },
      onSubmit = { fee, exp, days, note ->
        onSubmitBid(req.id, fee, exp, days, note)
        pricingRequest = null
      }
    )
  }
}

/**
 * كارت الطلب المصمم بنمط رحلات تطبيقات النقل الذكي
 */
@Composable
fun LawyerRequestOrderCard(
  request: ServiceRequest,
  currentUser: UserProfile,
  myBid: Bid?,
  clientFeePercentage: Double,
  onClick: () -> Unit,
  onOpenQuoteDialog: () -> Unit,
  onOpenWorkspace: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. الرأس: شارة النطاق الجغرافي والحالة
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = EmeraldContainer,
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Icon(Icons.Default.MyLocation, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
            Text(
              text = "مطابق لنطاقك: ${request.courtLocation?.district ?: request.city}",
              color = EmeraldSuccess,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        StatusBadge(status = request.status)
      }

      // 2. العنوان والتفاصيل
      Text(
        text = request.title,
        color = MaterialTheme.adaptiveTextPrimary,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = request.description,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 16.sp
      )

      // 3. المحكمة والموقع
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(14.dp))
        Text(
          text = "${request.city} - ${request.courtLocation?.courtJurisdiction ?: "محكمة الدائرة المختصة"}",
          color = MaterialTheme.adaptiveTextPrimary,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Divider(color = MaterialTheme.adaptiveBorder, thickness = 0.8.dp)

      // 4. تفاصيل التسعير والإجراءات
      if (myBid != null) {
        // المحامي قدّم عرضه بالفعل
        Surface(
          color = CreamSurfaceVariant,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("أتعابك المقدمة:", fontSize = 11.sp, color = TextSecondary)
              Text("${myBid.lawyerFee.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            if (myBid.legalExpenses > 0) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("المصاريف القانونية التقديرية:", fontSize = 11.sp, color = TextSecondary)
                Text("${myBid.legalExpenses.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              }
            }
            Divider(color = BorderSubtle, thickness = 0.6.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("الإجمالي المعروض للعميل شامل المنصة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Text("${myBid.grandTotalAmount.toInt()} ج.م", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            }
          }
        }
      } else {
        // الطلب بانتظار تسعير المحامي
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "حالة التسعير",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.5.sp
            )
            Text(
              text = "تسعير مفتوح (المحامي يحدد)",
              color = GoldDark,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (request.status == RequestStatus.OPEN) {
              Button(
                onClick = onOpenQuoteDialog,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
              ) {
                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تسعير وتقديم العرض", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }

            OutlinedButton(
              onClick = onClick,
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text("التفاصيل", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
  }
}

/**
 * نافذة تسعير الطلب الفوري وتحديد أتعاب المحامي والمصاريف القضائية
 */
@Composable
fun LawyerQuickPricingDialog(
  request: ServiceRequest,
  clientFeePercentage: Double,
  onDismiss: () -> Unit,
  onSubmit: (lawyerFee: Double, legalExpenses: Double, days: Int, note: String) -> Unit
) {
  var feeInput by remember { mutableStateOf("3000") }
  var expInput by remember { mutableStateOf("500") }
  var daysInput by remember { mutableStateOf("3") }
  var noteInput by remember { mutableStateOf("") }

  val feeVal = feeInput.toDoubleOrNull() ?: 0.0
  val expVal = expInput.toDoubleOrNull() ?: 0.0
  val baseTotal = feeVal + expVal
  val platformFeeAmount = if (clientFeePercentage > 0.0) baseTotal * (clientFeePercentage / 100.0) else 0.0
  val grandTotalAmount = baseTotal + platformFeeAmount

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Calculate, contentDescription = null, tint = NavyPrimary)
        Text("تسعير وتقديم العرض للطلب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          color = CreamSurfaceVariant,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = "القضية: ${request.title}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
            Text(text = "الموكل: ${request.clientName} • ${request.city}", fontSize = 11.sp, color = TextSecondary)
          }
        }

        // 1. سعر الأتعاب
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text("1. سعر الأتعاب المهنية (ج.م) *", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
          OutlinedTextField(
            value = feeInput,
            onValueChange = { feeInput = it },
            placeholder = { Text("مثال: 3000") },
            shape = RoundedCornerShape(8.dp),
            colors = maitreTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
          )
          LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(listOf(1500, 2500, 3000, 4500, 6000, 8000)) { opt ->
              Surface(
                color = if (feeVal.toInt() == opt) GoldSecondary else CreamBackground,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.clickable { feeInput = opt.toString() }
              ) {
                Text(
                  text = "$opt ج.م",
                  color = if (feeVal.toInt() == opt) NavyDark else TextPrimary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }

        // 2. سعر المصاريف القانونية التقديرية
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text("2. سعر المصاريف القانونية التقديرية (ج.م)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
          Text("رسوم قيد وإيداع، أمانات الخبراء، المعاينة والانتقال (0 إذا لم توجد)", fontSize = 9.5.sp, color = TextMuted)
          OutlinedTextField(
            value = expInput,
            onValueChange = { expInput = it },
            placeholder = { Text("مثال: 500") },
            shape = RoundedCornerShape(8.dp),
            colors = maitreTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
          )
        }

        // 3. مدة التنفيذ
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text("3. مدة التنفيذ المقترحة (أيام)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary)
          OutlinedTextField(
            value = daysInput,
            onValueChange = { daysInput = it },
            shape = RoundedCornerShape(8.dp),
            colors = maitreTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
          )
        }

        // 4. الحساب التلقائي الشامل لرسوم المنصة
        Surface(
          color = GoldContainer.copy(alpha = 0.6f),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("المجموع الأساسي (أتعاب + مصاريف):", fontSize = 11.sp, color = TextPrimary)
              Text("${baseTotal.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("أتعاب المنصة المضافة (${clientFeePercentage.toInt()}%):", fontSize = 11.sp, color = GoldDark)
              Text("+ ${platformFeeAmount.toInt()} ج.م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldDark)
            }
            Divider(color = GoldSecondary.copy(alpha = 0.5f), thickness = 0.8.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("السعر النهائي الشامل المعروض للعميل:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Text("${grandTotalAmount.toInt()} ج.م", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = NavyPrimary)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val fee = feeInput.toDoubleOrNull() ?: 3000.0
          val exp = expInput.toDoubleOrNull() ?: 0.0
          val days = daysInput.toIntOrNull() ?: 3
          val note = if (noteInput.isBlank()) "تم إعداد ودراسة الأتعاب والمصاريف القانونية لمباشرة الطلب فوراً." else noteInput
          onSubmit(fee, exp, days, note)
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
      ) {
        Text("إرسال العرض للعميل")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    }
  )
}
