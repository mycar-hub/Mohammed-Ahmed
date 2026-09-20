package com.example.service

import java.text.Normalizer
import java.util.regex.Pattern

data class LeakageCheckResult(
  val isBlocked: Boolean,
  val violationType: String? = null,
  val redactedSnippet: String? = null,
  val userMessage: String? = null
)

/**
 * Centralized Anti-Contact-Circumvention service for the Egyptian Maitre Platform.
 * Protects:
 * 1. Client service requests (case summary, title, details)
 * 2. Lawyer bids, proposals, and revision notes
 * 3. Pre-CONTACT_UNLOCKED chat messages
 */
object ContactLeakageDetectionService {

  private val STANDARD_BLOCK_MESSAGE =
    "لا يُسمح بإضافة أرقام الهواتف أو وسائل الاتصال الخارجية داخل الطلبات والعروض. يتم تبادل بيانات الاتصال رسميًا من خلال المنصة بعد سداد رسم المنصة وفتح الاتصال."

  // Map Arabic/Hindi numerals to Latin digits
  private val ARABIC_HINDI_DIGITS_MAP = mapOf(
    '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4',
    '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9',
    '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
    '۵' to '5', '۶' to '6', '۷' to '7', '۸' to '8', '۹' to '9'
  )

  // Spelled-out numbers in Arabic
  private val SPELLED_ARABIC_DIGITS = listOf(
    "صفر" to "0", "زيرو" to "0",
    "واحد" to "1",
    "اثنين" to "2", "اتنين" to "2",
    "ثلاثة" to "3", "تلاتة" to "3",
    "اربعة" to "4", "أربعة" to "4",
    "خمسة" to "5",
    "ستة" to "6",
    "سبعة" to "7",
    "ثمانية" to "8", "تمانية" to "8",
    "تسعة" to "9"
  )

  // Contact indicators
  private val CONTACT_KEYWORDS = listOf(
    "واتساب", "واتس", "whatsapp", "whats",
    "تليجرام", "تيليجرام", "telegram",
    "موبايل", "mobile", "فون", "phone",
    "تليفون", "تلفون", "telephone",
    "اتصل", "رقمي", "نمرتي", "call", "contact",
    "ابعتلي", "كلمني", "تواصل"
  )

  /**
   * Normalizes Unicode, translates Arabic-Indic numerals to Latin digits,
   * strips invisible formatting characters.
   */
  fun normalizeText(input: String): String {
    if (input.isBlank()) return ""
    var text = Normalizer.normalize(input, Normalizer.Form.NFKC)
    
    // Replace Arabic-Hindi numerals
    val sb = StringBuilder()
    for (ch in text) {
      if (ARABIC_HINDI_DIGITS_MAP.containsKey(ch)) {
        sb.append(ARABIC_HINDI_DIGITS_MAP[ch])
      } else {
        sb.append(ch)
      }
    }
    text = sb.toString()

    // Replace common textual Arabic numbers
    var spelledNormalized = text
    for ((word, digit) in SPELLED_ARABIC_DIGITS) {
      spelledNormalized = spelledNormalized.replace(Regex("(?i)\\b$word\\b"), digit)
    }
    return spelledNormalized
  }

