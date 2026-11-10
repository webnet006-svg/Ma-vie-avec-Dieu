package com.example.data.firestore

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreSyncRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth
) {

  // Primary constructor resolving database ID from string resource
  constructor(context: Context) : this(
    resolveFirestore(context),
    resolveAuth(context)
  )

  fun currentUserId(): String? = auth.currentUser?.uid

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("L'utilisateur doit être connecté pour accéder à Cloud Firestore.")
  }

  // --- USER PROFILE ---

  fun observeUserProfile(): Flow<FirestoreUserProfile?> = flow {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    emitAll(
      docRef.snapshots()
        .map { snapshot ->
          if (snapshot.exists()) snapshot.toObject(FirestoreUserProfile::class.java) else null
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.READ, docRef.path)
          throw error
        }
    )
  }

  suspend fun saveUserProfile(profile: FirestoreUserProfile) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    val payload = mutableMapOf<String, Any>(
      "userId" to uid,
      "displayName" to profile.displayName.ifBlank { "Disciple" },
      "offlineModeEnabled" to profile.offlineModeEnabled
    )
    profile.email?.let { payload["email"] = it }
    profile.denominationalAffiliation?.let { payload["denominationalAffiliation"] = it }
    profile.dailyNotificationTime?.let { payload["dailyNotificationTime"] = it }

    try {
      val existing = docRef.get().await()
      if (existing.exists()) {
        payload["updatedAt"] = FieldValue.serverTimestamp()
        docRef.update(payload).await()
      } else {
        payload["createdAt"] = FieldValue.serverTimestamp()
        payload["updatedAt"] = FieldValue.serverTimestamp()
        docRef.set(payload).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  // --- PRAYERS ---

  fun observePrayers(): Flow<List<FirestorePrayer>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/prayers"
    val colRef = db.collection("users").document(uid).collection("prayers")
    emitAll(
      colRef.snapshots()
        .map { snapshot -> snapshot.toObjects(FirestorePrayer::class.java) }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun savePrayer(prayer: FirestorePrayer) {
    val uid = requireUserId()
    require(prayer.id.isNotBlank()) { "Prayer ID must not be blank" }
    val docRef = db.collection("users").document(uid).collection("prayers").document(prayer.id)
    val payload = mutableMapOf<String, Any>(
      "id" to prayer.id,
      "userId" to uid,
      "title" to prayer.title,
      "category" to prayer.category,
      "isAnswered" to prayer.isAnswered,
      "isPrivate" to prayer.isPrivate,
      "prayCount" to prayer.prayCount
    )
    prayer.description?.let { payload["description"] = it }
    prayer.answeredDate?.let { payload["answeredDate"] = it }
    prayer.answeredNote?.let { payload["answeredNote"] = it }

    try {
      val existing = docRef.get().await()
      if (existing.exists()) {
        payload["updatedAt"] = FieldValue.serverTimestamp()
        docRef.update(payload).await()
      } else {
        payload["createdAt"] = FieldValue.serverTimestamp()
        payload["updatedAt"] = FieldValue.serverTimestamp()
        docRef.set(payload).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deletePrayer(prayerId: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("prayers").document(prayerId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- GOALS ---

  fun observeGoals(): Flow<List<FirestoreGoal>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/goals"
    val colRef = db.collection("users").document(uid).collection("goals")
    emitAll(
      colRef.snapshots()
        .map { snapshot -> snapshot.toObjects(FirestoreGoal::class.java) }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun saveGoal(goal: FirestoreGoal) {
    val uid = requireUserId()
    require(goal.id.isNotBlank()) { "Goal ID must not be blank" }
    val docRef = db.collection("users").document(uid).collection("goals").document(goal.id)
    val payload = mutableMapOf<String, Any>(
      "id" to goal.id,
      "userId" to uid,
      "title" to goal.title,
      "category" to goal.category,
      "targetDaysPerWeek" to goal.targetDaysPerWeek,
      "completedDaysThisWeek" to goal.completedDaysThisWeek,
      "streakWeeks" to goal.streakWeeks
    )
    goal.description?.let { payload["description"] = it }
    goal.lastProgressDate?.let { payload["lastProgressDate"] = it }

    try {
      val existing = docRef.get().await()
      if (existing.exists()) {
        payload["updatedAt"] = FieldValue.serverTimestamp()
        docRef.update(payload).await()
      } else {
        payload["createdAt"] = FieldValue.serverTimestamp()
        payload["updatedAt"] = FieldValue.serverTimestamp()
        docRef.set(payload).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteGoal(goalId: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("goals").document(goalId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- READING PROGRESS ---

  fun observeReadingProgress(): Flow<List<FirestoreReadingProgress>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/reading_progress"
    val colRef = db.collection("users").document(uid).collection("reading_progress")
    emitAll(
      colRef.snapshots()
        .map { snapshot -> snapshot.toObjects(FirestoreReadingProgress::class.java) }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun saveReadingProgress(progress: FirestoreReadingProgress) {
    val uid = requireUserId()
    require(progress.id.isNotBlank()) { "Reading progress ID must not be blank" }
    val docRef = db.collection("users").document(uid).collection("reading_progress").document(progress.id)
    val payload = mutableMapOf<String, Any>(
      "id" to progress.id,
      "userId" to uid,
      "planId" to progress.planId,
      "completedDays" to progress.completedDays,
      "totalDays" to progress.totalDays,
      "isCompleted" to progress.isCompleted,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    progress.startedAt?.let { payload["startedAt"] = it }
    progress.lastReadAt?.let { payload["lastReadAt"] = it }

    try {
      docRef.set(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  // --- PREFERENCES ---

  fun observePreferences(): Flow<FirestorePreferences?> = flow {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("preferences").document("user_preferences")
    emitAll(
      docRef.snapshots()
        .map { snapshot ->
          if (snapshot.exists()) snapshot.toObject(FirestorePreferences::class.java) else null
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.READ, docRef.path)
          throw error
        }
    )
  }

  suspend fun savePreferences(prefs: FirestorePreferences) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("preferences").document("user_preferences")
    val payload = mapOf(
      "id" to "user_preferences",
      "userId" to uid,
      "darkMode" to prefs.darkMode,
      "biblicalVersion" to prefs.biblicalVersion,
      "selectedAiModel" to (prefs.selectedAiModel ?: "gemini-2.5-flash"),
      "fontSizeScale" to prefs.fontSizeScale,
      "updatedAt" to FieldValue.serverTimestamp()
    )

    try {
      docRef.set(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  // --- ACCOUNT DELETION (DELETE ALL USER DATA) ---

  suspend fun deleteEntireAccountData() {
    val uid = requireUserId()
    val userRef = db.collection("users").document(uid)

    try {
      // 1. Delete all prayers
      val prayers = userRef.collection("prayers").get().await()
      for (doc in prayers.documents) {
        doc.reference.delete().await()
      }

      // 2. Delete all goals
      val goals = userRef.collection("goals").get().await()
      for (doc in goals.documents) {
        doc.reference.delete().await()
      }

      // 3. Delete reading progress
      val progress = userRef.collection("reading_progress").get().await()
      for (doc in progress.documents) {
        doc.reference.delete().await()
      }

      // 4. Delete preferences
      val prefs = userRef.collection("preferences").get().await()
      for (doc in prefs.documents) {
        doc.reference.delete().await()
      }

      // 5. Delete user document
      userRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, userRef.path)
      throw e
    }
  }
}

// Flow extension helper
private suspend fun <T> kotlinx.coroutines.flow.FlowCollector<T>.emitAll(flow: Flow<T>) {
  flow.collect { value -> emit(value) }
}

private fun resolveFirestore(context: Context): FirebaseFirestore {
  if (FirebaseApp.getApps(context).isEmpty()) {
    FirebaseApp.initializeApp(context)
  }
  val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
  val databaseId = if (resId != 0) context.getString(resId) else "default"
  return FirebaseFirestore.getInstance(databaseId)
}

private fun resolveAuth(context: Context): FirebaseAuth {
  if (FirebaseApp.getApps(context).isEmpty()) {
    FirebaseApp.initializeApp(context)
  }
  return Firebase.auth
}

