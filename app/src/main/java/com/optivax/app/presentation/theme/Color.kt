package com.optivax.app.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * OptivaX Unified Design System Tokens
 * Based on the 60-30-10 Mobile Architecture (Canvas - Surface - Single Brand Accent)
 */
object OptivaColors {
    // 1. Signature Brand Identity (The 1 Dominant Brand Accent)
    val BrandPrimary = Color(0xFF00E5FF)          // Electric Cobalt Cyan
    val BrandPrimaryDark = Color(0xFF00B8CC)      // Pressed / Active
    val BrandPrimaryContainer = Color(0x2400E5FF) // 14% Tonal Container for active pills/chips
    val BrandPrimarySubtle = Color(0x0F00E5FF)    // 6% Subtle Accent Glow

    // 2. Neutral Surfaces & Elevations (Luminance Hierarchy)
    val CanvasBase = Color(0xFF07090E)            // Level 0: Pure Deep Obsidian Canvas
    val SurfaceSub = Color(0xFF0E1420)            // Level 1: Sub-surface / Inactive Grouping
    val SurfaceCard = Color(0xFF131B2B)           // Level 2: Default Card Surface
    val SurfaceElevated = Color(0xFF1B253B)       // Level 3: Elevated Card / Sheet / Dialog
    val SurfaceInteractive = Color(0xFF24314C)    // Level 4: Hover / Active Interactive State

    // 3. Specular Borders & Dividers
    val BorderSubtle = Color(0x14FFFFFF)          // 8% White - Elegant card outline
    val BorderStrong = Color(0x26FFFFFF)          // 15% White - Highlight edge
    val BorderBrand = Color(0x6600E5FF)           // 40% Brand - Active outline
    val Divider = Color(0x0FFFFFFF)               // 6% White - Row separators

    // 4. Functional / Semantic Supporting Colors (Strict Usage)
    val Success = Color(0xFF34D399)               // Neural Emerald (Clean completed / freed space)
    val SuccessContainer = Color(0x2610B981)
    val Warning = Color(0xFFFBBF24)               // Cyber Amber (80%+ Storage / Missing permission)
    val WarningContainer = Color(0x26F59E0B)
    val Danger = Color(0xFFF87171)                // Coral Red (Destructive permanent deletion)
    val DangerContainer = Color(0x26EF4444)
    val Info = Color(0xFF38BDF8)                  // Slate Cyan (Telemetry & system specs)

    // 5. Typography Hierarchy (WCAG AAA Compliant)
    val TextPrimary = Color(0xFFF8FAFC)           // Slate 50 (Soft Off-White, anti-halation)
    val TextSecondary = Color(0xFF94A3B8)         // Slate 400 (Subtitles, metadata)
    val TextMuted = Color(0xFF64748B)             // Slate 500 (Placeholders, inactive nav)
}

// Backward-Compatible & Ergonomic Aliases
val DeepObsidian = OptivaColors.CanvasBase
val DarkBackground = OptivaColors.CanvasBase
val ObsidianElevated = OptivaColors.SurfaceSub
val DarkSurface = OptivaColors.SurfaceCard
val DarkSurfaceCard = OptivaColors.SurfaceCard
val DarkSurfaceVariant = OptivaColors.SurfaceElevated
val SurfaceHighlight = OptivaColors.SurfaceInteractive

// Brand Primary Accent
val NeonCyan = OptivaColors.BrandPrimary
val NeonCyanGlow = OptivaColors.BrandPrimarySubtle

// Semantic Colors (Mapped to clean, accessible palette)
val NeonEmerald = OptivaColors.Success
val NeonEmeraldGlow = OptivaColors.SuccessContainer
val CyberAmber = OptivaColors.Warning
val NeonCoral = OptivaColors.Danger
val NeonCoralGlow = OptivaColors.DangerContainer
val CyberPurple = Color(0xFF818CF8) // Refined Slate Indigo
val CyberPurpleGlow = Color(0x26818CF8)

// Text Hues
val TextPrimary = OptivaColors.TextPrimary
val TextSecondary = OptivaColors.TextSecondary
val TextMuted = OptivaColors.TextMuted

// Borders & Outlines
val GlassBorder = OptivaColors.BorderSubtle
val GlassBorderPurple = OptivaColors.BorderSubtle
val OutlineDark = OptivaColors.BorderSubtle
val BorderBrand = OptivaColors.BorderBrand
