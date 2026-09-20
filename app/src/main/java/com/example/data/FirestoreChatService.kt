package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.ChatMessage
import com.example.model.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Real-time Firestore Chat Service for Maitre legal workspace.
 * Uses Firestore collections:
 *   cases/{caseId}/messages/{messageId}
 *   cases/{caseId}/presence/{userId}
 *
 * Provides real-time snapshot listeners for live messaging between clients, lawyers, and platform mediators.
 */
object FirestoreChatService {
  private const val TAG = "FirestoreChatService"
  private const val CASES_COLLECTION = "cases"
  private const val MESSAGES_SUBCOLLECTION = "messages"
  private const val PRESENCE_SUBCOLLECTION = "presence"

  private var firestoreInstance: FirebaseFirestore? = null
  private var isInitialized = false

  fun init(context: Context) {
    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        firestoreInstance = FirebaseFirestore.getInstance()
        isInitialized = true
        Log.i(TAG, "Firestore initialized successfully for real-time chat.")
      } else {
        Log.w(TAG, "FirebaseApp is not configured. Running in offline/memory fallback mode.")
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firebase initialization exception: ${e.message}. Using graceful local fallback.")
      firestoreInstance = null
      isInitialized = false
    }
  }

  fun isCloudConnected(): Boolean = firestoreInstance != null

  /**
   * Listens to real-time message stream for a specific legal case/request in Firestore.
   */
  fun observeMessages(requestId: String): Flow<List<ChatMessage>> = callbackFlow {
    val db = firestoreInstance
    if (db == null) {
      // If Firebase isn't initialized, close immediately and caller uses local repository state
      channel.close()
      return@callbackFlow
    }

    var registration: ListenerRegistration? = null
    try {
      val query = db.collection(CASES_COLLECTION)
        .document(requestId)
        .collection(MESSAGES_SUBCOLLECTION)
        .orderBy("timestampMillis", Query.Direction.ASCENDING)

      registration = query.addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.e(TAG, "Firestore snapshot listener error for case $requestId: ${error.message}")
          return@addSnapshotListener
        }

        if (snapshot != null) {
          val messageList = snapshot.documents.mapNotNull { doc ->
            mapDocumentToChatMessage(doc, requestId)
          }
          trySend(messageList)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error attaching Firestore message listener: ${e.message}")
    }

    awaitClose {
      registration?.remove()
    }
  }

  /**
   * Sends a message to the case's Firestore message collection.
   */
  suspend fun sendMessage(requestId: String, message: ChatMessage): Result<Unit> {
    val db = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore is not available"))
    return try {
      val messageData = hashMapOf(
        "id" to message.id,
        "requestId" to requestId,
        "senderId" to message.senderId,
        "senderName" to message.senderName,
        "senderRole" to message.senderRole.name,
        "text" to message.text,
        "timestamp" to message.timestamp,
        "timestampMillis" to System.currentTimeMillis(),
        "attachmentName" to (message.attachmentName ?: ""),
        "isSystemMessage" to message.isSystemMessage,
        "deliveryStatus" to "DELIVERED",
        "createdAtServer" to FieldValue.serverTimestamp()
      )

      val messageDoc = db.collection(CASES_COLLECTION)
        .document(requestId)
        .collection(MESSAGES_SUBCOLLECTION)
        .document(message.id)

      messageDoc.set(messageData).await()

      // Also update case document metadata
      val caseSummaryUpdate = hashMapOf(
        "lastMessageText" to message.text,
        "lastMessageSender" to message.senderName,
        "lastMessageTime" to message.timestamp,
        "updatedAt" to FieldValue.serverTimestamp()
      )

      db.collection(CASES_COLLECTION)
        .document(requestId)
        .set(caseSummaryUpdate, SetOptions.merge())

      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send message to Firestore: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Sets user typing indicator in Firestore
   */
  fun setTyping(requestId: String, userId: String, userName: String, isTyping: Boolean) {
    val db = firestoreInstance ?: return
    try {
      val presenceDoc = db.collection(CASES_COLLECTION)
        .document(requestId)
        .collection(PRESENCE_SUBCOLLECTION)
        .document(userId)

      if (isTyping) {
        val data = hashMapOf(
          "userId" to userId,
          "userName" to userName,
          "isTyping" to true,
          "lastActive" to FieldValue.serverTimestamp()
        )
        presenceDoc.set(data, SetOptions.merge())
      } else {
        presenceDoc.delete()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error updating typing status: ${e.message}")
    }
  }

  /**
   * Real-time stream of users currently typing in the workspace
   */
  fun observeTypingUsers(requestId: String, currentUserId: String): Flow<List<String>> = callbackFlow {
    val db = firestoreInstance
    if (db == null) {
      channel.close()
      return@callbackFlow
    }

    var registration: ListenerRegistration? = null
    try {
      registration = db.collection(CASES_COLLECTION)
        .document(requestId)
        .collection(PRESENCE_SUBCOLLECTION)
        .addSnapshotListener { snapshot, error ->
          if (error != null || snapshot == null) return@addSnapshotListener
          val typingNames = snapshot.documents.mapNotNull { doc ->
            val uId = doc.getString("userId") ?: doc.id
            val isTyping = doc.getBoolean("isTyping") ?: false
            val name = doc.getString("userName") ?: "أحد الأطراف"
            if (uId != currentUserId && isTyping) name else null
          }
          trySend(typingNames)
        }
    } catch (e: Exception) {
      Log.e(TAG, "Error observing typing users: ${e.message}")
    }

    awaitClose {
      registration?.remove()
    }
  }

  private fun mapDocumentToChatMessage(doc: DocumentSnapshot, requestId: String): ChatMessage? {
    return try {
      val id = doc.getString("id") ?: doc.id
      val senderId = doc.getString("senderId") ?: ""
      val senderName = doc.getString("senderName") ?: "طرف بالنزاع"
      val roleStr = doc.getString("senderRole") ?: UserRole.CLIENT.name
      val senderRole = try {
        UserRole.valueOf(roleStr)
      } catch (e: Exception) {
        UserRole.CLIENT
      }
      val text = doc.getString("text") ?: ""
      val timestamp = doc.getString("timestamp") ?: "الآن"
      val attachmentName = doc.getString("attachmentName").takeIf { !it.isNullOrBlank() }
      val isSystemMessage = doc.getBoolean("isSystemMessage") ?: false

      ChatMessage(
        id = id,
        requestId = requestId,
        senderId = senderId,
        senderName = senderName,
        senderRole = senderRole,
        text = text,
        timestamp = timestamp,
        attachmentName = attachmentName,
        isSystemMessage = isSystemMessage
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing Firestore ChatMessage: ${e.message}")
      null
    }
  }
}
