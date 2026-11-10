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
  }

  val userProfileFlow: Flow<UserProfile> = context.dataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { preferences ->
      val name = preferences[PreferencesKeys.USER_NAME] ?: "Alexandre"
      val favoriteVerse = preferences[PreferencesKeys.FAVORITE_VERSE] ?: "Car je connais les projets que j'ai formés sur vous, dit l'Éternel... (Jérémie 29:11)"
      val preferredVersion = preferences[PreferencesKeys.PREFERRED_VERSION] ?: "Segond 21"
      val reminderTime = preferences[PreferencesKeys.PRAYER_REMINDER_TIME] ?: "07:00"
      val eveningTime = preferences[PreferencesKeys.EVENING_REFLECTION_TIME] ?: "21:30"
      val notifications = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
      val themeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
      val themeMode = runCatching { ThemeMode.valueOf(themeString) }.getOrDefault(ThemeMode.SYSTEM)
      val streak = preferences[PreferencesKeys.CURRENT_STREAK] ?: 7
      val prayersAnswered = preferences[PreferencesKeys.TOTAL_PRAYERS_ANSWERED] ?: 12
      val chaptersRead = preferences[PreferencesKeys.TOTAL_CHAPTERS_READ] ?: 48

      UserProfile(
        name = name,
        favoriteVerse = favoriteVerse,
        preferredVersion = preferredVersion,
        prayerReminderTime = reminderTime,
        eveningReflectionTime = eveningTime,
        notificationsEnabled = notifications,
        themeMode = themeMode,
        currentStreak = streak,
        totalPrayersAnswered = prayersAnswered,
        totalChaptersRead = chaptersRead
      )
    }

  suspend fun updateProfile(
    name: String,
    preferredVersion: String,
    reminderTime: String,
    themeMode: ThemeMode
  ) {
    context.dataStore.edit { preferences ->
      preferences[PreferencesKeys.USER_NAME] = name
      preferences[PreferencesKeys.PREFERRED_VERSION] = preferredVersion
      preferences[PreferencesKeys.PRAYER_REMINDER_TIME] = reminderTime
      preferences[PreferencesKeys.THEME_MODE] = themeMode.name
    }
  }

  suspend fun incrementStreak() {
    context.dataStore.edit { preferences ->
      val current = preferences[PreferencesKeys.CURRENT_STREAK] ?: 7
      preferences[PreferencesKeys.CURRENT_STREAK] = current + 1
    }
  }

  suspend fun incrementChaptersRead() {
    context.dataStore.edit { preferences ->
      val current = preferences[PreferencesKeys.TOTAL_CHAPTERS_READ] ?: 48
      preferences[PreferencesKeys.TOTAL_CHAPTERS_READ] = current + 1
    }
  }

  suspend fun resetStreak() {
    context.dataStore.edit { preferences ->
      preferences[PreferencesKeys.CURRENT_STREAK] = 0
    }
  }
}
