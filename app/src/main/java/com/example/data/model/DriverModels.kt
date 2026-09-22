package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DriverCoordinates(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double
)

@JsonClass(generateAdapter = true)
data class DriverStop(
    @Json(name = "id") val id: String,
    @Json(name = "position") val position: Int,
    @Json(name = "status") val status: String = "PENDING", // "PENDING", "DELIVERED", "FAILED"
    @Json(name = "customerName") val customerName: String,
    @Json(name = "customerPhone") val customerPhone: String? = null,
    @Json(name = "address") val address: String,
    @Json(name = "reference") val reference: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "amountCents") val amountCents: Int = 0,
    @Json(name = "etaSeconds") val etaSeconds: Int = 0,
    @Json(name = "coordinates") val coordinates: DriverCoordinates? = null,
    @Json(name = "exigeCodigo") val exigeCodigo: Boolean? = null,
    @Json(name = "deliveryCode") val deliveryCode: String? = null,
    @Json(name = "failureReason") val failureReason: String? = null
) {
    val isPending: Boolean get() = status == "PENDING"
    val isInProgress: Boolean get() = status == "IN_PROGRESS" || status == "IN_TRANSIT"
    val isDelivered: Boolean get() = status == "DELIVERED"
    val isFailed: Boolean get() = status == "FAILED" || status == "CANCELLED"
    val isCompleted: Boolean get() = isDelivered || isFailed
    val isOpen: Boolean get() = !isCompleted

    fun requiresConfirmationCode(routeDefault: Boolean = false): Boolean {
        return exigeCodigo ?: routeDefault
    }

    val formattedAmount: String get() {
        if (amountCents <= 0) return "Pago no app"
        val reais = amountCents / 100
        val centavos = amountCents % 100
        return "R$ $reais,${centavos.toString().padStart(2, '0')}"
    }

    val formattedEta: String get() {
        if (etaSeconds <= 0) return "--"
        val min = Math.max(1, etaSeconds / 60)
        return "$min min"
    }
}

@JsonClass(generateAdapter = true)
data class DriverRoute(
    @Json(name = "routeId") val routeId: String,
    @Json(name = "establishmentName") val establishmentName: String,
    @Json(name = "exigeCodigo") val exigeCodigo: Boolean = false,
    @Json(name = "courierName") val courierName: String,
    @Json(name = "status") val status: String = "IN_PROGRESS", // "PLANNED", "IN_PROGRESS", "FINISHED"
    @Json(name = "startedAt") val startedAt: String? = null,
    @Json(name = "origin") val origin: DriverCoordinates? = null,
    @Json(name = "totalStops") val totalStops: Int = 0,
    @Json(name = "stops") val stops: List<DriverStop> = emptyList()
) {
    val pendingStops: List<DriverStop> get() = stops.filter { it.isOpen }
    val completedStopsCount: Int get() = stops.count { it.isCompleted }
    val progressFraction: Float get() = if (stops.isEmpty()) 0f else completedStopsCount.toFloat() / stops.size.toFloat()
}

@JsonClass(generateAdapter = true)
data class PingRequest(
    @Json(name = "token") val token: String,
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double,
    @Json(name = "at") val at: String
)

@JsonClass(generateAdapter = true)
data class StopOutcomeRequest(
    @Json(name = "token") val token: String,
    @Json(name = "stopId") val stopId: String,
    @Json(name = "outcome") val outcome: String, // "DELIVERED" | "FAILED"
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "occurredAt") val occurredAt: String? = null,
    @Json(name = "deliveryCode") val deliveryCode: String? = null,
    @Json(name = "lat") val lat: Double? = null,
    @Json(name = "lng") val lng: Double? = null
)

@JsonClass(generateAdapter = true)
data class ReplanRequest(
    @Json(name = "token") val token: String,
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double
)
