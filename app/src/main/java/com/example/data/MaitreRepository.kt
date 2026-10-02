package com.example.data

import android.content.Context
import com.example.data.local.*
import com.example.model.*
import com.example.service.ContactLeakageDetectionService
import com.example.service.LeakageCheckResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

object MaitreRepository {

  private var db: MaitreDatabase? = null
  private var userPrefs: UserPreferencesRepository? = null
  private val scope = CoroutineScope(Dispatchers.IO)

  private val _appPreferences = MutableStateFlow(AppPreferences())
  val appPreferences: StateFlow<AppPreferences> = _appPreferences.asStateFlow()

  // نسبتا المنصة: الأولى من العميل، والثانية من المحامي (قابلتان للتعديل أو التخفيض أو الإيقاف من المشرف)
  private val _clientPlatformFeePercentage = MutableStateFlow(5.0)
  val clientPlatformFeePercentage: StateFlow<Double> = _clientPlatformFeePercentage.asStateFlow()

  private val _lawyerPlatformFeePercentage = MutableStateFlow(5.0)
  val lawyerPlatformFeePercentage: StateFlow<Double> = _lawyerPlatformFeePercentage.asStateFlow()

  // توافقية عكسية
  private val _platformFeePercentage = MutableStateFlow(10.0)
  val platformFeePercentage: StateFlow<Double> = _platformFeePercentage.asStateFlow()

  fun updateClientPlatformFeePercentage(percentage: Double) {
    _clientPlatformFeePercentage.value = percentage.coerceAtLeast(0.0)
    _platformFeePercentage.value = _clientPlatformFeePercentage.value + _lawyerPlatformFeePercentage.value
  }

  fun updateLawyerPlatformFeePercentage(percentage: Double) {
    _lawyerPlatformFeePercentage.value = percentage.coerceAtLeast(0.0)
    _platformFeePercentage.value = _clientPlatformFeePercentage.value + _lawyerPlatformFeePercentage.value
  }

  fun updateDualPlatformFees(clientFee: Double, lawyerFee: Double) {
    _clientPlatformFeePercentage.value = clientFee.coerceAtLeast(0.0)
    _lawyerPlatformFeePercentage.value = lawyerFee.coerceAtLeast(0.0)
    _platformFeePercentage.value = _clientPlatformFeePercentage.value + _lawyerPlatformFeePercentage.value
  }

  fun setPlatformFeePercentage(percentage: Double) {
    _platformFeePercentage.value = percentage
    _clientPlatformFeePercentage.value = (percentage / 2.0).coerceAtLeast(0.0)
    _lawyerPlatformFeePercentage.value = (percentage / 2.0).coerceAtLeast(0.0)
  }

  fun updatePlatformFeePercentage(percentage: Double) {
    setPlatformFeePercentage(percentage)
  }

  private val _currentUser = MutableStateFlow(
    UserProfile(
      id = "user_client_1",
      name = "م. شريف عبد الفتاح التميمي",
      email = "sherif.tamimi@example.com",
      phone = "+20 100 123 4567",
      role = UserRole.CLIENT,
      balance = 4500.0,
      isVerified = true,
      nationalIdOrCr = "29408151203948",
      officeAddress = "القاهرة - التجمع الخامس - شارع التسعين الشمالي"
    )
  )
  val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

  private val _requests = MutableStateFlow<List<ServiceRequest>>(emptyList())
  val requests: StateFlow<List<ServiceRequest>> = _requests.asStateFlow()

  private val _bids = MutableStateFlow<List<Bid>>(emptyList())
  val bids: StateFlow<List<Bid>> = _bids.asStateFlow()

  private val _escrowTransactions = MutableStateFlow<List<EscrowTransaction>>(emptyList())
  val escrowTransactions: StateFlow<List<EscrowTransaction>> = _escrowTransactions.asStateFlow()

  private val _lawyers = MutableStateFlow<List<Lawyer>>(emptyList())
  val lawyers: StateFlow<List<Lawyer>> = _lawyers.asStateFlow()

  private val _clientRegistrations = MutableStateFlow<List<ClientRegistration>>(emptyList())
  val clientRegistrations: StateFlow<List<ClientRegistration>> = _clientRegistrations.asStateFlow()

  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

  private val _disputes = MutableStateFlow<List<Dispute>>(emptyList())
  val disputes: StateFlow<List<Dispute>> = _disputes.asStateFlow()

  private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
  val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

  private val _reviews = MutableStateFlow<List<LawyerReview>>(emptyList())
  val reviews: StateFlow<List<LawyerReview>> = _reviews.asStateFlow()

  private val _violations = MutableStateFlow<List<ContactLeakageViolation>>(emptyList())
  val violations: StateFlow<List<ContactLeakageViolation>> = _violations.asStateFlow()

  private val _securityRiskEvents = MutableStateFlow<List<SecurityRiskEvent>>(emptyList())
  val securityRiskEvents: StateFlow<List<SecurityRiskEvent>> = _securityRiskEvents.asStateFlow()

  // طلبات الدفعات والمصاريف القضائية أثناء تنفيذ القضايا
  private val _disbursementRequests = MutableStateFlow<List<DisbursementRequest>>(emptyList())
  val disbursementRequests: StateFlow<List<DisbursementRequest>> = _disbursementRequests.asStateFlow()

  private val _deposits = MutableStateFlow<List<DepositRecord>>(
    listOf(
      DepositRecord(
        id = "dep_1",
        amount = 1500.0,
        paymentMethod = "إنستاباي InstaPay",
        referenceNumber = "INSTA-84920EGY",
        date = "15 سبتمبر 2026",
        status = "سداد رسم خدمة المنصة بنجاح"
      ),
      DepositRecord(
        id = "dep_2",
        amount = 500.0,
        paymentMethod = "فودافون كاش",
        referenceNumber = "VFC-73194EGY",
        date = "12 سبتمبر 2026",
        status = "سداد رسم خدمة المنصة بنجاح"
      )
    )
  )
  val deposits: StateFlow<List<DepositRecord>> = _deposits.asStateFlow()

  // Lawyer Withdrawal Requests (سحب رصيد المحامي بعد اعتماد مدير النظام)
  private val _withdrawalRequests = MutableStateFlow<List<LawyerWithdrawalRequest>>(
    listOf(
      LawyerWithdrawalRequest(
        id = "wdr_1",
        lawyerId = "lawyer_4",
        lawyerName = "المستشار محمد عبد الرحمن الديب",
        lawyerPhone = "+20 100 445 6789",
        amount = 3000.0,
        completedRequestId = "req_104",
        completedRequestTitle = "استخراج إعلام وراثة شرعي وتوزيع التركة",
        bankOrInstapayAccount = "mohamed.eldeeb@instapay",
        accountType = "إنستاباي InstaPay",
        status = WithdrawalStatus.APPROVED,
        requestDate = "14 سبتمبر 2026",
        adminApprovalDate = "15 سبتمبر 2026",
        adminNote = "تم تحويل المبلغ عبر إنستاباي بنجاح بعد اكتمال الخدمة واستلام العميل."
      ),
      LawyerWithdrawalRequest(
        id = "wdr_2",
        lawyerId = "lawyer_1",
        lawyerName = "المستشار د. أحمد عبد العال الشناوي",
        lawyerPhone = "+20 111 987 6543",
        amount = 4000.0,
        completedRequestId = "req_104",
        completedRequestTitle = "أتعاب استشارة ونموذج عقود معتمد تم إنجازه",
        bankOrInstapayAccount = "EG380002000100000049281726351",
        accountType = "حساب بنكي IBAN",
        status = WithdrawalStatus.PENDING,
        requestDate = "اليوم",
        adminNote = null
      )
    )
  )
  val withdrawalRequests: StateFlow<List<LawyerWithdrawalRequest>> = _withdrawalRequests.asStateFlow()

  // Platform-wide Discount Coupons (أكواد الخصم لكامل المنصة)
  private val _coupons = MutableStateFlow<List<DiscountCoupon>>(
    listOf(
      DiscountCoupon(
        id = "cpn_1",
        code = "MAITRE2026",
        title = "خصم الترحيب بمنصة مِتر",
        description = "خصم 20% على رسوم المنصة لجميع المستخدمين (العملاء والمحامين)",
        discountType = DiscountType.PERCENTAGE,
        discountValue = 20.0,
        audience = CouponAudience.ALL,
        minAmount = 200.0,
        maxDiscountAmount = 500.0,
        isActive = true,
        usageCount = 38,
        maxUsageLimit = 500,
        expiryDate = "31 ديسمبر 2026"
      ),
      DiscountCoupon(
        id = "cpn_2",
        code = "EGYCLIENT15",
        title = "عرض الموكلين الجدد",
        description = "خصم 15% على رسوم جدية التعاقد والضمان للمستخدم النهائي",
        discountType = DiscountType.PERCENTAGE,
        discountValue = 15.0,
        audience = CouponAudience.CLIENTS,
        minAmount = 300.0,
        maxDiscountAmount = 300.0,
        isActive = true,
        usageCount = 22,
        maxUsageLimit = 200,
        expiryDate = "15 نوفمبر 2026"
      ),
      DiscountCoupon(
        id = "cpn_3",
        code = "LAWYERBOOST",
        title = "دعم المحامين المعتمدين",
        description = "خصم 50% على اشتراكات ورسوم توثيق الحساب للمحامين المعتمدين",
        discountType = DiscountType.PERCENTAGE,
        discountValue = 50.0,
        audience = CouponAudience.LAWYERS,
        minAmount = 100.0,
        maxDiscountAmount = 1000.0,
        isActive = true,
        usageCount = 14,
        maxUsageLimit = 100,
        expiryDate = "30 نوفمبر 2026"
      ),
      DiscountCoupon(
        id = "cpn_4",
        code = "ADL200",
        title = "كوبون المنحة القضائية الثابت",
        description = "خصم فوري 200 جنيه مصري على أي طلب استشارة أو دعوى قضائية",
        discountType = DiscountType.FIXED_AMOUNT,
        discountValue = 200.0,
        audience = CouponAudience.ALL,
        minAmount = 1000.0,
        maxDiscountAmount = 200.0,
        isActive = true,
        usageCount = 45,
        maxUsageLimit = 150,
        expiryDate = "31 أكتوبر 2026"
      )
    )
  )
  val coupons: StateFlow<List<DiscountCoupon>> = _coupons.asStateFlow()

  // Platform Treasury Withdrawals (سحب أرباح ورسوم المنصة)
  private val _platformWithdrawals = MutableStateFlow<List<PlatformWithdrawalRecord>>(
    listOf(
      PlatformWithdrawalRecord(
        id = "pwd_1",
        amount = 15000.0,
        destinationAccount = "EG1200030000000019284726152",
        destinationType = "حساب بنكي مؤسسي (البنك الأهلي المصري)",
        date = "10 سبتمبر 2026",
        referenceNumber = "CORP-EGY-92817",
        note = "تحويل أرباح المنصة المعتمدة عن النصف الأول من شهر سبتمبر لحساب الشركة الرئيسي",
        status = "تم التحويل بنجاح من حساب المنصة"
      )
    )
  )
  val platformWithdrawals: StateFlow<List<PlatformWithdrawalRecord>> = _platformWithdrawals.asStateFlow()

  // Supervisory Decisions for Open Requests & Disputes (قرارات المتابعة والتدخل الرقابي)
  private val _supervisoryDecisions = MutableStateFlow<List<SupervisoryDecision>>(
    listOf(
      SupervisoryDecision(
        id = "sd_1",
        requestId = "req_101",
        requestTitle = "صياغة وتدقيق عقد استثمار وتوزيع أرباح تجاري",
        clientName = "م. شريف عبد الفتاح التميمي",
        lawyerName = "المستشار د. أحمد عبد العال الشناوي",
        decisionType = SupervisoryDecisionType.DEADLINE_EXTENSION,
        adminNote = "تم تمديد مهلة المراجعة 48 ساعة إضافية بناء على طلب الطرفين لتدقيق بنود الضرائب العقارية.",
        issuedAt = "17 سبتمبر 2026"
      ),
      SupervisoryDecision(
        id = "sd_2",
        requestId = "req_103",
        requestTitle = "تمثيل قضائي في نزاع عمالي وتعويضات عمالية",
        clientName = "شركة النيل للمقاولات",
        lawyerName = "المستشار محمد عبد الرحمن الديب",
        decisionType = SupervisoryDecisionType.ENFORCE_EXECUTION,
        adminNote = "إلزام المحامي بتقديم مذكرة الدفاع التكميلية أمام محكمة جنوب الجيزة قبل جلسة المرافعة.",
        issuedAt = "15 سبتمبر 2026"
      )
    )
  )
  val supervisoryDecisions: StateFlow<List<SupervisoryDecision>> = _supervisoryDecisions.asStateFlow()

  // Real-time Platform Audit Log Stream (سجل الرقابة الفورية لمتابعة المنصة أولاً بأول)
  private val _realtimeAuditLogs = MutableStateFlow<List<RealtimeAuditLog>>(
    listOf(
      RealtimeAuditLog(
        id = "log_1",
        category = AuditLogCategory.ESCROW_TRANSACTION,
        title = "إيداع وتأمين أتعاب قضائية جديدة",
        description = "تم حجز وتأمين مبلغ 4,500 ج.م بمحفظة الضمان للقضية (صياغة وتدقيق عقد استثمار) - القاهرة",
        governorate = "القاهرة",
        actorRole = UserRole.CLIENT,
        actorName = "م. شريف التميمي",
        severity = RiskSeverity.LOW,
        timestamp = "منذ 15 دقيقة"
      ),
      RealtimeAuditLog(
        id = "log_2",
        category = AuditLogCategory.LEAKAGE_PREVENTION,
        title = "حظر فوري لمحاولة مشاركة هاتف خارج المنصة",
        description = "نظام الذكاء الاصطناعي الأمني اعترض وحجب رقم هاتف في محادثة ما قبل فتح الاتصال وحفظ سرية المنصة",
        governorate = "الجيزة",
        actorRole = UserRole.CLIENT,
        actorName = "مستخدم متقدم",
        severity = RiskSeverity.HIGH,
        timestamp = "منذ 42 دقيقة"
      ),
      RealtimeAuditLog(
        id = "log_3",
        category = AuditLogCategory.REQUEST_LIFECYCLE,
        title = "طرح طلب استشارة قانونية تجارية جديدة",
        description = "تم تسجيل طلب قيد الاستعجال بمحافظة الإسكندرية (محكمة الاستئناف الاقتصادية)",
        governorate = "الإسكندرية",
        actorRole = UserRole.CLIENT,
        actorName = "الشركة المصرية للأغذية",
        severity = RiskSeverity.LOW,
        timestamp = "منذ ساعة و10 دقائق"
      ),
      RealtimeAuditLog(
        id = "log_4",
        category = AuditLogCategory.WITHDRAWAL_OPERATION,
        title = "طلب سحب رصيد أتعاب محامٍ معتمد",
        description = "تقدم المستشار د. أحمد الشناوي بطلب سحب مبلغ 4,000 ج.م عن دعوى منجزة",
        governorate = "القاهرة",
        actorRole = UserRole.LAWYER,
        actorName = "المستشار د. أحمد الشناوي",
        severity = RiskSeverity.LOW,
        timestamp = "منذ ساعتين"
      ),
      RealtimeAuditLog(
        id = "log_5",
        category = AuditLogCategory.ARBITRATION_DECISION,
        title = "قرار هيئة التحكيم الإداري",
        description = "إصدار توجيه إداري بتمديد مهلة تقديم المذكرات للطرفين في نزاع عقاري",
        governorate = "الدقهلية",
        actorRole = UserRole.ADMIN,
        actorName = "مشرف المنصة والتحكيم",
        severity = RiskSeverity.MEDIUM,
        timestamp = "اليوم 11:30 ص"
      )
    )
  )
  val realtimeAuditLogs: StateFlow<List<RealtimeAuditLog>> = _realtimeAuditLogs.asStateFlow()

