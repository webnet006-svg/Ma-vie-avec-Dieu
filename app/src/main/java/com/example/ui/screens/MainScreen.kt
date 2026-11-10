package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.BottomNavItem
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavItems
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  var currentRoute by rememberSaveable { mutableStateOf(Screen.Home.route) }

  // Handle system back button smoothly
  BackHandler(enabled = currentRoute != Screen.Home.route) {
    currentRoute = Screen.Home.route
  }

  val isBottomBarVisible = currentRoute in listOf(
    Screen.Home.route,
    Screen.Bible.route,
    Screen.Prayer.route,
    Screen.Assistant.route,
    Screen.Profile.route
  )

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      if (isBottomBarVisible) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 8.dp,
          windowInsets = WindowInsets.navigationBars,
          modifier = Modifier.testTag("main_bottom_nav_bar")
        ) {
          bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
              selected = isSelected,
              onClick = { currentRoute = item.route },
              icon = {
                Icon(
                  imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                  contentDescription = item.title,
                  modifier = Modifier.size(24.dp)
                )
              },
              label = {
                Text(
                  text = item.title,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 11.sp
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VertProfond,
                selectedTextColor = VertProfond,
                indicatorColor = OrDoux.copy(alpha = 0.25f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.testTag(item.testTag)
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentRoute) {
        Screen.Home.route -> {
          HomeScreen(
            viewModel = viewModel,
            onNavigateToBible = { currentRoute = Screen.Bible.route },
            onNavigateToPrayer = { currentRoute = Screen.Prayer.route },
            onNavigateToAssistant = { currentRoute = Screen.Assistant.route },
            onNavigateToGoals = { currentRoute = Screen.Goals.route },
            onNavigateToDevotionDetail = { currentRoute = Screen.DevotionDetail.route },
            onNavigateToReadingPlans = { currentRoute = Screen.ReadingPlans.route },
            onNavigateToDevotionHistory = { currentRoute = Screen.DevotionHistory.route },
            onNavigateToVerseOfTheDay = { currentRoute = Screen.VerseOfTheDay.route }
          )
        }
        Screen.VerseOfTheDay.route -> {
          VerseOfTheDayScreen(
            viewModel = viewModel,
            onBackClicked = { currentRoute = Screen.Home.route },
            onNavigateToBible = { bookId, chapter ->
              viewModel.selectBibleBook(bookId)
              viewModel.selectBibleChapter(chapter)
              currentRoute = Screen.Bible.route
            }
          )
        }
        Screen.Bible.route -> {
          BibleScreen(
            viewModel = viewModel
          )
        }
        Screen.Prayer.route -> {
          PrayerScreen(viewModel = viewModel)
        }
        Screen.Assistant.route -> {
          AssistantScreen(viewModel = viewModel)
        }
        Screen.Profile.route -> {
          ProfileScreen(
            viewModel = viewModel,
            onNavigateToGoals = { currentRoute = Screen.Goals.route }
          )
        }
        Screen.Goals.route -> {
          GoalsScreen(
            viewModel = viewModel,
            onBackClicked = { currentRoute = Screen.Home.route }
          )
        }
        Screen.DevotionDetail.route -> {
          DevotionDetailScreen(
            viewModel = viewModel,
            onBackClicked = { currentRoute = Screen.Home.route },
            onNavigateToHistory = { currentRoute = Screen.DevotionHistory.route }
          )
        }
        Screen.DevotionHistory.route -> {
          DevotionHistoryScreen(
            viewModel = viewModel,
            onBackClicked = { currentRoute = Screen.Home.route },
            onDevotionSelected = {
              currentRoute = Screen.DevotionDetail.route
            }
          )
        }
        Screen.ReadingPlans.route -> {
          ReadingPlansScreen(
            viewModel = viewModel,
            onBackClicked = { currentRoute = Screen.Home.route },
            onNavigateToBibleWithRef = { bookId, chapter ->
              viewModel.selectBibleBook(bookId)
              viewModel.selectBibleChapter(chapter)
              currentRoute = Screen.Bible.route
            }
          )
        }
      }
    }
  }
}
