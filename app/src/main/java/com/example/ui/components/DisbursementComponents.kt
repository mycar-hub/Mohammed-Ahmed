package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.theme.*

/**
 * قسم طلبات الدفعات والمصاريف القضائية أثناء فترة إتمام العرض
 */
@Composable
fun DisbursementSection(
  disbursements: List<DisbursementRequest>,
  currentUser: UserProfile,
  isRequestActive: Boolean,
  onRequestDisbursementClick: () -> Unit,
  onAcceptDisbursement: (String) -> Unit,
  onModifyDisbursementClick: (DisbursementRequest) -> Unit,
  onRejectDisbursementClick: (DisbursementRequest) -> Unit,
  onLawyerAcceptCounter: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val isLawyer = currentUser.role == UserRole.LAWYER
  val isClient = currentUser.role == UserRole.CLIENT

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.adaptiveSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
    shadowElevation = 2.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = GoldContainer,
            modifier = Modifier.size(34.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null,
                tint = GoldDark,
                modifier = Modifier.size(20.dp)
              )
            }
          }
          Column {
            Text(
              text = "الدفعات والمصاريف القضائية",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.adaptiveTextPrimary
            )
            Text(
              text = "طلبات الصرف المرحلي ورسوم التقاضي",
              fontSize = 11.sp,
              color = MaterialTheme.adaptiveTextSecondary
            )
          }
        }

        if (isLawyer && isRequestActive) {
          Button(
            onClick = onRequestDisbursementClick,
            colors = ButtonDefaults.buttonColors(
              containerColor = NavyPrimary,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("طلب دفعة / مصاريف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Text(
        text = if (isLawyer)
          "يحق لك أثناء فترة إتمام العرض طلب سلفة مصاريف قضائية (رسوم قيد، إعلان، خبير) أو جزء من الأتعاب المستحقة عن مرحلة منجزة، وللموكل حق القبول أو الرفض أو التعديل."
        else
          "يتاح للمحامي أثناء العمل تقديم طلبات مصاريف قضائية أو دفعات مرحلية. لك كامل الحق في قبول الطلب، رفضه، أو اقتراح تعديل المبلغ.",
        fontSize = 11.sp,
        color = MaterialTheme.adaptiveTextSecondary,
        lineHeight = 16.sp
      )

      Divider(color = BorderSubtle)

      if (disbursements.isEmpty()) {
        Surface(
          color = CreamSurfaceVariant.copy(alpha = 0.5f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
            Text(
              text = "لا توجد طلبات دفعات أو مصاريف قضائية مسجلة حتى الآن.",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          disbursements.forEach { item ->
            DisbursementItemCard(
              item = item,
              currentUser = currentUser,
              onAccept = { onAcceptDisbursement(item.id) },
              onModify = { onModifyDisbursementClick(item) },
              onReject = { onRejectDisbursementClick(item) },
              onLawyerAcceptCounter = { onLawyerAcceptCounter(item.id) }
            )
          }
        }
      }
    }
  }
}

/**
 * بطاقة عرض طلب الصرف وتفاصيله وخيارات الرد للعميل أو المحامي
 */
@Composable
fun DisbursementItemCard(
  item: DisbursementRequest,
  currentUser: UserProfile,
  onAccept: () -> Unit,
  onModify: () -> Unit,
  onReject: () -> Unit,
  onLawyerAcceptCounter: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isClient = currentUser.role == UserRole.CLIENT
  val isLawyer = currentUser.role == UserRole.LAWYER

  val borderColor = when (item.status) {
    DisbursementStatus.PENDING -> GoldSecondary
    DisbursementStatus.ACCEPTED -> EmeraldSuccess
    DisbursementStatus.REJECTED -> CrimsonError
    DisbursementStatus.MODIFIED_BY_CLIENT -> GoldDark
  }

  val containerColor = when (item.status) {
    DisbursementStatus.PENDING -> GoldContainer.copy(alpha = 0.35f)
    DisbursementStatus.ACCEPTED -> EmeraldContainer.copy(alpha = 0.4f)
    DisbursementStatus.REJECTED -> Color(0xFFFEE2E2).copy(alpha = 0.6f)
    DisbursementStatus.MODIFIED_BY_CLIENT -> CreamSurfaceVariant
  }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = containerColor,
    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Top Row: Type badge, Amount, Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = if (item.type == DisbursementType.JUDICIAL_EXPENSES) NavyPrimary else GoldDark,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = item.type.badgeAr,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "${item.amount.toInt()} ج.م",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = NavyDark
          )

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (item.status) {
              DisbursementStatus.PENDING -> GoldContainer
              DisbursementStatus.ACCEPTED -> EmeraldSuccess
              DisbursementStatus.REJECTED -> CrimsonError
              DisbursementStatus.MODIFIED_BY_CLIENT -> GoldSecondary
            }
          ) {
            Text(
              text = item.status.labelAr,
              color = when (item.status) {
                DisbursementStatus.PENDING -> GoldDark
                DisbursementStatus.ACCEPTED, DisbursementStatus.REJECTED -> Color.White
                DisbursementStatus.MODIFIED_BY_CLIENT -> NavyDark
              },
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      // Reason / Details
      Text(
        text = "البيان والسبب: ${item.reason}",
        fontSize = 12.sp,
        color = TextPrimary,
        lineHeight = 16.sp
      )

      if (!item.receiptOrRef.isNullOrBlank()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Icon(Icons.Default.AttachFile, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
          Text(
            text = "رقم الإيصال / المحضر: ${item.receiptOrRef}",
            fontSize = 11.sp,
            color = TextSecondary
          )
        }
      }

      // If Client proposed a counter amount
      if (item.status == DisbursementStatus.MODIFIED_BY_CLIENT && item.counterAmount != null) {
        Surface(
          color = Color.White,
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "⚖️ اقتراح تعديل الموكل:",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = GoldDark
              )
              Text(
                text = "${item.counterAmount.toInt()} ج.م",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = EmeraldSuccess
              )
            }
            if (!item.clientNote.isNullOrBlank()) {
              Text(
                text = "ملاحظة الموكل: \"${item.clientNote}\"",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
            if (isLawyer) {
              Button(
                onClick = onLawyerAcceptCounter,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("الموافقة على المبلغ المعدل (${item.counterAmount.toInt()} ج.م)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // If rejected, show reason
      if (item.status == DisbursementStatus.REJECTED && !item.clientNote.isNullOrBlank()) {
        Surface(
          color = Color.White,
          shape = RoundedCornerShape(6.dp),
          border = androidx.compose.foundation.BorderStroke(0.8.dp, CrimsonError.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "سبب الرفض من الموكل: ${item.clientNote}",
            fontSize = 11.sp,
            color = CrimsonError,
            modifier = Modifier.padding(8.dp)
          )
        }
      }

      // Action Buttons for Client on PENDING requests
      if (isClient && item.status == DisbursementStatus.PENDING) {
        Divider(color = BorderSubtle)
        Text(
          text = "خياراتك كموكل تجاه هذا الطلب:",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = NavyDark
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // 1. Accept Button
          Button(
            onClick = onAccept,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text("قبول", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          // 2. Modify Button
          Button(
            onClick = onModify,
            colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text("تعديل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          // 3. Reject Button
          OutlinedButton(
            onClick = onReject,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text("رفض", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      // Metadata Footer
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = "مقدم من: ${item.lawyerName}", fontSize = 10.sp, color = TextMuted)
        Text(text = item.createdAt, fontSize = 10.sp, color = TextMuted)
      }
    }
  }
}

/**
 * Dialog for Lawyer to submit a new disbursement request
 */
@Composable
fun RequestDisbursementDialog(
  onDismiss: () -> Unit,
  onSubmit: (type: DisbursementType, amount: Double, reason: String, receiptRef: String?) -> Unit
) {
  var selectedType by remember { mutableStateOf(DisbursementType.JUDICIAL_EXPENSES) }
  var amountInput by remember { mutableStateOf("") }
  var reasonInput by remember { mutableStateOf("") }
  var receiptInput by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = NavyPrimary)
        Text("طلب دفعة أتعاب / مصاريف قضائية", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "حدد نوع الطلب وأدخل القيمة والبيان التوضيحي مع إمكانية إرفاق رقم الإيصال أو المحضر.",
          fontSize = 11.sp,
          color = TextSecondary
        )

        // Type selection tabs
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("نوع الطلب *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              modifier = Modifier
                .weight(1f)
                .clickable { selectedType = DisbursementType.JUDICIAL_EXPENSES },
              shape = RoundedCornerShape(8.dp),
              color = if (selectedType == DisbursementType.JUDICIAL_EXPENSES) NavyPrimary else CreamSurfaceVariant,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (selectedType == DisbursementType.JUDICIAL_EXPENSES) NavyPrimary else BorderSubtle
              )
            ) {
              Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "مصاريف قضائية",
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = if (selectedType == DisbursementType.JUDICIAL_EXPENSES) Color.White else NavyDark
                )
                Text(
                  text = "رسوم قيد، خبير، إعلان",
                  fontSize = 9.sp,
                  color = if (selectedType == DisbursementType.JUDICIAL_EXPENSES) GoldLight else TextMuted
                )
              }
            }

            Surface(
              modifier = Modifier
                .weight(1f)
                .clickable { selectedType = DisbursementType.INTERIM_FEE },
              shape = RoundedCornerShape(8.dp),
              color = if (selectedType == DisbursementType.INTERIM_FEE) NavyPrimary else CreamSurfaceVariant,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (selectedType == DisbursementType.INTERIM_FEE) NavyPrimary else BorderSubtle
              )
            ) {
              Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "دفعة مرحلية من الأتعاب",
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = if (selectedType == DisbursementType.INTERIM_FEE) Color.White else NavyDark
                )
                Text(
                  text = "عن إنجاز مرحلة محددة",
                  fontSize = 9.sp,
                  color = if (selectedType == DisbursementType.INTERIM_FEE) GoldLight else TextMuted
                )
              }
            }
          }
        }

        // Amount Input
        Column {
          Text("المبلغ المطلوب (ج.م) *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = amountInput,
            onValueChange = {
              amountInput = it
              errorMessage = null
            },
            placeholder = { Text("مثال: 1000", fontSize = 12.sp) },
            shape = RoundedCornerShape(8.dp),
            colors = maitreTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Reason Input
        Column {
          Text("بيان الصرف والسبب بالتفصيل *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = reasonInput,
            onValueChange = {
              reasonInput = it
              errorMessage = null
            },
            placeholder = { Text("مثال: سداد رسم قيد الدعوى بجدول المحكمة وتوريد أمانة الخبير القضائي...", fontSize = 11.sp) },
            minLines = 3,
            shape = RoundedCornerShape(8.dp),
            colors = maitreTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Receipt Reference
        Column {
          Text("رقم الإيصال / قسيمة السداد / المحضر (اختياري)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = receiptInput,
            onValueChange = { receiptInput = it },
            placeholder = { Text("مثال: إيصال رقم 49281 / خزينة المحكمة", fontSize = 11.sp) },
            shape = RoundedCornerShape(8.dp),
            colors = maitreTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
          )
        }

        if (errorMessage != null) {
          Text(text = errorMessage!!, color = CrimsonError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountInput.toDoubleOrNull()
          if (amt == null || amt <= 0) {
            errorMessage = "يرجى إدخال مبلغ صحيح أكبر من الصفر"
            return@Button
          }
          if (reasonInput.isBlank()) {
            errorMessage = "يرجى كتابة بيان الصرف والسبب"
            return@Button
          }
          onSubmit(selectedType, amt, reasonInput.trim(), receiptInput.trim().ifBlank { null })
        },
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
      ) {
        Text("إرسال الطلب للموكل", fontWeight = FontWeight.Bold)
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
 * Dialog for Client to modify a disbursement request
 */
@Composable
fun ModifyDisbursementDialog(
  item: DisbursementRequest,
  onDismiss: () -> Unit,
  onSubmitModification: (counterAmount: Double, note: String) -> Unit
) {
  var counterAmountInput by remember { mutableStateOf(item.amount.toString()) }
  var noteInput by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Edit, contentDescription = null, tint = GoldDark)
        Text("اقتراح تعديل مبلغ الصرف", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("المبلغ المطلوب من المحامي أصلاً:", fontSize = 11.sp, color = TextSecondary)
            Text("${item.amount.toInt()} ج.م (${item.type.titleAr})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Text("البيان: ${item.reason}", fontSize = 11.sp, color = TextPrimary)
          }
        }

        Column {
          Text("المبلغ المعدل المقترح من قبلك (ج.م) *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = counterAmountInput,
            onValueChange = {
              counterAmountInput = it
              errorMessage = null
            },
            placeholder = { Text("أدخل المبلغ المناسب", fontSize = 12.sp) },
            shape = RoundedCornerShape(8.dp),
            colors = maitreTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
          )
        }

        Column {
          Text("سبب التعديل أو ملاحظاتك للمحامي *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = noteInput,
            onValueChange = {
              noteInput = it
              errorMessage = null
            },
            placeholder = { Text("مثال: رسوم القيد المسددة فعلياً 800 ج.م طبقاً للجدول أو يتم استكمال الباقي بعد ورود التقرير...", fontSize = 11.sp) },
            minLines = 3,
            shape = RoundedCornerShape(8.dp),
            colors = maitreTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
          )
        }

        if (errorMessage != null) {
          Text(text = errorMessage!!, color = CrimsonError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = counterAmountInput.toDoubleOrNull()
          if (amt == null || amt <= 0) {
            errorMessage = "يرجى إدخال مبلغ معدل صحيح"
            return@Button
          }
          if (noteInput.isBlank()) {
            errorMessage = "يرجى كتابة ملاحظات وسبب التعديل"
            return@Button
          }
          onSubmitModification(amt, noteInput.trim())
        },
        colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = NavyDark)
      ) {
        Text("إرسال التعديل للمحامي", fontWeight = FontWeight.Bold)
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
 * Dialog for Client to reject a disbursement request with reason
 */
@Composable
fun RejectDisbursementDialog(
  item: DisbursementRequest,
  onDismiss: () -> Unit,
  onSubmitRejection: (reason: String) -> Unit
) {
  var reasonInput by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Cancel, contentDescription = null, tint = CrimsonError)
        Text("رفض طلب الصرف", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "أنت على وشك رفض طلب (${item.type.titleAr}) بمبلغ ${item.amount.toInt()} ج.م.",
          fontSize = 12.sp,
          color = TextSecondary
        )

        Column {
          Text("سبب الرفض الموجه للمحامي *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = reasonInput,
            onValueChange = {
              reasonInput = it
              errorMessage = null
            },
            placeholder = { Text("مثال: هذه المصاريف مشمولة ضمن الأتعاب المتفق عليها / لم يتم استلام الإيصال الدال على السداد...", fontSize = 11.sp) },
            minLines = 3,
            shape = RoundedCornerShape(8.dp),
            colors = maitreTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
          )
        }

        if (errorMessage != null) {
          Text(text = errorMessage!!, color = CrimsonError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (reasonInput.isBlank()) {
            errorMessage = "يرجى كتابة سبب الرفض"
            return@Button
          }
          onSubmitRejection(reasonInput.trim())
        },
        colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
      ) {
        Text("تأكيد الرفض", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    }
  )
}
