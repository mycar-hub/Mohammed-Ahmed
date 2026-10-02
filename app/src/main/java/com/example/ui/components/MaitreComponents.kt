package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaitreTopAppBar(
  currentUser: UserProfile,
  unreadNotificationsCount: Int,
  onRoleSwitch: (UserRole) -> Unit = {},
  onNotificationsClick: () -> Unit,
  onWalletClick: () -> Unit
) {
  Surface(
    color = NavyPrimary,
    tonalElevation = 2.dp,
    shadowElevation = 3.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // 1. Compact Brand Identification (Start side in RTL)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          modifier = Modifier.size(32.dp),
          shape = RoundedCornerShape(8.dp),
          color = NavyDark,
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f))
        ) {
          Image(
            painter = painterResource(id = R.drawable.official_maitre_logo),
            contentDescription = "شعار منصة متر",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        }

        Column {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "مِـتـر",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
            Surface(
              color = GoldSecondary.copy(alpha = 0.25f),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "MAÎTRE",
                color = GoldLight,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
          Text(
            text = "مصر • EGY",
            color = GoldLight.copy(alpha = 0.8f),
            fontSize = 8.5.sp
          )
        }
      }

      // 2. Compact Actions Group (End side in RTL)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Static User Role Badge Pill (In-app switcher removed for independent system review)
        Surface(
          color = when (currentUser.role) {
            UserRole.CLIENT -> GoldSecondary
            UserRole.LAWYER -> EmeraldSuccess
            UserRole.ADMIN -> CrimsonError
          },
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.height(28.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = when (currentUser.role) {
                UserRole.CLIENT -> Icons.Default.Person
                UserRole.LAWYER -> Icons.Default.Gavel
                UserRole.ADMIN -> Icons.Default.Shield
              },
              contentDescription = null,
              tint = when (currentUser.role) {
                UserRole.CLIENT -> NavyDark
                else -> Color.White
              },
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = when (currentUser.role) {
                UserRole.CLIENT -> "حساب عميل"
                UserRole.LAWYER -> "حساب محامٍ"
                UserRole.ADMIN -> "حساب مشرف"
              },
              color = when (currentUser.role) {
                UserRole.CLIENT -> NavyDark
                else -> Color.White
              },
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
          }
        }

        // B. Compact Wallet Pill
        Surface(
          color = NavyContainer,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onWalletClick() }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AccountBalanceWallet,
              contentDescription = "Wallet",
              tint = GoldSecondary,
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = "${currentUser.balance.toInt()} ج.م",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1
            )
          }
        }

        // C. Compact Notification Bell with Badge
        Box(
          modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable { onNotificationsClick() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "الإشعارات",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          if (unreadNotificationsCount > 0) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(CrimsonError)
            )
          }
        }
      }
    }
  }
}

