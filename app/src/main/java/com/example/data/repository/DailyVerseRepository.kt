package com.example.data.repository

import com.example.data.local.dao.DailyVerseDao
import com.example.data.local.entity.DailyVerseEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyVerseRepository(
  private val dailyVerseDao: DailyVerseDao
) {

  val allDailyVerses: Flow<List<DailyVerseEntity>> = dailyVerseDao.getAllDailyVerses()

  val favoriteVerses: Flow<List<DailyVerseEntity>> = dailyVerseDao.getFavoriteDailyVerses()

  val latestVerse: Flow<DailyVerseEntity?> = dailyVerseDao.getLatestVerse()

  fun getVerseForDate(date: String): Flow<DailyVerseEntity?> {
    return dailyVerseDao.getVerseForDate(date)
  }

  suspend fun getVerseById(id: String): DailyVerseEntity? {
    return dailyVerseDao.getVerseById(id)
  }

  suspend fun saveDailyVerse(verse: DailyVerseEntity) {
    dailyVerseDao.insertVerse(verse)
  }

  suspend fun saveDailyVerses(verses: List<DailyVerseEntity>) {
    dailyVerseDao.insertAll(verses)
  }

  suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
    dailyVerseDao.setFavorite(id, isFavorite)
  }

  suspend fun deleteDailyVerse(id: String) {
    dailyVerseDao.deleteVerseById(id)
  }

  suspend fun ensureInitialVersesLoaded(seedVerses: List<DailyVerseEntity>) {
    if (dailyVerseDao.getCount() == 0) {
      dailyVerseDao.insertAll(seedVerses)
    }
  }

  companion object {
    fun getTodayDate(): String {
      return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
  }
}
