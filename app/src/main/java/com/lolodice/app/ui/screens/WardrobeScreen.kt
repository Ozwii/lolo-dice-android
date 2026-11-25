package com.lolodice.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lolodice.app.data.CatalogoAccesorio
import com.lolodice.app.data.CatalogoInicialLolo
import com.lolodice.app.data.ReaccionLolo
import com.lolodice.app.data.TipoAcceso
import com.lolodice.app.ui.components.AccesorioEquipado
import com.lolodice.app.ui.components.LoloAvatarCanvas
import com.lolodice.app.ui.components.RanuraAccesorio
import com.lolodice.app.ui.viewmodel.resolverDrawableLolo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    desbloqueadosIds: Set<String>,
    equipadosPorRanura: Map<RanuraAccesorio, AccesorioEquipado>,
    clubActivo: Boolean,
    onEquiparToggle: (CatalogoAccesorio) -> Unit,
    onSolicitarCompra: (CatalogoAccesorio) -> Unit,
    onBack: () -> Unit
) {
    var ranuraSeleccionada by remember { mutableStateOf<RanuraAccesorio?>(null) }

    val itemsFiltrados = remember(ranuraSeleccionada) {
        if (ranuraSeleccionada == null) {
            CatalogoInicialLolo.items
        } else {
            CatalogoInicialLolo.items.filter { it.ranura == ranuraSeleccionada }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tu Lolo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
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
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoloAvatarCanvas(
                        baseLoloRes = resolverDrawableLolo(ReaccionLolo.FELIZ),
                        cuello = equipadosPorRanura[RanuraAccesorio.CUELLO],
                        cara = equipadosPorRanura[RanuraAccesorio.CARA],
                        cabeza = equipadosPorRanura[RanuraAccesorio.CABEZA],
                        canvasSize = 185.dp,
                        animarReposo = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = ranuraSeleccionada == null,
                        onClick = { ranuraSeleccionada = null },
                        label = { Text("Todos") }
                    )
                }
                items(RanuraAccesorio.entries) { ranura ->
                    FilterChip(
                        selected = ranuraSeleccionada == ranura,
                        onClick = { ranuraSeleccionada = ranura },
                        label = {
                            Text(
                                when (ranura) {
                                    RanuraAccesorio.CABEZA -> "Cabeza"
                                    RanuraAccesorio.CARA -> "Cara"
                                    RanuraAccesorio.CUELLO -> "Cuello"
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(itemsFiltrados, key = { it.id }) { item ->
                    val estaDesbloqueado = when (item.tipoAcceso) {
                        TipoAcceso.GRATIS -> true
                        TipoAcceso.COMPRA_UNICA -> item.id in desbloqueadosIds
                        TipoAcceso.CLUB -> clubActivo || (item.id in desbloqueadosIds)
                    }
                    val estaEquipado = equipadosPorRanura[item.ranura]?.id == item.id

                    AccesorioRowCard(
                        item = item,
                        estaDesbloqueado = estaDesbloqueado,
                        estaEquipado = estaEquipado,
                        onEquiparClick = { onEquiparToggle(item) },
                        onComprarClick = { onSolicitarCompra(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccesorioRowCard(
    item: CatalogoAccesorio,
    estaDesbloqueado: Boolean,
    estaEquipado: Boolean,
    onEquiparClick: () -> Unit,
    onComprarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        estaDesbloqueado -> "Disponible"
                        item.tipoAcceso == TipoAcceso.CLUB -> "Exclusivo Club"
                        else -> "Compra única"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (estaDesbloqueado) {
                if (estaEquipado) {
                    OutlinedButton(onClick = onEquiparClick) {
                        Text("Quitar")
                    }
                } else {
                    Button(onClick = onEquiparClick) {
                        Text("Equipar")
                    }
                }
            } else {
                Button(onClick = onComprarClick) {
                    Text(if (item.tipoAcceso == TipoAcceso.CLUB) "Unirse al Club" else "Desbloquear")
                }
            }
        }
    }
}
