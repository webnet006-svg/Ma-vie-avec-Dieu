package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_verses")
data class DailyVerseEntity(
  @PrimaryKey
  val id: String,
  val reference: String,
  val text: String,
  val book: String,
  val chapter: Int,
  val verseNumber: String,
  val theme: String = "Grâce",
  val translation: String = "Segond 21",
  val reflectionPrompt: String = "",
  val prayerPrompt: String = "",
  val date: String, // Format YYYY-MM-DD
  val isFavorite: Boolean = false,
  val savedAtTimestamp: Long = System.currentTimeMillis()
)
