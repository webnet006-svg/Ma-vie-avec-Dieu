package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayers")
data class PrayerEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val description: String,
  val category: String, // FAMILLE, TRAVAIL, ETUDES, SANTE, EGLISE, VIE_PERSONNELLE, AUTRES
  val biblicalReference: String? = null,
  val createdAtTimestamp: Long = System.currentTimeMillis(),
  val isAnswered: Boolean = false,
  val answeredDateTimestamp: Long? = null,
  val testimony: String? = null,
  val prayerCount: Int = 1,
  val isPrivate: Boolean = true // Toutes les prières sont privées par défaut
)

@Entity(tableName = "devotions")
data class DevotionEntity(
  @PrimaryKey
  val id: String,
  val dayOrder: Int, // 1 to 14
  val title: String,
  val scriptureReference: String,
  val verseText: String,
  val contextExplanation: String, // Explication du contexte
  val reflectionQuestions: String, // Questions de réflexion
  val practicalAction: String, // Action pratique pour la journée
  val guidedPrayer: String, // Prière guidée
  val isCompleted: Boolean = false,
  val completedAtTimestamp: Long? = null
)

@Entity(tableName = "reading_plans")
data class ReadingPlanEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val description: String,
  val durationDays: Int, // 7 or 30
  val category: String, // FOI, PRIERE, PARDON, AMOUR, SAGESSE, NOUVELLE_NAISSANCE, VIE_EN_CHRIST
  val isEnrolled: Boolean = false
)

@Entity(tableName = "reading_plan_days")
data class ReadingPlanDayEntity(
  @PrimaryKey
  val id: String,
  val planId: String,
  val dayNumber: Int,
  val title: String,
  val scriptureReference: String,
  val summary: String,
  val isCompleted: Boolean = false,
  val completedAtTimestamp: Long? = null
)

@Entity(tableName = "spiritual_goals")
data class SpiritualGoalEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val description: String,
  val category: String, // PRIERE, LECTURE, MEMORISATION, MEDITATION
  val targetValue: Int,
  val currentValue: Int,
  val unit: String,
  val frequency: String, // QUOTIDIEN, HEBDOMADAIRE, MENSUEL
  val streakDays: Int = 0,
  val isCompleted: Boolean = false,
  val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
