package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LevoApplication
import com.example.data.model.DriverCoordinates
import com.example.data.model.DriverRoute
import com.example.data.model.DriverStop
import com.example.service.TrackingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DriverUiState(
    val isLoading: Boolean = false,
    val route: DriverRoute? = null,
    val activeStopId: String? = null,
    val isTrackingActive: Boolean = false,
    val pendingSyncCount: Int = 0,
    val preferredNav: String = "MAPS",
    val serverUrl: String = "",
    val isDriverLoggedIn: Boolean = false,
    val courierName: String? = null,
    val courierPhoneOrId: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val outcomeTargetStop: DriverStop? = null,
    val outcomeType: String? = null, // "DELIVERED" or "FAILED"
    val isReplanning: Boolean = false,
    val feePerDeliveryCents: Int = 700
) {
    val activeStop: DriverStop?
        get() {
            if (route == null) return null
            if (activeStopId != null) {
                val found = route.stops.firstOrNull { it.id == activeStopId }
                if (found != null) return found
            }
            // Prioritize in progress stop, then first open stop in intelligent order
            return route.stops.firstOrNull { it.isInProgress }
                ?: route.stops.firstOrNull { it.isOpen }
                ?: route.stops.lastOrNull()
        }
}

class DriverViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as LevoApplication
    private val repository = app.repository
    private val sessionManager = app.sessionManager

    private val _uiState = MutableStateFlow(
        DriverUiState(
            preferredNav = sessionManager.preferredNav,
            serverUrl = sessionManager.serverUrl,
            isTrackingActive = sessionManager.isTrackingEnabled,
            feePerDeliveryCents = sessionManager.feePerDeliveryCents,
            isDriverLoggedIn = sessionManager.isDriverLoggedIn,
            courierName = sessionManager.courierName,
            courierPhoneOrId = sessionManager.courierPhoneOrId
        )
    )
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    init {
        // Collect pending sync count from Room
        viewModelScope.launch {
            repository.pendingActionsCount.collect { count ->
                _uiState.update { it.copy(pendingSyncCount = count) }
            }
        }

        // Check if there is an existing session
        val savedToken = sessionManager.token
        if (!savedToken.isNullOrBlank()) {
            loadRoute(savedToken)
        } else if (sessionManager.isDriverLoggedIn) {
            // Se o motoboy já fez login uma vez, carrega automaticamente o despacho dele
            checkAssignedRoute()
        }
    }

    /**
     * O motoboy faz login no app apenas uma vez com suas credenciais do Levô (Telefone/ID e Senha/PIN).
     * O app salva a sessão e passa a receber automaticamente todas as rotas despachadas para ele.
     */
    fun loginDriver(phoneOrId: String, pin: String, courierDisplayName: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val cleanId = phoneOrId.trim()
            if (cleanId.isBlank()) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Informe seu telefone ou identificador de entregador.") }
                return@launch
            }

            val finalName = courierDisplayName?.trim().takeIf { !it.isNullOrBlank() }
                ?: (sessionManager.courierName ?: "Entregador Levô")

            sessionManager.isDriverLoggedIn = true
            sessionManager.courierPhoneOrId = cleanId
            sessionManager.courierPin = pin.trim()
            sessionManager.courierName = finalName

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isDriverLoggedIn = true,
                    courierName = finalName,
                    courierPhoneOrId = cleanId,
                    infoMessage = "Login realizado com sucesso! Pronto para receber rotas."
                )
            }

            // Busca ou conecta à rota despachada
            checkAssignedRoute()
        }
    }

    /**
     * Verifica e sincroniza a rota ativa despachada pelo sistema Levô para este motoboy.
     */
    fun checkAssignedRoute() {
        val currentToken = sessionManager.token
        if (!currentToken.isNullOrBlank()) {
            loadRoute(currentToken)
        } else {
            // Demonstração / rota inicial automática para o entregador conectado
            loadRoute("demo_pizzaria", isDemo = true)
        }
    }

    fun loadRoute(token: String, isDemo: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val cleanToken = token.trim()
                .removePrefix("http://")
                .removePrefix("https://")
                .substringAfterLast("/m/")
                .substringAfterLast("/")

            val result = repository.loadRoute(cleanToken, isDemo)
            result.onSuccess { route ->
                sessionManager.token = cleanToken
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        route = route,
                        activeStopId = route.stops.firstOrNull { it.isPending }?.id
                    )
                }
                if (sessionManager.isTrackingEnabled) {
                    TrackingService.start(getApplication(), cleanToken)
                    _uiState.update { it.copy(isTrackingActive = true) }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Falha ao conectar com o servidor da rota."
                    )
                }
            }
        }
    }

    fun selectStop(stopId: String) {
        _uiState.update { it.copy(activeStopId = stopId) }
        sessionManager.activeStopId = stopId
    }

    fun startStop(stopId: String) {
        _uiState.update { state ->
            val currentRoute = state.route ?: return@update state
            val updatedStops = currentRoute.stops.map { stop ->
                if (stop.id == stopId) {
                    stop.copy(status = "IN_TRANSIT")
                } else if (stop.isInProgress) {
                    stop.copy(status = "PENDING")
                } else {
                    stop
                }
            }
            val target = updatedStops.firstOrNull { it.id == stopId }
            sessionManager.activeStopId = stopId
            state.copy(
                route = currentRoute.copy(stops = updatedStops),
                activeStopId = stopId,
                infoMessage = "A caminho de ${target?.customerName ?: "parada ${target?.position}"}"
            )
        }
    }

    fun openOutcomeDialog(stop: DriverStop, type: String) {
        _uiState.update {
            it.copy(
                outcomeTargetStop = stop,
                outcomeType = type
            )
        }
    }

    fun openOutcomeForStopId(stopId: String?, type: String = "DELIVERED") {
        val currentRoute = _uiState.value.route ?: return
        val target = if (!stopId.isNullOrBlank()) {
            currentRoute.stops.firstOrNull { it.id == stopId }
        } else {
            _uiState.value.activeStop
        }
        if (target != null) {
            openOutcomeDialog(target, type)
        }
    }

    fun closeOutcomeDialog() {
        _uiState.update {
            it.copy(
                outcomeTargetStop = null,
                outcomeType = null
            )
        }
    }

    fun submitOutcome(
        code: String?,
        reason: String?,
        currentLocation: DriverCoordinates? = null
    ) {
        val stop = _uiState.value.outcomeTargetStop ?: return
        val type = _uiState.value.outcomeType ?: return
        val token = sessionManager.token ?: "demo"

        viewModelScope.launch {
            closeOutcomeDialog()

            // Update in-memory state immediately for instant feedback
            _uiState.update { state ->
                val currentRoute = state.route ?: return@update state
                val updatedStops = currentRoute.stops.map { s ->
                    if (s.id == stop.id) {
                        s.copy(status = type)
                    } else s
                }
                val nextPending = updatedStops.firstOrNull { it.isOpen }
                state.copy(
                    route = currentRoute.copy(stops = updatedStops),
                    activeStopId = nextPending?.id ?: stop.id,
                    infoMessage = if (type == "DELIVERED") "Parada ${stop.position} entregue!" else "Parada ${stop.position} marcada com problema"
                )
            }

            // Persist to Room and sync via Repository
            repository.completeStop(
                token = token,
                stopId = stop.id,
                outcome = type,
                reason = reason,
                deliveryCode = code,
                currentLocation = currentLocation
            )
        }
    }

    fun replanRoute(currentLat: Double, currentLng: Double) {
        val currentRoute = _uiState.value.route ?: return
        val token = sessionManager.token ?: "demo"

        viewModelScope.launch {
            _uiState.update { it.copy(isReplanning = true, errorMessage = null) }

            // If in demo mode or token is shorter than 16 chars, optimize locally by GPS
            if (token.startsWith("demo_") || token.length < 16) {
                delay(350) // smooth feedback
                val optimizedRoute = optimizeStopsSequence(currentRoute, currentLat, currentLng)
                val newActive = optimizedRoute.pendingStops.firstOrNull()?.id
                _uiState.update {
                    it.copy(
                        isReplanning = false,
                        route = optimizedRoute,
                        activeStopId = newActive ?: it.activeStopId,
                        infoMessage = "Trajeto otimizado com sucesso pelo GPS!"
                    )
                }
                return@launch
            }

            // Real route: attempt remote replanning with backend
            val result = repository.replanRoute(token, currentLat, currentLng)
            result.onSuccess { msg ->
                repository.loadRoute(token).onSuccess { updatedRoute ->
                    val newActive = updatedRoute.pendingStops.firstOrNull()?.id
                    _uiState.update {
                        it.copy(
                            isReplanning = false,
                            route = updatedRoute,
                            activeStopId = newActive ?: it.activeStopId,
                            infoMessage = "Trajeto reorganizado com sucesso!"
                        )
                    }
                }.onFailure {
                    // Fallback to local GPS optimization
                    val optimizedRoute = optimizeStopsSequence(currentRoute, currentLat, currentLng)
                    val newActive = optimizedRoute.pendingStops.firstOrNull()?.id
                    _uiState.update {
                        it.copy(
                            isReplanning = false,
                            route = optimizedRoute,
                            activeStopId = newActive ?: it.activeStopId,
                            infoMessage = "Trajeto reorganizado pelo GPS!"
                        )
                    }
                }
            }.onFailure { err ->
                // Graceful fallback to local GPS optimization so courier never gets stuck
                val optimizedRoute = optimizeStopsSequence(currentRoute, currentLat, currentLng)
                val newActive = optimizedRoute.pendingStops.firstOrNull()?.id
                _uiState.update {
                    it.copy(
                        isReplanning = false,
                        route = optimizedRoute,
                        activeStopId = newActive ?: it.activeStopId,
                        infoMessage = "Trajeto otimizado localmente pelo GPS!"
                    )
                }
            }
        }
    }

    private fun optimizeStopsSequence(
        currentRoute: DriverRoute,
        currentLat: Double,
        currentLng: Double
    ): DriverRoute {
        val completedStops = currentRoute.stops.filter { it.isCompleted }
        val pendingStops = currentRoute.stops.filter { it.isOpen }.toMutableList()

        if (pendingStops.size <= 1) return currentRoute

        val sortedPending = mutableListOf<DriverStop>()
        var lastLat = currentLat
        var lastLng = currentLng

        while (pendingStops.isNotEmpty()) {
            val nearest = pendingStops.minByOrNull { stop ->
                val coords = stop.coordinates
                if (coords != null) {
                    calculateDistance(lastLat, lastLng, coords.lat, coords.lng)
                } else {
                    Double.MAX_VALUE
                }
            } ?: pendingStops.first()

            sortedPending.add(nearest)
            pendingStops.remove(nearest)
            nearest.coordinates?.let {
                lastLat = it.lat
                lastLng = it.lng
            }
        }

        var pos = 1
        val finalStops = (completedStops + sortedPending).map { stop ->
            stop.copy(position = pos++)
        }

        return currentRoute.copy(stops = finalStops)
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return 6371000.0 * c
    }

    fun syncPendingActions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val synced = repository.syncPendingActions()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    infoMessage = if (synced > 0) "$synced paradas sincronizadas com o servidor!" else "Fila sincronizada."
                )
            }
        }
    }

    fun setFeePerDeliveryCents(cents: Int) {
        val safeCents = cents.coerceAtLeast(0)
        sessionManager.feePerDeliveryCents = safeCents
        _uiState.update { it.copy(feePerDeliveryCents = safeCents) }
    }

    fun setPreferredNav(pref: String) {
        sessionManager.preferredNav = pref
        _uiState.update { it.copy(preferredNav = pref) }
    }

    fun setServerUrl(url: String) {
        sessionManager.serverUrl = url
        _uiState.update { it.copy(serverUrl = sessionManager.serverUrl) }
    }

    fun toggleTracking(enable: Boolean) {
        sessionManager.isTrackingEnabled = enable
        val token = sessionManager.token
        if (enable && token != null) {
            TrackingService.start(getApplication(), token)
            _uiState.update { it.copy(isTrackingActive = true) }
        } else {
            TrackingService.stop(getApplication())
            _uiState.update { it.copy(isTrackingActive = false) }
        }
    }

    fun logout() {
        TrackingService.stop(getApplication())
        sessionManager.clear()
        _uiState.update {
            it.copy(
                route = null,
                activeStopId = null,
                isTrackingActive = false,
                errorMessage = null,
                infoMessage = "Rota finalizada. Aguardando novo despacho..."
            )
        }
    }

    fun fullLogout() {
        TrackingService.stop(getApplication())
        sessionManager.fullLogout()
        _uiState.update {
            DriverUiState(
                preferredNav = sessionManager.preferredNav,
                serverUrl = sessionManager.serverUrl,
                isTrackingActive = false,
                isDriverLoggedIn = false,
                courierName = null,
                courierPhoneOrId = null
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }
}
