package com.lolodice.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BillingEstado(
    val conectado: Boolean = false,
    val clubActivo: Boolean = false,
    val productosComprados: Set<String> = emptySet(),
    val mensajeError: String? = null
)

class BillingManager(
    context: Context,
    private val onCompraCompletada: (productId: String) -> Unit
) : PurchasesUpdatedListener {

    private val _estado = MutableStateFlow(BillingEstado())
    val estado: StateFlow<BillingEstado> = _estado.asStateFlow()

    private val detallesProductos = mutableMapOf<String, ProductDetails>()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun iniciarConexion(
        inappProductIds: List<String>,
        subsProductIds: List<String>
    ) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _estado.update { it.copy(conectado = true, mensajeError = null) }
                    consultarCatalogo(inappProductIds, subsProductIds)
                    consultarComprasActivas()
                } else {
                    _estado.update {
                        it.copy(
                            conectado = false,
                            mensajeError = "No se pudo conectar con Google Play."
                        )
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                _estado.update { it.copy(conectado = false) }
            }
        })
    }

    private fun consultarCatalogo(
        inappProductIds: List<String>,
        subsProductIds: List<String>
    ) {
        if (inappProductIds.isNotEmpty()) {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    inappProductIds.map { id ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(id)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    }
                ).build()

            billingClient.queryProductDetailsAsync(params) { result, list ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    list.forEach { detallesProductos[it.productId] = it }
                }
            }
        }

        if (subsProductIds.isNotEmpty()) {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    subsProductIds.map { id ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(id)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    }
                ).build()

            billingClient.queryProductDetailsAsync(params) { result, list ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    list.forEach { detallesProductos[it.productId] = it }
                }
            }
        }
    }

    fun consultarComprasActivas() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                procesarComprasExistentes(purchases)
            }
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val activa = purchases.any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                _estado.update { it.copy(clubActivo = activa) }
                purchases.forEach { confirmarCompraSiNecesario(it) }
            }
        }
    }

    private fun procesarComprasExistentes(purchases: List<Purchase>) {
        val ids = mutableSetOf<String>()
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                ids.addAll(purchase.products)
                confirmarCompraSiNecesario(purchase)
            }
        }
        _estado.update { it.copy(productosComprados = it.productosComprados + ids) }
    }

    fun lanzarCompra(activity: Activity, productId: String, esSuscripcion: Boolean = false) {
        val details = detallesProductos[productId]
        if (details == null) {
            _estado.update { it.copy(mensajeError = "Producto no disponible aún en Play.") }
            return
        }

        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        if (esSuscripcion) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken != null) {
                productParamsBuilder.setOfferToken(offerToken)
            }
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    confirmarCompraSiNecesario(purchase)
                    purchase.products.forEach { prodId ->
                        _estado.update {
                            it.copy(
                                productosComprados = it.productosComprados + prodId,
                                clubActivo = it.clubActivo || prodId.contains("club")
                            )
                        }
                        onCompraCompletada(prodId)
                    }
                }
            }
        } else if (billingResult.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            _estado.update {
                it.copy(mensajeError = "Error en el pago: ${billingResult.debugMessage}")
            }
        }
    }

    private fun confirmarCompraSiNecesario(purchase: Purchase) {
        if (!purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { /* Confirmación registrada */ }
        }
    }

    fun cerrar() {
        billingClient.endConnection()
    }
}
