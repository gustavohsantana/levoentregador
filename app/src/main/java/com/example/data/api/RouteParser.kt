package com.example.data.api

import com.example.data.model.DriverCoordinates
import com.example.data.model.DriverRoute
import com.example.data.model.DriverStop
import org.json.JSONArray
import org.json.JSONObject

object RouteParser {

    /**
     * Parses the response from Next.js /m/{token} endpoint.
     * Next.js embeds the server-rendered `route` prop in the HTML or RSC flight stream.
     */
    fun parseRoute(content: String, fallbackToken: String): DriverRoute? {
        try {
            // Case 1: Direct JSON response
            if (content.trim().startsWith("{") && content.contains("\"stops\"")) {
                val json = JSONObject(content)
                val targetObj = if (json.has("route")) json.getJSONObject("route") else json
                return parseRouteFromJson(targetObj)
            }

            // Case 2: RSC payload or HTML script containing "route":{...}
            val routeIdx = content.indexOf("\"route\":{")
            if (routeIdx != -1) {
                val jsonStr = extractJsonObject(content, routeIdx + 8)
                if (jsonStr != null) {
                    val jsonObj = JSONObject(jsonStr)
                    return parseRouteFromJson(jsonObj)
                }
            }

            // Case 3: Match "routeId" inside JSON
            val routeIdIdx = content.indexOf("\"routeId\":")
            if (routeIdIdx != -1) {
                // backtrack to opening brace
                var start = routeIdIdx
                while (start >= 0 && content[start] != '{') {
                    start--
                }
                if (start >= 0) {
                    val jsonStr = extractJsonObject(content, start)
                    if (jsonStr != null) {
                        val jsonObj = JSONObject(jsonStr)
                        return parseRouteFromJson(jsonObj)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun extractJsonObject(content: String, startIndex: Int): String? {
        var depth = 0
        var started = false
        val sb = StringBuilder()
        for (i in startIndex until content.length) {
            val c = content[i]
            if (c == '{') {
                depth++
                started = true
            } else if (c == '}') {
                depth--
            }
            if (started) {
                sb.append(c)
                if (depth == 0) {
                    return sb.toString()
                }
            }
        }
        return null
    }

    fun parseRouteFromJson(json: JSONObject): DriverRoute {
        val routeId = json.optString("routeId", "route_unknown")
        val establishmentName = json.optString("establishmentName", "Restaurante Levô")
        val courierName = json.optString("courierName", "Motoboy")
        val exigeCodigo = json.optBoolean("exigeCodigo", false)
        val status = json.optString("status", "IN_PROGRESS")
        val startedAt = if (json.has("startedAt") && !json.isNull("startedAt")) json.getString("startedAt") else null
        val totalStops = json.optInt("totalStops", 0)

        var origin: DriverCoordinates? = null
        if (json.has("origin") && !json.isNull("origin")) {
            val o = json.getJSONObject("origin")
            origin = DriverCoordinates(o.optDouble("lat", 0.0), o.optDouble("lng", 0.0))
        }

        val stopsList = mutableListOf<DriverStop>()
        if (json.has("stops")) {
            val stopsArray = json.getJSONArray("stops")
            for (i in 0 until stopsArray.length()) {
                val s = stopsArray.getJSONObject(i)
                val id = s.optString("id", "stop_$i")
                val position = s.optInt("position", i + 1)
                val stopStatus = s.optString("status", "PENDING")
                val customerName = s.optString("customerName", "Cliente $position")
                val customerPhone = if (s.has("customerPhone") && !s.isNull("customerPhone")) s.getString("customerPhone") else null
                val address = s.optString("address", "Endereço")
                val reference = if (s.has("reference") && !s.isNull("reference")) s.getString("reference") else null
                val notes = if (s.has("notes") && !s.isNull("notes")) s.getString("notes") else null
                val amountCents = s.optInt("amountCents", 0)
                val etaSeconds = s.optInt("etaSeconds", 0)

                val stopExigeCodigo = if (s.has("exigeCodigo") && !s.isNull("exigeCodigo")) {
                    s.optBoolean("exigeCodigo")
                } else if (s.has("requiresCode") && !s.isNull("requiresCode")) {
                    s.optBoolean("requiresCode")
                } else if (s.has("needCode") && !s.isNull("needCode")) {
                    s.optBoolean("needCode")
                } else null

                val deliveryCode = if (s.has("deliveryCode") && !s.isNull("deliveryCode")) s.getString("deliveryCode") else null
                val failureReason = if (s.has("failureReason") && !s.isNull("failureReason")) {
                    s.getString("failureReason")
                } else if (s.has("reason") && !s.isNull("reason")) {
                    s.getString("reason")
                } else null

                var coords: DriverCoordinates? = null
                if (s.has("coordinates") && !s.isNull("coordinates")) {
                    val c = s.getJSONObject("coordinates")
                    coords = DriverCoordinates(c.optDouble("lat", 0.0), c.optDouble("lng", 0.0))
                }

                stopsList.add(
                    DriverStop(
                        id = id,
                        position = position,
                        status = stopStatus,
                        customerName = customerName,
                        customerPhone = customerPhone,
                        address = address,
                        reference = reference,
                        notes = notes,
                        amountCents = amountCents,
                        etaSeconds = etaSeconds,
                        coordinates = coords,
                        exigeCodigo = stopExigeCodigo,
                        deliveryCode = deliveryCode,
                        failureReason = failureReason
                    )
                )
            }
        }

        return DriverRoute(
            routeId = routeId,
            establishmentName = establishmentName,
            exigeCodigo = exigeCodigo,
            courierName = courierName,
            status = status,
            startedAt = startedAt,
            origin = origin,
            totalStops = if (totalStops > 0) totalStops else stopsList.size,
            stops = stopsList
        )
    }

    /**
     * Realistic demonstration route with smart trajectory sequence for instant testing and offline practice.
     */
    fun createDemoRoute(token: String = "demo_route_token_123"): DriverRoute {
        val stops = listOf(
            DriverStop(
                id = "demo_stop_1",
                position = 1,
                status = "PENDING",
                customerName = "Juliana Ferraz",
                customerPhone = "11987654321",
                address = "Rua Padre Anchieta, 1500 - Bigorrilho",
                reference = "Apto 42, Bloco B (Próximo à Praça)",
                notes = "Interfonar direto. Não tocar campainha por causa do bebê.",
                amountCents = 3850,
                etaSeconds = 480, // 8 min
                coordinates = DriverCoordinates(-23.561684, -46.655981),
                exigeCodigo = true // Pedido iFood: exige código de 4 dígitos
            ),
            DriverStop(
                id = "demo_stop_2",
                position = 2,
                status = "PENDING",
                customerName = "Lucas Alencar",
                customerPhone = "11991234567",
                address = "Alameda Santos, 1800 - Cerqueira César",
                reference = "Portaria 2 - Comercial",
                notes = "Deixar na recepção com a Letícia. Cobrar na maquininha.",
                amountCents = 5400,
                etaSeconds = 960, // 16 min
                coordinates = DriverCoordinates(-23.565403, -46.662283),
                exigeCodigo = false // Entrega comercial: não exige código
            ),
            DriverStop(
                id = "demo_stop_3",
                position = 3,
                status = "PENDING",
                customerName = "Mariana Silveira",
                customerPhone = "11976543210",
                address = "Rua Oscar Freire, 920 - Jardins",
                reference = "Loja de calçados",
                notes = "Entregar nos fundos. Já está pago.",
                amountCents = 0,
                etaSeconds = 1440, // 24 min
                coordinates = DriverCoordinates(-23.562912, -46.669894),
                exigeCodigo = false // Paga online direta: sem código
            ),
            DriverStop(
                id = "demo_stop_4",
                position = 4,
                status = "PENDING",
                customerName = "Rodrigo Mendes",
                customerPhone = "11988997766",
                address = "Rua Augusta, 2200 - Consolação",
                reference = "Casa com portão preto alto",
                notes = "Troco para R$ 100 em dinheiro.",
                amountCents = 6200,
                etaSeconds = 1920, // 32 min
                coordinates = DriverCoordinates(-23.557421, -46.661250),
                exigeCodigo = true // Levô Seguro: exige código
            )
        )

        return DriverRoute(
            routeId = "route_${token.take(8)}",
            establishmentName = "Pizzaria Bella Nápoles",
            exigeCodigo = true, // Exige 4 dígitos de código na entrega para segurança
            courierName = "Carlos Motoboy",
            status = "IN_PROGRESS",
            startedAt = "2026-09-20T21:00:00Z",
            origin = DriverCoordinates(-23.561500, -46.655000),
            totalStops = stops.size,
            stops = stops
        )
    }
}
