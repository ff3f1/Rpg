package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RpgArcaneCyan
import com.example.ui.theme.RpgGoldPrimary
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    sizeDp: Int = 110,
    onMove: (Float, Float) -> Unit
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadiusPx = (sizeDp / 2f) * 1.8f

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = knobOffset + dragAmount
                        val distance = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        val clamped = if (distance > maxRadiusPx) {
                            Offset(
                                (newOffset.x / distance) * maxRadiusPx,
                                (newOffset.y / distance) * maxRadiusPx
                            )
                        } else {
                            newOffset
                        }
                        knobOffset = clamped
                        val normX = (clamped.x / maxRadiusPx).coerceIn(-1f, 1f)
                        val normY = (clamped.y / maxRadiusPx).coerceIn(-1f, 1f)
                        onMove(normX, normY)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width / 2f

            // Outer base ring
            drawCircle(
                color = Color(0x33000000),
                radius = baseRadius,
                center = center
            )
            drawCircle(
                color = RpgArcaneCyan.copy(alpha = 0.4f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Inner direction indicator
            drawCircle(
                color = Color(0x2200E5FF),
                radius = baseRadius * 0.5f,
                center = center
            )

            // Knob position
            val knobCenter = center + knobOffset
            drawCircle(
                color = RpgGoldPrimary.copy(alpha = 0.85f),
                radius = baseRadius * 0.4f,
                center = knobCenter
            )
            drawCircle(
                color = Color.White,
                radius = baseRadius * 0.4f,
                center = knobCenter,
                style = Stroke(width = 2f)
            )
        }
    }
}
