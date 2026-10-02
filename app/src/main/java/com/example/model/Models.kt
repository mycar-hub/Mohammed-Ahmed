package com.example.model

enum class UserRole(val labelAr: String) {
  CLIENT("عميل / مستخدم"),
  LAWYER("محامي معتمد"),
  ADMIN("مشرف المنصة")
}

enum class ClientType(val labelAr: String) {
  INDIVIDUAL("فرد / موكل طبيعي"),
  CORPORATE("شركة / منشأة تجارية واستثمار")
}

enum class KycVerificationStatus(val labelAr: String) {
  VERIFIED("موثق بالهوية الوطنية الرقمية (KYC) ✓"),
  PENDING_REVIEW("قيد تدقيق ومطابقة المستندات"),
  REJECTED("مرفوض - مستندات غير مكتملة")
}

/**
 * الألقاب المهنية الشرفية للمحامي
 */
enum class LawyerTitle(val labelAr: String) {
  COUNSELOR("المستشار"),
  PROFESSOR("الأستاذ"),
  MR("السيد"),
  ATTORNEY("المحامي"),
  DOCTOR("الدكتور")
}

/**
 * درجات قيد المحامين بنقابة المحامين المصرية
 */
enum class LawyerBarDegree(
  val titleAr: String,
  val formalTitleAr: String,
  val descAr: String,
  val allowedCourtsAr: String
) {
  GENERAL_TABLE(
    titleAr = "جدول عام",
    formalTitleAr = "محامٍ مقيد بالجدول العام (تحت التمرين)",
    descAr = "قيد أولي بنقابة المحامين - الحضور والتدريب ومتابعة الإجراءات الإدارية والتحقيقات تحت إشراف محامٍ استئناف",
    allowedCourtsAr = "حضور التحقيقات • الأعمال الإدارية • التنسيق القضائي"
  ),
  PRIMARY(
    titleAr = "ابتدائي",
    formalTitleAr = "محامٍ أمام المحاكم الابتدائية والجزئية",
    descAr = "مرخص للترافع ومباشرة الدعاوى أمام المحاكم الجزئية والابتدائية بكافة المحافظات المصرية",
    allowedCourtsAr = "المحاكم الجزئية • المحاكم الابتدائية • لجان فض المنازعات"
  ),
  APPEAL(
    titleAr = "استئناف",
    formalTitleAr = "محامٍ أمام محاكم الاستئناف العالي ومجلس الدولة",
    descAr = "مرخص للترافع أمام محاكم الاستئناف العالي ومحاكم القضاء الإداري والتأديبية والمحاكم الاقتصادية",
    allowedCourtsAr = "محاكم الاستئناف العالي • مجلس الدولة • المحاكم الاقتصادية"
  ),
  CASSATION(
    titleAr = "نقض",
    formalTitleAr = "محامٍ مقيد أمام محكمة النقض والدستورية العليا",
    descAr = "أعلى درجات القيد - مرخص أمام محكمة النقض والإدارية العليا والمحكمة الدستورية العليا",
    allowedCourtsAr = "محكمة النقض • المحكمة الدستورية العليا • المحكمة الإدارية العليا"
  )
}

/**
 * محرك التحقق والتدقيق الرقمي للهوية المصرية (Egyptian KYC Helper)
 */
object EgyptianKycHelper {
  fun validateNationalId(nid: String): KycValidationResult {
    val clean = nid.trim()
    if (clean.length != 14 || !clean.all { it.isDigit() }) {
      return KycValidationResult(isValid = false, error = "الرقم القومي يجب أن يتكون من 14 رقماً مصرياً صحيحاً.")
    }
    val centuryDigit = clean[0].digitToInt()
    if (centuryDigit != 2 && centuryDigit != 3) {
      return KycValidationResult(isValid = false, error = "الرقم القومي غير صالح (خانة القرن غير مطابقة).")
    }
    val year = (if (centuryDigit == 2) 1900 else 2000) + clean.substring(1, 3).toInt()
    val month = clean.substring(3, 5).toInt()
    val day = clean.substring(5, 7).toInt()
    if (month !in 1..12 || day !in 1..31) {
      return KycValidationResult(isValid = false, error = "تاريخ الميلاد المشفر بالرقم القومي غير صالح ($day/$month/$year).")
    }
    val govCode = clean.substring(7, 9)
    val govName = governorateCodes[govCode] ?: "محافظة مصرية"
    val genderDigit = clean[12].digitToInt()
    val gender = if (genderDigit % 2 == 1) "ذكر" else "أنثى"

    return KycValidationResult(
      isValid = true,
      birthDate = String.format("%02d/%02d/%04d", day, month, year),
      governorate = govName,
      gender = gender,
      nationalIdFormatted = "${clean.substring(0, 1)} ${clean.substring(1, 7)} ${clean.substring(7, 9)} ${clean.substring(9, 13)} ${clean.substring(13)}"
    )
  }

  val governorateCodes = mapOf(
    "01" to "القاهرة", "02" to "الإسكندرية", "03" to "بورسعيد", "04" to "السويس",
    "11" to "دمياط", "12" to "الدقهلية", "13" to "الشرقية", "14" to "القليوبية",
    "15" to "كفر الشيخ", "16" to "الغربية", "17" to "المنوفية", "18" to "البحيرة",
    "19" to "الإسماعيلية", "21" to "الجيزة", "22" to "بني سويف", "23" to "الفيوم",
    "24" to "المنيا", "25" to "أسيوط", "26" to "سوهاج", "27" to "قنا",
    "28" to "أسوان", "29" to "الأقصر", "31" to "البحر الأحمر", "32" to "الوادي الجديد",
    "33" to "مطروح", "34" to "شمال سيناء", "35" to "جنوب سيناء", "88" to "مواطنون بالخارج"
  )
}

data class KycValidationResult(
  val isValid: Boolean,
  val error: String? = null,
  val birthDate: String? = null,
  val governorate: String? = null,
  val gender: String? = null,
  val nationalIdFormatted: String? = null
)

data class UserProfile(
  val id: String,
  val name: String,
  val email: String,
  val phone: String,
  val role: UserRole,
  val balance: Double,
  val licenseNumber: String? = null,
  val barDegree: LawyerBarDegree = LawyerBarDegree.APPEAL,
  val isVerified: Boolean = false,
  val pendingVerification: Boolean = false,
  val isLoggedIn: Boolean = true,
  val nationalIdOrCr: String = "29408151203948",
  val companyName: String? = null,
  val clientType: ClientType = ClientType.INDIVIDUAL,
  val nafathVerified: Boolean = true, // تم توثيق الرقم القومي والتحقق
  val kycStatus: KycVerificationStatus = KycVerificationStatus.VERIFIED,
  val officeAddress: String? = "القاهرة - مصر الجديدة - شارع الأهرام - برج مِتر القانوني",
  val assignedGovernorate: String = "القاهرة",
  val assignedDistrict: String = "مصر الجديدة",
  val assignedCourtJurisdiction: String = "نيابة مصر الجديدة الجزئية • محكمة مصر الجديدة الابتدائية",
  val subBarAssociation: String = "نقابة محامي شمال القاهرة (العباسية)",
  val lawyerTitle: LawyerTitle = LawyerTitle.COUNSELOR,
  val bio: String = "محامٍ ومستشار قانوني مقيد بنقابة المحامين ومتخصص في تقديم الاستشارات القانونية وتمثيل الموكلين.",
  val officeAddressManually: String = "القاهرة - مصر الجديدة - شارع الأهرام - برج مِتر القانوني",
  val officeLatitude: Double? = 30.0911,
  val officeLongitude: Double? = 31.3253,
  val desiredPracticeDegrees: List<String> = listOf(
    "المحاكم الابتدائية ومحاكم الجنح المستأنفة ولجان التوفيق",
    "المحاكم الجزئية ومتابعة تحقيقات النيابة والتدريب"
  ),
  val selectedGovernorates: List<String> = listOf("القاهرة", "الجيزة"),
  val selectedCourts: List<String> = listOf(
    "دار القضاء العالي (محكمة النقض واستئناف القاهرة)",
    "مجمع محاكم شمال القاهرة (العباسية)",
    "محكمة الجيزة الابتدائية (شارع السودان)"
  ),
  val selectedDistricts: List<String> = listOf("مصر الجديدة", "الدقي", "مدينة نصر", "التجمع الخامس"),
  val nationalIdFrontUploaded: Boolean = true,
  val nationalIdBackUploaded: Boolean = true,
  val barCardUploaded: Boolean = false,
  val barCardBackUploaded: Boolean = false,
  val nationalIdCardFrontUri: String? = null,
  val nationalIdCardBackUri: String? = null,
  val barCardFrontUri: String? = null,
  val barCardBackUri: String? = null,
  val rejectionReason: String? = null,
  val isOfficeLocationActivated: Boolean = true, // تفعيل واعتماد الموقع الرسمي للمكتب
  val officeLocationPostponed: Boolean = false // تأجيل تحديد الموقع لحين الاعتماد
) {
  fun isWithinLawyerJurisdiction(requestCity: String, courtLocation: GeoLocation?): Boolean {
    if (role != UserRole.LAWYER) return true
    val allGovs = (listOf(assignedGovernorate) + selectedGovernorates).filter { it.isNotBlank() }
    val cleanReqCity = requestCity.trim()
    if (allGovs.any { it.trim().equals(cleanReqCity, ignoreCase = true) }) return true
    if (courtLocation != null) {
      val cleanCourtCity = courtLocation.city.trim()
      if (allGovs.any { it.trim().equals(cleanCourtCity, ignoreCase = true) }) return true
    }
    return false
  }

  fun canAcceptRequests(): Boolean {
    if (role != UserRole.LAWYER) return true
    return isOfficeLocationActivated && !officeAddressManually.isNullOrBlank()
  }
}

