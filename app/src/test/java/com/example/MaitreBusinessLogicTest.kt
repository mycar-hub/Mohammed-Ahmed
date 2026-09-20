package com.example

import com.example.data.MaitreRepository
import com.example.model.*
import com.example.service.ContactLeakageDetectionService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MaitreBusinessLogicTest {

  @Before
  fun setUp() {
    MaitreRepository.switchRole(UserRole.CLIENT)
  }

  @Test
  fun testContactLeakageService_detectsEgyptianPhoneNumbers() {
    // Standard 010, 011, 012, 015 Egyptian numbers
    assertTrue(ContactLeakageDetectionService.inspectText("كلمني فون على 01012345678 للتفاصيل").isBlocked)
    assertTrue(ContactLeakageDetectionService.inspectText("رقمي 01123456789 اتصل بي").isBlocked)
    assertTrue(ContactLeakageDetectionService.inspectText("تواصل عبر 01298765432").isBlocked)
    assertTrue(ContactLeakageDetectionService.inspectText("موبايلي 01555554444").isBlocked)

    // International Egyptian format +20 / 0020
    assertTrue(ContactLeakageDetectionService.inspectText("الاتصال على +201012345678").isBlocked)
    assertTrue(ContactLeakageDetectionService.inspectText("رقم الواتس 00201123456789").isBlocked)
  }

  @Test
  fun testContactLeakageService_detectsArabicIndicNumerals() {
    // Eastern Arabic numerals: ٠١٠١٢٣٤٥٦٧٨
    val textWithArabicNumerals = "كلمني على الرقم ٠١٠١٢٣٤٥٦٧٨ في أي وقت"
    val result = ContactLeakageDetectionService.inspectText(textWithArabicNumerals)
    assertTrue("Should detect phone numbers written with Arabic-Indic numerals", result.isBlocked)
    assertEquals("EGYPTIAN_PHONE_NUMBER", result.violationType)
  }

  @Test
  fun testContactLeakageService_detectsObfuscatedSeparators() {
    // Numbers separated by dashes, dots, spaces, or slashes
    assertTrue(ContactLeakageDetectionService.inspectText("رقمي 0 1 0 1 2 3 4 5 6 7 8").isBlocked)
    assertTrue(ContactLeakageDetectionService.inspectText("تليفوني 010-1234-5678").isBlocked)
    assertTrue(ContactLeakageDetectionService.inspectText("هاتفي 011.9876.5432").isBlocked)
  }

  @Test
  fun testContactLeakageService_detectsExternalAppsAndSocialKeywords() {
    assertTrue(ContactLeakageDetectionService.inspectText("تواصل معي واتساب على الخاص").isBlocked)
    assertTrue(ContactLeakageDetectionService.inspectText("ابعتلي رسالة على التيليجرام").isBlocked)
    assertTrue(ContactLeakageDetectionService.inspectText("راسلني wa.me/201012345678").isBlocked)
    assertTrue(ContactLeakageDetectionService.inspectText("حسابي على تليجرام t.me/lawyer_advocate").isBlocked)
  }

  @Test
  fun testContactLeakageService_permitsCleanLegalText() {
    val cleanLegalText = "أحتاج محامي متخصص في صياغة عقد تأسيس شركة ذات مسؤولية محدودة وفق القانون المصري رقم 159 لسنة 1981 أمام محكمة القاهرة الاقتصادية."
    val result = ContactLeakageDetectionService.inspectText(cleanLegalText)
    assertFalse("Clean legal text mentioning Egyptian laws must NOT be flagged as phone numbers", result.isBlocked)
  }

  @Test
  fun testRepository_rejectsRequestCreation_whenPhoneNumberPresent() {
    val result = MaitreRepository.createRequest(
      title = "طلب مشورة",
      category = RequestCategory.COMMERCIAL,
      description = "للتواصل رقمي 01012345678 مباشرة",
      city = "القاهرة",
      budgetAmount = 2000.0,
      urgency = RequestUrgency.NORMAL
    )

    assertTrue("Request creation with leaked phone number must fail", result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("حظر") == true || result.exceptionOrNull()?.message?.contains("المنصة") == true)
  }

  @Test
  fun testRepository_acceptsRequestCreation_whenClean() {
    val result = MaitreRepository.createRequest(
      title = "مراجعة عقد إيجار تجاري",
      category = RequestCategory.CONTRACTS,
      description = "مطلوب مراجعة بنود الفسخ والتعويض والشرط الجزائي وفق القانون المدني المصري في محكمة الجيزة.",
      city = "الجيزة",
      budgetAmount = 1500.0,
      urgency = RequestUrgency.URGENT
    )

    assertTrue("Valid legal request creation must succeed", result.isSuccess)
    val created = result.getOrNull()
    assertNotNull(created)
    assertEquals(RequestStatus.OPEN, created?.status)
  }

  @Test
  fun testRepository_rejectsBid_whenPhoneNumberPresent() {
    val requests = MaitreRepository.requests.value
    val openReq = requests.first { it.status == RequestStatus.OPEN }

    val bidResult = MaitreRepository.addBid(
      requestId = openReq.id,
      proposedAmount = 1800.0,
      proposedDays = 3,
      proposalNote = "أنا جاهز لمباشرة الدعوى كلمني واتس 01112223334"
    )

    assertTrue("Submitting bid with leaked phone number must fail", bidResult.isFailure)
  }

  @Test
  fun testRepository_acceptBid_updatesStatusAndCalculatesPlatformFee() {
    val requests = MaitreRepository.requests.value
    val targetReq = requests.first { it.status == RequestStatus.OPEN }

    // Add clean bid
    val bidResult = MaitreRepository.addBid(
      requestId = targetReq.id,
      proposedAmount = 2500.0,
      proposedDays = 2,
      proposalNote = "جاهز لصياغة صحيفة الدعوى والحضور أمام المحكمة المختصة."
    )
    assertTrue(bidResult.isSuccess)
    val bid = bidResult.getOrNull()!!

    // Accept Bid with Platform Fee payment
    MaitreRepository.acceptBid(
      requestId = targetReq.id,
      bidId = bid.id,
      paymentMethod = "إنستاباي InstaPay"
    )

    val updatedReq = MaitreRepository.requests.value.first { it.id == targetReq.id }
    assertEquals(RequestStatus.IN_PROGRESS, updatedReq.status)
    assertEquals(bid.id, updatedReq.acceptedBidId)

    val escrowTx = MaitreRepository.escrowTransactions.value.firstOrNull { it.requestId == targetReq.id }
    assertNotNull("Platform fee transaction must be recorded", escrowTx)
    assertEquals(EscrowStatus.HELD, escrowTx?.status)
    // 5% of 2500 is 125 EGP
    assertEquals(125.0, escrowTx?.platformFee ?: 0.0, 0.01)
  }

  @Test
  fun testRepository_chatProtection_blocksLeakageBeforeUnlock() {
    // Create new request
    val reqResult = MaitreRepository.createRequest(
      title = "استشارة تأسيس شركة مساهمة",
      category = RequestCategory.COMMERCIAL,
      description = "طلب استشارة تفصيلية حول رأس المال المصدر والمدفوع.",
      city = "الإسكندرية",
      budgetAmount = 3000.0,
      urgency = RequestUrgency.NORMAL
    )
    val req = reqResult.getOrNull()!!

    // Attempt to send message with phone before unlock
    val leakMsgResult = MaitreRepository.sendMessage(
      requestId = req.id,
      text = "تواصل معي تليفونياً على 01099887766 لتوفير العمولة"
    )
    assertTrue("Pre-unlock chat message with phone must be rejected", leakMsgResult.isFailure)

    // Send clean message before unlock
    val cleanMsgResult = MaitreRepository.sendMessage(
      requestId = req.id,
      text = "هل لديكم خبرة في إجراءات الهيئة العامة للاستثمار والمناطق الحرة (GAFI)؟"
    )
    assertTrue("Pre-unlock clean legal message must be accepted", cleanMsgResult.isSuccess)
  }

  @Test
  fun testRepository_disputeResolution_updatesStatus() {
    val requests = MaitreRepository.requests.value
    val inProgressReq = requests.firstOrNull { it.status == RequestStatus.IN_PROGRESS } ?: requests.first()

    // Open dispute
    MaitreRepository.openDispute(
      requestId = inProgressReq.id,
      reason = "تأخر غير مبرر في تسليم العقد",
      details = "مضى 5 أيام على الموعد المتفق عليه دون إرسال المسودة."
    )

    val disputes = MaitreRepository.disputes.value
    val createdDispute = disputes.first { it.requestId == inProgressReq.id }
    assertEquals(DisputeStatus.UNDER_REVIEW, createdDispute.status)

    // Admin resolves dispute
    MaitreRepository.resolveDispute(
      disputeId = createdDispute.id,
      decision = DisputeStatus.RESOLVED_REFUND,
      adminNote = "ثبت الإخلال بالموعد الزمني وتم استرداد رسم المنصة للعميل وفق اللائحة المصرية."
    )

    val updatedDispute = MaitreRepository.disputes.value.first { it.id == createdDispute.id }
    assertEquals(DisputeStatus.RESOLVED_REFUND, updatedDispute.status)
    assertNotNull(updatedDispute.adminNote)
  }
}
