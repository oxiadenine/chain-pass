package io.sunland.chainpass.common

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Theme {
    object ColorScheme {
        val lightPrimary = Color(0xff6750a4)
        val lightOnPrimary = Color(0xffffffff)
        val lightPrimaryContainer = Color(0xffe9ddff)
        val lightOnPrimaryContainer = Color(0xff23005c)
        val lightSecondary = Color(0xff006688)
        val lightOnSecondary = Color(0xffffffff)
        val lightSecondaryContainer = Color(0xffc2e8ff)
        val lightOnSecondaryContainer = Color(0xff001e2b)
        val lightTertiary = Color(0xff355ca8)
        val lightOnTertiary = Color(0xffffffff)
        val lightTertiaryContainer = Color(0xffd9e2ff)
        val lightOnTertiaryContainer = Color(0xff001943)
        val lightError = Color(0xffba1a1a)
        val lightErrorContainer = Color(0xffffdad6)
        val lightOnError = Color(0xffffffff)
        val lightOnErrorContainer = Color(0xff410002)
        val lightBackground = Color(0xfffffbff)
        val lightOnBackground = Color(0xff1c1b1e)
        val lightSurface = Color(0xfffffbff)
        val lightOnSurface = Color(0xff1c1b1e)
        val lightSurfaceVariant = Color(0xffe7e0eb)
        val lightOnSurfaceVariant = Color(0xff49454e)
        val lightOutline = Color(0xff7a757f)
        val lightInverseOnSurface = Color(0xfff4eff4)
        val lightInverseSurface = Color(0xff323033)
        val lightInversePrimary = Color(0xffd0bcff)
        val lightSurfaceTint = Color(0xff6750a4)
        val lightOutlineVariant = Color(0xffcac4cf)
        val lightScrim = Color(0xff000000)

        val darkPrimary = Color(0xffd0bcff)
        val darkOnPrimary = Color(0xff381e72)
        val darkPrimaryContainer = Color(0xff4f378a)
        val darkOnPrimaryContainer = Color(0xffe9ddff)
        val darkSecondary = Color(0xff75d1ff)
        val darkOnSecondary = Color(0xff003548)
        val darkSecondaryContainer = Color(0xff004d67)
        val darkOnSecondaryContainer = Color(0xffc2e8ff)
        val darkTertiary = Color(0xffafc6ff)
        val darkOnTertiary = Color(0xff002d6c)
        val darkTertiaryContainer = Color(0xff16448f)
        val darkOnTertiaryContainer = Color(0xffd9e2ff)
        val darkError = Color(0xffffb4ab)
        val darkErrorContainer = Color(0xff93000a)
        val darkOnError = Color(0xff690005)
        val darkOnErrorContainer = Color(0xffffdad6)
        val darkBackground = Color(0xff1c1b1e)
        val darkOnBackground = Color(0xffe6e1e6)
        val darkSurface = Color(0xff1c1b1e)
        val darkOnSurface = Color(0xffe6e1e6)
        val darkSurfaceVariant = Color(0xff49454e)
        val darkOnSurfaceVariant = Color(0xffcac4cf)
        val darkOutline = Color(0xff948f99)
        val darkInverseOnSurface = Color(0xff1c1b1e)
        val darkInverseSurface = Color(0xffe6e1e6)
        val darkInversePrimary = Color(0xff6750a4)
        val darkSurfaceTint = Color(0xffd0bcff)
        val darkOutlineVariant = Color(0xff49454e)
        val darkScrim = Color(0xff000000)
    }

    val LightColors = lightColorScheme(
        primary = ColorScheme.lightPrimary,
        onPrimary = ColorScheme.lightOnPrimary,
        primaryContainer = ColorScheme.lightPrimaryContainer,
        onPrimaryContainer = ColorScheme.lightOnPrimaryContainer,
        secondary = ColorScheme.lightSecondary,
        onSecondary = ColorScheme.lightOnSecondary,
        secondaryContainer = ColorScheme.lightSecondaryContainer,
        onSecondaryContainer = ColorScheme.lightOnSecondaryContainer,
        tertiary = ColorScheme.lightTertiary,
        onTertiary = ColorScheme.lightOnTertiary,
        tertiaryContainer = ColorScheme.lightTertiaryContainer,
        onTertiaryContainer = ColorScheme.lightOnTertiaryContainer,
        error = ColorScheme.lightError,
        errorContainer = ColorScheme.lightErrorContainer,
        onError = ColorScheme.lightOnError,
        onErrorContainer = ColorScheme.lightOnErrorContainer,
        background = ColorScheme.lightBackground,
        onBackground = ColorScheme.lightOnBackground,
        surface = ColorScheme.lightSurface,
        onSurface = ColorScheme.lightOnSurface,
        surfaceVariant = ColorScheme.lightSurfaceVariant,
        onSurfaceVariant = ColorScheme.lightOnSurfaceVariant,
        outline = ColorScheme.lightOutline,
        inverseOnSurface = ColorScheme.lightInverseOnSurface,
        inverseSurface = ColorScheme.lightInverseSurface,
        inversePrimary = ColorScheme.lightInversePrimary,
        surfaceTint = ColorScheme.lightSurfaceTint,
        outlineVariant = ColorScheme.lightOutlineVariant,
        scrim = ColorScheme.lightScrim
    )

    val DarkColors = darkColorScheme(
        primary = ColorScheme.darkPrimary,
        onPrimary = ColorScheme.darkOnPrimary,
        primaryContainer = ColorScheme.darkPrimaryContainer,
        onPrimaryContainer = ColorScheme.darkOnPrimaryContainer,
        secondary = ColorScheme.darkSecondary,
        onSecondary = ColorScheme.darkOnSecondary,
        secondaryContainer = ColorScheme.darkSecondaryContainer,
        onSecondaryContainer = ColorScheme.darkOnSecondaryContainer,
        tertiary = ColorScheme.darkTertiary,
        onTertiary = ColorScheme.darkOnTertiary,
        tertiaryContainer = ColorScheme.darkTertiaryContainer,
        onTertiaryContainer = ColorScheme.darkOnTertiaryContainer,
        error = ColorScheme.darkError,
        errorContainer = ColorScheme.darkErrorContainer,
        onError = ColorScheme.darkOnError,
        onErrorContainer = ColorScheme.darkOnErrorContainer,
        background = ColorScheme.darkBackground,
        onBackground = ColorScheme.darkOnBackground,
        surface = ColorScheme.darkSurface,
        onSurface = ColorScheme.darkOnSurface,
        surfaceVariant = ColorScheme.darkSurfaceVariant,
        onSurfaceVariant = ColorScheme.darkOnSurfaceVariant,
        outline = ColorScheme.darkOutline,
        inverseOnSurface = ColorScheme.darkInverseOnSurface,
        inverseSurface = ColorScheme.darkInverseSurface,
        inversePrimary = ColorScheme.darkInversePrimary,
        surfaceTint = ColorScheme.darkSurfaceTint,
        outlineVariant = ColorScheme.darkOutlineVariant,
        scrim = ColorScheme.darkScrim
    )
}