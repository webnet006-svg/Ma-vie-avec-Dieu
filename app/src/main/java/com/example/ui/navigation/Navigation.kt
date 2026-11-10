package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
  object Splash : Screen("splash")
  object Onboarding : Screen("onboarding")
  object Home : Screen("home")
  object Bible : Screen("bible")
  object Prayer : Screen("prayer")
  object Assistant : Screen("assistant")
  object Profile : Screen("profile")
  object Goals : Screen("goals")
  object DevotionDetail : Screen("devotion_detail")
  object DevotionHistory : Screen("devotion_history")
  object ReadingPlans : Screen("reading_plans")
  object VerseOfTheDay : Screen("verse_of_the_day")
}

sealed class BottomNavItem(
  val route: String,
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
) {
  object Home : BottomNavItem(
    route = Screen.Home.route,
    title = "Accueil",
    selectedIcon = Icons.Filled.Home,
    unselectedIcon = Icons.Outlined.Home,
    testTag = "nav_item_home"
  )

  object Bible : BottomNavItem(
    route = Screen.Bible.route,
    title = "Bible",
    selectedIcon = Icons.Filled.Book,
    unselectedIcon = Icons.Outlined.Book,
    testTag = "nav_item_bible"
  )

  object Prayer : BottomNavItem(
    route = Screen.Prayer.route,
    title = "Prière",
    selectedIcon = Icons.Filled.VolunteerActivism,
    unselectedIcon = Icons.Outlined.VolunteerActivism,
    testTag = "nav_item_prayer"
  )

  object Assistant : BottomNavItem(
    route = Screen.Assistant.route,
    title = "Assistant IA",
    selectedIcon = Icons.Filled.AutoAwesome,
    unselectedIcon = Icons.Outlined.AutoAwesome,
    testTag = "nav_item_assistant"
  )

  object Profile : BottomNavItem(
    route = Screen.Profile.route,
    title = "Profil",
    selectedIcon = Icons.Filled.Person,
    unselectedIcon = Icons.Outlined.Person,
    testTag = "nav_item_profile"
  )
}

val bottomNavItems = listOf(
  BottomNavItem.Home,
  BottomNavItem.Bible,
  BottomNavItem.Prayer,
  BottomNavItem.Assistant,
  BottomNavItem.Profile
)
