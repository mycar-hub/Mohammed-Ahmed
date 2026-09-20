package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.*
import com.example.ui.components.CategoryChip
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun HomeScreen(
  currentUser: UserProfile,
  requests: List<ServiceRequest>,
  lawyers: List<Lawyer>,
  onNewRequestClick: () -> Unit,
  onSelectTemplateClick: (String) -> Unit = {},
  onRequestClick: (String) -> Unit,
  onLawyerClick: (String) -> Unit,
  onBrowseLawyersClick: () -> Unit,
  onEscrowClick: () -> Unit,
  onAdminClick: (() -> Unit)? = null
) {
  val isLawyer = currentUser.role == UserRole.LAWYER
  val isAdmin = currentUser.role == UserRole.ADMIN
  var selectedTab by remember { mutableIntStateOf(0) } // 0: الكل / طلباتي / الوارد, 1: المفتوحة / عروضي, 2: قيد التنفيذ
  var selectedCategoryFilter by remember { mutableStateOf<RequestCategory?>(null) }

  val filteredRequests = requests.filter { req ->
    // Strict geographic restriction for Lawyer:
    // Lawyer receives requests ONLY within his pre-assigned jurisdiction!
    val matchesGeographicJurisdiction = if (isLawyer) {
      currentUser.isWithinLawyerJurisdiction(req.city, req.courtLocation)
    } else {
      true
    }

    val matchesTab = when (selectedTab) {
      0 -> when (currentUser.role) {
        UserRole.CLIENT -> req.clientId == currentUser.id
        UserRole.LAWYER -> req.status == RequestStatus.OPEN
        UserRole.ADMIN -> true
      }
      1 -> when (currentUser.role) {
        UserRole.CLIENT -> req.status == RequestStatus.OPEN
        UserRole.LAWYER -> req.bidsCount > 0
        UserRole.ADMIN -> req.status == RequestStatus.OPEN
      }
      2 -> req.status == RequestStatus.IN_PROGRESS || req.status == RequestStatus.COMPLETED
      else -> true
    }
    val matchesCat = selectedCategoryFilter == null || req.category == selectedCategoryFilter
    matchesGeographicJurisdiction && matchesTab && matchesCat
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(CreamBackground),
    contentPadding = PaddingValues(bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Hero Brand Card with Banner
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(
            Brush.linearGradient(
              listOf(NavyDark, NavyPrimary, NavySurface)
            )
          )
      ) {
        // Hero background image with gradient overlay
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(R.drawable.banner_legal_hero)
            .crossfade(true)
            .build(),
          contentDescription = "Legal Hero Banner",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp)),
          alpha = 0.35f
        )

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
          ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              color = if (isAdmin) CrimsonError else GoldSecondary,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = if (isAdmin) "الإدارة المركزية والرقابة" else if (isLawyer) "منظومة المحامي المتلقي" else "مرخص ومعتمد",
                color = if (isAdmin) Color.White else NavyDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
            Text(
              text = if (isAdmin) "نطاق جمهورية مصر العربية • لجان التحكيم" else if (isLawyer) "اختصاص: ${currentUser.assignedGovernorate} • حماية وضمان" else "حماية قانونية متكاملة",
              color = GoldLight,
              fontSize = 11.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = if (isAdmin)
              "لوحة الرقابة والتحكيم وإدارة المنصة"
            else if (isLawyer)
              "بوابة المحامي لتلقي القضايا والاستشارات"
            else
              "منصة متر للخدمات القانونية والتحكيم",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = if (isAdmin)
              "متابعة دورية للطلبات المفتوحة عبر 27 محافظة، قرارات لجان فض المنازعات والتحكيم، سحب رصيد المنصة المحصل، وتتبع سجلات التدقيق اللحظية."
            else if (isLawyer)
              "أنت متلقٍ معتمد لطلبات وقضايا الموكلين الواردة حصرياً ضمن نطاق اختصاصك الجغرافي المقيد (${currentUser.assignedGovernorate} - ${currentUser.assignedDistrict}). لا يجوز طلب تقديم خدمة أو الاطلاع على قضايا خارج نطاقك الجغرافي."
            else
              "اطرح قضيتك وتلقّ عروض أسعار شفافة ومفصلة من نخبة المحامين المعتمدين مع حفظ الأتعاب بمحفظة الضمان.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.5.sp,
            lineHeight = 17.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (isAdmin) {
              Button(
                onClick = { onAdminClick?.invoke() ?: onEscrowClick() },
                colors = ButtonDefaults.buttonColors(
                  containerColor = GoldSecondary,
                  contentColor = NavyDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("لوحة الإدارة والتحكيم", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = onEscrowClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldLight.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("رصيد ورسوم المنصة", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
              }
            } else if (isLawyer) {
              Button(
                onClick = { selectedTab = 0 },
                colors = ButtonDefaults.buttonColors(
                  containerColor = GoldSecondary,
                  contentColor = NavyDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("الطلبات الواردة في اختصاصي", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = onEscrowClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldLight.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("محفظة الضمان", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
              }
            } else {
              Button(
                onClick = onNewRequestClick,
                colors = ButtonDefaults.buttonColors(
                  containerColor = GoldSecondary,
                  contentColor = NavyDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("طلب جديد", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }

              OutlinedButton(
                onClick = onBrowseLawyersClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldLight.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("دليل المحامين", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
              }
            }
          }
        }
      }
    }

    // 1.5 Dedicated Lawyer Geographic Jurisdiction Banner (Strict Scope)
    if (isLawyer) {
      item {
        Surface(
          color = NavyContainer,
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.4f)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = null,
                  tint = GoldSecondary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "نطاق الاختصاص الجغرافي المقيد للمحامي",
                  color = Color.White,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Surface(
                color = EmeraldContainer,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "متلقٍ معتمد",
                  color = EmeraldSuccess,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Divider(color = Color.White.copy(alpha = 0.15f), thickness = 0.8.dp)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(15.dp))
              Text(
                text = "المحافظة المعتمدة: ${currentUser.assignedGovernorate} • دائرة: ${currentUser.assignedDistrict}",
                color = GoldLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(15.dp))
              Text(
                text = "الجهة القضائية: ${currentUser.assignedCourtJurisdiction}",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp
              )
            }

            Surface(
              color = Color.White.copy(alpha = 0.08f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "تنبيه نظامي: بصفتك محامياً، أنت متلقٍ حصري للطلبات في إطاره الجغرافي المحدد مسبقاً (${currentUser.assignedGovernorate})، ولا يجوز طلب تقديم خدمة أو إعطاء خيارات الترافع خارج المحافظة المعتمدة.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 10.5.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(8.dp)
              )
            }
          }
        }
      }
    }

    // 2. Escrow Protection Assurance Banner
    item {
      Surface(
        color = EmeraldContainer,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .clickable { onEscrowClick() }
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.padding(14.dp)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(EmeraldSuccess.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = "Escrow",
              tint = Color(0xFF047857),
              modifier = Modifier.size(24.dp)
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "محفظة الضمان المالي لمتر (Escrow)",
              color = Color(0xFF065F46),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "أتعابك محفوظة بأمان بنسبة 100% ولا يتم تحويلها للمحامي إلا بعد موافقتك الصريحة أو عبر لجنة التحكيم.",
              color = Color(0xFF047857),
              fontSize = 11.sp,
              lineHeight = 15.sp
            )
          }

          Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = "التفاصيل",
            tint = Color(0xFF047857)
          )
        }
      }
    }

    // 3. Case Templates Quick Selection Carousel (Available strictly to clients for initiating requests)
    if (currentUser.role == UserRole.CLIENT) {
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Article, contentDescription = null, tint = GoldDark, modifier = Modifier.size(18.dp))
              Text(
                text = "قوالب القضايا الجاهزة والمستندات",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = "نماذج فورية",
              color = TextMuted,
              fontSize = 11.sp
            )
          }

          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(CaseTemplates.ALL) { template ->
              Surface(
                color = CreamSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 1.dp,
                modifier = Modifier
                  .width(220.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .clickable { onSelectTemplateClick(template.id) }
              ) {
                Column(
                  modifier = Modifier.padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = when (template.category) {
                        RequestCategory.REAL_ESTATE -> Icons.Default.Home
                        RequestCategory.PERSONAL_STATUS -> Icons.Default.FamilyRestroom
                        RequestCategory.CONTRACTS -> Icons.Default.Description
                        RequestCategory.CRIMINAL_FINANCIAL -> Icons.Default.Gavel
                        else -> Icons.Default.Article
                      },
                      contentDescription = null,
                      tint = NavyPrimary,
                      modifier = Modifier.size(22.dp)
                    )
                    Surface(
                      color = when (template.recommendedBarDegree) {
                        LawyerBarDegree.CASSATION -> CrimsonContainer
                        LawyerBarDegree.APPEAL -> GoldContainer
                        LawyerBarDegree.PRIMARY -> EmeraldContainer
                        LawyerBarDegree.GENERAL_TABLE -> NavyContainer
                      },
                      shape = RoundedCornerShape(6.dp)
                    ) {
                      Text(
                        text = template.recommendedBarDegree.titleAr,
                        color = when (template.recommendedBarDegree) {
                          LawyerBarDegree.CASSATION -> CrimsonError
                          LawyerBarDegree.APPEAL -> GoldDark
                          LawyerBarDegree.PRIMARY -> EmeraldSuccess
                          LawyerBarDegree.GENERAL_TABLE -> NavyPrimary
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }

                  Text(
                    text = template.titleAr,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )

                  Text(
                    text = template.shortDescription,
                    fontSize = 10.5.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                  )

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "الميزانية: ${template.suggestedBudget.toInt()} ج.م",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = NavyPrimary
                    )
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
                  }
                }
              }
            }
          }
        }
      }
    }

    // 4. Featured Categories Horizontal Scroll
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = if (isLawyer) "تصنيف القضايا الواردة" else "التخصصات والاستشارات القانونية",
          color = TextPrimary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            CategoryChip(
              category = RequestCategory.COMMERCIAL,
              isSelected = selectedCategoryFilter == null,
              onClick = { selectedCategoryFilter = null }
            )
          }
          items(RequestCategory.values().toList()) { cat ->
            CategoryChip(
              category = cat,
              isSelected = selectedCategoryFilter == cat,
              onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat }
            )
          }
        }
      }
    }

    // 5. Requests & Market Feed Header with Tabs
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isLawyer) "الطلبات الواردة في نطاقك (${currentUser.assignedGovernorate})" else "القضايا والطلبات القانونية",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )

          if (isLawyer) {
            Surface(
              color = EmeraldContainer,
              shape = RoundedCornerShape(8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(
                  Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = EmeraldSuccess,
                  modifier = Modifier.size(13.dp)
                )
                Text(
                  text = "${filteredRequests.size} طلبات واردة",
                  color = EmeraldSuccess,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          } else if (isAdmin) {
            Surface(
              color = GoldContainer,
              shape = RoundedCornerShape(8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(
                  Icons.Default.Shield,
                  contentDescription = null,
                  tint = NavyDark,
                  modifier = Modifier.size(13.dp)
                )
                Text(
                  text = "${requests.size} قضية تحت الرقابة",
                  color = NavyDark,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          } else {
            TextButton(onClick = onNewRequestClick) {
              Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("إضافة طلب", color = GoldDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        // Tabs
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.Transparent,
          contentColor = NavyPrimary,
          divider = {}
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Text(
                text = if (isLawyer) "الوارد في نطاقي (${currentUser.assignedGovernorate})" else if (currentUser.role == UserRole.CLIENT) "طلباتي الخاصة" else "جميع الطلبات",
                fontSize = 11.5.sp,
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Text(
                text = if (isLawyer) "عروضي وقضايا التنافس" else "مفتوحة لتلقي العروض",
                fontSize = 11.5.sp,
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = {
              Text(
                text = "قيد المباشرة والضمان",
                fontSize = 11.5.sp,
                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
        }
      }
    }

    // Requests List
    if (filteredRequests.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              if (isLawyer) Icons.Outlined.Inbox else Icons.Outlined.Gavel,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(48.dp)
            )
            Text(
              text = if (isLawyer)
                "لا توجد طلبات واردة حالياً في نطاق اختصاصك (${currentUser.assignedGovernorate})"
              else
                "لا توجد طلبات في هذا القسم حالياً",
              color = TextSecondary,
              fontSize = 14.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (isLawyer) {
              Text(
                text = "بصفتك محامياً متلقياً للطلبات، ستصلك الإشعارات فور طرح أي موكل لقضية أو استشارة تقع ضمن دائرة ${currentUser.assignedDistrict}.",
                color = TextMuted,
                fontSize = 11.5.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
              )
            } else {
              Button(
                onClick = onNewRequestClick,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
              ) {
                Text("طرح طلب قضائي جديد")
              }
            }
          }
        }
      }
    } else {
      items(filteredRequests) { req ->
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
          RequestCardItem(
            request = req,
            onClick = { onRequestClick(req.id) }
          )
        }
      }
    }

    // Featured Lawyers Section
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isLawyer) "دليل الزملاء المعتمدين بالنقابة" else "نخبة المحامين المعتمدين",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )

          TextButton(onClick = onBrowseLawyersClick) {
            Text("عرض الجميع", color = GoldDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }

        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(lawyers.take(5)) { lawyer ->
            LawyerQuickCard(
              lawyer = lawyer,
              onClick = { onLawyerClick(lawyer.id) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun RequestCardItem(
  request: ServiceRequest,
  onClick: () -> Unit
) {
  Surface(
    color = CreamSurface,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
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
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = request.description,
        color = TextSecondary,
        fontSize = 12.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 18.sp
      )

      Divider(color = BorderSubtle, thickness = 0.8.dp)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = request.city, color = TextSecondary, fontSize = 11.sp)
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "${request.bidsCount} عروض", color = GoldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Text(
          text = "${request.budgetAmount.toInt()} ج.م",
          color = NavyPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
fun LawyerQuickCard(
  lawyer: Lawyer,
  onClick: () -> Unit
) {
  Surface(
    color = CreamSurface,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
    shadowElevation = 1.dp,
    modifier = Modifier
      .width(200.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
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
            Text(
              text = lawyer.name,
              color = TextPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(3.dp))
            Icon(Icons.Default.Verified, contentDescription = "مرخص", tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
          }

          Surface(
            color = when (lawyer.degree) {
              LawyerBarDegree.CASSATION -> CrimsonContainer
              LawyerBarDegree.APPEAL -> GoldContainer
              LawyerBarDegree.PRIMARY -> EmeraldContainer
              LawyerBarDegree.GENERAL_TABLE -> NavyContainer
            },
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = lawyer.degree.formalTitleAr,
              color = when (lawyer.degree) {
                LawyerBarDegree.CASSATION -> CrimsonError
                LawyerBarDegree.APPEAL -> GoldDark
                LawyerBarDegree.PRIMARY -> EmeraldSuccess
                LawyerBarDegree.GENERAL_TABLE -> NavyPrimary
              },
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
          }
        }
      }

      Text(
        text = lawyer.specialization.titleAr,
        color = TextSecondary,
        fontSize = 10.5.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Star, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(2.dp))
          Text(text = "${lawyer.rating} (${lawyer.reviewsCount})", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        Text(
          text = "خبرة ${lawyer.yearsExperience} سنة",
          color = TextMuted,
          fontSize = 10.sp
        )
      }
    }
  }
}
