package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =================================================================================================
// LUXURY DARK COLOR SYSTEM (Midnight Void, Obsidian, Cyber Indigo, Ethereal Cyan & Cosmic Aurora)
// Crafted for immersive focus, visual depth, and luxury aesthetic without harsh neon or eyestrain.
// =================================================================================================

// Deep Multi-Layered Dark Backgrounds & Obsidian Canvas
val LuxuryVoidBackground = Color(0xFF080B11)      // Ultra-deep canvas with faint indigo tint
val LuxuryCanvas = Color(0xFF0D121B)              // Primary dark screen surface
val LuxurySurface = Color(0xFF131A26)             // Glassmorphic / card surface
val LuxurySurfaceElevated = Color(0xFF1B2433)     // High-elevation cards & floating panels
val LuxurySurfaceHighlight = Color(0xFF2B384E)    // Crisp subtle borders & glass highlights
val LuxurySurfaceGlassBorder = Color(0x337DD3FC)  // Luminous glass border (20% Cyan/Sky)
val LuxurySurfaceGlassBorderGold = Color(0x40D4AF37) // Luminous gold border for epic series

// Additional Luxury Color Tokens
val LuxuryElectricCyan = Color(0xFF38BDF8)
val LuxuryAuroraViolet = Color(0xFFA78BFA)
val LuxuryCyberIndigo = Color(0xFF6366F1)
val LuxurySubtleGold = Color(0xFFFBBF24)

// Legacy aliases for full backward compatibility
val VeyronisBackground = LuxuryVoidBackground
val VeyronisPanel = LuxurySurface
val VeyronisPanelVariant = LuxurySurfaceElevated
val VeyronisSurfaceHighlight = LuxurySurfaceHighlight

// Primary Accent Spectrum: Electric Sky / Ethereal Cyan
val LuxuryPrimary = Color(0xFF38BDF8)             // Radiant sky cyan accent
val LuxuryPrimaryVariant = Color(0xFF0EA5E9)      // Deep electric blue
val LuxuryPrimaryContainer = Color(0xFF0C2744)    // Rich midnight container
val LuxuryPrimaryGlow = Color(0x6638BDF8)         // Soft ambient glow

val VeyronisPrimary = LuxuryPrimary
val VeyronisPrimaryContainer = LuxuryPrimaryContainer

// Secondary Accent Spectrum: Royal Violet & Neon Indigo
val LuxurySecondary = Color(0xFFA78BFA)           // Ethereal Lavender Violet
val LuxurySecondaryVariant = Color(0xFF8B5CF6)    // Royal Amethyst
val LuxurySecondaryContainer = Color(0xFF28184C)  // Deep cosmic purple container
val LuxurySecondaryGlow = Color(0x66A78BFA)

val VeyronisSecondary = LuxurySecondary
val VeyronisSecondaryContainer = LuxurySecondaryContainer

// Tertiary & Luxury Accents: Celestial Gold & Rose
val LuxuryGold = Color(0xFFFBBF24)                // Radiant Celestial Gold
val LuxuryGoldContainer = Color(0xFF45300B)       // Rich bronze container
val LuxuryRose = Color(0xFFFB7185)                // Soft glowing rose

val VeyronisTertiary = LuxuryGold
val VeyronisTertiaryContainer = LuxuryGoldContainer

// Semantic Tokens
val LuxurySuccess = Color(0xFF34D399)             // Emerald mint
val LuxurySuccessContainer = Color(0xFF0E3D2D)
val LuxuryWarning = Color(0xFFF87171)             // Crimson coral
val LuxuryWarningContainer = Color(0xFF421515)
val LuxuryInfo = Color(0xFF60A5FA)                // Azure

val VeyronisSuccess = LuxurySuccess
val VeyronisWarning = LuxuryWarning
val VeyronisWarningContainer = LuxuryWarningContainer

// Typography Tokens (High Legibility & Hierarchy)
val LuxuryTextPrimary = Color(0xFFF8FAFC)         // Crisp pure white for titles & reading
val LuxuryTextSecondary = Color(0xFF94A3B8)       // Balanced slate for subtitles & blurbs
val LuxuryTextMuted = Color(0xFF64748B)           // Muted metadata & timestamps
val LuxuryTextDisabled = Color(0xFF475569)

val VeyronisTextPrimary = LuxuryTextPrimary
val VeyronisTextSecondary = LuxuryTextSecondary
val VeyronisTextMuted = LuxuryTextMuted

// =================================================================================================
// LUXURY AURORA GRADIENT PRESETS
// =================================================================================================

// Button Gradients
val LuxuryPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF0EA5E9), Color(0xFF38BDF8), Color(0xFF67E8F9))
)

val LuxurySecondaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF7C3AED), Color(0xFFA78BFA), Color(0xFFC084FC))
)

val LuxuryGoldGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFB45309), Color(0xFFF59E0B), Color(0xFFFDE68A))
)

val LuxuryDestructiveGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF991B1B), Color(0xFFEF4444), Color(0xFFF87171))
)

val LuxuryGhostGlassGradient = Brush.linearGradient(
    colors = listOf(Color(0x261E293B), Color(0x140F172A))
)

val LuxuryPanelGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF161F2E), Color(0xFF0F1521))
)

// Background Ambient Aurora Orbs (Subtle, non-distracting background depth)
val LuxuryAuroraTopGlow = Brush.radialGradient(
    colors = listOf(Color(0x1F38BDF8), Color(0x0A7C3AED), Color.Transparent),
    radius = 700f
)

val LuxuryAuroraBottomGlow = Brush.radialGradient(
    colors = listOf(Color(0x1A8B5CF6), Color(0x0838BDF8), Color.Transparent),
    radius = 800f
)
