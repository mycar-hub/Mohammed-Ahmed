package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
  onBackClick: () -> Unit,
  onOpenLocationPicker: () -> Unit,
  chosenLocation: GeoLocation? = null,
  initialTemplateId: String? = null,
  onSubmitRequest: (title: String, category: RequestCategory, description: String, city: String, budget: Double, urgency: RequestUrgency, courtLocation: GeoLocation?, templateId: String?) -> Unit
) {
  var selectedTemplate by remember {
    mutableStateOf(CaseTemplates.ALL.find { it.id == initialTemplateId })
  }

  var title by remember {
    mutableStateOf(selectedTemplate?.defaultTitle ?: "")
  }
  var selectedCategory by remember {
    mutableStateOf(selectedTemplate?.category ?: RequestCategory.COMMERCIAL)
  }
  var description by remember {
    mutableStateOf(selectedTemplate?.detailedDescriptionTemplate ?: "")
  }

  // City (Governorate) & District (Sub-district / Neighborhood)
  var selectedGovernorate by remember {
    mutableStateOf(chosenLocation?.city ?: (selectedTemplate?.defaultGovernorate ?: "الجيزة"))
  }
  var selectedDistrict by remember {
    mutableStateOf(chosenLocation?.district ?: (selectedTemplate?.defaultDistrict ?: "الدقي"))
  }
  var specificLandmark by remember {
    mutableStateOf(chosenLocation?.specificLandmark ?: "")
  }
  var courtJurisdiction by remember {
    mutableStateOf(
      chosenLocation?.courtJurisdiction ?: (selectedTemplate?.defaultCourtJurisdiction ?: EgyptLocationHelper.getDefaultJurisdiction(selectedGovernorate, selectedDistrict))
    )
  }

  var budgetText by remember {
    mutableStateOf(selectedTemplate?.suggestedBudget?.toInt()?.toString() ?: "5000")
  }
  var selectedUrgency by remember {
    mutableStateOf(selectedTemplate?.urgency ?: RequestUrgency.NORMAL)
  }

  var showError by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  val governoratesList = EgyptLocationHelper.governoratesWithDistricts.keys.toList()
  val districtsList = EgyptLocationHelper.getDistrictsForGovernorate(selectedGovernorate)

  // Sync when template changes
  fun applyTemplate(template: CaseTemplate) {
    selectedTemplate = template
    title = template.defaultTitle
    selectedCategory = template.category
    description = template.detailedDescriptionTemplate
    budgetText = template.suggestedBudget.toInt().toString()
    selectedUrgency = template.urgency
    selectedGovernorate = template.defaultGovernorate
    selectedDistrict = template.defaultDistrict
    courtJurisdiction = template.defaultCourtJurisdiction
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "طرح قضية / طلب خدمة قانونية",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(
              imageVector = Icons.Default.ArrowForward,
              contentDescription = "رجوع",
              tint = Color.White
            )
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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Escrow assurance header
      Surface(
        color = GoldContainer,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.padding(12.dp)
        ) {
          Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GoldDark)
          Text(
            text = "طرح الطلب مجاني تماماً. ستصلك عروض أسعار تفصيلية (أتعاب المحامي + المصاريف القضائية + نسبة المنصة)، مع تحديد درجات القيد (ابتدائي / استئناف / نقض)، ولن يتم خصم أي مبالغ إلا بعد موافقتك الصريحة وإيداع المبلغ في محفظة الضمان.",
            color = GoldOnContainer,
            fontSize = 12.sp,
            lineHeight = 17.sp
          )
        }
      }

      // 2. Case Templates Quick Selector (Requirement #1)
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.AutoStories, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
            Text(
              text = "قوالب القضايا الجاهزة والموثقة (اختر لتعبئة النموذج فورياً)",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = NavyDark
            )
          }

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(CaseTemplates.ALL) { template ->
              val isSelected = selectedTemplate?.id == template.id
              Surface(
                color = if (isSelected) NavyPrimary else CreamBackground,
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GoldSecondary) else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    applyTemplate(template)
                  }
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = template.titleAr,
                      color = if (isSelected) Color.White else TextPrimary,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldLight, modifier = Modifier.size(14.dp))
                    }
                  }
                  Spacer(modifier = Modifier.height(3.dp))
                  Text(
                    text = "متوسط الأتعاب: ${template.estimatedBudgetRange}",
                    color = if (isSelected) GoldLight else TextSecondary,
                    fontSize = 10.sp
                  )
                }
              }
            }
          }

          // If a template is selected, show its required documents checklist & recommended degree
          selectedTemplate?.let { tmpl ->
            Surface(
              color = NavyDark.copy(alpha = 0.05f),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "📋 المستندات والبيانات المطلوبة للقالب:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                  )

                  Surface(
                    color = when (tmpl.recommendedBarDegree) {
                      LawyerBarDegree.CASSATION -> CrimsonContainer
                      LawyerBarDegree.APPEAL -> GoldContainer
                      LawyerBarDegree.PRIMARY -> EmeraldContainer
                      LawyerBarDegree.GENERAL_TABLE -> NavyContainer
                    },
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = "درجة القيد المقترحة: ${tmpl.recommendedBarDegree.titleAr}",
                      color = when (tmpl.recommendedBarDegree) {
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

                tmpl.requiredDocuments.forEach { doc ->
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
                    Text(doc, fontSize = 11.sp, color = TextPrimary)
                  }
                }
              }
            }
          }
        }
      }

      // 3. Title field
      Column {
        Text("عنوان الطلب أو القضية *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.adaptiveTextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          placeholder = { Text("مثال: دعوى صحة ونفاذ عقد بيع ابتدائي ونقل الملكية", fontSize = 13.sp) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = maitreTextFieldColors()
        )
      }

      // 4. Category Selector
      Column {
        Text("التصنيف القانوني *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(RequestCategory.values().toList()) { cat ->
            val isSelected = selectedCategory == cat
            Surface(
              color = if (isSelected) NavyPrimary else CreamSurface,
              shape = RoundedCornerShape(10.dp),
              border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { selectedCategory = cat }
            ) {
              Text(
                text = cat.titleAr,
                color = if (isSelected) Color.White else TextPrimary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              )
            }
          }
        }
      }

      // 5. Description field
      Column {
        Text("تفاصيل الوقائع والخدمة المطلوبة *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          placeholder = {
            Text(
              "يرجى شرح التفاصيل القانونية بدقة، الأطراف المعنية، المستندات المتوفرة، والهدف المنشود لتمكين المحامين من تقديم عروض دقيقة...",
              fontSize = 13.sp
            )
          },
          minLines = 4,
          maxLines = 6,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = maitreTextFieldColors()
        )
      }

      // 6. ENHANCED LOCATION SECTION: Detailed Governorate, District & Police/Court jurisdiction
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Dynamic Header according to Template (e.g. مكان الواقعة والنيابة / مكان الوفاة / موقع العقار)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonError)
              Column {
                Text(
                  text = selectedTemplate?.locationLabelTitle ?: "مكان الواقعة / الاختصاص المكاني",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = TextPrimary
                )
                Text(
                  text = "حدد المحافظة والمركز/الحي لتمكين المحامي من تقدير أتعابه ومصروفات الانتقال بدقة",
                  fontSize = 10.sp,
                  color = TextSecondary
                )
              }
            }

            IconButton(onClick = onOpenLocationPicker) {
              Icon(Icons.Default.Map, contentDescription = "خريطة ورادار", tint = NavyPrimary)
            }
          }

          // A. Governorate Selection
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "1. المحافظة (مثال: الجيزة، القاهرة، الإسكندرية) *",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = NavyDark
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(governoratesList) { gov ->
                val isSelected = selectedGovernorate == gov
                Surface(
                  color = if (isSelected) GoldSecondary else CreamBackground,
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldDark else BorderSubtle),
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                      selectedGovernorate = gov
                      val newDistricts = EgyptLocationHelper.getDistrictsForGovernorate(gov)
                      selectedDistrict = newDistricts.firstOrNull() ?: "المركز الرئيسي"
                      courtJurisdiction = EgyptLocationHelper.getDefaultJurisdiction(selectedGovernorate, selectedDistrict)
                    }
                ) {
                  Text(
                    text = gov,
                    color = if (isSelected) NavyDark else TextPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }

          // B. District / Neighborhood / Sub-Center Selection (e.g. الدقي، الحوامدية، المهندسين، 6 أكتوبر)
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "2. الحي / المركز في $selectedGovernorate (مثال: ${if (selectedGovernorate == "الجيزة") "الدقي، الحوامدية، المهندسين" else "مدينة نصر، مصر الجديدة"}) *",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = NavyDark
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(districtsList) { district ->
                val isSelected = selectedDistrict == district
                Surface(
                  color = if (isSelected) NavyPrimary else CreamBackground,
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NavyDark else BorderSubtle),
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                      selectedDistrict = district
                      courtJurisdiction = EgyptLocationHelper.getDefaultJurisdiction(selectedGovernorate, selectedDistrict)
                    }
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    if (isSelected) {
                      Icon(Icons.Default.Check, contentDescription = null, tint = GoldLight, modifier = Modifier.size(12.dp))
                    }
                    Text(
                      text = district,
                      color = if (isSelected) Color.White else TextPrimary,
                      fontSize = 11.5.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                  }
                }
              }
            }
          }

          // C. Specific Landmark / Police Station / Hospital / Street Input (Optional)
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "3. المقر المحدد أو قسم الشرطة / العنوان التفصيلي (اختياري)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = TextSecondary
            )
            OutlinedTextField(
              value = specificLandmark,
              onValueChange = { specificLandmark = it },
              placeholder = {
                Text(
                  when (selectedTemplate?.id) {
                    "template_niyaba_attendance" -> "مثال: قسم شرطة الدقي / نيابة الدقي أو قسم الحوامدية"
                    "template_elam_werasa" -> "مثال: مستشفى الحوامدية العام / شارع الجمهورية بالحوامدية"
                    "template_seha_nafath" -> "مثال: مأمورية الشهر العقاري بالشيخ زايد / عقار رقم 14"
                    else -> "مثال: قسم شرطة $selectedDistrict / محكمة $selectedDistrict الجزئية"
                  },
                  fontSize = 11.5.sp,
                  color = TextMuted
                )
              },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = maitreTextFieldColors()
            )
          }

          // D. Auto Calculated Jurisdiction Badge
          Surface(
            color = NavyDark,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(EmeraldContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
              }
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "الجهة القضائية والنيابة المختصة تلقائياً:",
                  color = GoldLight,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = courtJurisdiction,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp
                )
                Text(
                  text = "النطاق الجغرافي: $selectedGovernorate - $selectedDistrict${if (specificLandmark.isNotBlank()) " ($specificLandmark)" else ""}",
                  color = Color.White.copy(alpha = 0.8f),
                  fontSize = 10.sp
                )
              }
            }
          }
        }
      }

      // 7. Budget & Urgency
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("الميزانية التقديرية (ج.م)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.adaptiveTextPrimary)
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = budgetText,
            onValueChange = { budgetText = it },
            placeholder = { Text("مثال: 5000", fontSize = 13.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = maitreTextFieldColors()
          )
        }

        Column(modifier = Modifier.weight(1.2f)) {
          Text("درجة الأهمية والاستعجال", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.adaptiveTextPrimary)
          Spacer(modifier = Modifier.height(6.dp))
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RequestUrgency.values().forEach { urgency ->
              Surface(
                color = if (selectedUrgency == urgency) NavyPrimary else MaterialTheme.adaptiveSurface,
                shape = RoundedCornerShape(8.dp),
                border = if (selectedUrgency == urgency) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { selectedUrgency = urgency }
              ) {
                Text(
                  text = urgency.labelAr,
                  color = if (selectedUrgency == urgency) Color.White else MaterialTheme.adaptiveTextPrimary,
                  fontSize = 11.sp,
                  fontWeight = if (selectedUrgency == urgency) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      if (showError) {
        Text(
          text = errorMessage,
          color = CrimsonError,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Submit Button
      Button(
        onClick = {
          if (title.isBlank()) {
            errorMessage = "يرجى كتابة عنوان للطلب"
            showError = true
            return@Button
          }
          if (description.isBlank()) {
            errorMessage = "يرجى توضيح تفاصيل الطلب القانوني"
            showError = true
            return@Button
          }
          val budget = budgetText.toDoubleOrNull() ?: 2000.0
          showError = false

          val finalLocation = GeoLocation(
            city = selectedGovernorate,
            district = selectedDistrict,
            courtJurisdiction = courtJurisdiction,
            locationPurpose = selectedTemplate?.locationLabelTitle ?: "مكان الواقعة / الاختصاص القضائي",
            specificLandmark = specificLandmark,
            latitude = if (selectedGovernorate == "الجيزة") 30.0131 else 30.0444,
            longitude = if (selectedGovernorate == "الجيزة") 31.2089 else 31.2357,
            fullAddress = "$selectedGovernorate - $selectedDistrict${if (specificLandmark.isNotBlank()) " ($specificLandmark)" else ""} - $courtJurisdiction"
          )

          onSubmitRequest(
            title,
            selectedCategory,
            description,
            selectedGovernorate,
            budget,
            selectedUrgency,
            finalLocation,
            selectedTemplate?.id
          )
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = NavyPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
      ) {
        Icon(Icons.Default.Send, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("نشر الطلب واستقبال عروض المحامين", fontSize = 15.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
