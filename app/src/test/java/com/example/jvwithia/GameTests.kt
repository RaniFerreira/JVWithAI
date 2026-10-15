package com.example.jvwithia

import com.example.jvwithia.game.Difficulty
import com.example.jvwithia.game.GameLogic
import com.example.jvwithia.game.GameResult
import com.example.jvwithia.game.Player
import com.example.jvwithia.game.TicTacToeAI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameLogicTest {

    private fun board(s: String): List<Player?> = s.map {
        when (it) { 'X' -> Player.X; 'O' -> Player.O; else -> null }
    }

    @Test
    fun detectsRowColumnAndDiagonalWins() {
        assertEquals(GameResult.Win(Player.X, listOf(0, 1, 2)), GameLogic.evaluate(board("XXXOO....")))
        assertEquals(GameResult.Win(Player.O, listOf(1, 4, 7)), GameLogic.evaluate(board("XOXXO..O.")))
        assertEquals(GameResult.Win(Player.X, listOf(2, 4, 6)), GameLogic.evaluate(board("OOXOXXX..")))
    }

    @Test
    fun detectsDrawAndOngoing() {
        assertEquals(GameResult.Draw, GameLogic.evaluate(board("XOXXOOOXX")))
        assertNull(GameLogic.evaluate(board("XO.......")))
    }
}

class TicTacToeAITest {

    private val ai = TicTacToeAI(Random(42))

    @Test
    fun allLevelsOnlyPickEmptyCells() {
        val b = listOf(Player.X, Player.O, Player.X, null, Player.O, null, null, Player.X, null)
        for (d in Difficulty.entries) repeat(50) {
            assertNull(b[ai.chooseMove(b, Player.O, d)])
        }
    }

    @Test
    fun mediumAndHardWinOrBlock() {
        // O pode vencer em 2; X ameaça em 8
        val win = listOf(Player.O, Player.O, null, Player.X, Player.X, null, null, null, null)
        for (d in listOf(Difficulty.MEDIUM, Difficulty.HARD)) {
            assertEquals(2, ai.chooseMove(win, Player.O, d))
        }
        val block = listOf(Player.X, Player.X, null, null, Player.O, null, null, null, null)
        for (d in listOf(Difficulty.MEDIUM, Difficulty.HARD)) {
            assertEquals(2, ai.chooseMove(block, Player.O, d))
        }
    }

    /** Testa todas as sequências possíveis de jogadas humanas: a IA difícil nunca perde. */
    @Test
    fun hardNeverLoses() {
        for (aiStarts in listOf(false, true)) {
            explore(GameLogic.emptyBoard(), if (aiStarts) Player.O else Player.X)
        }
    }

    private fun explore(board: List<Player?>, turn: Player) {
        when (val r = GameLogic.evaluate(board)) {
            is GameResult.Win -> { assertTrue("IA perdeu: $board", r.player == Player.O); return }
            GameResult.Draw -> return
            null -> Unit
        }
        if (turn == Player.O) {
            val move = ai.chooseMove(board, Player.O, Difficulty.HARD)
            explore(board.toMutableList().also { it[move] = Player.O }, Player.X)
        } else {
            for (m in GameLogic.availableMoves(board)) {
                explore(board.toMutableList().also { it[m] = Player.X }, Player.O)
            }
        }
    }
}
