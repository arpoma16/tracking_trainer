package com.example.trianner4.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.trianner4.R
import com.example.trianner4.ui.ajustes.AjustesScreen
import com.example.trianner4.ui.ajustes.BackupRestoreScreen
import com.example.trianner4.ui.ajustes.PerfilScreen
import com.example.trianner4.ui.ajustes.TriggersProgresoScreen
import com.example.trianner4.ui.bienestar.BienestarScreen
import com.example.trianner4.ui.calendario.CalendarioDiaScreen
import com.example.trianner4.ui.calendario.CalendarioScreen
import com.example.trianner4.ui.exercises.ExerciseScreen
import com.example.trianner4.ui.rutinas.BibliotecaEjerciciosScreen
import com.example.trianner4.ui.rutinas.EjercicioDetalleScreen
import com.example.trianner4.ui.rutinas.RutinaEditorScreen
import com.example.trianner4.ui.rutinas.RutinasScreen
import com.example.trianner4.ui.session.SesionActivaScreen
import com.example.trianner4.ui.today.TodayScreen

// ── Definición de items del bottom bar ─────────────────────────────────────────

private data class BottomNavItem(
    val graphRoute: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(TabGraph.HOY,        R.string.nav_today,    Icons.Default.Home),
    BottomNavItem(TabGraph.CALENDARIO, R.string.nav_calendar, Icons.Default.DateRange),
    BottomNavItem(TabGraph.RUTINAS,    R.string.nav_routines, Icons.AutoMirrored.Filled.List),
    BottomNavItem(TabGraph.BIENESTAR,  R.string.nav_wellness, Icons.Default.Favorite),
    BottomNavItem(TabGraph.AJUSTES,    R.string.nav_settings, Icons.Default.AccountCircle),
)

// ── Punto de entrada ────────────────────────────────────────────────────────────

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination
        ?.hierarchy
        ?.any { it.route in TabGraph.all } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(navController = navController, currentDestination = currentDestination)
            }
        }
    ) {
        AppNavHost(navController = navController, innerPadding = it)
    }
}

// ── Bottom bar ──────────────────────────────────────────────────────────────────

@Composable
private fun AppBottomBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.graphRoute } == true
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                label = { Text(stringResource(item.labelRes)) },
                selected = selected,
                onClick = {
                    navController.navigate(item.graphRoute) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}

// ── Grafo de navegación ─────────────────────────────────────────────────────────

@Composable
private fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = TabGraph.HOY,
        modifier = Modifier.padding(innerPadding),
    ) {

        // ── Tab: Hoy ───────────────────────────────────────────────────────────
        navigation(startDestination = NavRoutes.HOY, route = TabGraph.HOY) {
            composable(
                route = NavRoutes.HOY,
                deepLinks = listOf(
                    androidx.navigation.navDeepLink {
                        uriPattern = "app://trianner/today"
                    }
                )
            ) {
                TodayScreen(
                    onStartSession = { routineId ->
                        navController.navigate(NavRoutes.sesionActiva(routineId))
                    },
                    onReportDiscomfort = {
                        navController.navigate(TabGraph.BIENESTAR) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }

        // ── Tab: Calendario ────────────────────────────────────────────────────
        navigation(startDestination = NavRoutes.CALENDARIO, route = TabGraph.CALENDARIO) {
            composable(NavRoutes.CALENDARIO) {
                CalendarioScreen(
                    onDayClick = { fecha -> navController.navigate(NavRoutes.calendarioDia(fecha)) },
                )
            }
            composable(
                route = NavRoutes.CALENDARIO_DIA,
                arguments = listOf(navArgument(NavRoutes.ARG_FECHA) { type = NavType.StringType }),
            ) { entry ->
                CalendarioDiaScreen(
                    fecha = entry.arguments?.getString(NavRoutes.ARG_FECHA).orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }
        }

        // ── Tab: Rutinas ───────────────────────────────────────────────────────
        navigation(startDestination = NavRoutes.RUTINAS, route = TabGraph.RUTINAS) {
            composable(NavRoutes.RUTINAS) {
                RutinasScreen(
                    onCreateRoutine = { navController.navigate(NavRoutes.RUTINA_NUEVA) },
                    onEditRoutine = { id -> navController.navigate(NavRoutes.rutinaEditor(id)) },
                    onOpenBiblioteca = { navController.navigate(NavRoutes.BIBLIOTECA) }
                )
            }
            composable(NavRoutes.RUTINA_NUEVA) {
                RutinaEditorScreen(rutinaId = null, onBack = { navController.popBackStack() })
            }
            composable(
                route = NavRoutes.RUTINA_EDITOR,
                arguments = listOf(navArgument(NavRoutes.ARG_RUTINA_ID) { type = NavType.LongType }),
            ) { entry ->
                RutinaEditorScreen(
                    rutinaId = entry.arguments?.getLong(NavRoutes.ARG_RUTINA_ID),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(NavRoutes.BIBLIOTECA) {
                BibliotecaEjerciciosScreen(
                    onExerciseClick = { id -> navController.navigate(NavRoutes.ejercicioEditor(id)) },
                    onCreateExerciseClick = { navController.navigate(NavRoutes.EJERCICIO_CREAR) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = NavRoutes.EJERCICIO_EDITOR,
                arguments = listOf(navArgument(NavRoutes.ARG_EJERCICIO_ID) { type = NavType.LongType }),
            ) { entry ->
                ExerciseScreen(
                    exerciseId = entry.arguments?.getLong(NavRoutes.ARG_EJERCICIO_ID),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = NavRoutes.EJERCICIO_DETALLE,
                arguments = listOf(navArgument(NavRoutes.ARG_EJERCICIO_ID) { type = NavType.LongType }),
            ) { entry ->
                EjercicioDetalleScreen(
                    ejercicioId = entry.arguments?.getLong(NavRoutes.ARG_EJERCICIO_ID) ?: -1L,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(route = NavRoutes.EJERCICIO_CREAR) {
                ExerciseScreen(onBack = { navController.popBackStack() })
            }
        }

        // ── Tab: Bienestar ─────────────────────────────────────────────────────
        navigation(startDestination = NavRoutes.BIENESTAR, route = TabGraph.BIENESTAR) {
            composable(NavRoutes.BIENESTAR) {
                BienestarScreen()
            }
        }

        // ── Tab: Ajustes ───────────────────────────────────────────────────────
        navigation(startDestination = NavRoutes.AJUSTES, route = TabGraph.AJUSTES) {
            composable(NavRoutes.AJUSTES) {
                AjustesScreen(
                    onOpenPerfil = { navController.navigate(NavRoutes.PERFIL) },
                    onOpenTriggers = { navController.navigate(NavRoutes.TRIGGERS) },
                    onOpenBackup = { navController.navigate(NavRoutes.BACKUP) },
                )
            }
            composable(NavRoutes.PERFIL) {
                PerfilScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.TRIGGERS) {
                TriggersProgresoScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.BACKUP) {
                BackupRestoreScreen(onBack = { navController.popBackStack() })
            }
        }

        // ── Sesión activa: top-level, fuera de todo tab graph ──────────────────
        composable(
            route = NavRoutes.SESION_ACTIVA,
            arguments = listOf(
                navArgument(NavRoutes.ARG_ROUTINE_ID_SESSION) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            ),
        ) {
            SesionActivaScreen(
                routineId = it.arguments?.getLong(NavRoutes.ARG_ROUTINE_ID_SESSION) ?: 0L,
                onSessionFinished = { navController.popBackStack() },
            )
        }
    }
}
