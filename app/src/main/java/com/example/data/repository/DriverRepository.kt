package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.api.ApiClient
import com.example.data.api.RouteParser
import com.example.data.local.CachedRouteEntity
import com.example.data.local.DriverDao
import com.example.data.local.DriverDatabase
import com.example.data.local.PendingStopActionEntity
import com.example.data.local.SessionManager
import com.example.data.model.DriverCoordinates
import com.example.data.model.DriverRoute
import com.example.data.model.PingRequest
import com.example.data.model.ReplanRequest
import com.example.data.model.StopOutcomeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DriverRepository(
    context: Context,
    val sessionManager: SessionManager,
    private val driverDao: DriverDao = DriverDatabase.getDatabase(context).driverDao(),
    private val apiClient: ApiClient = ApiClient(sessionManager)
) {
    val pendingActionsCount: Flow<Int> = driverDao.observePendingActionsCount()

    suspend fun loadRoute(token: String, isDemo: Boolean = false): Result<DriverRoute> = withContext(Dispatchers.IO) {
        if (isDemo || token.startsWith("demo_")) {
            val demoRoute = RouteParser.createDemoRoute(token)
            return@withContext Result.success(demoRoute)
        }

        try {
            val api = apiClient.getService()
            // Request route page from Next.js server component with RSC header first
            val response = try {
                api.getCourierPage(token = token, rscHeader = "1")
            } catch (e: Exception) {
                // fallback to regular request
                api.getCourierPage(token = token, rscHeader = null)
            }

            if (response.isSuccessful) {
                val rawBody = response.body()?.string() ?: ""
                val route = RouteParser.parseRoute(rawBody, token)
                if (route != null) {
                    sessionManager.token = token
                    sessionManager.courierName = route.courierName
                    // Save to Room cache for offline durability
                    driverDao.saveCachedRoute(
                        CachedRouteEntity(
                            token = token,
                            routeId = route.routeId,
                            establishmentName = route.establishmentName,
                            courierName = route.courierName,
                            exigeCodigo = route.exigeCodigo,
                            routeJson = rawBody
                        )
                    )
                    return@withContext Result.success(route)
                }
            }
        } catch (e: Exception) {
            Log.e("DriverRepo", "Failed to fetch remote route: ${e.message}")
        }

        // Try offline cache from Room
        val cached = driverDao.getCachedRoute(token)
        if (cached != null) {
            val route = RouteParser.parseRoute(cached.routeJson, token)
            if (route != null) {
                return@withContext Result.success(route)
            }
        }

        // If not found or empty, return friendly demo or error
        Result.failure(Exception("Não foi possível carregar a rota com o token fornecido."))
    }

    suspend fun completeStop(
        token: String,
        stopId: String,
        outcome: String, // "DELIVERED" or "FAILED"
        reason: String? = null,
        deliveryCode: String? = null,
        currentLocation: DriverCoordinates? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val nowIso = isoDateString(System.currentTimeMillis())

        // 1. Persist to local offline queue first (Guarantee zero data loss)
        val actionId = driverDao.insertPendingAction(
            PendingStopActionEntity(
                token = token,
                stopId = stopId,
                outcome = outcome,
                reason = reason,
                deliveryCode = deliveryCode,
                occurredAt = nowIso,
                lat = currentLocation?.lat,
                lng = currentLocation?.lng
            )
        )

        // 2. Try to sync immediately if online
        try {
            val api = apiClient.getService()
            val response = api.completeStop(
                StopOutcomeRequest(
                    token = token,
                    stopId = stopId,
                    outcome = outcome,
                    reason = reason,
                    occurredAt = nowIso,
                    deliveryCode = deliveryCode,
                    lat = currentLocation?.lat,
                    lng = currentLocation?.lng
                )
            )

            if (response.isSuccessful) {
                driverDao.deletePendingAction(actionId)
                return@withContext Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.w("DriverRepo", "Sync will happen later: ${e.message}")
        }

        // Return success even if offline, since it's queued
        Result.success(Unit)
    }

    suspend fun syncPendingActions(): Int = withContext(Dispatchers.IO) {
        val pending = driverDao.getAllPendingActions()
        var syncedCount = 0
        val api = apiClient.getService()

        for (action in pending) {
            try {
                val response = api.completeStop(
                    StopOutcomeRequest(
                        token = action.token,
                        stopId = action.stopId,
                        outcome = action.outcome,
                        reason = action.reason,
                        occurredAt = action.occurredAt,
                        deliveryCode = action.deliveryCode,
                        lat = action.lat,
                        lng = action.lng
                    )
                )

                if (response.isSuccessful || response.code() in 400..499) {
                    driverDao.deletePendingAction(action.id)
                    syncedCount++
                } else {
                    break // Network error, retry later
                }
            } catch (e: Exception) {
                break
            }
        }
        syncedCount
    }

    suspend fun replanRoute(token: String, lat: Double, lng: Double): Result<String> = withContext(Dispatchers.IO) {
        if (token.startsWith("demo_") || token.length < 16) {
            // Demo mode or short token: handled locally by GPS optimization
            return@withContext Result.success("Rota reorganizada localmente pelo GPS!")
        }

        try {
            val api = apiClient.getService()
            val response = api.replan(ReplanRequest(token = token, lat = lat, lng = lng))
            if (response.isSuccessful) {
                return@withContext Result.success("Rota reorganizada com sucesso!")
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                val msg = try {
                    if (errBody.trim().startsWith("[")) {
                        val array = org.json.JSONArray(errBody)
                        if (array.length() > 0) {
                            array.getJSONObject(0).optString("message", "Erro ao recalcular rota")
                        } else {
                            "Erro ao recalcular rota"
                        }
                    } else {
                        JSONObject(errBody).optString("error", "Erro ao recalcular rota")
                    }
                } catch (e: Exception) {
                    "Erro ao recalcular rota (${response.code()})"
                }
                return@withContext Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Não foi possível conectar ao servidor: ${e.message}"))
        }
    }

    suspend fun sendPing(token: String, lat: Double, lng: Double): Boolean = withContext(Dispatchers.IO) {
        try {
            val api = apiClient.getService()
            val response = api.ping(
                PingRequest(
                    token = token,
                    lat = lat,
                    lng = lng,
                    at = isoDateString(System.currentTimeMillis())
                )
            )
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        fun isoDateString(timeMs: Long): String {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date(timeMs))
        }
    }
}
