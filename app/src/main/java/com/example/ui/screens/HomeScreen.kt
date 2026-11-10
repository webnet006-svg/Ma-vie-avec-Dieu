package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Devotion
import com.example.data.model.UserProfile
import com.example.data.model.Verse
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
  viewModel: MainViewModel,
  onNavigateToBible: () -> Unit,
  onNavigateToPrayer: () -> Unit,
  onNavigateToAssistant: () -> Unit,
  onNavigateToGoals: () -> Unit,
  onNavigateToDevotionDetail: () -> Unit,
  onNavigateToReadingPlans: () -> Unit,
  onNavigateToDevotionHistory: () -> Unit,
  onNavigateToVerseOfTheDay: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val dailyVerse by viewModel.dailyVerse.collectAsState()
  val todayDailyVerseEntity by viewModel.todayVerseOfTheDay.collectAsState()
  val todayDevotion by viewModel.todayDevotion.collectAsState()
  val prayers by viewModel.prayers.collectAsState()
  val profile by viewModel.profile.collectAsState()
  val context = LocalContext.current

  val activeVerse = todayDailyVerseEntity?.let { entity ->
    dailyVerse.copy(
      text = entity.text,
      reference = entity.reference,
      version = entity.translation
    )
  } ?: dailyVerse

  val activePrayersCount = prayers.count { !it.isAnswered }
  val answeredPrayersCount = prayers.count { it.isAnswered }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
  ) {
    // 1. Salutation Header
    item {
      GreetingHeader(profile = profile)
    }

    // 2. Verset du Jour avec Référence
    item {
      DailyVerseCard(
        verse = activeVerse,
        onShare = {
          val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "« ${activeVerse.text} » — ${activeVerse.reference} (Ma Vie avec Dieu)")
            type = "text/plain"
          }
          val shareIntent = Intent.createChooser(sendIntent, "Partager le verset du jour")
          context.startActivity(shareIntent)
        },
        onReadMeditation = onNavigateToVerseOfTheDay
      )
    }

    // 3. Méditation Quotidienne
    item {
      todayDevotion?.let { devotion ->
        DailyDevotionCard(
          devotion = devotion,
          onOpenDetail = onNavigateToDevotionDetail,
          onOpenHistory = onNavigateToDevotionHistory
        )
      }
    }

    // Section title
    item {
      Text(
        text = "Votre Vie avec Dieu Aujourd'hui",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    // 4. Carte « Mon temps de prière »
    item {
      ActionCard(
        title = "Mon temps de prière",
        subtitle = "$activePrayersCount requêtes actives • $answeredPrayersCount exaucées",
        badgeText = "Ferveur",
        icon = Icons.Default.VolunteerActivism,
        accentColor = OrDoux,
        buttonText = "Ouvrir le journal",
        testTag = "home_card_prayer",
        onClick = onNavigateToPrayer
      )
    }

    // 5. Carte « Plans de lecture (7 & 30 jours) »
    item {
      ActionCard(
        title = "Plans de lecture biblique",
        subtitle = "Parcours Foi, Prière, Pardon, Sagesse, Vie en Christ (7 & 30 jours)",
        badgeText = "6 Parcours",
        icon = Icons.Default.MenuBook,
        accentColor = Color(0xFF2E7D32),
        buttonText = "Découvrir les plans",
        testTag = "home_card_reading_plans",
        onClick = onNavigateToReadingPlans
      )
    }

    // 6. Carte « Lire la Bible »
    item {
      ActionCard(
        title = "Lire la Bible",
        subtitle = "Psaume 23 • « L'Éternel est mon berger: je ne manquerai de rien »",
        badgeText = "Canon 66 Livres",
        icon = Icons.Default.Book,
        accentColor = VertProfond,
        buttonText = "Commencer la lecture",
        testTag = "home_card_bible",
        onClick = onNavigateToBible
      )
    }

    // 7. Carte « Parler à l'assistant IA »
    item {
      ActionCard(
        title = "Parler à Eli • Assistant Biblique",
        subtitle = "Conseiller pédagogique protestant fondé sur les Écritures",
        badgeText = "Assistant Eli",
        icon = Icons.Default.AutoAwesome,
        accentColor = Color(0xFF6B4E9B),
        buttonText = "Poser une question",
        testTag = "home_card_assistant",
        onClick = onNavigateToAssistant
      )
    }

    // 8. Carte « Ma croissance spirituelle »
    item {
      ActionCard(
        title = "Ma croissance spirituelle",
        subtitle = "${profile.currentStreak} jours de marche • Bilan hebdomadaire disponible",
        badgeText = "🔥 ${profile.currentStreak} jours",
        icon = Icons.Default.TrendingUp,
        accentColor = Color(0xFFE67E22),
        buttonText = "Voir mes objectifs",
        testTag = "home_card_goals",
        onClick = onNavigateToGoals
      )
    }
  }
}

