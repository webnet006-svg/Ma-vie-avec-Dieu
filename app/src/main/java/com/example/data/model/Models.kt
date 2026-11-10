package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PrayerCategory(val code: String, val label: String) {
  FAMILLE("FAMILLE", "Famille"),
  TRAVAIL("TRAVAIL", "Travail"),
  ETUDES("ETUDES", "Études"),
  SANTE("SANTE", "Santé"),
  EGLISE("EGLISE", "Église"),
  VIE_PERSONNELLE("VIE_PERSONNELLE", "Vie personnelle"),
  AUTRES("AUTRES", "Autres");

  companion object {
    fun fromCode(code: String): PrayerCategory =
      entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: AUTRES
  }
}

enum class Testament(val label: String) {
  ANCIEN("Ancien Testament"),
  NOUVEAU("Nouveau Testament")
}

enum class GoalCategory(val label: String) {
  PRIERE("Prière"),
  LECTURE("Lecture biblique"),
  MEMORISATION("Mémorisation"),
  MEDITATION("Méditation")
}

enum class GoalFrequency(val code: String, val label: String) {
  QUOTIDIEN("QUOTIDIEN", "Quotidien"),
  HEBDOMADAIRE("HEBDOMADAIRE", "Hebdomadaire"),
  MENSUEL("MENSUEL", "Mensuel");

  companion object {
    fun fromCode(code: String): GoalFrequency =
      entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: QUOTIDIEN
  }
}

enum class ThemeMode(val label: String) {
  SYSTEM("Selon le système"),
  LIGHT("Mode clair"),
  DARK("Mode sombre"),
  BLEU("Thème Bleu"),
  VERT("Thème Vert"),
  ROUGE("Thème Rouge"),
  VIOLET("Thème Violet")
}

data class Verse(
  val id: String,
  val reference: String,
  val text: String,
  val book: String,
  val chapter: Int,
  val verseNumber: String,
  val version: String = "Louis Segond 1910",
  val theme: String = "Foi & Paix",
  val isBookmarked: Boolean = false
)

data class Devotion(
  val id: String,
  val dayOrder: Int,
  val dateFormatted: String,
  val title: String,
  val verse: Verse,
  val contextExplanation: String, // Explication du contexte
  val reflectionQuestion: String, // Questions de réflexion
  val practicalAction: String, // Action pratique
  val prayer: String,
  val author: String = "Équipe Pastorale - Ma Vie avec Dieu",
  val isCompleted: Boolean = false,
  val completedDate: String? = null
)

data class PrayerItem(
  val id: String,
  val title: String,
  val description: String,
  val category: PrayerCategory,
  val biblicalReference: String? = null,
  val createdAtTimestamp: Long = System.currentTimeMillis(),
  val dateAdded: String = formatTimestamp(createdAtTimestamp),
  val isAnswered: Boolean = false,
  val answeredDate: String? = null,
  val testimony: String? = null,
  val prayerCount: Int = 1,
  val isPrivate: Boolean = true // Privée par défaut
)

data class ReadingPlan(
  val id: String,
  val title: String,
  val description: String,
  val durationDays: Int,
  val category: String,
  val isEnrolled: Boolean = false,
  val completedDaysCount: Int = 0,
  val days: List<ReadingPlanDay> = emptyList()
) {
  val progressPercent: Float
    get() = if (durationDays > 0) (completedDaysCount.toFloat() / durationDays.toFloat()).coerceIn(0f, 1f) else 0f
}

data class ReadingPlanDay(
  val id: String,
  val planId: String,
  val dayNumber: Int,
  val title: String,
  val scriptureReference: String,
  val summary: String,
  val isCompleted: Boolean = false
)

data class BibleBook(
  val id: String,
  val name: String,
  val testament: Testament,
  val chaptersCount: Int,
  val category: String
)

data class BibleVerseItem(
  val number: Int,
  val text: String,
  val isHighlighted: Boolean = false
)

data class BibleChapter(
  val bookId: String,
  val bookName: String,
  val chapterNumber: Int,
  val verses: List<BibleVerseItem>
)

data class SpiritualGoal(
  val id: String,
  val title: String,
  val description: String,
  val category: GoalCategory,
  val targetCount: Int,
  val currentCount: Int,
  val unit: String,
  val frequency: GoalFrequency,
  val streakDays: Int = 0,
  val isCompleted: Boolean = false
) {
  val progress: Float
    get() = if (targetCount > 0) (currentCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f) else 0f
}

data class WeeklyReview(
  val totalGoals: Int,
  val completedGoals: Int,
  val totalPrayersCount: Int,
  val answeredPrayersCount: Int,
  val devotionCompletedCount: Int,
  val encouragementMessage: String
)

data class SpiritualBadge(
  val id: String,
  val title: String,
  val description: String,
  val isUnlocked: Boolean,
  val progressText: String
)

data class ChatMessage(
  val id: String,
  val isUser: Boolean,
  val content: String,
  val timestamp: String,
  val biblicalCitations: List<String> = emptyList(),
  val prayerPrompt: String? = null
)

data class UserProfile(
  val name: String = "Disciple",
  val favoriteVerse: String = "Car je connais les projets que j'ai formés sur vous, dit l'Éternel... (Jérémie 29:11)",
  val preferredVersion: String = "Louis Segond 1910",
  val prayerReminderTime: String = "07:00",
  val eveningReflectionTime: String = "21:30",
  val notificationsEnabled: Boolean = true,
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val geminiApiKey: String = "",
  val currentStreak: Int = 0,
  val totalPrayersAnswered: Int = 0,
  val totalChaptersRead: Int = 0
)

fun formatTimestamp(timestamp: Long): String {
  val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
  return sdf.format(Date(timestamp))
}
