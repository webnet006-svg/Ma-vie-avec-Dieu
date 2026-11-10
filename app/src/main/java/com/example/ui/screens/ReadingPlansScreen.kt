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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReadingPlan
import com.example.data.model.ReadingPlanDay
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingPlansScreen(
  viewModel: MainViewModel,
  onBackClicked: () -> Unit,
  onNavigateToBibleWithRef: (bookId: String, chapter: Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val plans by viewModel.readingPlans.collectAsState()
  val selectedPlanId by viewModel.selectedPlanId.collectAsState()
  val planDays by viewModel.selectedPlanDays.collectAsState()

  var durationFilter by remember { mutableStateOf<Int?>(null) } // null = all, 7 = 7 days, 30 = 30 days

  val filteredPlans = remember(plans, durationFilter) {
    if (durationFilter == null) plans else plans.filter { it.durationDays == durationFilter }
  }

  val activePlan = plans.firstOrNull { it.id == selectedPlanId } ?: plans.firstOrNull()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Plans de Lecture Biblique",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = VertProfond
            )
            Text(
              text = "Parcours thématiques de 7 et 30 jours",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBackClicked,
            modifier = Modifier.testTag("reading_plans_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 20.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Duration Filter Chips
      item {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          FilterChip(
            selected = durationFilter == null,
            onClick = { durationFilter = null },
            label = { Text("Tous les plans (${plans.size})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = VertProfond,
              selectedLabelColor = Color.White
            )
          )

          FilterChip(
            selected = durationFilter == 7,
            onClick = { durationFilter = 7 },
            label = { Text("7 Jours") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = VertProfond,
              selectedLabelColor = Color.White
            )
          )

          FilterChip(
            selected = durationFilter == 30,
            onClick = { durationFilter = 30 },
            label = { Text("30 Jours") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = VertProfond,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      // Plans Carousel
      item {
        Text(
          text = "Choisissez votre parcours thématique",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold
        )
      }

      items(filteredPlans, key = { it.id }) { plan ->
        val isSelected = plan.id == activePlan?.id

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.selectReadingPlan(plan.id) }
            .testTag("reading_plan_card_${plan.id}"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) OrDoux.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = VertProfond.copy(alpha = 0.12f)
              ) {
                Text(
                  text = "${plan.durationDays} JOURS",
                  style = MaterialTheme.typography.labelSmall,
                  color = VertProfond,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  fontSize = 10.sp
                )
              }

              Text(
                text = "${plan.completedDaysCount} / ${plan.durationDays} complétés",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = plan.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = plan.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
              progress = { plan.progressPercent },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = if (plan.progressPercent >= 1f) AccentSuccess else VertProfond,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
          }
        }
      }

      // Detailed Days List for Active Plan
      activePlan?.let { plan ->
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Lectures : ${plan.title}",
              style = MaterialTheme.typography.titleMedium,
              color = VertProfond,
              fontWeight = FontWeight.Bold
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = OrDoux.copy(alpha = 0.2f)
            ) {
              Text(
                text = "${(plan.progressPercent * 100).toInt()}% accompli",
                style = MaterialTheme.typography.labelSmall,
                color = Charbon,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        items(planDays, key = { it.id }) { day ->
          ReadingPlanDayItem(
            day = day,
            onToggle = { viewModel.toggleReadingPlanDay(day.id, plan.id) },
            onReadInBible = {
              // Parse book & chapter (e.g. "Hébreux 11:1-6" -> HEB, 11)
              val bookCode = when {
                day.scriptureReference.startsWith("Héb") -> "HEB"
                day.scriptureReference.startsWith("Gen") -> "GEN"
                day.scriptureReference.startsWith("Rom") -> "ROM"
                day.scriptureReference.startsWith("Marc") -> "MAR"
                day.scriptureReference.startsWith("Matt") -> "MAT"
                day.scriptureReference.startsWith("Jacq") -> "JAC"
                day.scriptureReference.startsWith("1 Pier") -> "1PI"
                day.scriptureReference.startsWith("Luc") -> "LUC"
                day.scriptureReference.startsWith("Psa") -> "PSA"
                day.scriptureReference.startsWith("Jean") -> "JEA"
                day.scriptureReference.startsWith("Éph") -> "EPH"
                day.scriptureReference.startsWith("Phil") -> "PHI"
                day.scriptureReference.startsWith("Col") -> "COL"
                day.scriptureReference.startsWith("Prov") -> "PRO"
                day.scriptureReference.startsWith("1 Cor") -> "1CO"
                day.scriptureReference.startsWith("1 Jean") -> "1JE"
                else -> "PSA"
              }
              val chapterNumber = day.scriptureReference
                .substringAfter(" ")
                .substringBefore(":")
                .toIntOrNull() ?: 1
              onNavigateToBibleWithRef(bookCode, chapterNumber)
            }
          )
        }
      }
    }
  }
}

@Composable
fun ReadingPlanDayItem(
  day: ReadingPlanDay,
  onToggle: () -> Unit,
  onReadInBible: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (day.isCompleted) OrDoux.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onToggle,
        modifier = Modifier.testTag("toggle_reading_day_${day.dayNumber}")
      ) {
        Icon(
          imageVector = if (day.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
          contentDescription = "Marquer comme lu",
          tint = if (day.isCompleted) AccentSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(26.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = day.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = day.scriptureReference,
          style = MaterialTheme.typography.bodySmall,
          color = VertProfond,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = day.summary,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      FilledTonalButton(
        onClick = onReadInBible,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = VertProfond.copy(alpha = 0.1f),
          contentColor = VertProfond
        ),
        modifier = Modifier.height(34.dp)
      ) {
        Text("Lire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
