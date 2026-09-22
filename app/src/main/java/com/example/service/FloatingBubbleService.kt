package com.example.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.MainActivity
import com.example.R
import kotlin.math.abs

/**
 * Intelligent Floating HUD Service for Levô Couriers.
 *
 * Concepts:
 * 1. Compact Bubble by default: Shows minimal footprint so it never blocks Maps/Waze turns.
 * 2. Smart Expansion: Tapping expands into a micro HUD showing customer name, address, collection amount,
 *    and 1-tap actions (Call Customer, Mark Delivered, or Return to Levô).
 * 3. Magnetic Edge Snapping: When released after dragging, smoothly docks to the nearest screen edge.
 * 4. Safe Close button: Allows dismissing the HUD cleanly from inside the overlay.
 */
class FloatingBubbleService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null

    private var isExpanded = false
    private var currentStopId: String? = null
    private var currentStopName: String = "Parada"
    private var currentAddress: String = ""
    private var currentPhone: String? = null
    private var currentAmount: String? = null
    private var currentPosition: Int = 1

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 360
        }

        floatingView = createFloatingView()

        try {
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        currentStopId = intent?.getStringExtra(EXTRA_STOP_ID)
        currentStopName = intent?.getStringExtra(EXTRA_STOP_NAME) ?: "Cliente"
        currentAddress = intent?.getStringExtra(EXTRA_STOP_ADDRESS) ?: ""
        currentPhone = intent?.getStringExtra(EXTRA_STOP_PHONE)
        currentAmount = intent?.getStringExtra(EXTRA_STOP_AMOUNT)
        currentPosition = intent?.getIntExtra(EXTRA_STOP_POSITION, 1) ?: 1

        updateViews()

        return START_STICKY
    }

    private fun updateViews() {
        val view = floatingView ?: return

        val customerNameView = view.findViewById<TextView>(R.id.bubble_customer_name)
        val stepTagView = view.findViewById<TextView>(R.id.bubble_step_tag)
        val amountTagView = view.findViewById<TextView>(R.id.bubble_amount_tag)
        val addressView = view.findViewById<TextView>(R.id.bubble_address_text)
        val callBtn = view.findViewById<View>(R.id.bubble_btn_call)

        customerNameView?.text = currentStopName
        stepTagView?.text = "LEVÔ · PARADA $currentPosition"

        if (!currentAmount.isNullOrBlank()) {
            amountTagView?.visibility = View.VISIBLE
            amountTagView?.text = "COBRAR $currentAmount"
        } else {
            amountTagView?.visibility = View.GONE
        }

        if (currentAddress.isNotBlank()) {
            addressView?.text = currentAddress
        }

        callBtn?.visibility = if (!currentPhone.isNullOrBlank()) View.VISIBLE else View.GONE
    }

    private fun toggleExpansion() {
        isExpanded = !isExpanded
        val view = floatingView ?: return

        val deck = view.findViewById<View>(R.id.bubble_actions_deck)
        val expandBtn = view.findViewById<ImageView>(R.id.bubble_btn_expand)

        if (isExpanded) {
            deck?.visibility = View.VISIBLE
            expandBtn?.setImageResource(R.drawable.ic_bubble_collapse)
        } else {
            deck?.visibility = View.GONE
            expandBtn?.setImageResource(R.drawable.ic_bubble_expand)
        }

        try {
            windowManager?.updateViewLayout(floatingView, params)
        } catch (e: Exception) {
            // view detached
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingView(): View {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.layout_floating_bubble, null)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        // Top drag and tap area
        val headerRow = view.findViewById<View>(R.id.bubble_header_row)
        val expandBtn = view.findViewById<View>(R.id.bubble_btn_expand)
        val callBtn = view.findViewById<View>(R.id.bubble_btn_call)
        val deliverBtn = view.findViewById<View>(R.id.bubble_btn_deliver)
        val closeBtn = view.findViewById<View>(R.id.bubble_btn_close)

        expandBtn.setOnClickListener {
            toggleExpansion()
        }

        callBtn.setOnClickListener {
            dialPhone(currentPhone)
        }

        deliverBtn.setOnClickListener {
            // Reabre o Levô diretamente com o diálogo de confirmação da parada ativa
            openLevoApp(action = ACTION_CONFIRM_DELIVERY, stopId = currentStopId)
            stopSelf()
        }

        closeBtn.setOnClickListener {
            stopSelf()
        }

        headerRow.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params?.x ?: 0
                    initialY = params?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()

                    if (abs(deltaX) > 12 || abs(deltaY) > 12) {
                        isDragging = true
                    }

                    if (isDragging) {
                        params?.x = initialX + deltaX
                        params?.y = initialY + deltaY
                        try {
                            windowManager?.updateViewLayout(floatingView, params)
                        } catch (e: Exception) {
                            // view detached
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Tocou no header: alterna expansão inteligente do mini-card
                        toggleExpansion()
                    } else {
                        // Soltou do arrasto: Magnetic Snap para a borda lateral mais próxima
                        snapToNearestEdge()
                    }
                    true
                }
                else -> false
            }
        }

        return view
    }

    private fun snapToNearestEdge() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val currentX = params?.x ?: return

        params?.x = if (currentX + 100 > screenWidth / 2) {
            screenWidth - (floatingView?.width ?: 240) - 24
        } else {
            24
        }

        try {
            windowManager?.updateViewLayout(floatingView, params)
        } catch (e: Exception) {
            // view detached
        }
    }

    private fun dialPhone(phone: String?) {
        if (phone.isNullOrBlank()) return
        val clean = phone.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // dialer not found
        }
    }

    private fun openLevoApp(action: String? = null, stopId: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            if (action != null) {
                this.action = action
                putExtra(EXTRA_STOP_ID, stopId)
            }
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    companion object {
        const val ACTION_STOP = "com.example.action.STOP_BUBBLE"
        const val ACTION_CONFIRM_DELIVERY = "com.example.action.CONFIRM_DELIVERY"

        const val EXTRA_STOP_ID = "extra_stop_id"
        const val EXTRA_STOP_NAME = "extra_stop_name"
        const val EXTRA_STOP_ADDRESS = "extra_stop_address"
        const val EXTRA_STOP_PHONE = "extra_stop_phone"
        const val EXTRA_STOP_AMOUNT = "extra_stop_amount"
        const val EXTRA_STOP_POSITION = "extra_stop_position"

        fun start(
            context: Context,
            stopId: String,
            customerName: String,
            address: String,
            phone: String? = null,
            formattedAmount: String? = null,
            position: Int = 1
        ) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, FloatingBubbleService::class.java).apply {
                    putExtra(EXTRA_STOP_ID, stopId)
                    putExtra(EXTRA_STOP_NAME, customerName)
                    putExtra(EXTRA_STOP_ADDRESS, address)
                    putExtra(EXTRA_STOP_PHONE, phone)
                    putExtra(EXTRA_STOP_AMOUNT, formattedAmount)
                    putExtra(EXTRA_STOP_POSITION, position)
                }
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingBubbleService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
