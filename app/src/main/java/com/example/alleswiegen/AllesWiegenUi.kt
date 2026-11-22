package com.example.alleswiegen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alleswiegen.screens.AllEvents
import com.example.alleswiegen.screens.BagelScreen
import com.example.alleswiegen.screens.BestAndWorstScreen
import com.example.alleswiegen.screens.Events

import com.example.alleswiegen.screens.SetupScreen
import com.example.alleswiegen.screens.InitialWeighingScreen
import com.example.alleswiegen.screens.ItemHandling
import com.example.alleswiegen.screens.OptionsScreen
import com.example.alleswiegen.screens.PickExxerScreen
import com.example.alleswiegen.screens.PlayerWeightingScreen
import com.example.alleswiegen.screens.RankingScreen
import com.example.alleswiegen.screens.SetTargetScreen
import com.example.alleswiegen.screens.TBDScreen
import com.example.alleswiegen.screens.WeighAfterExxenScreen
import com.example.alleswiegen.screens.WeighBeforeExxenScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllesWiegenApp(viewModel: GameViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    viewModel.setTimeFilter(30)

    if (uiState.showStartupMessage && uiState.showStartupDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissStartupDialog() },
            title = { Text("Hinweis") },
            text = {
                Column {
                    Text("Nutze die App verantwortungsvoll und trinke nur in moderaten Mengen, auch eine Runde Wasser-Wiegen kann nicht schaden.\nDu findest weitere Infos zu der App in den Optionen (Kompatible Hardware, alle Feature etc). \nDort kannst du auch diese Nachricht deaktivieren.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "📖 Spieleanleitung online öffnen",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            uriHandler.openUri("https://zygon-labs.github.io/alles-wiegen/")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissStartupDialog() }) {
                    Text("OK")
                }
            },

        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Alles Wiegen", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(targetState = uiState.currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    Screen.SETUP -> SetupScreen(viewModel, uiState)
                    Screen.INITIAL_WEIGHING -> InitialWeighingScreen(viewModel, uiState)
                    Screen.SET_TARGET -> SetTargetScreen(viewModel, uiState)
                    Screen.PlAYER_WEIGHING -> PlayerWeightingScreen(viewModel, uiState)
                    Screen.RANKING -> RankingScreen(viewModel, uiState)
                    Screen.PICK_EXXER -> PickExxerScreen(viewModel, uiState)
                    Screen.WEIGH_BEFORE_EXXEN -> WeighBeforeExxenScreen(viewModel, uiState)
                    Screen.WEIGH_AFTER_EXXEN -> WeighAfterExxenScreen(viewModel, uiState)
                    Screen.SHOW_BEST_AND_WORST -> BestAndWorstScreen(viewModel, uiState)
                    Screen.OPTIONS -> OptionsScreen(viewModel, uiState)
                    Screen.EVENT -> Events(viewModel, uiState)
                    Screen.ALL_EVENTS -> AllEvents(viewModel, uiState)
                    Screen.ITEM_HANDLING -> ItemHandling(viewModel, uiState)



                }
            }
        }
    }
}








