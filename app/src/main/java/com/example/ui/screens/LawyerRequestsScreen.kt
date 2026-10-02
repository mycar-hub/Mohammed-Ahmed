package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
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
 * مع نظام مطابقة جغرافي ذكي وعرض الطلبات الفورية مثل تطبيقات النقل الذكي
 */
@Composable
fun LawyerRequestsScreen(
  currentUser: UserProfile,
  isAvailable: Boolean,
  requests: List<ServiceRequest>,
  bids: List<Bid>,
  onToggleAvailability: (Boolean) -> Unit,
  onSimulateIncomingRequest: () -> Unit,
  onRequestClick: (String) -> Unit,
  onAcceptRequestQuick: (ServiceRequest) -> Unit,
  onOpenWorkspace: (String) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: طلبات واردة في النطاق, 1: عروضي المقدمة, 2: قيد التنفيذ
  var selectedCategory by remember { mutableStateOf<RequestCategory?>(null) }

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
        LawyerRequestOrderCard(
          request = req,
          currentUser = currentUser,
          onClick = { onRequestClick(req.id) },
          onAcceptQuick = { onAcceptRequestQuick(req) },
          onOpenWorkspace = { onOpenWorkspace(req.id) }
        )
      }
    }
  }
}

/**
 * كارت الطلب المصمم بنمط رحلات تطبيقات النقل الذكي
 */
@Composable
fun LawyerRequestOrderCard(
  request: ServiceRequest,
  currentUser: UserProfile,
  onClick: () -> Unit,
  onAcceptQuick: () -> Unit,
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

      // 4. الأتعاب والإجراءات
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "الميزانية المقترحة",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.5.sp
          )
          Text(
            text = "${request.budgetAmount.toInt()} ج.م",
            color = EmeraldSuccess,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (request.status == RequestStatus.OPEN) {
            Button(
              onClick = onAcceptQuick,
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("قبول فوري", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text("تفاصيل الطلب", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}
