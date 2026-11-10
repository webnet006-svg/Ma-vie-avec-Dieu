package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleBook
import com.example.data.model.Testament
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val books = viewModel.bibleBooks
  val selectedBookId by viewModel.selectedBookId.collectAsState()
  val selectedChapterNum by viewModel.selectedChapterNumber.collectAsState()
  val currentChapter by viewModel.currentBibleChapter.collectAsState()
  val context = LocalContext.current

  var selectedTestament by remember { mutableStateOf(Testament.NOUVEAU) }
  var isBookSelectorOpen by remember { mutableStateOf(false) }
  var isChapterSelectorOpen by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var fontSizeSp by remember { mutableStateOf(16) }
  var highlightedVerses by remember { mutableStateOf(setOf<Int>()) }

  val currentBook = books.firstOrNull { it.id == selectedBookId } ?: books.first()

  val filteredBooks = remember(selectedTestament, searchQuery) {
    books.filter { it.testament == selectedTestament }
      .filter { searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { isBookSelectorOpen = true }
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${currentBook.name} $selectedChapterNum",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = VertProfond
                )
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = "Changer de livre",
                  tint = VertProfond
                )
              }
              Text(
                text = "${currentBook.category} • Louis Segond 1910",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          // Adjust font size button
          IconButton(
            onClick = {
              fontSizeSp = if (fontSizeSp >= 22) 14 else fontSizeSp + 2
            },
            modifier = Modifier.testTag("bible_font_size_button")
          ) {
            Text(
              text = "A+",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = VertProfond
            )
          }

          IconButton(
            onClick = { isChapterSelectorOpen = true },
            modifier = Modifier.testTag("bible_chapter_selector_button")
          ) {
            Icon(
              imageVector = Icons.Default.Numbers,
              contentDescription = "Sélectionner un chapitre",
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
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Testament switcher and search row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = selectedTestament == Testament.NOUVEAU,
          onClick = { selectedTestament = Testament.NOUVEAU },
          label = { Text("Nouveau Testament") },
          leadingIcon = if (selectedTestament == Testament.NOUVEAU) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
          } else null,
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = VertProfond,
            selectedLabelColor = Color.White
          ),
          modifier = Modifier.testTag("filter_nouveau_testament")
        )

        FilterChip(
          selected = selectedTestament == Testament.ANCIEN,
          onClick = { selectedTestament = Testament.ANCIEN },
          label = { Text("Ancien Testament") },
          leadingIcon = if (selectedTestament == Testament.ANCIEN) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
          } else null,
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = VertProfond,
            selectedLabelColor = Color.White
          ),
          modifier = Modifier.testTag("filter_ancien_testament")
        )
      }

      // Quick Horizontal Book Carousel
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
      ) {
        items(filteredBooks) { book ->
          val isSelected = book.id == currentBook.id
          SuggestionChip(
            onClick = {
              viewModel.selectBibleBook(book.id)
            },
            label = {
              Text(
                text = book.name,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
              containerColor = if (isSelected) OrDoux.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
            )
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Verses Reader
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        item {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
          ) {
            Text(
              text = currentBook.name.uppercase(),
              style = MaterialTheme.typography.headlineMedium,
              color = VertProfond,
              fontWeight = FontWeight.Bold,
              letterSpacing = 2.sp
            )
            Text(
              text = "Chapitre $selectedChapterNum",
              style = MaterialTheme.typography.titleMedium,
              color = OrDouxDark
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
              modifier = Modifier.width(60.dp),
              thickness = 2.dp,
              color = OrDoux
            )
          }
        }

        val chapter = currentChapter
        if (chapter == null) {
          item {
            Box(
              modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(color = OrDoux)
            }
          }
        } else {
          items(chapter.verses) { verse ->
          val isHighlighted = highlightedVerses.contains(verse.number)

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isHighlighted) OrDoux.copy(alpha = 0.2f) else Color.Transparent,
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                highlightedVerses = if (isHighlighted) {
                  highlightedVerses - verse.number
                } else {
                  highlightedVerses + verse.number
                }
              }
              .padding(vertical = 2.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.Top
            ) {
              Text(
                text = "${verse.number}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = OrDouxDark,
                modifier = Modifier
                  .width(28.dp)
                  .padding(top = 2.dp)
              )

              Text(
                text = verse.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                  fontSize = fontSizeSp.sp,
                  lineHeight = (fontSizeSp + 8).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
              )

              IconButton(
                onClick = {
                  val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                      Intent.EXTRA_TEXT,
                      "« ${verse.text} » — ${currentBook.name} $selectedChapterNum:${verse.number} (Louis Segond 1910)"
                    )
                    type = "text/plain"
                  }
                  context.startActivity(Intent.createChooser(sendIntent, "Partager ce verset"))
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Outlined.Share,
                  contentDescription = "Partager",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
          }
        }

        // Chapter Navigation Buttons
        item {
          Spacer(modifier = Modifier.height(16.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            OutlinedButton(
              onClick = {
                if (selectedChapterNum > 1) {
                  viewModel.selectBibleChapter(selectedChapterNum - 1)
                }
              },
              enabled = selectedChapterNum > 1,
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.ChevronLeft, contentDescription = null)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Chapitre précédent")
            }

            Button(
              onClick = {
                if (selectedChapterNum < currentBook.chaptersCount) {
                  viewModel.selectBibleChapter(selectedChapterNum + 1)
                }
              },
              enabled = selectedChapterNum < currentBook.chaptersCount,
              colors = ButtonDefaults.buttonColors(containerColor = VertProfond),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("Chapitre suivant")
              Spacer(modifier = Modifier.width(4.dp))
              Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
          }
        }
      }
    }
  }

  // Book Selector Bottom Sheet / Dialog
  if (isBookSelectorOpen) {
    AlertDialog(
      onDismissRequest = { isBookSelectorOpen = false },
      title = {
        Text("Choisir un Livre de la Bible", style = MaterialTheme.typography.titleLarge)
      },
      text = {
        Column(modifier = Modifier.heightIn(max = 420.dp)) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher un livre...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp)
          )

          LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(filteredBooks) { book ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (book.id == currentBook.id) VertProfond.copy(alpha = 0.15f) else Color.Transparent,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    viewModel.selectBibleBook(book.id)
                    isBookSelectorOpen = false
                  }
                  .padding(vertical = 4.dp)
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = book.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (book.id == currentBook.id) FontWeight.Bold else FontWeight.Normal
                  )
                  Text(
                    text = "${book.chaptersCount} chapitres",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { isBookSelectorOpen = false }) {
          Text("Fermer")
        }
      }
    )
  }

  // Chapter Selector Dialog
  if (isChapterSelectorOpen) {
    AlertDialog(
      onDismissRequest = { isChapterSelectorOpen = false },
      title = {
        Text("Chapitre dans ${currentBook.name}", style = MaterialTheme.typography.titleLarge)
      },
      text = {
        LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
          val chapters = (1..currentBook.chaptersCount).toList()
          val chunkedChapters = chapters.chunked(5)
          items(chunkedChapters) { row ->
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              row.forEach { ch ->
                val isSelected = ch == selectedChapterNum
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) VertProfond else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                      viewModel.selectBibleChapter(ch)
                      isChapterSelectorOpen = false
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "$ch",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { isChapterSelectorOpen = false }) {
          Text("Fermer")
        }
      }
    )
  }
}
