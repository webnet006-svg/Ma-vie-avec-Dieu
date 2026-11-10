package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.data.model.ThemeMode
import com.example.ui.navigation.Screen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MaVieAvecDieuTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Attempt silent auto-sign-in on startup
    viewModel.authManager.attemptAutoSignIn(lifecycleScope)

    setContent {
      val profile by viewModel.profile.collectAsState()
      val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
      val systemInDark = isSystemInDarkTheme()
      val isDarkTheme = when (profile.themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.BLEU -> false
        ThemeMode.VERT -> false
        ThemeMode.ROUGE -> false
        ThemeMode.VIOLET -> false
      }

      var currentRootScreen by rememberSaveable { mutableStateOf<String>(Screen.Splash.route) }

      MaVieAvecDieuTheme(darkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
          when (currentRootScreen) {
            Screen.Splash.route -> {
              SplashScreen(
                onSplashFinished = {
                  currentRootScreen = if (onboardingCompleted) {
                    Screen.Home.route
                  } else {
                    Screen.Onboarding.route
                  }
                }
              )
            }
            Screen.Onboarding.route -> {
              OnboardingScreen(
                onStartClicked = {
                  viewModel.completeOnboarding()
                  currentRootScreen = Screen.Home.route
                }
              )
            }
            else -> {
              MainScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}
