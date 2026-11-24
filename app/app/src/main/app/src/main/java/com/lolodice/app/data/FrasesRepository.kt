package com.lolodice.app.data

import android.content.Context
import org.json.JSONArray

enum class Veredicto {
    SI, NO, TAL_VEZ, ESPECIAL
}

enum class ReaccionLolo {
    FELIZ, ENOJADO, MAREADO, DUDOSO, SORPRESA
}

data class FraseLolo(
    val id: String,
    val veredicto: Veredicto,
    val reaccion: ReaccionLolo,
    val texto: String,
    val tono: String? = null,
    val esClub: Boolean = false
)

class FrasesRepository(private val context: Context) {

    private val frases: List<FraseLolo> by lazy { cargarDesdeAssets() }
    private val recientesIds = ArrayDeque<String>()
    private val maxRecientes = 14

    private fun cargarDesdeAssets(): List<FraseLolo> {
        val jsonString = context.assets.open("frases_lolo.json")
            .bufferedReader()
            .use { it.readText() }

        val array = JSONArray(jsonString)
        val lista = mutableListOf<FraseLolo>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            lista.add(
                FraseLolo(
                    id = obj.getString("id"),
                    veredicto = Veredicto.valueOf(obj.getString("veredicto")),
                    reaccion = ReaccionLolo.valueOf(obj.getString("reaccion").uppercase()),
                    texto = obj.getString("texto"),
                    tono = obj.optString("tono").takeIf { it.isNotBlank() },
                    esClub = obj.optBoolean("es_club", false)
                )
            )
        }
        return lista
    }

    fun obtenerFraseAleatoria(
        incluirClub: Boolean = false,
        veredictoForzado: Veredicto? = null
    ): FraseLolo? {
        val candidatas = frases.filter { frase ->
            (incluirClub || !frase.esClub) &&
                (veredictoForzado == null || frase.veredicto == veredictoForzado)
        }

        if (candidatas.isEmpty()) return null

        val noRepetidas = candidatas.filter { it.id !in recientesIds }
        val pool = noRepetidas.ifEmpty {
            repeat(minOf(6, recientesIds.size)) { recientesIds.removeFirstOrNull() }
            candidatas.filter { it.id !in recientesIds }.ifEmpty { candidatas }
        }

        val elegida = pool.random()
        registrarUso(elegida.id)
        return elegida
    }

    private fun registrarUso(id: String) {
        if (recientesIds.size >= maxRecientes) {
            recientesIds.removeFirstOrNull()
        }
        recientesIds.addLast(id)
    }
}
