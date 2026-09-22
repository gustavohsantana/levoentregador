package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Levô Precision Brand Identity (Material You / Google & FinTech Tier)
val LevoPrimary = Color(0xFFFF5722)         // Deep Flame Orange
val LevoPrimaryLight = Color(0xFFFF8A50)    // Subtle accent
val LevoPrimaryDark = Color(0xFFE64A19)     // Rich brand depth
val LevoPrimaryContainer = Color(0xFF2C1E18) // M3 tinted dark container
val LevoOnPrimaryContainer = Color(0xFFFFCCBC)

// Nubank / Google Modern Dark Architecture (Pure OLED Depth)
val LevoBackground = Color(0xFF0F1015)       // Ultra-refined near-black canvas
val LevoSurface = Color(0xFF16181F)          // Level 1: Clean background cards
val LevoSurfaceHigh = Color(0xFF1E212A)      // Level 2: Raised interactive modules
val LevoSurfaceHighlight = Color(0xFF272B36) // Level 3: Active states & chips

// Subtle Micro-borders (Replaces heavy borders with Apple/Google 1dp hair lines)
val LevoBorderSubtle = Color(0xFF262A35)
val LevoBorderFocus = Color(0xFF3E4554)

// State & Metric Indicators
val LevoSuccess = Color(0xFF10B981)          // Emerald 500 (Clean financial green)
val LevoSuccessBg = Color(0xFF063523)
val LevoWarning = Color(0xFFF59E0B)          // Amber 500
val LevoError = Color(0xFFEF4444)            // Rose 500
val LevoErrorBg = Color(0xFF381414)
val LevoInfo = Color(0xFF3B82F6)             // Royal Blue 500

// High-Fidelity Typography Tokens
val LevoTextPrimary = Color(0xFFF8FAFC)      // Slate 50 (Crisp, anti-fatigue)
val LevoTextSecondary = Color(0xFF94A3B8)    // Slate 400
val LevoTextTertiary = Color(0xFF64748B)     // Slate 500
val LevoTextInverse = Color(0xFF090A0F)

// Backward compatibility aliases
val LevoOrange = LevoPrimary
val LevoOrangeLight = LevoPrimaryLight
val LevoOrangeDark = LevoPrimaryDark
val LevoAmber = LevoWarning
val LevoBackgroundDark = LevoBackground
val LevoSurfaceDark = LevoSurface
val LevoSurfaceRaised = LevoSurfaceHigh
val LevoSurfaceElevated = LevoSurfaceHighlight
val LevoBorderDark = LevoBorderSubtle
val LevoBorderHighlight = LevoBorderFocus
val LevoGreenSuccess = LevoSuccess
val LevoGreenDark = Color(0xFF059669)
val LevoRedError = LevoError
val LevoWazeCyan = Color(0xFF06B6D4)
val LevoMapsBlue = Color(0xFF2563EB)
val LevoTextMuted = LevoTextTertiary
