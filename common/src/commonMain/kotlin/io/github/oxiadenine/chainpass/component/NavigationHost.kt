package io.github.oxiadenine.chainpass.component

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.*

class NavigationState {
    data class Route(
        val path: String = "",
        val argument: RouteArgument? = null,
        val animation: FiniteAnimationSpec<Float>? = null
    )

    open class RouteArgument

    class ComposableRoute<in T : RouteArgument>(
        val route: Route,
        val composable: (@Composable (T?) -> Unit)? = null
    )

    class ComposableRouteScope {
        val composableRouteListState = mutableStateListOf<ComposableRoute<RouteArgument>>()

        fun <T : RouteArgument> composableRoute(route: String, composable: @Composable (T?) -> Unit) {
            val composableRoute = ComposableRoute(route = Route(route), composable = composable)

            composableRouteListState.add(composableRoute as ComposableRoute<RouteArgument>)
        }
    }

    var composableRouteScope by mutableStateOf(ComposableRouteScope())

    var currentComposableRoute by mutableStateOf<ComposableRoute<RouteArgument>?>(null)

    val composableRouteStack = mutableStateListOf<ComposableRoute<RouteArgument>>()

    fun initStack(initialRoute: String) {
        val composableRoute = composableRouteScope.composableRouteListState
            .firstOrNull { composableRoute ->
                composableRoute.route.path == initialRoute
            } ?: ComposableRoute(route = Route(initialRoute))

        composableRouteStack.add(composableRoute)

        currentComposableRoute = composableRoute
    }

    fun pop(route: Route? = null) {
        val lastComposableRoute = composableRouteStack.removeLastOrNull()

        currentComposableRoute = composableRouteStack.lastOrNull()?.let { composableRoute ->
            ComposableRoute(
                route = Route(
                    path = composableRoute.route.path,
                    argument = route?.argument ?: composableRoute.route.argument,
                    animation = route?.animation ?: lastComposableRoute?.route?.animation
                ),
                composable = composableRoute.composable
            )
        }
    }

    fun push(route: Route) {
        composableRouteScope.composableRouteListState.firstOrNull { composableRoute ->
            composableRoute.route.path == route.path
        }?.let { composableRoute ->
            composableRouteStack.add(ComposableRoute(route = route, composable = composableRoute.composable))

            currentComposableRoute = composableRouteStack.last()
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