/**
 * تصنيفات درجات العمل القضائي التي يرغب المحامي في المرافعة أمامها
 */
enum class JudicialPracticeCategory(
  val id: String,
  val titleAr: String,
  val subtitleAr: String,
  val minRequiredDegree: LawyerBarDegree,
  val degreeRank: Int
) {
  TRAINEE_AND_SUMMARY(
    id = "cat_trainee",
    titleAr = "المحاكم الجزئية ومتابعة تحقيقات النيابة والأعمال الإدارية",
    subtitleAr = "حضور التحقيقات والمحاكم الجزئية ومتابعة الأقسام والنيابات",
    minRequiredDegree = LawyerBarDegree.GENERAL_TABLE,
    degreeRank = 1
  ),
  PRIMARY_AND_MISDEMEANOR(
    id = "cat_primary",
    titleAr = "المحاكم الابتدائية والجنح المستأنفة ولجان التوفيق",
    subtitleAr = "المرافعة في الدعاوى المدنية والتجارية والعمالية والشرعية الابتدائية",
    minRequiredDegree = LawyerBarDegree.PRIMARY,
    degreeRank = 2
  ),
  HIGH_APPEAL_AND_STATE_COUNCIL(
    id = "cat_appeal",
    titleAr = "محاكم الاستئناف العالي ومحاكم القضاء الإداري ومجلس الدولة",
    subtitleAr = "مباشرة طعون الاستئناف العالي ومجلس الدولة والمحاكم التأديبية",
    minRequiredDegree = LawyerBarDegree.APPEAL,
    degreeRank = 3
  ),
  CASSATION_AND_CONSTITUTIONAL(
    id = "cat_cassation",
    titleAr = "محكمة النقض والمحكمة الدستورية العليا والإدارية العليا",
    subtitleAr = "إعداد صحف ومذكرات الطعن بالنقض والدستورية ومنازعات التنفيذ",
    minRequiredDegree = LawyerBarDegree.CASSATION,
    degreeRank = 4
  ),
  ARBITRATION_AND_ECONOMIC(
    id = "cat_arbitration",
    titleAr = "هيئات التحكيم الدولي والمحاكم الاقتصادية المتخصصة",
    subtitleAr = "تمثيل أطراف النزاع وصياغة لوائح ومذكرات التحكيم التجاري",
    minRequiredDegree = LawyerBarDegree.APPEAL,
    degreeRank = 3
  );

  companion object {
    fun isAllowedForActualDegree(category: JudicialPracticeCategory, actualDegree: LawyerBarDegree): Boolean {
      val actualRank = when (actualDegree) {
        LawyerBarDegree.GENERAL_TABLE -> 1
        LawyerBarDegree.PRIMARY -> 2
        LawyerBarDegree.APPEAL -> 3
        LawyerBarDegree.CASSATION -> 4
      }
      return category.degreeRank <= actualRank
    }
  }
}

/**
 * النقابات الفرعية المصرية التابعة للمحامين
 */
object EgyptSubBarHelper {
  val egyptianSubBars = listOf(
    "نقابة محامي شمال القاهرة (العباسية)",
    "نقابة محامي جنوب القاهرة (زينهم / باب الخلق)",
    "نقابة محامي الجيزة (شارع السودان)",
    "نقابة محامي الإسكندرية (المنشية)",
    "نقابة محامي شمال القليوبية (بنها)",
    "نقابة محامي جنوب القليوبية (شبرا الخيمة)",
    "نقابة محامي الشرقية - جنوب (الزقازيق)",
    "نقابة محامي الشرقية - شمال (فاقوس)",
    "نقابة محامي الدقهلية - جنوب (المنصورة)",
    "نقابة محامي الدقهلية - شمال (دكرنس)",
    "نقابة محامي الغربية (طنطا)",
    "نقابة محامي الغربية (المحلة الكبرى)",
    "نقابة محامي المنوفية (شبين الكوم)",
    "نقابة محامي البحيرة - شمال (دمنهور)",
    "نقابة محامي البحيرة - جنوب (إيتاي البارود)",
    "نقابة محامي كفر الشيخ",
    "نقابة محامي دمياط",
    "نقابة محامي بورسعيد",
    "نقابة محامي الإسماعيلية",
    "نقابة محامي السويس",
    "نقابة محامي الفيوم",
    "نقابة محامي بني سويف",
    "نقابة محامي المنيا - شمال (مغاغة)",
    "نقابة محامي المنيا - جنوب (المنيا)",
    "نقابة محامي أسيوط - شمال (منفلوط)",
    "نقابة محامي أسيوط - جنوب (أسيوط)",
    "نقابة محامي سوهاج",
    "نقابة محامي قنا",
    "نقابة محامي الأقصر",
    "نقابة محامي أسوان",
    "نقابة محامي البحر الأحمر (الغردقة)",
    "نقابة محامي مرسى مطروح",
    "نقابة محامي الوادي الجديد (الخارجة)",
    "نقابة محامي شمال سيناء (العريش)",
    "نقابة محامي جنوب سيناء (طور سيناء)"
  )
}

/**
 * دليل المحاكم المصرية والمناطق والمحافظات
 */
object EgyptCourtsDirectory {
  val governoratesList = listOf(
    "القاهرة", "الجيزة", "الإسكندرية", "القليوبية", "الشرقية",
    "الدقهلية", "الغربية", "المنوفية", "البحيرة", "كفر الشيخ",
    "دمياط", "بورسعيد", "الإسماعيلية", "السويس", "الفيوم",
    "بني سويف", "المنيا", "أسيوط", "سوهاج", "قنا",
    "الأقصر", "أسوان", "البحر الأحمر", "مطروح", "الوادي الجديد",
    "شمال سيناء", "جنوب سيناء"
  )

