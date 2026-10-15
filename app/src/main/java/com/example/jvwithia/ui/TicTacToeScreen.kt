package com.example.jvwithia.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jvwithia.game.Difficulty
import com.example.jvwithia.game.GameMode
import com.example.jvwithia.game.GameResult
import com.example.jvwithia.game.GameUiState
import com.example.jvwithia.game.GameViewModel
import com.example.jvwithia.game.Player
import com.example.jvwithia.ui.theme.AccentBrush
import com.example.jvwithia.ui.theme.BackgroundBrush
import com.example.jvwithia.ui.theme.Glass
import com.example.jvwithia.ui.theme.GlassBorder
import com.example.jvwithia.ui.theme.GlassStrong
import com.example.jvwithia.ui.theme.JVWithIATheme
import com.example.jvwithia.ui.theme.Magenta
import com.example.jvwithia.ui.theme.OColor
import com.example.jvwithia.ui.theme.OnNightMuted
import com.example.jvwithia.ui.theme.Violet
import com.example.jvwithia.ui.theme.XColor

private val CardShape = RoundedCornerShape(24.dp)
private val PillShape = CircleShape

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
    // Fogos quando um humano vence: contra a IA só o jogador X; no PvP qualquer vencedor.
    val winner = (state.result as? GameResult.Win)?.player
    val celebrate = winner != null &&
        (state.mode == GameMode.PVP || winner == GameViewModel.HUMAN_PLAYER)

    Box(modifier = modifier.fillMaxSize()) {
        GameColumn(state, onCellClick, onNewGame, onModeChange, onDifficultyChange, onResetScore)
        // Sai da composição ao reiniciar, então cada nova vitória toca os fogos de novo.
        if (celebrate) {
            Fireworks(Modifier.matchParentSize())
        }
        // Quando a IA vence, ela "ri" do jogador.
        if (state.mode == GameMode.VS_AI && winner == GameViewModel.AI_PLAYER) {
            LaughingEmoji(Modifier.matchParentSize())
        }
    }
}

@Composable
private fun GameColumn(
    state: GameUiState,
    onCellClick: (Int) -> Unit,
    onNewGame: () -> Unit,
    onModeChange: (GameMode) -> Unit,
    onDifficultyChange: (Difficulty) -> Unit,
    onResetScore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header()

        PillSelector(
            options = GameMode.entries,
            selected = state.mode,
            label = { it.label },
            onSelect = onModeChange
        )

        AnimatedVisibility(
            visible = state.mode == GameMode.VS_AI,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SectionLabel("DIFICULDADE DA IA")
                Spacer(Modifier.height(8.dp))
                PillSelector(
                    options = Difficulty.entries,
                    selected = state.difficulty,
                    label = { it.label },
                    onSelect = onDifficultyChange
                )
            }
        }

        Scoreboard(state)

        StatusPill(state)

        Board(
            state = state,
            onCellClick = onCellClick,
            modifier = Modifier.widthIn(max = 380.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GradientButton("Nova partida", onClick = onNewGame, modifier = Modifier.weight(1f))
            GlassButton("Zerar placar", onClick = onResetScore, modifier = Modifier.weight(1f))
        }
    }
}

// ---------- Cabeçalho ----------

@Composable
private fun Header() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Jogo da Velha",
            style = MaterialTheme.typography.headlineLarge.merge(TextStyle(brush = AccentBrush))
        )
        Text(
            "com Inteligência Artificial",
            style = MaterialTheme.typography.bodyMedium,
            color = OnNightMuted
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = OnNightMuted)
}

// ---------- Superfícies ----------

private fun Modifier.glass(shape: Shape = CardShape, color: Color = Glass) =
    this
        .clip(shape)
        .background(color)
        .border(1.dp, GlassBorder, shape)

