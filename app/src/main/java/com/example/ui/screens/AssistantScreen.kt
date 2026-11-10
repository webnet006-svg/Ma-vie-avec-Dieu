package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.ai.model.EliMessage
import com.example.data.ai.verifier.VerifiedCitationResult
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val messages by viewModel.eliMessages.collectAsState()
  val inputMessage by viewModel.chatInput.collectAsState()
  val isThinking by viewModel.isChatThinking.collectAsState()
  val quotaState by viewModel.eliQuotaState.collectAsState()
  val selectedModel by viewModel.selectedAiModel.collectAsState()

  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  var messageToReport by remember { mutableStateOf<EliMessage?>(null) }
  var reportReason by remember { mutableStateOf("") }
  var isModelMenuOpen by remember { mutableStateOf(false) }

  val quickQuestions = listOf(
    "Expliquer Romains 8:28 dans son contexte",
    "Que dit la Bible sur le baptême ?",
    "Comment avoir l'assurance de mon salut ?",
    "Une prière pour surmonter l'anxiété",
    "La différence entre grâce et œuvres",
    "Créer un plan de lecture sur la foi"
  )

  LaunchedEffect(messages.size, isThinking) {
    if (messages.isNotEmpty()) {
      coroutineScope.launch {
        listState.animateScrollToItem(messages.size - 1)
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(VertProfond),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = OrDoux,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Eli • Assistant Biblique",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = VertProfond
                )
              }
              Text(
                text = "Orientation protestante • $selectedModel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
          }
        },
        actions = {
          // Quota chip
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (quotaState.isExceeded) MaterialTheme.colorScheme.errorContainer else OrDoux.copy(alpha = 0.2f),
            modifier = Modifier.padding(end = 4.dp)
          ) {
            Text(
              text = "${quotaState.remaining}/20 restants",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (quotaState.isExceeded) MaterialTheme.colorScheme.error else Charbon,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              fontSize = 10.sp
            )
          }

          // Model Selector Menu
          Box {
            IconButton(
              onClick = { isModelMenuOpen = true },
              modifier = Modifier.testTag("ai_model_menu_button")
            ) {
              Icon(Icons.Default.Tune, contentDescription = "Paramètres du modèle IA", tint = VertProfond)
            }
            DropdownMenu(
              expanded = isModelMenuOpen,
              onDismissRequest = { isModelMenuOpen = false }
            ) {
              DropdownMenuItem(
                text = { Text("gemini-2.5-flash (Par défaut)") },
                onClick = {
                  viewModel.setSelectedAiModel("gemini-2.5-flash")
                  isModelMenuOpen = false
                }
              )
              DropdownMenuItem(
                text = { Text("gemini-3.5-flash (Avancé)") },
                onClick = {
                  viewModel.setSelectedAiModel("gemini-3.5-flash")
                  isModelMenuOpen = false
                }
              )
              HorizontalDivider()
              DropdownMenuItem(
                text = { Text("Effacer l'historique de discussion") },
                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
                onClick = {
                  viewModel.clearEliConversation()
                  isModelMenuOpen = false
                }
              )
            }
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
        ) {
          // Suggested prompts chips
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
          ) {
            items(quickQuestions) { prompt ->
              SuggestionChip(
                onClick = { viewModel.sendEliQuestion(prompt) },
                label = { Text(prompt, fontSize = 12.sp) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                  containerColor = OrDoux.copy(alpha = 0.12f)
                )
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = inputMessage,
              onValueChange = { viewModel.onChatInputChange(it) },
              placeholder = { Text("Posez votre question biblique à Eli...") },
              modifier = Modifier
                .weight(1f)
                .testTag("assistant_input_field"),
              shape = RoundedCornerShape(24.dp),
              maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
              onClick = { viewModel.sendEliQuestion() },
              enabled = inputMessage.isNotBlank() && !isThinking,
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (inputMessage.isNotBlank() && !isThinking) VertProfond else MaterialTheme.colorScheme.surfaceVariant)
                .testTag("assistant_send_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Envoyer",
                tint = if (inputMessage.isNotBlank() && !isThinking) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      state = listState,
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Pastoral & Educational Disclaimer Card
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Ivoire),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = null,
              tint = VertProfond,
              modifier = Modifier.size(20.dp).padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Eli est un assistant pédagogique conçu pour vous accompagner dans l'étude des Écritures. Ses réponses peuvent comporter des erreurs : examinez toujours les Écritures comme les Béréens (Actes 17:11) et partagez avec vos pasteurs.",
              style = MaterialTheme.typography.bodySmall,
              color = Charbon,
              lineHeight = 18.sp,
              fontSize = 11.sp
            )
          }
        }
      }

      // 2. Chat messages
      items(messages, key = { it.id }) { msg ->
        EliMessageBubble(
          message = msg,
          onCopy = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Réponse d'Eli", msg.content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Réponse copiée dans le presse-papiers", Toast.LENGTH_SHORT).show()
          },
          onReport = {
            messageToReport = msg
            reportReason = ""
          },
          onPromptClick = { prompt ->
            viewModel.sendEliQuestion(prompt)
          }
        )
      }

      // 3. Thinking indicator
      if (isThinking) {
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = VertProfond
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Eli consulte les Saintes Écritures...",
              style = MaterialTheme.typography.bodySmall,
              fontStyle = FontStyle.Italic,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }

  // Report Inaccuracy Dialog
  messageToReport?.let { msg ->
    AlertDialog(
      onDismissRequest = { messageToReport = null },
      icon = { Icon(Icons.Outlined.Flag, contentDescription = null, tint = OrDouxDark) },
      title = { Text("Signaler une Réponse", style = MaterialTheme.typography.titleLarge) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Vous avez remarqué une inexactitude théologique ou un verset mal cité ? Décrivez le problème pour améliorer la rigueur d'Eli :",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = reportReason,
            onValueChange = { reportReason = it },
            placeholder = { Text("Ex: Le verset cité n'a pas ce sens dans le contexte...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth().testTag("input_report_reason")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.reportEliMessage(msg.id, reportReason)
            messageToReport = null
            Toast.makeText(context, "Merci pour votre signalement théologique.", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = VertProfond),
          modifier = Modifier.testTag("submit_report_button")
        ) {
          Text("Envoyer le signalement")
        }
      },
      dismissButton = {
        TextButton(onClick = { messageToReport = null }) {
          Text("Annuler")
        }
      }
    )
  }
}

