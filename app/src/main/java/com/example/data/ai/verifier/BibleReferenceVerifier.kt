package com.example.data.ai.verifier

data class VerifiedPassage(
  val reference: String,
  val book: String,
  val chapter: Int,
  val verses: String,
  val exactText: String,
  val historicalContext: String,
  val theologicalCategory: String
)

data class VerifiedCitationResult(
  val reference: String,
  val exactText: String?,
  val historicalContext: String?,
  val isVerified: Boolean,
  val warningNote: String? = null
)

object BibleReferenceVerifier {

  // Authorized and verified Scripture repository (Canon protestant)
  val verifiedPassages: List<VerifiedPassage> = listOf(
    VerifiedPassage(
      reference = "Éphésiens 2:8-9",
      book = "Éphésiens",
      chapter = 2,
      verses = "8-9",
      exactText = "Car c'est par la grâce que vous êtes sauvés, par le moyen de la foi. Et cela ne vient pas de vous, c'est le don de Dieu. Ce n'est point par les œuvres, afin que personne ne se glorifie.",
      historicalContext = "Écrit par Paul aux chrétiens d'Asie Mineure vers 60-62 ap. J.-C. lors de sa captivité à Rome. Il souligne le fondement du salut immérité face aux tentations de justice par les œuvres.",
      theologicalCategory = "Salut par Grâce"
    ),
    VerifiedPassage(
      reference = "Romains 8:28",
      book = "Romains",
      chapter = 8,
      verses = "28",
      exactText = "Nous savons, du reste, que toutes choses concourent au bien de ceux qui aiment Dieu, de ceux qui sont appelés selon son dessein.",
      historicalContext = "Écrit vers 57 ap. J.-C. depuis Corinthe. Paul conclut la section sur la souffrance présente et la gloire à venir, rappelant que Dieu est souverain au milieu de nos épreuves.",
      theologicalCategory = "Providence & Souveraineté"
    ),
    VerifiedPassage(
      reference = "Romains 5:1-2",
      book = "Romains",
      chapter = 5,
      verses = "1-2",
      exactText = "Étant donc justifiés par la foi, nous avons la paix avec Dieu par notre Seigneur Jésus-Christ, à qui nous devons d'avoir eu par la foi accès à cette grâce, dans laquelle nous demeurons fermes.",
      historicalContext = "Paul expose la paix juridique et relationnelle acquise par la foi en Jésus, sans intermédiaire sacerdotal humain.",
      theologicalCategory = "Justification par la Foi"
    ),
    VerifiedPassage(
      reference = "Jean 3:16",
      book = "Jean",
      chapter = 3,
      verses = "16",
      exactText = "Car Dieu a tant aimé le monde qu'il a donné son Fils unique, afin que quiconque croit en lui ne périsse point, mais qu'il ait la vie éternelle.",
      historicalContext = "Dialogue nocturne de Jésus avec Nicodème, docteur de la Loi, à Jérusalem. Jésus annonce le cœur sacrificiel du dessein rédempteur.",
      theologicalCategory = "Amour de Dieu & Salut"
    ),
    VerifiedPassage(
      reference = "Jean 14:6",
      book = "Jean",
      chapter = 14,
      verses = "6",
      exactText = "Jésus lui dit: Je suis le chemin, la vérité, et la vie. Nul ne vient au Père que par moi.",
      historicalContext = "Discours d'adieu dans la chambre haute avant Gethsémané. Jésus répond à l'interrogation de Thomas sur la direction du salut.",
      theologicalCategory = "Christologie (Solus Christus)"
    ),
    VerifiedPassage(
      reference = "Philippiens 4:6-7",
      book = "Philippiens",
      chapter = 4,
      verses = "6-7",
      exactText = "Ne vous inquiétez de rien; mais en toute chose faites connaître vos besoins à Dieu par des prières et des supplications, avec des actions de grâces. Et la paix de Dieu, qui surpasse toute intelligence, gardera vos cœurs et vos pensées en Jésus-Christ.",
      historicalContext = "Paul enchaîné à Rome remercie l'Église de Philippes et leur transmet l'antidote spirituel contre l'anxiété.",
      theologicalCategory = "Prière & Paix"
    ),
    VerifiedPassage(
      reference = "Psaume 23:1-3",
      book = "Psaumes",
      chapter = 23,
      verses = "1-3",
      exactText = "L'Éternel est mon berger: je ne manquerai de rien. Il me fait reposer dans de verts pâturages, Il me dirige près des eaux paisibles. Il restaure mon âme; Il me conduit dans les sentiers de la justice, À cause de son nom.",
      historicalContext = "Psaume de confiance royale et pastorale attribué à David, méditant sur la suffisance divine et la restauration intérieure.",
      theologicalCategory = "Confiance & Soin Pastoral"
    ),
    VerifiedPassage(
      reference = "2 Timothée 3:16-17",
      book = "2 Timothée",
      chapter = 3,
      verses = "16-17",
      exactText = "Toute Écriture est inspirée de Dieu, et utile pour enseigner, pour convaincre, pour corriger, pour instruire dans la justice, afin que l'homme de Dieu soit accompli et propre à toute bonne œuvre.",
      historicalContext = "Dernière lettre de Paul avant son martyre. Il exhorte Timothée à rester fidèle à l'autorité souveraine des Écritures (Sola Scriptura).",
      theologicalCategory = "Autorité des Écritures"
    ),
    VerifiedPassage(
      reference = "Colossiens 3:13",
      book = "Colossiens",
      chapter = 3,
      verses = "13",
      exactText = "Supportez-vous les uns les autres, et, si l'un a sujet de se plaindre de l'autre, pardonnez-vous réciproquement. De même que Christ vous a pardonné, pardonnez-vous aussi.",
      historicalContext = "Lettre aux Colossiens pour prévenir les dérives légalistes et gnostiques, insistant sur le pardon mutuel ancré dans la croix.",
      theologicalCategory = "Pardon & Vie communautaire"
    ),
    VerifiedPassage(
      reference = "Proverbes 3:5-6",
      book = "Proverbes",
      chapter = 3,
      verses = "5-6",
      exactText = "Confie-toi en l'Éternel de tout ton cœur, Et ne t'appuie pas sur ta sagesse; Reconnais-le dans toutes tes voies, Et il aplanira tes sentiers.",
      historicalContext = "Recueil de sagesse d'Israël, enseignant la soumission humble de la raison humaine à la providence divine.",
      theologicalCategory = "Sagesse & Discernement"
    ),
    VerifiedPassage(
      reference = "Matthieu 28:19-20",
      book = "Matthieu",
      chapter = 28,
      verses = "19-20",
      exactText = "Allez, faites de toutes les nations des disciples, les baptisant au nom du Père, du Fils et du Saint-Esprit, et enseignez-leur à observer tout ce que je vous ai prescrit. Et voici, je suis avec vous tous les jours, jusqu'à la fin du monde.",
      historicalContext = "La Grande Commission confiée par le Christ ressuscité sur une montagne de Galilée avant Son ascension.",
      theologicalCategory = "Mission & Baptême"
    ),
    VerifiedPassage(
      reference = "Jacques 1:5",
      book = "Jacques",
      chapter = 1,
      verses = "5",
      exactText = "Si quelqu'un d'entre vous manque de sagesse, qu'il la demande à Dieu, qui donne à tous simplement et sans reproche, et elle lui sera donnée.",
      historicalContext = "Jacques, responsable de l'Église de Jérusalem, écrit aux croyants dispersés pour les encourager à la prière persévérante dans l'épreuve.",
      theologicalCategory = "Prière & Sagesse"
    )
  )

  /**
   * Verifies if a reference is in our verified corpus and returns exact text and context.
   * If not in corpus, flags it responsibly so the AI doesn't pretend to give an exact checked citation.
   */
  fun verifyReference(rawReference: String): VerifiedCitationResult {
    val cleanRef = rawReference.trim()
    val match = verifiedPassages.firstOrNull { passage ->
      passage.reference.equals(cleanRef, ignoreCase = true) ||
        passage.reference.replace(" ", "").equals(cleanRef.replace(" ", ""), ignoreCase = true) ||
        cleanRef.contains(passage.reference, ignoreCase = true)
    }

    return if (match != null) {
      VerifiedCitationResult(
        reference = match.reference,
        exactText = match.exactText,
        historicalContext = match.historicalContext,
        isVerified = true,
        warningNote = null
      )
    } else {
      VerifiedCitationResult(
        reference = cleanRef,
        exactText = null,
        historicalContext = null,
        isVerified = false,
        warningNote = "Référence non indexée dans la base locale immédiate : veuillez vérifier le texte exact dans votre Bible."
      )
    }
  }
}
