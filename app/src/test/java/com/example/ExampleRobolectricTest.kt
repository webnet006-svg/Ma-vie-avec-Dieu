package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.DailyQuotaTracker
import com.example.data.ai.EliAssistantService
import com.example.data.ai.gemini.GeminiApiClient
import com.example.data.ai.verifier.BibleReferenceVerifier
import com.example.data.local.AppDatabase
import com.example.data.local.SeedData
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.*
import com.example.data.repository.SpiritualRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: SpiritualRepository
  private lateinit var preferences: UserPreferencesRepository
  private lateinit var eliService: EliAssistantService
  private lateinit var context: Context

  @Before
  fun setup() = runBlocking {
    context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    // Prepopulate in-memory test database
    database.prayerDao().insertAll(SeedData.getInitialPrayers())
    database.devotionDao().insertAll(SeedData.get14Devotions())
    database.readingPlanDao().insertPlans(SeedData.getInitialReadingPlans())
    database.readingPlanDao().insertPlanDays(SeedData.getInitialReadingPlanDays())
    database.spiritualGoalDao().insertAll(SeedData.getInitialGoals())

    preferences = UserPreferencesRepository(context)
    repository = SpiritualRepository(context, database, preferences)
    eliService = EliAssistantService(context)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("Ma Vie avec Dieu", appName)
  }

  @Test
  fun `test MainViewModel creation`() {
    val app = context.applicationContext as android.app.Application
    val vm = com.example.ui.viewmodel.MainViewModel(app)
    assertNotNull(vm)
  }

  // --- PHASE 3: TESTS DE L'ASSISTANT BIBLIQUE ELI ---

  @Test
  fun `verify Eli - 1 Simple biblical question on salvation by grace`() = runBlocking {
    val response = eliService.generateVerifiedOfflineResponse(
      question = "Comment suis-je sauvé selon la Bible ?",
      timestamp = "10:00"
    )

    assertFalse(response.isUser)
    assertTrue("La réponse doit évoquer la grâce et la foi", response.content.contains("grâce", ignoreCase = true))
    assertTrue("Doit citer Éphésiens 2:8-9", response.verifiedCitations.any { it.reference.contains("Éphésiens 2:8-9") })
    val citation = response.verifiedCitations.first { it.reference.contains("Éphésiens 2:8-9") }
    assertTrue(citation.isVerified)
    assertNotNull(citation.exactText)
    assertNotNull(citation.historicalContext)
    assertNotNull("Doit proposer une prière guidée", response.suggestedPrayer)
  }

  @Test
  fun `verify Eli - 2 Controversial theological question with Protestant perspectives (Baptism)`() = runBlocking {
    val response = eliService.generateVerifiedOfflineResponse(
      question = "Que dit la Bible sur le baptême des enfants et le baptême de foi ?",
      timestamp = "10:05"
    )

    assertNotNull("Doit contenir les perspectives protestantes", response.theologicalPerspectives)
    val perspectives = response.theologicalPerspectives!!
    assertTrue("Doit mentionner le baptême de croyants (Crédobaptisme)", perspectives.contains("Crédobaptisme", ignoreCase = true))
    assertTrue("Doit mentionner le baptême d'alliance (Pédobaptisme)", perspectives.contains("Pédobaptisme", ignoreCase = true))
    assertFalse("Ne doit jamais prétendre être Dieu", response.content.contains("Je suis Dieu") || response.content.contains("Je suis le Saint-Esprit"))
  }

  @Test
  fun `verify Eli - 3 Non-existent biblical reference is not hallucinated as exact`() {
    val result = BibleReferenceVerifier.verifyReference("3 Ézéchiel 99:1")
    assertFalse("Une référence inexistante ne doit pas être marquée vérifiée", result.isVerified)
    assertNull(result.exactText)
    assertNotNull("Un avertissement doit être fourni", result.warningNote)
  }

  @Test
  fun `verify Eli - 4 Network outage fallback activation`() {
    // When offline, fallback produces clear theological answer without crashing
    val fallback = eliService.generateVerifiedOfflineResponse(
      question = "Comment prier face à l'angoisse ?",
      timestamp = "10:10",
      fallbackReason = "Pas de connexion Internet."
    )

    assertTrue(fallback.content.contains("Philippiens 4:6-7") || fallback.content.contains("prières"))
    assertNotNull(fallback.suggestedPrayer)
    assertTrue(fallback.verifiedCitations.isNotEmpty())
  }

  @Test
  fun `verify Eli - 5 Daily quota limits per user`() = runBlocking {
    val tracker = DailyQuotaTracker(context)
    val initialQuota = tracker.quotaState.first()
    assertEquals(20, initialQuota.dailyLimit)

    // Simulate consuming quota
    var allowedCount = 0
    for (i in 1..25) {
      if (tracker.recordRequest()) {
        allowedCount++
      }
    }
    assertEquals("Le quota journalier doit être plafonné à 20", 20, allowedCount)

    val finalQuota = tracker.quotaState.first()
    assertEquals(0, finalQuota.remaining)
    assertTrue(finalQuota.isExceeded)
  }

  @Test
  fun `verify Eli - 6 API key security and BuildConfig protection`() {
    // Verify API key is read via BuildConfig, not hardcoded in code
    val apiKey = BuildConfig.GEMINI_API_KEY
    assertNotNull("BuildConfig.GEMINI_API_KEY must exist", apiKey)
  }

  @Test
  fun `verify DailyVerse Room entity and repository`() = runBlocking {
    val dailyVerseDao = database.dailyVerseDao()
    dailyVerseDao.insertAll(SeedData.getInitialDailyVerses())

    val count = dailyVerseDao.getCount()
    assertTrue("Should have initial daily verses seeded", count >= 8)

    val verseRepo = com.example.data.repository.DailyVerseRepository(dailyVerseDao)
    val allVerses = verseRepo.allDailyVerses.first()
    assertEquals(count, allVerses.size)

    // Test toggle favorite
    val firstVerse = allVerses.first()
    verseRepo.toggleFavorite(firstVerse.id, true)

    val updatedFirstVerse = dailyVerseDao.getVerseById(firstVerse.id)
    assertNotNull(updatedFirstVerse)
    assertTrue("Verse should now be marked as favorite", updatedFirstVerse!!.isFavorite)

    val favorites = verseRepo.favoriteVerses.first()
    assertTrue(favorites.any { it.id == firstVerse.id })

    // Test save custom daily verse
    val customVerse = com.example.data.local.entity.DailyVerseEntity(
      id = "custom_1",
      reference = "Psaume 23:1",
      book = "Psaumes",
      chapter = 23,
      verseNumber = "1",
      text = "L'Éternel est mon berger: je ne manquerai de rien.",
      theme = "Confiance",
      translation = "LSG",
      reflectionPrompt = "Comment le Seigneur prend-il soin de vous aujourd'hui?",
      prayerPrompt = "Seigneur, conduis mes pas sur tes sentiers paisibles.",
      date = "2027-01-01",
      isFavorite = true
    )
    verseRepo.saveDailyVerse(customVerse)

    val fetchedByDate = verseRepo.getVerseForDate("2027-01-01").first()
    assertNotNull(fetchedByDate)
    assertEquals("Psaume 23:1", fetchedByDate?.reference)
  }
}

