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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.VolunteerActivism
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
import com.example.data.model.PrayerCategory
import com.example.data.model.PrayerItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val prayers by viewModel.prayers.collectAsState()
  val selectedCategory by viewModel.prayerCategoryFilter.collectAsState()
  val showOnlyAnswered by viewModel.showOnlyAnsweredPrayers.collectAsState()
  val searchQuery by viewModel.prayerSearchQuery.collectAsState()

  var isNewPrayerDialogOpen by remember { mutableStateOf(false) }
  var prayerToEdit by remember { mutableStateOf<PrayerItem?>(null) }
  var prayerToDelete by remember { mutableStateOf<PrayerItem?>(null) }
  var prayerToMarkAnswered by remember { mutableStateOf<PrayerItem?>(null) }
  var answeredTestimonyInput by remember { mutableStateOf("") }

  // Form states for add/edit
  var formTitle by remember { mutableStateOf("") }
  var formDescription by remember { mutableStateOf("") }
  var formCategory by remember { mutableStateOf(PrayerCategory.FAMILLE) }
  var formBiblicalRef by remember { mutableStateOf("") }

  val filteredPrayers = remember(prayers, selectedCategory, showOnlyAnswered, searchQuery) {
    prayers.filter { prayer ->
      val categoryMatch = selectedCategory == null || prayer.category == selectedCategory
      val answeredMatch = !showOnlyAnswered || prayer.isAnswered
      val searchMatch = searchQuery.isBlank() ||
        prayer.title.contains(searchQuery, ignoreCase = true) ||
        prayer.description.contains(searchQuery, ignoreCase = true)
      categoryMatch && answeredMatch && searchMatch
    }
  }

  val activeCount = prayers.count { !it.isAnswered }
  val answeredCount = prayers.count { it.isAnswered }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Journal de Prière",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = VertProfond
            )
            Text(
              text = "$activeCount actives • $answeredCount exaucées • Prières privées",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        actions = {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = VertProfond.copy(alpha = 0.1f),
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Privé",
                tint = VertProfond,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Privé",
                style = MaterialTheme.typography.labelSmall,
                color = VertProfond,
                fontWeight = FontWeight.Bold
              )
            }
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
          formCategory = PrayerCategory.FAMILLE
          formBiblicalRef = ""
          isNewPrayerDialogOpen = true
        },
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("Nouvelle Prière") },
        containerColor = VertProfond,
        contentColor = Color.White,
        modifier = Modifier
          .navigationBarsPadding()
          .testTag("fab_add_prayer")
      )
    }
  ) { paddingValues ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Search Bar for Personal History
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.updatePrayerSearch(it) },
        placeholder = { Text("Rechercher dans mon journal...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VertProfond) },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { viewModel.updatePrayerSearch("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Effacer")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 6.dp)
          .testTag("search_prayer_field")
      )

      // Category filter chips (famille, travail, études, santé, église, vie personnelle, autres)
      LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          FilterChip(
            selected = selectedCategory == null && !showOnlyAnswered,
            onClick = { viewModel.setPrayerFilter(null, false) },
            label = { Text("Toutes (${prayers.size})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = VertProfond,
              selectedLabelColor = Color.White
            )
          )
        }

        item {
          FilterChip(
            selected = showOnlyAnswered,
            onClick = { viewModel.setPrayerFilter(null, true) },
            label = { Text("Exaucées ($answeredCount)") },
            leadingIcon = {
              Icon(Icons.Default.Celebration, contentDescription = null, tint = AccentAnsweredGold, modifier = Modifier.size(16.dp))
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = OrDoux,
              selectedLabelColor = Charbon
            )
          )
        }

        items(PrayerCategory.entries) { category ->
          val count = prayers.count { it.category == category && (!showOnlyAnswered || it.isAnswered) }
          FilterChip(
            selected = selectedCategory == category && !showOnlyAnswered,
            onClick = { viewModel.setPrayerFilter(category, false) },
            label = { Text("${category.label} ($count)") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = VertProfond,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      if (filteredPrayers.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Outlined.VolunteerActivism,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = if (searchQuery.isNotBlank()) "Aucune prière ne correspond à la recherche" else "Aucune prière dans cette catégorie",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "« Présentez vos requêtes à Dieu par des prières et des supplications »",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
          contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          items(filteredPrayers, key = { it.id }) { prayer ->
            PrayerCardItemV2(
              prayer = prayer,
              onPrayClicked = { viewModel.incrementPrayerCount(prayer.id) },
              onMarkAnsweredClicked = {
                prayerToMarkAnswered = prayer
                answeredTestimonyInput = ""
              },
              onEditClicked = {
                formTitle = prayer.title
                formDescription = prayer.description
                formCategory = prayer.category
                formBiblicalRef = prayer.biblicalReference ?: ""
                prayerToEdit = prayer
              },
              onDeleteClicked = {
                prayerToDelete = prayer
              }
            )
          }
        }
      }
    }
  }

  // --- DIALOG: AJOUTER UNE PRIÈRE ---
  if (isNewPrayerDialogOpen) {
    PrayerFormDialog(
      dialogTitle = "Ajouter une Prière",
      submitButtonText = "Enregistrer la prière",
      titleValue = formTitle,
      onTitleChange = { formTitle = it },
      descriptionValue = formDescription,
      onDescriptionChange = { formDescription = it },
      categoryValue = formCategory,
      onCategoryChange = { formCategory = it },
      biblicalRefValue = formBiblicalRef,
      onBiblicalRefChange = { formBiblicalRef = it },
      onDismiss = { isNewPrayerDialogOpen = false },
      onSubmit = {
        viewModel.addPrayer(formTitle.trim(), formDescription.trim(), formCategory, formBiblicalRef.trim())
        isNewPrayerDialogOpen = false
      }
    )
  }

  // --- DIALOG: MODIFIER UNE PRIÈRE ---
  prayerToEdit?.let { prayer ->
    PrayerFormDialog(
      dialogTitle = "Modifier la Prière",
      submitButtonText = "Mettre à jour",
      titleValue = formTitle,
      onTitleChange = { formTitle = it },
      descriptionValue = formDescription,
      onDescriptionChange = { formDescription = it },
      categoryValue = formCategory,
      onCategoryChange = { formCategory = it },
      biblicalRefValue = formBiblicalRef,
      onBiblicalRefChange = { formBiblicalRef = it },
      onDismiss = { prayerToEdit = null },
      onSubmit = {
        viewModel.updatePrayer(prayer.id, formTitle.trim(), formDescription.trim(), formCategory, formBiblicalRef.trim())
        prayerToEdit = null
      }
    )
  }

  // --- DIALOG: SUPPRIMER UNE PRIÈRE ---
  prayerToDelete?.let { prayer ->
    AlertDialog(
      onDismissRequest = { prayerToDelete = null },
      icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
      title = { Text("Supprimer cette prière ?") },
      text = {
        Text("Êtes-vous sûr de vouloir supprimer « ${prayer.title} » ? Cette action est irréversible.")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deletePrayer(prayer.id)
            prayerToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_delete_prayer_button")
        ) {
          Text("Supprimer")
        }
      },
      dismissButton = {
        TextButton(onClick = { prayerToDelete = null }) {
          Text("Annuler")
        }
      }
    )
  }

  // --- DIALOG: MARQUER COMME EXAUCÉE ---
  prayerToMarkAnswered?.let { prayer ->
    AlertDialog(
      onDismissRequest = { prayerToMarkAnswered = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Celebration, contentDescription = null, tint = AccentAnsweredGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Prière Exaucée !", style = MaterialTheme.typography.titleLarge)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "« ${prayer.title} »",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = VertProfond
          )
          Text(
            text = "Rendez gloire à Dieu en écrivant votre témoignage de reconnaissance :",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = answeredTestimonyInput,
            onValueChange = { answeredTestimonyInput = it },
            placeholder = { Text("Témoignage de la fidélité de Dieu...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_answered_testimony")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.markPrayerAsAnswered(
              id = prayer.id,
              testimony = answeredTestimonyInput.ifBlank { "Dieu s'est manifesté avec fidélité et grâce !" }
            )
            prayerToMarkAnswered = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = AccentAnsweredGold, contentColor = Charbon),
          modifier = Modifier.testTag("submit_answered_testimony_button")
        ) {
          Text("Enregistrer le témoignage", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { prayerToMarkAnswered = null }) {
          Text("Annuler")
        }
      }
    )
  }
}

