package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scale = remember { Animatable(0.7f) }
  val alpha = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    scale.animateTo(
      targetValue = 1f,
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
      )
    )
  }

  LaunchedEffect(Unit) {
    alpha.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 1000)
    )
    delay(3000)
    onSplashFinished()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            VertProfond,
            Color(0xFF133E31),
            Color(0xFF0C271F)
          )
        )
      )
      .systemBarsPadding(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .padding(32.dp)
        .scale(scale.value)
    ) {
      // Elegant Logo Frame
      Box(
        modifier = Modifier
          .size(130.dp)
          .clip(CircleShape)
          .background(OrDoux.copy(alpha = 0.15f))
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.app_logo),
          contentDescription = "Logo Ma Vie avec Dieu",
          modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "Ma Vie avec Dieu",
        style = MaterialTheme.typography.displayMedium,
        color = OrDoux,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "« Chaque jour, un pas de plus avec Dieu. »",
        style = MaterialTheme.typography.bodyLarge,
        color = Ivoire,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "« Ta parole est une lampe à mes pieds,\nEt une lumière sur mon sentier. »\nPsaume 119:105",
        style = MaterialTheme.typography.bodyMedium,
        color = Ivoire.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(48.dp))

      Button(
        onClick = onSplashFinished,
        colors = ButtonDefaults.buttonColors(
          containerColor = OrDoux,
          contentColor = Charbon
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .testTag("splash_skip_button")
          .height(48.dp)
      ) {
        Text(
          text = "Entrer dans la présence",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}