  val majorCourtsByGov = mapOf(
    "القاهرة" to listOf(
      "دار القضاء العالي (محكمة النقض واستئناف القاهرة)",
      "مجمع محاكم القاهرة الجديدة (التجمع الخامس)",
      "مجمع محاكم شمال القاهرة (العباسية)",
      "مجمع محاكم جنوب القاهرة (زينهم / باب الخلق)",
      "مجمع محاكم مصر الجديدة ومدينة نصر",
      "محكمة القاهرة الاقتصادية (التجمع الخامس)",
      "مجلس الدولة والمحكمة الإدارية العليا (الدقي / القاهرة)"
    ),
    "الجيزة" to listOf(
      "محكمة الجيزة الابتدائية (شارع السودان)",
      "مجمع محاكم تاج الدول (إمبابة)",
      "مجمع محاكم 6 أكتوبر والشيخ زايد",
      "مجمع محاكم جنوب الجيزة (الهرم وفيصل)",
      "محكمة شمال الجيزة (الكيت كات)"
    ),
    "الإسكندرية" to listOf(
      "مجمع محاكم المنشية (محكمة استئناف الإسكندرية)",
      "محكمة غرب الإسكندرية (الدخيلة)",
      "محكمة شرق الإسكندرية (سيدي جابر)",
      "محكمة الإسكندرية الاقتصادية",
      "مجلس الدولة ومحكمة القضاء الإداري بالإسكندرية"
    ),
    "القليوبية" to listOf(
      "مجمع محاكم بنها الابتدائي والاستئناف",
      "مجمع محاكم شبرا الخيمة",
      "محكمة الخانكة الجزئية والابتدائية",
      "محكمة طوخ الجزئية",
      "محكمة قليوب الجزئية"
    ),
    "الدقهلية" to listOf(
      "مجمع محاكم المنصورة الابتدائي والاستئناف",
      "محكمة دكرنس الابتدائية",
      "محكمة ميت غمر الابتدائية",
      "محكمة السنبلاوين الجزئية",
      "محكمة بلقاس الجزئية"
    ),
    "الشرقية" to listOf(
      "مجمع محاكم الزقازيق الابتدائي والاستئناف",
      "محكمة فاقوس الابتدائية",
      "مجمع محاكم العاشر من رمضان",
      "محكمة بلبيس الجزئية",
      "محكمة منيا القمح الجزئية"
    ),
    "الغربية" to listOf(
      "مجمع محاكم طنطا الابتدائي والاستئناف",
      "مجمع محاكم المحلة الكبرى",
      "محكمة زفتى الجزئية",
      "محكمة كفر الزيات الجزئية",
      "محكمة سمنود الجزئية"
    ),
    "المنوفية" to listOf(
      "مجمع محاكم شبين الكوم الابتدائي والاستئناف",
      "مجمع محاكم منوف",
      "محكمة أشمون الجزئية",
      "محكمة قويسنا الجزئية",
      "محكمة مدينة السادات"
    ),
    "البحيرة" to listOf(
      "مجمع محاكم دمنهور الابتدائي والاستئناف",
      "مجمع محاكم إيتاي البارود",
      "محكمة كفر الدوار الابتدائية",
      "محكمة رشيد الجزئية",
      "محكمة كوم حمادة"
    ),
    "كفر الشيخ" to listOf(
      "مجمع محاكم كفر الشيخ الابتدائي والاستئناف",
      "مجمع محاكم دسوق",
      "محكمة فوه الابتدائية",
      "محكمة بلطيم الجزئية"
    ),
    "دمياط" to listOf(
      "مجمع محاكم دمياط الابتدائي",
      "مجمع محاكم دمياط الجديدة",
      "محكمة فارسكور الجزئية",
      "محكمة كفر سعد"
    ),
    "بورسعيد" to listOf(
      "مجمع محاكم بورسعيد الابتدائي (حي العرب)",
      "محكمة بورفؤاد الجزئية",
      "محكمة بورسعيد الاقتصادية"
    ),
    "الإسماعيلية" to listOf(
      "مجمع محاكم الإسماعيلية الابتدائي والاستئناف",
      "محكمة القنطرة غرب الجزئية",
      "محكمة التل الكبير الجزئية",
      "محكمة فايد الجزئية"
    ),
    "السويس" to listOf(
      "مجمع محاكم السويس الابتدائي",
      "محكمة السويس الجزئية",
      "محكمة الأربعين الجزئية"
    ),
    "الفيوم" to listOf(
      "مجمع محاكم الفيوم الابتدائي",
      "محكمة سنورس الجزئية",
      "محكمة إطسا الجزئية",
      "محكمة أبشواي الجزئية"
    ),
    "بني سويف" to listOf(
      "مجمع محاكم بني سويف الابتدائي والاستئناف",
      "محكمة ببا الجزئية",
      "محكمة ناصر الجزئية",
      "محكمة الواسطى الجزئية"
    ),
    "المنيا" to listOf(
      "مجمع محاكم المنيا الابتدائي والاستئناف",
      "مجمع محاكم مغاغة",
      "محكمة بني مزار الجزئية",
      "محكمة ملوي الابتدائية",
      "محكمة سمالوط الجزئية"
    ),
    "أسيوط" to listOf(
      "مجمع محاكم أسيوط الابتدائي والاستئناف",
      "مجمع محاكم منفلوط",
      "محكمة ديروط الجزئية",
      "محكمة أبنوب الجزئية",
      "محكمة القوصية الجزئية"
    ),
    "سوهاج" to listOf(
      "مجمع محاكم سوهاج الابتدائي والاستئناف",
      "مجمع محاكم جرجا",
      "محكمة طما الجزئية",
      "محكمة طهطا الابتدائية",
      "محكمة البلينا الجزئية"
    ),
    "قنا" to listOf(
      "مجمع محاكم قنا الابتدائي والاستئناف",
      "مجمع محاكم نجع حمادي",
      "محكمة قوص الجزئية",
      "محكمة دشنا الجزئية"
    ),
    "الأقصر" to listOf(
      "مجمع محاكم الأقصر الابتدائي",
      "محكمة إسنا الجزئية",
      "محكمة أرمنت الجزئية"
    ),
    "أسوان" to listOf(
      "مجمع محاكم أسوان الابتدائي",
      "محكمة كوم أمبو الجزئية",
      "محكمة إدفو الجزئية",
      "محكمة نصر النوبة"
    ),
    "البحر الأحمر" to listOf(
      "مجمع محاكم الغردقة الابتدائي",
      "محكمة سفاجا الجزئية",
      "محكمة القصير الجزئية",
      "محكمة رأس غارب"
    ),
    "مطروح" to listOf(
      "مجمع محاكم مرسى مطروح الابتدائي",
      "محكمة الحمام الجزئية",
      "محكمة العلمين والضبعة الجزئية"
    ),
    "الوادي الجديد" to listOf(
      "مجمع محاكم الخارجة الابتدائي",
      "محكمة الداخلة الجزئية",
      "محكمة الفرافرة"
    ),
    "شمال سيناء" to listOf(
      "مجمع محاكم العريش الابتدائي",
      "محكمة بئر العبد الجزئية",
      "محكمة الشيخ زويد"
    ),
    "جنوب سيناء" to listOf(
      "مجمع محاكم طور سيناء الابتدائي",
      "مجمع محاكم شرم الشيخ الجزئي",
      "محكمة رأس سدر الجزئية"
    )
  )

  val generalCourts = listOf(
    "دار القضاء العالي (النقض والاستئناف)",
    "مجمع محاكم القاهرة الجديدة (التجمع الخامس)",
    "مجمع محاكم العباسية (شمال القاهرة)",
    "مجمع محاكم زينهم (جنوب القاهرة)",
    "مجمع محاكم مصر الجديدة ومدينة نصر",
    "محكمة الجيزة الابتدائية (السودان)",
    "مجمع محاكم 6 أكتوبر والشيخ زايد",
    "مجمع محاكم تاج الدول (إمبابة)",
    "مجلس الدولة والمحكمة الإدارية العليا",
    "محكمة القاهرة الاقتصادية",
    "مجمع محاكم المنشية بالإسكندرية",
    "مجمع محاكم بنها بالقليوبية",
    "مجمع محاكم المنصورة بالدقهلية",
    "مجمع محاكم الزقازيق بالشرقية",
    "مجمع محاكم طنطا بالغربية"
  )

  fun getCourtsForGovernorates(govs: Collection<String>): List<String> {
    val result = mutableListOf<String>()
    govs.forEach { gov ->
      majorCourtsByGov[gov]?.let { result.addAll(it) }
    }
    return result.distinct()
  }

  fun getDistrictsForGovernorates(govs: Collection<String>): List<String> {
    val result = mutableListOf<String>()
    govs.forEach { gov ->
      EgyptLocationHelper.governoratesWithDistricts[gov]?.let { result.addAll(it) }
    }
    return result.distinct()
  }
}

data class GeoLocation(
  val city: String = "الجيزة",
  val district: String = "الدقي",
  val courtJurisdiction: String = "نيابة الدقي الجزئية • محكمة شمال الجيزة",
  val latitude: Double = 30.0384,
  val longitude: Double = 31.2112,
  val locationPurpose: String = "مكان الواقعة / الاختصاص القضائي",
  val specificLandmark: String = "",
  val fullAddress: String = "$city - $district${if (specificLandmark.isNotBlank()) " ($specificLandmark)" else ""} - نطاق $courtJurisdiction"
)

