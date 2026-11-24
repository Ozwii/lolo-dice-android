package com.lolodice.app.ui.viewmodel

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lolodice.app.data.FraseLolo
import com.lolodice.app.data.FrasesRepository
import com.lolodice.app.data.ReaccionLolo
import com.lolodice.app.ui.components.AccesorioEquipado
import com.lolodice.app.ui.components.RanuraAccesorio
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FaseSacudida {
    REPOSO,
    AGITANDO,
    RESPONDIDO
}

@Immutable
data class OracleUiState(
    val preguntaActual: String = "",
    val sugerencias: List<String> = listOf(
        "¿Le escribo?",
        "¿Compro eso?",
        "¿Salgo hoy?",
        "¿Me arriesgo?"
    ),
    val fase: FaseSacudida = FaseSacudida.REPOSO,
    val reaccionActiva: ReaccionLolo = ReaccionLolo.FELIZ,
    val fraseActual: FraseLolo? = null,
    val incluyeClub: Boolean = false,
    val accesoriosEquipados: Map<RanuraAccesorio, AccesorioEquipado> = emptyMap(),
    val errorMensaje: String? = null
)

class LoloViewModel(application: Application) : AndroidViewModel(application) {

    private val frasesRepository = FrasesRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(OracleUiState())
    val uiState: StateFlow<OracleUiState> = _uiState.asStateFlow()

    private var shakeJob: Job? = null

    fun actualizarPregunta(texto: String) {
        _uiState.update { it.copy(preguntaActual = texto.take(90)) }
    }

    fun seleccionarSugerencia(sugerencia: String) {
        _uiState.update { it.copy(preguntaActual = sugerencia) }
        dispararSacudida()
    }

    fun equiparAccesorio(accesorio: AccesorioEquipado?) {
        _uiState.update { state ->
            val actualizado = state.accesoriosEquipados.toMutableMap()
            if (accesorio == null) {
                return@update state
            }
            if (actualizado[accesorio.ranura]?.id == accesorio.id) {
                actualizado.remove(accesorio.ranura)
            } else {
                actualizado[accesorio.ranura] = accesorio
            }
            state.copy(accesoriosEquipados = actualizado)
        }
    }

    fun dispararSacudida() {
        if (_uiState.value.fase == FaseSacudida.AGITANDO) return

        shakeJob?.cancel()
        shakeJob = viewModelScope.launch {
            vibrar(duracionMs = 75, amplitud = 140)

            _uiState.update {
                it.copy(
                    fase = FaseSacudida.AGITANDO,
                    reaccionActiva = ReaccionLolo.MAREADO,
                    errorMensaje = null
                )
            }

            delay(650L)

            val nuevaFrase = frasesRepository.obtenerFraseAleatoria(
                incluirClub = _uiState.value.incluyeClub
            )

            if (nuevaFrase != null) {
                vibrar(duracionMs = 110, amplitud = 210)
                _uiState.update {
                    it.copy(
                        fase = FaseSacudida.RESPONDIDO,
                        reaccionActiva = nuevaFrase.reaccion,
                        fraseActual = nuevaFrase
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        fase = FaseSacudida.REPOSO,
                        reaccionActiva = ReaccionLolo.DUDOSO,
                        errorMensaje = "Lolo no encontró frases cargadas."
                    )
                }
            }
        }
    }

    fun reiniciarPregunta() {
        shakeJob?.cancel()
        _uiState.update {
            it.copy(
                preguntaActual = "",
                fase = FaseSacudida.REPOSO,
                reaccionActiva = ReaccionLolo.FELIZ,
                fraseActual = null
            )
        }
    }

    private fun vibrar(duracionMs: Long, amplitud: Int) {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService<VibratorManager>()
            val vibrator = manager?.defaultVibrator ?: return
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(duracionMs, amplitud.coerceIn(1, 255))
                )
            }
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService<Vibrator>() ?: return
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(duracionMs, amplitud.coerceIn(1, 255))
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duracionMs)
                }
            }
        }
    }
}

@DrawableRes
fun resolverDrawableLolo(reaccion: ReaccionLolo): Int {
    // Sustituye estos R.drawable.* por tus drawables reales de Lolo según expresión
    return when (reaccion) {
        ReaccionLolo.FELIZ -> android.R.drawable.sym_def_app_icon
        ReaccionLolo.ENOJADO -> android.R.drawable.sym_def_app_icon
        ReaccionLolo.MAREADO -> android.R.drawable.sym_def_app_icon
        ReaccionLolo.DUDOSO -> android.R.drawable.sym_def_app_icon
        ReaccionLolo.SORPRESA -> android.R.drawable.sym_def_app_icon
    }
}