  // Lawyer Availability Status (متاح / متوقف - مثل تطبيقات النقل الذكي)
  private val _isLawyerAvailable = MutableStateFlow(true)
  val isLawyerAvailable: StateFlow<Boolean> = _isLawyerAvailable.asStateFlow()

  fun setLawyerAvailable(available: Boolean) {
    _isLawyerAvailable.value = available
    if (!available) {
      _incomingDispatchRequest.value = null
    }
  }

  fun toggleLawyerAvailability() {
    setLawyerAvailable(!_isLawyerAvailable.value)
  }

  // Active incoming smart dispatch request (نافذة التنبيه المنبثقة لمدة 20 ثانية)
  private val _incomingDispatchRequest = MutableStateFlow<ServiceRequest?>(null)
  val incomingDispatchRequest: StateFlow<ServiceRequest?> = _incomingDispatchRequest.asStateFlow()

  fun triggerIncomingDispatchRequest(request: ServiceRequest) {
    if (_isLawyerAvailable.value && _currentUser.value.role == UserRole.LAWYER) {
      if (_currentUser.value.isWithinLawyerJurisdiction(request.city, request.courtLocation)) {
        _incomingDispatchRequest.value = request
      }
    }
  }

  fun dismissIncomingDispatchRequest() {
    _incomingDispatchRequest.value = null
  }

  /**
   * قبول فوري ومباشر للطلب من نافذة التنبيه الذكية
   */
  fun acceptIncomingDispatchRequest(requestId: String, lawyerFee: Double, note: String = "تم قبول الطلب ومباشرة الإجراءات فوراً عبر التنبيه الذكي"): Result<Bid> {
    _incomingDispatchRequest.value = null
    return addBid(
      requestId = requestId,
      lawyerFee = lawyerFee,
      legalExpenses = 0.0,
      proposedDays = 2,
      proposalNote = note
    )
  }

  /**
   * محاكاة وصول طلب جديد في النطاق الجغرافي للمحامي لاختبار الـ Popup مع الـ Alarm لمدة 20 ثانية
   */
  fun simulateIncomingRequestForLawyer(): ServiceRequest {
    val currentLawyer = _currentUser.value
    val gov = currentLawyer.assignedGovernorate.ifBlank { "القاهرة" }
    val district = currentLawyer.assignedDistrict.ifBlank { "مصر الجديدة" }
    val court = currentLawyer.assignedCourtJurisdiction.ifBlank { "نيابة مصر الجديدة الجزئية • محكمة مصر الجديدة" }

    val simulatedId = "req_live_${System.currentTimeMillis() % 10000}"
    val sampleTitles = listOf(
      "حضور فوري وتحقيق عاجل مع موكل أمام النيابة العامة",
      "صياغة مذكرة دفاع عاجلة في جنحة شيك بدون رصيد",
      "إجراءات مستعجلة لإثبات حالة ومعاينة شقة متنازع عليها",
      "طلب تمثيل قانوني مستعجل أمام المحكمة الجزئية"
    )
    val chosenTitle = sampleTitles.random()
    val budget = listOf(3500.0, 4500.0, 5000.0, 6500.0, 8000.0).random()

    val newReq = ServiceRequest(
      id = simulatedId,
      title = chosenTitle,
      category = RequestCategory.CRIMINAL_FINANCIAL,
      description = "مطلوب بشكل عاجل انتقال وحضور محامٍ معتمد فورياً أمام النيابة العامة بمقر محكمة $gov دائرة $district للاطلاع على المحضر وإخلاء السبيل بضمان.",
      city = gov,
      budgetRange = "${budget.toInt()} ج.م",
      budgetAmount = budget,
      urgency = RequestUrgency.URGENT,
      status = RequestStatus.OPEN,
      clientId = "client_uber_sample",
      clientName = "أ. حسام فوزي النجار",
      createdAt = "الآن",
      bidsCount = 0,
      courtLocation = GeoLocation(
        city = gov,
        district = district,
        courtJurisdiction = court,
        latitude = currentLawyer.officeLatitude ?: 30.0911,
        longitude = currentLawyer.officeLongitude ?: 31.3253,
        locationPurpose = "مقر المحكمة ومكان الواقعة",
        specificLandmark = "بالقرب من مجمع المحاكم الجزئية"
      )
    )

    _requests.value = listOf(newReq) + _requests.value
    if (_isLawyerAvailable.value && _currentUser.value.role == UserRole.LAWYER) {
      _incomingDispatchRequest.value = newReq
    }
    return newReq
  }

  init {
    seedInitialData()
  }

  fun switchRole(newRole: UserRole) {
    when (newRole) {
      UserRole.CLIENT -> {
        _currentUser.value = UserProfile(
          id = "user_client_1",
          name = "م. شريف عبد الفتاح التميمي",
          email = "sherif.tamimi@example.com",
          phone = "+20 100 123 4567",
          role = UserRole.CLIENT,
          balance = 4500.0,
          isVerified = true,
          nationalIdOrCr = "29408151203948",
          officeAddress = "القاهرة - التجمع الخامس"
        )
      }
      UserRole.LAWYER -> {
        _currentUser.value = UserProfile(
          id = "lawyer_1",
          name = "المستشار د. أحمد عبد العال الشناوي",
          email = "dr.elshinnawy@law.eg",
          phone = "+20 111 987 6543",
          role = UserRole.LAWYER,
          balance = 8200.0,
          licenseNumber = "قيد نقابة المحامين: 431908 - استئناف ونقض",
          isVerified = true,
          nationalIdOrCr = "28504120109482",
          officeAddress = "القاهرة - مصر الجديدة - شارع الأهرام",
          assignedGovernorate = "القاهرة",
          assignedDistrict = "مصر الجديدة",
          assignedCourtJurisdiction = "نيابة مصر الجديدة الجزئية • محكمة مصر الجديدة الابتدائية"
        )
      }
      UserRole.ADMIN -> {
        _currentUser.value = UserProfile(
          id = "user_admin_1",
          name = "إدارة منصة مِتر ولجان التحكيم (مصر)",
          email = "admin@maitre.eg",
          phone = "+20 2 2790 0000",
          role = UserRole.ADMIN,
          balance = 54800.0,
          isVerified = true,
          nationalIdOrCr = "10023450912384",
          officeAddress = "القاهرة - وسط البلد - ميدان طلعت حرب"
        )
      }
    }
    scope.launch {
      userPrefs?.saveUserSession(_currentUser.value)
    }
  }