@Composable
fun GreetingHeader(
  profile: UserProfile,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = "La paix du Christ soit avec vous,",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = profile.name,
        style = MaterialTheme.typography.headlineLarge,
        color = VertProfond,
        fontWeight = FontWeight.Bold
      )
    }

    Surface(
      shape = CircleShape,
      color = OrDoux.copy(alpha = 0.2f),
      modifier = Modifier.size(48.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = Icons.Default.Church,
          contentDescription = "Icône foi",
          tint = VertProfond,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

@Composable
fun DailyVerseCard(
  verse: Verse,
  onShare: () -> Unit,
  onReadMeditation: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("home_daily_verse_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = VertProfond),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(VertProfond, Color(0xFF1B4E3E), Color(0xFF11382C))
          )
        )
        .padding(20.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = OrDoux,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "VERSET DU JOUR",
              style = MaterialTheme.typography.labelMedium,
              color = OrDoux,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }

          IconButton(
            onClick = onShare,
            modifier = Modifier
              .size(36.dp)
              .testTag("share_daily_verse_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Share,
              contentDescription = "Partager le verset",
              tint = Ivoire
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "« ${verse.text} »",
          style = MaterialTheme.typography.titleLarge,
          color = IvoireLight,
          lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${verse.reference} (${verse.version})",
            style = MaterialTheme.typography.labelLarge,
            color = OrDoux,
            fontWeight = FontWeight.Bold
          )

          TextButton(
            onClick = onReadMeditation,
            colors = ButtonDefaults.textButtonColors(contentColor = Ivoire)
          ) {
            Text("Méditer ce verset →")
          }
        }
      }
    }
  }
}

@Composable
fun DailyDevotionCard(
  devotion: Devotion,
  onOpenDetail: () -> Unit,
  onOpenHistory: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onOpenDetail() }
      .testTag("home_daily_devotion_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = OrDoux.copy(alpha = 0.18f)
        ) {
          Text(
            text = "MÉDITATION QUOTIDIENNE",
            style = MaterialTheme.typography.labelSmall,
            color = Charbon,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }

        TextButton(onClick = onOpenHistory) {
          Text("14 parcours →", fontSize = 12.sp, color = VertProfond, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = devotion.title,
        style = MaterialTheme.typography.headlineMedium,
        color = VertProfond,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = devotion.contextExplanation,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 22.sp
      )

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (devotion.isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
            contentDescription = null,
            tint = if (devotion.isCompleted) AccentSuccess else OrDouxDark,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (devotion.isCompleted) "Terminée" else "Temps estimé : 5 min",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        FilledTonalButton(
          onClick = onOpenDetail,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.height(36.dp)
        ) {
          Text("Lire la méditation")
        }
      }
    }
  }
}

@Composable
fun ActionCard(
  title: String,
  subtitle: String,
  badgeText: String,
  icon: ImageVector,
  accentColor: Color,
  buttonText: String,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag(testTag),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(accentColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
          )
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = accentColor.copy(alpha = 0.12f)
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall,
              color = accentColor,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              fontSize = 10.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Ouvrir",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(24.dp)
      )
    }
  }
}
