package com.example.data.ai

import android.content.Context
import com.example.data.ai.gemini.GeminiApiClient
import com.example.data.ai.gemini.GeminiResult
import com.example.data.ai.model.EliMessage
import com.example.data.ai.model.EliQuotaState
import com.example.data.ai.verifier.BibleReferenceVerifier
import com.example.data.ai.verifier.VerifiedCitationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EliAssistantService(
  private val context: Context,
  private val geminiClient: GeminiApiClient = GeminiApiClient(context),
  private val quotaTracker: DailyQuotaTracker = DailyQuotaTracker(context)
) {

  val quotaState: Flow<EliQuotaState> = quotaTracker.quotaState

  private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRENCH)

  suspend fun askEli(
    userQuestion: String,
    history: List<Pair<String, Boolean>>,
    configuredModel: String = "gemini-2.5-flash"
  ): EliMessage {
    val timestamp = timeFormat.format(Date())

    // 1. Check daily quota. A request is only counted once Gemini answers,
    // so offline fallbacks and failed network calls do not consume the quota.
    val quota = quotaTracker.quotaState.first()
    if (quota.isExceeded) {
      return EliMessage(
        id = "msg_${System.currentTimeMillis()}",
        isUser = false,
        content = "Vous avez atteint votre limite quotidienne de ${quota.dailyLimit} questions avec Eli. Cette mesure permet d'assurer un accès équitable et mesuré. Votre quota sera réinitialisé à minuit. En attendant, vous pouvez continuer à méditer les plans de lecture et les passages bibliques hors-ligne !",
        timestamp = timestamp,
        isError = true
      )
    }

    // 2. Try Gemini API
    val geminiResult = geminiClient.askGemini(
      userPrompt = userQuestion,
      conversationHistory = history,
      configuredModel = configuredModel
    )

    return when (geminiResult) {
      is GeminiResult.Success -> {
        quotaTracker.recordRequest()
        parseAndEnrichResponse(
          rawContent = geminiResult.rawText,
          timestamp = timestamp,
          userQuestion = userQuestion
        )
      }
      is GeminiResult.Error -> {
        // Fallback gracefully to the verified offline pedagogical engine
        generateVerifiedOfflineResponse(
          question = userQuestion,
          timestamp = timestamp,
          fallbackReason = geminiResult.errorMessage
        )
      }
    }
  }

  private fun parseAndEnrichResponse(
    rawContent: String,
    timestamp: String,
    userQuestion: String
  ): EliMessage {
    // Look for references mentioned in the text and verify them against our verified canon
    val citations = mutableListOf<VerifiedCitationResult>()
    val knownReferences = listOf(
      "Éphésiens 2:8-9", "Romains 8:28", "Romains 5:1-2", "Jean 3:16",
      "Jean 14:6", "Philippiens 4:6-7", "Psaume 23:1-3", "2 Timothée 3:16-17",
      "Colossiens 3:13", "Proverbes 3:5-6", "Matthieu 28:19-20", "Jacques 1:5"
    )

    knownReferences.forEach { ref ->
      if (rawContent.contains(ref, ignoreCase = true) || rawContent.contains(ref.replace(" ", ""), ignoreCase = true)) {
        citations.add(BibleReferenceVerifier.verifyReference(ref))
      }
    }

    // Extract prayer if present
    var prayerText: String? = null
    if (rawContent.contains("Prière", ignoreCase = true) || rawContent.contains("Prions", ignoreCase = true)) {
      val lines = rawContent.lines()
      val prayerStartIndex = lines.indexOfFirst {
        it.contains("Prière", ignoreCase = true) || it.contains("Prions", ignoreCase = true)
      }
      if (prayerStartIndex != -1 && prayerStartIndex < lines.size - 1) {
        prayerText = lines.drop(prayerStartIndex + 1).joinToString("\n").trim()
      }
    }

    return EliMessage(
      id = "msg_${System.currentTimeMillis()}",
      isUser = false,
      content = rawContent,
      timestamp = timestamp,
      verifiedCitations = citations,
      suggestedPrayer = prayerText,
      suggestedQuestions = listOf(
        "Peux-tu m'expliquer le contexte historique de ce passage ?",
        "Comment appliquer cette vérité dans mes épreuves ?",
        "Propose-moi un plan de lecture de 7 jours sur ce thème"
      )
    )
  }

  fun generateVerifiedOfflineResponse(
    question: String,
    timestamp: String,
    fallbackReason: String? = null
  ): EliMessage {
    val q = question.lowercase()

    val response: EliMessage = when {
      // THEME: BAPTÊME (DIVERGENCES PROTESTANTES)
      q.contains("baptême") || q.contains("baptiser") -> {
        val citation = BibleReferenceVerifier.verifyReference("Matthieu 28:19-20")
        EliMessage(
          id = "msg_${System.currentTimeMillis()}",
          isUser = false,
          content = """Le baptême est une ordonnance instituée par Jésus-Christ dans la Grande Commission (Matthieu 28:19). Dans la Réforme protestante et le mouvement évangélique, le baptême est reconnu comme un signe visible d'une réalité spirituelle invisible, fondé sur l'œuvre accomplie par Christ.""",
          timestamp = timestamp,
          verifiedCitations = listOf(citation),
          theologicalPerspectives = """NUANCES THÉOLOGIQUES PROTESTANTES :
1. Le Baptême de croyants (Crédobaptisme - Évangéliques, Baptistes, Pentecôtistes) :
Le baptême doit obligatoirement suivre une profession de foi personnelle et consciente (Actes 8:36-38). Il symbolise l'ensevelissement avec Christ et la résurrection en nouveauté de vie par immersion (Romains 6:3-4).

2. Le Baptême d'alliance (Pédobaptisme - Réformés calvinistes, Presbytériens, Luthériens) :
Le baptême est administré aux enfants de croyants comme signe de l'Alliance de grâce de Dieu avec les familles, en continuité avec la circoncision dans l'Ancien Testament (Genèse 17:7, Actes 2:39). L'enfant sera ensuite appelé à confirmer personnellement cette foi.""",
          suggestedPrayer = "Seigneur Jésus, merci pour l'alliance de ta grâce. Que ma vie entière témoigne de ma consécration à ton nom et de mon engagement d'obéissance envers toi. Amen.",
          suggestedQuestions = listOf(
            "Quelle est la signification de Romains 6:3-4 ?",
            "Que signifie être né de nouveau selon Jean 3 ?",
            "Comment préparer un temps de prière avant un baptême ?"
          )
        )
      }

      // THEME: GRÂCE ET SALUT / JUSTIFICATION
      q.contains("salut") || q.contains("grâce") || q.contains("œuvres") || q.contains("sauvé") -> {
        val citation1 = BibleReferenceVerifier.verifyReference("Éphésiens 2:8-9")
        val citation2 = BibleReferenceVerifier.verifyReference("Romains 5:1-2")
        EliMessage(
          id = "msg_${System.currentTimeMillis()}",
          isUser = false,
          content = """Le cœur même de l'Évangile est que le salut est un don par pure grâce, entièrement gratuit de Dieu (Sola Gratia), reçu par le seul moyen de la foi vivante en Jésus-Christ (Sola Fide).
          
Nous ne sommes pas sauvés par nos bonnes œuvres morales ou religieuses; nos œuvres sont le fruit reconnaissant de cette grâce et du salut, et non son fondement. Sur la croix, Jésus-Christ a payé l'intégralité de notre dette spirituelle. Comme l'écrit l'apôtre Paul dans Éphésiens 2:8-9, c'est par la grâce que nous sommes sauvés, par le moyen de la foi, et cela ne vient pas de nous, c'est le don de Dieu.""",
          timestamp = timestamp,
          verifiedCitations = listOf(citation1, citation2),
          theologicalPerspectives = """PERSPECTIVES PROTESTANTES SUR LA PERSÉVÉRANCE :
- Perspective réformée (Calviniste) : « Persévérance des saints ». Ceux qui sont réellement régénérés par le Saint-Esprit sont gardés par la puissance souveraine de Dieu jusqu'à la fin (Jean 10:28-29).
- Perspective arminienne (Méthodiste / Évangélique classique) : Souligne la responsabilité continue du croyant de demeurer volontairement en communion avec Christ, avec l'avertissement contre l'abandon délibéré de la foi.""",
          suggestedPrayer = "Père éternel, je te loue de tout mon cœur car mon salut ne dépend pas de mes forces chancelantes mais du sacrifice parfait de ton Fils Jésus. Remplis-moi de gratitude et apprends-moi à marcher dans tes bonnes œuvres. Amen.",
          suggestedQuestions = listOf(
            "Comment distinguer la justification et la sanctification ?",
            "Que signifie Jacques 2:26 sur la foi sans les œuvres ?",
            "Puis-je avoir l'assurance inébranlable de mon salut ?"
          )
        )
      }

      // THEME: ROMAINS 8:28 / SOUFFRANCE ET SOUVERAINETÉ
      q.contains("romains 8") || q.contains("8:28") || q.contains("épreuve") || q.contains("pourquoi le mal") -> {
        val citation = BibleReferenceVerifier.verifyReference("Romains 8:28")
        EliMessage(
          id = "msg_${System.currentTimeMillis()}",
          isUser = false,
          content = """Romains 8:28 est l'un des versets les plus réconfortants et profonds des Écritures : « Nous savons que toutes choses concourent au bien de ceux qui aiment Dieu, de ceux qui sont appelés selon son dessein ».
          
1. Contexte littéraire : Paul ne dit pas que toute tragédie ou maladie est bonne en elle-même. Le mal reste le mal dans un monde déchu.
2. La promesse : C'est Dieu lui-même qui agit souverainement pour orchestrer même nos détours, nos larmes et nos épreuves afin d'accomplir un bien spirituel suprême : nous conformer à l'image de son Fils (Romains 8:29).
3. La certitude : Rien dans la création ne pourra nous séparer de l'amour de Dieu manifesté en Jésus-Christ notre Seigneur (v. 38-39).""",
          timestamp = timestamp,
          verifiedCitations = listOf(citation),
          suggestedPrayer = "Seigneur souverain, même lorsque mes yeux ne voient que des tempêtes et des questions sans réponse, je dépose ma confiance entre tes mains fidèles. Que ton Esprit soutienne ma foi. Amen.",
          suggestedQuestions = listOf(
            "Comment Paul vivait-il ses propres épreuves en prison ?",
            "Quelle est la différence entre l'épreuve et la tentation ?",
            "Une prière pour trouver la paix intérieure aujourd'hui"
          )
        )
      }

      // THEME: ANXIÉTÉ / PAIX / PRIÈRE
      q.contains("anxiété") || q.contains("angoisse") || q.contains("peur") || q.contains("paix") -> {
        val citation = BibleReferenceVerifier.verifyReference("Philippiens 4:6-7")
        EliMessage(
          id = "msg_${System.currentTimeMillis()}",
          isUser = false,
          content = """La Bible ne nie jamais la réalité de nos luttes intérieures, mais elle nous offre une thérapie divine : le transfert de nos fardeaux à Dieu par la prière.
          
Dans Philippiens 4:6-7, Paul nous donne une démarche concrète :
1. « En toute chose » : Aucun détail de votre vie n'est trop petit pour le Seigneur.
2. « Prières et supplications avec actions de grâces » : Se remémorer les bontés passées de Dieu avant même de demander.
3. « La paix de Dieu gardera vos cœurs » : Le terme grec décrit une garnison de soldats qui veille sur la forteresse de votre esprit.""",
          timestamp = timestamp,
          verifiedCitations = listOf(citation),
          suggestedPrayer = "Jésus, Prince de la paix, je dépose à tes pieds chaque pensée d'angoisse qui tourmente mon cœur. Enveloppe mon esprit de ta présence rassurante aujourd'hui. Amen.",
          suggestedQuestions = listOf(
            "Quels psaumes lire en période de découragement ?",
            "Comment méditer le Psaume 23 pas à pas ?",
            "Aide-moi à rédiger une prière pour ma famille"
          )
        )
      }

      // DEFAULT PASTORAL TEACHER
      else -> {
        val citation = BibleReferenceVerifier.verifyReference("2 Timothée 3:16-17")
        EliMessage(
          id = "msg_${System.currentTimeMillis()}",
          isUser = false,
          content = """En tant qu'assistant pédagogique biblique, je vous encourage à examiner toute question à la lumière des Saintes Écritures (Sola Scriptura).
          
« Toute Écriture est inspirée de Dieu, et utile pour enseigner, pour convaincre, pour corriger, pour instruire dans la justice » (2 Timothée 3:16).
Dans la tradition protestante et évangélique, nous sommes appelés à imiter les chrétiens de Bérée (Actes 17:11), qui vérifiaient chaque jour dans les Écritures si ce qu'on leur disait était exact.
          
Je me tiens à votre disposition pour :
• Étudier un passage biblique particulier.
• Vous aider à préparer une méditation ou une prière.
• Élucider un concept théologique (grâce, sanctification, nouvelle naissance, Saint-Esprit).""",
          timestamp = timestamp,
          verifiedCitations = listOf(citation),
          theologicalPerspectives = fallbackReason?.let { "NOTE TECHNIQUE : $it" },
          suggestedPrayer = "Seigneur, que ta Parole soit une lampe à mes pieds et une lumière sur mon sentier. Remplis-moi de ton Esprit de sagesse et de discernement. Amen.",
          suggestedQuestions = listOf(
            "Que dit la Bible sur la grâce et les œuvres ?",
            "Comment prier efficacement au quotidien ?",
            "Expliquer le Psaume 23"
          )
        )
      }
    }

    return response
  }
}
