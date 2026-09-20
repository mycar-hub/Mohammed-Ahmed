package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.*

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
  @PrimaryKey val id: String,
  val name: String,
  val email: String,
  val phone: String,
  val role: String,
  val balance: Double,
  val licenseNumber: String?,
  val barDegree: String = "APPEAL",
  val isVerified: Boolean,
  val pendingVerification: Boolean,
  val isLoggedIn: Boolean,
  val nationalIdOrCr: String,
  val companyName: String?,
  val nafathVerified: Boolean,
  val officeAddress: String?
)

@Entity(tableName = "service_requests")
data class ServiceRequestEntity(
  @PrimaryKey val id: String,
  val title: String,
  val category: String,
  val description: String,
  val city: String,
  val budgetRange: String,
  val budgetAmount: Double,
  val urgency: String,
  val status: String,
  val clientId: String,
  val clientName: String,
  val acceptedBidId: String?,
  val createdAt: String,
  val bidsCount: Int,
  val courtDistrict: String?,
  val courtJurisdiction: String?,
  val appliedTemplateId: String? = null
)

@Entity(tableName = "bids")
data class BidEntity(
  @PrimaryKey val id: String,
  val requestId: String,
  val lawyerId: String,
  val lawyerName: String,
  val lawyerTitle: String,
  val lawyerDegree: String = "APPEAL",
  val lawyerLicenseNumber: String,
  val lawyerRating: Double,
  val lawyerCasesCount: Int,
  val lawyerFee: Double,
  val legalExpenses: Double = 0.0,
  val platformFeePercent: Double = 10.0,
  val proposedAmount: Double,
  val proposedDays: Int,
  val proposalNote: String,
  val status: String,
  val createdAt: String
)

@Entity(tableName = "lawyers")
data class LawyerEntity(
  @PrimaryKey val id: String,
  val name: String,
  val specialization: String,
  val degree: String = "APPEAL",
  val city: String,
  val licenseNumber: String,
  val isVerified: Boolean,
  val verificationStatus: String,
  val rating: Double,
  val reviewsCount: Int,
  val yearsExperience: Int,
  val bio: String,
  val consultationFee: Double
)

@Entity(tableName = "escrow_transactions")
data class EscrowTransactionEntity(
  @PrimaryKey val id: String,
  val requestId: String,
  val totalAmount: Double,
  val lawyerAmount: Double,
  val platformFee: Double,
  val status: String,
  val paymentMethod: String,
  val referenceNumber: String,
  val date: String
)

@Entity(tableName = "disputes")
data class DisputeEntity(
  @PrimaryKey val id: String,
  val requestId: String,
  val requestTitle: String,
  val openedByRole: String,
  val openedByName: String,
  val reason: String,
  val details: String,
  val status: String,
  val adminNote: String?,
  val createdAt: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey val id: String,
  val requestId: String,
  val senderId: String,
  val senderName: String,
  val senderRole: String,
  val text: String,
  val timestamp: String,
  val attachmentName: String?,
  val isSystemMessage: Boolean
)

@Entity(tableName = "contact_violations")
data class ViolationEntity(
  @PrimaryKey val id: String,
  val timestamp: String,
  val userRole: String,
  val userName: String,
  val contextField: String,
  val violationType: String,
  val redactedSnippet: String,
  val severity: String,
  val isBlocked: Boolean
)

@Entity(tableName = "security_risk_events")
data class SecurityRiskEntity(
  @PrimaryKey val id: String,
  val eventType: String,
  val description: String,
  val userIdentifier: String,
  val severity: String,
  val timestamp: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
  @PrimaryKey val id: String,
  val title: String,
  val body: String,
  val timestamp: String,
  val isRead: Boolean,
  val relatedRequestId: String?
)