object EgyptLocationHelper {
  val governoratesWithDistricts = mapOf(
    "الجيزة" to listOf(
      "الدقي",
      "الحوامدية",
      "المهندسين",
      "العجوزة",
      "الهرم",
      "فيصل",
      "مدينة 6 أكتوبر",
      "الشيخ زايد",
      "البدرشين",
      "إمبابة",
      "العياط",
      "أطفيح",
      "الصف",
      "أوسيم",
      "الوراق",
      "كرداسة",
      "العمرانية",
      "الطالبية",
      "بولاق الدكرور",
      "منشأة القناطر"
    ),
    "القاهرة" to listOf(
      "مدينة نصر",
      "مصر الجديدة",
      "التجمع الخامس (القاهرة الجديدة)",
      "المعادي",
      "وسط البلد",
      "الزمالك",
      "شبرا",
      "حلوان",
      "المقطم",
      "عين شمس",
      "الزيتون",
      "المرج",
      "النزهة",
      "الشروق",
      "بدر",
      "15 مايو",
      "عابدين",
      "باب الشعرية"
    ),
    "الإسكندرية" to listOf(
      "سموحة",
      "محطة الرمل",
      "سيدي جابر",
      "ميامي",
      "لوران",
      "المنتزه",
      "العجمي",
      "محرم بك",
      "المنشية",
      "سيدي بشر",
      "جليم",
      "الإبراهيمية",
      "العامرية",
      "برج العرب"
    ),
    "الدقهلية" to listOf(
      "المنصورة (حي الجامعة / المشاية)",
      "طلخا",
      "ميت غمر",
      "دكرنس",
      "السنبلاوين",
      "أجا",
      "المنزلة",
      "شربين",
      "بلقاس",
      "بني عبيد",
      "المنصورة الجديدة"
    ),
    "القليوبية" to listOf(
      "بنها",
      "شبرا الخيمة",
      "قليوب",
      "القناطر الخيرية",
      "طوخ",
      "كفر شكر",
      "شبين القناطر",
      "الخانكة",
      "العبور"
    ),
    "الشرقية" to listOf(
      "الزقازيق",
      "العاشر من رمضان",
      "بلبيس",
      "فاقوس",
      "منيا القمح",
      "أبو حماد",
      "ههيا",
      "ديرب نجم",
      "أبو كبير",
      "الصالحية الجديدة"
    ),
    "الغربية" to listOf(
      "طنطا",
      "المحلة الكبرى",
      "زفتى",
      "كفر الزيات",
      "سمنود",
      "بسيون",
      "قطور",
      "السنطة"
    ),
    "المنوفية" to listOf(
      "شبين الكوم",
      "قويسنا",
      "منوف",
      "أشمون",
      "تلا",
      "بركة السبع",
      "الباجور",
      "مدينة السادات"
    ),
    "البحيرة" to listOf(
      "دمنهور",
      "كفر الدوار",
      "إيتاي البارود",
      "رشيد",
      "كوم حمادة",
      "أبو المطامير",
      "حوش عيسى",
      "وادي النطرون"
    ),
    "بورسعيد" to listOf(
      "حي الشرق",
      "حي العرب",
      "حي المناخ",
      "حي الضواحي",
      "حي الزهور",
      "بورفؤاد"
    ),
    "السويس" to listOf(
      "حي السويس",
      "حي الأربعين",
      "حي فيصل",
      "حي الجناين",
      "عتاقة"
    ),
    "الإسماعيلية" to listOf(
      "حي أول",
      "حي ثان",
      "حي ثالث",
      "التل الكبير",
      "فايد",
      "القنطرة غرب",
      "القنطرة شرق"
    ),
    "كفر الشيخ" to listOf(
      "كفر الشيخ",
      "دسوق",
      "فوه",
      "مطوبس",
      "بلطيم",
      "سيدي سالم",
      "بيلا",
      "قلين"
    ),
    "دمياط" to listOf(
      "دمياط",
      "دمياط الجديدة",
      "رأس البر",
      "فارسكور",
      "كفر سعد",
      "الزرقا"
    ),
    "الفيوم" to listOf(
      "مدينة الفيوم",
      "سنورس",
      "إطسا",
      "طامية",
      "أبشواي",
      "يوسف الصديق"
    ),
    "بني سويف" to listOf(
      "مدينة بني سويف",
      "الواسطى",
      "ناصر",
      "إهناسيا",
      "ببا",
      "سمسطا",
      "الفشن",
      "بني سويف الجديدة"
    ),
    "المنيا" to listOf(
      "مدينة المنيا",
      "مغاغة",
      "بني مزار",
      "مطاي",
      "سمالوط",
      "أبو قرقاص",
      "ملوي",
      "دير مواس",
      "المنيا الجديدة"
    ),
    "أسيوط" to listOf(
      "حي شرق أسيوط",
      "حي غرب أسيوط",
      "ديروط",
      "القوصية",
      "منفلوط",
      "أبنوب",
      "الفتح",
      "أبو تيج",
      "الغنايم",
      "ساحل سليم",
      "البداري",
      "أسيوط الجديدة"
    ),
    "سوهاج" to listOf(
      "مدينة سوهاج",
      "حي شرق",
      "حي غرب",
      "أخميم",
      "البلينا",
      "جرجا",
      "طما",
      "طهطا",
      "المراغة",
      "المنشأة",
      "ساقلتة",
      "جهينة",
      "دار السلام",
      "سوهاج الجديدة"
    ),
    "قنا" to listOf(
      "مدينة قنا",
      "نجع حمادي",
      "قوص",
      "دشنا",
      "فرشوط",
      "أبو تشت",
      "فقط",
      "نقادة",
      "الوقف"
    ),
    "الأقصر" to listOf(
      "مدينة الأقصر",
      "البياضية",
      "القرنة",
      "أرمنت",
      "إسنا",
      "الطود",
      "الزينية"
    ),
    "أسوان" to listOf(
      "مدينة أسوان",
      "كوم أمبو",
      "إدفو",
      "نصر النوبة",
      "دراو",
      "أبو سمبل"
    ),
    "مطروح" to listOf(
      "مرسى مطروح",
      "الحمام",
      "العلمين",
      "الضبعة",
      "سيوة",
      "السلوم"
    ),
    "البحر الأحمر" to listOf(
      "الغردقة",
      "رأس غارب",
      "سفاجا",
      "القصير",
      "مرسى علم"
    ),
    "شمال سيناء" to listOf(
      "العريش",
      "الشيخ زويد",
      "بئر العبد",
      "الحسنة",
      "نخل"
    ),
    "جنوب سيناء" to listOf(
      "شرم الشيخ",
      "طور سيناء",
      "دهب",
      "نويبع",
      "طابا",
      "رأس سدر"
    )
  )

  fun getDistrictsForGovernorate(governorate: String): List<String> {
    return governoratesWithDistricts[governorate] ?: listOf("المركز الرئيسي", "حي أول", "حي ثان")
  }

  fun getDefaultJurisdiction(governorate: String, district: String): String {
    return when {
      governorate == "الجيزة" && district == "الدقي" -> "نيابة الدقي الجزئية • محكمة شمال الجيزة الابتدائية"
      governorate == "الجيزة" && district == "الحوامدية" -> "نيابة الحوامدية الجزئية • محكمة جنوب الجيزة الابتدائية"
      governorate == "الجيزة" && (district == "مدينة 6 أكتوبر" || district == "الشيخ زايد") -> "نيابة أول وثان أكتوبر • مجمع محاكم 6 أكتوبر"
      governorate == "الجيزة" && district == "المهندسين" -> "نيابة العجوزة الجزئية • محكمة شمال الجيزة"
      governorate == "الجيزة" && district == "الهرم" -> "نيابة الهرم الجزئية • محكمة جنوب الجيزة"
      governorate == "الجيزة" && district == "البدرشين" -> "نيابة البدرشين الجزئية • محكمة جنوب الجيزة"
      governorate == "القاهرة" && district == "مدينة نصر" -> "نيابة مدينة نصر أول وثان • محكمة مدينة نصر"
      governorate == "القاهرة" && district == "مصر الجديدة" -> "نيابة مصر الجديدة الجزئية • محكمة مصر الجديدة"
      governorate == "القاهرة" && district == "التجمع الخامس (القاهرة الجديدة)" -> "نيابة القاهرة الجديدة • مجمع محاكم التجمع الخامس"
      governorate == "القاهرة" && district == "المعادي" -> "نيابة المعادي الجزئية • محكمة المعادي"
      governorate == "القاهرة" && district == "وسط البلد" -> "نيابة قصر النيل وعابدين • محكمة جنوب القاهرة (زينهم)"
      governorate == "الإسكندرية" && district == "سموحة" -> "نيابة سيدي جابر • مجمع محاكم الإسكندرية"
      governorate == "الإسكندرية" && district == "المنشية" -> "نيابة المنشية واللبان • محكمة غرب الإسكندرية"
      governorate == "الدقهلية" && district.contains("المنصورة") -> "نيابة قسم ثان المنصورة • مجمع محاكم المنصورة"
      governorate == "الدقهلية" && district == "طلخا" -> "نيابة طلخا الجزئية • مجمع محاكم المنصورة"
      governorate == "الدقهلية" && district == "ميت غمر" -> "نيابة ميت غمر الكلية والجزئية"
      else -> "نيابة $district الجزئية • محكمة $governorate الابتدائية"
    }
  }
}