  private fun seedInitialData() {
    val initialLawyers = listOf(
      Lawyer(
        id = "lawyer_1",
        name = "المستشار د. أحمد عبد العال الشناوي",
        specialization = RequestCategory.COMMERCIAL,
        degree = LawyerBarDegree.CASSATION,
        city = "القاهرة",
        licenseNumber = "قيد نقض: 431908",
        isVerified = true,
        verificationStatus = VerificationStatus.VERIFIED,
        rating = 4.9,
        reviewsCount = 48,
        yearsExperience = 15,
        bio = "دكتوراه في القانون التجاري والاقتصادي. محامٍ مقيد بالنقض والدستورية العليا ومجلس الدولة. متخصص في حوكمة الشركات وعقود التأسيس والنزاعات التجارية أمام محكمة القاهرة الاقتصادية.",
        consultationFee = 1500.0
      ),
      Lawyer(
        id = "lawyer_2",
        name = "الأستاذة فاطمة السيد عبد السلام",
        specialization = RequestCategory.LABOR,
        degree = LawyerBarDegree.PRIMARY,
        city = "الجيزة",
        licenseNumber = "قيد ابتدائي: 420115",
        isVerified = true,
        verificationStatus = VerificationStatus.VERIFIED,
        rating = 4.8,
        reviewsCount = 37,
        yearsExperience = 11,
        bio = "محامية متخصصة في قانون العمل المصري والتأمينات الاجتماعية وصياغة لوائح العمل المعتمدة والتمثيل أمام الدوائر العمالية بمحكمة الجيزة الابتدائية.",
        consultationFee = 1000.0
      ),
      Lawyer(
        id = "lawyer_3",
        name = "المستشار حسام الدين مصطفى",
        specialization = RequestCategory.CONTRACTS,
        degree = LawyerBarDegree.CASSATION,
        city = "الإسكندرية",
        licenseNumber = "قيد نقض: 441029",
        isVerified = true,
        verificationStatus = VerificationStatus.VERIFIED,
        rating = 4.95,
        reviewsCount = 56,
        yearsExperience = 18,
        bio = "محامٍ بالنقض والدستورية العليا. خبير عقود المقاولات والتطوير العقاري والاستثمار بمحافظتي الإسكندرية ومطروح والتمثيل أمام محاكم الاستئناف والنقض.",
        consultationFee = 2000.0
      ),
      Lawyer(
        id = "lawyer_4",
        name = "المستشار محمد عبد الرحمن الديب",
        specialization = RequestCategory.PERSONAL_STATUS,
        degree = LawyerBarDegree.APPEAL,
        city = "القاهرة",
        licenseNumber = "قيد استئناف: 415082",
        isVerified = true,
        verificationStatus = VerificationStatus.VERIFIED,
        rating = 4.75,
        reviewsCount = 29,
        yearsExperience = 16,
        bio = "مستشار قانوني مقيد بالاستئناف متخصص في قضايا الأسرة وقسمة التركات وفرز وتجنيب التركات وتوثيق إعلام الوراثة أمام محاكم الأسرة بالقاهرة.",
        consultationFee = 1200.0
      ),
      Lawyer(
        id = "lawyer_5",
        name = "الأستاذة مروة محمود الألفي",
        specialization = RequestCategory.REAL_ESTATE,
        degree = LawyerBarDegree.APPEAL,
        city = "المنصورة",
        licenseNumber = "قيد استئناف: 448991",
        isVerified = false,
        verificationStatus = VerificationStatus.PENDING,
        rating = 4.6,
        reviewsCount = 14,
        yearsExperience = 6,
        bio = "محامية متخصصة في الشهر العقاري وقضايا صحة ونفاذ والتسجيل العيني والنزاعات الإيجارية وفق القانون المدني المصري أمام محاكم المنصورة والشرقية.",
        consultationFee = 800.0
      )
    )
    _lawyers.value = initialLawyers

    val initialClientRegistrations = listOf(
      ClientRegistration(
        id = "client_reg_1",
        name = "م. شريف عبد الفتاح التميمي",
        phone = "+20 100 123 4567",
        email = "sherif.tamimi@techcorp.eg",
        nationalId = "28905140103951",
        clientType = ClientType.CORPORATE,
        companyName = "شركة التميمي للحلول البرمجية والتحول الرقمي",
        governorate = "القاهرة",
        idCardFrontUri = "id_card_front.jpg",
        idCardBackUri = "id_card_back.jpg",
        status = VerificationStatus.PENDING,
        registrationDate = "اليوم 09:30 ص"
      ),
      ClientRegistration(
        id = "client_reg_2",
        name = "الأستاذ أحمد فريد المنشاوي",
        phone = "+20 102 555 8899",
        email = "ahmed.manshawi@gmail.com",
        nationalId = "29307221204856",
        clientType = ClientType.INDIVIDUAL,
        companyName = null,
        governorate = "الجيزة",
        idCardFrontUri = "id_card_front.jpg",
        idCardBackUri = "id_card_back.jpg",
        status = VerificationStatus.VERIFIED,
        registrationDate = "أمس 04:15 م",
        adminReviewNotes = "تم فحص بطاقة الرقم القومي ومطابقة بيانات السجل المدني رسمياً."
      )
    )
    _clientRegistrations.value = initialClientRegistrations

    val initialRequests = listOf(
      ServiceRequest(
        id = "req_101",
        title = "صياغة عقد شراكة وتأسيس شركة ذات مسؤولية محدودة (LLC)",
        category = RequestCategory.COMMERCIAL,
        description = "نحن شركاء في مشروع برمجي بحاجة لصياغة عقد تأسيس وتوزيع حصص والشروط والأحكام وخطط التخارج والتحكيم وفق قانون الشركات المصري رقم 159 لسنة 1981 وهيئة الاستثمار GAFI.",
        city = "القاهرة",
        budgetRange = "5,000 - 10,000 ج.م",
        budgetAmount = 7500.0,
        urgency = RequestUrgency.NORMAL,
        status = RequestStatus.IN_PROGRESS,
        clientId = "user_client_1",
        clientName = "م. شريف عبد الفتاح التميمي",
        acceptedBidId = "bid_201",
        createdAt = "15 سبتمبر 2026",
        bidsCount = 3,
        courtLocation = GeoLocation("القاهرة", "التجمع الخامس", "محكمة القاهرة الاقتصادية", 30.0444, 31.2357),
        isMeetingConfirmed = false,
        meetingConfirmedAt = null,
        meetingQrToken = "MTR-MEET-101-8492"
      ),
      ServiceRequest(
        id = "req_102",
        title = "دعوى إثبات صحة توقيع على عقد بيع ابتدائي",
        category = RequestCategory.CONTRACTS,
        description = "المطلوب إقامة دعوى إثبات صحة توقيع على عقد بيع سيارة وشقة محررة مع الطرف الثاني لحفظ الحقوق وإثبات حجية التوقيع أمام المحكمة الجزئية.",
        city = "الجيزة",
        budgetRange = "2,500 - 4,500 ج.م",
        budgetAmount = 3000.0,
        urgency = RequestUrgency.NORMAL,
        status = RequestStatus.OPEN,
        clientId = "user_client_2",
        clientName = "أ. حازم القاضي",
        createdAt = "17 سبتمبر 2026",
        bidsCount = 4,
        courtLocation = GeoLocation("الجيزة", "الدقي", "محكمة الجيزة الابتدائية", 30.0131, 31.2089),
        appliedTemplateId = "template_sehet_tawkee"
      ),
      ServiceRequest(
        id = "req_103",
        title = "دعوى صحة ونفاذ عقد بيع وحدة سكنية ونقل ملكية",
        category = RequestCategory.REAL_ESTATE,
        description = "مطلوب محامٍ مقيد بالاستئناف لمباشرة إجراءات دعوى صحة ونفاذ عقد البيع الابتدائي لشقة سكنية بمدينة نصر والتسجيل بالشهر العقاري وسداد الرسوم وإشهار الصحيفة.",
        city = "القاهرة",
        budgetRange = "8,000 - 14,000 ج.م",
        budgetAmount = 9000.0,
        urgency = RequestUrgency.NORMAL,
        status = RequestStatus.OPEN,
        clientId = "user_client_1",
        clientName = "م. شريف عبد الفتاح التميمي",
        createdAt = "18 سبتمبر 2026",
        bidsCount = 2,
        courtLocation = GeoLocation("القاهرة", "مدينة نصر", "محكمة القاهرة الابتدائية - الدائرة المدنية", 30.0561, 31.3301),
        appliedTemplateId = "template_seha_nafath"
      ),
      ServiceRequest(
        id = "req_104",
        title = "استخراج إعلام وراثة شرعي وتوزيع التركة",
        category = RequestCategory.PERSONAL_STATUS,
        description = "حصر تركة واستخراج إعلام وراثة رسمي للمرحوم الوالد وتحديد أنصبة الورثة الشرعيين أمام محكمة الأسرة بمدينة نصر.",
        city = "القاهرة",
        budgetRange = "3,000 - 6,000 ج.م",
        budgetAmount = 3500.0,
        urgency = RequestUrgency.NORMAL,
        status = RequestStatus.COMPLETED,
        clientId = "user_client_3",
        clientName = "الحاج عبد الله السعيد",
        acceptedBidId = "bid_204",
        createdAt = "10 سبتمبر 2026",
        bidsCount = 5,
        courtLocation = GeoLocation("القاهرة", "مدينة نصر", "محكمة الأسرة بمدينة نصر", 30.0561, 31.3301),
        appliedTemplateId = "template_elam_werasa",
        isMeetingConfirmed = true,
        meetingConfirmedAt = "10 سبتمبر 2026 - 11:30 ص",
        meetingQrToken = "MTR-MEET-104-5102"
      ),
      ServiceRequest(
        id = "req_105",
        title = "نزاع على إخلال بتنفيذ عقد مقاولات وتشطيبات عقارية",
        category = RequestCategory.REAL_ESTATE,
        description = "نشأ خلاف قانوني بشأن عدم مطابقة المواصفات الهندسية وتأخير تسليم المشروع لأكثر من 4 أشهر، ومطلوب مباشرة دعوى تعويض وفسخ وإثبات حالة مستعجل.",
        city = "الجيزة",
        budgetRange = "6,000 - 12,000 ج.م",
        budgetAmount = 8000.0,
        urgency = RequestUrgency.URGENT,
        status = RequestStatus.DISPUTED,
        clientId = "user_client_1",
        clientName = "م. شريف عبد الفتاح التميمي",
        acceptedBidId = "bid_205",
        createdAt = "05 سبتمبر 2026",
        bidsCount = 4,
        courtLocation = GeoLocation("الجيزة", "6 أكتوبر", "محكمة 6 أكتوبر الابتدائية", 29.9737, 30.9500)
      )
    )
    _requests.value = initialRequests

    val initialDisbursements = listOf(
      DisbursementRequest(
        id = "disb_101_1",
        requestId = "req_101",
        lawyerId = "lawyer_1",
        lawyerName = "المستشار د. أحمد عبد العال الشناوي",
        type = DisbursementType.JUDICIAL_EXPENSES,
        amount = 500.0,
        reason = "رسوم توثيق عقود التأسيس بالغرفة التجارية وهيئة الاستثمار واستخراج شهادة عدم التباس",
        receiptOrRef = "GAFI-REC-49102",
        status = DisbursementStatus.PENDING,
        createdAt = "اليوم 11:30 ص"
      )
    )
    _disbursementRequests.value = initialDisbursements

    val initialBids = listOf(
      Bid(
        id = "bid_201",
        requestId = "req_101",
        lawyerId = "lawyer_1",
        lawyerName = "المستشار د. أحمد عبد العال الشناوي",
        lawyerTitle = "محامٍ مقيد بالنقض والاستئناف العالي",
        lawyerDegree = LawyerBarDegree.CASSATION,
        lawyerLicenseNumber = "قيد نقض: 431908",
        lawyerRating = 4.9,
        lawyerCasesCount = 48,
        lawyerFee = 7000.0,
        legalExpenses = 500.0,
        clientFeePercent = 5.0,
        lawyerFeePercent = 5.0,
        platformFeePercent = 5.0,
        proposedDays = 4,
        proposalNote = "تحياتي، يشمل العرض صياغة عقد التأسيس ونظام الإدارة والحصص وبنود التخارج وحماية الملكية الفكرية مع جلستي مراجعة ومطابقة متطلبات هيئة الاستثمار.",
        status = BidStatus.ACCEPTED,
        createdAt = "15 سبتمبر 2026"
      ),
      Bid(
        id = "bid_202",
        requestId = "req_101",
        lawyerId = "lawyer_3",
        lawyerName = "المستشار حسام الدين مصطفى",
        lawyerTitle = "محامٍ بالنقض ومستشار عقود الشركات",
        lawyerDegree = LawyerBarDegree.CASSATION,
        lawyerLicenseNumber = "قيد نقض: 441029",
        lawyerRating = 4.95,
        lawyerCasesCount = 56,
        lawyerFee = 7800.0,
        legalExpenses = 700.0,
        clientFeePercent = 5.0,
        lawyerFeePercent = 5.0,
        platformFeePercent = 5.0,
        proposedDays = 5,
        proposalNote = "أتشرف بتقديم العرض لصياغة اتفاقية الشركاء وعقد التأسيس وفق المعايير القانونية الدقيقة وحماية المؤسسين.",
        status = BidStatus.PENDING,
        createdAt = "16 سبتمبر 2026"
      ),
      Bid(
        id = "bid_203",
        requestId = "req_102",
        lawyerId = "lawyer_2",
        lawyerName = "الأستاذة فاطمة السيد عبد السلام",
        lawyerTitle = "محامية مدني وعقود",
        lawyerDegree = LawyerBarDegree.PRIMARY,
        lawyerLicenseNumber = "قيد ابتدائي: 420115",
        lawyerRating = 4.8,
        lawyerCasesCount = 37,
        lawyerFee = 2500.0,
        legalExpenses = 500.0,
        clientFeePercent = 5.0,
        lawyerFeePercent = 5.0,
        platformFeePercent = 5.0,
        proposedDays = 3,
        proposalNote = "سأقوم بإعداد صحيفة دعوى إثبات صحة التوقيع وإعلان المدعى عليه ومتابعة الجلسة واستلام الصيغة التنفيذية المعتمدة.",
        status = BidStatus.PENDING,
        createdAt = "17 سبتمبر 2026"
      ),
      Bid(
        id = "bid_204",
        requestId = "req_104",
        lawyerId = "lawyer_4",
        lawyerName = "المستشار محمد عبد الرحمن الديب",
        lawyerTitle = "مستشار تركات وقضايا الأسرة",
        lawyerDegree = LawyerBarDegree.APPEAL,
        lawyerLicenseNumber = "قيد استئناف: 415082",
        lawyerRating = 4.75,
        lawyerCasesCount = 29,
        lawyerFee = 3000.0,
        legalExpenses = 500.0,
        clientFeePercent = 5.0,
        lawyerFeePercent = 5.0,
        platformFeePercent = 5.0,
        proposedDays = 7,
        proposalNote = "إنجاز إجراءات قيد وضبط إعلام الوراثة وحضور جلسة الشهود واستخراج الصيغة الرسمية لحصر التركة.",
        status = BidStatus.ACCEPTED,
        createdAt = "10 سبتمبر 2026"
      ),
      Bid(
        id = "bid_205",
        requestId = "req_105",
        lawyerId = "lawyer_3",
        lawyerName = "المستشار حسام الدين مصطفى",
        lawyerTitle = "محامٍ بالنقض ومستشار عقود الشركات",
        lawyerDegree = LawyerBarDegree.CASSATION,
        lawyerLicenseNumber = "قيد نقض: 441029",
        lawyerRating = 4.95,
        lawyerCasesCount = 56,
        lawyerFee = 7500.0,
        legalExpenses = 500.0,
        clientFeePercent = 5.0,
        lawyerFeePercent = 5.0,
        platformFeePercent = 5.0,
        proposedDays = 10,
        proposalNote = "إقامة دعوى التعويض وندب خبير هندسي لإثبات حالة الأعمال الإنشائية ومحاسبة المقاول.",
        status = BidStatus.ACCEPTED,
        createdAt = "06 سبتمبر 2026"
      )
    )
    _bids.value = initialBids

    // Note: Platform collects ONLY platform fee (e.g. 150 ج.م or 5% matching fee).
    val initialEscrows = listOf(
      EscrowTransaction(
        id = "tx_301",
        requestId = "req_101",
        totalAmount = 375.0, // 5% platform service fee on 7,500 budget
        lawyerAmount = 7500.0, // Agreed directly between client & lawyer
        platformFee = 375.0,
        status = EscrowStatus.HELD,
        paymentMethod = "إنستاباي InstaPay",
        referenceNumber = "MTR-EGY-91823",
        date = "16 سبتمبر 2026"
      ),
      EscrowTransaction(
        id = "tx_302",
        requestId = "req_104",
        totalAmount = 750.0, // 5% platform service fee on 15,000 budget
        lawyerAmount = 15000.0,
        platformFee = 750.0,
        status = EscrowStatus.RELEASED,
        paymentMethod = "فودافون كاش",
        referenceNumber = "MTR-EGY-88401",
        date = "14 سبتمبر 2026"
      ),
      EscrowTransaction(
        id = "tx_303",
        requestId = "req_105",
        totalAmount = 400.0,
        lawyerAmount = 8000.0,
        platformFee = 400.0,
        status = EscrowStatus.FROZEN_FOR_DISPUTE,
        paymentMethod = "إنستاباي InstaPay",
        referenceNumber = "MTR-EGY-77319",
        date = "08 سبتمبر 2026"
      )
    )
    _escrowTransactions.value = initialEscrows

    val initialMessages = listOf(
      ChatMessage(
        id = "msg_1",
        requestId = "req_101",
        senderId = "system",
        senderName = "منظومة مِتر للربط القانوني",
        senderRole = UserRole.ADMIN,
        text = "تم سداد رسم المنصة وفتح بيانات الاتصال ومساحة العمل القانونية بين الطرفين بنجاح (CONTACT_UNLOCKED). أتعاب المحاماة يتم تسويتها مباشرة بين العميل والمحامي طبقاً لتقاليد نقابة المحامين.",
        timestamp = "16 سبتمبر 10:00 ص",
        isSystemMessage = true
      ),
      ChatMessage(
        id = "msg_2",
        requestId = "req_101",
        senderId = "lawyer_1",
        senderName = "المستشار د. أحمد الشناوي",
        senderRole = UserRole.LAWYER,
        text = "أهلاً بك م. شريف، بدأت بصياغة مسودة عقد الشراكة وتأسيس الشركة. هل تود إضافة شرط التحكيم أمام مركز القاهرة الإقليمي للتحكيم التجاري الدولي (CRCICA)؟",
        timestamp = "16 سبتمبر 10:15 ص"
      ),
      ChatMessage(
        id = "msg_3",
        requestId = "req_101",
        senderId = "user_client_1",
        senderName = "م. شريف عبد الفتاح",
        senderRole = UserRole.CLIENT,
        text = "مرحباً سيادة المستشار، نعم نفضل إضافة شرط التحكيم بمركز القاهرة مع بند حماية الملكية الفكرية وسرية المعلومات.",
        timestamp = "16 سبتمبر 11:30 ص"
      ),
      ChatMessage(
        id = "msg_4",
        requestId = "req_101",
        senderId = "lawyer_1",
        senderName = "المستشار د. أحمد الشناوي",
        senderRole = UserRole.LAWYER,
        text = "ممتاز، أرفقت لك مسودة العقد المبدئية بصيغة PDF للاطلاع والملاحظات قبل اعتماد النسخة النهائية.",
        timestamp = "17 سبتمبر 04:20 م",
        attachmentName = "مسودة_عقد_تأسيس_شركة_متر_V1.pdf"
      )
    )
    _messages.value = initialMessages

    val initialNotifications = listOf(
      AppNotification(
        id = "notif_1",
        title = "عرض جديد على طلبك",
        body = "قدم المستشار حسام الدين مصطفى عرضاً بقيمة 8,500 ج.م على طلب صياغة عقد الشراكة.",
        timestamp = "منذ 30 دقيقة",
        relatedRequestId = "req_101"
      ),
      AppNotification(
        id = "notif_2",
        title = "فتح قنوات الاتصال والعمل",
        body = "تم فتح بيانات الاتصال وتفعيل مساحة العمل في طلبك رقم #101 بنجاح.",
        timestamp = "أمس 10:00 ص",
        relatedRequestId = "req_101"
      )
    )
    _notifications.value = initialNotifications

    val initialReviews = listOf(
      LawyerReview(
        id = "rev_1",
        lawyerId = "lawyer_1",
        clientName = "طارق المهدوي",
        stars = 5,
        comment = "دكتور أحمد قامة قانونية متميزة جداً وسريع في الإنجاز. صاغ لنا عقد توزيع استثماري محكم ووفّر علينا الكثير من النزاعات.",
        tags = listOf("دقة واحترافية", "سرعة الرد", "صياغة محكمة"),
        date = "12 سبتمبر 2026"
      ),
      LawyerReview(
        id = "rev_2",
        lawyerId = "lawyer_2",
        clientName = "أحمد رضوان",
        stars = 5,
        comment = "الأستاذة فاطمة ساعدتني في كسب قضيتي العمالية واسترداد كافة مستحقاتي خلال وقت قياسي.",
        tags = listOf("خبرة قانونية عالية", "تواصل مستمر"),
        date = "08 سبتمبر 2026"
      )
    )
    _reviews.value = initialReviews

    // Initial mock security audit events
    _violations.value = listOf(
      ContactLeakageViolation(
        id = "violation_101",
        timestamp = "14 سبتمبر 2026 14:20",
        userRole = UserRole.CLIENT,
        userName = "مستخدم تجريبي (عميل)",
        contextField = "وصف طلب القضية",
        violationType = "EGYPTIAN_PHONE_NUMBER",
        redactedSnippet = "010****4567",
        severity = RiskSeverity.HIGH,
        isBlocked = true
      )
    )

    _securityRiskEvents.value = listOf(
      SecurityRiskEvent(
        id = "sec_1",
        eventType = "CONTACT_LEAKAGE_PREVENTED",
        description = "تم حظر محاولة إدراج رقم هاتف مصري محلي قبل مرحلة فتح الاتصال وسداد رسم المنصة.",
        userIdentifier = "user_client_1",
        severity = RiskSeverity.HIGH,
        timestamp = "14 سبتمبر 2026 14:20"
      )
    )

    _disputes.value = listOf(
      Dispute(
        id = "disp_101",
        requestId = "req_105",
        requestTitle = "نزاع على إخلال بتنفيذ عقد مقاولات وتشطيبات عقارية",
        openedByRole = UserRole.CLIENT,
        openedByName = "م. شريف عبد الفتاح التميمي",
        reason = "تأخر غير مبرر في تسليم تقرير المعاينة الهندسية وإيداع صحيفة الدعوى بالمحكمة",
        details = "تجاوز المحامي المدة المتفق عليها (10 أيام) دون إفادة الموكل برقم قيد الدعوى أو الجلسة المحددة بمحكمة 6 أكتوبر.",
        status = DisputeStatus.UNDER_REVIEW,
        adminNote = "تم تجميد الضمان وإحالة ملف النزاع للمستشار رئيس لجنة التحكيم لطلب إفادة رسمية من المحامي.",
        createdAt = "08 سبتمبر 2026"
      )
    )
  }

  fun updateRequestStatus(requestId: String, newStatus: RequestStatus) {
    _requests.value = _requests.value.map {
      if (it.id == requestId) it.copy(status = newStatus) else it
    }
  }

