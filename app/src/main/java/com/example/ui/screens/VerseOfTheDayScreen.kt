package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyVerseEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseOfTheDayScreen(
  viewModel: MainViewModel,
  onBackClicked: () -> Unit,
  onNavigateToBible: (String, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBackClicked() }

  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val todayVerse by viewModel.todayVerseOfTheDay.collectAsState()
  val allVerses by viewModel.allDailyVerses.collectAsState()
  val favoriteVerses by viewModel.favoriteDailyVerses.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Aujourd'hui, 1: Versets Enregistrés
  var showAddVerseDialog by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedThemeFilter by remember { mutableStateOf<String?>(null) }
  var verseToDelete by remember { mutableStateOf<DailyVerseEntity?>(null) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Verset du Jour",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = VertProfond
            )
            Text(
              text = if (selectedTab == 0) "Parole vivante & méditation" else "${allVerses.size} versets sauvegardés",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBackClicked,
            modifier = Modifier.testTag("verse_screen_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Retour",
              tint = VertProfond
            )
          }
        },
        actions = {
          if (selectedTab == 0 && todayVerse != null) {
            val isFav = todayVerse?.isFavorite == true
            IconButton(
              onClick = {
                todayVerse?.let { v ->
                  viewModel.toggleDailyVerseFavorite(v.id, !isFav)
                  scope.launch {
                    snackbarHostState.showSnackbar(
                      if (!isFav) "Verset ajouté aux favoris !" else "Verset retiré des favoris."
                    )
                  }
                }
              },
              modifier = Modifier.testTag("verse_topbar_favorite_button")
            ) {
              Icon(
                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favori",
                tint = if (isFav) Color(0xFFE74C3C) else VertProfond
              )
            }

            IconButton(
              onClick = {
                todayVerse?.let { v ->
                  shareVerse(context, v)
                }
              },
              modifier = Modifier.testTag("verse_topbar_share_button")
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Partager",
                tint = VertProfond
              )
            }
          }

          IconButton(
            onClick = { showAddVerseDialog = true },
            modifier = Modifier.testTag("verse_topbar_add_button")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Enregistrer un verset",
              tint = VertProfond
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Primary Tabs: Verset du Jour vs Versets Enregistrés
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = VertProfond,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = VertProfond
          )
        }
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Aujourd'hui", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
            }
          },
          modifier = Modifier.testTag("verse_tab_today")
        )

        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "Enregistrés (${allVerses.size})",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
              )
            }
          },
          modifier = Modifier.testTag("verse_tab_saved")
        )
      }

      // Content of selected tab
      AnimatedContent(
        targetState = selectedTab,
        label = "verse_tab_content",
        modifier = Modifier.weight(1f)
      ) { tabIndex ->
        when (tabIndex) {
          0 -> {
            TodayVerseTabContent(
              verse = todayVerse,
              onToggleFavorite = { v ->
                val newFav = !v.isFavorite
                viewModel.toggleDailyVerseFavorite(v.id, newFav)
                scope.launch {
                  snackbarHostState.showSnackbar(
                    if (newFav) "Ajouté aux favoris !" else "Retiré des favoris."
                  )
                }
              },
              onCopyVerse = { text ->
                copyToClipboard(context, text)
                scope.launch {
                  snackbarHostState.showSnackbar("Verset copié dans le presse-papier !")
                }
              },
              onShareVerse = { v -> shareVerse(context, v) },
              onNavigateToBibleChapter = { book, chapter ->
                onNavigateToBible(book, chapter)
              }
            )
          }
          1 -> {
            SavedVersesTabContent(
              verses = allVerses,
              searchQuery = searchQuery,
              onSearchQueryChange = { searchQuery = it },
              selectedThemeFilter = selectedThemeFilter,
              onThemeFilterSelected = { selectedThemeFilter = it },
              onToggleFavorite = { v ->
                viewModel.toggleDailyVerseFavorite(v.id, !v.isFavorite)
              },
              onCopyVerse = { text ->
                copyToClipboard(context, text)
                scope.launch {
                  snackbarHostState.showSnackbar("Verset copié !")
                }
              },
              onShareVerse = { v -> shareVerse(context, v) },
              onDeleteVerse = { v -> verseToDelete = v },
              onAddVerseClicked = { showAddVerseDialog = true }
            )
          }
        }
      }
    }
  }

  // Dialog: Ajouter un nouveau verset dans la base Room
  if (showAddVerseDialog) {
    AddDailyVerseDialog(
      onDismiss = { showAddVerseDialog = false },
      onSave = { ref, text, book, chapter, verseNum, theme, reflection, prayer ->
        viewModel.saveCustomDailyVerse(
          reference = ref,
          text = text,
          book = book,
          chapter = chapter,
          verseNumber = verseNum,
          theme = theme,
          reflection = reflection,
          prayer = prayer
        )
        showAddVerseDialog = false
        scope.launch {
          snackbarHostState.showSnackbar("Verset enregistré avec succès dans la base !")
        }
      }
    )
  }

  // Dialog: Confirmer la suppression d'un verset
  verseToDelete?.let { verse ->
    AlertDialog(
      onDismissRequest = { verseToDelete = null },
      icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
      title = { Text("Supprimer ce verset ?") },
      text = { Text("Voulez-vous supprimer « ${verse.reference} » de vos versets enregistrés ?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteDailyVerse(verse.id)
            verseToDelete = null
            scope.launch { snackbarHostState.showSnackbar("Verset supprimé.") }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_delete_verse_button")
        ) {
          Text("Supprimer")
        }
      },
      dismissButton = {
        TextButton(onClick = { verseToDelete = null }) {
          Text("Annuler")
        }
      }
    )
  }
}

