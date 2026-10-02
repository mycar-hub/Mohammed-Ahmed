package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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

/**
 * مكون قائمة منسدلة أنيق وموحد يوفر مساحة الشاشة ويعطي رؤية واضحة ومباشرة للمستخدم
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaitreDropdown(
  label: String,
  selectedValue: String,
  options: List<String>,
  onValueChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
  leadingIcon: @Composable (() -> Unit)? = null
) {
  var expanded by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded },
    modifier = modifier.fillMaxWidth()
  ) {
    OutlinedTextField(
      value = selectedValue,
      onValueChange = {},
      readOnly = true,
      label = { Text(label, fontSize = 11.5.sp) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      leadingIcon = leadingIcon,
      modifier = Modifier
        .menuAnchor()
        .fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      colors = maitreTextFieldColors()
    )

    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.background(MaterialTheme.adaptiveSurface)
    ) {
      options.forEach { option ->
        val isSelected = option == selectedValue
        DropdownMenuItem(
          text = {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = option,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) NavyPrimary else MaterialTheme.adaptiveTextPrimary
              )
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = GoldSecondary,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          },
          onClick = {
            onValueChanged(option)
            expanded = false
          },
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
        )
      }
    }
  }
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
  val isInitialUrgent = initialTemplateId in listOf(
    "template_niyaba_attendance",
    "template_court_urgent_session",
    "template_police_station_report"
  )

  // 0: طلب خدمة عاجلة فورية (Workflow مخصص ومختصر بقوائم منسدلة), 1: طرح قضية / استشارة عامة (النموذج الكامل)
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

  // Geographic Location
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
    "نيابة الأسرة (أحوال شخصية)",
    "نيابة المرور",
    "نيابة الأحداث",
    "نيابة استئناف",
    "أخرى (تحديد نوع النيابة يدوياً)"
  )
  var customNiyabaType by remember { mutableStateOf("") }

  var niyabaSpecificLocation by remember { mutableStateOf("مقر نيابة الدقي الجزئية - مجمع محاكم الجيزة بشارع السودان") }

  // قائمة جنح وجنايات موسعة وشاملة
  val niyabaCaseTypesList = listOf(
    "حيازة وإحراز مواد مخدرة (تعاطي / اتجار) 💊",
    "قيادة تحت تأثير مخدر أو مسكر / تعاطي كحوليات 🚗",
    "شيك بدون رصيد / إيصال أمانة 📜",
    "تبديد وخيانة أمانة / منقولات 💼",
    "نصب واحتيال واستيلاء على أموال 💸",
    "سرقة عادية / جنحة سرقة متجر أو هاتف 🚨",
    "سرقة بالإكراه / جناية سطو ⚠️",
    "جنحة ضرب ومشاجرة وإحداث إصابات 🥊",
    "إحداث عاهة مستديمة / ضرب أفضى إلى موت ⚖️",
    "سب وقذف وابتزاز إلكتروني وتهديد 📱",
    "حيازة سلاح أبيض / سلاح ناري بدون ترخيص 🔫",
    "إتلاف وتخريب عمدي للأموال والممتلكات 🔨",
    "تهرب جمركي وضريبي / قضايا أموال عامة 🏛️",
    "رشوة وتربح واستغلال نفوذ وظيفي 📂",
    "قضية مرور وحادث سير (قتل خطأ / إصابة خطأ) 🚦",
    "نزاع أسري ومصنفات وحقوق ملكية 👨‍👩‍👦",
    "شروع في قتل / تشاجر بالأسلحة ⚔️",
    "غش تجاري واحتكار وقضايا تموينية 📦",
    "أخرى (تحديد وكتابة نوع التهمة يدوياً) 📝"
  )
  var niyabaCaseType by remember { mutableStateOf(niyabaCaseTypesList[0]) }
  var customNiyabaCaseType by remember { mutableStateOf("") }

  // التكييف القانوني الأولي (اختيارياً)
  val legalClassifications = listOf(
    "غير محدد بعد / قيد الفحص والتحقيق ⚪",
    "جنحة (قضية جنح) ⚖️",
    "جناية (قضية جنائية) 🔴",
    "مخالفة إدارية / مالية 🟡"
  )
  var legalClassification by remember { mutableStateOf(legalClassifications[0]) }

  var niyabaSummary by remember {
    mutableStateOf("استدعاء لجلسة تحقيق عاجلة أمام وكيل النيابة، ومطلوب حضور محامٍ مقيد فوراً لمرافقة الموكل وإثبات الدفوع والطلبات وتقديم طلب إخلاء سبيل أو كفالة.")
  }

  val niyabaPersonRoles = listOf("متهم / مشكو في حقه", "شاكي / مقدم البلاغ", "مجني عليه", "شاهد إثبات")
  var niyabaPersonRole by remember { mutableStateOf(niyabaPersonRoles[0]) }

  val urgentTimingOptions = listOf(
    "فوري الآن (خلال 30-60 دقيقة) ⚡",
    "خلال ساعتين اليوم",
    "جلسة تحقيق مسائية (بعد الظهر)",
    "صباح الغد الباكر"
  )
  var niyabaTiming by remember { mutableStateOf(urgentTimingOptions[0]) }

  // =========================================================================
  // 2. WORKFLOW SPECIFIC: تحرير محضر بقسم الشرطة (Police Station Report)
  // =========================================================================
  val policeReportTypesList = listOf(
    "محضر سرقة (منزل / سيارة / هاتف / متعلقات) 🚨",
    "محضر حيازة أو تعاطي مواد مخدرة وكحوليات 💊",
    "محضر تبديد وخيانة أمانة (منقولات زوجية / عهدة) 💼",
    "محضر نصب واحتيال وتوظيف أموال 💸",
    "محضر إتلاف وتخريب عمدي لممتلكات أو سيارة 🔨",
    "محضر سب وقذف وابتزاز إلكتروني وتهديد (مباحث الإنترنت / القسم) 📱",
    "محضر شيك بدون رصيد / إيصال أمانة 📜",
    "محضر تعدي وضرب وإحداث إصابات مع تقرير طبي ⚠️",
    "محضر إثبات حالة ومنازعة حيازة وتمكين 🏠",
    "محضر حيازة سلاح أبيض أو سلاح ناري بدون ترخيص 🔫",
    "محضر مشاجرة واعتداء وتبادل اتهامات 🥊",
    "محضر بلاغ مفقودات رسمي وتوثيق واقعة 📄",
    "محضر مضايقات وتعدي على حرمة الحياة الخاصة 🛡️",
    "أخرى (تحديد وكتابة نوع المحضر يدوياً) 📝"
  )
  var policeReportType by remember { mutableStateOf(policeReportTypesList[0]) }
  var customPoliceReportType by remember { mutableStateOf("") }

  var policeStationName by remember { mutableStateOf("قسم شرطة الدقي") }

  var policeReportSummary by remember {
    mutableStateOf("مطلوب حضور محامٍ برفقة الموكل لتحرير المحضر الرسمي بالقسم ضد المشكو في حقهم وتوثيق الأدلة وشهادة الشهود وإثبات رقم المحضر ومتابعته.")
  }
  var policeTiming by remember { mutableStateOf(urgentTimingOptions[0]) }

  // =========================================================================
  // 3. WORKFLOW SPECIFIC: جلسة محكمة مستعجلة (Urgent Court Session)
  // =========================================================================
  val courtTypesList = listOf(
    "محكمة الجنح الجزئية",
    "محكمة الأسرة (أحوال شخصية)",
    "محكمة الجنايات واستئناف عالي",
    "المحكمة الاقتصادية",
    "دائرة القضاء المستعجل",
    "المحكمة المدنية والتجارية",
    "الدائرة العمالية",
    "محكمة مجلس الدولة (القضاء الإداري)",
    "أخرى (تحديد المحكمة يدوياً)"
  )
  var courtType by remember { mutableStateOf(courtTypesList[0]) }
  var customCourtType by remember { mutableStateOf("") }

  var courtComplexName by remember { mutableStateOf("مجمع محاكم الجيزة (شارع السودان)") }

  val courtActionOptions = listOf(
    "طلب تأجيل إداري للاطلاع وتقديم المستندات والتوكيل ⏱️",
    "إثبات حضور وتقديم أصل التوكيل والمذكرات 📝",
    "طلب إخلاء سبيل أو استئناف أمر الحبس الاحتياطي 🔓",
    "مرافعة عاجلة ودفع شكلي بعدم الاختصاص أو انقضاء الدعوى ⚖️",
    "استخراج شهادة رسمية من الجدول أو إعلان بالحكم 📋",
    "أخرى (تحديد الإجراء يدوياً) ✍️"
  )
  var courtActionRequired by remember { mutableStateOf(courtActionOptions[0]) }
  var customCourtAction by remember { mutableStateOf("") }

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
      // 1. Top Workflow Switcher: Urgent Service vs Standard Case
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
      // MODE A: URGENT SERVICE WORKFLOW (قوائم منسدلة أنيقة وتفاصيل شاملة)
      // =======================================================================
      if (isUrgentMode) {
        // Urgent Service Type Selector
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
                text = "نوع الخدمة العاجلة (مسار عمل منسدل وسريع):",
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
        // 1. حضور أمام النيابة العامة (Niyaba Workflow - Dropdowns)
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
              // 1.1 مكان النيابة ونوعها (Dropdowns)
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                Text("1. مكان النيابة ونوعها المختص *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              // Dropdown: نوع النيابة
              MaitreDropdown(
                label = "نوع النيابة المختصة *",
                selectedValue = niyabaType,
                options = niyabaTypesList,
                onValueChanged = { niyabaType = it },
                leadingIcon = { Icon(Icons.Default.Gavel, contentDescription = null, tint = NavyPrimary) }
              )

              // حقل إدخال يدوي عند اختيار أخرى في نوع النيابة
              AnimatedVisibility(
                visible = niyabaType.startsWith("أخرى"),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
              ) {
                OutlinedTextField(
                  value = customNiyabaType,
                  onValueChange = { customNiyabaType = it },
                  label = { Text("اكتب نوع النيابة يدوياً *", fontSize = 11.5.sp) },
                  placeholder = { Text("مثال: نيابة الشؤون الضريبية والتجارية") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = maitreTextFieldColors(),
                  singleLine = true
                )
              }

              // Dropdown: المحافظة والحي
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                  MaitreDropdown(
                    label = "المحافظة *",
                    selectedValue = selectedGovernorate,
                    options = governoratesList,
                    onValueChanged = { gov ->
                      selectedGovernorate = gov
                      val newDistricts = EgyptLocationHelper.getDistrictsForGovernorate(gov)
                      selectedDistrict = newDistricts.firstOrNull() ?: "المركز الرئيسي"
                      niyabaSpecificLocation = "مقر نيابة $selectedDistrict الجزئية - $selectedGovernorate"
                    },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonError) }
                  )
                }

                Box(modifier = Modifier.weight(1f)) {
                  MaitreDropdown(
                    label = "الحي / الدائرة *",
                    selectedValue = selectedDistrict,
                    options = districtsList,
                    onValueChanged = { dist ->
                      selectedDistrict = dist
                      niyabaSpecificLocation = "مقر نيابة $dist الجزئية - $selectedGovernorate"
                    }
                  )
                }
              }

              // حقل المقر التفصيلي للنيابة
              OutlinedTextField(
                value = niyabaSpecificLocation,
                onValueChange = { niyabaSpecificLocation = it },
                label = { Text("المقر التفصيلي للنيابة أو مجمع المحاكم *", fontSize = 11.5.sp) },
                placeholder = { Text("مثال: مجمع محاكم الجلاء - الدور الرابع نيابة قصر النيل") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = maitreTextFieldColors(),
                singleLine = true
              )

              Divider(color = MaterialTheme.adaptiveBorder, thickness = 0.8.dp)

              // 1.2 نوع القضية والجنحة/الجناية (Dropdown غني وشامل)
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Security, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(18.dp))
                Text("2. نوع القضية والتكييف القانوني وملخص الواقعة *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              // Dropdown: نوع القضية / التهمة (يشمل المخدرات والمسكرات والسرقة والشيكات...)
              MaitreDropdown(
                label = "نوع التحقيق / التهمة المنسوبة *",
                selectedValue = niyabaCaseType,
                options = niyabaCaseTypesList,
                onValueChanged = { niyabaCaseType = it },
                leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GoldSecondary) }
              )

              // حقل إدخال يدوي عند اختيار أخرى في التهمة
              AnimatedVisibility(
                visible = niyabaCaseType.startsWith("أخرى"),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
              ) {
                OutlinedTextField(
                  value = customNiyabaCaseType,
                  onValueChange = { customNiyabaCaseType = it },
                  label = { Text("اكتب نوع التهمة أو القضية يدوياً بالتفصيل *", fontSize = 11.5.sp) },
                  placeholder = { Text("مثال: جنحة خيانة ائتمان وتزوير محرر عرفي") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = maitreTextFieldColors(),
                  singleLine = true
                )
              }

              // Dropdown: التكييف القانوني الأولي (اختيارياً)
              MaitreDropdown(
                label = "التكييف القانوني الأولي (اختياري)",
                selectedValue = legalClassification,
                options = legalClassifications,
                onValueChanged = { legalClassification = it },
                leadingIcon = { Icon(Icons.Default.Balance, contentDescription = null, tint = NavyPrimary) }
              )

              // ملخص مقتضب فقط لموضوع التحقيق
              OutlinedTextField(
                value = niyabaSummary,
                onValueChange = { niyabaSummary = it },
                label = { Text("ملخص مقتضب لموضوع التحقيق فقط *", fontSize = 11.5.sp) },
                placeholder = { Text("اكتب ملخص ما تم استدعاؤه بشأنه والطلبات العاجلة في جلسة التحقيق.") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                minLines = 3,
                colors = maitreTextFieldColors()
              )

              // Dropdowns: صفة الحاضر وموعد الحضور
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                  MaitreDropdown(
                    label = "صفة الموكل المطلوب الحضور معه *",
                    selectedValue = niyabaPersonRole,
                    options = niyabaPersonRoles,
                    onValueChanged = { niyabaPersonRole = it }
                  )
                }

                Box(modifier = Modifier.weight(1f)) {
                  MaitreDropdown(
                    label = "موعد الحضور بالنيابة *",
                    selectedValue = niyabaTiming,
                    options = urgentTimingOptions,
                    onValueChanged = { niyabaTiming = it }
                  )
                }
              }
            }
          }
        }

        // ---------------------------------------------------------------------
        // 2. تحرير محضر بقسم الشرطة (Police Station Workflow - Dropdowns)
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
              // 2.1 نوع المحضر (Dropdown غني)
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocalPolice, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                Text("1. نوع المحضر والقسم المختص *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              MaitreDropdown(
                label = "نوع المحضر المطلوب تحريره *",
                selectedValue = policeReportType,
                options = policeReportTypesList,
                onValueChanged = { policeReportType = it },
                leadingIcon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = EmeraldSuccess) }
              )

              // حقل إدخال يدوي عند اختيار أخرى في نوع المحضر
              AnimatedVisibility(
                visible = policeReportType.startsWith("أخرى"),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
              ) {
                OutlinedTextField(
                  value = customPoliceReportType,
                  onValueChange = { customPoliceReportType = it },
                  label = { Text("اكتب نوع المحضر المطلوب يدوياً *", fontSize = 11.5.sp) },
                  placeholder = { Text("مثال: محضر شروع في سرقة وتعدي على حارس العقار") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = maitreTextFieldColors(),
                  singleLine = true
                )
              }

              // Dropdown: المحافظة والحي
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                  MaitreDropdown(
                    label = "المحافظة *",
                    selectedValue = selectedGovernorate,
                    options = governoratesList,
                    onValueChanged = { gov ->
                      selectedGovernorate = gov
                      val newDistricts = EgyptLocationHelper.getDistrictsForGovernorate(gov)
                      selectedDistrict = newDistricts.firstOrNull() ?: "المركز الرئيسي"
                      policeStationName = "قسم شرطة $selectedDistrict"
                    },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonError) }
                  )
                }

                Box(modifier = Modifier.weight(1f)) {
                  MaitreDropdown(
                    label = "المركز / الحي *",
                    selectedValue = selectedDistrict,
                    options = districtsList,
                    onValueChanged = { dist ->
                      selectedDistrict = dist
                      policeStationName = "قسم شرطة $dist"
                    }
                  )
                }
              }

              // اسم قسم الشرطة
              OutlinedTextField(
                value = policeStationName,
                onValueChange = { policeStationName = it },
                label = { Text("اسم قسم الشرطة أو النقطة المختصة *", fontSize = 11.5.sp) },
                placeholder = { Text("مثال: قسم شرطة الدقي - شارع التحرير") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = maitreTextFieldColors(),
                singleLine = true
              )

              Divider(color = MaterialTheme.adaptiveBorder, thickness = 0.8.dp)

              // 2.2 ملخص موضوع المحضر وموعد التوجه
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Description, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                Text("2. ملخص موضوع البلاغ والأطراف والموعد *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              // Dropdown: التكييف الأولي للبلاغ (اختياري)
              MaitreDropdown(
                label = "التكييف القانوني الأولي للبلاغ (اختياري)",
                selectedValue = legalClassification,
                options = legalClassifications,
                onValueChanged = { legalClassification = it },
                leadingIcon = { Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldDark) }
              )

              // ملخص مقتضب للبلاغ
              OutlinedTextField(
                value = policeReportSummary,
                onValueChange = { policeReportSummary = it },
                label = { Text("ملخص ما حدث والمطلوب إثباته في المحضر فقط *", fontSize = 11.5.sp) },
                placeholder = { Text("اكتب مقتطفات الواقعة والأشخاص المشكو في حقهم والمستندات المرفقة فقط.") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                minLines = 3,
                colors = maitreTextFieldColors()
              )

              // Dropdown: موعد التوجه للقسم
              MaitreDropdown(
                label = "موعد التوجه للقسم لمباشرة المحضر *",
                selectedValue = policeTiming,
                options = urgentTimingOptions,
                onValueChanged = { policeTiming = it },
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = EmeraldSuccess) }
              )
            }
          }
        }

        // ---------------------------------------------------------------------
        // 3. حضور جلسة محكمة مستعجلة (Court Session Workflow - Dropdowns)
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
                Text("1. نوع المحكمة والمقر ورقم الدعوى *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              MaitreDropdown(
                label = "نوع المحكمة والدائرة *",
                selectedValue = courtType,
                options = courtTypesList,
                onValueChanged = { courtType = it },
                leadingIcon = { Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldSecondary) }
              )

              AnimatedVisibility(
                visible = courtType.startsWith("أخرى"),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
              ) {
                OutlinedTextField(
                  value = customCourtType,
                  onValueChange = { customCourtType = it },
                  label = { Text("اكتب نوع المحكمة يدوياً *", fontSize = 11.5.sp) },
                  placeholder = { Text("مثال: محكمة الاستئناف التجاري") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = maitreTextFieldColors(),
                  singleLine = true
                )
              }

              // Dropdown: المحافظة
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                  MaitreDropdown(
                    label = "المحافظة *",
                    selectedValue = selectedGovernorate,
                    options = governoratesList,
                    onValueChanged = { gov ->
                      selectedGovernorate = gov
                      val newDistricts = EgyptLocationHelper.getDistrictsForGovernorate(gov)
                      selectedDistrict = newDistricts.firstOrNull() ?: "المركز الرئيسي"
                      courtComplexName = "مجمع محاكم $selectedGovernorate ($selectedDistrict)"
                    }
                  )
                }

                Box(modifier = Modifier.weight(1f)) {
                  MaitreDropdown(
                    label = "المركز / الدائرة *",
                    selectedValue = selectedDistrict,
                    options = districtsList,
                    onValueChanged = { dist ->
                      selectedDistrict = dist
                      courtComplexName = "مجمع محاكم $selectedGovernorate ($dist)"
                    }
                  )
                }
              }

              OutlinedTextField(
                value = courtComplexName,
                onValueChange = { courtComplexName = it },
                label = { Text("مقر المحكمة ومجمع المحاكم *", fontSize = 11.5.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = maitreTextFieldColors(),
                singleLine = true
              )

              Divider(color = MaterialTheme.adaptiveBorder, thickness = 0.8.dp)

              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.FactCheck, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(18.dp))
                Text("2. الإجراء المطلوب بالجلسة ورقم القضية *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
              }

              MaitreDropdown(
                label = "الإجراء العاجل المطلوب بالجلسة *",
                selectedValue = courtActionRequired,
                options = courtActionOptions,
                onValueChanged = { courtActionRequired = it },
                leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null, tint = CrimsonError) }
              )

              AnimatedVisibility(
                visible = courtActionRequired.startsWith("أخرى"),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
              ) {
                OutlinedTextField(
                  value = customCourtAction,
                  onValueChange = { customCourtAction = it },
                  label = { Text("اكتب الإجراء المطلوب بالجلسة يدوياً *", fontSize = 11.5.sp) },
                  placeholder = { Text("مثال: تقديم تقرير خبير حسابي وطلب ندب لجنة ثلاثية") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = maitreTextFieldColors(),
                  singleLine = true
                )
              }

              OutlinedTextField(
                value = courtCaseNumberAndRoll,
                onValueChange = { courtCaseNumberAndRoll = it },
                label = { Text("رقم القضية / الدائرة / الرول *", fontSize = 11.5.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = maitreTextFieldColors(),
                singleLine = true
              )

              OutlinedTextField(
                value = courtSessionSummary,
                onValueChange = { courtSessionSummary = it },
                label = { Text("ملخص ما ترغب في إنجازه بالجلسة *", fontSize = 11.5.sp) },
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

        // Dropdown: التصنيف القانوني
        MaitreDropdown(
          label = "التصنيف القانوني *",
          selectedValue = standardCategory.titleAr,
          options = RequestCategory.values().map { it.titleAr },
          onValueChanged = { selectedTitle ->
            standardCategory = RequestCategory.values().firstOrNull { it.titleAr == selectedTitle } ?: RequestCategory.COMMERCIAL
          },
          leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = NavyPrimary) }
        )

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

        // Location Dropdowns
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

            // Dropdowns for Governorate & District
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(modifier = Modifier.weight(1f)) {
                MaitreDropdown(
                  label = "المحافظة *",
                  selectedValue = selectedGovernorate,
                  options = governoratesList,
                  onValueChanged = { gov ->
                    selectedGovernorate = gov
                    val newDistricts = EgyptLocationHelper.getDistrictsForGovernorate(gov)
                    selectedDistrict = newDistricts.firstOrNull() ?: "المركز الرئيسي"
                    courtJurisdiction = EgyptLocationHelper.getDefaultJurisdiction(selectedGovernorate, selectedDistrict)
                  }
                )
              }

              Box(modifier = Modifier.weight(1f)) {
                MaitreDropdown(
                  label = "الحي / المركز *",
                  selectedValue = selectedDistrict,
                  options = districtsList,
                  onValueChanged = { dist ->
                    selectedDistrict = dist
                    courtJurisdiction = EgyptLocationHelper.getDefaultJurisdiction(selectedGovernorate, selectedDistrict)
                  }
                )
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

        // Dropdown: درجة الاستعجال
        MaitreDropdown(
          label = "درجة الأهمية والاستعجال القضائي *",
          selectedValue = standardUrgency.labelAr,
          options = RequestUrgency.values().map { it.labelAr },
          onValueChanged = { selectedLabel ->
            standardUrgency = RequestUrgency.values().firstOrNull { it.labelAr == selectedLabel } ?: RequestUrgency.NORMAL
          },
          leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = GoldSecondary) }
        )
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
            val finalTitle: String
            val finalCategory: RequestCategory
            val finalDescription: String
            val finalTemplateId: String
            val specificLocStr: String

            when (urgentServiceType) {
              UrgentServiceType.NIYABA_ATTENDANCE -> {
                val effectiveNiyabaType = if (niyabaType.startsWith("أخرى")) customNiyabaType.ifBlank { "نيابة عامة" } else niyabaType
                val effectiveCaseType = if (niyabaCaseType.startsWith("أخرى")) customNiyabaCaseType.ifBlank { "تحقيق عام" } else niyabaCaseType

                if (niyabaSummary.isBlank()) {
                  errorMessage = "يرجى كتابة ملخص القضية أو موضوع التحقيق المطلوب الحضور فيه"
                  showError = true
                  return@Button
                }
                finalTitle = "حضور فوري وتحقيق عاجل أمام النيابة العامة ($effectiveNiyabaType - $effectiveCaseType)"
                finalCategory = RequestCategory.CRIMINAL_FINANCIAL
                finalTemplateId = "template_niyaba_attendance"
                specificLocStr = niyabaSpecificLocation
                finalDescription = buildString {
                  appendLine("🏛️ نوع النيابة ومقرها: $effectiveNiyabaType ($niyabaSpecificLocation) - محافظة $selectedGovernorate ($selectedDistrict)")
                  appendLine("⚖️ نوع القضية / التهمة: $effectiveCaseType")
                  if (legalClassification != legalClassifications[0]) {
                    appendLine("📌 التكييف القانوني الأولي: $legalClassification")
                  }
                  appendLine("👤 صفة الحاضر المطلوب مرافقته: $niyabaPersonRole")
                  appendLine("⏱️ موعد الحضور المطلوب: $niyabaTiming")
                  appendLine("📝 ملخص موضوع التحقيق:")
                  appendLine(niyabaSummary)
                }
              }

              UrgentServiceType.POLICE_STATION_REPORT -> {
                val effectiveReportType = if (policeReportType.startsWith("أخرى")) customPoliceReportType.ifBlank { "محضر شرطة" } else policeReportType

                if (policeReportSummary.isBlank()) {
                  errorMessage = "يرجى كتابة ملخص موضوع المحضر المطلوب تحريره"
                  showError = true
                  return@Button
                }
                finalTitle = "تحرير $effectiveReportType ($policeStationName - $selectedGovernorate)"
                finalCategory = RequestCategory.CRIMINAL_FINANCIAL
                finalTemplateId = "template_police_station_report"
                specificLocStr = policeStationName
                finalDescription = buildString {
                  appendLine("🚔 نوع المحضر المطلوب تحريره: $effectiveReportType")
                  appendLine("📍 قسم الشرطة المختص: $policeStationName ($selectedGovernorate - $selectedDistrict)")
                  if (legalClassification != legalClassifications[0]) {
                    appendLine("📌 التكييف القانوني الأولي: $legalClassification")
                  }
                  appendLine("⏱️ موعد التوجه للقسم: $policeTiming")
                  appendLine("📝 ملخص موضوع المحضر والوقائع:")
                  appendLine(policeReportSummary)
                }
              }

              UrgentServiceType.COURT_URGENT_SESSION -> {
                val effectiveCourtType = if (courtType.startsWith("أخرى")) customCourtType.ifBlank { "محكمة" } else courtType
                val effectiveAction = if (courtActionRequired.startsWith("أخرى")) customCourtAction.ifBlank { "إجراء مستعجل" } else courtActionRequired

                if (courtCaseNumberAndRoll.isBlank() || courtSessionSummary.isBlank()) {
                  errorMessage = "يرجى كتابة رقم الدعوى وملخص الإجراء المطلوب بالجلسة"
                  showError = true
                  return@Button
                }
                finalTitle = "حضور جلسة محكمة مستعجلة ($effectiveCourtType - $effectiveAction)"
                finalCategory = RequestCategory.LABOR
                finalTemplateId = "template_court_urgent_session"
                specificLocStr = courtComplexName
                finalDescription = buildString {
                  appendLine("⚖️ نوع المحكمة والمقر: $effectiveCourtType ($courtComplexName - $selectedGovernorate)")
                  appendLine("📜 رقم الدعوى والرول: $courtCaseNumberAndRoll")
                  appendLine("⏱️ الإجراء العاجل المطلوب بالجلسة: $effectiveAction")
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
              0.0, // السعر يقدمه المحامي
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
