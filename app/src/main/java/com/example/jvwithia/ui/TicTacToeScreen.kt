package com.example.jvwithia.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jvwithia.game.Difficulty
import com.example.jvwithia.game.GameMode
import com.example.jvwithia.game.GameResult
import com.example.jvwithia.game.GameUiState
import com.example.jvwithia.game.GameViewModel
import com.example.jvwithia.game.Player
import com.example.jvwithia.ui.theme.JVWithIATheme

private val XColor = Color(0xFF1E88E5)
private val OColor = Color(0xFFE53935)

@Composable
fun TicTacToeScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    TicTacToeContent(
        state = viewModel.state,
        onCellClick = viewModel::onCellClick,
        onNewGame = viewModel::newGame,
        onModeChange = viewModel::setMode,
        onDifficultyChange = viewModel::setDifficulty,
        onResetScore = viewModel::resetScore,
        modifier = modifier
    )
}

@Composable
fun TicTacToeContent(
    state: GameUiState,
    onCellClick: (Int) -> Unit,
    onNewGame: () -> Unit,
    onModeChange: (GameMode) -> Unit,
    onDifficultyChange: (Difficulty) -> Unit,
    onResetScore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Jogo da Velha",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        ModeSelector(state.mode, onModeChange)

        AnimatedVisibility(visible = state.mode == GameMode.VS_AI) {
            DifficultySelector(state.difficulty, onDifficultyChange)
        }

        Scoreboard(state)

        StatusPanel(state)

        Board(
            state = state,
            onCellClick = onCellClick,
            modifier = Modifier.widthIn(max = 360.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onNewGame) { Text("Reiniciar partida") }
            OutlinedButton(onClick = onResetScore) { Text("Zerar placar") }
        }
    }
}

@Composable
private fun ModeSelector(selected: GameMode, onSelect: (GameMode) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        GameMode.entries.forEachIndexed { i, mode ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(i, GameMode.entries.size)
            ) { Text(mode.label) }
        }
    }
}

@Composable
private fun DifficultySelector(selected: Difficulty, onSelect: (Difficulty) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Dificuldade da IA", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            Difficulty.entries.forEachIndexed { i, difficulty ->
                SegmentedButton(
                    selected = difficulty == selected,
                    onClick = { onSelect(difficulty) },
                    shape = SegmentedButtonDefaults.itemShape(i, Difficulty.entries.size)
                ) { Text(difficulty.label) }
            }
        }
    }
}

@Composable
private fun Scoreboard(state: GameUiState) {
    val vsAi = state.mode == GameMode.VS_AI
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreItem(if (vsAi) "Você (X)" else "Jogador X", state.score.xWins, XColor)
            ScoreItem("Empates", state.score.draws, MaterialTheme.colorScheme.onSurfaceVariant)
            ScoreItem(if (vsAi) "IA (O)" else "Jogador O", state.score.oWins, OColor)
        }
    }
}

@Composable
private fun ScoreItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(
            value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun StatusPanel(state: GameUiState) {
    val vsAi = state.mode == GameMode.VS_AI
    val text = when (val result = state.result) {
        is GameResult.Win -> when {
            !vsAi -> "Jogador ${result.player.symbol} venceu!"
            result.player == GameViewModel.HUMAN_PLAYER -> "Você venceu! 🎉"
            else -> "A IA venceu!"
        }
        GameResult.Draw -> "Deu velha! Empate."
        null -> when {
            state.isAiThinking -> "IA (${GameViewModel.AI_PLAYER.symbol}) está pensando…"
            vsAi -> "Sua vez (${GameViewModel.HUMAN_PLAYER.symbol})"
            else -> "Vez do Jogador ${state.currentPlayer.symbol}"
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.isAiThinking) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.size(8.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Board(state: GameUiState, onCellClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val winningLine = (state.result as? GameResult.Win)?.line.orEmpty()
    val humanTurn = !state.isAiThinking &&
        !(state.mode == GameMode.VS_AI && state.currentPlayer == GameViewModel.AI_PLAYER)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    Cell(
                        player = state.board[index],
                        highlighted = index in winningLine,
                        enabled = !state.isGameOver && humanTurn && state.board[index] == null,
                        dimmed = state.isGameOver && index !in winningLine,
                        onClick = { onCellClick(index) },
                        description = "Célula linha ${row + 1}, coluna ${col + 1}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun Cell(
    player: Player?,
    highlighted: Boolean,
    enabled: Boolean,
    dimmed: Boolean,
    onClick: () -> Unit,
    description: String,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(if (dimmed) 0.4f else 1f, label = "cellAlpha")
    val shape = RoundedCornerShape(12.dp)
    val background =
        if (highlighted) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(background, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = "$description: ${player?.symbol ?: "vazia"}" },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = player != null, enter = scaleIn() + fadeIn()) {
            Text(
                text = player?.symbol.orEmpty(),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = if (player == Player.X) XColor else OColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TicTacToePreview() {
    JVWithIATheme {
        TicTacToeContent(
            state = GameUiState(
                board = listOf(Player.X, Player.O, null, null, Player.X, null, Player.O, null, null)
            ),
            onCellClick = {}, onNewGame = {}, onModeChange = {},
            onDifficultyChange = {}, onResetScore = {}
        )
    }
}
