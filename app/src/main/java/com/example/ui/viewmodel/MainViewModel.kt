package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.EliAssistantService
import com.example.data.ai.model.EliMessage
import com.example.data.ai.model.EliQuotaState
import com.example.data.ai.verifier.BibleReferenceVerifier
import com.example.data.auth.FirebaseAuthManager
import com.example.data.firestore.*
import com.example.data.local.AppDatabase
import com.example.data.local.SeedData
import com.example.data.local.entity.DailyVerseEntity
import com.example.data.model.*
import com.example.data.repository.DailyVerseRepository
import com.example.data.repository.SpiritualRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SyncState(val label: String) {
  IDLE("Prêt"),
  SYNCING("Synchronisation en cours..."),
  IN_SYNC("Données synchronisées avec Cloud Firestore"),
  OFFLINE("Mode hors-ligne (modifications locales conservées)"),
  ERROR("Erreur de synchronisation")
}

class MainViewModel @JvmOverloads constructor(
  application: Application,
  private val repository: SpiritualRepository = SpiritualRepository(application),
  private val eliService: EliAssistantService = EliAssistantService(application),
  val authManager: FirebaseAuthManager = FirebaseAuthManager(application),
  val firestoreSyncRepo: FirestoreSyncRepository = FirestoreSyncRepository(application),
  val dailyVerseRepository: DailyVerseRepository = DailyVerseRepository(AppDatabase.getDatabase(application).dailyVerseDao())
) : AndroidViewModel(application) {

  val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser
  val isGuestMode: StateFlow<Boolean> = authManager.isGuestMode
  val authError: StateFlow<String?> = authManager.authError

  private val _syncStatus = MutableStateFlow(SyncState.IDLE)
  val syncStatus: StateFlow<SyncState> = _syncStatus.asStateFlow()

  private val _lastSyncTime = MutableStateFlow<String?>(null)
  val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

  init {
    viewModelScope.launch {
      dailyVerseRepository.ensureInitialVersesLoaded(SeedData.getInitialDailyVerses())
    }

    viewModelScope.launch {
      authManager.currentUser.collect { user ->
        if (user != null && !authManager.isGuestMode.value) {
          syncWithCloud()
        } else {
          _syncStatus.value = SyncState.IDLE
        }
      }
    }
  }

  // Daily Verses (Room Database)
  val allDailyVerses: StateFlow<List<DailyVerseEntity>> = dailyVerseRepository.allDailyVerses
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val favoriteDailyVerses: StateFlow<List<DailyVerseEntity>> = dailyVerseRepository.favoriteVerses
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todayVerseOfTheDay: StateFlow<DailyVerseEntity?> = dailyVerseRepository.allDailyVerses.map { list ->
    val today = DailyVerseRepository.getTodayDate()
    list.firstOrNull { it.date == today } ?: list.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Reactive StateFlows from Room & DataStore
  val prayers: StateFlow<List<PrayerItem>> = repository.prayers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allDevotions: StateFlow<List<Devotion>> = repository.allDevotions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todayDevotion: StateFlow<Devotion?> = repository.todayDevotion
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val dailyVerse: StateFlow<Verse> = repository.todayDevotion.map { it.verse }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      Verse(
        id = "v_today",
        reference = "Éphésiens 2:8-9",
        text = "Car c'est par la grâce que vous êtes sauvés, par le moyen de la foi. Et cela ne vient pas de vous, c'est le don de Dieu.",
        book = "Éphésiens",
        chapter = 2,
        verseNumber = "8-9"
      )
    )

  val readingPlans: StateFlow<List<ReadingPlan>> = repository.readingPlans
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val goals: StateFlow<List<SpiritualGoal>> = repository.goals
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val weeklyReview: StateFlow<WeeklyReview?> = repository.weeklyReview
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val profile: StateFlow<UserProfile> = repository.profile
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

  val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
  val bibleBooks: List<BibleBook> = repository.bibleBooks

  // Bible screen state
  private val _selectedBookId = MutableStateFlow("PSA")
  val selectedBookId: StateFlow<String> = _selectedBookId.asStateFlow()

  private val _selectedChapterNumber = MutableStateFlow(23)
  val selectedChapterNumber: StateFlow<Int> = _selectedChapterNumber.asStateFlow()

  val currentBibleChapter: StateFlow<BibleChapter> = combine(
    _selectedBookId,
    _selectedChapterNumber
  ) { bookId, chapterNum ->
    repository.getChapterContent(bookId, chapterNum)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = repository.getChapterContent("PSA", 23)
  )

  // Prayer Filter & Search
  private val _prayerCategoryFilter = MutableStateFlow<PrayerCategory?>(null)
  val prayerCategoryFilter: StateFlow<PrayerCategory?> = _prayerCategoryFilter.asStateFlow()

  private val _showOnlyAnsweredPrayers = MutableStateFlow(false)
  val showOnlyAnsweredPrayers: StateFlow<Boolean> = _showOnlyAnsweredPrayers.asStateFlow()

  private val _prayerSearchQuery = MutableStateFlow("")
  val prayerSearchQuery: StateFlow<String> = _prayerSearchQuery.asStateFlow()

  // Selected Devotion for detailed view
  private val _selectedDevotion = MutableStateFlow<Devotion?>(null)
  val selectedDevotion: StateFlow<Devotion?> = _selectedDevotion.asStateFlow()

  // Selected Reading Plan for detail
  private val _selectedPlanId = MutableStateFlow<String?>("plan_foi_7")
  val selectedPlanId: StateFlow<String?> = _selectedPlanId.asStateFlow()

  val selectedPlanDays: StateFlow<List<ReadingPlanDay>> = _selectedPlanId.flatMapLatest { id ->
    if (id != null) repository.getPlanDays(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // --- PHASE 3: ELI ASSISTANT STATE & GEMINI ---

  val eliQuotaState: StateFlow<EliQuotaState> = eliService.quotaState
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EliQuotaState())

  private val _selectedAiModel = MutableStateFlow("gemini-2.5-flash")
  val selectedAiModel: StateFlow<String> = _selectedAiModel.asStateFlow()

  private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRENCH)

  private val initialEliGreeting = EliMessage(
    id = "msg_welcome_eli",
    isUser = false,
    content = "La paix du Christ soit avec vous ! Je m'appelle **Eli**, votre assistant biblique d'orientation protestante et évangélique.\n\nJe suis là pour vous accompagner dans votre étude des Écritures, expliquer le contexte historique des textes, répondre à vos interrogations théologiques ou vous aider à formuler une prière d'édification.\n\nPosez-moi votre question en toute simplicité !",
    timestamp = timeFormat.format(Date()),
    verifiedCitations = listOf(
      BibleReferenceVerifier.verifyReference("2 Timothée 3:16-17")
    ),
    suggestedQuestions = listOf(
      "Expliquer Romains 8:28 dans son contexte",
      "Que dit la Bible sur le baptême ?",
      "Comment avoir l'assurance de mon salut ?",
      "Aide-moi à prier pour mon anxiété"
    )
  )

  private val _eliMessages = MutableStateFlow<List<EliMessage>>(listOf(initialEliGreeting))
  val eliMessages: StateFlow<List<EliMessage>> = _eliMessages.asStateFlow()

  private val _chatInput = MutableStateFlow("")
  val chatInput: StateFlow<String> = _chatInput.asStateFlow()

  private val _isChatThinking = MutableStateFlow(false)
  val isChatThinking: StateFlow<Boolean> = _isChatThinking.asStateFlow()

  // --- MODULE 1: PRAYER ACTIONS ---

  fun setPrayerFilter(category: PrayerCategory?, onlyAnswered: Boolean = false) {
    _prayerCategoryFilter.value = category
    _showOnlyAnsweredPrayers.value = onlyAnswered
  }

  fun updatePrayerSearch(query: String) {
    _prayerSearchQuery.value = query
  }

  fun addPrayer(title: String, description: String, category: PrayerCategory, biblicalRef: String?) {
    if (title.isNotBlank()) {
      viewModelScope.launch {
        repository.addPrayer(title, description, category, biblicalRef)
      }
    }
  }

  fun updatePrayer(id: String, title: String, description: String, category: PrayerCategory, biblicalRef: String?) {
    if (title.isNotBlank()) {
      viewModelScope.launch {
        repository.updatePrayer(id, title, description, category, biblicalRef)
      }
    }
  }

  fun deletePrayer(id: String) {
    viewModelScope.launch {
      repository.deletePrayer(id)
    }
  }

  fun markPrayerAsAnswered(id: String, testimony: String) {
    viewModelScope.launch {
      repository.markPrayerAsAnswered(id, testimony)
    }
  }

  fun incrementPrayerCount(id: String) {
    viewModelScope.launch {
      repository.incrementPrayerCount(id)
    }
  }

  // --- MODULE 2: DEVOTION ACTIONS ---

  fun selectDevotionForDetail(devotion: Devotion) {
    _selectedDevotion.value = devotion
  }

  fun markDevotionCompleted(id: String, isCompleted: Boolean) {
    viewModelScope.launch {
      repository.markDevotionCompleted(id, isCompleted)
    }
  }

  // --- MODULE 3: READING PLAN ACTIONS ---

  fun selectReadingPlan(planId: String) {
    _selectedPlanId.value = planId
  }

  fun toggleReadingPlanDay(dayId: String, planId: String) {
    viewModelScope.launch {
      repository.togglePlanDayCompleted(dayId, planId)
    }
  }

  // --- MODULE 4: SPIRITUAL GOAL ACTIONS ---

  fun createGoal(
    title: String,
    description: String,
    category: GoalCategory,
    targetValue: Int,
    unit: String,
    frequency: GoalFrequency
  ) {
    if (title.isNotBlank() && targetValue > 0) {
      viewModelScope.launch {
        repository.createGoal(title, description, category, targetValue, unit, frequency)
      }
    }
  }

  fun updateGoalProgress(id: String, delta: Int) {
    viewModelScope.launch {
      repository.updateGoalProgress(id, delta)
    }
  }

  fun toggleGoalCompleted(id: String) {
    viewModelScope.launch {
      repository.toggleGoalCompleted(id)
    }
  }

  fun deleteGoal(id: String) {
    viewModelScope.launch {
      repository.deleteGoal(id)
    }
  }

  // --- BIBLE & PROFILE ACTIONS ---

  fun selectBibleBook(bookId: String) {
    _selectedBookId.value = bookId
    _selectedChapterNumber.value = 1
  }

  fun selectBibleChapter(chapter: Int) {
    _selectedChapterNumber.value = chapter
  }

  fun updateProfile(name: String, version: String, reminderTime: String, themeMode: ThemeMode) {
    viewModelScope.launch {
      repository.updateProfile(name, version, reminderTime, themeMode)
    }
  }

  // --- PHASE 3: ELI CHAT ACTIONS ---

  fun onChatInputChange(newText: String) {
    _chatInput.value = newText
  }

  fun setSelectedAiModel(model: String) {
    _selectedAiModel.value = model
  }

  fun sendEliQuestion(presetPrompt: String? = null) {
    val textToSend = presetPrompt ?: _chatInput.value
    if (textToSend.isBlank() || _isChatThinking.value) return

    if (presetPrompt == null) {
      _chatInput.value = ""
    }

    val timestamp = timeFormat.format(Date())
    val userMsg = EliMessage(
      id = "msg_user_${System.currentTimeMillis()}",
      isUser = true,
      content = textToSend,
      timestamp = timestamp
    )

    _eliMessages.update { it + userMsg }

    viewModelScope.launch {
      _isChatThinking.value = true

      // History pairs of (text, isUser)
      val history = _eliMessages.value.map { Pair(it.content, it.isUser) }

      val response = eliService.askEli(
        userQuestion = textToSend,
        history = history,
        configuredModel = _selectedAiModel.value
      )

      _eliMessages.update { it + response }
      _isChatThinking.value = false
    }
  }

  fun reportEliMessage(messageId: String, reason: String) {
    _eliMessages.update { list ->
      list.map { if (it.id == messageId) it.copy(isReported = true) else it }
    }
  }

  fun clearEliConversation() {
    _eliMessages.value = listOf(initialEliGreeting)
  }

  // --- PHASE 4: CLOUD SYNC & AUTHENTICATION ACTIONS ---

  fun syncWithCloud() {
    val user = authManager.currentUser.value
    if (user == null || authManager.isGuestMode.value) {
      _syncStatus.value = SyncState.OFFLINE
      return
    }

    viewModelScope.launch {
      _syncStatus.value = SyncState.SYNCING
      try {
        // 1. Sync User Profile
        val currentProfile = repository.profile.first()
        firestoreSyncRepo.saveUserProfile(
          FirestoreUserProfile(
            userId = user.uid,
            displayName = currentProfile.name,
            email = user.email,
            denominationalAffiliation = currentProfile.preferredVersion,
            dailyNotificationTime = currentProfile.prayerReminderTime,
            offlineModeEnabled = false
          )
        )

        // 2. Sync Local Prayers to Firestore
        val localPrayers = repository.prayers.first()
        for (p in localPrayers) {
          firestoreSyncRepo.savePrayer(
            FirestorePrayer(
              id = p.id,
              userId = user.uid,
              title = p.title,
              description = p.description,
              category = p.category.name,
              isAnswered = p.isAnswered,
              answeredDate = p.answeredDate,
              answeredNote = p.testimony,
              isPrivate = p.isPrivate,
              prayCount = p.prayerCount
            )
          )
        }

        // 3. Sync Goals to Firestore
        val localGoals = repository.goals.first()
        for (g in localGoals) {
          firestoreSyncRepo.saveGoal(
            FirestoreGoal(
              id = g.id,
              userId = user.uid,
              title = g.title,
              description = g.description,
              category = g.category.label,
              targetDaysPerWeek = g.targetCount,
              completedDaysThisWeek = g.currentCount,
              streakWeeks = g.streakDays
            )
          )
        }

        // 4. Sync Preferences to Firestore
        firestoreSyncRepo.savePreferences(
          FirestorePreferences(
            userId = user.uid,
            darkMode = (currentProfile.themeMode == ThemeMode.DARK),
            biblicalVersion = currentProfile.preferredVersion,
            selectedAiModel = _selectedAiModel.value
          )
        )

        _syncStatus.value = SyncState.IN_SYNC
        _lastSyncTime.value = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date())
      } catch (e: Exception) {
        android.util.Log.w("MainViewModel", "Sync exception (expected when offline): ${e.message}")
        _syncStatus.value = SyncState.OFFLINE
      }
    }
  }

  fun signInWithGoogle(activity: android.app.Activity, onSuccess: () -> Unit, onError: (String) -> Unit) {
    authManager.signInWithGoogle(activity, viewModelScope, onSuccess = {
      syncWithCloud()
      onSuccess()
    }, onError = onError)
  }

  fun signOut() {
    authManager.signOut(viewModelScope) {
      _syncStatus.value = SyncState.IDLE
    }
  }

  fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
    authManager.deleteAccount(firestoreSyncRepo, viewModelScope, onSuccess = {
      viewModelScope.launch {
        repository.clearLocalUserData()
      }
      _syncStatus.value = SyncState.IDLE
      onSuccess()
    }, onError = onError)
  }

  // --- DAILY VERSE (ROOM) ACTIONS ---

  fun toggleDailyVerseFavorite(verseId: String, isFavorite: Boolean) {
    viewModelScope.launch {
      dailyVerseRepository.toggleFavorite(verseId, isFavorite)
    }
  }

  fun saveCustomDailyVerse(
    reference: String,
    text: String,
    book: String,
    chapter: Int,
    verseNumber: String,
    theme: String,
    reflection: String,
    prayer: String
  ) {
    viewModelScope.launch {
      val today = DailyVerseRepository.getTodayDate()
      val newVerse = DailyVerseEntity(
        id = "custom_${System.currentTimeMillis()}",
        reference = reference.trim(),
        text = text.trim(),
        book = book.trim(),
        chapter = chapter,
        verseNumber = verseNumber.trim(),
        theme = theme.ifBlank { "Méditation" },
        reflectionPrompt = reflection.trim(),
        prayerPrompt = prayer.trim(),
        date = today,
        isFavorite = true,
        savedAtTimestamp = System.currentTimeMillis()
      )
      dailyVerseRepository.saveDailyVerse(newVerse)
    }
  }

  fun deleteDailyVerse(verseId: String) {
    viewModelScope.launch {
      dailyVerseRepository.deleteDailyVerse(verseId)
    }
  }
}
