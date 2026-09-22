package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.SessionManager
import com.example.data.repository.DriverRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground Tracking Service for Levô Couriers.
 *
 * Robustness & Reliability Architecture:
 * 1. Uses PRIORITY_HIGH_ACCURACY when active to guarantee real-time motorcycle positioning.
 * 2. 10s intervals with 10m displacement: balanced for urban navigation and customer tracking.
 * 3. Immediate lastLocation fallback upon start to ensure a ping is dispatched without waiting for the first GPS fix.
 * 4. Resilient CoroutineScope handling with SupervisorJob.
 * 5. Automatic foreground notification keeps the process alive even when Maps or Waze take focus.
 */
class TrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: DriverRepository
    private var activeToken: String? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            dispatchPing(location.latitude, location.longitude)
        }
    }

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        repository = DriverRepository(this, sessionManager)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        activeToken = intent?.getStringExtra(EXTRA_TOKEN) ?: sessionManager.token

        if (activeToken == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Verify location permission before starting foreground
        val hasFine = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        // High accuracy GPS: ideal for motorcycle couriers in dense urban streets
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .setMinUpdateDistanceMeters(10f)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

            // Dispara imediatamente a última coordenada conhecida para não demorar
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    dispatchPing(loc.latitude, loc.longitude)
                }
            }
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun dispatchPing(lat: Double, lng: Double) {
        val token = activeToken ?: sessionManager.token ?: return
        serviceScope.launch {
            repository.sendPing(token, lat, lng)
        }
    }

    private fun buildNotification(): Notification {
        val tapIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.rastreio_titulo))
            .setContentText(getString(R.string.rastreio_texto))
            .setSmallIcon(R.drawable.ic_rastreio)
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.rastreio_canal),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.rastreio_canal_descricao)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "rastreio_levo"
        private const val NOTIFICATION_ID = 101
        private const val EXTRA_TOKEN = "extra_token"

        fun start(context: Context, token: String) {
            val intent = Intent(context, TrackingService::class.java).apply {
                putExtra(EXTRA_TOKEN, token)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TrackingService::class.java))
        }
    }
}
