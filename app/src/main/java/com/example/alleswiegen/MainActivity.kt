package com.example.alleswiegen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.alleswiegen.ui.theme.AllesWiegenTheme

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { PlayerScoreRepository(database.playerScoreDao()) }

    private val viewModel: GameViewModel by viewModels {
        GameViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadSavedSettings(this)

        setContent {
            AllesWiegenTheme {
                AllesWiegenApp(viewModel)
            }
        }
    }
}
