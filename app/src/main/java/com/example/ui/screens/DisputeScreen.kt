package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.Dispute
import com.example.model.DisputeStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisputeScreen(
  requestId: String?,
  disputes: List<Dispute>,
  onBackClick: () -> Unit,
  onSubmitDispute: (requestId: String, reason: String, details: String) -> Unit
) {
  val reasons = listOf(
    "عدم الالتزام بنطاق العمل أو الصياغة المتفق عليها",
    "تجاوز المدة الزمنية المحددة في العرض دون إنجاز",
    "عدم الرد أو الانقطاع عن التواصل في مساحة العمل",
    "طلب مبالغ أو أتعاب إضافية خارج العرض المعتمد",
    "سبب آخر يستوجب تدخل لجنة التحكيم"
  )

  var selectedReason by remember { mutableStateOf(reasons[0]) }
  var details by remember { mutableStateOf("") }
  var isSubmitted by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("لجنة التحكيم وفض النزاعات", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
        },
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Notice of Arbitration
      item {
        Surface(
          color = CrimsonContainer,
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = CrimsonError)
            Column {
              Text("آلية فض النزاع بمتر:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CrimsonError)
              Text(
                text = "عند رفع طلب نزاع، يتم فوراً تجميد الأتعاب بمحفظة الضمان المالي ويتم إحالة كافة المحادثات والمستندات للمشرف القانوني بالمنصة للفصل العادل خلال 24 ساعة.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
              )
            }
          }
        }
      }

      // 2. Open Dispute Form if requestId provided
      if (requestId != null && !isSubmitted) {
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
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text("تسجيل نزاع جديد للقضية #$requestId", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

              Text("سبب النزاع الرئيسي:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                reasons.forEach { r ->
                  Surface(
                    color = if (selectedReason == r) GoldContainer else CreamSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedReason == r) GoldSecondary else BorderSubtle),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { selectedReason = r }
                  ) {
                    Text(
                      text = r,
                      fontSize = 12.sp,
                      color = if (selectedReason == r) GoldOnContainer else TextPrimary,
                      fontWeight = if (selectedReason == r) FontWeight.Bold else FontWeight.Normal,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                  }
                }
              }

              Text("شرح تفصيلي للنزاع والأدلة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                placeholder = { Text("يرجى كتابة ما تم الاتفاق عليه وأوجه القصور مع الإشارة للرسائل...", fontSize = 12.sp) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              )

              Button(
                onClick = {
                  if (details.isNotBlank()) {
                    onSubmitDispute(requestId, selectedReason, details)
                    isSubmitted = true
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إرسال النزاع للجنة التحكيم", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      if (isSubmitted) {
        item {
          Surface(
            color = EmeraldContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(6.dp))
              Text("تم تسجيل النزاع وتجميد الأتعاب بنجاح", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF065F46))
              Text("ستتواصل معك إدارة التحكيم عبر مساحة العمل.", fontSize = 12.sp, color = Color(0xFF047857))
            }
          }
        }
      }

      // 3. Existing Disputes List
      item {
        Text("سجل النزاعات والقرارات التحكيمية (${disputes.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
      }

      if (disputes.isEmpty()) {
        item {
          Text("لا توجد نزاعات مفتوحة حالياً. جميع المعاملات تسير بأمان.", color = TextMuted, fontSize = 12.sp)
        }
      } else {
        items(disputes) { disp ->
          Surface(
            color = CreamSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
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
                Text(text = "نزاع: ${disp.requestTitle}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                Surface(
                  color = when (disp.status) {
                    DisputeStatus.UNDER_REVIEW -> AmberContainer
                    else -> EmeraldContainer
                  },
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = disp.status.labelAr,
                    color = when (disp.status) {
                      DisputeStatus.UNDER_REVIEW -> GoldDark
                      else -> Color(0xFF047857)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                  )
                }
              }

              Text(text = "السبب: ${disp.reason}", fontSize = 12.sp, color = TextSecondary)
              Text(text = disp.details, fontSize = 11.sp, color = TextMuted)

              if (disp.adminNote != null) {
                Surface(
                  color = GoldContainer,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(8.dp)) {
                    Text("قرار لجنة التحكيم:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                    Text(disp.adminNote, fontSize = 11.sp, color = GoldOnContainer)
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
