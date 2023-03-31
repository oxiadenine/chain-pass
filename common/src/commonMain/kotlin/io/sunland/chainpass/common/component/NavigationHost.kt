package io.sunland.chainpass.common.component

import androidx.compose.runtime.*

class NavigationState {
    data class RouteArgument(val name: String, val value: Any)

    class ComposableRoute(
        val route: String,
        val composable: (@Composable (List<RouteArgument>) -> Unit)? = null
    )

    class ComposableRouteScope {
        val composableRouteListState = mutableStateListOf<ComposableRoute>()
        val routeArgumentListState = mutableStateListOf<RouteArgument>()

        fun composableRoute(route: String, composable: @Composable (List<RouteArgument>) -> Unit) {
            val composableRoute = ComposableRoute(route, composable)

            composableRouteListState.add(composableRoute)
        }
    }

    var composableRouteScope by mutableStateOf(ComposableRouteScope())

    var currentComposableRoute by mutableStateOf(ComposableRoute(""))

    fun navigate(route: String, arguments: List<RouteArgument> = emptyList()) {
        val composableRoute = composableRouteScope.composableRouteListState.firstOrNull { composableRoute ->
            composableRoute.route == route
        }

        if (composableRoute != null) {
            currentComposableRoute = composableRoute

            composableRouteScope.routeArgumentListState.clear()
            composableRouteScope.routeArgumentListState.addAll(arguments)
        }
    }
}

@Composable
fun rememberNavigationState() = remember { NavigationState() }

@Composable
expect fun NavigationHost(
    navigationState: NavigationState,
    initialRoute: String,
    content: @Composable NavigationState.ComposableRouteScope.() -> Unit
)