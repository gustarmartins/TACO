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
import com.mekki.taco.presentation.ui.addfood.AddFoodToNewDietScreen

// Definição das rotas para evitar strings mágicas
object AppDestinations {
    const val HOME_ROUTE = "home"
    // Detalhes dos Alimentos
    const val ALIMENTO_DETAIL_BASE_ROUTE = "alimento_detail"
    const val ALIMENTO_DETAIL_WITH_ARG_ROUTE = "$ALIMENTO_DETAIL_BASE_ROUTE/{alimentoId}"
    const val ARG_ALIMENTO_ID = "alimentoId"
    // Funcionalidade de Dietas
    const val DIET_LIST_ROUTE = "diet_list"
    const val CREATE_DIET_ROUTE = "create_diet"
    const val DIET_DETAIL_BASE_ROUTE = "diet_item_detail"
    const val DIET_DETAIL_WITH_ARG_ROUTE = "$DIET_DETAIL_BASE_ROUTE/{dietId}"
    const val ARG_DIET_ID = "dietId"
    const val ALIMENTO_SEARCH_BASE_ROUTE = "alimento_search"
    const val ALIMENTO_SEARCH_WITH_ARG_ROUTE = "$ALIMENTO_SEARCH_BASE_ROUTE/{$ARG_DIET_ID}"
    // Adicionar um alimento a uma dieta
    const val ADD_FOOD_TO_DIET_BASE_ROUTE = "add_food_to_diet"
    const val ADD_FOOD_TO_DIET_WITH_ARGS_ROUTE = "$ADD_FOOD_TO_DIET_BASE_ROUTE/{$ARG_DIET_ID}/{$ARG_ALIMENTO_ID}"
    const val ALIMENTO_SEARCH_FOR_NEW_DIET_ROUTE = "alimento_search_for_new_diet"
    const val ADD_FOOD_TO_NEW_DIET_ROUTE = "add_food_to_new_diet/{$ARG_ALIMENTO_ID}"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    alimentoDao: AlimentoDao,
    dietaDao: DietaDao,
    itemDietaDao: ItemDietaDao
) {
    NavHost(navController = navController, startDestination = AppDestinations.HOME_ROUTE) {

        composable(route = AppDestinations.HOME_ROUTE) {
            val homeFactory = HomeViewModelFactory(dietaDao, alimentoDao)
            val homeViewModel: HomeViewModel = viewModel(factory = homeFactory)
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToDietList = {
                    navController.navigate(AppDestinations.DIET_LIST_ROUTE)
                },
                onNavigateToDiary = {
                    Log.d("AppNavHost", "Navigate to Diary clicked - TODO")
                },
                onNavigateToDetail = { alimentoId ->
                    navController.navigate("${AppDestinations.ALIMENTO_DETAIL_BASE_ROUTE}/$alimentoId")
                }
            )
        }

        composable(
            route = AppDestinations.ALIMENTO_DETAIL_WITH_ARG_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_ALIMENTO_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val alimentoId = backStackEntry.arguments?.getInt(AppDestinations.ARG_ALIMENTO_ID)
            if (alimentoId != null) {
                val detailViewModelFactory = AlimentoDetailViewModelFactory(alimentoId, alimentoDao)
                val alimentoDetailViewModel: AlimentoDetailViewModel = viewModel(factory = detailViewModelFactory)
                AlimentoDetailScreen(
                    viewModel = alimentoDetailViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else { Text("ID do alimento inválido.") }
        }

        composable(route = AppDestinations.DIET_LIST_ROUTE) {
            val dietListFactory = DietListViewModelFactory(dietaDao, itemDietaDao)
            val dietListViewModel: DietListViewModel = viewModel(factory = dietListFactory)
            DietListScreen(
                viewModel = dietListViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateDiet = {
                    navController.navigate(AppDestinations.CREATE_DIET_ROUTE)
                },
                onNavigateToDietDetail = { dietId ->
                    navController.navigate("${AppDestinations.DIET_DETAIL_BASE_ROUTE}/$dietId")
                }
            )
        }

        composable(route = AppDestinations.CREATE_DIET_ROUTE) {
            val parentEntry = remember(it) { navController.getBackStackEntry(AppDestinations.DIET_LIST_ROUTE) }
            val dietListViewModel: DietListViewModel = viewModel(viewModelStoreOwner = parentEntry)
            CreateDietScreen(
                viewModel = dietListViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddFood = { navController.navigate(AppDestinations.ALIMENTO_SEARCH_FOR_NEW_DIET_ROUTE) }
            )
        }

        composable(route = AppDestinations.ALIMENTO_SEARCH_FOR_NEW_DIET_ROUTE) {
            val searchFactory = AlimentoViewModelFactory(alimentoDao)
            val searchViewModel: AlimentoViewModel = viewModel(factory = searchFactory)
            AlimentoSearchScreen(
                viewModel = searchViewModel,
                onAlimentoClick = { alimentoId ->
                    // Navigates to the screen that adds to the temporary list
                    navController.navigate("add_food_to_new_diet/$alimentoId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Screen to add a food item to the temporary list in the ViewModel
        composable(
            route = AppDestinations.ADD_FOOD_TO_NEW_DIET_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_ALIMENTO_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getInt(AppDestinations.ARG_ALIMENTO_ID)
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(AppDestinations.DIET_LIST_ROUTE) }
            val dietListViewModel: DietListViewModel = viewModel(viewModelStoreOwner = parentEntry)

            if (foodId != null) {
                AddFoodToNewDietScreen(
                    foodId = foodId,
                    alimentoDao = alimentoDao,
                    dietListViewModel = dietListViewModel,
                    onFoodAdded = {
                        // Pop back twice to return to the CreateDietScreen
                        navController.popBackStack(AppDestinations.ALIMENTO_SEARCH_FOR_NEW_DIET_ROUTE, true)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppDestinations.DIET_DETAIL_WITH_ARG_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_DIET_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val dietId = backStackEntry.arguments?.getInt(AppDestinations.ARG_DIET_ID)
            if (dietId != null) {
                val detailFactory = DietDetailViewModelFactory(dietId, dietaDao, itemDietaDao)
                val detailViewModel: DietDetailViewModel = viewModel(factory = detailFactory)
                DietDetailScreen(
                    viewModel = detailViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddFood = {
                        navController.navigate("${AppDestinations.ALIMENTO_SEARCH_BASE_ROUTE}/$dietId")
                    }
                )
            }
        }

        composable(
            route = AppDestinations.ALIMENTO_SEARCH_WITH_ARG_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_DIET_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val dietId = backStackEntry.arguments?.getInt(AppDestinations.ARG_DIET_ID)
            if (dietId != null) {
                val searchViewModelFactory = AlimentoViewModelFactory(alimentoDao)
                val searchViewModel: AlimentoViewModel = viewModel(factory = searchViewModelFactory)

                AlimentoSearchScreen(
                    viewModel = searchViewModel,
                    onAlimentoClick = { alimentoId ->
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
                val addFoodFactory = AddFoodToDietViewModelFactory(alimentoDao, itemDietaDao)
                val addFoodViewModel: AddFoodToDietViewModel = viewModel(factory = addFoodFactory)

                AddFoodToDietScreen(
                    viewModel = addFoodViewModel,
                    dietId = dietId,
                    foodId = foodId,
                    onNavigateBack = { navController.popBackStack() },
                    onFoodAdded = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(screenName: String, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenName) },
                navigationIcon = {
                    onNavigateBack?.let { navBack ->
                        IconButton(onClick = navBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("(Em construção)", style = MaterialTheme.typography.bodyLarge)
        }
    }
}