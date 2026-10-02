package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

enum class UrgentServiceType(val titleAr: String, val icon: String, val defaultTemplateId: String) {
  NIYABA_ATTENDANCE("حضور أمام النيابة العامة", "🏛️", "template_niyaba_attendance"),
  POLICE_STATION_REPORT("تحرير محضر بقسم الشرطة", "🚔", "template_police_station_report"),
  COURT_URGENT_SESSION("حضور جلسة محكمة مستعجلة", "⚖️", "template_court_urgent_session"),
  OTHER_URGENT("خدمة عاجلة أخرى فورية", "⚡", "template_urgent_other")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
  onBackClick: () -> Unit,
  onOpenLocationPicker: () -> Unit,
  chosenLocation: GeoLocation? = null,
  initialTemplateId: String? = null,
  onSubmitRequest: (title: String, category: RequestCategory, description: String, city: String, budget: Double, urgency: RequestUrgency, courtLocation: GeoLocation?, templateId: String?) -> Unit
) {
  // Determine if entering from an urgent template
  val isInitialUrgent = initialTemplateId in listOf(
    "template_niyaba_attendance",
    "template_court_urgent_session",
    "template_police_station_report"
  )

  // 0: طلب خدمة عاجلة فورية (Workflow مخصص ومختصر), 1: طرح قضية / استشارة عامة (النموذج الكامل)
  var isUrgentMode by remember { mutableStateOf(isInitialUrgent) }

  var urgentServiceType by remember {
    mutableStateOf(
      when (initialTemplateId) {
        "template_police_station_report" -> UrgentServiceType.POLICE_STATION_REPORT
        "template_court_urgent_session" -> UrgentServiceType.COURT_URGENT_SESSION
        else -> UrgentServiceType.NIYABA_ATTENDANCE
      }
    )
  }

  // Common location state
  var selectedGovernorate by remember {
    mutableStateOf(chosenLocation?.city ?: "الجيزة")
  }
  var selectedDistrict by remember {
    mutableStateOf(chosenLocation?.district ?: "الدقي")
  }
  val governoratesList = EgyptLocationHelper.governoratesWithDistricts.keys.toList()
  val districtsList = EgyptLocationHelper.getDistrictsForGovernorate(selectedGovernorate)

  // =========================================================================
  // 1. WORKFLOW SPECIFIC: حضور نيابة عامة (Niyaba Attendance)
  // =========================================================================
  var niyabaType by remember { mutableStateOf("نيابة جزئية") }
  val niyabaTypesList = listOf(
    "نيابة جزئية",
    "نيابة كلية",
    "نيابة أمن الدولة العليا",
    "نيابة الأموال العامة والشؤون المالية",
    "نيابة الأسرة",
    "نيابة المرور",
    "نيابة الأحداث",
    "نيابة استئناف"
  )
  var niyabaSpecificLocation by remember { mutableStateOf("مقر نيابة الدقي الجزئية - مجمع محاكم الجيزة بشارع السودان") }
  var niyabaCaseType by remember { mutableStateOf("شيك بدون رصيد / إيصال أمانة") }
  val niyabaCaseTypesList = listOf(
    "شيك بدون رصيد / إيصال أمانة",
    "جنحة ضرب وتشاجر وإصابات",
    "تبديد ونصب وخيانة أمانة",
    "أموال عامة وتهرب ضريبي وجمركي",
    "سرقة وإتلاف عمدي",
    "جناية تلبس ومخدرات",
    "قضية مرور وحوادث سير",
    "نزاع أسري ومصنفات",
    "أخرى (تحديد يدوي)"
  )
  var niyabaSummary by remember {
    mutableStateOf("استدعاء لجلسة تحقيق عاجلة في بلاغ شيك، مطلوب حضور محامٍ مقيد فوراً لمرافقة الموكل أمام وكيل النيابة وإثبات الدفوع وتقديم طلب إخلاء سبيل أو كفالة.")
  }
  var niyabaPersonRole by remember { mutableStateOf("متهم / مشكو في حقه") }
  val niyabaPersonRoles = listOf("متهم / مشكو في حقه", "شاكي / مقدم البلاغ", "مجني عليه", "شاهد إثبات")
  var niyabaTiming by remember { mutableStateOf("فوري الآن (خلال 30-60 دقيقة) ⚡") }
  val urgentTimingOptions = listOf(
    "فوري الآن (خلال 30-60 دقيقة) ⚡",
    "خلال ساعتين اليوم",
    "جلسة تحقيق مسائية (بعد الظهر)",
    "صباح الغد الباكر"
  )

  // =========================================================================
  // 2. WORKFLOW SPECIFIC: تحرير محضر بقسم الشرطة (Police Station Report)
  // =========================================================================
  var policeReportType by remember { mutableStateOf("محضر سرقة 🚨") }
  val policeReportTypesList = listOf(
    "محضر سرقة 🚨",
    "محضر تبديد وخيانة أمانة 💼",
    "محضر نصب واحتيال واستيلاء 💸",
    "محضر إتلاف وتخريب عمدي 🔨",
    "محضر سب وقذف وابتزاز إلكتروني 📱",
    "محضر شيك بدون رصيد / إيصال أمانة 📜",
    "محضر تعدي وضرب وإحداث إصابات ⚠️",
    "محضر إثبات حالة ومنازعة حيازة 🏠",
    "محضر بلاغ مفقودات ورسمي 📄"
  )
  var policeStationName by remember { mutableStateOf("قسم شرطة الدقي") }
  var policeReportSummary by remember {
    mutableStateOf("مطلوب حضور محامٍ برفقة الموكل لتحرير محضر سرقة ضد المشكو في حقهم مع توثيق الأدلة وشهادة الشهود وإثبات رقم المحضر الرسمي ومتابعته.")
  }
  var policeTiming by remember { mutableStateOf("فوري الآن (خلال 30-60 دقيقة) ⚡") }

  // =========================================================================
  // 3. WORKFLOW SPECIFIC: جلسة محكمة مستعجلة (Urgent Court Session)
  // =========================================================================
  var courtType by remember { mutableStateOf("محكمة الجنح الجزئية") }
  val courtTypesList = listOf(
    "محكمة الجنح الجزئية",
    "محكمة الأسرة (أحوال شخصية)",
    "محكمة الجنايات واستئناف عالي",
    "المحكمة الاقتصادية",
    "دائرة القضاء المستعجل",
    "المحكمة المدنية والتجارية",
    "الدائرة العمالية"
  )
  var courtComplexName by remember { mutableStateOf("مجمع محاكم الجيزة (شارع السودان)") }
  var courtActionRequired by remember { mutableStateOf("طلب تأجيل إداري للاطلاع وتقديم المستندات ⏱️") }
  val courtActionOptions = listOf(
    "طلب تأجيل إداري للاطلاع وتقديم المستندات ⏱️",
    "إثبات حضور وتقديم أصل التوكيل والمذكرات 📝",
    "طلب إخلاء سبيل أو استئناف أمر الحبس 🔓",
    "مرافعة عاجلة ودفع شكلي بعدم الاختصاص ⚖️",
    "استخراج شهادة رسمية من الجدول أو إعلان 📋"
  )
  var courtCaseNumberAndRoll by remember { mutableStateOf("قضية رقم 4128 لسنة 2024 جنح - رول رقم 14") }
  var courtSessionSummary by remember {
    mutableStateOf("جلسة اليوم منعقدة بالدائرة، مطلوب حضور المحامي لإثبات التوكيل والصفة وطلب أجل مناسب للاطلاع على تقرير الخبير وتقديم أصل المستندات.")
  }

  // =========================================================================
  // 4. STANDARD CASE FIELDS (النموذج العام)
  // =========================================================================
  var selectedTemplate by remember {
    mutableStateOf(CaseTemplates.ALL.find { it.id == initialTemplateId })
  }
  var standardTitle by remember {
    mutableStateOf(selectedTemplate?.defaultTitle ?: "")
  }
  var standardCategory by remember {
    mutableStateOf(selectedTemplate?.category ?: RequestCategory.COMMERCIAL)
  }
  var standardDescription by remember {
    mutableStateOf(selectedTemplate?.detailedDescriptionTemplate ?: "")
  }
  var standardUrgency by remember {
    mutableStateOf(selectedTemplate?.urgency ?: RequestUrgency.NORMAL)
  }
  var specificLandmark by remember {
    mutableStateOf(chosenLocation?.specificLandmark ?: "")
  }
  var courtJurisdiction by remember {
    mutableStateOf(
      chosenLocation?.courtJurisdiction ?: (selectedTemplate?.defaultCourtJurisdiction ?: EgyptLocationHelper.getDefaultJurisdiction(selectedGovernorate, selectedDistrict))
    )
  }

  var showError by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  // Function to apply template to standard fields
  fun applyTemplate(template: CaseTemplate) {
    selectedTemplate = template
    standardTitle = template.defaultTitle
    standardCategory = template.category
    standardDescription = template.detailedDescriptionTemplate
    standardUrgency = template.urgency
    selectedGovernorate = template.defaultGovernorate
    selectedDistrict = template.defaultDistrict
    courtJurisdiction = template.defaultCourtJurisdiction
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (isUrgentMode) "🚨 طلب خدمة قانونية عاجلة (فوري)" else "طرح قضية / طلب خدمة قانونية",
            fontSize = 16.5.sp,
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = if (isUrgentMode) NavyDark else NavyPrimary)
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
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Top Workflow Switcher: Urgent Service (مخصص ومختصر) vs Standard Case (قضية عادية)
      Surface(
        color = NavyDark,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
        ) {
          // Tab 1: Urgent Service Workflow
          Surface(
            color = if (isUrgentMode) CrimsonError else Color.Transparent,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { isUrgentMode = true }
          ) {
            Row(
              modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "🚨 خدمة عاجلة وفورية",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUrgentMode) Color.White else GoldLight
              )
            }
          }

          // Tab 2: Standard Case Workflow
          Surface(
            color = if (!isUrgentMode) NavyPrimary else Color.Transparent,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { isUrgentMode = false }
          ) {
            Row(
              modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "⚖️ قضية / استشارة عادية",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (!isUrgentMode) Color.White else GoldLight
              )
            }
          }
        }
      }

      // =======================================================================
      // MODE A: URGENT SERVICE WORKFLOW (WORKFLOW مخصص ومختصر جداً)
      // =======================================================================
      if (isUrgentMode) {
        // Urgent Service Type Selector (حضور نيابة / تحرير محضر قسم / جلسة محكمة مستعجلة)
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonError.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Bolt, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(20.dp))
              Text(
                text = "اختر نوع الخدمة العاجلة (مسار عمل مخصص وسريع):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.adaptiveTextPrimary
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              UrgentServiceType.values().take(3).forEach { type ->
                val isSelected = urgentServiceType == type
                Surface(
                  color = if (isSelected) CrimsonError else MaterialTheme.adaptiveSurfaceVariant,
                  shape = RoundedCornerShape(10.dp),
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) CrimsonError else MaterialTheme.adaptiveBorder
                  ),
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                      urgentServiceType = type
                      if (type == UrgentServiceType.POLICE_STATION_REPORT) {
                        policeStationName = "قسم شرطة $selectedDistrict"
                      } else if (type == UrgentServiceType.NIYABA_ATTENDANCE) {
                        niyabaSpecificLocation = "مقر نيابة $selectedDistrict الجزئية"
                      }
                    }
                ) {
                  Column(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Text(type.icon, fontSize = 20.sp)
                    Text(
                      text = when (type) {
                        UrgentServiceType.NIYABA_ATTENDANCE -> "حضور نيابة"
                        UrgentServiceType.POLICE_STATION_REPORT -> "محضر بقسم"
                        UrgentServiceType.COURT_URGENT_SESSION -> "جلسة محكمة"
                        else -> "خدمة أخرى"
                      },
                      fontSize = 11.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary,
                      textAlign = TextAlign.Center
                    )
                  }
                }
              }
            }
          }
        }

        // ---------------------------------------------------------------------
        // 1. حضور أمام النيابة العامة (Niyaba Workflow)
        // ---------------------------------------------------------------------
        if (urgentServiceType == UrgentServiceType.NIYABA_ATTENDANCE) {
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
              // Section 1: Niyaba Type & Location (مكان النيابة ونوعها)
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                Text("1. مكان النيابة ونوعها المختص *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              // A. Niyaba Type Selector
              Text("نوع النيابة:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(niyabaTypesList) { nType ->
                  val isSelected = niyabaType == nType
                  Surface(
                    color = if (isSelected) NavyPrimary else MaterialTheme.adaptiveSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NavyDark else MaterialTheme.adaptiveBorder),
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { niyabaType = nType }
                  ) {
                    Text(
                      text = nType,
                      color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary,
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                  }
                }
              }

              // B. Governorate & District
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text("المحافظة:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                  LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(governoratesList) { gov ->
                      val isSel = selectedGovernorate == gov
                      Surface(
                        color = if (isSel) GoldSecondary else MaterialTheme.adaptiveSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                          .clip(RoundedCornerShape(6.dp))
                          .clickable {
                            selectedGovernorate = gov
                            val newDistricts = EgyptLocationHelper.getDistrictsForGovernorate(gov)
                            selectedDistrict = newDistricts.firstOrNull() ?: "المركز الرئيسي"
                            niyabaSpecificLocation = "مقر نيابة $selectedDistrict الجزئية - $selectedGovernorate"
                          }
                      ) {
                        Text(
                          text = gov,
                          color = if (isSel) NavyDark else MaterialTheme.adaptiveTextPrimary,
                          fontSize = 10.5.sp,
                          fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                      }
                    }
                  }
                }
              }

              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("الحي / الدائرة التابعة للنيابة في $selectedGovernorate:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(districtsList) { district ->
                    val isSel = selectedDistrict == district
                    Surface(
                      color = if (isSel) NavyPrimary else MaterialTheme.adaptiveSurfaceVariant,
                      shape = RoundedCornerShape(6.dp),
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                          selectedDistrict = district
                          niyabaSpecificLocation = "مقر نيابة $district الجزئية - $selectedGovernorate"
                        }
                    ) {
                      Text(
                        text = district,
                        color = if (isSel) Color.White else MaterialTheme.adaptiveTextPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                      )
                    }
                  }
                }
              }

              // C. Specific Niyaba headquarters description
              OutlinedTextField(
                value = niyabaSpecificLocation,
                onValueChange = { niyabaSpecificLocation = it },
                label = { Text("المقر التفصيلي للنيابة أو مجمع المحاكم *", fontSize = 11.sp) },
                placeholder = { Text("مثال: مجمع محاكم الجيزة بشارع السودان - الدور الثالث نيابة الدقي") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = maitreTextFieldColors(),
                singleLine = true
              )

              Divider(color = MaterialTheme.adaptiveBorder, thickness = 0.8.dp)

              // Section 2: Case Type & Brief Summary (نوع القضية وملخص الواقعة فقط)
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(18.dp))
                Text("2. نوع القضية وملخص الواقعة فقط *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              Text("نوع التحقيق / التهمة المنسوبة:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(niyabaCaseTypesList) { cType ->
                  val isSelected = niyabaCaseType == cType
                  Surface(
                    color = if (isSelected) CrimsonError else MaterialTheme.adaptiveSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CrimsonError else MaterialTheme.adaptiveBorder),
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { niyabaCaseType = cType }
                  ) {
                    Text(
                      text = cType,
                      color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary,
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                  }
                }
              }

              // Brief Case Summary only
              OutlinedTextField(
                value = niyabaSummary,
                onValueChange = { niyabaSummary = it },
                label = { Text("ملخص مقتضب لموضوع التحقيق المطلوب الحضور فيه *", fontSize = 11.sp) },
                placeholder = { Text("مثال: استدعاء لسماع الأقوال في محضر شيك، ومطلوب الحضور الفوري لمرافقة الموكل.") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                minLines = 3,
                colors = maitreTextFieldColors()
              )

              // Person Role & Timing
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text("صفة الشخص المطلوب مرافقته:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                  niyabaPersonRoles.forEach { role ->
                    val isSel = niyabaPersonRole == role
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { niyabaPersonRole = role }
                        .padding(vertical = 2.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      RadioButton(
                        selected = isSel,
                        onClick = { niyabaPersonRole = role },
                        colors = RadioButtonDefaults.colors(selectedColor = CrimsonError)
                      )
                      Text(role, fontSize = 10.5.sp, color = if (isSel) CrimsonError else TextPrimary, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                    }
                  }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text("موعد الحضور بالنيابة:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                  urgentTimingOptions.take(3).forEach { t ->
                    val isSel = niyabaTiming == t
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { niyabaTiming = t }
                        .padding(vertical = 2.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      RadioButton(
                        selected = isSel,
                        onClick = { niyabaTiming = t },
                        colors = RadioButtonDefaults.colors(selectedColor = CrimsonError)
                      )
                      Text(t, fontSize = 10.5.sp, color = if (isSel) CrimsonError else TextPrimary, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                    }
                  }
                }
              }
            }
          }
        }

        // ---------------------------------------------------------------------
        // 2. تحرير محضر بقسم الشرطة (Police Station Workflow)
        // ---------------------------------------------------------------------
        if (urgentServiceType == UrgentServiceType.POLICE_STATION_REPORT) {
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
              // Section 1: Police Report Type (نوع المحضر: سرقة / تبديد / نصب / إتلاف...)
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocalPolice, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                Text("1. نوع المحضر المطلوب تحريره *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(policeReportTypesList) { rType ->
                  val isSelected = policeReportType == rType
                  Surface(
                    color = if (isSelected) EmeraldSuccess else MaterialTheme.adaptiveSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) EmeraldSuccess else MaterialTheme.adaptiveBorder),
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { policeReportType = rType }
                  ) {
                    Text(
                      text = rType,
                      color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary,
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                  }
                }
              }

              Divider(color = MaterialTheme.adaptiveBorder, thickness = 0.8.dp)

              // Section 2: Department / Police Station (القسم المختص)
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(18.dp))
                Text("2. تحديد قسم الشرطة المختص جغرافياً *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text("المحافظة:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                  LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(governoratesList) { gov ->
                      val isSel = selectedGovernorate == gov
                      Surface(
                        color = if (isSel) GoldSecondary else MaterialTheme.adaptiveSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                          .clip(RoundedCornerShape(6.dp))
                          .clickable {
                            selectedGovernorate = gov
                            val newDistricts = EgyptLocationHelper.getDistrictsForGovernorate(gov)
                            selectedDistrict = newDistricts.firstOrNull() ?: "المركز الرئيسي"
                            policeStationName = "قسم شرطة $selectedDistrict"
                          }
                      ) {
                        Text(
                          text = gov,
                          color = if (isSel) NavyDark else MaterialTheme.adaptiveTextPrimary,
                          fontSize = 10.5.sp,
                          fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                      }
                    }
                  }
                }
              }

              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("المركز / الحي التابع للقسم:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(districtsList) { district ->
                    val isSel = selectedDistrict == district
                    Surface(
                      color = if (isSel) NavyPrimary else MaterialTheme.adaptiveSurfaceVariant,
                      shape = RoundedCornerShape(6.dp),
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                          selectedDistrict = district
                          policeStationName = "قسم شرطة $district"
                        }
                    ) {
                      Text(
                        text = district,
                        color = if (isSel) Color.White else MaterialTheme.adaptiveTextPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                      )
                    }
                  }
                }
              }

              OutlinedTextField(
                value = policeStationName,
                onValueChange = { policeStationName = it },
                label = { Text("اسم قسم الشرطة أو النقطة المختصة *", fontSize = 11.sp) },
                placeholder = { Text("مثال: قسم شرطة الدقي - شارع التحرير") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = maitreTextFieldColors(),
                singleLine = true
              )

              Divider(color = MaterialTheme.adaptiveBorder, thickness = 0.8.dp)

              // Section 3: Summary of the report & timing (ملخص الواقعة والأطراف فقط)
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Description, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                Text("3. ملخص موضوع البلاغ والأطراف *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              OutlinedTextField(
                value = policeReportSummary,
                onValueChange = { policeReportSummary = it },
                label = { Text("ملخص ما حدث والمطلوب إثباته في المحضر *", fontSize = 11.sp) },
                placeholder = { Text("اكتب مقتطفات الواقعة والأشخاص المشكو في حقهم فقط.") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                minLines = 3,
                colors = maitreTextFieldColors()
              )

              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("موعد التوجه للقسم:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  items(urgentTimingOptions.take(3)) { t ->
                    val isSel = policeTiming == t
                    Surface(
                      color = if (isSel) EmeraldSuccess else MaterialTheme.adaptiveSurfaceVariant,
                      shape = RoundedCornerShape(8.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EmeraldSuccess else MaterialTheme.adaptiveBorder),
                      modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { policeTiming = t }
                    ) {
                      Text(
                        text = t,
                        color = if (isSel) Color.White else MaterialTheme.adaptiveTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // ---------------------------------------------------------------------
        // 3. حضور جلسة محكمة مستعجلة (Court Session Workflow)
        // ---------------------------------------------------------------------
        if (urgentServiceType == UrgentServiceType.COURT_URGENT_SESSION) {
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
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(18.dp))
                Text("1. نوع المحكمة ومجمع المحاكم *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(courtTypesList) { cType ->
                  val isSelected = courtType == cType
                  Surface(
                    color = if (isSelected) NavyPrimary else MaterialTheme.adaptiveSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NavyDark else MaterialTheme.adaptiveBorder),
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { courtType = cType }
                  ) {
                    Text(
                      text = cType,
                      color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary,
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                  }
                }
              }

              OutlinedTextField(
                value = courtComplexName,
                onValueChange = { courtComplexName = it },
                label = { Text("مقر المحكمة ومجمع المحاكم *", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = maitreTextFieldColors(),
                singleLine = true
              )

              Divider(color = MaterialTheme.adaptiveBorder, thickness = 0.8.dp)

              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(18.dp))
                Text("2. الإجراء المطلوب بالجلسة ورقم القضية *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("الإجراء العاجل المطلوب:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                courtActionOptions.forEach { action ->
                  val isSel = courtActionRequired == action
                  Surface(
                    color = if (isSel) GoldContainer else MaterialTheme.adaptiveSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) GoldSecondary else MaterialTheme.adaptiveBorder),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { courtActionRequired = action }
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      RadioButton(
                        selected = isSel,
                        onClick = { courtActionRequired = action },
                        colors = RadioButtonDefaults.colors(selectedColor = GoldDark)
                      )
                      Text(
                        text = action,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) NavyDark else MaterialTheme.adaptiveTextPrimary
                      )
                    }
                  }
                }
              }

              OutlinedTextField(
                value = courtCaseNumberAndRoll,
                onValueChange = { courtCaseNumberAndRoll = it },
                label = { Text("رقم القضية / الدائرة / الرول *", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = maitreTextFieldColors(),
                singleLine = true
              )

              OutlinedTextField(
                value = courtSessionSummary,
                onValueChange = { courtSessionSummary = it },
                label = { Text("ملخص ما ترغب في إنجازه بالجلسة *", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                minLines = 2,
                colors = maitreTextFieldColors()
              )
            }
          }
        }

        // Smart Radar Dispatch Notice
        Surface(
          color = NavyDark,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.Sensors, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text("توزيع راداري فوري مع منبه 20 ثانية ⚡", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text(
                text = "يتم إطلاق إشعار عاجل لكافة المحامين المتاحين في $selectedGovernorate فور النشر لتلقي عروض الأسعار والمباشرة فوراً.",
                color = GoldLight,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
              )
            }
          }
        }
      }

      // =======================================================================
      // MODE B: STANDARD CASE WORKFLOW (طرح قضية / استشارة عادية بالنموذج الكامل)
      // =======================================================================
      if (!isUrgentMode) {
        // Escrow Assurance Header
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
              text = "طرح الطلب مجاني تماماً. يقدم المحامون المقيدون عروض أتعابهم القضائية مباشرة، وتتولى المنصة حساب رسوم الخدمة وإضافتها تلقائياً للإجمالي، ولا يتم سداد أي مبالغ إلا بعد موافقتك الصريحة على العرض الأنسب.",
              color = GoldOnContainer,
              fontSize = 12.sp,
              lineHeight = 17.sp
            )
          }
        }

        // Case Templates Quick Selector
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
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
                  color = if (isSelected) NavyPrimary else MaterialTheme.adaptiveSurfaceVariant,
                  shape = RoundedCornerShape(12.dp),
                  border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GoldSecondary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { applyTemplate(template) }
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
                        color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                      )
                      if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldLight, modifier = Modifier.size(14.dp))
                      }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                      text = "الاختصاص: ${template.defaultCourtJurisdiction}",
                      color = if (isSelected) GoldLight else MaterialTheme.adaptiveTextSecondary,
                      fontSize = 10.sp
                    )
                  }
                }
              }
            }
          }
        }

        // Title Field
        Column {
          Text("عنوان الطلب أو القضية *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.adaptiveTextPrimary)
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = standardTitle,
            onValueChange = { standardTitle = it },
            placeholder = { Text("مثال: دعوى صحة ونفاذ عقد بيع ابتدائي ونقل الملكية", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = maitreTextFieldColors()
          )
        }

        // Category Selector
        Column {
          Text("التصنيف القانوني *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.adaptiveTextPrimary)
          Spacer(modifier = Modifier.height(6.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(RequestCategory.values().toList()) { cat ->
              val isSelected = standardCategory == cat
              Surface(
                color = if (isSelected) NavyPrimary else MaterialTheme.adaptiveSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { standardCategory = cat }
              ) {
                Text(
                  text = cat.titleAr,
                  color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary,
                  fontSize = 11.5.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
              }
            }
          }
        }

        // Description Field
        Column {
          Text("شرح وتفاصيل الطلب القانوني *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.adaptiveTextPrimary)
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = standardDescription,
            onValueChange = { standardDescription = it },
            placeholder = { Text("اشرح ملابسات الدعوى أو الاستشارة والمستندات المتوفرة لديك بالتفصيل...", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 4,
            colors = maitreTextFieldColors()
          )
        }

        // Location Selection (Governorate & District)
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonError)
                Text(
                  text = selectedTemplate?.locationLabelTitle ?: "مكان الواقعة / الاختصاص المكاني",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = MaterialTheme.adaptiveTextPrimary
                )
              }
              IconButton(onClick = onOpenLocationPicker) {
                Icon(Icons.Default.Map, contentDescription = "خريطة ورادار", tint = NavyPrimary)
              }
            }

            // Governorate Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("1. المحافظة *", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(governoratesList) { gov ->
                  val isSelected = selectedGovernorate == gov
                  Surface(
                    color = if (isSelected) GoldSecondary else MaterialTheme.adaptiveSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldDark else MaterialTheme.adaptiveBorder),
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
                      color = if (isSelected) NavyDark else MaterialTheme.adaptiveTextPrimary,
                      fontSize = 11.5.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                  }
                }
              }
            }

            // District Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("2. الحي / المركز في $selectedGovernorate *", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(districtsList) { district ->
                  val isSelected = selectedDistrict == district
                  Surface(
                    color = if (isSelected) NavyPrimary else MaterialTheme.adaptiveSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NavyDark else MaterialTheme.adaptiveBorder),
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .clickable {
                        selectedDistrict = district
                        courtJurisdiction = EgyptLocationHelper.getDefaultJurisdiction(selectedGovernorate, selectedDistrict)
                      }
                  ) {
                    Text(
                      text = district,
                      color = if (isSelected) Color.White else MaterialTheme.adaptiveTextPrimary,
                      fontSize = 11.5.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                  }
                }
              }
            }

            // Specific Landmark
            OutlinedTextField(
              value = specificLandmark,
              onValueChange = { specificLandmark = it },
              placeholder = { Text("المقر المحدد أو قسم الشرطة / العنوان التفصيلي (اختياري)", fontSize = 11.5.sp) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = maitreTextFieldColors()
            )
          }
        }

        // Urgency Level
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.adaptiveSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("درجة الأهمية والاستعجال القضائي", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.adaptiveTextPrimary)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              RequestUrgency.values().forEach { urgency ->
                Surface(
                  color = if (standardUrgency == urgency) NavyPrimary else MaterialTheme.adaptiveSurfaceVariant,
                  shape = RoundedCornerShape(8.dp),
                  border = if (standardUrgency == urgency) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.adaptiveBorder),
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { standardUrgency = urgency }
                ) {
                  Text(
                    text = urgency.labelAr,
                    textAlign = TextAlign.Center,
                    color = if (standardUrgency == urgency) Color.White else MaterialTheme.adaptiveTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = if (standardUrgency == urgency) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                  )
                }
              }
            }
          }
        }
      }

      if (showError) {
        Surface(color = CrimsonContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
          Text(
            text = errorMessage,
            color = CrimsonError,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(10.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // =======================================================================
      // FINAL SUBMIT BUTTON
      // =======================================================================
      Button(
        onClick = {
          if (isUrgentMode) {
            // Compose urgent request data based on specific service workflow
            val finalTitle: String
            val finalCategory: RequestCategory
            val finalDescription: String
            val finalTemplateId: String
            val specificLocStr: String

            when (urgentServiceType) {
              UrgentServiceType.NIYABA_ATTENDANCE -> {
                if (niyabaSummary.isBlank()) {
                  errorMessage = "يرجى كتابة ملخص القضية أو موضوع التحقيق المطلوب الحضور فيه"
                  showError = true
                  return@Button
                }
                finalTitle = "حضور فوري وتحقيق عاجل أمام النيابة العامة ($niyabaType - $niyabaCaseType)"
                finalCategory = RequestCategory.CRIMINAL_FINANCIAL
                finalTemplateId = "template_niyaba_attendance"
                specificLocStr = niyabaSpecificLocation
                finalDescription = buildString {
                  appendLine("🏛️ نوع النيابة ومقرها: $niyabaType ($niyabaSpecificLocation) - محافظة $selectedGovernorate ($selectedDistrict)")
                  appendLine("⚖️ نوع القضية / التهمة: $niyabaCaseType")
                  appendLine("👤 صفة الحاضر المطلوب مرافقته: $niyabaPersonRole")
                  appendLine("⏱️ موعد الحضور المطلوب: $niyabaTiming")
                  appendLine("📝 ملخص موضوع التحقيق:")
                  appendLine(niyabaSummary)
                }
              }

              UrgentServiceType.POLICE_STATION_REPORT -> {
                if (policeReportSummary.isBlank()) {
                  errorMessage = "يرجى كتابة ملخص موضوع المحضر المطلوب تحريره"
                  showError = true
                  return@Button
                }
                finalTitle = "تحرير $policeReportType ($policeStationName - $selectedGovernorate)"
                finalCategory = RequestCategory.CRIMINAL_FINANCIAL
                finalTemplateId = "template_police_station_report"
                specificLocStr = policeStationName
                finalDescription = buildString {
                  appendLine("🚔 نوع المحضر المطلوب تحريره: $policeReportType")
                  appendLine("📍 قسم الشرطة المختص: $policeStationName ($selectedGovernorate - $selectedDistrict)")
                  appendLine("⏱️ موعد التوجه للقسم: $policeTiming")
                  appendLine("📝 ملخص موضوع المحضر والوقائع:")
                  appendLine(policeReportSummary)
                }
              }

              UrgentServiceType.COURT_URGENT_SESSION -> {
                if (courtCaseNumberAndRoll.isBlank() || courtSessionSummary.isBlank()) {
                  errorMessage = "يرجى كتابة رقم الدعوى وملخص الإجراء المطلوب بالجلسة"
                  showError = true
                  return@Button
                }
                finalTitle = "حضور جلسة محكمة مستعجلة ($courtType - $courtActionRequired)"
                finalCategory = RequestCategory.LABOR
                finalTemplateId = "template_court_urgent_session"
                specificLocStr = courtComplexName
                finalDescription = buildString {
                  appendLine("⚖️ نوع المحكمة والمقر: $courtType ($courtComplexName - $selectedGovernorate)")
                  appendLine("📜 رقم الدعوى والرول: $courtCaseNumberAndRoll")
                  appendLine("⏱️ الإجراء العاجل المطلوب بالجلسة: $courtActionRequired")
                  appendLine("📝 تفاصيل ما يرغب الموكل في إنجازه:")
                  appendLine(courtSessionSummary)
                }
              }

              else -> {
                finalTitle = "طلب خدمة قانونية عاجلة وفورية ($selectedGovernorate)"
                finalCategory = RequestCategory.COMMERCIAL
                finalTemplateId = "template_urgent_other"
                specificLocStr = "محافظة $selectedGovernorate"
                finalDescription = "طلب استجابة قانونية عاجلة وفورية بمحافظة $selectedGovernorate"
              }
            }

            showError = false
            val finalLocation = GeoLocation(
              city = selectedGovernorate,
              district = selectedDistrict,
              courtJurisdiction = "$specificLocStr - $selectedGovernorate",
              locationPurpose = when (urgentServiceType) {
                UrgentServiceType.NIYABA_ATTENDANCE -> "مقر النيابة العامة"
                UrgentServiceType.POLICE_STATION_REPORT -> "قسم الشرطة المختص"
                UrgentServiceType.COURT_URGENT_SESSION -> "مجمع المحاكم والقاعة"
                else -> "مقر الواقعة"
              },
              specificLandmark = specificLocStr,
              latitude = if (selectedGovernorate == "الجيزة") 30.0131 else 30.0444,
              longitude = if (selectedGovernorate == "الجيزة") 31.2089 else 31.2357,
              fullAddress = "$selectedGovernorate - $selectedDistrict ($specificLocStr)"
            )

            onSubmitRequest(
              finalTitle,
              finalCategory,
              finalDescription,
              selectedGovernorate,
              0.0, // لا يوجد سعر محدد، يقدم المحامي عرضه
              RequestUrgency.URGENT,
              finalLocation,
              finalTemplateId
            )
          } else {
            // Standard Case Submission
            if (standardTitle.isBlank()) {
              errorMessage = "يرجى كتابة عنوان للطلب"
              showError = true
              return@Button
            }
            if (standardDescription.isBlank()) {
              errorMessage = "يرجى توضيح تفاصيل الطلب القانوني"
              showError = true
              return@Button
            }
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
              standardTitle,
              standardCategory,
              standardDescription,
              selectedGovernorate,
              0.0,
              standardUrgency,
              finalLocation,
              selectedTemplate?.id
            )
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isUrgentMode) CrimsonError else NavyPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
      ) {
        Icon(
          imageVector = if (isUrgentMode) Icons.Default.Bolt else Icons.Default.Send,
          contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isUrgentMode) "إرسال وتنبيه المحامين فورياً (تنبيه 20 ث)" else "نشر الطلب واستقبال عروض المحامين",
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
