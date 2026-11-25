package com.lolodice.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lolodice.app.data.Veredicto
import com.lolodice.app.sensors.ShakeDetector
import com.lolodice.app.ui.components.LoloAvatarCanvas
import com.lolodice.app.ui.components.RanuraAccesorio
import com.lolodice.app.ui.viewmodel.FaseSacudida
import com.lolodice.app.ui.viewmodel.LoloViewModel
import com.lolodice.app.ui.viewmodel.resolverDrawableLolo

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OracleScreen(
    onOpenWardrobe: () -> Unit,
    viewModel: LoloViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val shakeDetector = remember(context) {
        ShakeDetector(
            context = context,
            onShake = { viewModel.dispararSacudida() }
        )
    }

    DisposableEffect(shakeDetector) {
        shakeDetector.start()
        onDispose { shakeDetector.stop() }
    }

    val shakeTransition = rememberInfiniteTransition(label = "lolo_shake_anim")
    val shakeAngle by shakeTransition.animateFloat(
        initialValue = if (state.fase == FaseSacudida.AGITANDO) -11f else 0f,
        targetValue = if (state.fase == FaseSacudida.AGITANDO) 11f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 85, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake_rotation"
    )

    val shakeOffsetX by shakeTransition.animateFloat(
        initialValue = if (state.fase == FaseSacudida.AGITANDO) -14f else 0f,
        targetValue = if (state.fase == FaseSacudida.AGITANDO) 14f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 75, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake_x"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lolo Dice",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = onOpenWardrobe) {
                        Text("Tu Lolo")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = state.preguntaActual,
                    onValueChange = { viewModel.actualizarPregunta(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Pregúntale algo a Lolo (o agita)...") },
                    trailingIcon = {
                        if (state.preguntaActual.isNotEmpty() || state.fraseActual != null) {
                            TextButton(onClick = { viewModel.reiniciarPregunta() }) {
                                Text("Limpiar")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.sugerencias.forEach { sugerencia ->
                        AssistChip(
                            onClick = { viewModel.seleccionarSugerencia(sugerencia) },
                            label = { Text(sugerencia) }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoloAvatarCanvas(
                    baseLoloRes = resolverDrawableLolo(state.reaccionActiva),
                    cuello = state.accesoriosEquipados[RanuraAccesorio.CUELLO],
                    cara = state.accesoriosEquipados[RanuraAccesorio.CARA],
                    cabeza = state.accesoriosEquipados[RanuraAccesorio.CABEZA],
                    canvasSize = 250.dp,
                    animarReposo = state.fase == FaseSacudida.REPOSO,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = if (state.fase == FaseSacudida.AGITANDO) shakeAngle else 0f
                        translationX = if (state.fase == FaseSacudida.AGITANDO) shakeOffsetX else 0f
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = state.fraseActual,
                    label = "respuesta_lolo"
                ) { frase ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when {
                                state.fase == FaseSacudida.AGITANDO -> {
                                    Text(
                                        text = "Lolo se está mareando...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                frase != null -> {
                                    if (state.preguntaActual.isNotBlank()) {
                                        Text(
                                            text = "“${state.preguntaActual}”",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    Text(
                                        text = etiquetaVeredicto(frase.veredicto),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = frase.texto,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                else -> {
                                    Text(
                                        text = "Agita el teléfono o toca el botón para que Lolo decida.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenWardrobe,
                    modifier = Modifier
                        .weight(0.4f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Accesorios")
                }

                Button(
                    onClick = { viewModel.dispararSacudida() },
                    enabled = state.fase != FaseSacudida.AGITANDO,
                    modifier = Modifier
                        .weight(0.6f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (state.fase == FaseSacudida.AGITANDO) "Agitando..." else "¡Agítalo!",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun etiquetaVeredicto(veredicto: Veredicto): String {
    return when (veredicto) {
        Veredicto.SI -> "LOLO DICE: SÍ"
        Veredicto.NO -> "LOLO DICE: NO"
        Veredicto.TAL_VEZ -> "LOLO DICE: TAL VEZ"
        Veredicto.ESPECIAL -> "VEREDICTO ESPECIAL"
    }
}