@Composable
fun StatusBadge(status: RequestStatus) {
  val (bgColor, textColor, icon) = when (status) {
    RequestStatus.OPEN -> Triple(AmberContainer, Color(0xFFB45309), Icons.Default.Schedule)
    RequestStatus.NEGOTIATING -> Triple(GoldContainer, GoldDark, Icons.Default.Handshake)
    RequestStatus.IN_PROGRESS -> Triple(EmeraldContainer, Color(0xFF047857), Icons.Default.Lock)
    RequestStatus.COMPLETED -> Triple(EmeraldContainer, Color(0xFF047857), Icons.Default.CheckCircle)
    RequestStatus.DISPUTED -> Triple(CrimsonContainer, CrimsonError, Icons.Default.ReportProblem)
  }

  Surface(
    color = bgColor,
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(13.dp))
      Text(
        text = status.labelAr,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun EscrowStatusBadge(status: EscrowStatus) {
  val (bgColor, textColor, label) = when (status) {
    EscrowStatus.HELD -> Triple(AmberContainer, Color(0xFFB45309), "محفوظة بمحفظة الضمان")
    EscrowStatus.RELEASED -> Triple(EmeraldContainer, Color(0xFF047857), "تم تحرير الأتعاب بنجاح")
    EscrowStatus.REFUNDED -> Triple(Color(0xFFE0E7FF), Color(0xFF3730A3), "تمت استعادة الأتعاب للعميل")
    EscrowStatus.FROZEN_FOR_DISPUTE -> Triple(CrimsonContainer, CrimsonError, "مجمدة لوجود نزاع قيد التحكيم")
  }

  Surface(
    color = bgColor,
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Icon(
        imageVector = Icons.Default.VerifiedUser,
        contentDescription = null,
        tint = textColor,
        modifier = Modifier.size(13.dp)
      )
      Text(
        text = label,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun CategoryChip(category: RequestCategory, isSelected: Boolean = false, onClick: (() -> Unit)? = null) {
  Surface(
    color = if (isSelected) NavyPrimary else MaterialTheme.adaptiveSurface,
    shape = RoundedCornerShape(12.dp),
    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
    shadowElevation = if (isSelected) 2.dp else 0.dp,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
  ) {
    Text(
      text = category.titleAr,
      color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )
  }
}

@Composable
fun MaitreBottomNavigation(
  currentScreen: String,
  userRole: UserRole,
  onNavigate: (String) -> Unit
) {
  NavigationBar(
    containerColor = NavyPrimary,
    contentColor = Color.White,
    modifier = Modifier.navigationBarsPadding()
  ) {
    NavigationBarItem(
      selected = currentScreen == "home",
      onClick = { onNavigate("home") },
      icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
      label = { Text(if (userRole == UserRole.LAWYER) "سوق القضايا" else "الرئيسية", fontSize = 11.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = GoldLight,
        indicatorColor = GoldSecondary,
        unselectedIconColor = Color.White.copy(alpha = 0.7f),
        unselectedTextColor = Color.White.copy(alpha = 0.7f)
      )
    )

    NavigationBarItem(
      selected = currentScreen == "tracker",
      onClick = { onNavigate("tracker") },
      icon = { Icon(Icons.Default.Timeline, contentDescription = "تتبع القضايا") },
      label = { Text("تتبع القضايا", fontSize = 11.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = GoldLight,
        indicatorColor = GoldSecondary,
        unselectedIconColor = Color.White.copy(alpha = 0.7f),
        unselectedTextColor = Color.White.copy(alpha = 0.7f)
      )
    )

    NavigationBarItem(
      selected = currentScreen == "escrow",
      onClick = { onNavigate("escrow") },
      icon = { Icon(Icons.Default.Security, contentDescription = "الضمان والإيداع") },
      label = { Text(if (userRole == UserRole.LAWYER) "محفظة الأتعاب" else "الضمان والإيداع", fontSize = 11.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = GoldLight,
        indicatorColor = GoldSecondary,
        unselectedIconColor = Color.White.copy(alpha = 0.7f),
        unselectedTextColor = Color.White.copy(alpha = 0.7f)
      )
    )

    if (userRole == UserRole.LAWYER) {
      NavigationBarItem(
        selected = currentScreen == "lawyer_requests",
        onClick = { onNavigate("lawyer_requests") },
        icon = { Icon(Icons.Default.Bolt, contentDescription = "طلبات") },
        label = { Text("طلبات", fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = NavyDark,
          selectedTextColor = GoldLight,
          indicatorColor = GoldSecondary,
          unselectedIconColor = Color.White.copy(alpha = 0.7f),
          unselectedTextColor = Color.White.copy(alpha = 0.7f)
        )
      )
    } else {
      NavigationBarItem(
        selected = currentScreen == "lawyers",
        onClick = { onNavigate("lawyers") },
        icon = { Icon(Icons.Default.Gavel, contentDescription = "المحامون") },
        label = { Text("المحامون", fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = NavyDark,
          selectedTextColor = GoldLight,
          indicatorColor = GoldSecondary,
          unselectedIconColor = Color.White.copy(alpha = 0.7f),
          unselectedTextColor = Color.White.copy(alpha = 0.7f)
        )
      )
    }

    NavigationBarItem(
      selected = currentScreen == "profile",
      onClick = { onNavigate("profile") },
      icon = { Icon(Icons.Default.Person, contentDescription = "حسابي") },
      label = { Text(if (userRole == UserRole.LAWYER) "رخصتي وحسابي" else "حسابي", fontSize = 11.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = GoldLight,
        indicatorColor = GoldSecondary,
        unselectedIconColor = Color.White.copy(alpha = 0.7f),
        unselectedTextColor = Color.White.copy(alpha = 0.7f)
      )
    )

    if (userRole == UserRole.ADMIN) {
      NavigationBarItem(
        selected = currentScreen == "admin",
        onClick = { onNavigate("admin") },
        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "لوحة الرقابة") },
        label = { Text("الرقابة والتحكيم", fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = NavyDark,
          selectedTextColor = GoldLight,
          indicatorColor = CrimsonError,
          unselectedIconColor = Color.White.copy(alpha = 0.7f),
          unselectedTextColor = Color.White.copy(alpha = 0.7f)
        )
      )
    }
  }
}