@Composable
private fun <T> PillSelector(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(PillShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            PillOption(
                text = label(option),
                selected = option == selected,
                onClick = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun RowScope.PillOption(text: String, selected: Boolean, onClick: () -> Unit) {
    val textColor by animateColorAsState(
        if (selected) Color.White else OnNightMuted, label = "pillText"
    )
    val selectedAlpha by animateFloatAsState(if (selected) 1f else 0f, label = "pillBg")
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(PillShape)
            .background(AccentBrush, alpha = selectedAlpha)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(PillShape)
            .background(AccentBrush)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GlassButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .glass(PillShape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.labelLarge)
    }
}

// ---------- Placar ----------

@Composable
private fun Scoreboard(state: GameUiState) {
    val vsAi = state.mode == GameMode.VS_AI
    val playing = !state.isGameOver
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ScoreTile(
            label = if (vsAi) "VOCÊ" else "JOGADOR X",
            value = state.score.xWins,
            accent = XColor,
            active = playing && state.currentPlayer == Player.X
        )
        ScoreTile(
            label = "EMPATES",
            value = state.score.draws,
            accent = OnNightMuted,
            active = false
        )
        ScoreTile(
            label = if (vsAi) "IA" else "JOGADOR O",
            value = state.score.oWins,
            accent = OColor,
            active = playing && state.currentPlayer == Player.O
        )
    }
}

@Composable
private fun RowScope.ScoreTile(label: String, value: Int, accent: Color, active: Boolean) {
    val borderColor by animateColorAsState(
        if (active) accent else GlassBorder, label = "tileBorder"
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(CardShape)
            .background(Glass)
            .border(if (active) 2.dp else 1.dp, borderColor, CardShape)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(accent, CircleShape)
            )
            Spacer(Modifier.size(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = OnNightMuted, maxLines = 1)
        }
        Spacer(Modifier.height(4.dp))
        AnimatedContent(
            targetState = value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "score"
        ) { v ->
            Text(
                v.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
        }
    }
}

// ---------- Status ----------

@Composable
private fun StatusPill(state: GameUiState) {
    val vsAi = state.mode == GameMode.VS_AI
    val text = when (val result = state.result) {
        is GameResult.Win -> when {
            !vsAi -> "Jogador ${result.player.symbol} venceu!"
            result.player == GameViewModel.HUMAN_PLAYER -> "Você venceu! 🎉"
            else -> "A IA venceu!"
        }
        GameResult.Draw -> "Deu velha! Empate."
        null -> when {
            state.isAiThinking -> "IA está pensando…"
            vsAi -> "Sua vez (${GameViewModel.HUMAN_PLAYER.symbol})"
            else -> "Vez do Jogador ${state.currentPlayer.symbol}"
        }
    }
    val highlight = state.result != null
    Row(
        modifier = Modifier
            .then(
                if (highlight) Modifier.clip(PillShape).background(AccentBrush)
                else Modifier.glass(PillShape, GlassStrong)
            )
            .padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.isAiThinking) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Magenta
            )
            Spacer(Modifier.size(10.dp))
        }
        AnimatedContent(
            targetState = text,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "status"
        ) { t ->
            Text(
                t,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------- Tabuleiro ----------

@Composable
private fun Board(state: GameUiState, onCellClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val winningLine = (state.result as? GameResult.Win)?.line.orEmpty()
    val humanTurn = !state.isAiThinking &&
        !(state.mode == GameMode.VS_AI && state.currentPlayer == GameViewModel.AI_PLAYER)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .glass(RoundedCornerShape(28.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
    val alpha by animateFloatAsState(if (dimmed) 0.35f else 1f, label = "cellAlpha")
    val highlightAlpha by animateFloatAsState(if (highlighted) 1f else 0f, label = "cellHighlight")
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha)
            .clip(shape)
            .background(Violet.copy(alpha = 0.16f))
            .background(
                Brush.linearGradient(listOf(Violet.copy(alpha = 0.55f), Magenta.copy(alpha = 0.45f))),
                alpha = highlightAlpha
            )
            .border(1.dp, GlassBorder, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = "$description: ${player?.symbol ?: "vazia"}" },
        contentAlignment = Alignment.Center
    ) {
        if (player != null) {
            Piece(player, Modifier.fillMaxSize(0.55f))
        }
    }
}

/** Desenha X ou O com traço arredondado, brilho suave e animação de "desenho". */
@Composable
private fun Piece(player: Player, modifier: Modifier = Modifier) {
    val progress = remember(player) { Animatable(0f) }
    LaunchedEffect(player) {
        progress.animateTo(1f, tween(durationMillis = 350, easing = FastOutSlowInEasing))
    }
    val color = if (player == Player.X) XColor else OColor
    Canvas(modifier) {
        val stroke = size.minDimension * 0.14f
        // Camada de brilho (mais larga e translúcida) seguida do traço principal
        drawPiece(player, progress.value, color.copy(alpha = 0.25f), stroke * 2.2f)
        drawPiece(player, progress.value, color, stroke)
    }
}

private fun DrawScope.drawPiece(player: Player, progress: Float, color: Color, strokeWidth: Float) {
    when (player) {
        Player.X -> {
            val first = (progress * 2f).coerceIn(0f, 1f)
            val second = (progress * 2f - 1f).coerceIn(0f, 1f)
            drawLine(
                color, Offset.Zero, Offset(size.width * first, size.height * first),
                strokeWidth, StrokeCap.Round
            )
            if (second > 0f) {
                drawLine(
                    color, Offset(size.width, 0f),
                    Offset(size.width * (1f - second), size.height * second),
                    strokeWidth, StrokeCap.Round
                )
            }
        }
        Player.O -> {
            // Mesmo raio para o brilho e o traço, independente da largura
            val inset = size.minDimension * 0.07f
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun TicTacToePreview() {
    JVWithIATheme {
        Box(Modifier.background(BackgroundBrush)) {
            TicTacToeContent(
                state = GameUiState(
                    board = listOf(Player.X, Player.O, null, null, Player.X, null, Player.O, null, null)
                ),
                onCellClick = {}, onNewGame = {}, onModeChange = {},
                onDifficultyChange = {}, onResetScore = {}
            )
        }
    }
}
