package com.example.jvwithia.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

private const val DURATION = 3.2f   // segundos da animação inteira
private const val FADE_OUT = 0.5f   // segundos finais de desaparecimento

private class FloatingEmoji(val x: Float, val delay: Float, val speed: Float, val sizeSp: Int, val phase: Float)

/**
 * Emoji gargalhando quando a IA vence: um 🤣 grande surge com efeito de mola,
 * balança e pula como se estivesse rindo, enquanto pequenos 😂 sobem pela tela.
 * Toca uma vez ao entrar na composição e não intercepta toques.
 */
@Composable
fun LaughingEmoji(modifier: Modifier = Modifier) {
    val floaters = remember {
        val random = Random(System.nanoTime())
        List(7) {
            FloatingEmoji(
                x = 0.08f + random.nextFloat() * 0.84f,
                delay = random.nextFloat() * 0.9f,
                speed = 0.35f + random.nextFloat() * 0.25f,
                sizeSp = 26 + random.nextInt(18),
                phase = random.nextFloat() * 6.28f
            )
        }
    }
    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val startNanos = withFrameNanos { it }
        while (time < DURATION) {
            withFrameNanos { now -> time = (now - startNanos) / 1_000_000_000f }
        }
    }

    if (time >= DURATION) return
    val fade = ((DURATION - time) / FADE_OUT).coerceIn(0f, 1f)

    BoxWithConstraints(modifier) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Pequenos 😂 subindo e balançando
        for (f in floaters) {
            val t = time - f.delay
            if (t < 0f) continue
            Text(
                "😂",
                fontSize = f.sizeSp.sp,
                modifier = Modifier.graphicsLayer {
                    translationX = f.x * widthPx + sin(t * 4f + f.phase) * 24f
                    translationY = heightPx - t * f.speed * heightPx
                    rotationZ = sin(t * 6f + f.phase) * 15f
                    alpha = fade * (t / 0.3f).coerceIn(0f, 1f)
                }
            )
        }

        // 🤣 grande no centro
        Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
            Text(
                "🤣",
                fontSize = 120.sp,
                modifier = Modifier.graphicsLayer {
                    val s = popScale(time)
                    scaleX = s
                    scaleY = s
                    rotationZ = sin(time * 18f) * 14f                   // chacoalhando de rir
                    translationY = -abs(sin(time * 9f)) * 40f * (1f - time / DURATION) // pulinhos
                    alpha = fade
                }
            )
        }
    }
}

/** Escala com "mola": cresce de 0, passa um pouco de 1 e se acomoda. */
private fun popScale(t: Float): Float =
    1f - exp(-6f * t) * cos(2f * PI.toFloat() * 1.6f * t)