enum class RequestCategory(val titleAr: String, val descAr: String) {
  COMMERCIAL("قضايا تجارية وشركات", "نزاعات تجارية، تأسيس، صياغة عقود تجارية وتوزيع حصص"),
  LABOR("نزاعات عمالية وتأمينات", "مستحقات نهاية الخدمة، فصل تعسفي، عقود العمل واللوائح"),
  CONTRACTS("صياغة ومراجعة العقود", "عقود مقاولات، اتفاقيات توريد، وكالات، صياغة الشروط والأحكام"),
  PERSONAL_STATUS("أحوال شخصية وتركات", "قسمة التركات وحصر الإرث، قضايا الأسرة والوصايا والأوقاف"),
  REAL_ESTATE("قضايا عقارية ومقاولات", "نزاعات الإيجار، تملك العقار، عقود التطوير والمقاولات"),
  CRIMINAL_FINANCIAL("قضايا مالية وجنائية", "مطالبات مالية، شيكات، احتيال مالي، قضايا عامة"),
  CONSULTATION("استشارة قانونية فورية", "رأي قانوني كتابي أو هاتفي موثق خلال 24 ساعة")
}

/**
 * قوالب القضايا الجاهزة والموثقة
 */
data class CaseTemplate(
  val id: String,
  val titleAr: String,
  val category: RequestCategory,
  val defaultTitle: String,
  val shortDescription: String,
  val detailedDescriptionTemplate: String,
  val suggestedBudget: Double,
  val estimatedBudgetRange: String,
  val urgency: RequestUrgency,
  val requiredDocuments: List<String>,
  val recommendedBarDegree: LawyerBarDegree,
  val defaultCourtJurisdiction: String,
  val locationLabelTitle: String = "مكان الواقعة / الاختصاص القضائي",
  val locationHintAr: String = "حدد المحافظة (مثال: الجيزة) ثم الحي أو المركز (مثال: الدقي أو الحوامدية) لتمكين المحامي من تقدير أتعابه بدقة",
  val defaultGovernorate: String = "الجيزة",
  val defaultDistrict: String = "الدقي"
)

