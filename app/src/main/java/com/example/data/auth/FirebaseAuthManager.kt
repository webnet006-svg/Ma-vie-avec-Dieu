package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.firestore.FirestoreSyncRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager(
  private val context: Context,
  auth: FirebaseAuth? = null
) {

  private val auth: FirebaseAuth = auth ?: run {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    Firebase.auth
  }

  private val credentialManager = CredentialManager.create(context)

  private val _currentUser = MutableStateFlow<FirebaseUser?>(this.auth.currentUser)
  val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

  private val _isGuestMode = MutableStateFlow(this.auth.currentUser == null)
  val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

  private val _authError = MutableStateFlow<String?>(null)
  val authError: StateFlow<String?> = _authError.asStateFlow()

  init {
    this.auth.addAuthStateListener { firebaseAuth ->
      _currentUser.value = firebaseAuth.currentUser
      _isGuestMode.value = (firebaseAuth.currentUser == null)
    }
  }

  fun setGuestMode(enabled: Boolean) {
    _isGuestMode.value = enabled
  }

  fun clearError() {
    _authError.value = null
  }

  // 1. Silent Auto-Sign-In on Startup
  fun attemptAutoSignIn(scope: CoroutineScope, onComplete: () -> Unit = {}) {
    if (auth.currentUser != null) {
      _currentUser.value = auth.currentUser
      _isGuestMode.value = false
      onComplete()
      return
    }

    val clientId = try {
      context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
      Log.w("Auth", "default_web_client_id not found for auto-sign-in")
      onComplete()
      return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
      .setFilterByAuthorizedAccounts(true)
      .setServerClientId(clientId)
      .setAutoSelectEnabled(true)
      .build()

    val request = GetCredentialRequest.Builder()
      .addCredentialOption(googleIdOption)
      .build()

    scope.launch {
      try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
          val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
          auth.signInWithCredential(authCredential).await()
          _isGuestMode.value = false
        }
      } catch (e: Exception) {
        // Silent auto-sign-in failure is normal on first run or emulators
        Log.d("Auth", "Silent auto-sign-in skipped or no saved credentials: ${e.message}")
      } finally {
        onComplete()
      }
    }
  }

  // 2. Interactive Google Sign-In
  fun signInWithGoogle(
    activity: Activity,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    val clientId = try {
      context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
      val msg = "Configuration Google Sign-In manquante (default_web_client_id introuvable)"
      _authError.value = msg
      onError(msg)
      return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
      .addCredentialOption(signInOption)
      .build()

    scope.launch {
      try {
        val result = credentialManager.getCredential(activity, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
          val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
          auth.signInWithCredential(authCredential).await()
          _isGuestMode.value = false
          _authError.value = null
          onSuccess()
        } else {
          val msg = "Type d'identifiant Google inattendu"
          _authError.value = msg
          onError(msg)
        }
      } catch (e: GetCredentialCancellationException) {
        Log.w("Auth", "Connexion Google annulée par l'utilisateur: ${e.message}", e)
        // Cancellation is not a hard error; don't set error message
      } catch (e: Exception) {
        Log.e("Auth", "Échec de la connexion Google", e)
        val msg = e.localizedMessage ?: "Échec de l'authentification Google"
        _authError.value = msg
        onError(msg)
      }
    }
  }

  // 3. Sign Out
  fun signOut(scope: CoroutineScope, onComplete: () -> Unit = {}) {
    auth.signOut()
    _isGuestMode.value = true
    scope.launch {
      try {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
      } catch (e: Exception) {
        Log.e("Auth", "Impossible d'effacer les identifiants locaux: ${e.message}", e)
      } finally {
        onComplete()
      }
    }
  }

  // 4. Delete Account and its data
  fun deleteAccount(
    firestoreSyncRepo: FirestoreSyncRepository,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    val user = auth.currentUser
    if (user == null) {
      onError("Aucun utilisateur connecté à supprimer.")
      return
    }

    scope.launch {
      try {
        // Step 1: Delete all Firestore cloud data for this user
        firestoreSyncRepo.deleteEntireAccountData()

        // Step 2: Delete Firebase Auth user
        user.delete().await()

        // Step 3: Clear local credentials
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        _isGuestMode.value = true
        onSuccess()
      } catch (e: Exception) {
        Log.e("Auth", "Erreur lors de la suppression du compte: ${e.message}", e)
        onError(e.localizedMessage ?: "Erreur lors de la suppression du compte")
      }
    }
  }
}
