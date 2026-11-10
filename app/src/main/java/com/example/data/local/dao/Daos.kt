package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
  @Query("SELECT * FROM prayers ORDER BY createdAtTimestamp DESC")
  fun getAllPrayers(): Flow<List<PrayerEntity>>

  @Query("SELECT * FROM prayers WHERE id = :id")
  suspend fun getPrayerById(id: String): PrayerEntity?

  @Query("SELECT * FROM prayers WHERE isAnswered = 1 ORDER BY answeredDateTimestamp DESC")
  fun getAnsweredPrayers(): Flow<List<PrayerEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPrayer(prayer: PrayerEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(prayers: List<PrayerEntity>)

  @Update
  suspend fun updatePrayer(prayer: PrayerEntity)

  @Delete
  suspend fun deletePrayer(prayer: PrayerEntity)

  @Query("DELETE FROM prayers WHERE id = :id")
  suspend fun deletePrayerById(id: String)

  @Query("DELETE FROM prayers")
  suspend fun deleteAllPrayers()
}

@Dao
interface DevotionDao {
  @Query("SELECT * FROM devotions ORDER BY dayOrder ASC")
  fun getAllDevotions(): Flow<List<DevotionEntity>>

  @Query("SELECT * FROM devotions WHERE id = :id")
  suspend fun getDevotionById(id: String): DevotionEntity?

  @Query("SELECT * FROM devotions WHERE dayOrder = :dayOrder LIMIT 1")
  fun getDevotionByDay(dayOrder: Int): Flow<DevotionEntity?>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(devotions: List<DevotionEntity>)

  @Update
  suspend fun updateDevotion(devotion: DevotionEntity)

  @Query("SELECT COUNT(*) FROM devotions WHERE isCompleted = 1")
  fun getCompletedCount(): Flow<Int>
}

@Dao
interface ReadingPlanDao {
  @Query("SELECT * FROM reading_plans")
  fun getAllPlans(): Flow<List<ReadingPlanEntity>>

  @Query("SELECT * FROM reading_plans WHERE id = :id")
  suspend fun getPlanById(id: String): ReadingPlanEntity?

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertPlans(plans: List<ReadingPlanEntity>)

  @Update
  suspend fun updatePlan(plan: ReadingPlanEntity)

  @Query("SELECT * FROM reading_plan_days WHERE planId = :planId ORDER BY dayNumber ASC")
  fun getDaysForPlan(planId: String): Flow<List<ReadingPlanDayEntity>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertPlanDays(days: List<ReadingPlanDayEntity>)

  @Update
  suspend fun updatePlanDay(day: ReadingPlanDayEntity)

  @Query("SELECT COUNT(*) FROM reading_plan_days WHERE planId = :planId AND isCompleted = 1")
  fun getCompletedDaysCount(planId: String): Flow<Int>
}

@Dao
interface SpiritualGoalDao {
  @Query("SELECT * FROM spiritual_goals ORDER BY lastUpdatedTimestamp DESC")
  fun getAllGoals(): Flow<List<SpiritualGoalEntity>>

  @Query("SELECT * FROM spiritual_goals WHERE id = :id")
  suspend fun getGoalById(id: String): SpiritualGoalEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGoal(goal: SpiritualGoalEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(goals: List<SpiritualGoalEntity>)

  @Update
  suspend fun updateGoal(goal: SpiritualGoalEntity)

  @Delete
  suspend fun deleteGoal(goal: SpiritualGoalEntity)

  @Query("DELETE FROM spiritual_goals WHERE id = :id")
  suspend fun deleteGoalById(id: String)

  @Query("DELETE FROM spiritual_goals")
  suspend fun deleteAllGoals()
}
