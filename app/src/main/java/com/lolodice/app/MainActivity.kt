package com.lolodice.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lolodice.app.billing.BillingManager
import com.lolodice.app.data.CatalogoAccesorio
import com.lolodice.app.data.CatalogoInicialLolo
import com.lolodice.app.data.InventarioStorage
import com.lolodice.app.data.TipoAcceso
import com.lolodice.app.ui.components.AccesorioEquipado
import com.lolodice.app.ui.components.RanuraAccesorio
import com.lolodice.app.ui.screens.OracleScreen
import com.lolodice.app.ui.screens.WardrobeScreen
import com.lolodice.app.ui.viewmodel.LoloViewModel

private enum class PantallaLolo {
    ORACULO,
    ARMARIO
}

class MainActivity : ComponentActivity() {

    private val loloViewModel: LoloViewModel by viewModels()
    private lateinit var inventarioStorage: InventarioStorage
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inventarioStorage = InventarioStorage(applicationContext)

        billingManager = BillingManager(
            context = applicationContext,
            onCompraCompletada = { productId ->
                val accesorioMatch = CatalogoInicialLolo.items.firstOrNull { it.productId == productId }
                if (accesorioMatch != null && accesorioMatch.tipoAcceso == TipoAcceso.COMPRA_UNICA) {
                    inventarioStorage.guardarDesbloqueado(accesorioMatch.id)
                }
            }
        )

        val inappIds = CatalogoInicialLolo.items
            .filter { it.tipoAcceso == TipoAcceso.COMPRA_UNICA }
            .mapNotNull { it.productId }
            .distinct()

        val subsIds = listOf("lolo_club_mensual")
        billingManager.iniciarConexion(inappProductIds = inappIds, subsProductIds = subsIds)

        val equipadosIniciales = inventarioStorage.cargarEquipados()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier) {
                    var pantallaActual by remember { mutableStateOf(PantallaLolo.ORACULO) }
                    var desbloqueadosIds by remember {
                        mutableStateOf(inventarioStorage.obtenerDesbloqueados())
                    }
                    var equipadosPorRanura by remember {
                        mutableStateOf(equipadosIniciales)
                    }

                    val billingEstado by billingManager.estado.collectAsState()

                    LaunchedEffect(equipadosIniciales) {
                        equipadosIniciales.values.forEach { item ->
                            loloViewModel.equiparAccesorio(item)
                        }
                    }

                    LaunchedEffect(billingEstado.productosComprados, billingEstado.clubActivo) {
                        billingEstado.productosComprados.forEach { prodId ->
                            CatalogoInicialLolo.items
                                .filter { it.productId == prodId && it.tipoAcceso == TipoAcceso.COMPRA_UNICA }
                                .forEach { inventarioStorage.guardarDesbloqueado(it.id) }
                        }
                        desbloqueadosIds = inventarioStorage.obtenerDesbloqueados()
                    }

                    fun aplicarEquipado(item: CatalogoAccesorio) {
                        val actual: AccesorioEquipado? = equipadosPorRanura[item.ranura]
                        val nuevoMapa: MutableMap<RanuraAccesorio, AccesorioEquipado> =
                            equipadosPorRanura.toMutableMap()

                        if (actual?.id == item.id) {
                            nuevoMapa.remove(item.ranura)
                            loloViewModel.equiparAccesorio(actual)
                        } else {
                            if (actual != null) {
                                loloViewModel.equiparAccesorio(actual)
                            }
                            val equipado = item.aEquipado()
                            nuevoMapa[item.ranura] = equipado
                            loloViewModel.equiparAccesorio(equipado)
                        }

                        equipadosPorRanura = nuevoMapa
                        inventarioStorage.guardarEquipados(nuevoMapa)
                    }

                    when (pantallaActual) {
                        PantallaLolo.ORACULO -> {
                            OracleScreen(
                                onOpenWardrobe = { pantallaActual = PantallaLolo.ARMARIO },
                                viewModel = loloViewModel
                            )
                        }

                        PantallaLolo.ARMARIO -> {
                            WardrobeScreen(
                                desbloqueadosIds = desbloqueadosIds,
                                equipadosPorRanura = equipadosPorRanura,
                                clubActivo = billingEstado.clubActivo,
                                onEquiparToggle = { item -> aplicarEquipado(item) },
                                onSolicitarCompra = { item ->
                                    val prodId = item.productId ?: return@WardrobeScreen
                                    val esClub = item.tipoAcceso == TipoAcceso.CLUB
                                    billingManager.lanzarCompra(
                                        activity = this@MainActivity,
                                        productId = prodId,
                                        esSuscripcion = esClub
                                    )
                                },
                                onBack = { pantallaActual = PantallaLolo.ORACULO }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        billingManager.cerrar()
        super.onDestroy()
    }
}
