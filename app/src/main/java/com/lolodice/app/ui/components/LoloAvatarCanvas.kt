package com.lolodice.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class RanuraAccesorio {
    CUELLO, CARA, CABEZA
}

data class AccesorioEquipado(
    val id: String,
    val ranura: RanuraAccesorio,
    @DrawableRes val drawableRes: Int,
    val scaleFactor: Float = 0.45f,
    val offsetXFactor: Float = 0f,
    val offsetYFactor: Float = 0f
)

@Composable
fun LoloAvatarCanvas(
    @DrawableRes baseLoloRes: Int,
    cuello: AccesorioEquipado? = null,
    cara: AccesorioEquipado? = null,
    cabeza: AccesorioEquipado? = null,
    canvasSize: Dp = 260.dp,
    animarReposo: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lolo_idle")
    val flotacionOffset by infiniteTransition.animateFloat(
        initialValue = if (animarReposo) -4f else 0f,
        targetValue = if (animarReposo) 4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lolo_float"
    )

    Box(
        modifier = modifier
            .size(canvasSize)
            .graphicsLayer {
                translationY = flotacionOffset
            },
        contentAlignment = Alignment.Center
    ) {
        // Capa 1: Cuerpo y expresión base de Lolo
        Image(
            painter = painterResource(id = baseLoloRes),
            contentDescription = "Lolo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Capa 2: Cuello / Cuerpo
        cuello?.let { item ->
            AccesorioLayer(item = item, canvasSize = canvasSize)
        }

        // Capa 3: Cara
        cara?.let { item ->
            AccesorioLayer(item = item, canvasSize = canvasSize)
        }

        // Capa 4: Cabeza
        cabeza?.let { item ->
            AccesorioLayer(item = item, canvasSize = canvasSize)
        }
    }
}

@Composable
private fun AccesorioLayer(
    item: AccesorioEquipado,
    canvasSize: Dp
) {
    val itemSize = canvasSize * item.scaleFactor
    val dx: Dp = canvasSize * item.offsetXFactor
    val dy: Dp = canvasSize * item.offsetYFactor

    Image(
        painter = painterResource(id = item.drawableRes),
        contentDescription = item.id,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(itemSize)
            .offset(x = dx, y = dy)
    )
}
