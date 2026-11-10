package com.example.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
  val userId: String = "",
  val displayName: String = "",
  val email: String? = null,
  val denominationalAffiliation: String? = null,
  val dailyNotificationTime: String? = null,
  val offlineModeEnabled: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestorePrayer(
  val id: String = "",
  val userId: String = "",
  val title: String = "",
  val description: String? = null,
  val category: String = "AUTRE",
  val isAnswered: Boolean = false,
  val answeredDate: String? = null,
  val answeredNote: String? = null,
  val isPrivate: Boolean = true,
  val prayCount: Int = 0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreGoal(
  val id: String = "",
  val userId: String = "",
  val title: String = "",
  val description: String? = null,
  val category: String = "Prière",
  val targetDaysPerWeek: Int = 7,
  val completedDaysThisWeek: Int = 0,
  val streakWeeks: Int = 0,
  val lastProgressDate: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreReadingProgress(
  val id: String = "",
  val userId: String = "",
  val planId: String = "",
  val completedDays: Int = 0,
  val totalDays: Int = 7,
  val isCompleted: Boolean = false,
  val startedAt: String? = null,
  val lastReadAt: String? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestorePreferences(
  val id: String = "user_preferences",
  val userId: String = "",
  val darkMode: Boolean = false,
  val biblicalVersion: String = "LSG",
  val selectedAiModel: String? = "gemini-2.5-flash",
  val fontSizeScale: Double = 1.0,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
