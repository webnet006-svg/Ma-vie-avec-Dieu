package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
  onStartClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding()
        ) {
          Button(
            onClick = onStartClicked,
            colors = ButtonDefaults.buttonColors(
              containerColor = VertProfond,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("onboarding_start_button")
          ) {
            Text(
              text = "Commencer ma marche quotidienne",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
          modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(OrDoux.copy(alpha = 0.2f))
            .padding(6.dp),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Logo",
            modifier = Modifier
              .size(84.dp)
              .clip(CircleShape)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Bienvenue dans",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
          text = "Ma Vie avec Dieu",
          style = MaterialTheme.typography.displayMedium,
          color = VertProfond,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
          color = OrDoux.copy(alpha = 0.15f),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier.padding(vertical = 4.dp)
        ) {
          Text(
            text = "« Chaque jour, un pas de plus avec Dieu. »",
            style = MaterialTheme.typography.titleSmall,
            color = Charbon,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontWeight = FontWeight.Medium
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Une application chrétienne pour nourrir votre intimité spirituelle, méditer les Écritures et persévérer dans la prière.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
      }

      item {
        OnboardingPillarCard(
          icon = Icons.Default.Book,
          title = "Méditation & Bible Quotidienne",
          description = "Recevez le verset du jour, une réflexion théologique inspirante et les 66 livres bibliques complets."
        )
      }

      item {
        OnboardingPillarCard(
          icon = Icons.Default.VolunteerActivism,
          title = "Journal de Prière Fervent",
          description = "Notez vos requêtes, intercédez pour vos proches et célébrez chaque prière exaucée par Dieu."
        )
      }

      item {
        OnboardingPillarCard(
          icon = Icons.Default.AutoAwesome,
          title = "Assistant Pastoral & Écritures",
          description = "Un conseiller biblique bienveillant pour répondre à vos questions spirituelles avec des versets précis."
        )
      }

      item {
        OnboardingPillarCard(
          icon = Icons.Default.TrendingUp,
          title = "Croissance & Objectifs de Foi",
          description = "Suivez votre régularité, mémorisez des versets et débloquez des jalons d'édification spirituelle."
        )
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
fun OnboardingPillarCard(
  icon: ImageVector,
  title: String,
  description: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(VertProfond.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = VertProfond,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 20.sp
        )
      }
    }
  }
}