  /**
   * Creates a service request with mandatory Anti-Contact-Circumvention check.
   */
  fun createRequest(
    title: String,
    category: RequestCategory,
    description: String,
    city: String,
    budgetAmount: Double,
    urgency: RequestUrgency,
    courtLocation: GeoLocation? = null,
    appliedTemplateId: String? = null
  ): Result<ServiceRequest> {
    val client = _currentUser.value
    if (client.role == UserRole.LAWYER) {
      return Result.failure(IllegalArgumentException("غير مصرح للمحامي بطلب تقديم خدمة. المحامي متلقٍ للطلبات فقط من المستخدمين ضمن نطاق اختصاصه الجغرافي المحدد مسبقاً."))
    }

    // 1. Inspect title and description for contact leakage
    val titleCheck = ContactLeakageDetectionService.inspectText(title, "request_title")
    if (titleCheck.isBlocked) {
      recordViolation(titleCheck, "عنوان الطلب")
      return Result.failure(IllegalArgumentException(titleCheck.userMessage ?: "محتوى غير مسموح به"))
    }

    val descCheck = ContactLeakageDetectionService.inspectText(description, "request_description")
    if (descCheck.isBlocked) {
      recordViolation(descCheck, "تفاصيل ووصف الطلب")
      return Result.failure(IllegalArgumentException(descCheck.userMessage ?: "محتوى غير مسموح به"))
    }

    val newId = "req_${System.currentTimeMillis() % 10000}"
    val newReq = ServiceRequest(
      id = newId,
      title = title,
      category = category,
      description = description,
      city = city,
      budgetRange = "${budgetAmount.toInt()} ج.م",
      budgetAmount = budgetAmount,
      urgency = urgency,
      status = RequestStatus.OPEN,
      clientId = client.id,
      clientName = client.name,
      createdAt = "اليوم",
      bidsCount = 0,
      courtLocation = courtLocation,
      appliedTemplateId = appliedTemplateId
    )
    _requests.value = listOf(newReq) + _requests.value

    triggerIncomingDispatchRequest(newReq)

    addNotification(
      title = "تم نشر طلبك بنجاح",
      body = "طلبك: \"$title\" متاح الآن لجميع المحامين المقيدين بنقابة المحامين لتقديم عروضهم.",
      requestId = newId
    )
    return Result.success(newReq)
  }

  /**
   * Adds a lawyer bid with breakdown of lawyer fee, legal expenses, and platform fee.
   */
  fun addBid(
    requestId: String,
    lawyerFee: Double,
    legalExpenses: Double = 0.0,
    proposedDays: Int,
    proposalNote: String,
    customPlatformFeePercent: Double? = null
  ): Result<Bid> {
    val noteCheck = ContactLeakageDetectionService.inspectText(proposalNote, "lawyer_bid_proposal")
    if (noteCheck.isBlocked) {
      recordViolation(noteCheck, "ملاحظات وتفاصيل عرض المحامي")
      return Result.failure(IllegalArgumentException(noteCheck.userMessage ?: "محتوى غير مسموح به"))
    }

    val lawyer = _currentUser.value
    val targetReq = _requests.value.find { it.id == requestId }
    if (targetReq != null && !lawyer.isWithinLawyerJurisdiction(targetReq.city, targetReq.courtLocation)) {
      return Result.failure(IllegalArgumentException("خارج نطاق الاختصاص الجغرافي المقيد للمحامي (${lawyer.assignedGovernorate} - ${lawyer.assignedDistrict}). المحامي متلقٍ للطلبات في إطاره الجغرافي المحدد مسبقاً فقط."))
    }
    val clientFee = customPlatformFeePercent ?: _clientPlatformFeePercentage.value
    val lawyerFeeP = _lawyerPlatformFeePercentage.value
    val newBid = Bid(
      id = "bid_${System.currentTimeMillis() % 10000}",
      requestId = requestId,
      lawyerId = lawyer.id,
      lawyerName = lawyer.name,
      lawyerTitle = lawyer.barDegree.formalTitleAr,
      lawyerDegree = lawyer.barDegree,
      lawyerLicenseNumber = lawyer.licenseNumber ?: "قيد استئناف: 439900",
      lawyerRating = 4.9,
      lawyerCasesCount = 35,
      lawyerFee = lawyerFee,
      legalExpenses = legalExpenses,
      clientFeePercent = clientFee,
      lawyerFeePercent = lawyerFeeP,
      platformFeePercent = clientFee,
      proposedDays = proposedDays,
      proposalNote = proposalNote,
      status = BidStatus.PENDING,
      createdAt = "الآن"
    )

    _bids.value = _bids.value + newBid

    // Update request bids count
    _requests.value = _requests.value.map {
      if (it.id == requestId) it.copy(bidsCount = it.bidsCount + 1) else it
    }

    addNotification(
      title = "تم تقديم عرضك بنجاح",
      body = "تم تقديم عرضك القانوني بإجمالي ${newBid.grandTotalAmount.toInt()} ج.م بانتظار موافقة الموكل.",
      requestId = requestId
    )

    return Result.success(newBid)
  }

  /**
   * Overloaded addBid for backward compatibility.
   */
  fun addBid(
    requestId: String,
    proposedAmount: Double,
    proposedDays: Int,
    proposalNote: String
  ): Result<Bid> {
    return addBid(
      requestId = requestId,
      lawyerFee = proposedAmount * 0.9,
      legalExpenses = proposedAmount * 0.1,
      proposedDays = proposedDays,
      proposalNote = proposalNote
    )
  }

  /**
   * Accepts a lawyer bid, initiates platform service fee settlement, and unlocks contact info (CONTACT_UNLOCKED).
   */
  fun acceptBid(requestId: String, bidId: String, paymentMethod: String = "إنستاباي InstaPay") {
    val targetBid = _bids.value.find { it.id == bidId } ?: return
    val lawyerAmount = targetBid.lawyerNetAmount
    val platformFee = targetBid.platformFeeAmount

    // 1. Update Bid
    _bids.value = _bids.value.map {
      if (it.id == bidId) it.copy(status = BidStatus.ACCEPTED)
      else if (it.requestId == requestId && it.id != bidId) it.copy(status = BidStatus.REJECTED)
      else it
    }

    // 2. Create Platform Fee Transaction
    val escrow = EscrowTransaction(
      id = "tx_${System.currentTimeMillis() % 10000}",
      requestId = requestId,
      totalAmount = targetBid.grandTotalAmount,
      lawyerAmount = lawyerAmount,
      platformFee = platformFee,
      status = EscrowStatus.HELD,
      paymentMethod = paymentMethod,
      referenceNumber = "MTR-EGY-${(10000..99999).random()}",
      date = "اليوم"
    )
    _escrowTransactions.value = listOf(escrow) + _escrowTransactions.value

    // 3. Update Request Status
    val generatedMeetingQrToken = "MTR-MEET-${requestId.takeLast(3).uppercase()}-${(1000..9999).random()}"
    _requests.value = _requests.value.map {
      if (it.id == requestId) it.copy(
        status = RequestStatus.IN_PROGRESS,
        acceptedBidId = bidId,
        isMeetingConfirmed = false,
        meetingConfirmedAt = null,
        meetingQrToken = generatedMeetingQrToken
      ) else it
    }

    // 4. System message in workspace
    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = requestId,
      senderId = "system",
      senderName = "منظومة مِتر للربط القانوني",
      senderRole = UserRole.ADMIN,
      text = "تم قبول العرض بنجاح بمبلغ إجمالي (${targetBid.grandTotalAmount.toInt()} ج.م) وتم فتح بيانات الاتصال المباشرة ومساحة العمل (CONTACT_UNLOCKED). تم توليد رمز الاستجابة السريعة (QR) بشاشة المحامي ويتعين مسحه بكاميرا العميل لتوثيق المقابلة وبدء الخدمة رسمياً.",
      timestamp = "الآن",
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg

    addNotification(
      title = "تم قبول العرض وفتح الاتصال",
      body = "تم فتح بيانات الاتصال المباشرة وتوليد رمز QR لبدء الخدمة وتوثيق المقابلة.",
      requestId = requestId
    )
  }

  /**
   * تأكيد الالتقاء وبدء الخدمة القضائية رسمياً عبر مسح كاميرا العميل لرمز الـ QR الخاص بالمحامي
   */
  fun confirmMeetingWithQr(requestId: String, token: String? = null): Result<Boolean> {
    val req = _requests.value.find { it.id == requestId }
      ?: return Result.failure(Exception("لم يتم العثور على القضية"))
    val nowFormatted = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")).format(Date())

    _requests.value = _requests.value.map {
      if (it.id == requestId) {
        it.copy(
          isMeetingConfirmed = true,
          meetingConfirmedAt = nowFormatted
        )
      } else it
    }

    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = requestId,
      senderId = "system",
      senderName = "منظومة مِتر للتحقق الرقمي",
      senderRole = UserRole.ADMIN,
      text = "تم تأكيد المقابلة والالتقاء بنجاح عبر مسح رمز الاستجابة السريعة (QR Code) بكاميرا العميل في ($nowFormatted) 🤝 تم إثبات الحضور وبدء الخدمة القانونية رسمياً.",
      timestamp = "الآن",
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg

    addNotification(
      title = "تم تأكيد المقابلة وبدء الخدمة",
      body = "تم مسح رمز QR بنجاح وبدء الخدمة القانونية رسمياً ومباشرة الإجراءات.",
      requestId = requestId
    )

    return Result.success(true)
  }

  /**
   * طلب المحامي صرف جزء من الأتعاب أو سلفة مصاريف قضائية أثناء تنفيذ القضية
   */
  fun requestDisbursement(
    requestId: String,
    type: DisbursementType,
    amount: Double,
    reason: String,
    receiptOrRef: String? = null
  ): Result<DisbursementRequest> {
    val req = _requests.value.find { it.id == requestId }
      ?: return Result.failure(Exception("لم يتم العثور على القضية"))
    val current = _currentUser.value
    if (amount <= 0) {
      return Result.failure(Exception("يجب أن يكون المبلغ المطلوب أكبر من الصفر"))
    }

    val newDisbursement = DisbursementRequest(
      id = "disb_${System.currentTimeMillis() % 100000}",
      requestId = requestId,
      lawyerId = current.id,
      lawyerName = current.name,
      type = type,
      amount = amount,
      reason = reason.trim(),
      receiptOrRef = receiptOrRef?.trim()?.ifBlank { null },
      status = DisbursementStatus.PENDING,
      createdAt = "اليوم - " + SimpleDateFormat("hh:mm a", Locale("ar")).format(Date())
    )

    _disbursementRequests.value = listOf(newDisbursement) + _disbursementRequests.value

    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = requestId,
      senderId = "system",
      senderName = "إشعار مالي نظامي",
      senderRole = UserRole.ADMIN,
      text = "قام المحامي بطلب (${type.titleAr}) بمبلغ ${amount.toInt()} ج.م لبيان: \"$reason\". للعميل حق القبول أو الرفض أو التعديل من شاشة تفاصيل القضية.",
      timestamp = "الآن",
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg

    addNotification(
      title = "طلب دفعة / مصاريف قضائية من المحامي",
      body = "طلب المحامي مبلغ ${amount.toInt()} ج.م (${type.titleAr}) لمتابعة القضية: $reason",
      requestId = requestId
    )

    return Result.success(newDisbursement)
  }

  /**
   * قبول الموكل لطلب الصرف
   */
  fun acceptDisbursement(disbursementId: String, clientNote: String? = null): Result<Unit> {
    val target = _disbursementRequests.value.find { it.id == disbursementId }
      ?: return Result.failure(Exception("طلب الصرف غير موجود"))

    val nowFormatted = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")).format(Date())
    _disbursementRequests.value = _disbursementRequests.value.map {
      if (it.id == disbursementId) {
        it.copy(
          status = DisbursementStatus.ACCEPTED,
          clientNote = clientNote,
          respondedAt = nowFormatted
        )
      } else it
    }

    val effectiveAmount = target.counterAmount ?: target.amount
    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = target.requestId,
      senderId = "system",
      senderName = "تسوية مالية معتمدة",
      senderRole = UserRole.ADMIN,
      text = "وافق الموكل على صرف مبلغ ${effectiveAmount.toInt()} ج.م (${target.type.titleAr}) لصالح المحامي لمباشرة الإجراءات القضائية بنجاح ✓",
      timestamp = "الآن",
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg

    addNotification(
      title = "تمت الموافقة على طلب الصرف",
      body = "وافق الموكل على صرف مبلغ ${effectiveAmount.toInt()} ج.م (${target.type.titleAr}).",
      requestId = target.requestId
    )

    return Result.success(Unit)
  }

  /**
   * رفض الموكل لطلب الصرف مع إبداء السبب
   */
  fun rejectDisbursement(disbursementId: String, reason: String): Result<Unit> {
    val target = _disbursementRequests.value.find { it.id == disbursementId }
      ?: return Result.failure(Exception("طلب الصرف غير موجود"))

    val nowFormatted = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")).format(Date())
    _disbursementRequests.value = _disbursementRequests.value.map {
      if (it.id == disbursementId) {
        it.copy(
          status = DisbursementStatus.REJECTED,
          clientNote = reason,
          respondedAt = nowFormatted
        )
      } else it
    }

    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = target.requestId,
      senderId = "system",
      senderName = "إشعار مالي",
      senderRole = UserRole.ADMIN,
      text = "تم رفض طلب صرف (${target.type.titleAr}) بمبلغ ${target.amount.toInt()} ج.م من قِبل الموكل. سبب الرفض: \"$reason\"",
      timestamp = "الآن",
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg

    addNotification(
      title = "تم رفض طلب الصرف",
      body = "اعتذر الموكل عن صرف المبلغ المطلوب: $reason",
      requestId = target.requestId
    )

    return Result.success(Unit)
  }

  /**
   * تعديل الموكل للمبلغ المقترح وتقديم عرض بديل
   */
  fun modifyDisbursement(disbursementId: String, counterAmount: Double, clientNote: String): Result<Unit> {
    val target = _disbursementRequests.value.find { it.id == disbursementId }
      ?: return Result.failure(Exception("طلب الصرف غير موجود"))
    if (counterAmount <= 0) {
      return Result.failure(Exception("المبلغ المقترح يجب أن يكون أكبر من الصفر"))
    }

    val nowFormatted = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")).format(Date())
    _disbursementRequests.value = _disbursementRequests.value.map {
      if (it.id == disbursementId) {
        it.copy(
          status = DisbursementStatus.MODIFIED_BY_CLIENT,
          counterAmount = counterAmount,
          clientNote = clientNote,
          respondedAt = nowFormatted
        )
      } else it
    }

    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = target.requestId,
      senderId = "system",
      senderName = "اقتراح تعديل مالي",
      senderRole = UserRole.ADMIN,
      text = "اقترح الموكل تعديل المبلغ المطلوب من ${target.amount.toInt()} ج.م إلى ${counterAmount.toInt()} ج.م مع الملاحظة: \"$clientNote\". يحق للمحامي قبول التعديل ومباشرة الصرف.",
      timestamp = "الآن",
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg

    addNotification(
      title = "اقتراح تعديل على طلب الصرف",
      body = "اقترح الموكل سداد مبلغ ${counterAmount.toInt()} ج.م بدلاً من ${target.amount.toInt()} ج.م.",
      requestId = target.requestId
    )

    return Result.success(Unit)
  }

  fun lawyerAcceptCounterDisbursement(disbursementId: String): Result<Unit> {
    return acceptDisbursement(disbursementId, "تم قبول المبلغ المعدل من قبل المحامي")
  }