// =================================================================
// TAB 0 : CONTENU DU VERSET DU JOUR
// =================================================================

@Composable
private fun TodayVerseTabContent(
  verse: DailyVerseEntity?,
  onToggleFavorite: (DailyVerseEntity) -> Unit,
  onCopyVerse: (String) -> Unit,
  onShareVerse: (DailyVerseEntity) -> Unit,
  onNavigateToBibleChapter: (String, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  if (verse == null) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = VertProfond)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Chargement du verset du jour...",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
    return
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    contentPadding = PaddingValues(top = 18.dp, bottom = 40.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // 1. CARTE HERO DU VERSET
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("today_verse_hero_card")
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  VertProfond.copy(alpha = 0.08f),
                  OrDoux.copy(alpha = 0.12f),
                  MaterialTheme.colorScheme.surface
                )
              )
            )
            .padding(22.dp)
        ) {
          Column {
            // Header row with Badge and translation
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = VertProfond.copy(alpha = 0.12f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = VertProfond,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = verse.theme.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = VertProfond,
                    letterSpacing = 1.sp
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              ) {
                Text(
                  text = verse.translation,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quotation mark icon
            Icon(
              imageVector = Icons.Default.FormatQuote,
              contentDescription = null,
              tint = OrDoux,
              modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Main Scripture Verse Text
            Text(
              text = "« ${verse.text} »",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.SemiBold,
              fontStyle = FontStyle.Italic,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reference
            Row(
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(OrDoux)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = verse.reference,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VertProfond
              )
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Bar
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Toggle Favorite
              TextButton(
                onClick = { onToggleFavorite(verse) },
                colors = ButtonDefaults.textButtonColors(
                  contentColor = if (verse.isFavorite) Color(0xFFE74C3C) else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("verse_action_favorite")
              ) {
                Icon(
                  imageVector = if (verse.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (verse.isFavorite) "Favori" else "Enregistrer",
                  style = MaterialTheme.typography.labelMedium
                )
              }

              // Copy
              TextButton(
                onClick = { onCopyVerse("« ${verse.text} » — ${verse.reference} (${verse.translation})") },
                colors = ButtonDefaults.textButtonColors(contentColor = VertProfond),
                modifier = Modifier.testTag("verse_action_copy")
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copier", style = MaterialTheme.typography.labelMedium)
              }

              // Share
              TextButton(
                onClick = { onShareVerse(verse) },
                colors = ButtonDefaults.textButtonColors(contentColor = VertProfond),
                modifier = Modifier.testTag("verse_action_share")
              ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Partager", style = MaterialTheme.typography.labelMedium)
              }
            }
          }
        }
      }
    }

    // 2. BOUTON : LIRE DANS LE CONTEXTE BIBLIQUE
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToBibleChapter(verse.book, verse.chapter) }
          .testTag("verse_read_chapter_card")
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(VertProfond.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.MenuBook,
              contentDescription = null,
              tint = VertProfond,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Lire le chapitre complet",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Ouvrir ${verse.book} chapitre ${verse.chapter} dans la Bible",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = VertProfond
          )
        }
      }
    }

    // 3. PENSÉE & MÉDITATION DU JOUR
    if (verse.reflectionPrompt.isNotBlank()) {
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(OrDoux.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Lightbulb,
                  contentDescription = null,
                  tint = Color(0xFFB7791F),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Méditation & Pensée du Jour",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VertProfond
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = verse.reflectionPrompt,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 24.sp
            )
          }
        }
      }
    }

    // 4. PRIÈRE INSPIRÉE
    if (verse.prayerPrompt.isNotBlank()) {
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = VertProfond.copy(alpha = 0.06f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(VertProfond),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.VolunteerActivism,
                  contentDescription = null,
                  tint = OrDoux,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Prière Fondée sur le Verset",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VertProfond
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "« ${verse.prayerPrompt} »",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = Charbon,
                lineHeight = 24.sp,
                modifier = Modifier.padding(16.dp)
              )
            }
          }
        }
      }
    }
  }
}

