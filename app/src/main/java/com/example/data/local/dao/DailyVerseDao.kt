package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.DailyVerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyVerseDao {

  @Query("SELECT * FROM daily_verses ORDER BY savedAtTimestamp DESC")
  fun getAllDailyVerses(): Flow<List<DailyVerseEntity>>

  @Query("SELECT * FROM daily_verses WHERE isFavorite = 1 ORDER BY savedAtTimestamp DESC")
  fun getFavoriteDailyVerses(): Flow<List<DailyVerseEntity>>

  @Query("SELECT * FROM daily_verses WHERE date = :date ORDER BY savedAtTimestamp DESC LIMIT 1")
  fun getVerseForDate(date: String): Flow<DailyVerseEntity?>

  @Query("SELECT * FROM daily_verses ORDER BY date DESC, savedAtTimestamp DESC LIMIT 1")
  fun getLatestVerse(): Flow<DailyVerseEntity?>

  @Query("SELECT * FROM daily_verses WHERE id = :id LIMIT 1")
  suspend fun getVerseById(id: String): DailyVerseEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVerse(verse: DailyVerseEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(verses: List<DailyVerseEntity>)

  @Update
  suspend fun updateVerse(verse: DailyVerseEntity)

  @Delete
  suspend fun deleteVerse(verse: DailyVerseEntity)

  @Query("UPDATE daily_verses SET isFavorite = :isFavorite WHERE id = :id")
  suspend fun setFavorite(id: String, isFavorite: Boolean)

  @Query("DELETE FROM daily_verses WHERE id = :id")
  suspend fun deleteVerseById(id: String)

  @Query("SELECT COUNT(*) FROM daily_verses")
  suspend fun getCount(): Int
}
