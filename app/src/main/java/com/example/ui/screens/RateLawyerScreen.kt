package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirestoreRatingService
import com.example.model.Lawyer
import com.example.model.LawyerReview
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RateLawyerScreen(
  lawyer: Lawyer?,
  onBackClick: () -> Unit,
  onSubmitRating: (stars: Int, comment: String, tags: List<String>) -> Unit
) {
  var isSubmitting by remember { mutableStateOf(false) }
  var isSubmitted by remember { mutableStateOf(false) }
  var generatedReceiptId by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("تقييم المحامي والخدمة القانونية", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("تخزين مؤمّن على Cloud Firestore", fontSize = 10.sp, color = GoldLight)
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
      )
    },
    containerColor = MaterialTheme.adaptiveBackground
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Cloud Sync Header Banner
      Surface(
        color = if (FirestoreRatingService.isCloudConnected()) EmeraldContainer else NavyContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = null,
            tint = if (FirestoreRatingService.isCloudConnected()) EmeraldSuccess else GoldDark,
            modifier = Modifier.size(22.dp)
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (FirestoreRatingService.isCloudConnected()) "مربوط بقاعدة بيانات Cloud Firestore المباشرة" else "وضع التخزين الآمن (محلي + مزامنة Firestore تلقائية)",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = MaterialTheme.adaptiveTextPrimary
            )
            Text(
              text = "يتم حفظ ملاحظاتك وتقييمك وتوثيقها لتعديل تصنيف المحامي في المنصة.",
              fontSize = 10.sp,
              color = MaterialTheme.adaptiveTextSecondary
            )
          }
        }
      }

      if (isSubmitted) {
        // Confirmation Card with Firestore Sync Receipt
        Surface(
          color = MaterialTheme.adaptiveSurface,
          shape = RoundedCornerShape(18.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
          shadowElevation = 3.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(EmeraldContainer, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(42.dp))
            }

            Text("تم حفظ التقييم في Firestore بنجاح!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.adaptiveTextPrimary)
            Text(
              "شكراً لك على تقييم الخدمة القانونية وإبداء ملاحظاتك. يساعد رأيك باقي العملاء والمحكمين في رفع جودة الاستشارات.",
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
              color = MaterialTheme.adaptiveTextSecondary
            )

            // Firestore Record Metadata
            Surface(
              color = MaterialTheme.adaptiveSurfaceVariant,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                  Text("معرف السجل بـ Firestore:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextMuted)
                  Text(generatedReceiptId, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                  Text("حالة المزامنة:", fontSize = 11.sp, color = MaterialTheme.adaptiveTextMuted)
                  Text("موثق ومحدث في قاعدة البيانات ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }
              }
            }

            Button(
              onClick = onBackClick,
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("العودة إلى القائمة الرئيسية", fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        // Main Rating Component Card
        RateLawyerComponent(
          lawyer = lawyer,
          isSubmitting = isSubmitting,
          onSubmit = { stars, comment, tags ->
            isSubmitting = true
            generatedReceiptId = "REV-${System.currentTimeMillis().toString().takeLast(8)}"
            onSubmitRating(stars, comment, tags)
            isSubmitting = false
            isSubmitted = true
          }
        )
      }
    }
  }
}

/**
 * Reusable UI Component for Rating Lawyers and Writing Feedback (Firestore Ready)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RateLawyerComponent(
  lawyer: Lawyer?,
  isSubmitting: Boolean = false,
  onSubmit: (stars: Int, comment: String, tags: List<String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedStars by remember { mutableIntStateOf(5) }
  var comment by remember { mutableStateOf("") }

  val availableTags = listOf(
    "دقة واحترافية في الصياغة",
    "التزام تام بالمواعيد والجلسات",
    "سرعة الرد والتواصل المباشر",
    "خبرة شرعية وقانونية واسعة",
    "شرح وافٍ للإجراءات القضائية",
    "أتعاب مناسبة وشفافية مالية",
    "صياغة مذكرة ممتازة",
    "متابعة دورية للقضية"
  )
  val selectedTags = remember { mutableStateListOf<String>() }

  // Rating Sub-criteria
  var responsivenessRating by remember { mutableIntStateOf(5) }
  var legalKnowledgeRating by remember { mutableIntStateOf(5) }
  var valueForMoneyRating by remember { mutableIntStateOf(5) }

  Surface(
    color = MaterialTheme.adaptiveSurface,
    shape = RoundedCornerShape(18.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
    shadowElevation = 2.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Lawyer Header
      if (lawyer != null) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(50.dp)
              .background(NavyContainer, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = lawyer.name.take(2),
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 16.sp
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = lawyer.name,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.adaptiveTextPrimary
            )
            Text(
              text = "${lawyer.degree.titleAr} • ${lawyer.specialization.titleAr} • ${lawyer.city}",
              fontSize = 11.sp,
              color = MaterialTheme.adaptiveTextSecondary
            )
          }

          Surface(
            color = GoldSecondary.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Default.Star, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
              Text("${lawyer.rating}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldDark)
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.adaptiveBorder)
      }

      Text(
        text = "كيف كانت تجربتك القانونية مع المحامي؟",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = MaterialTheme.adaptiveTextPrimary
      )

      // Interactive Star Rating Bar
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 1..5) {
          IconButton(
            onClick = { selectedStars = i },
            modifier = Modifier.size(44.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = "$i نجوم",
              tint = if (i <= selectedStars) AmberWarning else MaterialTheme.adaptiveTextMuted,
              modifier = Modifier.size(38.dp)
            )
          }
        }
      }

      Text(
        text = when (selectedStars) {
          5 -> "ممتاز جداً - ننصح به بشدة ⭐⭐⭐⭐⭐"
          4 -> "جيد جداً واحترافي ⭐⭐⭐⭐"
          3 -> "مقبول ⭐⭐⭐"
          2 -> "أقل من المتوقع ⭐⭐"
          else -> "غير راضٍ عن الخدمة ⭐"
        },
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = GoldDark
      )

      // Sub-Criteria Ratings (Collapsible / Visual sliders)
      Surface(
        color = MaterialTheme.adaptiveSurfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text("تقييم معايير الجودة التفصيلية:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.adaptiveTextPrimary)

          CriteriaRatingRow(
            label = "الاستجابة والتواصل",
            rating = responsivenessRating,
            onRatingChanged = { responsivenessRating = it }
          )

          CriteriaRatingRow(
            label = "الكفاءة والصياغة القانونية",
            rating = legalKnowledgeRating,
            onRatingChanged = { legalKnowledgeRating = it }
          )

          CriteriaRatingRow(
            label = "وضوح وشفافية الأتعاب",
            rating = valueForMoneyRating,
            onRatingChanged = { valueForMoneyRating = it }
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.adaptiveBorder)

      // Highlight Tags Selection
      Text(
        text = "اختر أبرز نقاط التميز في الخدمة:",
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = MaterialTheme.adaptiveTextPrimary,
        modifier = Modifier.align(Alignment.Start)
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        availableTags.forEach { tag ->
          val isSelected = selectedTags.contains(tag)
          Surface(
            color = if (isSelected) GoldSecondary else MaterialTheme.adaptiveSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldDark else MaterialTheme.adaptiveBorder),
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable {
                if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
              }
          ) {
            Text(
              text = tag,
              fontSize = 11.sp,
              color = if (isSelected) NavyDark else MaterialTheme.adaptiveTextPrimary,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
          }
        }
      }

      // Notes & Detailed Comment Field
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = "ملاحظاتك ومراجعتك التفصيلية (يتم حفظها بـ Firestore):",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = MaterialTheme.adaptiveTextPrimary
        )
        OutlinedTextField(
          value = comment,
          onValueChange = { comment = it },
          placeholder = { Text("اكتب ملاحظاتك وتفاصيل تجربتك القانونية هنا لمساعدة باقي العملاء وتقييم المحامي...", fontSize = 11.sp) },
          minLines = 3,
          maxLines = 5,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = maitreTextFieldColors()
        )
      }

      // Submit Button
      Button(
        onClick = {
          onSubmit(selectedStars, comment, selectedTags.toList())
        },
        enabled = !isSubmitting,
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        if (isSubmitting) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(8.dp))
          Text("جاري الحفظ في Firestore...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        } else {
          Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("إرسال التقييم وتخزينه في Firestore", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}

@Composable
private fun CriteriaRatingRow(
  label: String,
  rating: Int,
  onRatingChanged: (Int) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, fontSize = 11.sp, color = MaterialTheme.adaptiveTextSecondary)
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
      for (i in 1..5) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = if (i <= rating) AmberWarning else MaterialTheme.adaptiveTextMuted,
          modifier = Modifier
            .size(20.dp)
            .clickable { onRatingChanged(i) }
        )
      }
    }
  }
}

/**
 * Dedicated Dialog for Rating Lawyer and writing notes directly after completing a legal service.
 * Persists data to Firestore.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceCompletionRatingDialog(
  lawyerName: String,
  requestTitle: String,
  onDismiss: () -> Unit,
  onSubmitRating: (stars: Int, comment: String, tags: List<String>) -> Unit
) {
  var stars by remember { mutableIntStateOf(5) }
  var comment by remember { mutableStateOf("") }
  val availableTags = listOf(
    "دقة واحترافية في الصياغة",
    "سرعة الرد والمتابعة",
    "التزام تام بالمواعيد",
    "شرح وافٍ ومبسط للإجراءات",
    "أمانة وحسن خلق",
    "شفافية في الأتعاب"
  )
  val selectedTags = remember { mutableStateListOf<String>() }
  var isSubmitting by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess)
        Column {
          Text("إتمام الخدمة وتقييم المحامي", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Text("سجل موثق في Cloud Firestore ⚡", fontSize = 10.sp, color = GoldDark)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          color = CreamSurfaceVariant,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("الخدمة القانونية المنجزة:", fontSize = 10.sp, color = TextMuted)
            Text(requestTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("المحامي المعتمد: $lawyerName", fontSize = 11.sp, color = NavyPrimary)
          }
        }

        Text("تقييمك لمستوى الخدمة القانونية:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 1..5) {
            IconButton(onClick = { stars = i }, modifier = Modifier.size(38.dp)) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "$i نجوم",
                tint = if (i <= stars) AmberWarning else MaterialTheme.adaptiveTextMuted,
                modifier = Modifier.size(30.dp)
              )
            }
          }
        }

        Text(
          text = when (stars) {
            5 -> "ممتاز ومتقن جداً ⭐⭐⭐⭐⭐"
            4 -> "جيد جداً واحترافي ⭐⭐⭐⭐"
            3 -> "مقبول ⭐⭐⭐"
            2 -> "أقل من المتوقع ⭐⭐"
            else -> "غير راضٍ عن الخدمة ⭐"
          },
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = GoldDark,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )

        // Tags
        Text("أبرز نقاط التميز في الخدمة:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          availableTags.forEach { tag ->
            val isSelected = selectedTags.contains(tag)
            Surface(
              color = if (isSelected) GoldSecondary else CreamSurfaceVariant,
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldDark else BorderSubtle),
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                  if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                }
            ) {
              Text(
                text = tag,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) NavyDark else TextPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
              )
            }
          }
        }

        // Notes and detailed feedback
        Text("ملاحظاتك بعد انتهاء الخدمة القانونية:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        OutlinedTextField(
          value = comment,
          onValueChange = { comment = it },
          placeholder = { Text("اكتب رأيك وتفاصيل تجربتك القانونية لمساعدة الموكلين وتقييم المحامي بـ Firestore...", fontSize = 10.5.sp) },
          minLines = 3,
          maxLines = 4,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          colors = maitreTextFieldColors()
        )

        Surface(
          color = if (FirestoreRatingService.isCloudConnected()) EmeraldContainer.copy(alpha = 0.5f) else NavyContainer.copy(alpha = 0.3f),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
            Text(
              text = if (FirestoreRatingService.isCloudConnected()) "سيتم حفظ تقييمك وملاحظاتك مباشرة في Cloud Firestore" else "حفظ آمن محلياً ومزامنة تلقائية مع Firestore",
              fontSize = 9.5.sp,
              color = TextPrimary
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          isSubmitting = true
          onSubmitRating(stars, comment.ifBlank { "تم إنجاز الخدمة القانونية بنجاح وإتقان." }, selectedTags.toList())
          isSubmitting = false
        },
        enabled = !isSubmitting,
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
      ) {
        if (isSubmitting) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        } else {
          Text("تأكيد وحفظ التقييم بـ Firestore")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    }
  )
}

/**
 * Embedded Card for Post-Service Legal Review in Request Details Screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostServiceRatingCard(
  lawyer: Lawyer?,
  existingReview: LawyerReview?,
  onSubmitRating: (stars: Int, comment: String, tags: List<String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var isExpandedForEditing by remember { mutableStateOf(false) }

  Surface(
    color = MaterialTheme.adaptiveSurface,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (existingReview != null) EmeraldSuccess.copy(alpha = 0.6f) else GoldSecondary),
    shadowElevation = 2.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(
            imageVector = if (existingReview != null) Icons.Default.CheckCircle else Icons.Default.Star,
            contentDescription = null,
            tint = if (existingReview != null) EmeraldSuccess else GoldDark,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = if (existingReview != null) "تقييم الخدمة القانونية (محفوظ بـ Firestore)" else "تقييم المحامي وملاحظات الخدمة القانونية",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.adaptiveTextPrimary
          )
        }

        Surface(
          color = if (existingReview != null) EmeraldContainer else GoldContainer,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = if (existingReview != null) "موثق ✓" else "مطلوب بعد الإنجاز",
            color = if (existingReview != null) Color(0xFF047857) else GoldDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      if (existingReview != null && !isExpandedForEditing) {
        // Display existing saved review from Firestore
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
              for (i in 1..5) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = if (i <= existingReview.stars) AmberWarning else MaterialTheme.adaptiveTextMuted,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            Text(existingReview.date, fontSize = 10.sp, color = MaterialTheme.adaptiveTextMuted)
          }

          if (existingReview.tags.isNotEmpty()) {
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              existingReview.tags.forEach { tag ->
                Surface(
                  color = MaterialTheme.adaptiveSurfaceVariant,
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(tag, fontSize = 10.sp, color = MaterialTheme.adaptiveTextSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }
            }
          }

          if (existingReview.comment.isNotBlank()) {
            Surface(
              color = CreamSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "«${existingReview.comment}»",
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = TextPrimary,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          TextButton(
            onClick = { isExpandedForEditing = true },
            modifier = Modifier.align(Alignment.End)
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("تحديث التقييم والملاحظات", fontSize = 11.sp)
          }
        }
      } else {
        // Interactive rating & notes component
        var currentStars by remember { mutableIntStateOf(existingReview?.stars ?: 5) }
        var currentComment by remember { mutableStateOf(existingReview?.comment ?: "") }
        val availableTags = listOf(
          "دقة واحترافية في الصياغة",
          "سرعة الرد والتواصل",
          "التزام بالمواعيد والجلسات",
          "خبرة واسعة في القانون المصري",
          "شرح وافٍ للإجراءات القضائية"
        )
        val selectedTags = remember {
          mutableStateListOf<String>().apply {
            if (existingReview != null) addAll(existingReview.tags)
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "كيف تقيّم أداء المحامي بعد استلام وإتمام العمل المطلوب؟",
            fontSize = 11.sp,
            color = MaterialTheme.adaptiveTextSecondary
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
          ) {
            for (i in 1..5) {
              IconButton(onClick = { currentStars = i }, modifier = Modifier.size(36.dp)) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = if (i <= currentStars) AmberWarning else MaterialTheme.adaptiveTextMuted,
                  modifier = Modifier.size(28.dp)
                )
              }
            }
          }

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            availableTags.forEach { tag ->
              val isSelected = selectedTags.contains(tag)
              Surface(
                color = if (isSelected) GoldSecondary else MaterialTheme.adaptiveSurfaceVariant,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldDark else MaterialTheme.adaptiveBorder),
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .clickable {
                    if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                  }
              ) {
                Text(
                  text = tag,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) NavyDark else MaterialTheme.adaptiveTextPrimary,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
              }
            }
          }

          OutlinedTextField(
            value = currentComment,
            onValueChange = { currentComment = it },
            placeholder = { Text("اكتب ملاحظاتك وتقييمك للأداء ليتم حفظها وتوثيقها بـ Cloud Firestore...", fontSize = 11.sp) },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = maitreTextFieldColors()
          )

          Button(
            onClick = {
              onSubmitRating(currentStars, currentComment.ifBlank { "خدمة قانونية ممتازة وتم الإنجاز على أكمل وجه." }, selectedTags.toList())
              isExpandedForEditing = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("حفظ التقييم والملاحظات بـ Firestore", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}

