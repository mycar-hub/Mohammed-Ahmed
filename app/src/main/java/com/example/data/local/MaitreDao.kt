package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceRequestDao {
  @Query("SELECT * FROM service_requests ORDER BY createdAt DESC")
  fun getAllRequests(): Flow<List<ServiceRequestEntity>>

  @Query("SELECT * FROM service_requests WHERE id = :id")
  suspend fun getRequestById(id: String): ServiceRequestEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRequest(request: ServiceRequestEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(requests: List<ServiceRequestEntity>)

  @Update
  suspend fun updateRequest(request: ServiceRequestEntity)

  @Query("DELETE FROM service_requests")
  suspend fun deleteAll()
}

@Dao
interface BidDao {
  @Query("SELECT * FROM bids ORDER BY createdAt DESC")
  fun getAllBids(): Flow<List<BidEntity>>

  @Query("SELECT * FROM bids WHERE requestId = :requestId ORDER BY createdAt DESC")
  fun getBidsForRequest(requestId: String): Flow<List<BidEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBid(bid: BidEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(bids: List<BidEntity>)

  @Update
  suspend fun updateBid(bid: BidEntity)
}

@Dao
interface LawyerDao {
  @Query("SELECT * FROM lawyers ORDER BY rating DESC")
  fun getAllLawyers(): Flow<List<LawyerEntity>>

  @Query("SELECT * FROM lawyers WHERE id = :id")
  suspend fun getLawyerById(id: String): LawyerEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(lawyers: List<LawyerEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLawyer(lawyer: LawyerEntity)
}

@Dao
interface EscrowDao {
  @Query("SELECT * FROM escrow_transactions ORDER BY date DESC")
  fun getAllTransactions(): Flow<List<EscrowTransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: EscrowTransactionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(transactions: List<EscrowTransactionEntity>)

  @Update
  suspend fun updateTransaction(transaction: EscrowTransactionEntity)
}

@Dao
interface DisputeDao {
  @Query("SELECT * FROM disputes ORDER BY createdAt DESC")
  fun getAllDisputes(): Flow<List<DisputeEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDispute(dispute: DisputeEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(disputes: List<DisputeEntity>)

  @Update
  suspend fun updateDispute(dispute: DisputeEntity)
}

@Dao
interface ChatMessageDao {
  @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
  fun getAllMessages(): Flow<List<ChatMessageEntity>>

  @Query("SELECT * FROM chat_messages WHERE requestId = :requestId ORDER BY timestamp ASC")
  fun getMessagesForRequest(requestId: String): Flow<List<ChatMessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessageEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(messages: List<ChatMessageEntity>)
}

@Dao
interface ViolationDao {
  @Query("SELECT * FROM contact_violations ORDER BY timestamp DESC")
  fun getAllViolations(): Flow<List<ViolationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertViolation(violation: ViolationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(violations: List<ViolationEntity>)
}

@Dao
interface SecurityRiskDao {
  @Query("SELECT * FROM security_risk_events ORDER BY timestamp DESC")
  fun getAllEvents(): Flow<List<SecurityRiskEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEvent(event: SecurityRiskEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(events: List<SecurityRiskEntity>)
}

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
  fun getAllNotifications(): Flow<List<NotificationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: NotificationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(notifications: List<NotificationEntity>)

  @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
  suspend fun markAsRead(id: String)
}

@Dao
interface UserDao {
  @Query("SELECT * FROM user_profiles WHERE id = :id")
  suspend fun getUserById(id: String): UserProfileEntity?

  @Query("SELECT * FROM user_profiles")
  fun getAllUsers(): Flow<List<UserProfileEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserProfileEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(users: List<UserProfileEntity>)
}
