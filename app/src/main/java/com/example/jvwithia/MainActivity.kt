package com.example.jvwithia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.jvwithia.game.GameViewModel
import com.example.jvwithia.ui.TicTacToeScreen
import com.example.jvwithia.ui.theme.BackgroundBrush
import com.example.jvwithia.ui.theme.JVWithIATheme

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barras do sistema transparentes com ícones claros sobre o fundo roxo
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            JVWithIATheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundBrush),
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    TicTacToeScreen(
                        viewModel = gameViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
