package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ThemeMode
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

  private object PreferencesKeys {
    val USER_NAME = stringPreferencesKey("user_name")
    val FAVORITE_VERSE = stringPreferencesKey("favorite_verse")
    val PREFERRED_VERSION = stringPreferencesKey("preferred_version")
    val PRAYER_REMINDER_TIME = stringPreferencesKey("prayer_reminder_time")
    val EVENING_REFLECTION_TIME = stringPreferencesKey("evening_reflection_time")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val CURRENT_STREAK = intPreferencesKey("current_streak")
    val TOTAL_PRAYERS_ANSWERED = intPreferencesKey("total_prayers_answered")
    val TOTAL_CHAPTERS_READ = intPreferencesKey("total_chapters_read")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
    val FAVORITE_VERSES = stringSetPreferencesKey("favorite_verses")
  }

  val onboardingCompleted: Flow<Boolean> = context.dataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { preferences -> preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }

  val userProfileFlow: Flow<UserProfile> = context.dataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { preferences ->
      val name = preferences[PreferencesKeys.USER_NAME] ?: "Disciple"
      val favoriteVerse = preferences[PreferencesKeys.FAVORITE_VERSE] ?: "Car je connais les projets que j'ai formés sur vous, dit l'Éternel... (Jérémie 29:11)"
      val preferredVersion = preferences[PreferencesKeys.PREFERRED_VERSION] ?: "Louis Segond 1910"
      val reminderTime = preferences[PreferencesKeys.PRAYER_REMINDER_TIME] ?: "07:00"
      val eveningTime = preferences[PreferencesKeys.EVENING_REFLECTION_TIME] ?: "21:30"
      val notifications = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
      val themeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
      val themeMode = runCatching { ThemeMode.valueOf(themeString) }.getOrDefault(ThemeMode.SYSTEM)
      val geminiApiKey = preferences[PreferencesKeys.GEMINI_API_KEY] ?: ""
      val streak = preferences[PreferencesKeys.CURRENT_STREAK] ?: 0
      val prayersAnswered = preferences[PreferencesKeys.TOTAL_PRAYERS_ANSWERED] ?: 0
      val chaptersRead = preferences[PreferencesKeys.TOTAL_CHAPTERS_READ] ?: 0

      UserProfile(
        name = name,
        favoriteVerse = favoriteVerse,
        preferredVersion = preferredVersion,
        prayerReminderTime = reminderTime,
        eveningReflectionTime = eveningTime,
        notificationsEnabled = notifications,
        themeMode = themeMode,
        geminiApiKey = geminiApiKey,
        currentStreak = streak,
        totalPrayersAnswered = prayersAnswered,
        totalChaptersRead = chaptersRead
      )
    }

  val favoritedVerses: Flow<Set<String>> = context.dataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { preferences ->
      preferences[PreferencesKeys.FAVORITE_VERSES] ?: emptySet()
    }

  suspend fun updateProfile(
    name: String,
    preferredVersion: String,
    reminderTime: String,
    themeMode: ThemeMode,
    geminiApiKey: String = ""
  ) {
    context.dataStore.edit { preferences ->
      preferences[PreferencesKeys.USER_NAME] = name
      preferences[PreferencesKeys.PREFERRED_VERSION] = preferredVersion
      preferences[PreferencesKeys.PRAYER_REMINDER_TIME] = reminderTime
      preferences[PreferencesKeys.THEME_MODE] = themeMode.name
      preferences[PreferencesKeys.GEMINI_API_KEY] = geminiApiKey
    }
  }

  suspend fun incrementStreak() {
    context.dataStore.edit { preferences ->
      val current = preferences[PreferencesKeys.CURRENT_STREAK] ?: 0
      preferences[PreferencesKeys.CURRENT_STREAK] = current + 1
    }
  }

  suspend fun incrementChaptersRead() {
    context.dataStore.edit { preferences ->
      val current = preferences[PreferencesKeys.TOTAL_CHAPTERS_READ] ?: 0
      preferences[PreferencesKeys.TOTAL_CHAPTERS_READ] = current + 1
    }
  }

  suspend fun resetStreak() {
    context.dataStore.edit { preferences ->
      preferences[PreferencesKeys.CURRENT_STREAK] = 0
    }
  }

  suspend fun markOnboardingCompleted() {
    context.dataStore.edit { preferences ->
      preferences[PreferencesKeys.ONBOARDING_COMPLETED] = true
    }
  }

  /**
   * Removes personal data while keeping device-level preferences such as
   * the onboarding flag and the selected theme.
   */
  suspend fun clearUserData() {
    context.dataStore.edit { preferences ->
      preferences.remove(PreferencesKeys.USER_NAME)
      preferences.remove(PreferencesKeys.FAVORITE_VERSE)
      preferences.remove(PreferencesKeys.PREFERRED_VERSION)
      preferences.remove(PreferencesKeys.PRAYER_REMINDER_TIME)
      preferences.remove(PreferencesKeys.EVENING_REFLECTION_TIME)
      preferences.remove(PreferencesKeys.NOTIFICATIONS_ENABLED)
      preferences.remove(PreferencesKeys.CURRENT_STREAK)
      preferences.remove(PreferencesKeys.TOTAL_PRAYERS_ANSWERED)
      preferences.remove(PreferencesKeys.TOTAL_CHAPTERS_READ)
      preferences.remove(PreferencesKeys.FAVORITE_VERSES)
    }
  }

  suspend fun addFavoriteVerse(verseKey: String) {
    context.dataStore.edit { preferences ->
      val current = preferences[PreferencesKeys.FAVORITE_VERSES] ?: emptySet<String>()
      val updated = current.toMutableSet().apply { add(verseKey) }
      preferences[PreferencesKeys.FAVORITE_VERSES] = updated
    }
  }

  suspend fun removeFavoriteVerse(verseKey: String) {
    context.dataStore.edit { preferences ->
      val current = preferences[PreferencesKeys.FAVORITE_VERSES] ?: emptySet<String>()
      val updated = current.toMutableSet().apply { remove(verseKey) }
      preferences[PreferencesKeys.FAVORITE_VERSES] = updated
    }
  }
}