  /**
   * Authoritative check for contact leakage.
   * Returns [LeakageCheckResult.isBlocked] = true if suspicious contact sharing is detected.
   */
  fun inspectText(rawText: String, context: String = "general"): LeakageCheckResult {
    if (rawText.isBlank()) {
      return LeakageCheckResult(isBlocked = false)
    }

    val normalized = normalizeText(rawText)

    // 1. Check for Egyptian Mobile Numbers:
    // Formats: 010xxxxxxxx, 011xxxxxxxx, 012xxxxxxxx, 015xxxxxxxx, +201xxxxxxxxx, 00201xxxxxxxxx
    // Accommodate separators like spaces, dashes, dots, slashes between digits
    val egDirectMobileRegex = Regex("(?:\\+20|0020|0)?1[0125][\\s\\-_./()]*\\d[\\s\\-_./()]*\\d[\\s\\-_./()]*\\d[\\s\\-_./()]*\\d[\\s\\-_./()]*\\d[\\s\\-_./()]*\\d[\\s\\-_./()]*\\d[\\s\\-_./()]*\\d")
    val egDirectMatch = egDirectMobileRegex.find(normalized)
    if (egDirectMatch != null) {
      val rawMatched = egDirectMatch.value
      val cleanedDigits = rawMatched.filter { it.isDigit() }
      if (cleanedDigits.length in 10..13) {
        val redacted = redactNumber(cleanedDigits)
        return LeakageCheckResult(
          isBlocked = true,
          violationType = "EGYPTIAN_PHONE_NUMBER",
          redactedSnippet = redacted,
          userMessage = STANDARD_BLOCK_MESSAGE
        )
      }
    }

    // 2. Continuous Digit Sequence Check (Excluding Legal Articles, Years, Amounts)
    // Extract digit sequences by collapsing separated digits
    val digitSequenceRegex = Regex("(?:\\d[\\s\\-_./()]{0,3}){8,14}\\d")
    val digitMatches = digitSequenceRegex.findAll(normalized)
    for (match in digitMatches) {
      val cleaned = match.value.filter { it.isDigit() }
      
      // Check if this looks like an Egyptian phone or general phone
      if (isLegitimateLegalContext(normalized, match.range.first, match.range.last)) {
        // Safe legal reference (e.g. Legal Article, Case Number, Date, Budget)
        continue
      }

      if (cleaned.length in 9..14) {
        // Likely phone number
        return LeakageCheckResult(
          isBlocked = true,
          violationType = "SUSPICIOUS_DIGIT_SEQUENCE",
          redactedSnippet = redactNumber(cleaned),
          userMessage = STANDARD_BLOCK_MESSAGE
        )
      }
    }

    // 3. External URLs / Social Channels (t.me, wa.me, etc.)
    val linkRegex = Regex("(?i)(?:wa\\.me|t\\.me|telegram\\.me|whatsapp\\.com|facebook\\.com|instagr\\.am|instagram\\.com)/[a-zA-Z0-9_.-]+")
    val linkMatch = linkRegex.find(rawText)
    if (linkMatch != null) {
      return LeakageCheckResult(
        isBlocked = true,
        violationType = "EXTERNAL_COMMUNICATION_LINK",
        redactedSnippet = "[رابط تواصل خارجي محظور: ${linkMatch.value.take(20)}]",
        userMessage = STANDARD_BLOCK_MESSAGE
      )
    }

    // 4. Off-Platform Communication Solicitation (e.g. "واتساب على الخاص", "ابعتلي على التيليجرام", "راسلني واتس")
    val offPlatformSolicitationRegex = Regex("(?i)(?:واتساب|واتس|whatsapp|تيليجرام|تليجرام|telegram|ماسنجر|messenger|على الخاص|عالخاص|انستجرام|instagram)")
    val solicitationMatch = offPlatformSolicitationRegex.find(normalized)
    if (solicitationMatch != null) {
      return LeakageCheckResult(
        isBlocked = true,
        violationType = "OFF_PLATFORM_COMMUNICATION_ATTEMPT",
        redactedSnippet = "[محاولة توجيه لتطبيق خارجي: ${solicitationMatch.value}]",
        userMessage = STANDARD_BLOCK_MESSAGE
      )
    }

    // 5. Keyword + Number Combinations (e.g., "كلمني على..")
    for (kw in CONTACT_KEYWORDS) {
      if (normalized.contains(kw, ignoreCase = true)) {
        val kwIdx = normalized.indexOf(kw, ignoreCase = true)
        val snippetStart = (kwIdx - 20).coerceAtLeast(0)
        val snippetEnd = (kwIdx + kw.length + 25).coerceAtMost(normalized.length)
        val window = normalized.substring(snippetStart, snippetEnd)
        
        val digitsInWindow = window.filter { it.isDigit() }
        if (digitsInWindow.length >= 6) {
          return LeakageCheckResult(
            isBlocked = true,
            violationType = "KEYWORD_WITH_CONTACT_DIGITS",
            redactedSnippet = "$kw: ${redactNumber(digitsInWindow)}",
            userMessage = STANDARD_BLOCK_MESSAGE
          )
        }
      }
    }

    return LeakageCheckResult(isBlocked = false)
  }

  /**
   * Helper to verify if matched numbers are legal references
   * such as Egyptian law articles ("مادة 123"), case numbers ("قضية رقم 45 لسنة 2024"),
   * monetary amounts ("5000 ج.م"), or calendar dates ("2026/09/18").
   */
  private fun isLegitimateLegalContext(fullText: String, start: Int, end: Int): Boolean {
    val windowStart = (start - 35).coerceAtLeast(0)
    val windowEnd = (end + 35).coerceAtMost(fullText.length)
    val contextWindow = fullText.substring(windowStart, windowEnd)

    val legalPrefixes = listOf(
      "مادة", "المادة", "قانون", "فقرة", "بند",
      "قضية", "دعوى", "جنحة", "جناية", "محضر", "حكم",
      "لسنة", "سنة", "بتاريخ", "تاريخ",
      "جنيه", "ج.م", "أتعاب", "رسوم", "مبلغ", "قيمة"
    )

    for (prefix in legalPrefixes) {
      if (contextWindow.contains(prefix)) {
        // If it also contains mobile keywords or +20, it's NOT a safe legal context
        if (contextWindow.contains("موبايل") || contextWindow.contains("واتس") || contextWindow.contains("اتصل") || contextWindow.contains("+20")) {
          return false
        }
        val digits = fullText.substring(start, end).filter { it.isDigit() }
        // Case numbers or article numbers are usually not 11-digit mobile patterns starting with 01
        if (!digits.startsWith("010") && !digits.startsWith("011") && !digits.startsWith("012") && !digits.startsWith("015")) {
          return true
        }
      }
    }
    return false
  }

  /**
   * Redacts numbers for secure storage in audit logs without leaking plaintext digits.
   */
  fun redactNumber(rawNumber: String): String {
    if (rawNumber.length <= 4) return "****"
    val prefix = rawNumber.take(3)
    val suffix = rawNumber.takeLast(2)
    val masked = "*".repeat((rawNumber.length - 5).coerceAtLeast(3))
    return "$prefix$masked$suffix"
  }
}
