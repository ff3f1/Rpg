package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.RpgBottomNavigation
import com.example.ui.components.RpgTopAppBar
import com.example.ui.dialogs.CharacterCustomizationDialog
import com.example.ui.dialogs.CloudSyncDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RpgDarkBackground
import com.example.ui.theme.RpgGoldPrimary
import com.example.viewmodel.AppNavTab
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AetheriaRpgApp()
            }
        }
    }
}

@Composable
fun AetheriaRpgApp(viewModel: GameViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val playerProfile by viewModel.playerProfile.collectAsState()
    val currentWeather by viewModel.currentWeather.collectAsState()
    val liveHp by viewModel.liveHp.collectAsState()
    val liveMana by viewModel.liveMana.collectAsState()
    val liveStamina by viewModel.liveStamina.collectAsState()
    val alertMessage by viewModel.alertMessage.collectAsState()

    val isCustomizationOpen by viewModel.isCustomizationOpen.collectAsState()
    val isCloudDialogOpen by viewModel.isCloudDialogOpen.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(RpgDarkBackground),
        topBar = {
            RpgTopAppBar(
                profile = playerProfile,
                currentHp = liveHp,
                currentMana = liveMana,
                currentStamina = liveStamina,
                weather = currentWeather,
                cloudStatus = "🟢 Синхронизировано",
                onWeatherClick = { viewModel.cycleWeatherManually() },
                onCloudClick = { viewModel.openCloudDialog() },
                onCustomizeClick = { viewModel.openCustomization() }
            )
        },
        bottomBar = {
            RpgBottomNavigation(
                selectedTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Routing
            when (currentTab) {
                AppNavTab.WORLD -> OpenWorldGameScreen(viewModel = viewModel)
                AppNavTab.CHARACTER -> CharacterProgressionScreen(viewModel = viewModel)
                AppNavTab.CRAFTING -> CraftingScreen(viewModel = viewModel)
                AppNavTab.GUILD -> GuildAndTerritoryScreen(viewModel = viewModel)
                AppNavTab.STORY -> StoryNpcDialogScreen(viewModel = viewModel)
                AppNavTab.MARKET -> MarketplaceAndSocialScreen(viewModel = viewModel)
                AppNavTab.QUESTS -> QuestsAndAchievementsScreen(viewModel = viewModel)
            }

            // Floating In-Game Toast / Alert Banner
            AnimatedVisibility(
                visible = alertMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp, start = 16.dp, end = 16.dp)
            ) {
                alertMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xE61F2433))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = msg,
                            color = RpgGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Customization Modal
            if (isCustomizationOpen) {
                CharacterCustomizationDialog(viewModel = viewModel)
            }

            // Cloud Sync Modal
            if (isCloudDialogOpen) {
                CloudSyncDialog(viewModel = viewModel)
            }
        }
    }
}
