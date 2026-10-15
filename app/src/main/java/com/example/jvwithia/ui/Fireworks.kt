package com.example.jvwithia.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

private val FireworkColors = listOf(
    Color(0xFFC4B5FD), // lavanda
    Color(0xFF8B5CF6), // violeta
    Color(0xFFE879F9), // magenta
    Color(0xFFF472B6), // rosa
    Color(0xFFFDE68A), // dourado
    Color(0xFFFFFFFF)  // branco
)

private const val RISE_TIME = 0.55f   // segundos do foguete subindo
private const val BURST_TIME = 1.6f   // segundos da explosão
private const val GRAVITY = 0.35f     // em "tamanhos de tela" por s²
private const val DRAG = 1.8f         // desaceleração das faíscas

/** Uma explosão: foguete sobe de [launchX] até ([x], [y]) e se abre em faíscas. Coordenadas normalizadas (0..1). */
private class Burst(
    val start: Float,
    val launchX: Float,
    val x: Float,
    val y: Float,
    val color: Color,
    val accent: Color,
    val sparks: List<Spark>
)

private class Spark(val angle: Float, val speed: Float, val useAccent: Boolean, val twinklePhase: Float)

private fun randomBursts(random: Random, count: Int = 8): List<Burst> = List(count) { i ->
    val color = FireworkColors.random(random)
    Burst(
        start = i * 0.32f + random.nextFloat() * 0.15f,
        launchX = 0.2f + random.nextFloat() * 0.6f,
        x = 0.15f + random.nextFloat() * 0.7f,
        y = 0.12f + random.nextFloat() * 0.4f,
        color = color,
        accent = FireworkColors.random(random),
        sparks = List(64) {
            Spark(
                angle = (2 * PI * it / 64).toFloat() + random.nextFloat() * 0.12f,
                speed = 0.5f + random.nextFloat() * 0.35f,
                useAccent = random.nextInt(4) == 0,
                twinklePhase = random.nextFloat() * 6.28f
            )
        }
    )
}

/**
 * Fogos de artifício desenhados em Canvas. Toca uma vez ao entrar na composição
 * e não intercepta toques (o tabuleiro e os botões continuam clicáveis).
 */
@Composable
fun Fireworks(modifier: Modifier = Modifier) {
    val bursts = remember { randomBursts(Random(System.nanoTime())) }
    val totalDuration = remember(bursts) { bursts.maxOf { it.start } + RISE_TIME + BURST_TIME }
    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val startNanos = withFrameNanos { it }
        while (time < totalDuration) {
            withFrameNanos { now -> time = (now - startNanos) / 1_000_000_000f }
        }
    }

    Canvas(modifier) {
        if (time >= totalDuration) return@Canvas
        for (burst in bursts) drawBurst(burst, time - burst.start)
    }
}

private fun DrawScope.drawBurst(burst: Burst, t: Float) {
    if (t < 0f) return
    val unit = size.minDimension
    val origin = Offset(burst.x * size.width, burst.y * size.height)

    if (t < RISE_TIME) {
        // Foguete subindo com rastro
        val p = t / RISE_TIME
        val eased = 1f - (1f - p) * (1f - p)
        val from = Offset(burst.launchX * size.width, size.height)
        val head = from + (origin - from) * eased
        val tail = from + (origin - from) * (eased - 0.12f).coerceAtLeast(0f)
        drawLine(burst.color.copy(alpha = 0.5f), tail, head, unit * 0.006f, StrokeCap.Round)
        drawCircle(Color.White, radius = unit * 0.008f, center = head)
        return
    }

    val bt = t - RISE_TIME
    if (bt > BURST_TIME) return
    val life = 1f - bt / BURST_TIME

    // Clarão inicial
    if (bt < 0.15f) {
        drawCircle(
            burst.color.copy(alpha = (1f - bt / 0.15f) * 0.6f),
            radius = unit * (0.04f + bt * 0.5f),
            center = origin
        )
    }

    for (spark in burst.sparks) {
        val head = sparkPosition(origin, spark, bt, unit)
        val tail = sparkPosition(origin, spark, (bt - 0.09f).coerceAtLeast(0f), unit)
        val twinkle = 0.75f + 0.25f * sin(bt * 30f + spark.twinklePhase)
        val color = (if (spark.useAccent) burst.accent else burst.color)
            .copy(alpha = (life * twinkle).coerceIn(0f, 1f))
        drawLine(color, tail, head, unit * 0.007f * (0.4f + life * 0.6f), StrokeCap.Round)
        drawCircle(color, radius = unit * 0.006f * (0.5f + life * 0.5f), center = head)
    }
}

/** Posição analítica de uma faísca: velocidade com arrasto exponencial + gravidade. */
private fun sparkPosition(origin: Offset, spark: Spark, t: Float, unit: Float): Offset {
    val travel = spark.speed * (1f - exp(-DRAG * t)) / DRAG
    val fall = 0.5f * GRAVITY * t * t
    return Offset(
        origin.x + cos(spark.angle) * travel * unit,
        origin.y + (sin(spark.angle) * travel + fall) * unit
    )
}
