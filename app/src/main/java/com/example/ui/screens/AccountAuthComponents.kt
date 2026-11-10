package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SyncState
import com.google.firebase.auth.FirebaseUser

@Composable
fun AccountSyncCard(
  currentUser: FirebaseUser?,
  isGuestMode: Boolean,
  syncStatus: SyncState,
  lastSyncTime: String?,
  onGoogleSignIn: () -> Unit,
  onSyncNow: () -> Unit,
  onSignOut: () -> Unit,
  onRequestDeleteAccount: () -> Unit,
  authErrorMessage: String?,
  modifier: Modifier = Modifier
) {
  val isConnected = (currentUser != null && !isGuestMode)

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isConnected) VertProfond.copy(alpha = 0.08f) else Color(0xFFFFF8E7)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isConnected) VertProfond else Color(0xFFE67E22)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isConnected) "Compte Cloud Connecté" else "Mode Invité (Stockage Local)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isConnected) VertProfond else Color(0xFFD35400)
          )
          Text(
            text = if (isConnected) {
              currentUser?.email ?: currentUser?.displayName ?: "Utilisateur connecté"
            } else {
              "Données conservées uniquement sur cet appareil"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (!authErrorMessage.isNullOrBlank()) {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Text(
            text = authErrorMessage,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(10.dp)
          )
        }
      }

      if (isConnected) {
        // Sync status details
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = syncStatus.label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = when (syncStatus) {
                  SyncState.IN_SYNC -> VertProfond
                  SyncState.SYNCING -> Color(0xFF2980B9)
                  SyncState.OFFLINE -> Color(0xFFE67E22)
                  SyncState.ERROR -> MaterialTheme.colorScheme.error
                  SyncState.IDLE -> MaterialTheme.colorScheme.onSurface
                }
              )
              if (lastSyncTime != null) {
                Text(
                  text = "Dernière synchro : $lastSyncTime",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            IconButton(
              onClick = onSyncNow,
              modifier = Modifier.testTag("account_sync_button")
            ) {
              Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Synchroniser maintenant",
                tint = VertProfond
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier
              .weight(1f)
              .testTag("account_sign_out_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VertProfond)
          ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Déconnexion")
          }

          TextButton(
            onClick = onRequestDeleteAccount,
            modifier = Modifier.testTag("account_delete_button"),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
          ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Supprimer")
          }
        }
      } else {
        // Guest mode call to action
        Text(
          text = "Connectez-vous pour synchroniser vos prières, votre progression biblique et vos objectifs spirituels sur Cloud Firestore.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = onGoogleSignIn,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("google_sign_in_button"),
          colors = ButtonDefaults.buttonColors(containerColor = VertProfond)
        ) {
          Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text("Se connecter avec Google")
        }
      }
    }
  }
}

@Composable
fun DeleteAccountDialog(
  onDismiss: () -> Unit,
  onConfirmDelete: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
    },
    title = {
      Text(
        text = "Supprimer définitivement le compte ?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.error
      )
    },
    text = {
      Text(
        text = "Cette action est irréversible. Toutes vos prières personnelles, objectifs spirituels, préférences et données cloud seront immédiatement et définitivement supprimées de Cloud Firestore.",
        style = MaterialTheme.typography.bodyMedium
      )
    },
    confirmButton = {
      Button(
        onClick = onConfirmDelete,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.testTag("confirm_delete_account_button")
      ) {
        Text("Supprimer tout")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Annuler")
      }
    }
  )
}
