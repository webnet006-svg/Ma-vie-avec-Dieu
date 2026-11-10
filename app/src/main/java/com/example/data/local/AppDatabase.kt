package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DailyVerseDao
import com.example.data.local.dao.DevotionDao
import com.example.data.local.dao.PrayerDao
import com.example.data.local.dao.ReadingPlanDao
import com.example.data.local.dao.SpiritualGoalDao
import com.example.data.local.entity.DailyVerseEntity
import com.example.data.local.entity.DevotionEntity
import com.example.data.local.entity.PrayerEntity
import com.example.data.local.entity.ReadingPlanDayEntity
import com.example.data.local.entity.ReadingPlanEntity
import com.example.data.local.entity.SpiritualGoalEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    PrayerEntity::class,
    DevotionEntity::class,
    ReadingPlanEntity::class,
    ReadingPlanDayEntity::class,
    SpiritualGoalEntity::class,
    DailyVerseEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

  abstract fun prayerDao(): PrayerDao
  abstract fun devotionDao(): DevotionDao
  abstract fun readingPlanDao(): ReadingPlanDao
  abstract fun spiritualGoalDao(): SpiritualGoalDao
  abstract fun dailyVerseDao(): DailyVerseDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "ma_vie_avec_dieu.db"
        )
          .addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              // Prepopulate initial data on creation
              CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                database.prayerDao().insertAll(SeedData.getInitialPrayers())
                database.devotionDao().insertAll(SeedData.get14Devotions())
                database.readingPlanDao().insertPlans(SeedData.getInitialReadingPlans())
                database.readingPlanDao().insertPlanDays(SeedData.getInitialReadingPlanDays())
                database.spiritualGoalDao().insertAll(SeedData.getInitialGoals())
                database.dailyVerseDao().insertAll(SeedData.getInitialDailyVerses())
              }
            }
          })
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
