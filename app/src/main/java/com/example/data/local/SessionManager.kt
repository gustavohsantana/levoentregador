package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("levo_driver_prefs", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
        set(value) {
            val sanitized = value.trim().removeSuffix("/")
            prefs.edit().putString(KEY_SERVER_URL, sanitized).apply()
        }

    var preferredNav: String
        get() = prefs.getString(KEY_NAV_PREF, NAV_MAPS) ?: NAV_MAPS
        set(value) = prefs.edit().putString(KEY_NAV_PREF, value).apply()

    var isTrackingEnabled: Boolean
        get() = prefs.getBoolean(KEY_TRACKING_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_TRACKING_ENABLED, value).apply()

    var activeStopId: String?
        get() = prefs.getString(KEY_ACTIVE_STOP_ID, null)
        set(value) = prefs.edit().putString(KEY_ACTIVE_STOP_ID, value).apply()

    var courierName: String?
        get() = prefs.getString(KEY_COURIER_NAME, null)
        set(value) = prefs.edit().putString(KEY_COURIER_NAME, value).apply()

    var courierPhoneOrId: String?
        get() = prefs.getString(KEY_COURIER_PHONE_OR_ID, null)
        set(value) = prefs.edit().putString(KEY_COURIER_PHONE_OR_ID, value).apply()

    var courierPin: String?
        get() = prefs.getString(KEY_COURIER_PIN, null)
        set(value) = prefs.edit().putString(KEY_COURIER_PIN, value).apply()

    var isDriverLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var feePerDeliveryCents: Int
        get() = prefs.getInt(KEY_FEE_PER_DELIVERY, 700) // R$ 7,00 padrão por entrega
        set(value) = prefs.edit().putInt(KEY_FEE_PER_DELIVERY, value).apply()

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_ACTIVE_STOP_ID).apply()
    }

    fun fullLogout() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_ACTIVE_STOP_ID)
            .remove(KEY_COURIER_NAME)
            .remove(KEY_COURIER_PHONE_OR_ID)
            .remove(KEY_COURIER_PIN)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }

    companion object {
        const val DEFAULT_SERVER_URL = "https://levoentregas.vercel.app"
        const val NAV_MAPS = "MAPS"
        const val NAV_WAZE = "WAZE"

        private const val KEY_TOKEN = "key_route_token"
        private const val KEY_SERVER_URL = "key_server_url"
        private const val KEY_NAV_PREF = "key_nav_pref"
        private const val KEY_TRACKING_ENABLED = "key_tracking_enabled"
        private const val KEY_ACTIVE_STOP_ID = "key_active_stop_id"
        private const val KEY_COURIER_NAME = "key_courier_name"
        private const val KEY_COURIER_PHONE_OR_ID = "key_courier_phone_or_id"
        private const val KEY_COURIER_PIN = "key_courier_pin"
        private const val KEY_IS_LOGGED_IN = "key_is_driver_logged_in"
        private const val KEY_FEE_PER_DELIVERY = "key_fee_per_delivery_cents"
    }
}
