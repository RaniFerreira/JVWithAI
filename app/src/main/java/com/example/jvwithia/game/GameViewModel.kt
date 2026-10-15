package com.example.jvwithia.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Placar de um modo de jogo. Em [GameMode.VS_AI], X é o jogador humano e O é a IA. */
data class Score(val xWins: Int = 0, val oWins: Int = 0, val draws: Int = 0)

data class GameUiState(
    val board: List<Player?> = GameLogic.emptyBoard(),
    val currentPlayer: Player = Player.X,
    val startingPlayer: Player = Player.X,
    val result: GameResult? = null,
    val mode: GameMode = GameMode.VS_AI,
    val difficulty: Difficulty = Difficulty.HARD,
    val scores: Map<GameMode, Score> = GameMode.entries.associateWith { Score() },
    val isAiThinking: Boolean = false
) {
    val score: Score get() = scores.getValue(mode)
    val isGameOver: Boolean get() = result != null
}

/**
 * Mantém o estado da partida e o placar durante toda a sessão
 * (sobrevive a rotações de tela por ser um ViewModel).
 */
class GameViewModel(
    private val ai: TicTacToeAI = TicTacToeAI(),
    private val aiDelayMillis: Long = 500L
) : ViewModel() {

    var state by mutableStateOf(GameUiState())
        private set

    private var aiJob: Job? = null

    fun onCellClick(index: Int) {
        val s = state
        if (s.isGameOver || s.isAiThinking || s.board[index] != null) return
        if (s.mode == GameMode.VS_AI && s.currentPlayer == AI_PLAYER) return
        play(index)
        maybeTriggerAi()
    }

    /** Inicia uma nova partida mantendo o placar. Quem começa alterna a cada rodada. */
    fun newGame() {
        aiJob?.cancel()
        val starter = state.startingPlayer.opponent
        state = state.copy(
            board = GameLogic.emptyBoard(),
            currentPlayer = starter,
            startingPlayer = starter,
            result = null,
            isAiThinking = false
        )
        maybeTriggerAi()
    }

    fun setMode(mode: GameMode) {
        if (mode == state.mode) return
        state = state.copy(mode = mode)
        restartRound()
    }

    fun setDifficulty(difficulty: Difficulty) {
        if (difficulty == state.difficulty) return
        state = state.copy(difficulty = difficulty)
        restartRound()
    }

    fun resetScore() {
        state = state.copy(scores = state.scores + (state.mode to Score()))
    }

    /** Reinicia o tabuleiro com X começando (usado ao trocar modo/dificuldade). */
    private fun restartRound() {
        aiJob?.cancel()
        state = state.copy(
            board = GameLogic.emptyBoard(),
            currentPlayer = Player.X,
            startingPlayer = Player.X,
            result = null,
            isAiThinking = false
        )
    }

    private fun play(index: Int) {
        val s = state
        val board = s.board.toMutableList().also { it[index] = s.currentPlayer }
        val result = GameLogic.evaluate(board)
        state = s.copy(
            board = board,
            currentPlayer = if (result == null) s.currentPlayer.opponent else s.currentPlayer,
            result = result,
            scores = if (result != null) s.scores + (s.mode to s.score.add(result)) else s.scores
        )
    }

    private fun maybeTriggerAi() {
        val s = state
        if (s.mode != GameMode.VS_AI || s.isGameOver || s.currentPlayer != AI_PLAYER) return
        state = s.copy(isAiThinking = true)
        aiJob = viewModelScope.launch {
            delay(aiDelayMillis) // pequena pausa para a jogada da IA ser perceptível
            val move = ai.chooseMove(state.board, AI_PLAYER, state.difficulty)
            state = state.copy(isAiThinking = false)
            play(move)
        }
    }

    private fun Score.add(result: GameResult): Score = when (result) {
        is GameResult.Win -> if (result.player == Player.X) copy(xWins = xWins + 1) else copy(oWins = oWins + 1)
        GameResult.Draw -> copy(draws = draws + 1)
    }

    companion object {
        val HUMAN_PLAYER = Player.X
        val AI_PLAYER = Player.O
    }
}
