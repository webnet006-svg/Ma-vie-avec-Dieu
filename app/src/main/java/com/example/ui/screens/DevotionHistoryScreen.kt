package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Devotion
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevotionHistoryScreen(
  viewModel: MainViewModel,
  onBackClicked: () -> Unit,
  onDevotionSelected: (Devotion) -> Unit,
  modifier: Modifier = Modifier
) {
  val devotions by viewModel.allDevotions.collectAsState()
  var filterOnlyCompleted by remember { mutableStateOf<Boolean?>(null) } // null = all, true = completed, false = pending

  val filteredDevotions = remember(devotions, filterOnlyCompleted) {
    when (filterOnlyCompleted) {
      true -> devotions.filter { it.isCompleted }
      false -> devotions.filter { !it.isCompleted }
      null -> devotions
    }
  }

  val completedCount = devotions.count { it.isCompleted }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Historique des Méditations",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = VertProfond
            )
            Text(
              text = "$completedCount terminées sur ${devotions.size} parcours",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBackClicked,
            modifier = Modifier.testTag("devotion_history_back_button")
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
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Filter Chips
      LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          FilterChip(
            selected = filterOnlyCompleted == null,
            onClick = { filterOnlyCompleted = null },
            label = { Text("Toutes (${devotions.size})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = VertProfond,
              selectedLabelColor = Color.White
            )
          )
        }
        item {
          FilterChip(
            selected = filterOnlyCompleted == true,
            onClick = { filterOnlyCompleted = true },
            label = { Text("Terminées ($completedCount)") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AccentSuccess,
              selectedLabelColor = Color.White
            )
          )
        }
        item {
          FilterChip(
            selected = filterOnlyCompleted == false,
            onClick = { filterOnlyCompleted = false },
            label = { Text("À méditer (${devotions.size - completedCount})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = OrDoux,
              selectedLabelColor = Charbon
            )
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredDevotions, key = { it.id }) { item ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                viewModel.selectDevotionForDetail(item)
                onDevotionSelected(item)
              }
              .testTag("devotion_item_${item.dayOrder}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (item.isCompleted) OrDoux.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp),
                contentAlignment = Alignment.Center
              ) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (item.isCompleted) AccentSuccess.copy(alpha = 0.15f) else VertProfond.copy(alpha = 0.1f)
                ) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "J${item.dayOrder}",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = if (item.isCompleted) AccentSuccess else VertProfond
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.title,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = item.verse.reference,
                  style = MaterialTheme.typography.bodySmall,
                  color = OrDouxDark,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (item.isCompleted) AccentSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (item.isCompleted) "Terminée" else "À découvrir",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isCompleted) AccentSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                }
              }

              Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ouvrir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}
