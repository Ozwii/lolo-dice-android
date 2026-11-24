package com.lolodice.app.data

import android.content.Context
import androidx.annotation.DrawableRes
import com.lolodice.app.ui.components.AccesorioEquipado
import com.lolodice.app.ui.components.RanuraAccesorio

enum class TipoAcceso {
    GRATIS,
    COMPRA_UNICA,
    CLUB
}

data class CatalogoAccesorio(
    val id: String,
    val nombre: String,
    val ranura: RanuraAccesorio,
    val tipoAcceso: TipoAcceso,
    val productId: String? = null,
    @DrawableRes val drawableRes: Int,
    val scaleFactor: Float = 0.45f,
    val offsetXFactor: Float = 0f,
    val offsetYFactor: Float = 0f
) {
    fun aEquipado(): AccesorioEquipado = AccesorioEquipado(
        id = id,
        ranura = ranura,
        drawableRes = drawableRes,
        scaleFactor = scaleFactor,
        offsetXFactor = offsetXFactor,
        offsetYFactor = offsetYFactor
    )
}

object CatalogoInicialLolo {
    // Reemplaza android.R.drawable.sym_def_app_icon por tus R.drawable.acc_* reales
    val items: List<CatalogoAccesorio> = listOf(
        // CABEZA
        CatalogoAccesorio(
            id = "gorra_atras",
            nombre = "Gorra hacia atrás",
            ranura = RanuraAccesorio.CABEZA,
            tipoAcceso = TipoAcceso.GRATIS,
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.44f,
            offsetYFactor = -0.26f
        ),
        CatalogoAccesorio(
            id = "corona_dorada",
            nombre = "Corona dorada",
            ranura = RanuraAccesorio.CABEZA,
            tipoAcceso = TipoAcceso.COMPRA_UNICA,
            productId = "lolo_acc_corona_dorada",
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.34f,
            offsetYFactor = -0.32f
        ),
        CatalogoAccesorio(
            id = "sombrero_copa",
            nombre = "Sombrero de copa",
            ranura = RanuraAccesorio.CABEZA,
            tipoAcceso = TipoAcceso.CLUB,
            productId = "lolo_club_mensual",
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.42f,
            offsetYFactor = -0.33f
        ),
        CatalogoAccesorio(
            id = "diadema_brote",
            nombre = "Diadema con brote",
            ranura = RanuraAccesorio.CABEZA,
            tipoAcceso = TipoAcceso.GRATIS,
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.38f,
            offsetYFactor = -0.28f
        ),

        // CARA
        CatalogoAccesorio(
            id = "lentes_sol",
            nombre = "Lentes de sol",
            ranura = RanuraAccesorio.CARA,
            tipoAcceso = TipoAcceso.GRATIS,
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.46f,
            offsetYFactor = -0.02f
        ),
        CatalogoAccesorio(
            id = "lentes_intelectual",
            nombre = "Lentes redondos",
            ranura = RanuraAccesorio.CARA,
            tipoAcceso = TipoAcceso.CLUB,
            productId = "lolo_club_mensual",
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.46f,
            offsetYFactor = -0.02f
        ),
        CatalogoAccesorio(
            id = "rubor_estrellas",
            nombre = "Rubor con estrellas",
            ranura = RanuraAccesorio.CARA,
            tipoAcceso = TipoAcceso.COMPRA_UNICA,
            productId = "lolo_acc_rubor_estrellas",
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.50f,
            offsetYFactor = 0.04f
        ),
        CatalogoAccesorio(
            id = "gafas_pixel",
            nombre = "Gafas pixeladas",
            ranura = RanuraAccesorio.CARA,
            tipoAcceso = TipoAcceso.GRATIS,
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.46f,
            offsetYFactor = -0.02f
        ),

        // CUELLO / CUERPO
        CatalogoAccesorio(
            id = "bufanda_rayas",
            nombre = "Bufanda a rayas",
            ranura = RanuraAccesorio.CUELLO,
            tipoAcceso = TipoAcceso.GRATIS,
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.52f,
            offsetYFactor = 0.22f
        ),
        CatalogoAccesorio(
            id = "corbatin_elegante",
            nombre = "Corbatín",
            ranura = RanuraAccesorio.CUELLO,
            tipoAcceso = TipoAcceso.COMPRA_UNICA,
            productId = "lolo_acc_corbatin",
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.30f,
            offsetYFactor = 0.22f
        ),
        CatalogoAccesorio(
            id = "collar_estrella",
            nombre = "Collar estrella",
            ranura = RanuraAccesorio.CUELLO,
            tipoAcceso = TipoAcceso.CLUB,
            productId = "lolo_club_mensual",
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.32f,
            offsetYFactor = 0.24f
        ),
        CatalogoAccesorio(
            id = "pechera_aventurero",
            nombre = "Pechera aventurera",
            ranura = RanuraAccesorio.CUELLO,
            tipoAcceso = TipoAcceso.GRATIS,
            drawableRes = android.R.drawable.sym_def_app_icon,
            scaleFactor = 0.54f,
            offsetYFactor = 0.24f
        )
    )
}

class InventarioStorage(context: Context) {

    private val prefs = context.getSharedPreferences("lolo_dice_inventario", Context.MODE_PRIVATE)

    fun obtenerDesbloqueados(): Set<String> {
        val gratisIds = CatalogoInicialLolo.items
            .filter { it.tipoAcceso == TipoAcceso.GRATIS }
            .map { it.id }
            .toSet()

        val guardados = prefs.getStringSet(KEY_DESBLOQUEADOS, emptySet()) ?: emptySet()
        return gratisIds + guardados
    }

    fun guardarDesbloqueado(idAccesorio: String) {
        val actuales = (prefs.getStringSet(KEY_DESBLOQUEADOS, emptySet()) ?: emptySet()).toMutableSet()
        actuales.add(idAccesorio)
        prefs.edit().putStringSet(KEY_DESBLOQUEADOS, actuales).apply()
    }

    fun guardarEquipados(equipados: Map<RanuraAccesorio, AccesorioEquipado>) {
        val editor = prefs.edit()
        RanuraAccesorio.entries.forEach { ranura ->
            val id = equipados[ranura]?.id
            if (id != null) {
                editor.putString("equipado_${ranura.name}", id)
            } else {
                editor.remove("equipado_${ranura.name}")
            }
        }
        editor.apply()
    }

    fun cargarEquipados(): Map<RanuraAccesorio, AccesorioEquipado> {
        val resultado = mutableMapOf<RanuraAccesorio, AccesorioEquipado>()
        val catalogoPorId = CatalogoInicialLolo.items.associateBy { it.id }
        val desbloqueados = obtenerDesbloqueados()

        RanuraAccesorio.entries.forEach { ranura ->
            val idGuardado = prefs.getString("equipado_${ranura.name}", null)
            val item = idGuardado?.let { catalogoPorId[it] }
            if (item != null && item.id in desbloqueados) {
                resultado[ranura] = item.aEquipado()
            }
        }
        return resultado
    }

    companion object {
        private const val KEY_DESBLOQUEADOS = "accesorios_desbloqueados"
    }
}
