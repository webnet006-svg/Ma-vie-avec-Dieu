package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firestore.FirestoreGoal
import com.example.data.firestore.FirestorePrayer
import com.example.data.firestore.FirestoreSyncRepository
import com.example.data.firestore.FirestoreUserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class FirestoreSyncRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun `authenticated owner can save and observe their own prayer`() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = FirestoreSyncRepository(firestore, auth)

    val prayerId = "prayer_${UUID.randomUUID().toString().replace("-", "")}"
    val prayer = FirestorePrayer(
      id = prayerId,
      userId = aliceUid,
      title = "Prière pour la paix intérieure",
      description = "Seigneur, garde mon cœur dans la paix de Christ.",
      category = "VIE_PERSONNELLE",
      isAnswered = false,
      isPrivate = true,
      prayCount = 1
    )

    withTimeout(TIMEOUT_MS) {
      repository.savePrayer(prayer)
    }

    val observedPrayers = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observePrayers().first { list -> list.any { it.id == prayerId } }
    }

    assertTrue(observedPrayers.any { it.id == prayerId && it.title == prayer.title })
  }

  @Test
  fun `authenticated owner can save and observe their user profile`() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = FirestoreSyncRepository(firestore, auth)

    val profile = FirestoreUserProfile(
      userId = aliceUid,
      displayName = "Alice Martin",
      email = ALICE_EMAIL,
      denominationalAffiliation = "Église Réformée Évangélique",
      dailyNotificationTime = "08:00",
      offlineModeEnabled = false
    )

    withTimeout(TIMEOUT_MS) {
      repository.saveUserProfile(profile)
    }

    val observedProfile = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeUserProfile().first { it != null && it.displayName == "Alice Martin" }
    }

    assertNotNull(observedProfile)
    assertEquals("Alice Martin", observedProfile?.displayName)
    assertEquals("Église Réformée Évangélique", observedProfile?.denominationalAffiliation)
  }

  @Test
  fun `cross user access is strictly forbidden - Bob cannot read Alice prayers`() = runBlocking {
    // 1. Alice creates a prayer
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = FirestoreSyncRepository(firestore, auth)
    val prayerId = "prayer_alice_${UUID.randomUUID().toString().replace("-", "")}"
    aliceRepo.savePrayer(
      FirestorePrayer(
        id = prayerId,
        userId = aliceUid,
        title = "Prière confidentielle d'Alice",
        category = "FAMILLE",
        isAnswered = false,
        isPrivate = true,
        prayCount = 1
      )
    )

    // 2. Bob signs in and attempts to access Alice's prayer directly
    signInTestUser(BOB_EMAIL)
    val bobFirestore = firestore
    val alicePrayerDocRef = bobFirestore.collection("users").document(aliceUid).collection("prayers").document(prayerId)

    try {
      val doc: DocumentSnapshot = withTimeout(TIMEOUT_MS) {
        alicePrayerDocRef.get().await()
      }
      fail("Bob ne doit pas pouvoir lire la prière d'Alice : PERMISSION_DENIED attendu")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    } catch (e: Exception) {
      // In case wrapped, verify cause or permission denied
      assertTrue(e.message?.contains("PERMISSION_DENIED") == true || e is FirebaseFirestoreException)
    }
  }

  @Test
  fun `unauthenticated access is strictly rejected with permission denied`() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = FirestoreSyncRepository(firestore, auth)
    val prayerId = "prayer_${UUID.randomUUID().toString().replace("-", "")}"
    aliceRepo.savePrayer(
      FirestorePrayer(
        id = prayerId,
        userId = aliceUid,
        title = "Test sans auth",
        category = "AUTRE",
        isAnswered = false,
        isPrivate = true,
        prayCount = 1
      )
    )

    // Sign out to simulate unauthenticated caller
    auth.signOut()
    val unauthRepo = FirestoreSyncRepository(firestore, auth)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        unauthRepo.observePrayers().first()
      }
      fail("Une requête non authentifiée doit échouer")
    } catch (e: Exception) {
      // Expected to fail with IllegalStateException or PERMISSION_DENIED
      assertTrue(e is IllegalStateException || e is FirebaseFirestoreException)
    }
  }

  @Test
  fun `user can delete entire account data`() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repo = FirestoreSyncRepository(firestore, auth)

    val prayerId = "p_del_${UUID.randomUUID().toString().replace("-", "")}"
    repo.savePrayer(
      FirestorePrayer(
        id = prayerId,
        userId = aliceUid,
        title = "Prière avant suppression",
        category = "AUTRE"
      )
    )

    val goalId = "g_del_${UUID.randomUUID().toString().replace("-", "")}"
    repo.saveGoal(
      FirestoreGoal(
        id = goalId,
        userId = aliceUid,
        title = "Objectif avant suppression",
        targetDaysPerWeek = 5
      )
    )

    withTimeout(TIMEOUT_MS) {
      repo.deleteEntireAccountData()
    }

    // Verify document deletion
    val checkDoc = firestore.collection("users").document(aliceUid).get().await()
    assertFalse("Le document utilisateur doit être supprimé", checkDoc.exists())
  }

  private companion object {
    const val ALICE_EMAIL = "alice_test@mavieavecdieu.org"
    const val BOB_EMAIL = "bob_test@mavieavecdieu.org"
    const val TIMEOUT_MS = 6000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
