package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Devotion
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevotionDetailScreen(
  viewModel: MainViewModel,
  onBackClicked: () -> Unit,
  onNavigateToHistory: () -> Unit,
  modifier: Modifier = Modifier
) {
  val selectedDevotion by viewModel.selectedDevotion.collectAsState()
  val todayDevotion by viewModel.todayDevotion.collectAsState()
  val devotion: Devotion? = selectedDevotion ?: todayDevotion
  val context = LocalContext.current

  if (devotion == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator(color = VertProfond)
    }
    return
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Méditation Biblique",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBackClicked,
            modifier = Modifier.testTag("devotion_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Retour"
            )
          }
        },
        actions = {
          IconButton(
            onClick = onNavigateToHistory,
            modifier = Modifier.testTag("devotion_history_button")
          ) {
            Icon(
              imageVector = Icons.Default.DateRange,
              contentDescription = "Historique des méditations",
              tint = VertProfond
            )
          }

          IconButton(
            onClick = {
              val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                  Intent.EXTRA_TEXT,
                  "« ${devotion.title} »\n${devotion.verse.reference}\n\n« ${devotion.verse.text} »\n\nMa Vie avec Dieu — Chaque jour, un pas de plus avec Dieu."
                )
                type = "text/plain"
              }
              context.startActivity(Intent.createChooser(shareIntent, "Partager la méditation"))
            }
          ) {
            Icon(
              imageVector = Icons.Outlined.Share,
              contentDescription = "Partager",
              tint = VertProfond
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .navigationBarsPadding()
        ) {
          Button(
            onClick = {
              viewModel.markDevotionCompleted(devotion.id, !devotion.isCompleted)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (devotion.isCompleted) AccentSuccess else VertProfond,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("devotion_complete_button")
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (devotion.isCompleted) "Méditation terminée ! (Cliquer pour annuler)" else "Marquer comme terminée",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
      // Header Day badge & Author
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = OrDoux.copy(alpha = 0.2f)
          ) {
            Text(
              text = devotion.dateFormatted,
              style = MaterialTheme.typography.labelMedium,
              color = Charbon,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }

          if (devotion.isCompleted) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = AccentSuccess.copy(alpha = 0.15f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentSuccess, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Terminée ${devotion.completedDate?.let { "($it)" } ?: ""}",
                  style = MaterialTheme.typography.labelSmall,
                  color = AccentSuccess,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = devotion.title,
          style = MaterialTheme.typography.headlineLarge,
          color = VertProfond,
          fontWeight = FontWeight.Bold
        )
      }

      // 1. Référence Biblique & Verset
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Ivoire,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.MenuBook, contentDescription = null, tint = VertProfond, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = devotion.verse.reference,
                style = MaterialTheme.typography.titleMedium,
                color = VertProfond,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "« ${devotion.verse.text} »",
              style = MaterialTheme.typography.bodyLarge,
              fontStyle = FontStyle.Italic,
              color = Charbon,
              lineHeight = 24.sp
            )
          }
        }
      }

      // 2. Explication du Contexte
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Lightbulb, contentDescription = null, tint = OrDouxDark, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Explication du Contexte",
                style = MaterialTheme.typography.titleMedium,
                color = VertProfond,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = devotion.contextExplanation,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 22.sp
            )
          }
        }
      }

      // 3. Questions de Réflexion
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = OrDoux.copy(alpha = 0.12f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Questions de Réflexion :",
              style = MaterialTheme.typography.titleSmall,
              color = OrDouxDark,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = devotion.reflectionQuestion,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 22.sp
            )
          }
        }
      }

      // 4. Action Pratique
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = VertProfond.copy(alpha = 0.08f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.PanTool, contentDescription = null, tint = VertProfond, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Action Pratique pour Aujourd'hui :",
                style = MaterialTheme.typography.titleSmall,
                color = VertProfond,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = devotion.practicalAction,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 22.sp
            )
          }
        }
      }

      // 5. Prière Guidée
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = VertProfond, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Prière guidée :",
                style = MaterialTheme.typography.titleSmall,
                color = VertProfond,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = devotion.prayer,
              style = MaterialTheme.typography.bodyMedium,
              fontStyle = FontStyle.Italic,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 22.sp
            )
          }
        }
      }
    }
  }
}
