package com.rix.womblab.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rix.womblab.presentation.calendar.CalendarScreen
import com.rix.womblab.presentation.detail.EventDetailScreen
import com.rix.womblab.presentation.home.HomeScreen
import com.rix.womblab.presentation.notifications.NotificationsScreen
import com.rix.womblab.presentation.profile.EditProfileScreen
import com.rix.womblab.presentation.profile.ProfileScreen
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var currentTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(currentDestination?.route) {
        currentTabIndex = when (currentDestination?.route) {
            Screen.Home.route -> 0
            Screen.Calendar.route -> 1
            Screen.Profile.route -> 2
            else -> currentTabIndex
        }
    }

    var swipeOffset by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 120.dp.toPx() }

    Scaffold(
        bottomBar = {
            if (currentDestination?.route != Screen.EventDetail.route && currentDestination?.route != Screen.Notifications.route && currentDestination?.route != Screen.EditProfile.route) {
                ProfessionalNavigationBar(
                    currentDestination = currentDestination,
                    currentTabIndex = currentTabIndex,
                    onNavigate = { route, index ->
                        currentTabIndex = index
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(currentTabIndex) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (abs(swipeOffset) > swipeThreshold) {
                                val newIndex = if (swipeOffset > 0) {
                                    (currentTabIndex - 1).coerceAtLeast(0)
                                } else {
                                    (currentTabIndex + 1).coerceAtMost(2)
                                }

                                if (newIndex != currentTabIndex) {
                                    val newRoute = when (newIndex) {
                                        0 -> Screen.Home.route
                                        1 -> Screen.Calendar.route
                                        2 -> Screen.Profile.route
                                        else -> Screen.Home.route
                                    }

                                    currentTabIndex = newIndex
                                    navController.navigate(newRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            swipeOffset = 0f
                        }
                    ) { _, dragAmount ->
                        swipeOffset += dragAmount
                    }
                }
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = swipeOffset * 0.1f
                    },
                enterTransition = {
                    val direction = getSlideDirection(initialState.destination.route, targetState.destination.route)
                    slideInHorizontally(
                        initialOffsetX = { fullWidth ->
                            if (direction > 0) fullWidth else -fullWidth
                        },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = LinearEasing
                        )
                    )
                },
                exitTransition = {
                    val direction = getSlideDirection(initialState.destination.route, targetState.destination.route)
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth ->
                            if (direction > 0) -fullWidth else fullWidth
                        },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 150,
                            easing = LinearEasing
                        )
                    )
                },
                popEnterTransition = {
                    val direction = getSlideDirection(targetState.destination.route, initialState.destination.route)
                    slideInHorizontally(
                        initialOffsetX = { fullWidth ->
                            if (direction > 0) fullWidth else -fullWidth
                        },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = LinearEasing
                        )
                    )
                },
                popExitTransition = {
                    val direction = getSlideDirection(targetState.destination.route, initialState.destination.route)
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth ->
                            if (direction > 0) -fullWidth else fullWidth
                        },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 150,
                            easing = LinearEasing
                        )
                    )
                }
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onEventClick = { eventId ->
                            navController.navigate(Screen.EventDetail.createRoute(eventId))
                        },
                        onNavigateToNotifications = {
                            navController.navigate(Screen.Notifications.route)
                        }
                    )
                }

                composable(
                    route = Screen.Notifications.route,
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { it }) + fadeIn()
                    },
                    exitTransition = {
                        slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    }
                ) {
                    NotificationsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEvent = { eventId ->
                            navController.navigate(Screen.EventDetail.createRoute(eventId))
                        }
                    )
                }

                composable(Screen.Calendar.route) {
                    CalendarScreen(
                        onEventClick = { eventId ->
                            navController.navigate(Screen.EventDetail.createRoute(eventId))
                        }
                    )
                }

                composable(Screen.Profile.route) { backStackEntry ->
                    val profileUpdated = navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.get<Boolean>("profile_updated") ?: false

                    if (profileUpdated) {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("profile_updated", false)
                    }

                    ProfileScreen(
                        onLogoutSuccess = {
                            onLogout()
                        },
                        onNavigateToEditProfile = {
                            navController.navigate(Screen.EditProfile.route)
                        }
                    )
                }

                composable(
                    route = Screen.EditProfile.route,
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { it }) + fadeIn()
                    },
                    exitTransition = {
                        slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    }
                ) {
                    EditProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onProfileUpdated = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("profile_updated", true)
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = Screen.EventDetail.route,
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = LinearEasing
                            )
                        )
                    },
                    exitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = LinearEasing
                            )
                        )
                    }
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                    EventDetailScreen(
                        eventId = eventId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfessionalNavigationBar(
    currentDestination: androidx.navigation.NavDestination?,
    currentTabIndex: Int,
    onNavigate: (String, Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        bottomNavigationItems.forEachIndexed { index, item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            val selectionAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                ),
                label = "selectionAlpha"
            )

            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1f,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                ),
                label = "iconScale"
            )

            NavigationBarItem(
                icon = {
                    Box {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.labelResId.toString(),
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )
                    }
                },
                label = {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing
                            )
                        ) + scaleIn(
                            initialScale = 0.8f,
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = 150,
                                easing = FastOutSlowInEasing
                            )
                        ) + scaleOut(
                            targetScale = 0.8f,
                            animationSpec = tween(
                                durationMillis = 150,
                                easing = FastOutSlowInEasing
                            )
                        )
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(id = item.labelResId),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onNavigate(item.route, index)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = selectionAlpha)
                )
            )
        }
    }
}

private fun getSlideDirection(fromRoute: String?, toRoute: String?): Int {
    val fromIndex = when (fromRoute) {
        Screen.Home.route -> 0
        Screen.Calendar.route -> 1
        Screen.Profile.route -> 2
        else -> 0
    }

    val toIndex = when (toRoute) {
        Screen.Home.route -> 0
        Screen.Calendar.route -> 1
        Screen.Profile.route -> 2
        else -> 0
    }

    return toIndex - fromIndex
}
