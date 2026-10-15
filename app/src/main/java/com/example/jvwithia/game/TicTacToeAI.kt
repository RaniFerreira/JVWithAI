package com.example.jvwithia.game

import kotlin.random.Random

/**
 * Oponente controlado por Inteligência Artificial.
 *
 * - [Difficulty.EASY]: escolhe uma célula livre aleatória.
 * - [Difficulty.MEDIUM]: heurística de regras — vence se puder, bloqueia o
 *   adversário se necessário, senão prioriza centro, cantos e laterais
 *   (com um pouco de aleatoriedade para não ser previsível).
 * - [Difficulty.HARD]: algoritmo Minimax com poda Alfa-Beta. Explora a árvore
 *   completa de jogadas e nunca perde.
 */
class TicTacToeAI(private val random: Random = Random.Default) {

    fun chooseMove(board: List<Player?>, ai: Player, difficulty: Difficulty): Int {
        val moves = GameLogic.availableMoves(board)
        require(moves.isNotEmpty()) { "Não há jogadas disponíveis" }
        return when (difficulty) {
            Difficulty.EASY -> moves.random(random)
            Difficulty.MEDIUM -> heuristicMove(board, ai, moves)
            Difficulty.HARD -> minimaxMove(board, ai, moves)
        }
    }

    // ---------- Médio: heurística ----------

    private fun heuristicMove(board: List<Player?>, ai: Player, moves: List<Int>): Int {
        // 1. Vencer imediatamente
        findWinningMove(board, ai)?.let { return it }
        // 2. Bloquear vitória do adversário
        findWinningMove(board, ai.opponent)?.let { return it }
        // 3. 20% de chance de errar com uma jogada aleatória (torna o nível "médio")
        if (random.nextInt(100) < 20) return moves.random(random)
        // 4. Prioridade posicional: centro > cantos > laterais
        if (4 in moves) return 4
        val corners = listOf(0, 2, 6, 8).filter { it in moves }
        if (corners.isNotEmpty()) return corners.random(random)
        return moves.random(random)
    }

    private fun findWinningMove(board: List<Player?>, player: Player): Int? =
        GameLogic.availableMoves(board).firstOrNull { move ->
            val next = board.toMutableList().also { it[move] = player }
            (GameLogic.evaluate(next) as? GameResult.Win)?.player == player
        }

    // ---------- Difícil: Minimax com poda Alfa-Beta ----------

    private fun minimaxMove(board: List<Player?>, ai: Player, moves: List<Int>): Int {
        val work = board.toMutableList()
        var bestScore = Int.MIN_VALUE
        val bestMoves = mutableListOf<Int>()
        for (move in moves) {
            work[move] = ai
            val score = minimax(work, depth = 1, isMaximizing = false, ai = ai,
                alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE)
            work[move] = null
            when {
                score > bestScore -> {
                    bestScore = score
                    bestMoves.clear()
                    bestMoves += move
                }
                score == bestScore -> bestMoves += move
            }
        }
        // Entre jogadas igualmente ótimas, sorteia uma para variar as partidas.
        return bestMoves.random(random)
    }

    /**
     * Pontuação: +10 - profundidade quando a IA vence (vitórias mais rápidas valem mais),
     * profundidade - 10 quando perde (derrotas mais tardias são "menos ruins"), 0 no empate.
     */
    private fun minimax(
        board: MutableList<Player?>,
        depth: Int,
        isMaximizing: Boolean,
        ai: Player,
        alpha: Int,
        beta: Int
    ): Int {
        when (val result = GameLogic.evaluate(board)) {
            is GameResult.Win -> return if (result.player == ai) 10 - depth else depth - 10
            GameResult.Draw -> return 0
            null -> Unit
        }

        var a = alpha
        var b = beta
        val current = if (isMaximizing) ai else ai.opponent
        var best = if (isMaximizing) Int.MIN_VALUE else Int.MAX_VALUE

        for (move in board.indices) {
            if (board[move] != null) continue
            board[move] = current
            val score = minimax(board, depth + 1, !isMaximizing, ai, a, b)
            board[move] = null
            if (isMaximizing) {
                best = maxOf(best, score)
                a = maxOf(a, best)
            } else {
                best = minOf(best, score)
                b = minOf(b, best)
            }
            if (b <= a) break // poda
        }
        return best
    }
}
