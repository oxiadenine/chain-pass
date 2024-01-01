@file:JvmName("NavigationHostAndroid")

package io.sunland.chainpass.common.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun NavigationHost(
    navigationState: NavigationState,
    initialRoute: String,
    content: @Composable NavigationState.ComposableRouteScope.() -> Unit
) {
    content(navigationState.composableRouteScope)

    val composableRoute = navigationState.currentComposableRoute
    val routeArguments = navigationState.composableRouteScope.routeArgumentListState

    composableRoute.composable?.invoke(routeArguments)

    LaunchedEffect(Unit) {
        navigationState.currentComposableRoute = navigationState.composableRouteScope.composableRouteListState
            .firstOrNull { composableRoute ->
                composableRoute.route == initialRoute
            } ?: NavigationState.ComposableRoute(initialRoute)
    }
}