package com.mekki.taco.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mekki.taco.data.local.AlimentoDao // Import do DAO
import com.mekki.taco.ui.fooddetail.AlimentoDetailScreen // Import da tela de detalhes
import com.mekki.taco.ui.search.AlimentoDetailViewModel // ViewModel para detalhes
import com.mekki.taco.ui.search.AlimentoDetailViewModelFactory // Factory para ViewModel de detalhes
import com.mekki.taco.ui.search.AlimentoViewModel // ViewModel para busca
import com.mekki.taco.ui.search.AlimentoViewModelFactory // Factory para ViewModel de busca

// Definição das rotas para evitar strings mágicas
object AppDestinations {
    const val MAIN_SCREEN_ROUTE = "main_screen"
    const val DETAIL_SCREEN_BASE_ROUTE = "detail" // Rota base para detalhes
    const val DETAIL_SCREEN_WITH_ARG_ROUTE = "$DETAIL_SCREEN_BASE_ROUTE/{alimentoId}"
    const val ARG_ALIMENTO_ID = "alimentoId"

    // Rotas para funcionalidades futuras (placeholders)
    const val DIET_PLANNING_ROUTE = "diet_planning"
    const val DAILY_LOG_ROUTE = "daily_log"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    alimentoDao: AlimentoDao, // DAO é necessário para a AlimentoDetailViewModelFactory
    // A Factory do AlimentoSearchViewModel é passada diretamente, pois é criada na MainActivity
    alimentoSearchViewModelFactory: AlimentoViewModelFactory
) {
    NavHost(navController = navController, startDestination = AppDestinations.MAIN_SCREEN_ROUTE) {

        // Rota para a Tela Principal (que contém a busca e os botões de navegação)
        composable(route = AppDestinations.MAIN_SCREEN_ROUTE) {
            // Instancia o AlimentoViewModel (para a busca) usando sua factory
            val alimentoSearchViewModel: AlimentoViewModel = viewModel(factory = alimentoSearchViewModelFactory)

            MainScreen(
                alimentoViewModel = alimentoSearchViewModel,
                onAlimentoClick = { alimentoId ->
                    // Navega para a tela de detalhes, passando o ID do alimento
                    navController.navigate("${AppDestinations.DETAIL_SCREEN_BASE_ROUTE}/$alimentoId")
                },
                onPlanejarDietaClick = {
                    Log.d("AppNavHost", "Navegando para Planejamento de Dieta (TODO)")
                    navController.navigate(AppDestinations.DIET_PLANNING_ROUTE)
                },
                onDiarioAlimentarClick = {
                    Log.d("AppNavHost", "Navegando para Diário Alimentar (TODO)")
                    navController.navigate(AppDestinations.DAILY_LOG_ROUTE)
                }
            )
        }

        // Rota para a Tela de Detalhes do Alimento
        composable(
            route = AppDestinations.DETAIL_SCREEN_WITH_ARG_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_ALIMENTO_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val alimentoId = backStackEntry.arguments?.getInt(AppDestinations.ARG_ALIMENTO_ID)
            if (alimentoId != null) {
                // Cria a Factory para o AlimentoDetailViewModel, passando o alimentoId e o DAO
                val detailViewModelFactory = AlimentoDetailViewModelFactory(alimentoId, alimentoDao)
                val alimentoDetailViewModel: AlimentoDetailViewModel = viewModel(factory = detailViewModelFactory)

                AlimentoDetailScreen(
                    viewModel = alimentoDetailViewModel,
                    onNavigateBack = { navController.popBackStack() } // Função para o botão "Voltar"
                )
            } else {
                // Lida com o caso de ID nulo/inválido
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Erro: ID do alimento não fornecido ou inválido.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // Placeholder para a tela de Planejamento de Dieta
        composable(route = AppDestinations.DIET_PLANNING_ROUTE) {
            PlaceholderScreen(screenName = "Planejamento de Dieta")
        }

        // Placeholder para a tela de Diário Alimentar
        composable(route = AppDestinations.DAILY_LOG_ROUTE) {
            PlaceholderScreen(screenName = "Diário Alimentar")
        }
    }
}

// Um Composable genérico para telas futuras que ainda não foram implementadas
@Composable
fun PlaceholderScreen(screenName: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tela: $screenName", style = MaterialTheme.typography.headlineMedium)
        Text("(Em construção)", style = MaterialTheme.typography.bodyLarge)
    }
}