@Composable
fun EliMessageBubble(
  message: EliMessage,
  onCopy: () -> Unit,
  onReport: () -> Unit,
  onPromptClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val isUser = message.isUser

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Top
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(VertProfond),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "E",
          style = MaterialTheme.typography.titleMedium,
          color = OrDoux,
          fontWeight = FontWeight.Bold
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Surface(
      shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp
      ),
      color = if (isUser) VertProfond else MaterialTheme.colorScheme.surface,
      tonalElevation = if (isUser) 0.dp else 2.dp,
      modifier = Modifier.widthIn(max = 330.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        // Main Explanation Content
        Text(
          text = message.content,
          style = MaterialTheme.typography.bodyMedium,
          color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
          lineHeight = 22.sp
        )

        // Verified Scripture Citations Cards
        if (!isUser && message.verifiedCitations.isNotEmpty()) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Passages Bibliques Vérifiés :",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = VertProfond
          )
          Spacer(modifier = Modifier.height(6.dp))

          message.verifiedCitations.forEach { citation ->
            VerifiedCitationCard(citation = citation)
            Spacer(modifier = Modifier.height(6.dp))
          }
        }

        // Theological Perspectives Nuances Block
        if (!isUser && !message.theologicalPerspectives.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = OrDoux.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = OrDouxDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Perspectives Théologiques Protestantes :",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Charbon
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = message.theologicalPerspectives,
                style = MaterialTheme.typography.bodySmall,
                color = Charbon,
                lineHeight = 18.sp,
                fontSize = 11.sp
              )
            }
          }
        }

        // Suggested Guided Prayer
        if (!isUser && !message.suggestedPrayer.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = VertProfond.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = VertProfond, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Prière guidée suggérée :",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = VertProfond
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = message.suggestedPrayer,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
              )
            }
          }
        }

        // Suggested Follow-up Questions
        if (!isUser && message.suggestedQuestions.isNotEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Approfondir avec Eli :",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          message.suggestedQuestions.forEach { question ->
            Text(
              text = "• $question",
              style = MaterialTheme.typography.bodySmall,
              color = VertProfond,
              modifier = Modifier
                .clickable { onPromptClick(question) }
                .padding(vertical = 2.dp),
              fontSize = 12.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Actions: Timestamp, Copy, Report
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!isUser) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              IconButton(
                onClick = onCopy,
                modifier = Modifier.size(28.dp).testTag("copy_button_${message.id}")
              ) {
                Icon(
                  imageVector = Icons.Outlined.ContentCopy,
                  contentDescription = "Copier la réponse",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp)
                )
              }

              IconButton(
                onClick = onReport,
                modifier = Modifier.size(28.dp).testTag("report_button_${message.id}")
              ) {
                Icon(
                  imageVector = Icons.Outlined.Flag,
                  contentDescription = "Signaler une inexactitude",
                  tint = if (message.isReported) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          } else {
            Spacer(modifier = Modifier.weight(1f))
          }

          Text(
            text = message.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
          )
        }
      }
    }

    if (isUser) {
      Spacer(modifier = Modifier.width(8.dp))
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(OrDoux),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = null,
          tint = Charbon,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun VerifiedCitationCard(
  citation: VerifiedCitationResult,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (citation.isVerified) Ivoire else MaterialTheme.colorScheme.surfaceVariant,
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            tint = VertProfond,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = citation.reference,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = VertProfond
          )
        }

        Surface(
          shape = RoundedCornerShape(4.dp),
          color = if (citation.isVerified) AccentSuccess.copy(alpha = 0.15f) else OrDoux.copy(alpha = 0.25f)
        ) {
          Text(
            text = if (citation.isVerified) "VÉRIFIÉ" else "À VÉRIFIER",
            style = MaterialTheme.typography.labelSmall,
            color = if (citation.isVerified) AccentSuccess else Charbon,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            fontSize = 9.sp
          )
        }
      }

      citation.exactText?.let { text ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "« $text »",
          style = MaterialTheme.typography.bodySmall,
          fontStyle = FontStyle.Italic,
          color = Charbon,
          lineHeight = 17.sp
        )
      }

      citation.historicalContext?.let { context ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Contexte : $context",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp,
          lineHeight = 15.sp
        )
      }

      citation.warningNote?.let { note ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = note,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          fontSize = 11.sp
        )
      }
    }
  }
}
