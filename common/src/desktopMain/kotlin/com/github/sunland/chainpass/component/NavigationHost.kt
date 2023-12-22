@file:JvmName("NavigationHostDesktop")

package com.github.sunland.chainpass.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.github.sunland.chainpass.component.NavigationState

@Composable
actual fun NavigationHost(
    navigationState: NavigationState,
    initialRoute: String,
    content: @Composable NavigationState.ComposableRouteScope.() -> Unit
) {
    content(navigationState.composableRouteScope)

    Crossfade(
        targetState = navigationState.currentComposableRoute,
        animationSpec = navigationState.currentComposableRoute?.route?.animation ?: tween()
    ) { composableRoute -> composableRoute?.composable?.invoke(composableRoute.route.argument) }

    LaunchedEffect(Unit) {
        navigationState.initStack(initialRoute)
    }
}