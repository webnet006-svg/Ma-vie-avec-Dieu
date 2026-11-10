package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoalCategory
import com.example.data.model.GoalFrequency
import com.example.data.model.SpiritualGoal
import com.example.data.model.WeeklyReview
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
  viewModel: MainViewModel,
  onBackClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val goals by viewModel.goals.collectAsState()
  val weeklyReview by viewModel.weeklyReview.collectAsState()
  val profile by viewModel.profile.collectAsState()

  var isCreateGoalDialogOpen by remember { mutableStateOf(false) }
  var isWeeklyReviewDialogOpen by remember { mutableStateOf(false) }
  var goalToDelete by remember { mutableStateOf<SpiritualGoal?>(null) }

  // New goal form state
  var formTitle by remember { mutableStateOf("") }
  var formDescription by remember { mutableStateOf("") }
  var formCategory by remember { mutableStateOf(GoalCategory.PRIERE) }
  var formTargetValue by remember { mutableStateOf("15") }
  var formUnit by remember { mutableStateOf("min") }
  var formFrequency by remember { mutableStateOf(GoalFrequency.QUOTIDIEN) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Objectifs Spirituels",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = VertProfond
            )
            Text(
              text = "Marcher chaque jour dans la grâce et la fidélité",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBackClicked,
            modifier = Modifier.testTag("goals_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
          }
        },
        actions = {
          IconButton(
            onClick = { isWeeklyReviewDialogOpen = true },
            modifier = Modifier.testTag("goals_weekly_review_button")
          ) {
            Icon(Icons.Default.Assessment, contentDescription = "Bilan hebdomadaire", tint = VertProfond)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = {
          formTitle = ""
          formDescription = ""
          formCategory = GoalCategory.PRIERE
          formTargetValue = "15"
          formUnit = "min"
          formFrequency = GoalFrequency.QUOTIDIEN
          isCreateGoalDialogOpen = true
        },
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("Nouvel Objectif") },
        containerColor = VertProfond,
        contentColor = Color.White,
        modifier = Modifier
          .navigationBarsPadding()
          .testTag("fab_create_goal")
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 20.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Streak Card Hero
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = VertProfond),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(OrDoux.copy(alpha = 0.25f)),
              contentAlignment = Alignment.Center
            ) {
              Text(text = "🔥", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
              Text(
                text = "${profile.currentStreak} Jours de Fidélité",
                style = MaterialTheme.typography.titleLarge,
                color = IvoireLight,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "« Courons avec persévérance dans la carrière qui nous est ouverte » (Héb 12:1)",
                style = MaterialTheme.typography.bodySmall,
                color = Ivoire.copy(alpha = 0.85f),
                lineHeight = 18.sp
              )
            }
          }
        }
      }

      // Weekly Encouragement Banner
      weeklyReview?.let { review ->
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OrDoux.copy(alpha = 0.15f)),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { isWeeklyReviewDialogOpen = true }
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(OrDoux),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Charbon, modifier = Modifier.size(20.dp))
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Bilan & Encouragement de la Semaine",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Charbon
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "${review.completedGoals} / ${review.totalGoals} objectifs atteints • Touchez pour voir le message",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Charbon)
            }
          }
        }
      }

      // Goals Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Mes Engagements Spirituels",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${goals.count { it.isCompleted }} complétés",
            style = MaterialTheme.typography.bodySmall,
            color = VertProfond,
            fontWeight = FontWeight.Bold
          )
        }
      }

      if (goals.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(54.dp))
              Spacer(modifier = Modifier.height(12.dp))
              Text("Aucun objectif configuré", style = MaterialTheme.typography.titleMedium)
              Spacer(modifier = Modifier.height(4.dp))
              Text("Touchez « Nouvel Objectif » pour créer un temps de prière ou de lecture.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      } else {
        items(goals, key = { it.id }) { goal ->
          SpiritualGoalCardV2(
            goal = goal,
            onToggle = { viewModel.toggleGoalCompleted(goal.id) },
            onIncrement = { viewModel.updateGoalProgress(goal.id, 1) },
            onDecrement = { viewModel.updateGoalProgress(goal.id, -1) },
            onDelete = { goalToDelete = goal }
          )
        }
      }
    }
  }

  // --- DIALOG: CRÉER UN OBJECTIF SPIRITUEL ---
  if (isCreateGoalDialogOpen) {
    AlertDialog(
      onDismissRequest = { isCreateGoalDialogOpen = false },
      title = { Text("Créer un Objectif Spirituel", style = MaterialTheme.typography.titleLarge) },
      text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = formTitle,
            onValueChange = { formTitle = it },
            label = { Text("Titre de l'engagement *") },
            placeholder = { Text("Ex: Prière matinale, Épître aux Romains...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_goal_title")
          )

          OutlinedTextField(
            value = formDescription,
            onValueChange = { formDescription = it },
            label = { Text("Description bienveillante") },
            placeholder = { Text("Pourquoi cet objectif nourrit votre foi...") },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
          )

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = formTargetValue,
              onValueChange = { formTargetValue = it },
              label = { Text("Cible (chiffre)") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
              value = formUnit,
              onValueChange = { formUnit = it },
              label = { Text("Unité") },
              placeholder = { Text("min, chap...") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }

          Text("Fréquence :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GoalFrequency.entries.forEach { freq ->
              FilterChip(
                selected = formFrequency == freq,
                onClick = { formFrequency = freq },
                label = { Text(freq.label, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = VertProfond,
                  selectedLabelColor = Color.White
                )
              )
            }
          }

          Text("Catégorie :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(GoalCategory.entries) { cat ->
              FilterChip(
                selected = formCategory == cat,
                onClick = { formCategory = cat },
                label = { Text(cat.label, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = VertProfond,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val target = formTargetValue.toIntOrNull() ?: 1
            viewModel.createGoal(
              title = formTitle.trim(),
              description = formDescription.trim(),
              category = formCategory,
              targetValue = target,
              unit = formUnit.trim().ifBlank { "unités" },
              frequency = formFrequency
            )
            isCreateGoalDialogOpen = false
          },
          enabled = formTitle.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = VertProfond),
          modifier = Modifier.testTag("submit_create_goal_button")
        ) {
          Text("Créer l'objectif")
        }
      },
      dismissButton = {
        TextButton(onClick = { isCreateGoalDialogOpen = false }) {
          Text("Annuler")
        }
      }
    )
  }

  // --- DIALOG: BILAN HEBDOMADAIRE ---
  if (isWeeklyReviewDialogOpen) {
    weeklyReview?.let { review ->
      AlertDialog(
        onDismissRequest = { isWeeklyReviewDialogOpen = false },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Assessment, contentDescription = null, tint = VertProfond)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bilan Hebdomadaire", style = MaterialTheme.typography.titleLarge)
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = VertProfond.copy(alpha = 0.08f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = "Résumé de votre marche :",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = VertProfond
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("• ${review.completedGoals} sur ${review.totalGoals} objectifs atteints", style = MaterialTheme.typography.bodyMedium)
                Text("• ${review.totalPrayersCount} prières et supplications montées vers Dieu", style = MaterialTheme.typography.bodyMedium)
                Text("• ${review.answeredPrayersCount} prières exaucées avec reconnaissance", style = MaterialTheme.typography.bodyMedium)
                Text("• ${review.devotionCompletedCount} méditations bibliques achevées", style = MaterialTheme.typography.bodyMedium)
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = OrDoux.copy(alpha = 0.15f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = "Parole d'encouragement :",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Charbon
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = review.encouragementMessage,
                  style = MaterialTheme.typography.bodyMedium,
                  fontStyle = FontStyle.Italic,
                  color = Charbon,
                  lineHeight = 20.sp
                )
              }
            }
          }
        },
        confirmButton = {
          Button(
            onClick = { isWeeklyReviewDialogOpen = false },
            colors = ButtonDefaults.buttonColors(containerColor = VertProfond)
          ) {
            Text("Amen !")
          }
        }
      )
    }
  }

  // --- DIALOG: SUPPRESSION D'UN OBJECTIF ---
  goalToDelete?.let { goal ->
    AlertDialog(
      onDismissRequest = { goalToDelete = null },
      icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
      title = { Text("Supprimer cet objectif ?") },
      text = { Text("Voulez-vous supprimer l'objectif « ${goal.title} » ?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteGoal(goal.id)
            goalToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Supprimer")
        }
      },
      dismissButton = {
        TextButton(onClick = { goalToDelete = null }) {
          Text("Annuler")
        }
      }
    )
  }
}

