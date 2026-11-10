package com.example.data.ai

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.ai.model.EliQuotaState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.quotaDataStore by preferencesDataStore(name = "eli_quota_prefs")

class DailyQuotaTracker(private val context: Context) {

  private object Keys {
    val QUOTA_DATE = stringPreferencesKey("quota_date")
    val QUOTA_USED = intPreferencesKey("quota_used")
  }

  private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
  private val todayKey: String
    get() = dateFormat.format(Date())

  val quotaState: Flow<EliQuotaState> = context.quotaDataStore.data.map { preferences ->
    val storedDate = preferences[Keys.QUOTA_DATE] ?: ""
    val used = if (storedDate == todayKey) {
      preferences[Keys.QUOTA_USED] ?: 0
    } else {
      0
    }
    EliQuotaState(dailyLimit = 20, usedToday = used)
  }

  suspend fun recordRequest(): Boolean {
    var allowed = false
    context.quotaDataStore.edit { preferences ->
      val storedDate = preferences[Keys.QUOTA_DATE] ?: ""
      val used = if (storedDate == todayKey) preferences[Keys.QUOTA_USED] ?: 0 else 0

      if (used < 20) {
        preferences[Keys.QUOTA_DATE] = todayKey
        preferences[Keys.QUOTA_USED] = used + 1
        allowed = true
      } else {
        allowed = false
      }
    }
    return allowed
  }
}
