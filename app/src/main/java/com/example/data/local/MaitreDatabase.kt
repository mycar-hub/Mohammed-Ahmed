package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
  entities = [
    UserProfileEntity::class,
    ServiceRequestEntity::class,
    BidEntity::class,
    LawyerEntity::class,
    EscrowTransactionEntity::class,
    DisputeEntity::class,
    ChatMessageEntity::class,
    ViolationEntity::class,
    SecurityRiskEntity::class,
    NotificationEntity::class
  ],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MaitreDatabase : RoomDatabase() {
  abstract fun requestDao(): ServiceRequestDao
  abstract fun bidDao(): BidDao
  abstract fun lawyerDao(): LawyerDao
  abstract fun escrowDao(): EscrowDao
  abstract fun disputeDao(): DisputeDao
  abstract fun chatMessageDao(): ChatMessageDao
  abstract fun violationDao(): ViolationDao
  abstract fun securityRiskDao(): SecurityRiskDao
  abstract fun notificationDao(): NotificationDao
  abstract fun userDao(): UserDao

  companion object {
    @Volatile
    private var INSTANCE: MaitreDatabase? = null

    fun getInstance(context: Context): MaitreDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MaitreDatabase::class.java,
          "maitre_egypt_legal.db"
        )
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
