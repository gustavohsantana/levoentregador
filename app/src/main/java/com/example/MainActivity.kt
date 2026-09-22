package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import com.example.service.FloatingBubbleService
import com.example.ui.DriverViewModel
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.DriverHomeScreen
import com.example.ui.screens.LoginRouteScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DriverViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LevoApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == FloatingBubbleService.ACTION_CONFIRM_DELIVERY) {
            val stopId = intent.getStringExtra(FloatingBubbleService.EXTRA_STOP_ID)
            viewModel.openOutcomeForStopId(stopId, "DELIVERED")
        }
    }
}

@Composable
fun LevoApp(viewModel: DriverViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLoginSettings by remember { mutableStateOf(false) }

    // Request necessary runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    val currentRoute = uiState.route
    if (currentRoute == null) {
        LoginRouteScreen(
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            serverUrl = uiState.serverUrl,
            isDriverLoggedIn = uiState.isDriverLoggedIn,
            savedCourierName = uiState.courierName,
            savedPhoneOrId = uiState.courierPhoneOrId,
            onDriverLogin = { phoneOrId, pin, name ->
                viewModel.loginDriver(phoneOrId, pin, name)
            },
            onOpenRoute = { token, isDemo -> viewModel.loadRoute(token, isDemo) },
            onCheckAssignedRoute = { viewModel.checkAssignedRoute() },
            onOpenSettings = { showLoginSettings = true },
            onFullLogout = { viewModel.fullLogout() }
        )

        if (showLoginSettings) {
            SettingsDialog(
                preferredNav = uiState.preferredNav,
                serverUrl = uiState.serverUrl,
                isTrackingActive = uiState.isTrackingActive,
                pendingSyncCount = uiState.pendingSyncCount,
                onPreferredNavChange = { viewModel.setPreferredNav(it) },
                onServerUrlChange = { viewModel.setServerUrl(it) },
                onToggleTracking = { viewModel.toggleTracking(it) },
                onSyncNow = { viewModel.syncPendingActions() },
                onLogout = {
                    showLoginSettings = false
                    viewModel.fullLogout()
                },
                onDismiss = { showLoginSettings = false }
            )
        }
    } else {
        DriverHomeScreen(
            route = currentRoute,
            activeStop = uiState.activeStop,
            isTrackingActive = uiState.isTrackingActive,
            pendingSyncCount = uiState.pendingSyncCount,
            isReplanning = uiState.isReplanning,
            preferredNav = uiState.preferredNav,
            serverUrl = uiState.serverUrl,
            errorMessage = uiState.errorMessage,
            infoMessage = uiState.infoMessage,
            outcomeTargetStop = uiState.outcomeTargetStop,
            outcomeType = uiState.outcomeType,
            onSelectStop = { viewModel.selectStop(it) },
            onStartStop = { viewModel.startStop(it) },
            onOpenOutcomeDialog = { stop, type -> viewModel.openOutcomeDialog(stop, type) },
            onCloseOutcomeDialog = { viewModel.closeOutcomeDialog() },
            onSubmitOutcome = { code, reason, coords ->
                viewModel.submitOutcome(code, reason, coords)
            },
            onReplan = { lat, lng -> viewModel.replanRoute(lat, lng) },
            onSyncPending = { viewModel.syncPendingActions() },
            onPreferredNavChange = { viewModel.setPreferredNav(it) },
            onServerUrlChange = { viewModel.setServerUrl(it) },
            onToggleTracking = { viewModel.toggleTracking(it) },
            feePerDeliveryCents = uiState.feePerDeliveryCents,
            onFeeChange = { viewModel.setFeePerDeliveryCents(it) },
            onLogout = { viewModel.logout() },
            onClearMessages = { viewModel.clearMessages() }
        )
    }
}

