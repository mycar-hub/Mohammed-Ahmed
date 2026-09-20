package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.model.LawyerReview
import com.example.model.RequestCategory
import com.example.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LawyerProfileDetailScreen(
  lawyer: Lawyer,
  reviews: List<LawyerReview>,
  currentUserRole: UserRole = UserRole.CLIENT,
  onBackClick: () -> Unit,
  onRequestConsultation: (RequestCategory) -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(lawyer.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White) },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
      )
    },
    containerColor = MaterialTheme.adaptiveBackground,
    bottomBar = {
      Surface(
        color = MaterialTheme.adaptiveSurface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("أتعاب الاستشارة المبدئية:", fontSize = 11.sp, color = TextMuted)
            Text("${lawyer.consultationFee.toInt()} ج.م", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          }

          if (currentUserRole == UserRole.LAWYER) {
            Surface(
              color = NavyContainer,
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(
                text = "ملف زميل مهني (اطلاع فقط)",
                color = GoldSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              )
            }
          } else {
            Button(
              onClick = { onRequestConsultation(lawyer.specialization) },
              colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("طلب استشارة خاصة", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Lawyer Header Profile
      item {
        Surface(
          color = CreamSurface,
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
          shadowElevation = 1.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(NavyPrimary),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = lawyer.name.take(2),
                color = GoldLight,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = lawyer.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
              )
              if (lawyer.isVerified) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = "محامٍ معتمد",
                  tint = EmeraldSuccess,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Text(
              text = "نقابة المحامين المصرية • قيد استئناف ونقض رقم: ${lawyer.licenseNumber} • ${lawyer.city}",
              fontSize = 12.sp,
              color = TextMuted
            )

            Row(
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              modifier = Modifier.padding(top = 6.dp)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${lawyer.rating} ⭐", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldDark)
                Text("${lawyer.reviewsCount} تقييم", fontSize = 11.sp, color = TextMuted)
              }

              Divider(
                modifier = Modifier
                  .height(30.dp)
                  .width(1.dp),
                color = BorderSubtle
              )

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${lawyer.yearsExperience}+", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                Text("سنوات خبرة", fontSize = 11.sp, color = TextMuted)
              }

              Divider(
                modifier = Modifier
                  .height(30.dp)
                  .width(1.dp),
                color = BorderSubtle
              )

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("100%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldSuccess)
                Text("ضمان مالي", fontSize = 11.sp, color = TextMuted)
              }
            }
          }
        }
      }

      // 2. Bio & Qualifications
      item {
        Surface(
          color = CreamSurface,
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("نبذة عن المحامي والخبرات:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Text(
              text = lawyer.bio,
              fontSize = 13.sp,
              color = TextSecondary,
              lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text("التخصص المعتمد:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
            Surface(
              color = GoldContainer,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = lawyer.specialization.titleAr,
                color = GoldDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // 3. Client Reviews
      item {
        Text(
          text = "تقييمات وآراء العملاء (${reviews.size})",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.adaptiveTextPrimary
        )
      }

      if (reviews.isEmpty()) {
        item {
          Text("لا توجد مراجعات سابقة حتى الآن.", color = MaterialTheme.adaptiveTextMuted, fontSize = 12.sp)
        }
      } else {
        items(reviews) { rev ->
          Surface(
            color = MaterialTheme.adaptiveSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
            modifier = Modifier.fillMaxWidth()
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
                Text(text = rev.clientName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.adaptiveTextPrimary)
                Row {
                  repeat(rev.stars) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                  }
                }
              }

              Text(text = rev.comment, fontSize = 12.sp, color = MaterialTheme.adaptiveTextSecondary, lineHeight = 17.sp)

              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rev.tags.forEach { tag ->
                  Surface(
                    color = MaterialTheme.adaptiveSurfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = tag,
                      fontSize = 10.sp,
                      color = MaterialTheme.adaptiveTextSecondary,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
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
