package com.tneff.remotecomposeexample

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * REM-108-example **S3 — theme** (TechSpec §7): the `./elf-schemes/material3-{light,dark}-theme.css`
 * Material3 `--md-sys-color-*` tokens, hand-mapped ONCE into two Compose [androidx.compose.material3.ColorScheme]
 * constants (the CSS is the source of truth; these constants are the code — no runtime CSS parsing). The
 * mapping is `--md-sys-color-<kebab>` → the camelCase ColorScheme field; CSS hex `#355CA8` → `Color(0xFF355CA8)`.
 *
 * Applied system-driven at the app root (App.kt): `if (isSystemInDarkTheme()) RcExampleDarkColors else
 * RcExampleLightColors`. Styles only the app chrome (menu/list/top-bar/viewer frame); the rendered `.rc`
 * docs carry their own colors and are unaffected.
 */
val RcExampleLightColors = lightColorScheme(
    primary = Color(0xFF355CA8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF001944),
    secondary = Color(0xFF166B53),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFA4F2D4),
    onSecondaryContainer = Color(0xFF002117),
    tertiary = Color(0xFF7C5800),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDEA6),
    onTertiaryContainer = Color(0xFF271900),
    error = Color(0xFFA83543),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDADA),
    onErrorContainer = Color(0xFF40000C),
    background = Color(0xFFFAF8FE),
    onBackground = Color(0xFF1A1B1F),
    surface = Color(0xFFFAF8FE),
    onSurface = Color(0xFF1A1B1F),
    surfaceVariant = Color(0xFFE0E2EF),
    onSurfaceVariant = Color(0xFF434751),
    surfaceDim = Color(0xFFDAD9DF),
    surfaceBright = Color(0xFFFAF8FE),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F3F9),
    surfaceContainer = Color(0xFFEEEDF3),
    surfaceContainerHigh = Color(0xFFE9E7ED),
    surfaceContainerHighest = Color(0xFFE3E2E7),
    outline = Color(0xFF747782),
    outlineVariant = Color(0xFFC3C6D3),
    inverseSurface = Color(0xFF2F3035),
    inverseOnSurface = Color(0xFFF1F0F6),
    inversePrimary = Color(0xFFAFC6FF),
    surfaceTint = Color(0xFF355CA8),
    scrim = Color(0xFF000000),
)

val RcExampleDarkColors = darkColorScheme(
    primary = Color(0xFFAFC6FF),
    onPrimary = Color(0xFF002D6D),
    primaryContainer = Color(0xFF16448F),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = Color(0xFF89D6B9),
    onSecondary = Color(0xFF003829),
    secondaryContainer = Color(0xFF00513D),
    onSecondaryContainer = Color(0xFFA4F2D4),
    tertiary = Color(0xFFF3BE55),
    onTertiary = Color(0xFF412D00),
    tertiaryContainer = Color(0xFF5E4200),
    onTertiaryContainer = Color(0xFFFFDEA6),
    error = Color(0xFFFFB3B6),
    onError = Color(0xFF680019),
    errorContainer = Color(0xFF881C2D),
    onErrorContainer = Color(0xFFFFDADA),
    background = Color(0xFF121317),
    onBackground = Color(0xFFE3E2E7),
    surface = Color(0xFF121317),
    onSurface = Color(0xFFE3E2E7),
    surfaceVariant = Color(0xFF434751),
    onSurfaceVariant = Color(0xFFC3C6D3),
    surfaceDim = Color(0xFF121317),
    surfaceBright = Color(0xFF38393D),
    surfaceContainerLowest = Color(0xFF0D0E12),
    surfaceContainerLow = Color(0xFF1A1B1F),
    surfaceContainer = Color(0xFF1E1F24),
    surfaceContainerHigh = Color(0xFF292A2E),
    surfaceContainerHighest = Color(0xFF343539),
    outline = Color(0xFF8D909C),
    outlineVariant = Color(0xFF434751),
    inverseSurface = Color(0xFFE3E2E7),
    inverseOnSurface = Color(0xFF2F3035),
    inversePrimary = Color(0xFF355CA8),
    surfaceTint = Color(0xFFAFC6FF),
    scrim = Color(0xFF000000),
)