@Composable
fun PrayerFormDialog(
  dialogTitle: String,
  submitButtonText: String,
  titleValue: String,
  onTitleChange: (String) -> Unit,
  descriptionValue: String,
  onDescriptionChange: (String) -> Unit,
  categoryValue: PrayerCategory,
  onCategoryChange: (PrayerCategory) -> Unit,
  biblicalRefValue: String,
  onBiblicalRefChange: (String) -> Unit,
  onDismiss: () -> Unit,
  onSubmit: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(dialogTitle, style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = titleValue,
          onValueChange = onTitleChange,
          label = { Text("Titre de la prière *") },
          placeholder = { Text("Ex: Guérison de maman, Paix...") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_prayer_title")
        )

        OutlinedTextField(
          value = descriptionValue,
          onValueChange = onDescriptionChange,
          label = { Text("Détails et intentions") },
          placeholder = { Text("Partagez votre cœur et besoins...") },
          minLines = 3,
          maxLines = 5,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_prayer_description")
        )

        OutlinedTextField(
          value = biblicalRefValue,
          onValueChange = onBiblicalRefChange,
          label = { Text("Verset d'appui (optionnel)") },
          placeholder = { Text("Ex: Psaume 46:1, Jacques 5:16") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Text(
          text = "Catégorie :",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(PrayerCategory.entries) { cat ->
            FilterChip(
              selected = categoryValue == cat,
              onClick = { onCategoryChange(cat) },
              label = { Text(cat.label, fontSize = 12.sp) },
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
        onClick = onSubmit,
        enabled = titleValue.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = VertProfond),
        modifier = Modifier.testTag("submit_prayer_button")
      ) {
        Text(submitButtonText)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Annuler")
      }
    }
  )
}

@Composable
fun PrayerCardItemV2(
  prayer: PrayerItem,
  onPrayClicked: () -> Unit,
  onMarkAnsweredClicked: () -> Unit,
  onEditClicked: () -> Unit,
  onDeleteClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (prayer.isAnswered) OrDoux.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (prayer.isAnswered) AccentAnsweredGold.copy(alpha = 0.2f) else VertProfond.copy(alpha = 0.12f)
          ) {
            Text(
              text = if (prayer.isAnswered) "EXAUCÉE" else prayer.category.label.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              color = if (prayer.isAnswered) Charbon else VertProfond,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              fontSize = 10.sp
            )
          }

          if (prayer.isPrivate) {
            Icon(
              imageVector = Icons.Outlined.Lock,
              contentDescription = "Prière privée",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.size(14.dp)
            )
          }
        }

        Box {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (prayer.isAnswered) "Exaucée (${prayer.answeredDate})" else prayer.dateAdded,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(
              onClick = { menuExpanded = true },
              modifier = Modifier.size(32.dp).testTag("prayer_menu_${prayer.id}")
            ) {
              Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(18.dp))
            }
          }

          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
          ) {
            DropdownMenuItem(
              text = { Text("Modifier") },
              leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
              onClick = {
                menuExpanded = false
                onEditClicked()
              }
            )
            DropdownMenuItem(
              text = { Text("Supprimer", color = MaterialTheme.colorScheme.error) },
              leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
              onClick = {
                menuExpanded = false
                onDeleteClicked()
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = prayer.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      if (prayer.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = prayer.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 20.sp
        )
      }

      prayer.biblicalReference?.let { ref ->
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            tint = OrDouxDark,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = ref,
            style = MaterialTheme.typography.labelSmall,
            color = OrDouxDark,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Answered Testimony Banner
      if (prayer.isAnswered && !prayer.testimony.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = OrDoux.copy(alpha = 0.18f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Star, contentDescription = null, tint = AccentAnsweredGold, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Témoignage de reconnaissance :",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Charbon
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = prayer.testimony,
              style = MaterialTheme.typography.bodySmall,
              fontStyle = FontStyle.Italic,
              color = Charbon
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = VertProfondLight,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Priée ${prayer.prayerCount} fois",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (!prayer.isAnswered) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
              onClick = onPrayClicked,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.height(36.dp)
            ) {
              Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Prier", fontSize = 12.sp)
            }

            FilledTonalButton(
              onClick = onMarkAnsweredClicked,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = OrDoux.copy(alpha = 0.25f),
                contentColor = Charbon
              ),
              modifier = Modifier.height(36.dp)
            ) {
              Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Exaucée !", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