@Composable
fun SpiritualGoalCardV2(
  goal: SpiritualGoal,
  onToggle: () -> Unit,
  onIncrement: () -> Unit,
  onDecrement: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = goal.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = OrDoux.copy(alpha = 0.15f)
            ) {
              Text(
                text = goal.frequency.label,
                style = MaterialTheme.typography.labelSmall,
                color = Charbon,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          if (goal.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = goal.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Outlined.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Linear progress bar
      LinearProgressIndicator(
        progress = { goal.progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = if (goal.isCompleted) AccentSuccess else VertProfond,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Counters and Steppers
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${goal.currentCount} / ${goal.targetCount} ${goal.unit}",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = if (goal.isCompleted) AccentSuccess else MaterialTheme.colorScheme.onSurface
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          OutlinedIconButton(
            onClick = onDecrement,
            enabled = goal.currentCount > 0,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Diminuer", modifier = Modifier.size(16.dp))
          }

          FilledTonalIconButton(
            onClick = onIncrement,
            enabled = goal.currentCount < goal.targetCount,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Augmenter", modifier = Modifier.size(16.dp))
          }

          IconButton(
            onClick = onToggle,
            modifier = Modifier.size(36.dp).testTag("goal_toggle_${goal.id}")
          ) {
            Icon(
              imageVector = if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
              contentDescription = "Marquer complété",
              tint = if (goal.isCompleted) AccentSuccess else VertProfond,
              modifier = Modifier.size(28.dp)
            )
          }
        }
      }
    }
  }
}