object CaseTemplates {
  val ALL = listOf(
    CaseTemplate(
      id = "template_seha_nafath",
      titleAr = "عقد صحة ونفاذ",
      category = RequestCategory.REAL_ESTATE,
      defaultTitle = "دعوى صحة ونفاذ عقد بيع ابتدائي ونقل الملكية",
      shortDescription = "إقامة دعوى صحة ونفاذ لنقل الملكية العقارية بالشهر العقاري وإشهار المحكمة",
      detailedDescriptionTemplate = "المطلوب: إقامة دعوى صحة ونفاذ لعقد البيع الابتدائي المبرم لنقل ملكية العقار/الشقة السكنية، ومباشرة الإجراءات المساحية، سداد الرسوم القضائية، تقديم الطلب للشهر العقاري، ومتابعة الجلسات حتى صدور حكم نهائي واجب النفاذ.",
      suggestedBudget = 8500.0,
      estimatedBudgetRange = "7,000 - 12,000 ج.م",
      urgency = RequestUrgency.NORMAL,
      requiredDocuments = listOf("أصل عقد البيع الابتدائي", "كشف تحديد مساحي / شهادة عقارية", "توكيلات وسلسلة الملكية السابقة", "صورة بطاقة الرقم القومي"),
      recommendedBarDegree = LawyerBarDegree.APPEAL,
      defaultCourtJurisdiction = "محكمة شمال الجيزة الابتدائية - مأمورية الشهر العقاري",
      locationLabelTitle = "موقع العقار ونطاق مأمورية الشهر العقاري",
      locationHintAr = "حدد المحافظة (مثل الجيزة) ثم المدينة/الحي (مثل: الشيخ زايد، 6 أكتوبر، الدقي) لتحديد الشهر العقاري والمحكمة المختصة ومصاريف المعاينة المساحية.",
      defaultGovernorate = "الجيزة",
      defaultDistrict = "الشيخ زايد"
    ),
    CaseTemplate(
      id = "template_elam_werasa",
      titleAr = "إعلام وراثة",
      category = RequestCategory.PERSONAL_STATUS,
      defaultTitle = "استخراج إعلام وراثة شرعي لبيان الورثة المستحقين",
      shortDescription = "إجراءات قيد ومباشرة طلب إعلام وراثة أمام محكمة الأسرة واستخراج الصيغة الرسمية",
      detailedDescriptionTemplate = "المطلوب: اتخاذ الإجراءات القانونية لاستخراج إعلام وراثة رسمي لحصر الورثة الشرعيين البالغين والقصر، إعلان الورثة بالموعد المحدد، إحضار شهود إثبات الوفاة والوراثة، واستلام الصورة التنفيذية للحكم.",
      suggestedBudget = 3500.0,
      estimatedBudgetRange = "2,500 - 5,000 ج.م",
      urgency = RequestUrgency.NORMAL,
      requiredDocuments = listOf("شهادة وفاة مميكنة للمورث", "صور بطاقات الرقم القومي للورثة", "شهادات ميلاد القصر (إن وجد)", "قرار وصاية في حال وجود قصر"),
      recommendedBarDegree = LawyerBarDegree.PRIMARY,
      defaultCourtJurisdiction = "محكمة أسرة الحوامدية والبدرشين - دائرة الوراثات",
      locationLabelTitle = "مكان الوفاة ومحل إقامة المتوفى الأخير",
      locationHintAr = "حدد مكان الوفاة / الإقامة الأخيرة للمورث (مثال: الجيزة ➔ الحوامدية أو الدقي) لتحديد محكمة الأسرة المختصة مكانياً وإعلان الورثة.",
      defaultGovernorate = "الجيزة",
      defaultDistrict = "الحوامدية"
    ),
    CaseTemplate(
      id = "template_sehet_tawkee",
      titleAr = "صحة توقيع",
      category = RequestCategory.CONTRACTS,
      defaultTitle = "دعوى إثبات صحة توقيع على عقد بيع / اتفاقية",
      shortDescription = "إثبات صدور التوقيع وصحته رسمياً من الطرف الآخر أمام المحكمة الجزئية",
      detailedDescriptionTemplate = "المطلوب: رفع دعوى إثبات صحة توقيع على عقد صادر من المدعى عليه، وإعلانه قانوناً بصحيفة الدعوى، وحضور الجلسة وإثبات حضور الخصم أو إقراره، واستلام العقد مذيلاً بالصيغة الرسمية.",
      suggestedBudget = 2800.0,
      estimatedBudgetRange = "2,000 - 4,000 ج.م",
      urgency = RequestUrgency.NORMAL,
      requiredDocuments = listOf("أصل العقد العرفي المراد إثبات صحة توقيعه", "صورة بطاقة الرقم القومي للمدعي والمدعى عليه", "توكيل رسمي عام قضايا"),
      recommendedBarDegree = LawyerBarDegree.PRIMARY,
      defaultCourtJurisdiction = "محكمة الدقي الجزئية - الدائرة المدنية",
      locationLabelTitle = "مكان توقيع العقد / موطن المدعى عليه",
      locationHintAr = "حدد موطن الخصم أو مكان العقار/العقد (مثال: الجيزة ➔ الدقي أو المهندسين) لتحديد المحكمة الجزئية المختصة بإعلان صحيفة الدعوى.",
      defaultGovernorate = "الجيزة",
      defaultDistrict = "الدقي"
    ),
    CaseTemplate(
      id = "template_niyaba_attendance",
      titleAr = "طلب حضور أمام النيابة العامة",
      category = RequestCategory.CRIMINAL_FINANCIAL,
      defaultTitle = "حضور فوري وتحقيق عاجل مع الموكل أمام النيابة العامة",
      shortDescription = "تمثيل قانوني عاجل وحضور جلسة التحقيق الرسمية بالنيابة العامة لضمان حقوق الموكل",
      detailedDescriptionTemplate = "المطلوب بشكل عاجل: انتقال وحضور محامٍ معتمد فورياً أمام النيابة العامة بخصوص المحضر وقسم الشرطة المختص، لحضور استجواب الموكل والاطلاع على الأوراق، وتقديم طلب إخلاء السبيل بضمان مالي أو شخصي، وتقديم الدفوع القانونية والمذكرات اللازمة.",
      suggestedBudget = 5000.0,
      estimatedBudgetRange = "3,500 - 7,000 ج.م",
      urgency = RequestUrgency.URGENT,
      requiredDocuments = listOf("رقم المحضر / القضية وتاريخ الواقعة", "اسم النيابة العامة أو قسم الشرطة المختص", "توكيل قضايا خاص أو إثبات وكالة بمحضر الجلسة"),
      recommendedBarDegree = LawyerBarDegree.APPEAL,
      defaultCourtJurisdiction = "نيابة الدقي الجزئية • قسم شرطة الدقي",
      locationLabelTitle = "مقر النيابة العامة / قسم الشرطة ومكان الواقعة",
      locationHintAr = "حدد بدقة مكان الواقعة وقسم الشرطة أو النيابة (مثال: الجيزة ➔ الدقي أو الحوامدية) لتمكين المحامي من سرعة الانتقال وتقدير مصاريف الحضور بدقة.",
      defaultGovernorate = "الجيزة",
      defaultDistrict = "الدقي"
    ),
    CaseTemplate(
      id = "template_court_hearing_urgent",
      titleAr = "طلب حضور جلسة محكمة اليوم",
      category = RequestCategory.CRIMINAL_FINANCIAL,
      defaultTitle = "حضور عاجل ومرافعة بجلسة المحكمة المنعقدة اليوم",
      shortDescription = "تمثيل قانوني فوري وإثبات حضور ودفاع بجلسة اليوم لنظر الدعوى القضائية",
      detailedDescriptionTemplate = "المطلوب فوراً: حضور محامٍ مرخص بجلسة المحكمة المنعقدة اليوم لإثبات الحضور، والاطلاع على أوراق الدعوى، وإبداء الطلبات والدفوع القانونية اللازمة، أو طلب أجل للاطلاع وتقديم المستندات لمنع صدور حكم غيابي أو تفويت المواعيد الإجرائية.",
      suggestedBudget = 4000.0,
      estimatedBudgetRange = "3,000 - 6,000 ج.م",
      urgency = RequestUrgency.URGENT,
      requiredDocuments = listOf("رقم الدائرة ورقم القضية / الرول", "اسم المحكمة وقاعة الانعقاد", "صورة بطاقة الرقم القومي والتوكيل"),
      recommendedBarDegree = LawyerBarDegree.APPEAL,
      defaultCourtJurisdiction = "مجمع محاكم شمال الجيزة (شارع السودان)",
      locationLabelTitle = "مقر المحكمة والدائرة المنعقدة",
      locationHintAr = "حدد المحكمة ومقر انعقاد الجلسة بالضبط لسرعة وصول أقرب محامٍ متاح في النطاق.",
      defaultGovernorate = "الجيزة",
      defaultDistrict = "الدقي"
    ),
    CaseTemplate(
      id = "template_police_station_urgent",
      titleAr = "انتقال فوري لقسم الشرطة",
      category = RequestCategory.CRIMINAL_FINANCIAL,
      defaultTitle = "حضور ومتابعة عاجلة بمحضر قسم الشرطة والإشراف على الإجراءات",
      shortDescription = "انتقال فوري لقسم الشرطة لحضور تحرير المحضر وضمان السلامة القانونية للطرفين",
      detailedDescriptionTemplate = "المطلوب بشكل فوري: انتقال محامٍ لقسم الشرطة لحضور سماع أقوال الموكل أو تحرير محضر إثبات حالة أو جنحة، ومتابعة قيد المحضر وإرساله للعرض الصباحي على النيابة المختصة، وسداد الكفالة إن وجدت.",
      suggestedBudget = 3000.0,
      estimatedBudgetRange = "2,500 - 5,000 ج.م",
      urgency = RequestUrgency.URGENT,
      requiredDocuments = listOf("اسم قسم الشرطة والحي", "موضوع الواقعة أو رقم المحضر إن وجد"),
      recommendedBarDegree = LawyerBarDegree.PRIMARY,
      defaultCourtJurisdiction = "قسم شرطة قصر النيل • نيابة قصر النيل",
      locationLabelTitle = "مقر قسم الشرطة ومكان الواقعة",
      locationHintAr = "حدد القسم التابع لمكان الواقعة لسرعة التوجه والتواجد.",
      defaultGovernorate = "القاهرة",
      defaultDistrict = "وسط البلد"
    ),
    CaseTemplate(
      id = "template_shahr_akary_urgent",
      titleAr = "مأمورية شهر عقاري وتوثيق عاجلة",
      category = RequestCategory.CONTRACTS,
      defaultTitle = "انتقال عاجل للتوثيق بالشهر العقاري ومراجعة العقود والتوكيلات",
      shortDescription = "حضور وتوثيق فوري لعقود البيع أو إشهار المحررات الرسمية بمأمورية الشهر العقاري",
      detailedDescriptionTemplate = "المطلوب: مرافقة الموكل لمأمورية الشهر العقاري والتوثيق لمراجعة صحة الصياغة، والتحقق من سلسلة التوكيلات والأهلية القانونية، وتوثيق المحرر الرسمي فوراً وحمايته من أي عيوب شكلية أو موضوعية.",
      suggestedBudget = 2500.0,
      estimatedBudgetRange = "2,000 - 4,000 ج.م",
      urgency = RequestUrgency.URGENT,
      requiredDocuments = listOf("أصل بطاقات الرقم القومي للأطراف", "مسودة العقد أو المحرر المراد توثيقه", "شهادة سلبية أو كشف عقاري إن وجد"),
      recommendedBarDegree = LawyerBarDegree.PRIMARY,
      defaultCourtJurisdiction = "مأمورية الشهر العقاري بمصر الجديدة",
      locationLabelTitle = "مقر مأمورية الشهر العقاري والتوثيق",
      locationHintAr = "حدد مأمورية الشهر العقاري وموقع التوثيق لسرعة التنسيق.",
      defaultGovernorate = "القاهرة",
      defaultDistrict = "مصر الجديدة"
    )
  )
}

enum class RequestUrgency(val labelAr: String) {
  URGENT("عاجل (خلال 24-48 ساعة)"),
  NORMAL("عادي (خلال أسبوع)"),
  FLEXIBLE("مرن (أكثر من أسبوع)")
}

enum class RequestStatus(val labelAr: String) {
  OPEN("مفتوح لتلقي العروض"),
  NEGOTIATING("جاري التفاوض"),
  IN_PROGRESS("قيد التنفيذ (الضمان مفعل)"),
  COMPLETED("مكتمل وتم تحرير الأتعاب"),
  DISPUTED("نزاع قيد التحكيم")
}

data class ServiceRequest(
  val id: String,
  val title: String,
  val category: RequestCategory,
  val description: String,
  val city: String,
  val budgetRange: String,
  val budgetAmount: Double,
  val urgency: RequestUrgency,
  val status: RequestStatus,
  val clientId: String,
  val clientName: String,
  val acceptedBidId: String? = null,
  val createdAt: String,
  val bidsCount: Int = 0,
  val courtLocation: GeoLocation? = null,
  val appliedTemplateId: String? = null,
  val isMeetingConfirmed: Boolean = false,
  val meetingConfirmedAt: String? = null,
  val meetingQrToken: String? = null
)

/**
 * أنواع طلبات الصرف أثناء تنفيذ القضية (أتعاب مرحلية أو مصاريف قضائية)
 */
enum class DisbursementType(val titleAr: String, val badgeAr: String, val descAr: String) {
  JUDICIAL_EXPENSES(
    titleAr = "مصاريف ورسوم قضائية",
    badgeAr = "مصاريف ورسوم قضائية",
    descAr = "رسوم قيد وإيداع، أمانة الخبير القضائي، المعاينة الهندسية، الانتقال، أو استخراج شهادات رسمية"
  ),
  INTERIM_FEE(
    titleAr = "دفعة مرحلية من الأتعاب",
    badgeAr = "دفعة أتعاب مرحلية",
    descAr = "دفعة مستحقة عن إنجاز مرحلة محددة (كصياغة صحيفة الدعوى، إتمام التوقيع، أو حضور جلسة المرافعة)"
  )
}

