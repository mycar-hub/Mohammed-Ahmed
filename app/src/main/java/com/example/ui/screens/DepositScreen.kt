package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DepositRecord
import com.example.model.UserProfile
import com.example.ui.theme.*

enum class DepositPaymentType(val titleAr: String, val icon: String) {
  INSTAPAY("إنستاباي (InstaPay)", "التحويل اللحظي المعتمد بالبنك المركزي المصري"),
  VODAFONE_CASH("محافظ إلكترونية (فودافون كاش / أورانج / وي)", "دفع لحظي عبر رقم المحفظة"),
  CREDIT_CARD("بطاقة بنكية (ميزة / فيزا / ماستركارد)", "دفع إلكتروني آمن 3D Secure"),
  BANK_TRANSFER("تحويل بنكي مباشر (البنك الأهلي / بنك مصر / CIB)", "حساب الضمان بالبنوك المصرية")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
  currentUser: UserProfile,
  deposits: List<DepositRecord>,
  onBackClick: () -> Unit,
  onDepositConfirm: (amount: Double, method: String, iban: String?) -> Unit
) {
  var selectedAmount by remember { mutableStateOf(5000.0) }
  var customAmountInput by remember { mutableStateOf("5000") }
  var isCustomAmount by remember { mutableStateOf(false) }
  var selectedMethod by remember { mutableStateOf(DepositPaymentType.INSTAPAY) }
  var transferReference by remember { mutableStateOf("INSTA-902184") }
  var isReceiptUploaded by remember { mutableStateOf(false) }

  val clipboardManager = LocalClipboardManager.current
  val escrowIban = "EG380002000100000020194857211"
  val escrowBankName = "البنك الأهلي المصري - حساب ضمان منصة مِتر القانونية"
  val instaPayAddress = "maitre.escrow@instapay"

  val presetAmounts = listOf(1000.0, 3000.0, 5000.0, 10000.0, 20000.0)

  val scrollState = rememberScrollState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              "إيداع وشحن محفظة الضمان (Escrow)",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              "جمهورية مصر العربية • معاملات آمنة بالجنيه المصري",
              fontSize = 10.sp,
              color = GoldLight
            )
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
        .verticalScroll(scrollState)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Balance Overview Card
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("الرصيد المتاح حالياً", fontSize = 12.sp, color = GoldLight)
              Text(
                "${currentUser.balance.toInt()} ج.م",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
              )
            }

            Surface(
              color = EmeraldSuccess.copy(alpha = 0.2f),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                Text("حماية الضمان 100%", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
              }
            }
          }

          Divider(color = Color.White.copy(alpha = 0.1f))

          Text(
            "المبالغ المودعة تظل بأمان تام في حساب الضمان البنكي الخاضع للبنك المركزي المصري، ولا تسحب أو تسلم للمحامي إلا بموافقتك الرسمية بعد اكتمال ومراجعة الخدمة القانونية.",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f),
            lineHeight = 16.sp
          )
        }
      }

      // Choose Amount Section
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text("حدد مبلغ الإيداع في المحفظة:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.adaptiveTextPrimary)

          // Preset Chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            presetAmounts.take(3).forEach { amount ->
              val isSelected = !isCustomAmount && selectedAmount == amount
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    isCustomAmount = false
                    selectedAmount = amount
                    customAmountInput = amount.toInt().toString()
                  }
                  .border(
                    1.dp,
                    if (isSelected) NavyPrimary else MaterialTheme.adaptiveBorder,
                    RoundedCornerShape(10.dp)
                  ),
                color = if (isSelected) NavyContainer else MaterialTheme.adaptiveSurfaceVariant
              ) {
                Text(
                  text = "${amount.toInt()} ج.م",
                  modifier = Modifier.padding(vertical = 12.dp),
                  textAlign = TextAlign.Center,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            presetAmounts.drop(3).forEach { amount ->
              val isSelected = !isCustomAmount && selectedAmount == amount
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    isCustomAmount = false
                    selectedAmount = amount
                    customAmountInput = amount.toInt().toString()
                  }
                  .border(
                    1.dp,
                    if (isSelected) NavyPrimary else MaterialTheme.adaptiveBorder,
                    RoundedCornerShape(10.dp)
                  ),
                color = if (isSelected) NavyContainer else MaterialTheme.adaptiveSurfaceVariant
              ) {
                Text(
                  text = "${amount.toInt()} ج.م",
                  modifier = Modifier.padding(vertical = 12.dp),
                  textAlign = TextAlign.Center,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary
                )
              }
            }
          }

          // Custom Amount Input
          OutlinedTextField(
            value = customAmountInput,
            onValueChange = {
              customAmountInput = it
              isCustomAmount = true
              selectedAmount = it.toDoubleOrNull() ?: 0.0
            },
            label = { Text("أو أدخل مبلغاً مخصصاً (بالجنيه المصري)") },
            leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldDark) },
            trailingIcon = { Text("ج.م ", fontWeight = FontWeight.Bold, color = MaterialTheme.adaptiveTextPrimary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = maitreTextFieldColors(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
          )
        }
      }

      // Payment Method Section (Egyptian Payment Ecosystem)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text("طرق الدفع والشحن المعتمدة في مصر:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

          DepositPaymentType.values().forEach { method ->
            val isSelected = selectedMethod == method
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { selectedMethod = method }
                .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) GoldDark else BorderSubtle,
                  shape = RoundedCornerShape(12.dp)
                ),
              color = if (isSelected) GoldContainer.copy(alpha = 0.5f) else CreamSurfaceVariant
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                RadioButton(
                  selected = isSelected,
                  onClick = { selectedMethod = method },
                  colors = RadioButtonDefaults.colors(selectedColor = GoldDark)
                )

                Column(modifier = Modifier.weight(1f)) {
                  Text(method.titleAr, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                  Text(method.icon, fontSize = 11.sp, color = TextMuted)
                }

                Icon(
                  imageVector = when (method) {
                    DepositPaymentType.INSTAPAY -> Icons.Default.SendToMobile
                    DepositPaymentType.VODAFONE_CASH -> Icons.Default.PhoneAndroid
                    DepositPaymentType.CREDIT_CARD -> Icons.Default.Payment
                    DepositPaymentType.BANK_TRANSFER -> Icons.Default.AccountBalance
                  },
                  contentDescription = null,
                  tint = if (isSelected) GoldDark else TextMuted
                )
              }
            }
          }

          // Bank / InstaPay details
          if (selectedMethod == DepositPaymentType.INSTAPAY || selectedMethod == DepositPaymentType.BANK_TRANSFER) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = NavyDark,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    if (selectedMethod == DepositPaymentType.INSTAPAY) "عنوان الدفع اللحظي إنستاباي IPA:" else "الحساب البنكي لحماية الضمان:",
                    color = GoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                  TextButton(
                    onClick = {
                      clipboardManager.setText(
                        AnnotatedString(if (selectedMethod == DepositPaymentType.INSTAPAY) instaPayAddress else escrowIban)
                      )
                    }
                  ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = GoldSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نسخ", color = GoldSecondary, fontSize = 11.sp)
                  }
                }

                Text(escrowBankName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = if (selectedMethod == DepositPaymentType.INSTAPAY) instaPayAddress else escrowIban,
                  color = EmeraldSuccess,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                )

                Divider(color = Color.White.copy(alpha = 0.1f))

                OutlinedTextField(
                  value = transferReference,
                  onValueChange = { transferReference = it },
                  label = { Text("رقم العملية المرجعي / رقم الحساب المحول منه", color = GoldLight) },
                  modifier = Modifier.fillMaxWidth(),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldSecondary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                  )
                )

                Button(
                  onClick = { isReceiptUploaded = true },
                  colors = ButtonDefaults.buttonColors(containerColor = NavyContainer),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Icon(Icons.Default.Receipt, contentDescription = null, tint = GoldSecondary)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(if (isReceiptUploaded) "تم إرفاق إشعار التحويل بنجاح ✓" else "إرفاق إشعار التحويل (صورة/PDF)", fontSize = 12.sp, color = Color.White)
                }
              }
            }
          }
        }
      }

      // Fee Breakdown Summary
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("المبلغ المودع في المحفظة:", fontSize = 12.sp, color = TextSecondary)
            Text("${selectedAmount.toInt()} ج.م", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("رسوم منصة مِتر على الإيداع:", fontSize = 12.sp, color = TextSecondary)
            Text("0.00 ج.م (مجاناً)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
          }

          Divider(color = BorderSubtle)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("إجمالي الإيداع بمحفظة الضمان:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Text(
              "${selectedAmount.toInt()} ج.م",
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              color = EmeraldSuccess
            )
          }
        }
      }

      // Action Button
      Button(
        onClick = {
          onDepositConfirm(
            selectedAmount,
            selectedMethod.titleAr,
            if (selectedMethod == DepositPaymentType.BANK_TRANSFER) escrowIban else null
          )
        },
        enabled = selectedAmount > 0,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
      ) {
        Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          "تأكيد الإيداع الفوري (${selectedAmount.toInt()} ج.م)",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = Color.White
        )
      }

      // Deposit History
      if (deposits.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("سجل الإيداعات السابقة في الضمان:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)

          deposits.forEach { dep ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = CreamSurface,
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(EmeraldContainer),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                  }
                  Column {
                    Text("+${dep.amount.toInt()} ج.م", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("${dep.paymentMethod} • مرجع: ${dep.referenceNumber}", fontSize = 11.sp, color = TextMuted)
                  }
                }
                Text(dep.date, fontSize = 11.sp, color = TextMuted)
              }
            }
          }
        }
      }
    }
  }
}
