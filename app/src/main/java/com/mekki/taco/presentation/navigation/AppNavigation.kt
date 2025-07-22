package com.mekki.taco.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.presentation.ui.diet.DietDetailScreen
import com.mekki.taco.presentation.ui.diet.DietDetailViewModel
import com.mekki.taco.presentation.ui.diet.DietDetailViewModelFactory
import com.mekki.taco.presentation.ui.diet.DietListScreen
import com.mekki.taco.presentation.ui.diet.DietListViewModel
import com.mekki.taco.presentation.ui.diet.DietListViewModelFactory
import com.mekki.taco.presentation.ui.diet.CreateDietScreen
import com.mekki.taco.presentation.ui.fooddetail.AlimentoDetailScreen
import com.mekki.taco.presentation.ui.fooddetail.AlimentoDetailViewModel
import com.mekki.taco.presentation.ui.fooddetail.AlimentoDetailViewModelFactory
import com.mekki.taco.presentation.ui.search.AlimentoViewModel
import com.mekki.taco.presentation.ui.search.AlimentoViewModelFactory
import com.mekki.taco.presentation.ui.addfood.AddFoodToDietViewModel
import com.mekki.taco.presentation.ui.addfood.AddFoodToDietViewModelFactory
import com.mekki.taco.presentation.ui.addfood.AddFoodToDietScreen
import com.mekki.taco.presentation.ui.home.HomeScreen
import com.mekki.taco.presentation.ui.home.HomeViewModel
import com.mekki.taco.presentation.ui.home.HomeViewModelFactory
import com.mekki.taco.presentation.ui.search.AlimentoSearchScreen
import com.mekki.taco.data.db.dao.ItemDietaDao

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

        composable(
            route = AppDestinations.DIET_DETAIL_WITH_ARG_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_DIET_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val dietId = backStackEntry.arguments?.getInt(AppDestinations.ARG_DIET_ID)
            if (dietId != null) {
                val detailFactory = DietDetailViewModelFactory(dietId, dietaDao)
                val detailViewModel: DietDetailViewModel = viewModel(factory = detailFactory)
                DietDetailScreen(
                    viewModel = detailViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddFood = {
                        navController.navigate("${AppDestinations.ALIMENTO_SEARCH_BASE_ROUTE}/$dietId") }
                )
            }
        }

        composable(
            route = AppDestinations.ALIMENTO_SEARCH_WITH_ARG_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_DIET_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val dietId = backStackEntry.arguments?.getInt(AppDestinations.ARG_DIET_ID)
            if (dietId != null) {
                // Reuse the same ViewModelFactory as your MainScreen
                val searchViewModel: AlimentoViewModel = viewModel(factory = alimentoSearchViewModelFactory)

                // Assuming your AlimentoSearchScreen looks something like this:
                AlimentoSearchScreen(
                    viewModel = searchViewModel,
                    onAlimentoClick = { alimentoId ->
                        // When a food is clicked, navigate to the AddFoodToDietScreen
                        navController.navigate(
                            "${AppDestinations.ADD_FOOD_TO_DIET_BASE_ROUTE}/$dietId/$alimentoId"
                        )
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppDestinations.ADD_FOOD_TO_DIET_WITH_ARGS_ROUTE,
            arguments = listOf(
                navArgument(AppDestinations.ARG_DIET_ID) { type = NavType.IntType },
                navArgument(AppDestinations.ARG_ALIMENTO_ID) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val dietId = backStackEntry.arguments?.getInt(AppDestinations.ARG_DIET_ID)
            val foodId = backStackEntry.arguments?.getInt(AppDestinations.ARG_ALIMENTO_ID)

            if (dietId != null && foodId != null) {
                // Create the factory with the DAOs from AppNavHost
                val addFoodFactory = AddFoodToDietViewModelFactory(alimentoDao, itemDietaDao)
                val addFoodViewModel: AddFoodToDietViewModel = viewModel(factory = addFoodFactory)

                AddFoodToDietScreen(
                    viewModel = addFoodViewModel,
                    dietId = dietId,
                    foodId = foodId,
                    onNavigateBack = { navController.popBackStack() },
                    onFoodAdded = {
                        // Pop back to the Diet Detail screen
                        navController.popBackStack()
                    }
                )
            }
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