/**
 * حالة طلب سلفة المصاريف أو الدفعة
 */
enum class DisbursementStatus(val labelAr: String) {
  PENDING("بانتظار رد الموكل"),
  ACCEPTED("تم القبول والصرف ✓"),
  REJECTED("تم الرفض ✕"),
  MODIFIED_BY_CLIENT("تم اقتراح تعديل من الموكل ⚖️")
}

/**
 * طلب صرف دفعة أو مصاريف قضائية يقدمه المحامي أثناء تنفيذ العرض
 */
data class DisbursementRequest(
  val id: String,
  val requestId: String,
  val lawyerId: String,
  val lawyerName: String,
  val type: DisbursementType,
  val amount: Double,
  val reason: String,
  val receiptOrRef: String? = null,
  val status: DisbursementStatus = DisbursementStatus.PENDING,
  val counterAmount: Double? = null, // المبلغ المقترح بعد التعديل من العميل
  val clientNote: String? = null, // ملاحظات الموكل عند الرفض أو التعديل
  val createdAt: String,
  val respondedAt: String? = null
)

enum class BidStatus(val labelAr: String) {
  PENDING("بانتظار موافقة العميل"),
  ACCEPTED("مقبول ومودع بالضمان"),
  REJECTED("مرفوض")
}

data class Bid(
  val id: String,
  val requestId: String,
  val lawyerId: String,
  val lawyerName: String,
  val lawyerTitle: String,
  val lawyerDegree: LawyerBarDegree = LawyerBarDegree.APPEAL,
  val lawyerLicenseNumber: String,
  val lawyerRating: Double,
  val lawyerCasesCount: Int,
  val lawyerFee: Double, // خانة أتعاب المحامي
  val legalExpenses: Double = 0.0, // خانة المصاريف القانونية المقدرة إن وجدت
  val clientFeePercent: Double = 5.0, // نسبة المنصة المطبقة على العميل (تضاف للمبلغ)
  val lawyerFeePercent: Double = 5.0, // نسبة المنصة المطبقة على المحامي (تستقطع من أتعابه)
  val platformFeePercent: Double = clientFeePercent, // توافقية عكسية
  val proposedDays: Int,
  val proposalNote: String,
  val status: BidStatus = BidStatus.PENDING,
  val createdAt: String
) {
  // إجمالي الأتعاب والمصاريف الأساسية المقدمة من المحامي
  val baseTotalAmount: Double
    get() = lawyerFee + legalExpenses

  // قيمة رسم المنصة من العميل (تحسبه المنصة وتضيفه لإجمالي المبلغ المقدم من المحامي)
  val clientFeeAmount: Double
    get() = if (clientFeePercent > 0.0) baseTotalAmount * (clientFeePercent / 100.0) else 0.0

  // قيمة رسم المنصة المستقطع من المحامي
  val lawyerFeeAmount: Double
    get() = if (lawyerFeePercent > 0.0) lawyerFee * (lawyerFeePercent / 100.0) else 0.0

  // إجمالي رسوم المنصة المحصلة من الطرفين
  val platformFeeAmount: Double
    get() = clientFeeAmount + lawyerFeeAmount

  // الإجمالي الشامل النهائي المعروض للمستخدم النهائي (المبلغ المقدم من المحامي + رسم العميل المضاف تلقائياً)
  val grandTotalAmount: Double
    get() = baseTotalAmount + clientFeeAmount

  // صافي استحقاق المحامي بعد خصم نسبته المقررة
  val lawyerNetAmount: Double
    get() = (lawyerFee - lawyerFeeAmount) + legalExpenses

  // التوافقية العكسية
  val proposedAmount: Double
    get() = grandTotalAmount

  fun getMaskedDisplayName(): String {
    val cleanTitle = lawyerTitle.ifBlank { "الأستاذ" }
    val rawNameWithoutTitle = lawyerName
      .replace("المستشار", "")
      .replace("الأستاذة", "")
      .replace("الأستاذ", "")
      .replace("السيدة", "")
      .replace("السيد", "")
      .replace("د.", "")
      .replace("الدكتور", "")
      .replace("الدكتورة", "")
      .trim()
    val parts = rawNameWithoutTitle.split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (parts.isEmpty()) return "$cleanTitle ****"
    val firstName = parts[0]
    val maskedRest = parts.drop(1).joinToString(" ") { "★".repeat(it.length.coerceIn(3, 6)) }
    return if (maskedRest.isBlank()) "$cleanTitle $firstName ★★★" else "$cleanTitle $firstName $maskedRest"
  }

  fun getFullDisplayName(): String {
    val cleanTitle = lawyerTitle.ifBlank { "الأستاذ" }
    val hasTitleAlready = lawyerName.contains("المستشار") || lawyerName.contains("الأستاذ") || lawyerName.contains("السيد") || lawyerName.contains("الدكتور")
    return if (hasTitleAlready) lawyerName else "$cleanTitle $lawyerName"
  }
}

enum class EscrowStatus(val labelAr: String) {
  HELD("الأتعاب محتجزة بأمان في المحفظة"),
  RELEASED("تم تحرير المبلغ للمحامي بنجاح"),
  REFUNDED("تمت إعادة المبلغ للعميل"),
  FROZEN_FOR_DISPUTE("مجمد لوجود نزاع قيد التحكيم")
}

data class EscrowTransaction(
  val id: String,
  val requestId: String,
  val totalAmount: Double,
  val lawyerAmount: Double,
  val platformFee: Double,
  val status: EscrowStatus,
  val paymentMethod: String,
  val referenceNumber: String,
  val date: String
)

enum class VerificationStatus(val labelAr: String) {
  VERIFIED("معتمد وموثق"),
  PENDING("قيد تدقيق الوثائق"),
  REJECTED("مرفوض")
}

data class Lawyer(
  val id: String,
  val name: String,
  val title: LawyerTitle = LawyerTitle.COUNSELOR,
  val specialization: RequestCategory,
  val degree: LawyerBarDegree = LawyerBarDegree.APPEAL,
  val city: String,
  val subBarAssociation: String = "نقابة المحامين الفرعية",
  val courtJurisdictionScope: String = "محاكم الاستئناف والابتدائية ومجلس الدولة",
  val licenseNumber: String,
  val nationalIdNumber: String = "28504120109482",
  val phone: String = "+20 111 987 6543",
  val email: String = "lawyer@maitre.eg",
  val isVerified: Boolean,
  val verificationStatus: VerificationStatus,
  val rating: Double,
  val reviewsCount: Int,
  val yearsExperience: Int,
  val bio: String,
  val consultationFee: Double,
  val firmName: String? = null,
  val officeAddressManually: String = "القاهرة - مصر الجديدة - شارع الأهرام - برج مِتر القانوني",
  val officeLatitude: Double? = 30.0911,
  val officeLongitude: Double? = 31.3253,
  val nationalIdCardFrontUri: String? = "id_card_front.jpg",
  val nationalIdCardBackUri: String? = "id_card_back.jpg",
  val barCardFrontUri: String? = "bar_card_front.jpg",
  val barCardBackUri: String? = "bar_card_back.jpg",
  val criminalRecordCertUri: String? = null,
  val registrationDate: String = "اليوم",
  val adminReviewNotes: String? = null,
  val rejectionReason: String? = null,
  val approvedByAdmin: String? = null
) {
  /**
   * يظهر فقط اللقب والاسم الأول وباقي الاسم نجوم للعميل قبل الاتفاق
   * مثال: "المستشار سامح **** *****"
   */
  fun getMaskedDisplayName(): String {
    val cleanTitle = title.labelAr
    // إزالة اللقب إذا كان مدمجاً في الاسم
    val rawNameWithoutTitle = name
      .replace("المستشار", "")
      .replace("الأستاذة", "")
      .replace("الأستاذ", "")
      .replace("السيدة", "")
      .replace("السيد", "")
      .replace("د.", "")
      .replace("الدكتور", "")
      .replace("الدكتورة", "")
      .trim()
    val parts = rawNameWithoutTitle.split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (parts.isEmpty()) return "$cleanTitle ****"
    val firstName = parts[0]
    val maskedRest = parts.drop(1).joinToString(" ") { "★".repeat(it.length.coerceIn(3, 6)) }
    return if (maskedRest.isBlank()) "$cleanTitle $firstName ★★★" else "$cleanTitle $firstName $maskedRest"
  }

  fun getFullDisplayName(): String {
    val cleanTitle = title.labelAr
    val hasTitleAlready = name.contains("المستشار") || name.contains("الأستاذ") || name.contains("السيد") || name.contains("الدكتور")
    return if (hasTitleAlready) name else "$cleanTitle $name"
  }
}

