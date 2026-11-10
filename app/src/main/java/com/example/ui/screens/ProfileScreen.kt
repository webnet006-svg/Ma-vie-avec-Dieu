package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ThemeMode
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  viewModel: MainViewModel,
  onNavigateToGoals: () -> Unit,
  modifier: Modifier = Modifier
) {
  val profile by viewModel.profile.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val isGuestMode by viewModel.isGuestMode.collectAsState()
  val syncStatus by viewModel.syncStatus.collectAsState()
  val lastSyncTime by viewModel.lastSyncTime.collectAsState()
  val context = androidx.compose.ui.platform.LocalContext.current
  val activity = context as? android.app.Activity

  var showDeleteDialog by remember { mutableStateOf(false) }
  var authErrorMessage by remember { mutableStateOf<String?>(null) }
  var authSuccessMessage by remember { mutableStateOf<String?>(null) }

  var isEditingProfile by remember { mutableStateOf(false) }
  var nameInput by remember { mutableStateOf(profile.name) }
  var reminderInput by remember { mutableStateOf(profile.prayerReminderTime) }
  var selectedVersion by remember { mutableStateOf(profile.preferredVersion) }
  var selectedTheme by remember { mutableStateOf(profile.themeMode) }

  val bibleVersions = listOf("Louis Segond 1910")

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Profil & Paramètres",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = VertProfond
          )
        },
        actions = {
          IconButton(
            onClick = {
              nameInput = profile.name
              reminderInput = profile.prayerReminderTime
              selectedVersion = profile.preferredVersion
              selectedTheme = profile.themeMode
              isEditingProfile = true
            },
            modifier = Modifier.testTag("profile_edit_button")
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Modifier le profil", tint = VertProfond)
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
      contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // User Profile Header Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(VertProfond),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = profile.name.take(1).uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                color = OrDoux,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = profile.name,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Text(
              text = "Version biblique : ${profile.preferredVersion}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Ivoire,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "« ${profile.favoriteVerse} »",
                style = MaterialTheme.typography.bodySmall,
                color = Charbon,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp)
              )
            }
          }
        }
      }

      // Cloud Account & Sync Status Card
      item {
        AccountSyncCard(
          currentUser = currentUser,
          isGuestMode = isGuestMode,
          syncStatus = syncStatus,
          lastSyncTime = lastSyncTime,
          onGoogleSignIn = {
            if (activity != null) {
              authErrorMessage = null
              viewModel.signInWithGoogle(
                activity = activity,
                onSuccess = {
                  authSuccessMessage = "Connexion Google réussie !"
                },
                onError = { err ->
                  authErrorMessage = err
                }
              )
            }
          },
          onSyncNow = {
            viewModel.syncWithCloud()
          },
          onSignOut = {
            viewModel.signOut()
          },
          onRequestDeleteAccount = {
            showDeleteDialog = true
          },
          authErrorMessage = authErrorMessage
        )
      }

      // Stats Row
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StatCard(
            title = "Jours fidèles",
            value = "${profile.currentStreak}",
            icon = Icons.Default.Whatshot,
            accentColor = Color(0xFFE67E22),
            modifier = Modifier.weight(1f)
          )

          StatCard(
            title = "Prières exaucées",
            value = "${profile.totalPrayersAnswered}",
            icon = Icons.Default.Celebration,
            accentColor = AccentAnsweredGold,
            modifier = Modifier.weight(1f)
          )

          StatCard(
            title = "Chapitres lus",
            value = "${profile.totalChaptersRead}",
            icon = Icons.Default.Book,
            accentColor = VertProfond,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Navigation to Goals
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToGoals() }
            .testTag("profile_goals_card")
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(OrDoux.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.TrendingUp, contentDescription = null, tint = VertProfond)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Gérer mes Objectifs Spirituels",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Suivi de prière, lecture, mémorisation et badges",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }

      // Settings Section
      item {
        Text(
          text = "Préférences & Notifications",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            SettingRowItem(
              icon = Icons.Default.Schedule,
              title = "Rappel de prière du matin",
              subtitle = profile.prayerReminderTime
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            SettingRowItem(
              icon = Icons.Default.MenuBook,
              title = "Version de la Bible par défaut",
              subtitle = profile.preferredVersion
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            SettingRowItem(
              icon = Icons.Default.Palette,
              title = "Apparence & Thème",
              subtitle = profile.themeMode.label
            )
          }
        }
      }

      // About App Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = VertProfond.copy(alpha = 0.08f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Image(
              painter = painterResource(id = R.drawable.app_logo),
              contentDescription = null,
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
              Text(
                text = "Ma Vie avec Dieu • v1.0",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = VertProfond
              )
              Text(
                text = "« Chaque jour, un pas de plus avec Dieu. »",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Conçu pour l'édification des croyants francophones.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }
  }

  // Edit Profile Dialog
  if (isEditingProfile) {
    AlertDialog(
      onDismissRequest = { isEditingProfile = false },
      title = { Text("Modifier le Profil", style = MaterialTheme.typography.titleLarge) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Votre prénom") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_input_name")
          )

          OutlinedTextField(
            value = reminderInput,
            onValueChange = { reminderInput = it },
            label = { Text("Heure du rappel (ex: 07:00)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Text("Version de la Bible préférée :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            bibleVersions.forEach { ver ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedVersion = ver }
              ) {
                RadioButton(
                  selected = selectedVersion == ver,
                  onClick = { selectedVersion = ver }
                )
                Text(ver, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateProfile(
              name = nameInput.trim().ifBlank { "Disciple" },
              version = selectedVersion,
              reminderTime = reminderInput.trim().ifBlank { "07:00" },
              themeMode = selectedTheme
            )
            isEditingProfile = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = VertProfond),
          modifier = Modifier.testTag("profile_save_button")
        ) {
          Text("Enregistrer")
        }
      },
      dismissButton = {
        TextButton(onClick = { isEditingProfile = false }) {
          Text("Annuler")
        }
      }
    )
  }

  if (showDeleteDialog) {
    DeleteAccountDialog(
      onDismiss = { showDeleteDialog = false },
      onConfirmDelete = {
        showDeleteDialog = false
        viewModel.deleteAccount(
          onSuccess = {
            authSuccessMessage = "Compte et données supprimés."
          },
          onError = { err ->
            authErrorMessage = err
          }
        )
      }
    )
  }
}

@Composable
fun StatCard(
  title: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(accentColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        fontSize = 11.sp
      )
    }
  }
}

@Composable
fun SettingRowItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = VertProfond,
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