// =================================================================
// TAB 1 : VERSETS ENREGISTRÉS & HISTORIQUE (ROOM DATABASE)
// =================================================================

@Composable
private fun SavedVersesTabContent(
  verses: List<DailyVerseEntity>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  selectedThemeFilter: String?,
  onThemeFilterSelected: (String?) -> Unit,
  onToggleFavorite: (DailyVerseEntity) -> Unit,
  onCopyVerse: (String) -> Unit,
  onShareVerse: (DailyVerseEntity) -> Unit,
  onDeleteVerse: (DailyVerseEntity) -> Unit,
  onAddVerseClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val themes = remember(verses) {
    verses.map { it.theme }.filter { it.isNotBlank() }.distinct()
  }

  val filteredVerses = remember(verses, searchQuery, selectedThemeFilter) {
    verses.filter { v ->
      val matchesSearch = searchQuery.isBlank() ||
        v.reference.contains(searchQuery, ignoreCase = true) ||
        v.text.contains(searchQuery, ignoreCase = true) ||
        v.book.contains(searchQuery, ignoreCase = true)

      val matchesTheme = selectedThemeFilter == null ||
        v.theme.equals(selectedThemeFilter, ignoreCase = true)

      matchesSearch && matchesTheme
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Search Bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Rechercher un verset, livre, mot-clé...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VertProfond) },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { onSearchQueryChange("") }) {
              Icon(Icons.Default.Close, contentDescription = "Effacer")
            }
          }
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = VertProfond,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("saved_verses_search_field")
      )
    }

    // 2. Filter chips row (All, Themes)
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedThemeFilter == null,
            onClick = { onThemeFilterSelected(null) },
            label = { Text("Tous (${verses.size})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = VertProfond,
              selectedLabelColor = Color.White
            )
          )
        }

        items(themes) { theme ->
          val isSelected = selectedThemeFilter == theme
          FilterChip(
            selected = isSelected,
            onClick = { onThemeFilterSelected(if (isSelected) null else theme) },
            label = { Text(theme) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = VertProfond,
              selectedLabelColor = Color.White
            )
          )
        }
      }
    }

    // 3. Header info & Add button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${filteredVerses.size} verset(s) affiché(s)",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TextButton(
          onClick = onAddVerseClicked,
          modifier = Modifier.testTag("saved_verses_add_action")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = VertProfond)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Ajouter", color = VertProfond, fontWeight = FontWeight.Bold)
        }
      }
    }

    // 4. Verses List or Empty State
    if (filteredVerses.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.BookmarkBorder,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (searchQuery.isNotBlank()) "Aucun verset ne correspond à votre recherche" else "Aucun verset enregistré",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      items(filteredVerses, key = { it.id }) { itemVerse ->
        SavedVerseCard(
          verse = itemVerse,
          onToggleFavorite = { onToggleFavorite(itemVerse) },
          onCopy = { onCopyVerse("« ${itemVerse.text} » — ${itemVerse.reference}") },
          onShare = { onShareVerse(itemVerse) },
          onDelete = { onDeleteVerse(itemVerse) }
        )
      }
    }
  }
}

