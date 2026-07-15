package com.example.sistemacitasmedicas.view

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Principal : Screen("principal")
    object Alta : Screen("alta")
    object Edicion : Screen("edicion")
}

@Composable
fun NavGraph(viewModel: CitasViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Principal.route
    ) {
        composable(Screen.Principal.route) {
            PantallaPrincipal(
                viewModel = viewModel,
                onNavegarAlta = {
                    viewModel.limpiarFormulario()
                    navController.navigate(Screen.Alta.route)
                },
                onNavegarEdicion = { cita ->
                    viewModel.cargarCitaParaEdicion(cita)
                    navController.navigate(Screen.Edicion.route)
                }
            )
        }

        composable(Screen.Alta.route) {
            PantallaAlta(
                viewModel = viewModel,
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(Screen.Edicion.route) {
            PantallaEdicion(
                viewModel = viewModel,
                onRegresar = { navController.popBackStack() }
            )
        }
    }
}