data class ClientRegistration(
  val id: String,
  val name: String,
  val phone: String,
  val email: String,
  val nationalId: String,
  val clientType: ClientType,
  val companyName: String? = null,
  val governorate: String,
  val idCardFrontUri: String? = "id_card_front.jpg",
  val idCardBackUri: String? = "id_card_back.jpg",
  val status: VerificationStatus = VerificationStatus.PENDING,
  val registrationDate: String = "اليوم",
  val rejectionReason: String? = null,
  val adminReviewNotes: String? = null
)

data class ChatMessage(
  val id: String,
  val requestId: String,
  val senderId: String,
  val senderName: String,
  val senderRole: UserRole,
  val text: String,
  val timestamp: String,
  val attachmentName: String? = null,
  val isSystemMessage: Boolean = false
)

enum class DisputeStatus(val labelAr: String) {
  UNDER_REVIEW("قيد المراجعة القانونية"),
  RESOLVED_REFUND("تم الفصل: إعادة الأتعاب للعميل"),
  RESOLVED_RELEASE("تم الفصل: تحرير الأتعاب للمحامي"),
  MUTUAL_SETTLEMENT("تسوية ودية مناصفة")
}

data class Dispute(
  val id: String,
  val requestId: String,
  val requestTitle: String,
  val openedByRole: UserRole,
  val openedByName: String,
  val reason: String,
  val details: String,
  val status: DisputeStatus,
  val adminNote: String? = null,
  val createdAt: String
)

data class AppNotification(
  val id: String,
  val title: String,
  val body: String,
  val timestamp: String,
  val isRead: Boolean = false,
  val relatedRequestId: String? = null
)

data class LawyerReview(
  val id: String,
  val lawyerId: String,
  val clientName: String,
  val stars: Int,
  val comment: String,
  val tags: List<String>,
  val date: String
)

data class DepositRecord(
  val id: String,
  val amount: Double,
  val paymentMethod: String,
  val referenceNumber: String,
  val date: String,
  val status: String = "مكتمل ومودع بالضمان",
  val iban: String? = null
)

enum class WithdrawalStatus(val labelAr: String) {
  PENDING("بانتظار اعتماد مدير النظام"),
  APPROVED("معتمد وتم التحويل البنكي"),
  REJECTED("مرفوض من الإدارة")
}

data class LawyerWithdrawalRequest(
  val id: String,
  val lawyerId: String,
  val lawyerName: String,
  val lawyerPhone: String,
  val amount: Double,
  val completedRequestId: String,
  val completedRequestTitle: String,
  val bankOrInstapayAccount: String,
  val accountType: String, // "إنستاباي InstaPay" أو "حساب بنكي IBAN" أو "محفظة كاش"
  val status: WithdrawalStatus = WithdrawalStatus.PENDING,
  val requestDate: String,
  val adminApprovalDate: String? = null,
  val adminNote: String? = null
)

enum class CouponAudience(val labelAr: String) {
  ALL("كامل المنصة (الجميع)"),
  CLIENTS("المستخدم النهائي (العميل) فقط"),
  LAWYERS("المحامين فقط")
}

enum class DiscountType(val labelAr: String) {
  PERCENTAGE("نسبة مئوية (%)"),
  FIXED_AMOUNT("مبلغ ثابت (ج.م)")
}

data class DiscountCoupon(
  val id: String,
  val code: String, // e.g. "MAITRE2026", "EGYLAW15", "LAWYERBOOST"
  val title: String,
  val description: String,
  val discountType: DiscountType,
  val discountValue: Double, // percentage e.g. 15.0 or amount e.g. 200.0
  val audience: CouponAudience,
  val minAmount: Double = 0.0,
  val maxDiscountAmount: Double = 1000.0,
  val isActive: Boolean = true,
  val usageCount: Int = 0,
  val maxUsageLimit: Int = 100,
  val expiryDate: String = "31 ديسمبر 2026",
  val createdBy: String = "مدير النظام"
)

enum class RiskSeverity(val labelAr: String) {
  LOW("منخفض"),
  MEDIUM("متوسط"),
  HIGH("مرتفع - محاولة تحايل صريحة")
}

data class ContactLeakageViolation(
  val id: String,
  val timestamp: String,
  val userRole: UserRole,
  val userName: String,
  val contextField: String, // "وصف طلب القضية", "عرض المحامي", "رسائل ما قبل فتح الاتصال"
  val violationType: String,
  val redactedSnippet: String,
  val severity: RiskSeverity = RiskSeverity.HIGH,
  val isBlocked: Boolean = true
)

data class SecurityRiskEvent(
  val id: String,
  val eventType: String,
  val description: String,
  val userIdentifier: String,
  val severity: RiskSeverity,
  val timestamp: String
)

data class PlatformWithdrawalRecord(
  val id: String,
  val amount: Double,
  val destinationAccount: String,
  val destinationType: String, // "حساب بنكي مؤسسي IBAN", "إنستاباي أعمال InstaPay", "محفظة كاش إلكترونية"
  val date: String,
  val referenceNumber: String,
  val note: String,
  val status: String = "تم التحويل بنجاح من حساب المنصة"
)

enum class SupervisoryDecisionType(val titleAr: String, val descAr: String) {
  SUPERVISORY_WARNING("إنذار وتوجيه رقابي", "إصدار تنبيه رسمي لطرفي النزاع/الطلب بالالتزام بالمهل وأصول المهنة"),
  PRECAUTIONARY_FREEZE("تجميد احترازي للطلب", "تجميد الصرف والاتصال مؤقتاً للاشتباه أو فحص المستندات"),
  DEADLINE_EXTENSION("تمديد مهلة التنفيذ/العروض", "منح مهلة إضافية للأطراف بناء على طلب مسبب"),
  ADMINISTRATIVE_CANCEL_REFUND("إلغاء إداري مع رد التأمين", "إلغاء الطلب ورد كامل المبلغ المحتجز لمحفظة الموكل"),
  ENFORCE_EXECUTION("اعتماد التوافق وإلزام التنفيذ", "إلزام المحامي بمواصلة المباشرة وفق جدول زمني محدد"),
  PRIORITY_ESCALATION("رفع لدرجة قيد أعلى", "إحالة القضية للترافع أمام محامٍ بدرجة استئناف أو نقض")
}

data class SupervisoryDecision(
  val id: String,
  val requestId: String,
  val requestTitle: String,
  val clientName: String,
  val lawyerName: String?,
  val decisionType: SupervisoryDecisionType,
  val adminNote: String,
  val clientRefundPercentage: Double = 0.0,
  val lawyerFeePercentage: Double = 0.0,
  val issuedAt: String,
  val issuedBy: String = "هيئة الرقابة والتحكيم بجمهورية مصر العربية"
)

enum class AuditLogCategory(val labelAr: String) {
  REQUEST_LIFECYCLE("دورة الطلبات"),
  ESCROW_TRANSACTION("حركات الضمان المالي"),
  LEAKAGE_PREVENTION("حظر التسريب والتحايل"),
  ARBITRATION_DECISION("قرارات التحكيم والرقابة"),
  WITHDRAWAL_OPERATION("سحوبات الأرصدة"),
  LAWYER_COMPLIANCE("امتثال المحامين")
}

data class RealtimeAuditLog(
  val id: String,
  val category: AuditLogCategory,
  val title: String,
  val description: String,
  val governorate: String,
  val actorRole: UserRole,
  val actorName: String,
  val severity: RiskSeverity = RiskSeverity.LOW,
  val timestamp: String
)

data class SupervisoryReportData(
  val reportId: String,
  val generatedAt: String,
  val activeGovernoratesCount: Int,
  val totalOpenRequests: Int,
  val totalActiveEscrowEgp: Double,
  val totalReleasedFeesEgp: Double,
  val platformTotalRevenueEgp: Double,
  val totalPlatformWithdrawalsEgp: Double,
  val availablePlatformBalanceEgp: Double,
  val leakageViolationsBlocked: Int,
  val disputesCount: Int,
  val disputesResolvedCount: Int,
  val integrityComplianceRate: Double,
  val recentLogs: List<RealtimeAuditLog>
)

