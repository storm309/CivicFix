package com.example.smartwastemanagementapp.ui.theme

import androidx.compose.ui.graphics.Color

// ────────────────────────────────────────────────────────────
//  CivicFix Premium Eco-Tech Color System
// ────────────────────────────────────────────────────────────

// Eco-Green (Primary) tonal palette
val EcoGreen10  = Color(0xFF002111)
val EcoGreen20  = Color(0xFF003920)
val EcoGreen30  = Color(0xFF005230)
val EcoGreen40  = Color(0xFF006D41)   // Light primary
val EcoGreen50  = Color(0xFF1B8A57)
val EcoGreen60  = Color(0xFF3EA872)
val EcoGreen70  = Color(0xFF5CC68D)
val EcoGreen80  = Color(0xFF79E4A9)
val EcoGreen90  = Color(0xFFA6F1C6)
val EcoGreen95  = Color(0xFFCEFBE2)
val EcoGreen99  = Color(0xFFF4FFF6)

// Teal (Secondary) tonal palette
val Teal10  = Color(0xFF00201A)
val Teal20  = Color(0xFF00382E)
val Teal40  = Color(0xFF006B5E)
val Teal80  = Color(0xFF7DECDC)
val Teal90  = Color(0xFFABF5ED)

// Amber (Tertiary / CTA) tonal palette
val Amber10  = Color(0xFF2E1500)
val Amber40  = Color(0xFFB45300)
val Amber80  = Color(0xFFFFBD88)
val Amber90  = Color(0xFFFFDCBE)

// Neutral Variant
val NeutralVar40 = Color(0xFF3D6357)
val NeutralVar80 = Color(0xFFAACBBF)
val NeutralVar90 = Color(0xFFC6E7DA)
val NeutralVar95 = Color(0xFFE3F5EE)

// Legacy aliases (kept for older references)
val Green80       = EcoGreen80
val GreenGrey80   = EcoGreen90
val Teal80Legacy  = Teal80
val Green40       = EcoGreen40
val GreenGrey40   = EcoGreen50
val Teal40Legacy  = Teal40

// Semantic status colours
val StatusPending          = Color(0xFFE65100)
val StatusPendingContainer = Color(0xFFFFEDE0)
val StatusCleaned          = Color(0xFF1B5E20)
val StatusCleanedContainer = Color(0xFFE8F5E9)

// Gradient helpers
val GradientStart = EcoGreen40
val GradientMid   = EcoGreen50
val GradientEnd   = Teal40
