package com.example.data.repository

import android.content.Context
import com.example.data.bible.BibleRepository
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.entity.DevotionEntity
import com.example.data.local.entity.PrayerEntity
import com.example.data.local.entity.ReadingPlanDayEntity
import com.example.data.local.entity.SpiritualGoalEntity
import com.example.data.firestore.FirestoreGoal
import com.example.data.firestore.FirestorePrayer
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SpiritualRepository(
  private val context: Context,
  private val database: AppDatabase = AppDatabase.getDatabase(context),
  private val preferences: UserPreferencesRepository = UserPreferencesRepository(context)
) {

  private val prayerDao = database.prayerDao()
  private val bibleRepository = BibleRepository(context)
  private val devotionDao = database.devotionDao()
  private val readingPlanDao = database.readingPlanDao()
  private val goalDao = database.spiritualGoalDao()

  // Reactive Flow of Prayers mapped to domain models
  val prayers: Flow<List<PrayerItem>> = prayerDao.getAllPrayers().map { list ->
    list.map { entity ->
      PrayerItem(
        id = entity.id,
        title = entity.title,
        description = entity.description,
        category = PrayerCategory.fromCode(entity.category),
        biblicalReference = entity.biblicalReference,
        createdAtTimestamp = entity.createdAtTimestamp,
        isAnswered = entity.isAnswered,
        answeredDate = entity.answeredDateTimestamp?.let { formatTimestamp(it) },
        testimony = entity.testimony,
        prayerCount = entity.prayerCount,
        isPrivate = entity.isPrivate
      )
    }
  }

  // Reactive Flow of 14 Devotions
  val allDevotions: Flow<List<Devotion>> = devotionDao.getAllDevotions().map { list ->
    list.map { entity ->
      Devotion(
        id = entity.id,
        dayOrder = entity.dayOrder,
        dateFormatted = "Jour ${entity.dayOrder} sur 14",
        title = entity.title,
        verse = Verse(
          id = "verse_${entity.id}",
          reference = entity.scriptureReference,
          text = entity.verseText,
          book = entity.scriptureReference.substringBefore(" "),
          chapter = 1,
          verseNumber = ""
        ),
        contextExplanation = entity.contextExplanation,
        reflectionQuestion = entity.reflectionQuestions,
        practicalAction = entity.practicalAction,
        prayer = entity.guidedPrayer,
        isCompleted = entity.isCompleted,
        completedDate = entity.completedAtTimestamp?.let { formatTimestamp(it) }
      )
    }
  }

  // Today's devotion is default day 1 or the first uncompleted devotion
  val todayDevotion: Flow<Devotion> = allDevotions.map { list ->
    list.firstOrNull { !it.isCompleted } ?: list.firstOrNull() ?: Devotion(
      id = "fallback",
      dayOrder = 1,
      dateFormatted = "Aujourd'hui",
      title = "La grâce seule qui sauve",
      verse = Verse("v1", "Éphésiens 2:8-9", "Car c'est par la grâce que vous êtes sauvés...", "Éphésiens", 2, "8-9"),
      contextExplanation = "L'apôtre Paul rappelle que le salut est un don immérité de Dieu.",
      reflectionQuestion = "Comment la grâce transforme-t-elle votre journée ?",
      practicalAction = "Prenez 5 minutes de louange gratuite.",
      prayer = "Seigneur, merci pour ta grâce inébranlable.",
      isCompleted = false
    )
  }

  // Reactive Flow of Reading Plans with completed progress
  val readingPlans: Flow<List<ReadingPlan>> = readingPlanDao.getAllPlans().flatMapLatest { plans ->
    if (plans.isEmpty()) {
      flowOf(emptyList())
    } else {
      combine(
        plans.map { plan ->
          readingPlanDao.getCompletedDaysCount(plan.id).map { count ->
            ReadingPlan(
              id = plan.id,
              title = plan.title,
              description = plan.description,
              durationDays = plan.durationDays,
              category = plan.category,
              isEnrolled = plan.isEnrolled,
              completedDaysCount = count
            )
          }
        }
      ) { it.toList() }
    }
  }

  // Reactive Flow of Goals
  val goals: Flow<List<SpiritualGoal>> = goalDao.getAllGoals().map { list ->
    list.map { entity ->
      SpiritualGoal(
        id = entity.id,
        title = entity.title,
        description = entity.description,
        category = GoalCategory.entries.firstOrNull { it.name == entity.category } ?: GoalCategory.PRIERE,
        targetCount = entity.targetValue,
        currentCount = entity.currentValue,
        unit = entity.unit,
        frequency = GoalFrequency.fromCode(entity.frequency),
        streakDays = entity.streakDays,
        isCompleted = entity.isCompleted
      )
    }
  }

  // Profile preferences
  val profile: Flow<UserProfile> = preferences.userProfileFlow
  val favoritedVerses: Flow<Set<String>> = preferences.favoritedVerses
  val onboardingCompleted: Flow<Boolean> = preferences.onboardingCompleted

  // Weekly Review calculation
  val weeklyReview: Flow<WeeklyReview> = combine(goals, prayers, allDevotions) { gList, pList, dList ->
    val completedG = gList.count { it.isCompleted }
    val totalG = gList.size
    val totalP = pList.sumOf { it.prayerCount }
    val answeredP = pList.count { it.isAnswered }
    val completedD = dList.count { it.isCompleted }

    val encouragement = when {
      completedG == totalG && totalG > 0 ->
        "Gloire à Dieu ! Vous avez persévéré avec fidélité cette semaine. Rappelez-vous que votre valeur découle de l'amour inconditionnel du Christ sur la croix, et non de vos performances. Continuez d'avancer d'un cœur joyeux !"
      completedG > 0 ->
        "Chaque pas compte devant le Seigneur ! « Celui qui a commencé en vous cette bonne œuvre la rendra parfaite pour le jour de Jésus-Christ » (Phil 1:6). Que Sa grâce vous fortifie pour la semaine à venir."
      else ->
        "La grâce de Dieu se renouvelle chaque matin ! Ne soyez point découragé : venez simplement à Jésus tel que vous êtes, car Son joug est doux et Son fardeau léger."
    }

    WeeklyReview(
      totalGoals = totalG,
      completedGoals = completedG,
      totalPrayersCount = totalP,
      answeredPrayersCount = answeredP,
      devotionCompletedCount = completedD,
      encouragementMessage = encouragement
    )
  }

  // --- MODULE 1 : PRAYER METHODS ---

  suspend fun addPrayer(
    title: String,
    description: String,
    category: PrayerCategory,
    biblicalRef: String?
  ) {
    val entity = PrayerEntity(
      id = "prayer_${System.currentTimeMillis()}",
      title = title,
      description = description,
      category = category.code,
      biblicalReference = biblicalRef?.takeIf { it.isNotBlank() },
      createdAtTimestamp = System.currentTimeMillis(),
      isAnswered = false,
      prayerCount = 1,
      isPrivate = true
    )
    prayerDao.insertPrayer(entity)
  }

  suspend fun updatePrayer(
    id: String,
    title: String,
    description: String,
    category: PrayerCategory,
    biblicalRef: String?
  ) {
    val existing = prayerDao.getPrayerById(id) ?: return
    val updated = existing.copy(
      title = title,
      description = description,
      category = category.code,
      biblicalReference = biblicalRef?.takeIf { it.isNotBlank() }
    )
    prayerDao.updatePrayer(updated)
  }

  suspend fun deletePrayer(id: String) {
    prayerDao.deletePrayerById(id)
  }

  suspend fun markPrayerAsAnswered(id: String, testimony: String) {
    val existing = prayerDao.getPrayerById(id) ?: return
    val updated = existing.copy(
      isAnswered = true,
      answeredDateTimestamp = System.currentTimeMillis(),
      testimony = testimony
    )
    prayerDao.updatePrayer(updated)
  }

  suspend fun incrementPrayerCount(id: String) {
    val existing = prayerDao.getPrayerById(id) ?: return
    val updated = existing.copy(prayerCount = existing.prayerCount + 1)
    prayerDao.updatePrayer(updated)
  }

  // --- MODULE 2 : DEVOTIONS METHODS ---

  suspend fun markDevotionCompleted(id: String, isCompleted: Boolean) {
    val devotions = devotionDao.getAllDevotions().first()
    val target = devotions.firstOrNull { it.id == id } ?: return
    val updated = target.copy(
      isCompleted = isCompleted,
      completedAtTimestamp = if (isCompleted) System.currentTimeMillis() else null
    )
    devotionDao.updateDevotion(updated)
    if (isCompleted) {
      preferences.incrementStreak()
      preferences.incrementChaptersRead()
    }
  }

  // --- MODULE 3 : READING PLANS METHODS ---

  fun getPlanDays(planId: String): Flow<List<ReadingPlanDay>> {
    return readingPlanDao.getDaysForPlan(planId).map { days ->
      days.map { d ->
        ReadingPlanDay(
          id = d.id,
          planId = d.planId,
          dayNumber = d.dayNumber,
          title = d.title,
          scriptureReference = d.scriptureReference,
          summary = d.summary,
          isCompleted = d.isCompleted
        )
      }
    }
  }

  suspend fun togglePlanDayCompleted(dayId: String, planId: String) {
    val days = readingPlanDao.getDaysForPlan(planId).first()
    val target = days.firstOrNull { it.id == dayId } ?: return
    val updated = target.copy(
      isCompleted = !target.isCompleted,
      completedAtTimestamp = if (!target.isCompleted) System.currentTimeMillis() else null
    )
    readingPlanDao.updatePlanDay(updated)
    if (updated.isCompleted) {
      preferences.incrementChaptersRead()
    }
  }

  // --- MODULE 4 : SPIRITUAL GOALS METHODS ---

  suspend fun createGoal(
    title: String,
    description: String,
    category: GoalCategory,
    targetValue: Int,
    unit: String,
    frequency: GoalFrequency
  ) {
    val entity = SpiritualGoalEntity(
      id = "goal_${System.currentTimeMillis()}",
      title = title,
      description = description,
      category = category.name,
      targetValue = targetValue,
      currentValue = 0,
      unit = unit,
      frequency = frequency.code,
      streakDays = 0,
      isCompleted = false,
      lastUpdatedTimestamp = System.currentTimeMillis()
    )
    goalDao.insertGoal(entity)
  }

  suspend fun updateGoalProgress(id: String, delta: Int) {
    val goal = goalDao.getGoalById(id) ?: return
    val newCurrent = (goal.currentValue + delta).coerceIn(0, goal.targetValue)
    val isDone = newCurrent >= goal.targetValue
    val newStreak = if (isDone && !goal.isCompleted) goal.streakDays + 1 else goal.streakDays
    val updated = goal.copy(
      currentValue = newCurrent,
      isCompleted = isDone,
      streakDays = newStreak,
      lastUpdatedTimestamp = System.currentTimeMillis()
    )
    goalDao.updateGoal(updated)
  }

  suspend fun toggleGoalCompleted(id: String) {
    val goal = goalDao.getGoalById(id) ?: return
    val nextState = !goal.isCompleted
    val nextValue = if (nextState) goal.targetValue else 0
    val updated = goal.copy(
      isCompleted = nextState,
      currentValue = nextValue,
      streakDays = if (nextState) goal.streakDays + 1 else goal.streakDays,
      lastUpdatedTimestamp = System.currentTimeMillis()
    )
    goalDao.updateGoal(updated)
  }

  suspend fun deleteGoal(id: String) {
    goalDao.deleteGoalById(id)
  }

  // --- USER PROFILE & PREFERENCES ---

  suspend fun updateProfile(name: String, version: String, reminderTime: String, themeMode: ThemeMode, geminiApiKey: String = "") {
    preferences.updateProfile(name, version, reminderTime, themeMode, geminiApiKey)
  }

  suspend fun toggleFavoriteVerse(verseKey: String, isFavorite: Boolean) {
    if (isFavorite) {
      preferences.addFavoriteVerse(verseKey)
    } else {
      preferences.removeFavoriteVerse(verseKey)
    }
  }

  suspend fun markOnboardingCompleted() {
    preferences.markOnboardingCompleted()
  }

  // --- BIBLE CANON (66 Books) ---
  val bibleBooks: List<BibleBook> = listOf(
    // Ancien Testament
    BibleBook("GEN", "Genèse", Testament.ANCIEN, 50, "Pentateuque"),
    BibleBook("EXO", "Exode", Testament.ANCIEN, 40, "Pentateuque"),
    BibleBook("LEV", "Lévitique", Testament.ANCIEN, 27, "Pentateuque"),
    BibleBook("NOM", "Nombres", Testament.ANCIEN, 36, "Pentateuque"),
    BibleBook("DEU", "Deutéronome", Testament.ANCIEN, 34, "Pentateuque"),
    BibleBook("JOS", "Josué", Testament.ANCIEN, 24, "Livres Historiques"),
    BibleBook("JUG", "Juges", Testament.ANCIEN, 21, "Livres Historiques"),
    BibleBook("RUT", "Ruth", Testament.ANCIEN, 4, "Livres Historiques"),
    BibleBook("1SA", "1 Samuel", Testament.ANCIEN, 31, "Livres Historiques"),
    BibleBook("2SA", "2 Samuel", Testament.ANCIEN, 24, "Livres Historiques"),
    BibleBook("1RO", "1 Rois", Testament.ANCIEN, 22, "Livres Historiques"),
    BibleBook("2RO", "2 Rois", Testament.ANCIEN, 25, "Livres Historiques"),
    BibleBook("1CH", "1 Chroniques", Testament.ANCIEN, 29, "Livres Historiques"),
    BibleBook("2CH", "2 Chroniques", Testament.ANCIEN, 36, "Livres Historiques"),
    BibleBook("ESD", "Esdras", Testament.ANCIEN, 10, "Livres Historiques"),
    BibleBook("NEH", "Néhémie", Testament.ANCIEN, 13, "Livres Historiques"),
    BibleBook("EST", "Esther", Testament.ANCIEN, 10, "Livres Historiques"),
    BibleBook("JOB", "Job", Testament.ANCIEN, 42, "Poésie & Sagesse"),
    BibleBook("PSA", "Psaumes", Testament.ANCIEN, 150, "Poésie & Sagesse"),
    BibleBook("PRO", "Proverbes", Testament.ANCIEN, 31, "Poésie & Sagesse"),
    BibleBook("ECC", "Ecclésiaste", Testament.ANCIEN, 12, "Poésie & Sagesse"),
    BibleBook("CAN", "Cantique des Cantiques", Testament.ANCIEN, 8, "Poésie & Sagesse"),
    BibleBook("ESA", "Ésaïe", Testament.ANCIEN, 66, "Grands Prophètes"),
    BibleBook("JER", "Jérémie", Testament.ANCIEN, 52, "Grands Prophètes"),
    BibleBook("LAM", "Lamentations", Testament.ANCIEN, 5, "Grands Prophètes"),
    BibleBook("EZE", "Ézéchiel", Testament.ANCIEN, 48, "Grands Prophètes"),
    BibleBook("DAN", "Daniel", Testament.ANCIEN, 12, "Grands Prophètes"),
    BibleBook("OSE", "Osée", Testament.ANCIEN, 14, "Petits Prophètes"),
    BibleBook("JOE", "Joël", Testament.ANCIEN, 3, "Petits Prophètes"),
    BibleBook("AMO", "Amos", Testament.ANCIEN, 9, "Petits Prophètes"),
    BibleBook("ABD", "Abdias", Testament.ANCIEN, 1, "Petits Prophètes"),
    BibleBook("JON", "Jonas", Testament.ANCIEN, 4, "Petits Prophètes"),
    BibleBook("MIC", "Michée", Testament.ANCIEN, 7, "Petits Prophètes"),
    BibleBook("NAH", "Nahum", Testament.ANCIEN, 3, "Petits Prophètes"),
    BibleBook("HAB", "Habacuc", Testament.ANCIEN, 3, "Petits Prophètes"),
    BibleBook("SOP", "Sophonie", Testament.ANCIEN, 3, "Petits Prophètes"),
    BibleBook("AGG", "Aggée", Testament.ANCIEN, 2, "Petits Prophètes"),
    BibleBook("ZAC", "Zacharie", Testament.ANCIEN, 14, "Petits Prophètes"),
    BibleBook("MAL", "Malachie", Testament.ANCIEN, 3, "Petits Prophètes"),

    // Nouveau Testament
    BibleBook("MAT", "Matthieu", Testament.NOUVEAU, 28, "Évangiles"),
    BibleBook("MAR", "Marc", Testament.NOUVEAU, 16, "Évangiles"),
    BibleBook("LUC", "Luc", Testament.NOUVEAU, 24, "Évangiles"),
    BibleBook("JEA", "Jean", Testament.NOUVEAU, 21, "Évangiles"),
    BibleBook("ACT", "Actes", Testament.NOUVEAU, 28, "Histoire de l'Église"),
    BibleBook("ROM", "Romains", Testament.NOUVEAU, 16, "Épîtres de Paul"),
    BibleBook("1CO", "1 Corinthiens", Testament.NOUVEAU, 16, "Épîtres de Paul"),
    BibleBook("2CO", "2 Corinthiens", Testament.NOUVEAU, 13, "Épîtres de Paul"),
    BibleBook("GAL", "Galates", Testament.NOUVEAU, 6, "Épîtres de Paul"),
    BibleBook("EPH", "Éphésiens", Testament.NOUVEAU, 6, "Épîtres de Paul"),
    BibleBook("PHI", "Philippiens", Testament.NOUVEAU, 4, "Épîtres de Paul"),
    BibleBook("COL", "Colossiens", Testament.NOUVEAU, 4, "Épîtres de Paul"),
    BibleBook("1TH", "1 Thessaloniciens", Testament.NOUVEAU, 5, "Épîtres de Paul"),
    BibleBook("2TH", "2 Thessaloniciens", Testament.NOUVEAU, 3, "Épîtres de Paul"),
    BibleBook("1TI", "1 Timothée", Testament.NOUVEAU, 6, "Épîtres Pastorales"),
    BibleBook("2TI", "2 Timothée", Testament.NOUVEAU, 4, "Épîtres Pastorales"),
    BibleBook("TIT", "Tite", Testament.NOUVEAU, 3, "Épîtres Pastorales"),
    BibleBook("PHM", "Philémon", Testament.NOUVEAU, 1, "Épîtres de Paul"),
    BibleBook("HEB", "Hébreux", Testament.NOUVEAU, 13, "Épîtres Générales"),
    BibleBook("JAC", "Jacques", Testament.NOUVEAU, 5, "Épîtres Générales"),
    BibleBook("1PI", "1 Pierre", Testament.NOUVEAU, 5, "Épîtres Générales"),
    BibleBook("2PI", "2 Pierre", Testament.NOUVEAU, 3, "Épîtres Générales"),
    BibleBook("1JE", "1 Jean", Testament.NOUVEAU, 5, "Épîtres Générales"),
    BibleBook("2JE", "2 Jean", Testament.NOUVEAU, 1, "Épîtres Générales"),
    BibleBook("3JE", "3 Jean", Testament.NOUVEAU, 1, "Épîtres Générales"),
    BibleBook("JUD", "Jude", Testament.NOUVEAU, 1, "Épîtres Générales"),
    BibleBook("APO", "Apocalypse", Testament.NOUVEAU, 22, "Prophétie")
  )

  /**
   * Reads a chapter from the bundled Louis Segond 1910 asset.
   * Returns null when the book or chapter does not exist.
   */
  suspend fun getChapterContent(bookId: String, chapterNumber: Int): BibleChapter? {
    val book = bibleBooks.firstOrNull { it.id == bookId } ?: return null
    if (chapterNumber !in 1..book.chaptersCount) return null
    return bibleRepository.getChapter(book.id, book.name, chapterNumber)
  }

  // Pastoral Assistant Logic
  private val _chatMessages = MutableStateFlow(
    listOf(
      ChatMessage(
        id = "m1",
        isUser = false,
        content = "La paix du Seigneur soit avec vous. Je suis votre assistant spirituel biblique. Vous pouvez me poser des questions sur un passage des Écritures, demander un conseil pastoral fondé sur la Parole de Dieu, ou chercher une prière d'encouragement.",
        timestamp = "09:00",
        biblicalCitations = listOf("Psaume 119:105", "Jacques 1:5"),
        prayerPrompt = "Voulez-vous que nous priions pour une situation particulière ?"
      )
    )
  )
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  fun sendUserMessage(question: String): ChatMessage {
    val userMsg = ChatMessage(
      id = "msg_${System.currentTimeMillis()}",
      isUser = true,
      content = question,
      timestamp = "À l'instant"
    )
    _chatMessages.update { it + userMsg }

    val assistantResponse = generateBiblicalResponse(question)
    _chatMessages.update { it + assistantResponse }
    return assistantResponse
  }

  private fun generateBiblicalResponse(question: String): ChatMessage {
    val q = question.lowercase()
    val (content, citations, prayer) = when {
      q.contains("angoisse") || q.contains("peur") || q.contains("anxiété") || q.contains("paix") -> {
        Triple(
          "Face à l'angoisse et aux tempêtes de la vie, la Bible nous rappelle constamment que la présence de Dieu est notre plus sûr abri. Dans Philippiens 4:6-7, l'apôtre Paul nous exhorte à ne nous inquiéter de rien, mais à transformer chaque souci en prière avec reconnaissance. Jésus a dit : « Je vous laisse la paix, je vous donne ma paix. Je ne vous donne pas comme le monde donne. Que votre cœur ne se trouble point, et ne s'alarme point » (Jean 14:27).",
          listOf("Philippiens 4:6-7", "Jean 14:27", "Psaume 91:1-2"),
          "Seigneur Jésus, Prince de la Paix, je remets entre tes mains mes angoisses et mes craintes. Apaise mon cœur agité, et que ta paix souveraine règne sur mes pensées aujourd'hui. Amen."
        )
      }
      q.contains("pardon") || q.contains("pardonner") || q.contains("rancune") -> {
        Triple(
          "Le pardon est au cœur de l'Évangile. Nous pardonnons non pas parce que l'autre mérite d'être pardonné, mais parce que le Christ nous a pardonnés sans condition sur la croix (Éphésiens 4:32). Le pardon n'est pas un sentiment instantané, c'est une décision d'obéissance qui libère notre propre cœur des chaînes de l'amertume.",
          listOf("Éphésiens 4:32", "Colossiens 3:13", "Matthieu 6:14-15"),
          "Père très saint, toi qui m'as tant pardonné par le sacrifice de Jésus, donne-moi la force d'abandonner toute rancœur. Accorde-moi un cœur doux et prêt à bénir. Amen."
        )
      }
      q.contains("romains 8") || q.contains("8:28") || q.contains("épreuve") -> {
        Triple(
          "Romains 8:28 proclame une vérité magnifique : « Nous savons que toutes choses concourent au bien de ceux qui aiment Dieu, de ceux qui sont appelés selon son dessein ». L'apôtre n'affirme pas que toutes les choses qui nous arrivent sont bonnes en elles-mêmes, mais que le Dieu souverain est capable de tisser même nos épreuves pour accomplir un bien spirituel éternel.",
          listOf("Romains 8:28", "Romains 8:31-39", "Genèse 50:20"),
          "Seigneur, même lorsque je ne comprends pas les chemins par lesquels tu me conduis, je choisis de faire confiance à ta souveraineté bienveillante. Amen."
        )
      }
      else -> {
        Triple(
          "L'Écriture déclare : « Ta parole est une lampe à mes pieds, et une lumière sur mon sentier » (Psaume 119:105). Dans toute décision, dans la joie comme dans l'affliction, le Seigneur vous invite à chercher Sa volonté dans la prière et l'écoute de Son Esprit.",
          listOf("Psaume 119:105", "Proverbes 3:5-6", "Jacques 1:5"),
          "Éternel mon Dieu, éclaire ma route par ta Sainte Parole. Que chacune de mes paroles et chacun de mes actes glorifient ton saint nom. Au nom de Jésus, Amen."
        )
      }
    }

    return ChatMessage(
      id = "msg_${System.currentTimeMillis() + 1}",
      isUser = false,
      content = content,
      timestamp = "À l'instant",
      biblicalCitations = citations,
      prayerPrompt = prayer
    )
  }

  // --- CLOUD SYNC & LOCAL CACHE OPERATIONS ---

  suspend fun clearLocalUserData() {
    prayerDao.deleteAllPrayers()
    goalDao.deleteAllGoals()
    preferences.clearUserData()
  }

  suspend fun insertRemotePrayers(remotePrayers: List<FirestorePrayer>) {
    val entities = remotePrayers.map { p ->
      PrayerEntity(
        id = p.id,
        title = p.title,
        description = p.description ?: "",
        category = p.category,
        biblicalReference = "",
        createdAtTimestamp = p.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
        isAnswered = p.isAnswered,
        answeredDateTimestamp = if (p.isAnswered) System.currentTimeMillis() else null,
        testimony = p.answeredNote ?: "",
        prayerCount = p.prayCount,
        isPrivate = p.isPrivate
      )
    }
    prayerDao.insertAll(entities)
  }

  suspend fun insertRemoteGoals(remoteGoals: List<FirestoreGoal>) {
    val entities = remoteGoals.map { g ->
      SpiritualGoalEntity(
        id = g.id,
        title = g.title,
        description = g.description ?: "",
        category = GoalCategory.entries.firstOrNull {
          it.name == g.category || it.label == g.category
        }?.name ?: GoalCategory.PRIERE.name,
        targetValue = g.targetDaysPerWeek,
        currentValue = g.completedDaysThisWeek,
        unit = "jours/semaine",
        frequency = "HEBDOMADAIRE",
        streakDays = g.streakWeeks,
        isCompleted = (g.completedDaysThisWeek >= g.targetDaysPerWeek),
        lastUpdatedTimestamp = g.updatedAt?.toDate()?.time ?: System.currentTimeMillis()
      )
    }
    goalDao.insertAll(entities)
  }
}
