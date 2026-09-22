package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.DriverCoordinates
import com.example.data.model.DriverStop
import com.example.service.FloatingBubbleService

object NavigationHelper {

    /**
     * Inicia a navegação de acordo com o app preferido do motoboy (Google Maps ou Waze)
     * e já ativa o HUD flutuante inteligente sobre o mapa.
     */
    fun openPreferredNavigation(
        context: Context,
        stop: DriverStop,
        preferredNav: String
    ) {
        if (preferredNav.uppercase() == "WAZE") {
            openWaze(context, stop)
        } else {
            openGoogleMaps(context, stop)
        }
    }

    /**
     * Inicia navegação curva a curva no Google Maps e abre o Floating HUD inteligente sobre o mapa.
     */
    fun openGoogleMaps(
        context: Context,
        stop: DriverStop
    ) {
        try {
            // Ativa o HUD inteligente do Levô passando contexto completo da parada
            FloatingBubbleService.start(
                context = context,
                stopId = stop.id,
                customerName = stop.customerName ?: "Cliente",
                address = stop.address,
                phone = stop.customerPhone,
                formattedAmount = if (stop.amountCents > 0) stop.formattedAmount else null,
                position = stop.position
            )

            val coordinates = stop.coordinates
            val uri = if (coordinates != null) {
                Uri.parse("google.navigation:q=${coordinates.lat},${coordinates.lng}&mode=d")
            } else {
                Uri.parse("google.navigation:q=${Uri.encode(stop.address)}&mode=d")
            }

            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val webUri = if (coordinates != null) {
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${coordinates.lat},${coordinates.lng}&travelmode=driving")
                } else {
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(stop.address)}&travelmode=driving")
                }
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível abrir o Google Maps", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Inicia navegação curva a curva no Waze e abre o Floating HUD inteligente sobre o mapa.
     */
    fun openWaze(
        context: Context,
        stop: DriverStop
    ) {
        try {
            // Ativa o HUD inteligente do Levô passando contexto completo da parada
            FloatingBubbleService.start(
                context = context,
                stopId = stop.id,
                customerName = stop.customerName ?: "Cliente",
                address = stop.address,
                phone = stop.customerPhone,
                formattedAmount = if (stop.amountCents > 0) stop.formattedAmount else null,
                position = stop.position
            )

            val coordinates = stop.coordinates
            val uri = if (coordinates != null) {
                Uri.parse("waze://?ll=${coordinates.lat},${coordinates.lng}&navigate=yes")
            } else {
                Uri.parse("waze://?q=${Uri.encode(stop.address)}&navigate=yes")
            }

            val wazeIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (wazeIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(wazeIntent)
            } else {
                val webUri = if (coordinates != null) {
                    Uri.parse("https://waze.com/ul?ll=${coordinates.lat},${coordinates.lng}&navigate=yes")
                } else {
                    Uri.parse("https://waze.com/ul?q=${Uri.encode(stop.address)}&navigate=yes")
                }
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Waze não encontrado no aparelho", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Dials customer phone number directly.
     */
    fun dialCustomerPhone(context: Context, phone: String?) {
        if (phone.isNullOrBlank()) {
            Toast.makeText(context, "Telefone do cliente não informado", Toast.LENGTH_SHORT).show()
            return
        }
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível abrir o discador", Toast.LENGTH_SHORT).show()
        }
    }
}
