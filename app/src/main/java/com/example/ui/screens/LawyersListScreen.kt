package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Lawyer
import com.example.model.RequestCategory
import com.example.model.UserRole
import com.example.model.VerificationStatus
import com.example.ui.components.CategoryChip
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LawyersListScreen(
  lawyers: List<Lawyer>,
  currentUserRole: UserRole = UserRole.CLIENT,
  assignedGovernorate: String? = null,
  onBackClick: (() -> Unit)? = null,
  onLawyerClick: (String) -> Unit,
  onRequestConsultation: (category: RequestCategory) -> Unit
) {
  val isLawyer = currentUserRole == UserRole.LAWYER
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<RequestCategory?>(null) }
  var selectedCity by remember { mutableStateOf<String?>(null) }

  val filteredLawyers = lawyers.filter { lawyer ->
    val matchesSearch = searchQuery.isBlank() || lawyer.name.contains(searchQuery, ignoreCase = true) ||
      lawyer.bio.contains(searchQuery, ignoreCase = true)
    val matchesCat = selectedCategory == null || lawyer.specialization == selectedCategory
    val matchesCity = selectedCity == null || lawyer.city == selectedCity
    matchesSearch && matchesCat && matchesCity
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (isLawyer) "دليل الزملاء المعتمدين بنقابة المحامين" else "دليل المحامين المعتمدين",
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
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      if (isLawyer) {
        item {
          Surface(
            color = NavyContainer,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(20.dp))
              Text(
                text = "تنويه مهني: بصفتك محامياً، دورك بالمنصة هو متلقٍ للطلبات من الموكلين في إطارك الجغرافي (${assignedGovernorate ?: "القاهرة"}). غير متاح للمحامين طلب تقديم خدمة.",
                color = Color.White,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
              )
            }
          }
        }
      }
      // 1. Search Bar
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("ابحث باسم المحامي، المدينة، أو التخصص...", fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.adaptiveTextMuted) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "مسح", tint = MaterialTheme.adaptiveTextMuted)
              }
            }
          },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth(),
          colors = maitreTextFieldColors()
        )
      }

      // 2. Specialization Filters
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("تصفية حسب التخصص:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
              Surface(
                color = if (selectedCategory == null) NavyPrimary else CreamSurface,
                shape = RoundedCornerShape(10.dp),
                border = if (selectedCategory == null) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { selectedCategory = null }
              ) {
                Text(
                  text = "جميع التخصصات",
                  color = if (selectedCategory == null) Color.White else TextPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
            items(RequestCategory.values().toList()) { cat ->
              val isSelected = selectedCategory == cat
              Surface(
                color = if (isSelected) NavyPrimary else CreamSurface,
                shape = RoundedCornerShape(10.dp),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { selectedCategory = if (isSelected) null else cat }
              ) {
                Text(
                  text = cat.titleAr,
                  color = if (isSelected) Color.White else TextPrimary,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      // 3. Count Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "المحامون المتاحون (${filteredLawyers.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )

          Surface(
            color = EmeraldContainer,
            shape = RoundedCornerShape(6.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("تراخيص عدلية مفحوصة", color = Color(0xFF065F46), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // 4. Lawyer Cards
      items(filteredLawyers) { lawyer ->
        LawyerDetailedCard(
          lawyer = lawyer,
          isLawyerUser = isLawyer,
          onClick = { onLawyerClick(lawyer.id) },
          onRequestConsultation = { onRequestConsultation(lawyer.specialization) }
        )
      }
    }
  }
}

@Composable
fun LawyerDetailedCard(
  lawyer: Lawyer,
  isLawyerUser: Boolean = false,
  onClick: () -> Unit,
  onRequestConsultation: () -> Unit
) {
  Surface(
    color = MaterialTheme.adaptiveSurface,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
    shadowElevation = 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
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
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(CircleShape)
              .background(NavyPrimary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = lawyer.name.take(2),
              color = GoldLight,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = lawyer.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.adaptiveTextPrimary
              )
              if (lawyer.isVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = "معتمد",
                  tint = EmeraldSuccess,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
            Text(
              text = "ترخيص عدل: ${lawyer.licenseNumber} • ${lawyer.city}",
              fontSize = 11.sp,
              color = MaterialTheme.adaptiveTextMuted
            )
          }
        }

        Surface(
          color = GoldContainer,
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "${lawyer.rating}",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = GoldDark
            )
          }
        }
      }

      Text(
        text = lawyer.bio,
        fontSize = 12.sp,
        color = MaterialTheme.adaptiveTextSecondary,
        lineHeight = 17.sp,
        maxLines = 3
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = MaterialTheme.adaptiveSurfaceVariant,
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = lawyer.specialization.titleAr,
            color = if (MaterialTheme.colorScheme.background == NavyDark) GoldLight else NavyPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        Text(
          text = "${lawyer.yearsExperience} سنوات خبرة",
          fontSize = 11.sp,
          color = TextMuted
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
          text = "الاستشارة: ${lawyer.consultationFee.toInt()} ج.م",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
      }

      Divider(color = BorderSubtle, thickness = 0.8.dp)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onClick,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Text("الملف الشخصي", fontSize = 12.sp)
        }

        if (!isLawyerUser) {
          Button(
            onClick = onRequestConsultation,
            colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("طلب استشارة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}
