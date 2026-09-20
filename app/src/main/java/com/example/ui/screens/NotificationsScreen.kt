package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppNotification
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
  notifications: List<AppNotification>,
  onBackClick: () -> Unit,
  onNotificationClick: (AppNotification) -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("الإشعارات والتنبيهات", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White) },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
      )
    },
    containerColor = CreamBackground
  ) { padding ->
    if (notifications.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
        contentAlignment = Alignment.Center
      ) {
        Text("لا توجد إشعارات حالياً", color = TextMuted, fontSize = 14.sp)
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(notifications) { notif ->
          val isEscrow = notif.title.contains("الضمان") || notif.title.contains("أتعاب")
          val isBid = notif.title.contains("عرض")
          val isDispute = notif.title.contains("نزاع")

          Surface(
            color = if (notif.isRead) CreamSurface else CreamSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (notif.isRead) BorderSubtle else GoldSecondary.copy(alpha = 0.5f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable { onNotificationClick(notif) }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(
                    when {
                      isEscrow -> EmeraldContainer
                      isDispute -> CrimsonContainer
                      isBid -> GoldContainer
                      else -> NavyContainer
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = when {
                    isEscrow -> Icons.Default.Lock
                    isDispute -> Icons.Default.Gavel
                    isBid -> Icons.Default.LocalOffer
                    else -> Icons.Default.Notifications
                  },
                  contentDescription = null,
                  tint = when {
                    isEscrow -> EmeraldSuccess
                    isDispute -> CrimsonError
                    isBid -> GoldDark
                    else -> NavyPrimary
                  },
                  modifier = Modifier.size(20.dp)
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                  Text(notif.timestamp, fontSize = 10.sp, color = TextMuted)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(notif.body, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
              }
            }
          }
        }
      }
    }
  }
}
