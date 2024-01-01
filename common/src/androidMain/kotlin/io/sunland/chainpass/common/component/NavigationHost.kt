@file:JvmName("NavigationHostAndroid")

package io.sunland.chainpass.common.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

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
    ) { composableRoute ->
        BackHandler(
            enabled = navigationState.currentComposableRoute?.route?.path != initialRoute,
            onBack = { navigationState.pop() }
        )

        composableRoute?.composable?.invoke(composableRoute.route.argument)
    }

    LaunchedEffect(Unit) {
        navigationState.initStack(initialRoute)
    }
}