// =================================================================
// ITEM CARD POUR VERSET SAUVEGARDÉ
// =================================================================

@Composable
private fun SavedVerseCard(
  verse: DailyVerseEntity,
  onToggleFavorite: () -> Unit,
  onCopy: () -> Unit,
  onShare: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = VertProfond.copy(alpha = 0.1f)
          ) {
            Text(
              text = verse.theme,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = VertProfond,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          if (verse.date.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = verse.date,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = if (verse.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Favori",
            tint = if (verse.isFavorite) Color(0xFFE74C3C) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "« ${verse.text} »",
        style = MaterialTheme.typography.bodyMedium,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "${verse.reference} • ${verse.translation}",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = VertProfond
      )

      if (verse.reflectionPrompt.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = verse.reflectionPrompt,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
          Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = VertProfond, modifier = Modifier.size(18.dp))
        }

        IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
          Icon(Icons.Default.Share, contentDescription = "Partager", tint = VertProfond, modifier = Modifier.size(18.dp))
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
          Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

// =================================================================
// DIALOG : ENREGISTRER UN NOUVEAU VERSET
// =================================================================

@Composable
private fun AddDailyVerseDialog(
  onDismiss: () -> Unit,
  onSave: (ref: String, text: String, book: String, chapter: Int, verseNum: String, theme: String, reflection: String, prayer: String) -> Unit
) {
  var reference by remember { mutableStateOf("") }
  var text by remember { mutableStateOf("") }
  var theme by remember { mutableStateOf("Méditation") }
  var reflection by remember { mutableStateOf("") }
  var prayer by remember { mutableStateOf("") }

  val themesList = listOf("Grâce", "Paix", "Espérance", "Foi", "Amour", "Force", "Courage", "Sagesse", "Pardon")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Enregistrer un Verset Biblique",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = VertProfond
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item {
          OutlinedTextField(
            value = reference,
            onValueChange = { reference = it },
            label = { Text("Référence (ex: Jean 14:27)") },
            placeholder = { Text("Ex: Romains 12:2") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("add_verse_reference_input")
          )
        }

        item {
          OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Texte du verset") },
            placeholder = { Text("Entrez la parole biblique...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("add_verse_text_input")
          )
        }

        item {
          Text(
            text = "Thème principal :",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(themesList) { t ->
              FilterChip(
                selected = theme == t,
                onClick = { theme = t },
                label = { Text(t, fontSize = 12.sp) }
              )
            }
          }
        }

        item {
          OutlinedTextField(
            value = reflection,
            onValueChange = { reflection = it },
            label = { Text("Méditation / Pensée personnelle (optionnel)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = prayer,
            onValueChange = { prayer = it },
            label = { Text("Prière inspirée (optionnel)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (reference.isNotBlank() && text.isNotBlank()) {
            val parts = reference.split(" ")
            val book = parts.firstOrNull() ?: "Livre"
            val chapterAndVerse = parts.getOrNull(1) ?: "1:1"
            val ch = chapterAndVerse.substringBefore(":").toIntOrNull() ?: 1
            val vNum = chapterAndVerse.substringAfter(":", "1")

            onSave(
              reference.trim(),
              text.trim(),
              book,
              ch,
              vNum,
              theme,
              reflection.trim(),
              prayer.trim()
            )
          }
        },
        enabled = reference.isNotBlank() && text.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = VertProfond),
        modifier = Modifier.testTag("add_verse_submit_button")
      ) {
        Text("Enregistrer")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Annuler")
      }
    }
  )
}

// Helpers
private fun copyToClipboard(context: Context, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val clip = ClipData.newPlainText("Verset Biblique", text)
  clipboard.setPrimaryClip(clip)
}

private fun shareVerse(context: Context, verse: DailyVerseEntity) {
  val shareText = "« ${verse.text} »\n— ${verse.reference} (${verse.translation})\n\nPartagé depuis Ma Vie avec Dieu"
  val intent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, shareText)
    type = "text/plain"
  }
  context.startActivity(Intent.createChooser(intent, "Partager le verset"))
}
