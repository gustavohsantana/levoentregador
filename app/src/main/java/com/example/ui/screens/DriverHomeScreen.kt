package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.DriverCoordinates
import com.example.data.model.DriverRoute
import com.example.data.model.DriverStop
import com.example.ui.components.ActiveStopCard
import com.example.ui.components.OutcomeDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StopListItem
import com.example.ui.components.TransitStartedDialog
import com.example.ui.theme.LevoBorderSubtle
import com.example.ui.theme.LevoPrimary
import com.example.ui.theme.LevoSuccess
import com.example.ui.theme.LevoSuccessBg
import com.example.ui.theme.LevoSurfaceHigh
import com.example.ui.theme.LevoSurfaceHighlight
import com.example.ui.theme.LevoTextPrimary
import com.example.ui.theme.LevoTextSecondary
import com.example.ui.theme.LevoTextTertiary
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Driver Screen designed with Google Maps & Nubank principles:
 * - Fluid high contrast typography
 * - Clean surface hierarchy
 * - Zero visual clutter, pure driver ergonomics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    route: DriverRoute,
    activeStop: DriverStop?,
    isTrackingActive: Boolean,
    pendingSyncCount: Int,
    isReplanning: Boolean,
    preferredNav: String,
    serverUrl: String,
    errorMessage: String?,
    infoMessage: String?,
    outcomeTargetStop: DriverStop?,
    outcomeType: String?,
    feePerDeliveryCents: Int = 700,
    onFeeChange: (Int) -> Unit = {},
    onSelectStop: (String) -> Unit,
    onStartStop: (String) -> Unit,
    onOpenOutcomeDialog: (DriverStop, String) -> Unit,
    onCloseOutcomeDialog: () -> Unit,
    onSubmitOutcome: (code: String?, reason: String?, coords: DriverCoordinates?) -> Unit,
    onReplan: (lat: Double, lng: Double) -> Unit,
    onSyncPending: () -> Unit,
    onPreferredNavChange: (String) -> Unit,
    onServerUrlChange: (String) -> Unit,
    onToggleTracking: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onClearMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSettings by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Rota/Cockpit, 1: Ganhos
    var stopFilterMode by remember { mutableIntStateOf(0) } // 0: Todas, 1: Pendentes, 2: Concluídas
    var transitDialogStop by remember { mutableStateOf<DriverStop?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            onReplan(loc.latitude, loc.longitude)
                        } else {
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                                val lat = lastLoc?.latitude ?: route.origin?.lat ?: -23.5505
                                val lng = lastLoc?.longitude ?: route.origin?.lng ?: -46.6333
                                onReplan(lat, lng)
                            }
                        }
                    }
            } catch (e: SecurityException) {
                // ignore
            }
        }
    }

    val triggerReplan = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            onReplan(loc.latitude, loc.longitude)
                        } else {
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                                val lat = lastLoc?.latitude ?: route.origin?.lat ?: -23.5505
                                val lng = lastLoc?.longitude ?: route.origin?.lng ?: -46.6333
                                onReplan(lat, lng)
                            }
                        }
                    }
            } catch (e: SecurityException) {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(errorMessage, infoMessage) {
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(errorMessage)
            onClearMessages()
        } else if (!infoMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(infoMessage)
            onClearMessages()
        }
    }

    val completedDeliveriesCount = route.stops.count { it.isDelivered }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LevoSurfaceHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = LevoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = route.establishmentName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = LevoTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(LevoSuccess)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Rastreamento Ativo",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = LevoSuccess
                                    )
                                )

                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "·",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LevoTextTertiary
                                )
                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = "${route.completedStopsCount}/${route.totalStops} entregas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LevoTextSecondary
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (pendingSyncCount > 0) {
                        IconButton(onClick = onSyncPending, modifier = Modifier.testTag("action_sync")) {
                            BadgedBox(badge = {
                                Badge(containerColor = LevoPrimary) {
                                    Text("$pendingSyncCount")
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Sincronizar",
                                    tint = LevoPrimary
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LevoSurfaceHigh)
                            .testTag("action_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Preferências",
                            tint = LevoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Rota",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Entregas",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LevoPrimary,
                        selectedTextColor = LevoPrimary,
                        indicatorColor = LevoSurfaceHighlight,
                        unselectedIconColor = LevoTextTertiary,
                        unselectedTextColor = LevoTextTertiary
                    ),
                    modifier = Modifier.testTag("tab_route")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(badge = {
                            if (completedDeliveriesCount > 0) {
                                Badge(containerColor = LevoSuccess) {
                                    Text("$completedDeliveriesCount")
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Ganhos",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Ganhos",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LevoSuccess,
                        selectedTextColor = LevoSuccess,
                        indicatorColor = LevoSurfaceHighlight,
                        unselectedIconColor = LevoTextTertiary,
                        unselectedTextColor = LevoTextTertiary
                    ),
                    modifier = Modifier.testTag("tab_earnings")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (selectedTab == 1) {
            EarningsScreen(
                route = route,
                feePerDeliveryCents = feePerDeliveryCents,
                onFeeChange = onFeeChange,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Shift Quick Summary Header (Zé Delivery / Box Delivery style)
                item {
                    val completedCount = route.stops.count { it.isDelivered }
                    val totalCollectCents = route.stops.filter { it.isDelivered }.sumOf { it.amountCents }
                    val totalToCollectRemaining = route.stops.filter { it.isOpen }.sumOf { it.amountCents }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = LevoSurfaceHigh,
                        border = BorderStroke(1.dp, LevoBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(LevoSuccess)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "TURNO ATIVO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            fontSize = 11.sp
                                        ),
                                        color = LevoTextTertiary
                                    )
                                }

                                Text(
                                    text = "$completedCount/${route.totalStops} entregues (${(route.progressFraction * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = LevoSuccess
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Micro progress track
                            LinearProgressIndicator(
                                progress = { route.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = LevoSuccess,
                                trackColor = LevoSurfaceHighlight
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Taxa estimada",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = LevoTextTertiary
                                    )
                                    Text(
                                        text = "R$ ${(completedCount * feePerDeliveryCents / 100)},${(completedCount * feePerDeliveryCents % 100).toString().padStart(2, '0')}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = LevoTextPrimary
                                        )
                                    )
                                }

                                if (totalToCollectRemaining > 0) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "A receber em mãos",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = LevoTextTertiary
                                        )
                                        Text(
                                            text = "R$ ${(totalToCollectRemaining / 100)},${(totalToCollectRemaining % 100).toString().padStart(2, '0')}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = LevoSuccess
                                            )
                                        )
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Cobranças",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = LevoTextTertiary
                                        )
                                        Text(
                                            text = "100% Online",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF38BDF8)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Completed state
                if (route.pendingStops.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = LevoSurfaceHigh,
                            border = BorderStroke(1.dp, LevoSuccess.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(26.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(LevoSuccessBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = LevoSuccess,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Rota Finalizada",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.4).sp
                                    ),
                                    color = LevoTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Todas as ${route.totalStops} entregas foram concluídas com sucesso.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LevoTextSecondary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = onLogout,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("button_finish_and_wait_next"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LevoSuccess,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        text = "Aguardar Próximo Despacho",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else if (activeStop != null) {
                    item {
                        ActiveStopCard(
                            stop = activeStop,
                            totalStops = route.totalStops,
                            routeExigeCodigo = route.exigeCodigo,
                            onStartClick = { stop ->
                                onStartStop(stop.id)
                                transitDialogStop = stop
                            },
                            onDeliveredClick = { stop -> onOpenOutcomeDialog(stop, "DELIVERED") },
                            onFailedClick = { stop -> onOpenOutcomeDialog(stop, "FAILED") },
                            onRetryClick = { stop -> onStartStop(stop.id) }
                        )
                    }
                }

                // Stops Section Header
                if (route.stops.size > 1) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Sequência de Entregas",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        ),
                                        color = LevoTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LevoSurfaceHighlight)
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${route.stops.size} paradas",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.5.sp
                                            ),
                                            color = LevoTextSecondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${route.completedStopsCount} de ${route.totalStops} concluídas · Toque para alternar",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = LevoTextTertiary
                                )
                            }

                            if (route.pendingStops.size >= 2) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = LevoPrimary.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, LevoPrimary.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(enabled = !isReplanning) { triggerReplan() }
                                        .testTag("button_replan_clean")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isReplanning) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(13.dp),
                                                strokeWidth = 2.dp,
                                                color = LevoPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.AltRoute,
                                                contentDescription = null,
                                                tint = LevoPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                        }
                                        Text(
                                            text = if (isReplanning) "Calculando..." else "Otimizar",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = LevoPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Filter Chips (Todas, Pendentes, Concluídas)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple(0, "Todas", route.stops.size),
                                Triple(1, "Pendentes", route.pendingStops.size),
                                Triple(2, "Concluídas", route.completedStopsCount)
                            ).forEach { (mode, label, count) ->
                                val isSelected = stopFilterMode == mode
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) LevoPrimary else LevoSurfaceHigh,
                                    border = BorderStroke(1.dp, if (isSelected) LevoPrimary else LevoBorderSubtle),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { stopFilterMode = mode }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color.White else LevoTextSecondary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White.copy(alpha = 0.25f) else LevoSurfaceHighlight)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "$count",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) Color.White else LevoTextTertiary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val filteredStops = when (stopFilterMode) {
                        1 -> route.stops.filter { it.isOpen }
                        2 -> route.stops.filter { it.isCompleted }
                        else -> route.stops
                    }

                    items(filteredStops, key = { it.id }) { stop ->
                        StopListItem(
                            stop = stop,
                            isActive = activeStop?.id == stop.id,
                            preferredNav = preferredNav,
                            routeExigeCodigo = route.exigeCodigo,
                            onClick = { onSelectStop(stop.id) },
                            onStartClick = { onStartStop(stop.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (outcomeTargetStop != null && outcomeType != null) {
        OutcomeDialog(
            stop = outcomeTargetStop,
            outcomeType = outcomeType,
            exigeCodigo = route.exigeCodigo,
            onDismiss = onCloseOutcomeDialog,
            onConfirm = { code, reason ->
                com.example.service.FloatingBubbleService.stop(context)
                onSubmitOutcome(code, reason, null)
            }
        )
    }

    if (transitDialogStop != null) {
        TransitStartedDialog(
            stop = transitDialogStop!!,
            preferredNav = preferredNav,
            onConfirmGoToMap = {
                val target = transitDialogStop!!
                transitDialogStop = null
                com.example.util.NavigationHelper.openPreferredNavigation(
                    context = context,
                    stop = target,
                    preferredNav = preferredNav
                )
            },
            onDismiss = { transitDialogStop = null }
        )
    }

    if (showSettings) {
        SettingsDialog(
            preferredNav = preferredNav,
            serverUrl = serverUrl,
            isTrackingActive = isTrackingActive,
            pendingSyncCount = pendingSyncCount,
            onPreferredNavChange = onPreferredNavChange,
            onServerUrlChange = onServerUrlChange,
            onToggleTracking = onToggleTracking,
            onSyncNow = onSyncPending,
            onLogout = {
                showSettings = false
                onLogout()
            },
            onDismiss = { showSettings = false }
        )
    }
}