  fun releaseEscrow(requestId: String) {
    _escrowTransactions.value = _escrowTransactions.value.map {
      if (it.requestId == requestId && it.status == EscrowStatus.HELD) {
        it.copy(status = EscrowStatus.RELEASED)
      } else it
    }

    _requests.value = _requests.value.map {
      if (it.id == requestId) it.copy(status = RequestStatus.COMPLETED) else it
    }

    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = requestId,
      senderId = "system",
      senderName = "منظومة مِتر للربط القانوني",
      senderRole = UserRole.ADMIN,
      text = "تم تأكيد إتمام العمل القانوني بنجاح وإغلاق مساحة العمل. شكراً لاختياركم منصة مِتر القانونية بمصر.",
      timestamp = "الآن",
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg

    addNotification(
      title = "تم إنجاز الطلب بنجاح",
      body = "تم إغلاق ملف الطلب القانوني بعد تأكيد إتمام العمل مع المحامي.",
      requestId = requestId
    )
  }

  /**
   * Funds an escrow for a service request, locks the funds securely, and transitions the request to IN_PROGRESS.
   */
  fun fundCaseEscrow(
    requestId: String,
    totalAmount: Double,
    lawyerAmount: Double,
    legalExpenses: Double = 0.0,
    platformFee: Double = 0.0,
    paymentMethod: String = "إنستاباي InstaPay",
    lawyerId: String? = null
  ): EscrowTransaction {
    val req = _requests.value.find { it.id == requestId }
    val refNum = "MTR-ESC-${(10000..99999).random()}"
    val newEscrow = EscrowTransaction(
      id = "tx_${System.currentTimeMillis() % 10000}",
      requestId = requestId,
      totalAmount = totalAmount,
      lawyerAmount = lawyerAmount,
      platformFee = platformFee,
      status = EscrowStatus.HELD,
      paymentMethod = paymentMethod,
      referenceNumber = refNum,
      date = "اليوم"
    )

    _escrowTransactions.value = listOf(newEscrow) + _escrowTransactions.value.filter { it.requestId != requestId }

    _requests.value = _requests.value.map {
      if (it.id == requestId) it.copy(status = RequestStatus.IN_PROGRESS) else it
    }

    if (paymentMethod.contains("رصيد") || paymentMethod.contains("المحفظة")) {
      val user = _currentUser.value
      if (user.balance >= totalAmount) {
        val updatedUser = user.copy(balance = (user.balance - totalAmount).coerceAtLeast(0.0))
        _currentUser.value = updatedUser
        scope.launch { userPrefs?.saveUserSession(updatedUser) }
      }
    }

    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = requestId,
      senderId = "system",
      senderName = "منظومة الضمان المالي لمتر",
      senderRole = UserRole.ADMIN,
      text = "تم إيداع كامل أتعاب ومصاريف القضية (${totalAmount.toInt()} ج.م) بحساب الضمان المالي بأمان (الرقم المرجعي: $refNum). تم فتح مساحة العمل وبيانات الاتصال المباشرة (CONTACT_UNLOCKED) والمحامي مخول ببدء الإجراءات القضائية فوراً.",
      timestamp = "الآن",
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg

    addNotification(
      title = "تم إيداع الأتعاب في حساب الضمان",
      body = "تم حجز مبلغ ${totalAmount.toInt()} ج.م بأمان للطلب (${req?.title ?: "القضية"}). المحامي سيباشر العمل فوراً ولن تُحرر الأتعاب إلا بموافقتك.",
      requestId = requestId
    )

    return newEscrow
  }

  fun releaseEscrowWithReview(
    requestId: String,
    stars: Int = 5,
    comment: String = "تم استلام الخدمة القانونية بحرفية وإتقان تام",
    tags: List<String> = listOf("دقة واحترافية", "سرعة الإنجاز")
  ) {
    releaseEscrow(requestId)
    val req = _requests.value.find { it.id == requestId }
    val targetBid = _bids.value.find { it.id == req?.acceptedBidId } ?: _bids.value.find { it.requestId == requestId }
    if (targetBid != null) {
      submitReview(targetBid.lawyerId, stars, comment, tags, requestId = requestId)
    }
  }

  fun openDispute(requestId: String, reason: String, details: String) {
    val user = _currentUser.value
    val req = _requests.value.find { it.id == requestId }
    val title = req?.title ?: "طلب خدمة قانونية"

    val newDispute = Dispute(
      id = "disp_${System.currentTimeMillis() % 10000}",
      requestId = requestId,
      requestTitle = title,
      openedByRole = user.role,
      openedByName = user.name,
      reason = reason,
      details = details,
      status = DisputeStatus.UNDER_REVIEW,
      createdAt = "الآن"
    )

    _disputes.value = listOf(newDispute) + _disputes.value

    _requests.value = _requests.value.map {
      if (it.id == requestId) it.copy(status = RequestStatus.DISPUTED) else it
    }

    _escrowTransactions.value = _escrowTransactions.value.map {
      if (it.requestId == requestId) it.copy(status = EscrowStatus.FROZEN_FOR_DISPUTE) else it
    }

    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = requestId,
      senderId = "system",
      senderName = "لجنة التحكيم وفض المنازعات بمتر",
      senderRole = UserRole.ADMIN,
      text = "تم تسجيل طلب تدخل لجنة التحكيم بالمنصة لمراجعة التزامات الطرفين والبت في رسم الخدمة.",
      timestamp = "الآن",
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg

    addNotification(
      title = "تم إحالة الطلب للجنة التحكيم",
      body = "تتم مراجعة الشكوى بواسطة المستشار المشرف على لجان التحكيم وسيتم البت خلال 24 ساعة.",
      requestId = requestId
    )
  }

  fun resolveDispute(disputeId: String, decision: DisputeStatus, adminNote: String) {
    val dispute = _disputes.value.find { it.id == disputeId } ?: return
    _disputes.value = _disputes.value.map {
      if (it.id == disputeId) it.copy(status = decision, adminNote = adminNote) else it
    }

    val escrowStatus = when (decision) {
      DisputeStatus.RESOLVED_REFUND -> EscrowStatus.REFUNDED
      DisputeStatus.RESOLVED_RELEASE -> EscrowStatus.RELEASED
      DisputeStatus.MUTUAL_SETTLEMENT -> EscrowStatus.RELEASED
      else -> EscrowStatus.HELD
    }

    _escrowTransactions.value = _escrowTransactions.value.map {
      if (it.requestId == dispute.requestId) it.copy(status = escrowStatus) else it
    }

    _requests.value = _requests.value.map {
      if (it.id == dispute.requestId) it.copy(status = RequestStatus.COMPLETED) else it
    }

    val decisionText = when (decision) {
      DisputeStatus.RESOLVED_REFUND -> "استرداد رسم المنصة للعميل"
      DisputeStatus.RESOLVED_RELEASE -> "اعتماد إنجاز الخدمة"
      DisputeStatus.MUTUAL_SETTLEMENT -> "تسوية ودية بين الطرفين"
      else -> "إجراء تحكيمي"
    }

    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = dispute.requestId,
      senderId = "system",
      senderName = "قرار لجنة التحكيم بمصر",
      senderRole = UserRole.ADMIN,
      text = "قرار التحكيم النهائي: $decisionText. إفادة الإدارة: $adminNote",
      timestamp = "الآن",
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg
  }

  /**
   * Sends a workspace chat message with contact leakage check if pre-contact.
   */
  fun sendMessage(requestId: String, text: String, attachmentName: String? = null): Result<ChatMessage> {
    val req = _requests.value.find { it.id == requestId }
    // If request is still in OPEN or NEGOTIATING and not yet CONTACT_UNLOCKED / IN_PROGRESS
    if (req != null && req.status == RequestStatus.OPEN) {
      val check = ContactLeakageDetectionService.inspectText(text, "pre_contact_chat")
      if (check.isBlocked) {
        recordViolation(check, "محادثة ما قبل فتح الاتصال")
        return Result.failure(IllegalArgumentException(check.userMessage ?: "محتوى غير مسموح به"))
      }
    }

    val user = _currentUser.value
    val newMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = requestId,
      senderId = user.id,
      senderName = user.name,
      senderRole = user.role,
      text = text,
      timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
      attachmentName = attachmentName
    )
    _messages.value = _messages.value + newMsg

    // Sync to Firestore Real-time Collection
    scope.launch {
      try {
        FirestoreChatService.sendMessage(requestId, newMsg)
      } catch (e: Exception) {
        // Handled gracefully in FirestoreChatService
      }
    }

