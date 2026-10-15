package com.example.jvwithia.game

/** Marcadores possíveis em uma célula do tabuleiro. */
enum class Player(val symbol: String) {
    X("X"),
    O("O");

    val opponent: Player
        get() = if (this == X) O else X
}

/** Modo de jogo: contra a IA ou dois jogadores locais. */
enum class GameMode(val label: String) {
    VS_AI("Jogador vs IA"),
    PVP("Jogador vs Jogador")
}

/** Níveis de dificuldade da IA. */
enum class Difficulty(val label: String) {
    EASY("Fácil"),
    MEDIUM("Médio"),
    HARD("Difícil")
}

/** Resultado de uma partida finalizada. */
sealed interface GameResult {
    data class Win(val player: Player, val line: List<Int>) : GameResult
    data object Draw : GameResult
}

/**
 * Regras do Jogo da Velha. O tabuleiro é uma lista de 9 posições
 * (índices 0..8, linha a linha), onde `null` representa célula vazia.
 */
object GameLogic {

    val WINNING_LINES: List<List<Int>> = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // linhas
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // colunas
        listOf(0, 4, 8), listOf(2, 4, 6)                   // diagonais
    )

    fun emptyBoard(): List<Player?> = List(9) { null }

    fun availableMoves(board: List<Player?>): List<Int> =
        board.indices.filter { board[it] == null }

    /** Retorna o resultado da partida, ou `null` se ela ainda não terminou. */
    fun evaluate(board: List<Player?>): GameResult? {
        for (line in WINNING_LINES) {
            val (a, b, c) = line
            val p = board[a]
            if (p != null && p == board[b] && p == board[c]) {
                return GameResult.Win(p, line)
            }
        }
        return if (board.all { it != null }) GameResult.Draw else null
    }
}
