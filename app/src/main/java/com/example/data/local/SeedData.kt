package com.example.data.local

import com.example.data.local.entity.DailyVerseEntity
import com.example.data.local.entity.DevotionEntity
import com.example.data.local.entity.PrayerEntity
import com.example.data.local.entity.ReadingPlanDayEntity
import com.example.data.local.entity.ReadingPlanEntity
import com.example.data.local.entity.SpiritualGoalEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SeedData {

  fun getInitialPrayers(): List<PrayerEntity> = listOf(
    PrayerEntity(
      id = "prayer_1",
      title = "Santé et rétablissement de maman",
      description = "Seigneur, nous te confions l'opération et le repos de maman. Que ton baume guérisseur l'accompagne et lui donne une paix profonde.",
      category = "SANTE",
      biblicalReference = "Ésaïe 53:5",
      createdAtTimestamp = System.currentTimeMillis() - 86400000L * 4,
      isAnswered = false,
      prayerCount = 6,
      isPrivate = true
    ),
    PrayerEntity(
      id = "prayer_2",
      title = "Sagesse et intégrité au travail",
      description = "Direction divine dans mes projets d'équipe et la gestion des délais difficiles cette semaine.",
      category = "TRAVAIL",
      biblicalReference = "Colossiens 3:23",
      createdAtTimestamp = System.currentTimeMillis() - 86400000L * 6,
      isAnswered = false,
      prayerCount = 10,
      isPrivate = true
    ),
    PrayerEntity(
      id = "prayer_3",
      title = "Réussite et sérénité pour les examens",
      description = "Prière pour la clarté d'esprit de Thomas pendant ses épreuves universitaires.",
      category = "ETUDES",
      biblicalReference = "Jacques 1:5",
      createdAtTimestamp = System.currentTimeMillis() - 86400000L * 8,
      isAnswered = false,
      prayerCount = 4,
      isPrivate = true
    ),
    PrayerEntity(
      id = "prayer_4",
      title = "Croissance et unité dans notre communauté",
      description = "Que l'amour fraternel et l'annonce fidèle de l'Évangile caractérisent nos cultes et groupes de maison.",
      category = "EGLISE",
      biblicalReference = "Actes 2:42",
      createdAtTimestamp = System.currentTimeMillis() - 86400000L * 10,
      isAnswered = false,
      prayerCount = 14,
      isPrivate = true
    ),
    PrayerEntity(
      id = "prayer_5",
      title = "Restauration des liens familiaux avec mon frère",
      description = "Dieu a adouci nos cœurs. Après des mois de froid, nous avons pu nous parler avec douceur et pardon.",
      category = "FAMILLE",
      biblicalReference = "Éphésiens 4:32",
      createdAtTimestamp = System.currentTimeMillis() - 86400000L * 14,
      isAnswered = true,
      answeredDateTimestamp = System.currentTimeMillis() - 86400000L * 2,
      testimony = "Gloire au Seigneur pour Sa grâce réconciliatrice ! Nous avons prié ensemble lors de notre dernier repas familial.",
      prayerCount = 22,
      isPrivate = true
    )
  )

  fun get14Devotions(): List<DevotionEntity> = listOf(
    DevotionEntity(
      id = "dev_1",
      dayOrder = 1,
      title = "La grâce seule qui sauve",
      scriptureReference = "Éphésiens 2:8-9",
      verseText = "Car c'est par la grâce que vous êtes sauvés, par le moyen de la foi. Et cela ne vient pas de vous, c'est le don de Dieu. Ce n'est point par les œuvres, afin que personne ne se glorifie.",
      contextExplanation = "L'apôtre Paul écrit aux chrétiens d'Éphèse pour leur rappeler le socle absolu de l'Évangile : le salut n'est pas une récompense acquise par nos mérites moraux ou religieux, mais un cadeau immérité accordé par Dieu en Jésus-Christ sur la croix.",
      reflectionQuestions = "1. Essayez-vous parfois d'acheter l'approbation de Dieu par vos propres efforts ?\n2. Comment le fait de savoir que votre salut est un don gratuit transforme-t-il votre reconnaissance ?",
      practicalAction = "Prenez 5 minutes aujourd'hui pour louer Dieu sans rien lui demander d'autre que de le remercier pour Sa grâce inconditionnelle.",
      guidedPrayer = "Père céleste, merci parce que mon salut ne repose pas sur mes forces chancelantes, mais sur le roc inébranlable de ta grâce en Jésus. Je me repose en toi aujourd'hui. Amen."
    ),
    DevotionEntity(
      id = "dev_2",
      dayOrder = 2,
      title = "La paix au-delà de toute intelligence",
      scriptureReference = "Philippiens 4:6-7",
      verseText = "Ne vous inquiétez de rien; mais en toute chose faites connaître vos besoins à Dieu par des prières et des supplications, avec des actions de grâces. Et la paix de Dieu, qui surpasse toute intelligence, gardera vos cœurs et vos pensées en Jésus-Christ.",
      contextExplanation = "Paul rédige cette épître depuis une cellule à Rome. Malgré l'emprisonnement et les menaces, il exhorte les croyants à transformer l'anxiété en supplication reconnaissante. La paix promise n'est pas une illusion passive, mais la garde armée de Dieu sur notre cœur.",
      reflectionQuestions = "1. Quel est le souci précis qui pèse le plus sur vos épaules ce matin ?\n2. L'avez-vous déjà transformé en prière avec action de grâces ?",
      practicalAction = "Écrivez sur un papier vos trois plus grandes inquiétudes du jour, priez dessus en remerciant Dieu d'avance pour son secours, puis déposez-les symboliquement.",
      guidedPrayer = "Seigneur Jésus, Prince de la Paix, je refuse de porter seul mes fardeaux. Je remets entre tes mains mes craintes et je reçois ta paix surnaturelle. Amen."
    ),
    DevotionEntity(
      id = "dev_3",
      dayOrder = 3,
      title = "Le bon berger qui restaure mon âme",
      scriptureReference = "Psaume 23:1-3",
      verseText = "L'Éternel est mon berger: je ne manquerai de rien. Il me fait reposer dans de verts pâturages, Il me dirige près des eaux paisibles. Il restaure mon âme; Il me conduit dans les sentiers de la justice, À cause de son nom.",
      contextExplanation = "David, lui-même ancien berger dans les collines de Judée, connaît intimement la vulnérabilité de la brebis. Il confesse que c'est Dieu lui-même qui subvient, guide et guérit les blessures intérieures de son peuple.",
      reflectionQuestions = "1. Dans quel domaine de votre vie ressentez-vous une soif de restauration ?\n2. Acceptez-vous de laisser le Berger décider du chemin ?",
      practicalAction = "Pendant votre pause déjeuner, isolez-vous dans le calme et relisez lentement le Psaume 23 en personnalisant chaque verset.",
      guidedPrayer = "Éternel, mon bon Berger, conduis mes pas vers tes eaux paisibles. Guéris mes lassitudes et renouvelle mes forces pour ta gloire. Amen."
    ),
    DevotionEntity(
      id = "dev_4",
      dayOrder = 4,
      title = "La justification et la paix avec Dieu",
      scriptureReference = "Romains 5:1-2",
      verseText = "Étant donc justifiés par la foi, nous avons la paix avec Dieu par notre Seigneur Jésus-Christ, à qui nous devons d'avoir eu par la foi accès à cette grâce, dans laquelle nous demeurons fermes.",
      contextExplanation = "Au cœur de l'épître aux Romains, Paul expose la doctrine centrale de la Réforme : la justification par la foi seule. Nous ne sommes plus sous la condamnation, mais nous avons un accès direct et permanent à la présence divine.",
      reflectionQuestions = "1. Vivez-vous dans la peur d'être rejeté par Dieu ou dans la certitude de la réconciliation ?\n2. Quelle différence cela fait-il dans votre assurance quotidienne ?",
      practicalAction = "Affirmez à voix haute aujourd'hui : « Rien ne peut me séparer de l'amour de Dieu en Jésus-Christ. »",
      guidedPrayer = "Merci Seigneur Jésus d'avoir payé ma dette à la croix. Parce que tu es ressuscité, j'ai la paix avec le Père pour l'éternité. Amen."
    ),
    DevotionEntity(
      id = "dev_5",
      dayOrder = 5,
      title = "Demeurer attaché au cep de vie",
      scriptureReference = "Jean 15:4-5",
      verseText = "Demeurez en moi, et je demeurerai en vous. Comme le sarment ne peut de lui-même porter du fruit, s'il ne demeure attaché au cep, ainsi vous ne le pouvez non plus, si vous ne demeurez en moi. Sans moi, vous ne pouvez rien faire.",
      contextExplanation = "Quelques heures avant la croix, Jésus donne à ses disciples cette allégorie agricole vitale. Le disciple ne produit pas le fruit par un activisme charnel, mais par une communion vivante et continue avec la sève du Christ.",
      reflectionQuestions = "1. Vous sentez-vous parfois spirituellement épuisé ? Se pourrait-il que vous essayiez de porter du fruit par vous-même ?\n2. Comment cultiver cette intimité avec le Christ au milieu de vos journées chargées ?",
      practicalAction = "Programmez un rappel silencieux dans l'après-midi pour simplement murmurer : « Jésus, je demeure en toi. »",
      guidedPrayer = "Seigneur Jésus, tu es le vrai cep. Détourne-moi de la vaine autosuffisance et apprends-moi à puiser chaque souffle dans ta sève divine. Amen."
    ),
    DevotionEntity(
      id = "dev_6",
      dayOrder = 6,
      title = "Le pardon qui libère les cœurs",
      scriptureReference = "Colossiens 3:13",
      verseText = "Supportez-vous les uns les autres, et, si l'un a sujet de se plaindre de l'autre, pardonnez-vous réciproquement. De même que Christ vous a pardonné, pardonnez-vous aussi.",
      contextExplanation = "La communauté de Colosses était traversée par des tensions relationnelles et culturelles. Paul n'établit pas le pardon sur les mérites de l'offenseur, mais sur l'immense mesure du pardon que Christ nous a accordé.",
      reflectionQuestions = "1. Gardez-vous de la rancœur envers une personne en particulier ?\n2. Réalisez-vous que le non-pardon est une prison pour celui qui refuse de relâcher l'offense ?",
      practicalAction = "Priez expressément aujourd'hui pour une personne qui vous a blessé, en demandant à Dieu de bénir sa vie.",
      guidedPrayer = "Père saint, puisque tu m'as tant pardonné, donne-moi le courage d'abandonner toute amertume et de bénir ceux qui m'ont outragé. Amen."
    ),
    DevotionEntity(
      id = "dev_7",
      dayOrder = 7,
      title = "Une nouvelle création en Christ",
      scriptureReference = "2 Corinthiens 5:17",
      verseText = "Si quelqu'un est en Christ, il est une nouvelle créature. Les choses anciennes sont passées; voici, toutes choses sont devenues nouvelles.",
      contextExplanation = "Paul enseigne que la conversion n'est pas un simple ajustement moral, mais une résurrection spirituelle intérieure. Dieu ne répare pas l'ancien homme déchu, Il crée une vie nouvelle animée par le Saint-Esprit.",
      reflectionQuestions = "1. Êtes-vous encore hanté par des erreurs passées que Dieu a déjà pardonnées ?\n2. Comment marcher en nouveauté de vie aujourd'hui ?",
      practicalAction = "Bannissez aujourd'hui toute parole d'autodépréciation : vous êtes l'enfant bien-aimé du Tout-Puissant.",
      guidedPrayer = "Merci mon Dieu pour la nouvelle naissance. Les condamnations d'hier sont englouties dans la victoire de la croix. Je marche libre avec toi. Amen."
    ),
    DevotionEntity(
      id = "dev_8",
      dayOrder = 8,
      title = "Toutes choses concourent au bien",
      scriptureReference = "Romains 8:28",
      verseText = "Nous savons, du reste, que toutes choses concourent au bien de ceux qui aiment Dieu, de ceux qui sont appelés selon son dessein.",
      contextExplanation = "Ce verset ne dit pas que toutes les circonstances sont bonnes en soi, mais que le Dieu Tout-Puissant est le Maître souverain capable de tisser même nos détours, nos larmes et nos épreuves pour forger en nous le caractère de Christ.",
      reflectionQuestions = "1. Traversez-vous un événement incompréhensible ou douloureux ?\n2. Croyez-vous que Dieu est capable d'en faire jaillir un bien spirituel éternel ?",
      practicalAction = "Partagez un mot d'encouragement biblique avec un frère ou une sœur qui traverse l'épreuve.",
      guidedPrayer = "Seigneur souverain, même quand le brouillard m'entoure, je choisis de faire confiance à ta bonté suprême. Tu tiens mon avenir entre tes mains. Amen."
    ),
    DevotionEntity(
      id = "dev_9",
      dayOrder = 9,
      title = "Confiance absolue de tout son cœur",
      scriptureReference = "Proverbes 3:5-6",
      verseText = "Confie-toi en l'Éternel de tout ton cœur, Et ne t'appuie pas sur ta sagesse; Reconnais-le dans toutes tes voies, Et il aplanira tes sentiers.",
      contextExplanation = "Salomon transmet la quintessence de la sagesse biblique : la véritable intelligence commence par la soumission de notre raison limitée à la sagesse infinie du Créateur.",
      reflectionQuestions = "1. Y a-t-il une décision importante que vous tentez de résoudre sans consulter le Seigneur ?\n2. Qu'implique de « le reconnaître dans toutes ses voies » ?",
      practicalAction = "Avant de commencer toute tâche importante aujourd'hui, marquez un temps d'arrêt de 10 secondes pour dire : « Seigneur, sois le maître de ce moment. »",
      guidedPrayer = "Père bienveillant, je renonce à mon orgueil intellectuel. Je remets mes projets sous ta lumière souveraine. Conduis mes pas selon ta vérité. Amen."
    ),
    DevotionEntity(
      id = "dev_10",
      dayOrder = 10,
      title = "L'amour agapé qui ne périt jamais",
      scriptureReference = "1 Corinthiens 13:4-7",
      verseText = "L'amour est patient, il est plein de bonté; l'amour n'est point envieux; l'amour ne se vante point, il ne s'enfle point d'orgueil, il ne fait rien de malhonnête, il ne cherche point son intérêt, il ne s'irrite point, il ne soupçonne point le mal...",
      contextExplanation = "Dans une Église corinthienne fascinée par les démonstrations spirituelles spectaculaires mais divisée, Paul rappelle que sans l'amour sacrificiel qui reflète le Christ, tous nos dons ne sont qu'un bruit vide.",
      reflectionQuestions = "1. Quel trait de l'amour décrit dans ce passage vous met le plus au défi en ce moment ?\n2. Comment manifester la patience envers une personne difficile aujourd'hui ?",
      practicalAction = "Faites un geste d'amour désintéressé pour quelqu'un aujourd'hui, sans attendre aucun retour.",
      guidedPrayer = "Seigneur, remplis mon cœur de ton amour agapé. Que mon regard et mes attitudes reflètent ta patience et ta compassion envers chacun. Amen."
    ),
    DevotionEntity(
      id = "dev_11",
      dayOrder = 11,
      title = "Une lampe sur mon sentier",
      scriptureReference = "Psaume 119:105",
      verseText = "Ta parole est une lampe à mes pieds, Et une lumière sur mon sentier.",
      contextExplanation = "Le psalmiste utilise l'image de la petite lampe à huile portée à la cheville dans la nuit orientale. La Bible n'éclaire pas forcément l'horizon lointain à des kilomètres, mais elle donne la lumière nécessaire pour poser le prochain pas avec assurance.",
      reflectionQuestions = "1. Cherchez-vous des réponses dans les opinions du monde ou dans les Écritures ?\n2. Prenez-vous le temps de laisser la Parole nourrir vos décisions ?",
      practicalAction = "Choisissez un verset biblique aujourd'hui et récitez-le mentalement à plusieurs reprises dans la journée.",
      guidedPrayer = "Dieu fidèle, que ta Sainte Parole soit ma boussole suprême. Garde-moi des faux sentiers et éclaire ma route au quotidien. Amen."
    ),
    DevotionEntity(
      id = "dev_12",
      dayOrder = 12,
      title = "Être le sel et la lumière du monde",
      scriptureReference = "Matthieu 5:13-16",
      verseText = "Vous êtes le sel de la terre... Vous êtes la lumière du monde. Une ville située sur une montagne ne peut être cachée... Que votre lumière luise ainsi devant les hommes, afin qu'ils voient vos bonnes œuvres, et qu'ils glorifient votre Père qui est dans les cieux.",
      contextExplanation = "Dans le Sermon sur la Montagne, Jésus définit l'identité publique de ses disciples. Le sel préserve de la corruption et apporte la saveur; la lumière dissipe les ténèbres de l'ignorance et du péché.",
      reflectionQuestions = "1. Votre entourage perçoit-il la saveur et l'espérance du Christ dans vos paroles ?\n2. Comment être une lumière bienveillante sur votre lieu de travail ou d'études ?",
      practicalAction = "Exprimez des encouragements sincères à trois personnes aujourd'hui au lieu de participer aux critiques ou commérages.",
      guidedPrayer = "Seigneur Jésus, fais de moi un témoin lumineux de ta vérité. Que mes actes et mes paroles rendent témoignage de ta bonté à ceux qui ne te connaissent pas encore. Amen."
    ),
    DevotionEntity(
      id = "dev_13",
      dayOrder = 13,
      title = "L'Esprit qui intercède dans nos faiblesses",
      scriptureReference = "Romains 8:26-27",
      verseText = "De même aussi l'Esprit nous aide dans notre faiblesse, car nous ne savons pas ce qu'il nous convient de demander dans nos prières; mais l'Esprit lui-même intercède par des soupirs inexprimables.",
      contextExplanation = "Paul apporte une immense consolation aux chrétiens : quand la douleur ou la fatigue nous enlève les mots pour prier, le Saint-Esprit qui habite en nous présente au Père des soupirs conformes à la volonté divine.",
      reflectionQuestions = "1. Avez-vous déjà eu l'impression de ne plus savoir quoi dire à Dieu ?\n2. Réalisez-vous que le Saint-Esprit intercède pour vous en cet instant précis ?",
      practicalAction = "Passez 5 minutes en silence devant Dieu, en lui disant simplement : « Saint-Esprit, prie à travers moi selon la volonté du Père. »",
      guidedPrayer = "Saint-Esprit Consolateur, merci de me soutenir dans mes limites. Prends le relais de mes prières chancelantes et glorifie le Christ dans mon cœur. Amen."
    ),
    DevotionEntity(
      id = "dev_14",
      dayOrder = 14,
      title = "Courir avec persévérance vers Jésus",
      scriptureReference = "Hébreux 12:1-2",
      verseText = "Rejetons tout fardeau, et le péché qui nous enveloppe si facilement, et courons avec persévérance dans la carrière qui nous est ouverte, ayant les regards sur Jésus, qui suscite la foi et la mène à la perfection.",
      contextExplanation = "L'auteur de l'épître aux Hébreux compare la vie chrétienne à une course d'endurance. Entourés d'une grande nuée de témoins de la foi, nous ne courons pas pour nous épuiser, mais les yeux fixés sur le Christ crucifié et glorifié.",
      reflectionQuestions = "1. Quel fardeau inutile ralentit votre course spirituelle en ce moment ?\n2. Comment garder les yeux fixés sur Jésus malgré les distractions environnantes ?",
      practicalAction = "Déchargez-vous consciemment d'un poids ou d'une culpabilité aujourd'hui, et décidez d'avancer d'un pas ferme.",
      guidedPrayer = "Seigneur Jésus, auteur et consommateur de ma foi, je fixe mes regards sur toi. Donne-moi l'endurance et la joie pour achever ma course à ta gloire. Amen."
    )
  )

  fun getInitialReadingPlans(): List<ReadingPlanEntity> = listOf(
    ReadingPlanEntity("plan_foi_7", "La Foi Inébranlable", "Fortifier sa confiance en Dieu au cœur des défis quotidiens.", 7, "FOI", true),
    ReadingPlanEntity("plan_priere_7", "Le Cœur de la Prière", "Découvrir la profondeur de la communion intime avec le Père.", 7, "PRIERE", true),
    ReadingPlanEntity("plan_pardon_7", "Le Pardon Libérateur", "Cheminer vers la guérison intérieure et la réconciliation biblique.", 7, "PARDON", false),
    ReadingPlanEntity("plan_sagesse_7", "La Sagesse pour le Quotidien", "Apprendre à discerner les choix sages inspirés des Proverbes et de Jacques.", 7, "SAGESSE", false),
    ReadingPlanEntity("plan_vie_christ_30", "La Vie en Christ & Nouvelle Naissance", "Parcours de 30 jours pour ancrer son identité de disciple régénéré.", 30, "VIE_EN_CHRIST", true),
    ReadingPlanEntity("plan_amour_30", "L'Amour de Dieu et du Prochain", "30 jours pour méditer l'amour sacrificiel et le vivre au quotidien.", 30, "AMOUR", false)
  )

  fun getInitialReadingPlanDays(): List<ReadingPlanDayEntity> {
    val days = mutableListOf<ReadingPlanDayEntity>()

    // 1. Plan Foi 7 jours
    val foiPassages = listOf(
      Pair("Hébreux 11:1-6", "La définition et le fondement de la foi"),
      Pair("Genèse 15:1-6", "La foi d'Abraham comptée comme justice"),
      Pair("Romains 4:18-25", "Espérer contre toute espérance"),
      Pair("Marc 9:17-27", "Viens au secours de mon incrédulité"),
      Pair("Matthieu 14:22-33", "Marcher sur les eaux les yeux fixés sur Christ"),
      Pair("Jacques 2:14-26", "La foi vivante démontrée par les œuvres"),
      Pair("1 Pierre 1:3-9", "L'épreuve de la foi plus précieuse que l'or")
    )
    foiPassages.forEachIndexed { index, pair ->
      days.add(
        ReadingPlanDayEntity(
          id = "foi_day_${index + 1}",
          planId = "plan_foi_7",
          dayNumber = index + 1,
          title = "Jour ${index + 1} : ${pair.second}",
          scriptureReference = pair.first,
          summary = "Méditez ce passage en demandant à Dieu d'élargir votre foi.",
          isCompleted = index == 0
        )
      )
    }

    // 2. Plan Prière 7 jours
    val prierePassages = listOf(
      Pair("Matthieu 6:5-15", "Le modèle du Notre Père"),
      Pair("Luc 11:1-13", "La persévérance dans la supplication"),
      Pair("Psaume 51:1-19", "La prière de repentance sincère"),
      Pair("1 Rois 18:36-39", "La prière ardente d'Élie"),
      Pair("Jean 17:1-26", "La prière sacerdotale de Jésus pour les siens"),
      Pair("Éphésiens 3:14-21", "La prière pour la puissance intérieure"),
      Pair("Philippiens 4:4-9", "La prière avec action de grâces")
    )
    prierePassages.forEachIndexed { index, pair ->
      days.add(
        ReadingPlanDayEntity(
          id = "priere_day_${index + 1}",
          planId = "plan_priere_7",
          dayNumber = index + 1,
          title = "Jour ${index + 1} : ${pair.second}",
          scriptureReference = pair.first,
          summary = "Appliquez cette clé de prière dans votre temps secret avec Dieu.",
          isCompleted = index == 0
        )
      )
    }

    // 3. Plan Pardon 7 jours
    val pardonPassages = listOf(
      Pair("Matthieu 18:21-35", "La parabole du serviteur impitoyable"),
      Pair("Genèse 45:1-15", "Joseph pardonne à ses frères"),
      Pair("Éphésiens 4:29-32", "Pardonner comme Christ nous a pardonné"),
      Pair("Colossiens 3:12-15", "Revêtir les entrailles de miséricorde"),
      Pair("Luc 23:33-38", "Père, pardonne-leur à la croix"),
      Pair("Romains 12:17-21", "Surmonter le mal par le bien"),
      Pair("Psaume 103:1-14", "Il éloigne nos fautes autant que l'orient de l'occident")
    )
    pardonPassages.forEachIndexed { index, pair ->
      days.add(
        ReadingPlanDayEntity(
          id = "pardon_day_${index + 1}",
          planId = "plan_pardon_7",
          dayNumber = index + 1,
          title = "Jour ${index + 1} : ${pair.second}",
          scriptureReference = pair.first,
          summary = "Laissez l'amour divin dissoudre toute rancœur dans votre cœur.",
          isCompleted = false
        )
      )
    }

    // 4. Plan Sagesse 7 jours
    val sagessePassages = listOf(
      Pair("Proverbes 1:1-7", "Le commencement de la sagesse est la crainte de l'Éternel"),
      Pair("Proverbes 3:1-12", "Ne t'appuie pas sur ta propre sagesse"),
      Pair("Proverbes 4:18-27", "Garde ton cœur plus que toute autre chose"),
      Pair("Jacques 1:5-8", "Demander la sagesse avec foi sans douter"),
      Pair("Jacques 3:13-18", "La sagesse d'en haut est pure et pacifique"),
      Pair("1 Corinthiens 1:18-25", "La sagesse de la croix face à la folie du monde"),
      Pair("Colossiens 2:1-7", "En Christ sont cachés tous les trésors de la sagesse")
    )
    sagessePassages.forEachIndexed { index, pair ->
      days.add(
        ReadingPlanDayEntity(
          id = "sagesse_day_${index + 1}",
          planId = "plan_sagesse_7",
          dayNumber = index + 1,
          title = "Jour ${index + 1} : ${pair.second}",
          scriptureReference = pair.first,
          summary = "Recevez le discernement d'en haut pour vos choix quotidiens.",
          isCompleted = false
        )
      )
    }

    // 5. Plan Vie en Christ 30 jours
    val vieEnChristKeyDays = listOf(
      Pair("Jean 3:1-16", "La nouvelle naissance d'en haut"),
      Pair("Romains 6:1-11", "Morts au péché, vivants pour Dieu en Jésus"),
      Pair("Galates 2:19-21", "Crucifié avec Christ, ce n'est plus moi qui vis"),
      Pair("Colossiens 3:1-11", "Chercher les choses d'en haut"),
      Pair("1 Jean 3:1-3", "Voyez quel amour le Père nous a témoigné"),
      Pair("Romains 8:1-17", "Conduits par l'Esprit de Dieu"),
      Pair("2 Corinthiens 5:14-21", "Le ministère de la réconciliation")
    )
    for (i in 1..30) {
      val passage = if (i <= vieEnChristKeyDays.size) vieEnChristKeyDays[i - 1] else Pair("Jean ${((i % 21) + 1)}:1-15", "Demeurer dans la Parole et la vérité")
      days.add(
        ReadingPlanDayEntity(
          id = "vie_christ_day_$i",
          planId = "plan_vie_christ_30",
          dayNumber = i,
          title = "Jour $i : ${passage.second}",
          scriptureReference = passage.first,
          summary = "Enracinez votre nouvelle vie dans les promesses éternelles.",
          isCompleted = i <= 3
        )
      )
    }

    // 6. Plan Amour 30 jours
    val amourKeyDays = listOf(
      Pair("1 Corinthiens 13:1-13", "L'hymne à l'amour agapé"),
      Pair("1 Jean 4:7-21", "Dieu est amour, celui qui aime est né de Dieu"),
      Pair("Luc 10:25-37", "La parabole du bon Samaritain"),
      Pair("Jean 13:31-35", "Aimez-vous les uns les autres"),
      Pair("Romains 12:9-21", "Que l'amour soit sans hypocrisie")
    )
    for (i in 1..30) {
      val passage = if (i <= amourKeyDays.size) amourKeyDays[i - 1] else Pair("1 Jean ${((i % 5) + 1)}:1-10", "Marcher dans la lumière et la communion fraternelle")
      days.add(
        ReadingPlanDayEntity(
          id = "amour_day_$i",
          planId = "plan_amour_30",
          dayNumber = i,
          title = "Jour $i : ${passage.second}",
          scriptureReference = passage.first,
          summary = "Vivez l'amour du Christ par des actes de compassion concrète.",
          isCompleted = false
        )
      )
    }

    return days
  }

  fun getInitialGoals(): List<SpiritualGoalEntity> = listOf(
    SpiritualGoalEntity(
      id = "goal_1",
      title = "Temps de prière matinal",
      description = "Consacrer 15 minutes à la communion intime avec Dieu chaque matin.",
      category = "PRIERE",
      targetValue = 15,
      currentValue = 15,
      unit = "min",
      frequency = "QUOTIDIEN",
      streakDays = 7,
      isCompleted = true
    ),
    SpiritualGoalEntity(
      id = "goal_2",
      title = "Lecture des Écritures",
      description = "Méditer 2 chapitres de la Parole de Dieu.",
      category = "LECTURE",
      targetValue = 2,
      currentValue = 1,
      unit = "chapitres",
      frequency = "QUOTIDIEN",
      streakDays = 5,
      isCompleted = false
    ),
    SpiritualGoalEntity(
      id = "goal_3",
      title = "Mémorisation d'un verset clé",
      description = "Garder la Parole dans son cœur pour affronter la semaine.",
      category = "MEMORISATION",
      targetValue = 1,
      currentValue = 1,
      unit = "verset",
      frequency = "HEBDOMADAIRE",
      streakDays = 3,
      isCompleted = true
    ),
    SpiritualGoalEntity(
      id = "goal_4",
      title = "Méditation & Silence",
      description = "Prendre un temps pour écouter la voix de Dieu sans agitation.",
      category = "MEDITATION",
      targetValue = 30,
      currentValue = 20,
      unit = "min",
      frequency = "HEBDOMADAIRE",
      streakDays = 4,
      isCompleted = false
    )
  )

  fun getInitialDailyVerses(): List<DailyVerseEntity> {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val now = System.currentTimeMillis()
    val dayMillis = 86400000L

    fun dateOffset(daysAgo: Long): String {
      return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now - daysAgo * dayMillis))
    }

    return listOf(
      DailyVerseEntity(
        id = "verse_today",
        reference = "Éphésiens 2:8-9",
        text = "Car c'est par la grâce que vous êtes sauvés, par le moyen de la foi. Et cela ne vient pas de vous, c'est le don de Dieu. Ce n'est point par les œuvres, afin que personne ne se glorifie.",
        book = "Éphésiens",
        chapter = 2,
        verseNumber = "8-9",
        theme = "Grâce",
        translation = "Louis Segond 1910",
        reflectionPrompt = "Le salut ne dépend pas de nos performances humaines mais de la bienveillance infinie du Père offerte en Jésus-Christ. Reposez-vous aujourd'hui dans cette grâce imméritée.",
        prayerPrompt = "Seigneur, merci pour ton salut gratuit. Libère-moi du piège de la performance et aide-moi à vivre aujourd'hui dans la joie et la reconnaissance de ta grâce.",
        date = today,
        isFavorite = true,
        savedAtTimestamp = now
      ),
      DailyVerseEntity(
        id = "verse_romans_8_28",
        reference = "Romains 8:28",
        text = "Nous savons, du reste, que toutes choses concourent au bien de ceux qui aiment Dieu, de ceux qui sont appelés selon son dessein.",
        book = "Romains",
        chapter = 8,
        verseNumber = "28",
        theme = "Espérance",
        translation = "Louis Segond 1910",
        reflectionPrompt = "Même dans les saisons d'incompréhension ou d'épreuves, la souveraineté divine tisse chaque fil pour accomplir un bien spirituel éternel.",
        prayerPrompt = "Père céleste, même quand les circonstances me troublent, je place ma confiance en ta bienveillance. Tu as le contrôle de toute chose dans ma vie.",
        date = dateOffset(1),
        isFavorite = true,
        savedAtTimestamp = now - dayMillis
      ),
      DailyVerseEntity(
        id = "verse_philippians_4_6_7",
        reference = "Philippiens 4:6-7",
        text = "Ne vous inquiétez de rien; mais en toute chose faites connaître vos besoins à Dieu par des prières et des supplications, avec des actions de grâces. Et la paix de Dieu, qui surpasse toute intelligence, gardera vos cœurs et vos pensées en Jésus-Christ.",
        book = "Philippiens",
        chapter = 4,
        verseNumber = "6-7",
        theme = "Paix",
        translation = "Louis Segond 1910",
        reflectionPrompt = "La prière est le remède divin contre l'anxiété. Transformez chaque sujet d'angoisse en requête pleine de reconnaissance.",
        prayerPrompt = "Seigneur Jésus, je dépose au pied de ta croix tous mes soucis et angoisses. Remplace mon agitation par ta paix surnaturelle.",
        date = dateOffset(2),
        isFavorite = true,
        savedAtTimestamp = now - dayMillis * 2
      ),
      DailyVerseEntity(
        id = "verse_jeremiah_29_11",
        reference = "Jérémie 29:11",
        text = "Car je connais les projets que j'ai formés sur vous, dit l'Éternel, projets de paix et non de malheur, afin de vous donner un avenir et de l'espérance.",
        book = "Jérémie",
        chapter = 29,
        verseNumber = "11",
        theme = "Confiance",
        translation = "Louis Segond 1910",
        reflectionPrompt = "Dieu n'improvise rien avec votre destinée. Ses pensées envers vous sont remplies d'amour, de paix et d'un futur radieux.",
        prayerPrompt = "Éternel mon Dieu, je m'abandonne à tes desseins parfaits. Guide mes pas et affermis ma foi en tes promesses.",
        date = dateOffset(3),
        isFavorite = false,
        savedAtTimestamp = now - dayMillis * 3
      ),
      DailyVerseEntity(
        id = "verse_isaiah_40_31",
        reference = "Ésaïe 40:31",
        text = "Mais ceux qui se confient en l'Éternel renouvellent leur force. Ils prennent le vol comme les aigles; Ils courent, et ne se lassent point; Ils marchent, et ne se fatiguent point.",
        book = "Ésaïe",
        chapter = 40,
        verseNumber = "31",
        theme = "Force",
        translation = "Louis Segond 1910",
        reflectionPrompt = "L'attente active devant Dieu n'est jamais une perte de temps. C'est le lieu secret où notre faiblesse est échangée contre Sa vigueur.",
        prayerPrompt = "Seigneur, alors que mes forces s'épuisent, je me tourne vers toi. Renouvelle mon esprit et donne-moi la force de persévérer.",
        date = dateOffset(4),
        isFavorite = false,
        savedAtTimestamp = now - dayMillis * 4
      ),
      DailyVerseEntity(
        id = "verse_john_3_16",
        reference = "Jean 3:16",
        text = "Car Dieu a tant aimé le monde qu'il a donné son Fils unique, afin que quiconque croit en lui ne périsse point, mais qu'il ait la vie éternelle.",
        book = "Jean",
        chapter = 3,
        verseNumber = "16",
        theme = "Amour",
        translation = "Louis Segond 1910",
        reflectionPrompt = "Le cœur de l'Évangile réside dans ce don inestimable : l'amour de Dieu manifesté au Calvaire pour nous racheter.",
        prayerPrompt = "Mon Sauveur, comment mesurer un tel amour ? Merci d'avoir donné ta vie pour moi. Que ma journée reflète cet amour envers mon prochain.",
        date = dateOffset(5),
        isFavorite = true,
        savedAtTimestamp = now - dayMillis * 5
      ),
      DailyVerseEntity(
        id = "verse_proverbs_3_5_6",
        reference = "Proverbes 3:5-6",
        text = "Confie-toi en l'Éternel de tout ton cœur, et ne t'appuie pas sur ta sagesse; reconnais-le dans toutes tes voies, et il aplanira tes sentiers.",
        book = "Proverbes",
        chapter = 3,
        verseNumber = "5-6",
        theme = "Sagesse",
        translation = "Louis Segond 1910",
        reflectionPrompt = "Abandonner notre propre sagesse pour suivre les conseils de Dieu est la plus haute forme d'intelligence spirituelle.",
        prayerPrompt = "Seigneur, j'aligne mes décisions sur ta Parole. Conduis mes pas et aplanis les chemins devant moi.",
        date = dateOffset(6),
        isFavorite = false,
        savedAtTimestamp = now - dayMillis * 6
      ),
      DailyVerseEntity(
        id = "verse_psalm_23_1",
        reference = "Psaume 23:1",
        text = "L'Éternel est mon berger: je ne manquerai de rien.",
        book = "Psaume",
        chapter = 23,
        verseNumber = "1",
        theme = "Protection",
        translation = "Louis Segond 1910",
        reflectionPrompt = "Sous la houlette du Bon Berger, nous avons la certitude que nos besoins spirituels, émotionnels et matériels sont pleinement comblés.",
        prayerPrompt = "Jésus, Bon Berger de mon âme, conduis-moi près des eaux paisibles. Garde-moi de toute crainte.",
        date = dateOffset(7),
        isFavorite = false,
        savedAtTimestamp = now - dayMillis * 7
      )
    )
  }
}