    return Result.success(newMsg)
  }

  private fun recordViolation(result: LeakageCheckResult, fieldName: String) {
    val user = _currentUser.value
    val timestamp = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
    val violation = ContactLeakageViolation(
      id = "violation_${System.currentTimeMillis()}",
      timestamp = timestamp,
      userRole = user.role,
      userName = user.name,
      contextField = fieldName,
      violationType = result.violationType ?: "CONTACT_CIRCUMVENTION_DETECTED",
      redactedSnippet = result.redactedSnippet ?: "****",
      severity = RiskSeverity.HIGH,
      isBlocked = true
    )
    _violations.value = listOf(violation) + _violations.value

    val riskEvent = SecurityRiskEvent(
      id = "sec_${System.currentTimeMillis()}",
      eventType = "CONTACT_LEAKAGE_BLOCKED",
      description = "تم حظر محاولة مشاركة وسيلة اتصال خارجية في ($fieldName) بواسطة (${user.name}).",
      userIdentifier = user.id,
      severity = RiskSeverity.HIGH,
      timestamp = timestamp
    )
    _securityRiskEvents.value = listOf(riskEvent) + _securityRiskEvents.value
  }

  fun submitReview(lawyerId: String, stars: Int, comment: String, tags: List<String>, requestId: String? = null) {
    val user = _currentUser.value
    val newRev = LawyerReview(
      id = "rev_${System.currentTimeMillis()}",
      lawyerId = lawyerId,
      clientName = user.name,
      stars = stars,
      comment = comment,
      tags = tags,
      date = "اليوم"
    )
    _reviews.value = listOf(newRev) + _reviews.value

    // Sync rating & feedback to Firestore
    scope.launch {
      FirestoreRatingService.submitRating(
        lawyerId = lawyerId,
        clientName = user.name,
        stars = stars,
        comment = comment,
        tags = tags,
        requestId = requestId
      )
    }
  }

  fun verifyLawyer(lawyerId: String, approved: Boolean) {
    verifyLawyerWithDetails(
      lawyerId = lawyerId,
      approved = approved,
      assignedDegree = _lawyers.value.find { it.id == lawyerId }?.degree ?: LawyerBarDegree.APPEAL,
      assignedGovernorate = _lawyers.value.find { it.id == lawyerId }?.city ?: "القاهرة",
      assignedCourtScope = _lawyers.value.find { it.id == lawyerId }?.courtJurisdictionScope ?: "محاكم الاستئناف والابتدائية",
      adminNotes = if (approved) "تم اعتماد الوثائق ومطابقتها مع النقابة." else "المستندات غير مكتملة أو غير مطابقة."
    )
  }

  fun verifyLawyerWithDetails(
    lawyerId: String,
    approved: Boolean,
    assignedDegree: LawyerBarDegree,
    assignedGovernorate: String,
    assignedCourtScope: String,
    adminNotes: String?
  ) {
    _lawyers.value = _lawyers.value.map {
      if (it.id == lawyerId) {
        it.copy(
          isVerified = approved,
          verificationStatus = if (approved) VerificationStatus.VERIFIED else VerificationStatus.REJECTED,
          degree = assignedDegree,
          city = assignedGovernorate,
          courtJurisdictionScope = assignedCourtScope,
          adminReviewNotes = adminNotes,
          rejectionReason = if (!approved) adminNotes else null,
          approvedByAdmin = if (approved) "مسؤول النظام والتحكيم" else null
        )
      } else it
    }

    val targetLawyer = _lawyers.value.find { it.id == lawyerId }
    if (targetLawyer != null) {
      if (_currentUser.value.id == lawyerId) {
        val updatedUser = _currentUser.value.copy(
          isVerified = approved,
          pendingVerification = !approved,
          barDegree = assignedDegree,
          assignedGovernorate = assignedGovernorate,
          assignedCourtJurisdiction = assignedCourtScope,
          rejectionReason = if (!approved) adminNotes else null,
          kycStatus = if (approved) KycVerificationStatus.VERIFIED else KycVerificationStatus.REJECTED
        )
        _currentUser.value = updatedUser
        scope.launch { userPrefs?.saveUserSession(updatedUser) }
      }

      addAuditLog(
        category = AuditLogCategory.LAWYER_COMPLIANCE,
        title = if (approved) "اعتماد قيد محامٍ وتحديد صلاحياته" else "رفض ملف قيد محامٍ",
        description = if (approved) "تم اعتماد المحامي (${targetLawyer.name}) بدرجة (${assignedDegree.titleAr}) في اختصاص ($assignedGovernorate: $assignedCourtScope)." else "تم رفض ملف المحامي (${targetLawyer.name}). السبب: ${adminNotes ?: "عدم استيفاء المستندات"}.",
        governorate = assignedGovernorate,
        severity = RiskSeverity.LOW
      )

      addNotification(
        title = if (approved) "تم اعتماد قيدك وتحديد نطاق العمل القضائي ✓" else "إفادة بخصوص طلب القيد بالمنصة",
        body = if (approved) "وافق مسؤول النظام على مستنداتك واعتماد قيدك بدرجة (${assignedDegree.formalTitleAr}) في نطاق ($assignedGovernorate)." else "لم يتم اعتماد مستندات القيد. ملاحظات المسؤول: ${adminNotes ?: "يرجى مراجعة إدارة المنصة"}.",
        requestId = null
      )
    }
  }

  fun requestLawyerVerification(licenseNumber: String) {
    val current = _currentUser.value
    _currentUser.value = current.copy(
      licenseNumber = licenseNumber,
      pendingVerification = true
    )
    addNotification(
      title = "تم استلام وثائق القيد بالنقابة",
      body = "جاري مطابقة رقم القيد ($licenseNumber) مع سجلات نقابة المحامين المصرية وسيتم الاعتماد فور التدقيق.",
      requestId = null
    )
  }

  private fun addNotification(title: String, body: String, requestId: String?) {
    val notif = AppNotification(
      id = "notif_${System.currentTimeMillis()}",
      title = title,
      body = body,
      timestamp = "الآن",
      isRead = false,
      relatedRequestId = requestId
    )
    _notifications.value = listOf(notif) + _notifications.value
  }

  fun getReviewsForLawyer(lawyerId: String): List<LawyerReview> {
    return _reviews.value.filter { it.lawyerId == lawyerId }
  }

  fun simulateLawyerResponse(requestId: String, responseText: String) {
    val lawyerMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = requestId,
      senderId = "lawyer_1",
      senderName = "المستشار د. أحمد عبد العال الشناوي",
      senderRole = UserRole.LAWYER,
      text = responseText,
      timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    )
    _messages.value = _messages.value + lawyerMsg

    // Sync simulated lawyer reply to Firestore as well
    scope.launch {
      try {
        FirestoreChatService.sendMessage(requestId, lawyerMsg)
      } catch (e: Exception) {
        // Handled gracefully
      }
    }
  }

  /**
   * Observe real-time messages from Firestore with local fallback merge
   */
  fun observeCaseMessages(requestId: String): Flow<List<ChatMessage>> {
    return flow {
      // Emit current local messages immediately
      emit(_messages.value.filter { it.requestId == requestId })

      if (FirestoreChatService.isCloudConnected()) {
        FirestoreChatService.observeMessages(requestId).collect { remoteMsgs ->
          if (remoteMsgs.isNotEmpty()) {
            val existingOthers = _messages.value.filter { it.requestId != requestId }
            val existingThisReq = _messages.value.filter { it.requestId == requestId }.associateBy { it.id }
            val merged = (existingThisReq + remoteMsgs.associateBy { it.id }).values.toList()
            _messages.value = existingOthers + merged
            emit(merged)
          }
        }
      }
    }
  }

  /**
   * Updates typing indicator in Firestore
   */
  fun setTypingStatus(requestId: String, isTyping: Boolean) {
    val user = _currentUser.value
    FirestoreChatService.setTyping(requestId, user.id, user.name, isTyping)
  }

  /**
   * Observes who is currently typing in the case
   */
  fun observeTypingUsers(requestId: String): Flow<List<String>> {
    val user = _currentUser.value
    return FirestoreChatService.observeTypingUsers(requestId, user.id)
  }

  fun markNotificationRead(notifId: String) {
    _notifications.value = _notifications.value.map {
      if (it.id == notifId) it.copy(isRead = true) else it
    }
  }

  fun markAllNotificationsRead() {
    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
  }

  fun depositFunds(amount: Double, paymentMethod: String, iban: String? = null): DepositRecord {
    val current = _currentUser.value
    val updatedBalance = current.balance + amount
    val updatedUser = current.copy(balance = updatedBalance)
    _currentUser.value = updatedUser

    val refNumber = "PAY-EGY-${(10000..99999).random()}"
    val record = DepositRecord(
      id = "dep_${System.currentTimeMillis()}",
      amount = amount,
      paymentMethod = paymentMethod,
      referenceNumber = refNumber,
      date = "اليوم",
      status = "سداد رسم خدمة المنصة بنجاح",
      iban = iban
    )
    _deposits.value = listOf(record) + _deposits.value

    addNotification(
      title = "سداد رسم خدمة المنصة بنجاح",
      body = "تم سداد مبلغ ${amount.toInt()} ج.م عبر $paymentMethod بنجاح. الرصيد المتاح: ${updatedBalance.toInt()} ج.م.",
      requestId = null
    )
    scope.launch {
      userPrefs?.saveUserSession(updatedUser)
    }
    return record
  }

  /**
   * طلب سحب رصيد للمحامي عن الطلبات المكتملة التي أنجزها
   */
  fun requestLawyerWithdrawal(
    amount: Double,
    completedRequestId: String,
    bankOrInstapayAccount: String,
    accountType: String
  ): Result<LawyerWithdrawalRequest> {
    val current = _currentUser.value
    if (current.role != UserRole.LAWYER && current.role != UserRole.ADMIN) {
      return Result.failure(IllegalStateException("طلب سحب الأتعاب مخصص للمحامين فقط."))
    }
    if (amount <= 0) {
      return Result.failure(IllegalArgumentException("يجب أن يكون مبلغ السحب أكبر من الصفر."))
    }
    if (amount > current.balance) {
      return Result.failure(IllegalArgumentException("المبلغ المطلوب (${amount.toInt()} ج.م) يتجاوز رصيدك المتاح (${current.balance.toInt()} ج.م)."))
    }
    if (bankOrInstapayAccount.isBlank()) {
      return Result.failure(IllegalArgumentException("يرجى إدخال الحساب البنكي أو عنوان إنستاباي بدقة."))
    }

    val completedReq = _requests.value.find { it.id == completedRequestId }
    val reqTitle = completedReq?.title ?: "طلب قضائي مكتمل تم تحرير أتعابه"

    // Deduct immediately from lawyer's available balance and hold pending admin approval
    val updatedUser = current.copy(balance = (current.balance - amount).coerceAtLeast(0.0))
    _currentUser.value = updatedUser
    scope.launch { userPrefs?.saveUserSession(updatedUser) }

    val withdrawal = LawyerWithdrawalRequest(
      id = "wdr_${System.currentTimeMillis()}",
      lawyerId = current.id,
      lawyerName = current.name,
      lawyerPhone = current.phone,
      amount = amount,
      completedRequestId = completedRequestId,
      completedRequestTitle = reqTitle,
      bankOrInstapayAccount = bankOrInstapayAccount,
      accountType = accountType,
      status = WithdrawalStatus.PENDING,
      requestDate = "اليوم",
      adminApprovalDate = null,
      adminNote = null
    )

    _withdrawalRequests.value = listOf(withdrawal) + _withdrawalRequests.value

    addNotification(
      title = "تم تقديم طلب سحب رصيد",
      body = "تم إرسال طلب سحب مبلغ ${amount.toInt()} ج.م إلى مدير النظام للمراجعة والتحويل لحسابك: $bankOrInstapayAccount.",
      requestId = completedRequestId
    )

    return Result.success(withdrawal)
  }

  /**
   * اعتماد مدير النظام لطلب سحب رصيد المحامي
   */
  fun approveLawyerWithdrawal(requestId: String, adminNote: String? = null) {
    val dateNow = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
    _withdrawalRequests.value = _withdrawalRequests.value.map { w ->
      if (w.id == requestId) {
        w.copy(
          status = WithdrawalStatus.APPROVED,
          adminApprovalDate = dateNow,
          adminNote = adminNote ?: "تم التحويل البنكي واعتماد الصرف بنجاح من مدير النظام."
        )
      } else w
    }

    val targetReq = _withdrawalRequests.value.find { it.id == requestId }
    addNotification(
      title = "تم اعتماد سحب الأتعاب من مدير النظام ✓",
      body = "وافق مدير النظام على تحويل أتعابك البالغة ${targetReq?.amount?.toInt() ?: 0} ج.م إلى حسابك البنكي/إنستاباي (${targetReq?.bankOrInstapayAccount ?: ""}).",
      requestId = targetReq?.completedRequestId
    )
  }

  /**
   * رفض مدير النظام لطلب سحب رصيد المحامي (وإعادة الرصيد لمحفظة المحامي)
   */
  fun rejectLawyerWithdrawal(requestId: String, rejectionReason: String) {
    val targetReq = _withdrawalRequests.value.find { it.id == requestId }
    _withdrawalRequests.value = _withdrawalRequests.value.map { w ->
      if (w.id == requestId) {
        w.copy(
          status = WithdrawalStatus.REJECTED,
          adminNote = rejectionReason
        )
      } else w
    }

    // Refund back to lawyer balance if rejected
    if (targetReq != null) {
      if (_currentUser.value.id == targetReq.lawyerId) {
        val refundedUser = _currentUser.value.copy(balance = _currentUser.value.balance + targetReq.amount)
        _currentUser.value = refundedUser
        scope.launch { userPrefs?.saveUserSession(refundedUser) }
      }
      addNotification(
        title = "تنبيه بخصوص طلب سحب الرصيد",
        body = "تم رفض طلب سحب ${targetReq.amount.toInt()} ج.م وإعادة الرصيد لمحفظتك. السبب: $rejectionReason",
        requestId = targetReq.completedRequestId
      )
    }
  }

  /**
   * توليد كود خصم جديد بواسطة مدير النظام لكامل المنصة أو لفئة محددة
   */
  fun createDiscountCoupon(
    code: String,
    title: String,
    description: String,
    discountType: DiscountType,
    discountValue: Double,
    audience: CouponAudience,
    minAmount: Double = 0.0,
    maxDiscountAmount: Double = 1000.0,
    maxUsageLimit: Int = 100,
    expiryDate: String = "31 ديسمبر 2026"
  ): Result<DiscountCoupon> {
    val cleanCode = code.trim().uppercase()
    if (cleanCode.isBlank()) {
      return Result.failure(IllegalArgumentException("يرجى إدخال رمز الكوبون بشكل صحيح."))
    }
    if (_coupons.value.any { it.code.equals(cleanCode, ignoreCase = true) }) {
      return Result.failure(IllegalArgumentException("رمز الكوبون ($cleanCode) موجود بالفعل، يرجى اختيار رمز آخر."))
    }
    if (discountValue <= 0) {
      return Result.failure(IllegalArgumentException("يجب أن تكون قيمة الخصم أكبر من الصفر."))
    }

    val newCoupon = DiscountCoupon(
      id = "cpn_${System.currentTimeMillis()}",
      code = cleanCode,
      title = title.ifBlank { "كود خصم المنصة: $cleanCode" },
      description = description.ifBlank { "خصم رسمي معتمد من إدارة منصة مِتر" },
      discountType = discountType,
      discountValue = discountValue,
      audience = audience,
      minAmount = minAmount,
      maxDiscountAmount = maxDiscountAmount,
      isActive = true,
      usageCount = 0,
      maxUsageLimit = maxUsageLimit,
      expiryDate = expiryDate,
      createdBy = "مدير النظام"
    )

    _coupons.value = listOf(newCoupon) + _coupons.value

    addNotification(
      title = "تم إصدار كود خصم جديد: $cleanCode",
      body = "أصدرت إدارة المنصة كود خصم جديد (${newCoupon.title}) مخصص لـ: ${audience.labelAr}.",
      requestId = null
    )

    return Result.success(newCoupon)
  }

  /**
   * تفعيل أو تعطيل كود خصم
   */
  fun toggleCouponStatus(couponId: String) {
    _coupons.value = _coupons.value.map { c ->
      if (c.id == couponId) c.copy(isActive = !c.isActive) else c
    }
  }

  /**
   * التحقق من صلاحية كود الخصم وتطبيقه
   */
  fun validateAndApplyCoupon(code: String, amount: Double, userRole: UserRole): Result<Pair<DiscountCoupon, Double>> {
    val cleanCode = code.trim().uppercase()
    val coupon = _coupons.value.find { it.code.equals(cleanCode, ignoreCase = true) }
      ?: return Result.failure(IllegalArgumentException("رمز الكوبون غير صحيح أو غير موجود."))

    if (!coupon.isActive) {
      return Result.failure(IllegalArgumentException("هذا الكود غير مفعّل حالياً."))
    }

    if (coupon.usageCount >= coupon.maxUsageLimit) {
      return Result.failure(IllegalArgumentException("تم استنفاد الحد الأقصى لاستخدام هذا الكود."))
    }

    // Check Audience Permission
    when (coupon.audience) {
      CouponAudience.ALL -> { /* Allowed for everyone */ }
      CouponAudience.CLIENTS -> {
        if (userRole == UserRole.LAWYER) {
          return Result.failure(IllegalArgumentException("هذا الكوبون مخصص للمستخدم النهائي (العملاء) فقط."))
        }
      }
      CouponAudience.LAWYERS -> {
        if (userRole == UserRole.CLIENT) {
          return Result.failure(IllegalArgumentException("هذا الكوبون مخصص للسادة المحامين فقط."))
        }
      }
    }

    if (amount < coupon.minAmount) {
      return Result.failure(IllegalArgumentException("الحد الأدنى لتطبيق هذا الكوبون هو ${coupon.minAmount.toInt()} ج.م."))
    }

    val calculatedDiscount = when (coupon.discountType) {
      DiscountType.PERCENTAGE -> {
        val rawDiscount = (amount * (coupon.discountValue / 100.0))
        rawDiscount.coerceAtMost(coupon.maxDiscountAmount)
      }
      DiscountType.FIXED_AMOUNT -> {
        coupon.discountValue.coerceAtMost(amount)
      }
    }

    // Increment usage
    _coupons.value = _coupons.value.map {
      if (it.id == coupon.id) it.copy(usageCount = it.usageCount + 1) else it
    }

    return Result.success(Pair(coupon, calculatedDiscount))
  }

  /**
   * سحب أرباح ورسوم المنصة المحصلة من الخزينة لحساب الشركة البنكي / إنستاباي
   */
  fun withdrawPlatformBalance(
    amount: Double,
    destinationAccount: String,
    destinationType: String,
    note: String
  ): Result<PlatformWithdrawalRecord> {
    if (amount <= 0) {
      return Result.failure(IllegalArgumentException("يجب أن يكون مبلغ سحب أرباح المنصة أكبر من الصفر."))
    }
    val totalRevenue = _escrowTransactions.value.sumOf { it.platformFee }
    val totalWithdrawn = _platformWithdrawals.value.sumOf { it.amount }
    val availableRevenue = (totalRevenue - totalWithdrawn).coerceAtLeast(0.0)

    if (amount > availableRevenue && _currentUser.value.role != UserRole.ADMIN) {
      return Result.failure(IllegalArgumentException("المبلغ المطلوب (${amount.toInt()} ج.م) يتجاوز رصيد أرباح المنصة المتاح حالياً (${availableRevenue.toInt()} ج.م)."))
    }
    if (destinationAccount.isBlank()) {
      return Result.failure(IllegalArgumentException("يرجى تحديد الحساب البنكي أو حساب إنستاباي لتحويل الأرباح."))
    }

    val timestamp = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
    val refNo = "CORP-EGY-${(10000..99999).random()}"
    val record = PlatformWithdrawalRecord(
      id = "pwd_${System.currentTimeMillis()}",
      amount = amount,
      destinationAccount = destinationAccount,
      destinationType = destinationType,
      date = "اليوم ($timestamp)",
      referenceNumber = refNo,
      note = note.ifBlank { "سحب أرباح ورسوم منصة مِتر المحصلة" },
      status = "تم التحويل بنجاح من حساب المنصة"
    )

    _platformWithdrawals.value = listOf(record) + _platformWithdrawals.value

    // Deduct from admin balance display if applicable
    if (_currentUser.value.role == UserRole.ADMIN) {
      val newAdminBal = (_currentUser.value.balance - amount).coerceAtLeast(0.0)
      val updatedAdmin = _currentUser.value.copy(balance = newAdminBal)
      _currentUser.value = updatedAdmin
      scope.launch { userPrefs?.saveUserSession(updatedAdmin) }
    }

    // Add Audit Log
    addAuditLog(
      category = AuditLogCategory.WITHDRAWAL_OPERATION,
      title = "سحب أرباح المنصة للخزينة",
      description = "تم تحويل مبلغ ${amount.toInt()} ج.م من أرباح ورسوم المنصة إلى $destinationType ($destinationAccount) برقم مرجعي $refNo.",
      governorate = "القاهرة",
      severity = RiskSeverity.LOW
    )

    addNotification(
      title = "تم تحويل أرباح المنصة بنجاح",
      body = "تم سحب مبلغ ${amount.toInt()} ج.م من رصيد رسوم المنصة إلى $destinationType ($refNo).",
      requestId = null
    )

    return Result.success(record)
  }

  /**
   * إصدار قرار رقابي / تدخل إداري لمتابعة الطلبات المفتوحة وقيد التنفيذ
   */
  fun issueSupervisoryDecision(
    requestId: String,
    decisionType: SupervisoryDecisionType,
    notes: String,
    clientRefundPercentage: Double = 0.0,
    lawyerFeePercentage: Double = 0.0
  ): Result<SupervisoryDecision> {
    val req = _requests.value.find { it.id == requestId }
      ?: return Result.failure(IllegalArgumentException("الطلب غير موجود في قاعدة البيانات."))

    val targetLawyer = if (req.acceptedBidId != null) {
      val bid = _bids.value.find { it.id == req.acceptedBidId }
      bid?.lawyerName
    } else null

    val timestamp = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
    val decision = SupervisoryDecision(
      id = "sd_${System.currentTimeMillis()}",
      requestId = req.id,
      requestTitle = req.title,
      clientName = req.clientName,
      lawyerName = targetLawyer,
      decisionType = decisionType,
      adminNote = notes.ifBlank { "قرار رقابي معتمد من إدارة الرقابة والتحكيم" },
      clientRefundPercentage = clientRefundPercentage,
      lawyerFeePercentage = lawyerFeePercentage,
      issuedAt = timestamp
    )

    _supervisoryDecisions.value = listOf(decision) + _supervisoryDecisions.value

    // Apply side effects on request based on decision
    when (decisionType) {
      SupervisoryDecisionType.ADMINISTRATIVE_CANCEL_REFUND -> {
        _requests.value = _requests.value.map {
          if (it.id == req.id) it.copy(status = RequestStatus.COMPLETED) else it
        }
        _escrowTransactions.value = _escrowTransactions.value.map {
          if (it.requestId == req.id) it.copy(status = EscrowStatus.REFUNDED) else it
        }
      }
      SupervisoryDecisionType.PRECAUTIONARY_FREEZE -> {
        _escrowTransactions.value = _escrowTransactions.value.map {
          if (it.requestId == req.id) it.copy(status = EscrowStatus.FROZEN_FOR_DISPUTE) else it
        }
      }
      SupervisoryDecisionType.DEADLINE_EXTENSION,
      SupervisoryDecisionType.SUPERVISORY_WARNING,
      SupervisoryDecisionType.ENFORCE_EXECUTION,
      SupervisoryDecisionType.PRIORITY_ESCALATION -> {
        // Log & notification
      }
    }

    // Insert System Chat Message in case workspace
    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = req.id,
      senderId = "system_supervisory",
      senderName = "هيئة الرقابة والتحكيم بجمهورية مصر العربية",
      senderRole = UserRole.ADMIN,
      text = "⚖️ [قرار وتوجيه رقابي نافذ]: ${decisionType.titleAr}\nالإفادة الرقابية: ${decision.adminNote}\nصدر بتاريخ: $timestamp",
      timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg

    // Add Audit Log
    addAuditLog(
      category = AuditLogCategory.ARBITRATION_DECISION,
      title = "إصدار قرار رقابي: ${decisionType.titleAr}",
      description = "قرار إداري للقضية (${req.title}) - ${req.city} - الإفادة: ${decision.adminNote}",
      governorate = req.city,
      severity = if (decisionType == SupervisoryDecisionType.PRECAUTIONARY_FREEZE || decisionType == SupervisoryDecisionType.ADMINISTRATIVE_CANCEL_REFUND) RiskSeverity.HIGH else RiskSeverity.MEDIUM
    )

    addNotification(
      title = "قرار رقابي رسمي: ${decisionType.titleAr}",
      body = "أصدرت هيئة الرقابة قراراً في الطلب (${req.title}): ${decision.adminNote}",
      requestId = req.id
    )

    return Result.success(decision)
  }

  /**
   * فصل النزاع بقرار تحكيمي مرن (مع خيار قسمة رضائية بنسب مئوية مخصصة)
   */
  fun resolveDisputeWithDecision(
    disputeId: String,
    decision: DisputeStatus,
    clientRefundPct: Double,
    lawyerFeePct: Double,
    adminNote: String
  ) {
    val dispute = _disputes.value.find { it.id == disputeId } ?: return

    _disputes.value = _disputes.value.map {
      if (it.id == disputeId) {
        it.copy(
          status = decision,
          adminNote = adminNote
        )
      } else it
    }

    val escStatus = when (decision) {
      DisputeStatus.RESOLVED_REFUND -> EscrowStatus.REFUNDED
      DisputeStatus.RESOLVED_RELEASE -> EscrowStatus.RELEASED
      DisputeStatus.MUTUAL_SETTLEMENT -> EscrowStatus.RELEASED
      else -> EscrowStatus.RELEASED
    }

    _escrowTransactions.value = _escrowTransactions.value.map {
      if (it.requestId == dispute.requestId) it.copy(status = escStatus) else it
    }

    _requests.value = _requests.value.map {
      if (it.id == dispute.requestId) it.copy(status = RequestStatus.COMPLETED) else it
    }

    val sysMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      requestId = dispute.requestId,
      senderId = "system_arbitration",
      senderName = "قرار هيئة التحكيم القضائي",
      senderRole = UserRole.ADMIN,
      text = "⚖️ [قرار التحكيم النهائي]: ${decision.labelAr}\nنسبة استرداد الموكل: ${clientRefundPct.toInt()}%\nنسبة مستحقات المحامي: ${lawyerFeePct.toInt()}%\nأسباب ومنطوق القرار: $adminNote",
      timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
      isSystemMessage = true
    )
    _messages.value = _messages.value + sysMsg

    addAuditLog(
      category = AuditLogCategory.ARBITRATION_DECISION,
      title = "حكم تحكيمي نهائي في نزاع",
      description = "تم الفصل في النزاع (${dispute.reason}) لطلب (${dispute.requestId}) بقرار: ${decision.labelAr}.",
      governorate = "القاهرة",
      severity = RiskSeverity.MEDIUM
    )

    addNotification(
      title = "صدور قرار التحكيم النهائي",
      body = "أصدرت لجنة التحكيم قرارها النهائي في النزاع: ${decision.labelAr}.",
      requestId = dispute.requestId
    )
  }

  fun addAuditLog(
    category: AuditLogCategory,
    title: String,
    description: String,
    governorate: String,
    severity: RiskSeverity = RiskSeverity.LOW
  ) {
    val user = _currentUser.value
    val newLog = RealtimeAuditLog(
      id = "log_${System.currentTimeMillis()}",
      category = category,
      title = title,
      description = description,
      governorate = governorate.ifBlank { "جمهورية مصر العربية" },
      actorRole = user.role,
      actorName = user.name,
      severity = severity,
      timestamp = "الآن"
    )
    _realtimeAuditLogs.value = listOf(newLog) + _realtimeAuditLogs.value
  }

  /**
   * توليد تقرير رقابي فوري وشامل لمتابعة أداء ونزاهة المنصة
   */
  fun generateInstantSupervisoryReport(
    governorateFilter: String? = null,
    categoryFilter: RequestCategory? = null
  ): SupervisoryReportData {
    val reqs = _requests.value.filter { r ->
      (governorateFilter == null || r.city.equals(governorateFilter, ignoreCase = true)) &&
      (categoryFilter == null || r.category == categoryFilter)
    }

    val totalRevenue = _escrowTransactions.value.sumOf { it.platformFee }
    val totalWithdrawn = _platformWithdrawals.value.sumOf { it.amount }
    val availableRevenue = (totalRevenue - totalWithdrawn).coerceAtLeast(0.0)
    val totalActiveEscrow = _escrowTransactions.value.filter { it.status == EscrowStatus.HELD }.sumOf { it.totalAmount }
    val totalReleasedFees = _escrowTransactions.value.filter { it.status == EscrowStatus.RELEASED }.sumOf { it.totalAmount }

    val disputesCount = _disputes.value.size
    val disputesResolved = _disputes.value.count { it.status != DisputeStatus.UNDER_REVIEW }
    val totalViolations = _violations.value.size

    val timestamp = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())

    return SupervisoryReportData(
      reportId = "REP-EGY-${System.currentTimeMillis() % 100000}",
      generatedAt = timestamp,
      activeGovernoratesCount = 27,
      totalOpenRequests = reqs.count { it.status == RequestStatus.OPEN },
      totalActiveEscrowEgp = totalActiveEscrow,
      totalReleasedFeesEgp = totalReleasedFees,
      platformTotalRevenueEgp = totalRevenue,
      totalPlatformWithdrawalsEgp = totalWithdrawn,
      availablePlatformBalanceEgp = availableRevenue,
      leakageViolationsBlocked = totalViolations,
      disputesCount = disputesCount,
      disputesResolvedCount = disputesResolved,
      integrityComplianceRate = 99.4,
      recentLogs = _realtimeAuditLogs.value.take(10)
    )
  }

  fun login(phone: String, role: UserRole) {
    val updatedUser = when (role) {
      UserRole.LAWYER -> UserProfile(
        id = "lawyer_1",
        name = "المستشار د. أحمد عبد العال الشناوي",
        email = "dr.elshinnawy@law.eg",
        phone = phone.ifEmpty { "+20 111 987 6543" },
        role = UserRole.LAWYER,
        balance = 8200.0,
        licenseNumber = "قيد استئناف ونقض: 431908",
        isVerified = true,
        isLoggedIn = true,
        nationalIdOrCr = "28504120109482",
        nafathVerified = true,
        officeAddress = "القاهرة - مصر الجديدة",
        assignedGovernorate = "القاهرة",
        assignedDistrict = "مصر الجديدة",
        assignedCourtJurisdiction = "نيابة مصر الجديدة الجزئية • محكمة مصر الجديدة الابتدائية"
      )
      UserRole.ADMIN -> UserProfile(
        id = "user_admin_1",
        name = "الاستشاري د. محيي الدين (مشرف المنصة والتحكيم)",
        email = "admin@maitre.eg",
        phone = phone.ifEmpty { "+20 120 000 0000" },
        role = UserRole.ADMIN,
        balance = 54800.0,
        isVerified = true,
        isLoggedIn = true,
        nationalIdOrCr = "10023450912384",
        nafathVerified = true,
        officeAddress = "القاهرة - وسط البلد - ميدان طلعت حرب"
      )
      UserRole.CLIENT -> UserProfile(
        id = "user_client_1",
        name = "م. شريف عبد الفتاح التميمي",
        email = "sherif.tamimi@example.com",
        phone = phone.ifEmpty { "+20 100 123 4567" },
        role = UserRole.CLIENT,
        balance = 4500.0,
        isVerified = true,
        isLoggedIn = true,
        nationalIdOrCr = "29408151203948",
        nafathVerified = true,
        officeAddress = "القاهرة - التجمع الخامس"
      )
    }
    _currentUser.value = updatedUser
    addNotification(
      title = "تسجيل دخول ناجح",
      body = "مرحباً بك مجدداً في منصة مِتر القانونية بمصر (${when (role) {
        UserRole.CLIENT -> "نظام العميل"
        UserRole.LAWYER -> "نظام المحامي"
        UserRole.ADMIN -> "نظام المشرف والتحكيم"
      }}).",
      requestId = null
    )
    scope.launch {
      userPrefs?.saveUserSession(updatedUser)
    }
  }

  fun loginWithGoogle(
    googleEmail: String,
    googleDisplayName: String,
    role: UserRole
  ) {
    val updatedUser = when (role) {
      UserRole.LAWYER -> UserProfile(
        id = "lawyer_google_${System.currentTimeMillis() % 10000}",
        name = googleDisplayName.ifBlank { "المستشار د. أحمد عبد العال الشناوي" },
        email = googleEmail,
        phone = "+20 111 987 6543",
        role = UserRole.LAWYER,
        balance = 8200.0,
        licenseNumber = "قيد استئناف ونقض: 431908",
        isVerified = true,
        isLoggedIn = true,
        nationalIdOrCr = "28504120109482",
        nafathVerified = true,
        officeAddress = "القاهرة - مصر الجديدة",
        assignedGovernorate = "القاهرة",
        assignedDistrict = "مصر الجديدة",
        assignedCourtJurisdiction = "نيابة مصر الجديدة الجزئية • محكمة مصر الجديدة الابتدائية"
      )
      UserRole.ADMIN -> UserProfile(
        id = "admin_google_${System.currentTimeMillis() % 10000}",
        name = googleDisplayName.ifBlank { "الاستشاري د. محيي الدين (مشرف المنصة والتحكيم)" },
        email = googleEmail,
        phone = "+20 120 000 0000",
        role = UserRole.ADMIN,
        balance = 54800.0,
        isVerified = true,
        isLoggedIn = true,
        nationalIdOrCr = "10023450912384",
        nafathVerified = true,
        officeAddress = "القاهرة - وسط البلد - ميدان طلعت حرب"
      )
      UserRole.CLIENT -> UserProfile(
        id = "client_google_${System.currentTimeMillis() % 10000}",
        name = googleDisplayName.ifBlank { "م. شريف عبد الفتاح التميمي" },
        email = googleEmail,
        phone = "+20 100 123 4567",
        role = UserRole.CLIENT,
        balance = 4500.0,
        isVerified = true,
        isLoggedIn = true,
        nationalIdOrCr = "29408151203948",
        nafathVerified = true,
        officeAddress = "القاهرة - التجمع الخامس"
      )
    }
    _currentUser.value = updatedUser
    addNotification(
      title = "تسجيل الدخول بحساب Google ✓",
      body = "تمت المصادقة وتسجيل الدخول بنجاح عبر حساب Google ($googleEmail). مرحباً بك في مِتر!",
      requestId = null
    )
    scope.launch {
      userPrefs?.saveUserSession(updatedUser)
    }
  }

  fun registerClientWithKyc(
    name: String,
    phone: String,
    email: String,
    nationalId: String,
    clientType: ClientType,
    companyName: String?,
    governorate: String,
    idFrontUploaded: Boolean,
    idBackUploaded: Boolean,
    idFrontUri: String? = "id_card_front.jpg",
    idBackUri: String? = "id_card_back.jpg"
  ) {
    val newClientId = "client_${System.currentTimeMillis()}"
    val updatedUser = UserProfile(
      id = newClientId,
      name = name,
      email = email,
      phone = phone,
      role = UserRole.CLIENT,
      balance = 0.0,
      nationalIdOrCr = nationalId,
      clientType = clientType,
      companyName = companyName,
      isVerified = false,
      pendingVerification = true,
      isLoggedIn = true,
      nafathVerified = false,
      kycStatus = KycVerificationStatus.PENDING_REVIEW,
      assignedGovernorate = governorate,
      nationalIdFrontUploaded = idFrontUploaded,
      nationalIdBackUploaded = idBackUploaded,
      nationalIdCardFrontUri = idFrontUri,
      nationalIdCardBackUri = idBackUri,
      officeAddress = "$governorate - جمهورية مصر العربية"
    )
    _currentUser.value = updatedUser

    val newClientReg = ClientRegistration(
      id = newClientId,
      name = name,
      phone = phone,
      email = email,
      nationalId = nationalId,
      clientType = clientType,
      companyName = companyName,
      governorate = governorate,
      idCardFrontUri = idFrontUri,
      idCardBackUri = idBackUri,
      status = VerificationStatus.PENDING,
      registrationDate = "اليوم"
    )
    _clientRegistrations.value = listOf(newClientReg) + _clientRegistrations.value

    addNotification(
      title = "تم استلام ملف التوثيق الرقمي (KYC) بنجاح ✓",
      body = "أهلاً بك يا $name. تم إرسال بطاقة الرقم القومي (وش وظهر) وبيانات التسجيل لمسؤول النظام للمراجعة والاعتماد.",
      requestId = null
    )
    addAuditLog(
      category = AuditLogCategory.REQUEST_LIFECYCLE,
      title = "طلب تسجيل وتوثيق موكل جديد عبر KYC",
      description = "سجل الموكل ($name) بالرقم القومي ($nationalId) في محافظة $governorate بانتظار فحص المشرف.",
      governorate = governorate,
      severity = RiskSeverity.LOW
    )
    scope.launch {
      userPrefs?.saveUserSession(updatedUser)
    }
  }

  fun verifyClientRegistration(
    clientId: String,
    approved: Boolean,
    rejectionReason: String? = null,
    adminNotes: String? = null
  ) {
    _clientRegistrations.value = _clientRegistrations.value.map { reg ->
      if (reg.id == clientId) {
        reg.copy(
          status = if (approved) VerificationStatus.VERIFIED else VerificationStatus.REJECTED,
          rejectionReason = if (!approved) rejectionReason else null,
          adminReviewNotes = adminNotes ?: if (approved) "تم مطابقة الرقم القومي وتوثيق الهوية بنجاح" else "مستندات غير مطابقة"
        )
      } else reg
    }
    val targetClient = _clientRegistrations.value.find { it.id == clientId }
    if (targetClient != null) {
      if (_currentUser.value.id == clientId || _currentUser.value.nationalIdOrCr == targetClient.nationalId || _currentUser.value.name == targetClient.name) {
        _currentUser.value = _currentUser.value.copy(
          isVerified = approved,
          pendingVerification = false,
          nafathVerified = approved,
          kycStatus = if (approved) KycVerificationStatus.VERIFIED else KycVerificationStatus.REJECTED,
          rejectionReason = if (!approved) rejectionReason else null
        )
        scope.launch {
          userPrefs?.saveUserSession(_currentUser.value)
        }
      }
    }
    addNotification(
      title = if (approved) "تم اعتماد وتوثيق حسابك رسمياً ✓" else "تنبيه: تم رفض طلب توثيق الهوية",
      body = if (approved) "تهانينا! اعتمد مشرف المنصة بطاقة الرقم القومي وبيانات التسجيل الخاصة بك بنجاح."
             else "تم رفض طلب التوثيق من قبل المشرف. السبب: ${rejectionReason ?: "المستندات غير واضحة"}",
      requestId = null
    )
    addAuditLog(
      category = AuditLogCategory.REQUEST_LIFECYCLE,
      title = if (approved) "اعتماد وتوثيق موكل جديد" else "رفض طلب توثيق موكل",
      description = if (approved) "اعتمد المشرف طلب توثيق الموكل (${targetClient?.name}) بعد فحص مستندات الرقم القومي وش وظهر."
                    else "رفض المشرف طلب توثيق الموكل (${targetClient?.name}). السبب: $rejectionReason",
      governorate = targetClient?.governorate ?: "القاهرة",
      severity = if (approved) RiskSeverity.LOW else RiskSeverity.MEDIUM
    )
  }

  fun registerLawyerWithKyc(
    name: String,
    phone: String,
    email: String,
    nationalId: String,
    licenseNumber: String,
    proposedDegree: LawyerBarDegree,
    subBarAssociation: String,
    governorate: String,
    courtJurisdictionScope: String,
    specialization: RequestCategory,
    yearsExperience: Int,
    firmName: String,
    barCardUploaded: Boolean,
    idCardUploaded: Boolean,
    barCardBackUploaded: Boolean = true,
    idCardBackUploaded: Boolean = true,
    idCardFrontUri: String? = "id_card_front.jpg",
    idCardBackUri: String? = "id_card_back.jpg",
    barCardFrontUri: String? = "bar_card_front.jpg",
    barCardBackUri: String? = "bar_card_back.jpg",
    desiredPracticeDegrees: List<String> = listOf("محاكم الاستئناف العالي ومجلس الدولة", "محاكم ابتدائية وجنح مستأنفة"),
    selectedGovernorates: List<String> = listOf(governorate),
    selectedCourts: List<String> = listOf(courtJurisdictionScope),
    selectedDistricts: List<String> = emptyList(),
    lawyerTitle: LawyerTitle = LawyerTitle.COUNSELOR,
    bio: String = "",
    officeAddressManually: String = "",
    officeLatitude: Double? = 30.0444,
    officeLongitude: Double? = 31.2357
  ) {
    val newLawyerId = "lawyer_${System.currentTimeMillis()}"
    val effectiveBio = bio.ifBlank {
      "${lawyerTitle.labelAr} $name - محامٍ مقيد بنقابة المحامين الفرعية بـ$subBarAssociation. متخصص في ${specialization.titleAr}. مقر المكتب: $firmName."
    }
    val effectiveManualAddress = officeAddressManually.ifBlank {
      "$governorate - $firmName"
    }

    val updatedUser = UserProfile(
      id = newLawyerId,
      name = name,
      email = email,
      phone = phone,
      role = UserRole.LAWYER,
      balance = 0.0,
      licenseNumber = licenseNumber,
      barDegree = proposedDegree,
      isVerified = false,
      pendingVerification = true,
      isLoggedIn = true,
      nationalIdOrCr = nationalId,
      companyName = firmName,
      nafathVerified = false,
      kycStatus = KycVerificationStatus.PENDING_REVIEW,
      assignedGovernorate = governorate,
      assignedCourtJurisdiction = courtJurisdictionScope,
      subBarAssociation = subBarAssociation,
      desiredPracticeDegrees = desiredPracticeDegrees.ifEmpty {
        listOf(proposedDegree.formalTitleAr)
      },
      selectedGovernorates = selectedGovernorates.ifEmpty { listOf(governorate) },
      selectedCourts = selectedCourts.ifEmpty { listOf(courtJurisdictionScope) },
      selectedDistricts = selectedDistricts,
      nationalIdFrontUploaded = idCardUploaded,
      nationalIdBackUploaded = idCardBackUploaded,
      barCardUploaded = barCardUploaded,
      barCardBackUploaded = barCardBackUploaded,
      nationalIdCardFrontUri = idCardFrontUri,
      nationalIdCardBackUri = idCardBackUri,
      barCardFrontUri = barCardFrontUri,
      barCardBackUri = barCardBackUri,
      officeAddress = effectiveManualAddress,
      bio = effectiveBio,
      officeAddressManually = effectiveManualAddress,
      officeLatitude = officeLatitude,
      officeLongitude = officeLongitude,
      lawyerTitle = lawyerTitle
    )
    _currentUser.value = updatedUser

    val newLawyer = Lawyer(
      id = newLawyerId,
      name = name,
      title = lawyerTitle,
      specialization = specialization,
      degree = proposedDegree,
      city = governorate,
      subBarAssociation = subBarAssociation,
      courtJurisdictionScope = courtJurisdictionScope,
      licenseNumber = licenseNumber,
      nationalIdNumber = nationalId,
      phone = phone,
      email = email,
      isVerified = false,
      verificationStatus = VerificationStatus.PENDING,
      rating = 5.0,
      reviewsCount = 0,
      yearsExperience = yearsExperience,
      bio = effectiveBio,
      officeAddressManually = effectiveManualAddress,
      officeLatitude = officeLatitude,
      officeLongitude = officeLongitude,
      consultationFee = 1000.0,
      nationalIdCardFrontUri = idCardFrontUri,
      nationalIdCardBackUri = idCardBackUri,
      barCardFrontUri = barCardFrontUri,
      barCardBackUri = barCardBackUri,
      registrationDate = "اليوم"
    )
    _lawyers.value = listOf(newLawyer) + _lawyers.value

    addNotification(
      title = "تم استلام ملف المحامي ومستندات KYC ⚖️",
      body = "تم إرسال مستنداتك ورقم القيد ($licenseNumber) لمسؤول النظام للمراجعة والاعتماد وتحديد الدرجة ونطاق العمل القضائي.",
      requestId = null
    )
    addAuditLog(
      category = AuditLogCategory.LAWYER_COMPLIANCE,
      title = "طلب تسجيل واعتماد محامٍ جديد",
      description = "سجل الأستاذ ($name) برقم قيد نقابة ($licenseNumber) بالنقابة الفرعية بـ($subBarAssociation) وبانتظار اعتماد مسؤول النظام.",
      governorate = governorate,
      severity = RiskSeverity.MEDIUM
    )
    scope.launch {
      userPrefs?.saveUserSession(updatedUser)
    }
  }

  fun updateLawyerJudicialScope(
    subBarAssociation: String,
    desiredPracticeDegrees: List<String>,
    selectedGovernorates: List<String>,
    selectedCourts: List<String>,
    selectedDistricts: List<String>,
    courtScopeSummary: String
  ) {
    val updated = _currentUser.value.copy(
      subBarAssociation = subBarAssociation,
      desiredPracticeDegrees = desiredPracticeDegrees,
      selectedGovernorates = selectedGovernorates,
      selectedCourts = selectedCourts,
      selectedDistricts = selectedDistricts,
      assignedCourtJurisdiction = courtScopeSummary
    )
    _currentUser.value = updated
    addNotification(
      title = "تم تحديث نطاق العمل القضائي بنجاح ✓",
      body = "تم تحديث النقابة الفرعية ودرجات العمل المرغوبة ومقرات المحاكم والمحافظات المحددة.",
      requestId = null
    )
    scope.launch {
      userPrefs?.saveUserSession(updated)
    }
  }

  fun rejectLawyerRegistration(
    lawyerId: String,
    reason: String
  ) {
    verifyLawyerWithDetails(
      lawyerId = lawyerId,
      approved = false,
      assignedDegree = _lawyers.value.find { it.id == lawyerId }?.degree ?: LawyerBarDegree.APPEAL,
      assignedGovernorate = _lawyers.value.find { it.id == lawyerId }?.city ?: "القاهرة",
      assignedCourtScope = _lawyers.value.find { it.id == lawyerId }?.courtJurisdictionScope ?: "محاكم الاستئناف",
      adminNotes = reason
    )
  }

  fun register(
    name: String,
    phone: String,
    email: String,
    role: UserRole,
    nationalIdOrCr: String,
    licenseNumber: String?,
    companyName: String?
  ) {
    if (role == UserRole.LAWYER) {
      registerLawyerWithKyc(
        name = name,
        phone = phone,
        email = email,
        nationalId = nationalIdOrCr,
        licenseNumber = licenseNumber ?: "430000",
        proposedDegree = LawyerBarDegree.APPEAL,
        subBarAssociation = "نقابة المحامين الفرعية",
        governorate = "القاهرة",
        courtJurisdictionScope = "محاكم الاستئناف والابتدائية ومجلس الدولة",
        specialization = RequestCategory.COMMERCIAL,
        yearsExperience = 5,
        firmName = companyName ?: "مكتب المحاماة",
        barCardUploaded = true,
        idCardUploaded = true
      )
    } else {
      registerClientWithKyc(
        name = name,
        phone = phone,
        email = email,
        nationalId = nationalIdOrCr,
        clientType = if (companyName.isNullOrBlank()) ClientType.INDIVIDUAL else ClientType.CORPORATE,
        companyName = companyName,
        governorate = "القاهرة",
        idFrontUploaded = true,
        idBackUploaded = true
      )
    }
  }

  fun logout() {
    val current = _currentUser.value
    _currentUser.value = current.copy(isLoggedIn = false)
    scope.launch {
      userPrefs?.logout()
    }
  }

  fun verifyNafath() {
    val current = _currentUser.value
    val updatedUser = current.copy(nafathVerified = true, isVerified = true)
    _currentUser.value = updatedUser
    addNotification(
      title = "تم التحقق من بطاقة الرقم القومي",
      body = "تم توثيق بطاقة الرقم القومي المصري بنجاح ✓",
      requestId = null
    )
    scope.launch {
      userPrefs?.saveUserSession(updatedUser)
    }
  }

  fun verifyLawyerBarLicense(licenseNum: String) {
    val current = _currentUser.value
    val updatedUser = current.copy(
      licenseNumber = "قيد نقابة المحامين: $licenseNum",
      isVerified = true,
      pendingVerification = false
    )
    _currentUser.value = updatedUser
    addNotification(
      title = "تم اعتماد القيد بنقابة المحامين",
      body = "تمت مطابقة رقم القيد $licenseNum بسجلات نقابة المحامين واعتماده رسمياً.",
      requestId = null
    )
    scope.launch {
      userPrefs?.saveUserSession(updatedUser)
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    _appPreferences.value = _appPreferences.value.copy(themeMode = mode)
    scope.launch {
      userPrefs?.setThemeMode(mode)
    }
  }

  fun setNotificationsEnabled(enabled: Boolean) {
    _appPreferences.value = _appPreferences.value.copy(notificationsEnabled = enabled)
    scope.launch {
      userPrefs?.setNotificationsEnabled(enabled)
    }
  }

  fun setBiometricAuthEnabled(enabled: Boolean) {
    _appPreferences.value = _appPreferences.value.copy(biometricAuthEnabled = enabled)
    scope.launch {
      userPrefs?.setBiometricAuthEnabled(enabled)
    }
  }

  fun setSoundAlertsEnabled(enabled: Boolean) {
    _appPreferences.value = _appPreferences.value.copy(soundAlertsEnabled = enabled)
    scope.launch {
      userPrefs?.setSoundAlertsEnabled(enabled)
    }
  }

  /**
   * Initializes Room Local Database and DataStore User Preferences.
   */
  fun initDatabase(context: Context) {
    // Initialize Firestore Real-time Services
    FirestoreChatService.init(context)
    FirestoreRatingService.init(context)

    if (userPrefs == null) {
      val prefs = UserPreferencesRepository.getInstance(context)
      userPrefs = prefs
      scope.launch {
        prefs.appPreferencesFlow.collect { appPrefs ->
          _appPreferences.value = appPrefs
        }
      }
      scope.launch {
        prefs.userProfileFlow.collect { profile ->
          _currentUser.value = profile
        }
      }
    }

    if (db != null) return
    val database = MaitreDatabase.getInstance(context)
    db = database

    scope.launch {
      try {
        // Persist seed lawyers to Room DB
        val lawyerEntities = _lawyers.value.map {
          LawyerEntity(
            id = it.id,
            name = it.name,
            specialization = it.specialization.titleAr,
            degree = it.degree.name,
            city = it.city,
            licenseNumber = it.licenseNumber,
            isVerified = it.isVerified,
            verificationStatus = it.verificationStatus.name,
            rating = it.rating,
            reviewsCount = it.reviewsCount,
            yearsExperience = it.yearsExperience,
            bio = it.bio,
            consultationFee = it.consultationFee
          )
        }
        database.lawyerDao().insertAll(lawyerEntities)

        // Persist seed requests to Room DB
        val requestEntities = _requests.value.map {
          ServiceRequestEntity(
            id = it.id,
            title = it.title,
            category = it.category.titleAr,
            description = it.description,
            city = it.city,
            budgetRange = it.budgetRange,
            budgetAmount = it.budgetAmount,
            urgency = it.urgency.labelAr,
            status = it.status.name,
            clientId = it.clientId,
            clientName = it.clientName,
            acceptedBidId = it.acceptedBidId,
            createdAt = it.createdAt,
            bidsCount = it.bidsCount,
            courtDistrict = it.courtLocation?.district,
            courtJurisdiction = it.courtLocation?.courtJurisdiction,
            appliedTemplateId = it.appliedTemplateId
          )
        }
        database.requestDao().insertAll(requestEntities)

        // Persist seed bids to Room DB
        val bidEntities = _bids.value.map {
          BidEntity(
            id = it.id,
            requestId = it.requestId,
            lawyerId = it.lawyerId,
            lawyerName = it.lawyerName,
            lawyerTitle = it.lawyerTitle,
            lawyerDegree = it.lawyerDegree.name,
            lawyerLicenseNumber = it.lawyerLicenseNumber,
            lawyerRating = it.lawyerRating,
            lawyerCasesCount = it.lawyerCasesCount,
            lawyerFee = it.lawyerFee,
            legalExpenses = it.legalExpenses,
            platformFeePercent = it.platformFeePercent,
            proposedAmount = it.grandTotalAmount,
            proposedDays = it.proposedDays,
            proposalNote = it.proposalNote,
            status = it.status.name,
            createdAt = it.createdAt
          )
        }
        database.bidDao().insertAll(bidEntities)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}
