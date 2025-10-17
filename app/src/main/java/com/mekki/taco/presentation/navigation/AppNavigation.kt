package com.mekki.taco.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.db.dao.ItemDietaDao
import com.mekki.taco.data.repository.UserProfileRepository
import com.mekki.taco.presentation.ui.addfood.AddFoodScreen
import com.mekki.taco.presentation.ui.addfood.AddFoodViewModel
import com.mekki.taco.presentation.ui.addfood.AddFoodViewModelFactory
import com.mekki.taco.presentation.ui.diet.CreateDietScreen
import com.mekki.taco.presentation.ui.diet.DietDetailScreen
import com.mekki.taco.presentation.ui.diet.DietDetailViewModel
import com.mekki.taco.presentation.ui.diet.DietDetailViewModelFactory
import com.mekki.taco.presentation.ui.diet.DietListScreen
import com.mekki.taco.presentation.ui.diet.DietListViewModel
import com.mekki.taco.presentation.ui.diet.DietListViewModelFactory
import com.mekki.taco.presentation.ui.fooddetail.AlimentoDetailScreen
import com.mekki.taco.presentation.ui.fooddetail.AlimentoDetailViewModel
import com.mekki.taco.presentation.ui.fooddetail.AlimentoDetailViewModelFactory
import com.mekki.taco.presentation.ui.home.HomeScreen
import com.mekki.taco.presentation.ui.home.HomeViewModel
import com.mekki.taco.presentation.ui.home.HomeViewModelFactory
import com.mekki.taco.presentation.ui.profile.ProfileViewModel
import com.mekki.taco.presentation.ui.profile.ProfileViewModelFactory
import com.mekki.taco.presentation.ui.search.AlimentoSearchScreen
import com.mekki.taco.presentation.ui.search.AlimentoViewModel
import com.mekki.taco.presentation.ui.search.AlimentoViewModelFactory

object AppDestinations {
    object Args {
        const val ALIMENTO_ID = "alimentoId"
        const val DIET_ID = "dietId"
    }

    const val HOME_ROUTE = "home"
    const val DIET_LIST_ROUTE = "diet_list"
    const val CREATE_DIET_ROUTE = "create_diet"
    const val ALIMENTO_DETAIL_ROUTE = "alimento_detail/{${Args.ALIMENTO_ID}}"
    const val DIET_DETAIL_ROUTE = "diet_detail/{${Args.DIET_ID}}"

    // query-like optional param style (use defaultValue = -1 in navArgument)
    const val ALIMENTO_SEARCH_ROUTE = "alimento_search?dietId={${Args.DIET_ID}}"
    const val ADD_FOOD_ROUTE = "add_food/{${Args.ALIMENTO_ID}}?dietId={${Args.DIET_ID}}"

    fun alimentoDetail(alimentoId: Int) = "alimento_detail/$alimentoId"
    fun dietDetail(dietId: Int) = "diet_detail/$dietId"
    fun addFood(alimentoId: Int, dietId: Int? = null) = "add_food/$alimentoId?dietId=${dietId ?: -1}"
    fun alimentoSearch(dietId: Int? = null) = "alimento_search?dietId=${dietId ?: -1}"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onTitleChange: (String) -> Unit,
    onFabChange: (@Composable (() -> Unit)?) -> Unit,
    alimentoDao: AlimentoDao,
    dietaDao: DietaDao,
    itemDietaDao: ItemDietaDao
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = AppDestinations.HOME_ROUTE
    ) {
        // HOME
        composable(route = AppDestinations.HOME_ROUTE) {
            val context = LocalContext.current
            val profileRepository = remember { UserProfileRepository(context) }
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(profileRepository))
            val homeFactory = HomeViewModelFactory(dietaDao, alimentoDao)
            val homeViewModel: HomeViewModel = viewModel(factory = homeFactory)

            HomeScreen(
                homeViewModel = homeViewModel,
                profileViewModel = profileViewModel,
                onNavigateToDietList = { navController.navigate(AppDestinations.DIET_LIST_ROUTE) },
                onNavigateToDiary = { Log.d("AppNavHost", "Navigate to Diary clicked - TODO") },
                onNavigateToDetail = { alimentoId ->
                    navController.navigate(AppDestinations.alimentoDetail(alimentoId))
                }
            )
        }

        // DIET LIST
        composable(route = AppDestinations.DIET_LIST_ROUTE) {
            val dietListFactory = DietListViewModelFactory(dietaDao, itemDietaDao)
            val dietListViewModel: DietListViewModel = viewModel(factory = dietListFactory)
            DietListScreen(
                viewModel = dietListViewModel,
                onNavigateToCreateDiet = { navController.navigate(AppDestinations.CREATE_DIET_ROUTE) },
                onNavigateToDietDetail = { dietId ->
                    navController.navigate(AppDestinations.dietDetail(dietId))
                },
                onFabChange = onFabChange
            )
        }

        // DIET DETAIL
        composable(
            route = AppDestinations.DIET_DETAIL_ROUTE,
            arguments = listOf(navArgument(AppDestinations.Args.DIET_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val dietId = backStackEntry.arguments?.getInt(AppDestinations.Args.DIET_ID)
            if (dietId != null) {
                val detailFactory = DietDetailViewModelFactory(dietId, dietaDao, itemDietaDao)
                val detailViewModel: DietDetailViewModel = viewModel(factory = detailFactory)
                DietDetailScreen(
                    viewModel = detailViewModel,
                    onNavigateToAddFood = {
                        navController.navigate(AppDestinations.alimentoSearch(dietId = dietId))
                    },
                    onTitleChange = onTitleChange,
                    onFabChange = onFabChange
                )
            }
        }

        // CREATE DIET
        composable(route = AppDestinations.CREATE_DIET_ROUTE) {
            val parentEntry = remember(it) { navController.getBackStackEntry(AppDestinations.DIET_LIST_ROUTE) }
            val dietListViewModel: DietListViewModel = viewModel(viewModelStoreOwner = parentEntry)
            CreateDietScreen(
                viewModel = dietListViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddFood = {
                    // open generic search (no diet preselected)
                    navController.navigate(AppDestinations.alimentoSearch())
                }
            )
        }

        // ALIMENTO SEARCH
        composable(
            route = AppDestinations.ALIMENTO_SEARCH_ROUTE,
            arguments = listOf(navArgument(AppDestinations.Args.DIET_ID) {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val dietId = backStackEntry.arguments?.getInt(AppDestinations.Args.DIET_ID)
            val searchViewModel: AlimentoViewModel = viewModel(factory = AlimentoViewModelFactory(alimentoDao))

            AlimentoSearchScreen(
                viewModel = searchViewModel,
                onAlimentoClick = { alimentoId ->
                    navController.navigate(AppDestinations.addFood(alimentoId = alimentoId, dietId = dietId?.takeIf { it != -1 }))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ADD FOOD
        composable(
            route = AppDestinations.ADD_FOOD_ROUTE,
            arguments = listOf(
                navArgument(AppDestinations.Args.ALIMENTO_ID) { type = NavType.IntType },
                navArgument(AppDestinations.Args.DIET_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getInt(AppDestinations.Args.ALIMENTO_ID)
            val dietId = backStackEntry.arguments?.getInt(AppDestinations.Args.DIET_ID)?.takeIf { it != -1 }

            if (foodId != null) {
                val addFoodFactory = AddFoodViewModelFactory(
                    foodId = foodId,
                    alimentoDao = alimentoDao,
                    dietaDao = dietaDao,
                    itemDietaDao = itemDietaDao,
                    initialDietId = dietId
                )
                val addFoodViewModel: AddFoodViewModel = viewModel(factory = addFoodFactory)
                AddFoodScreen(
                    viewModel = addFoodViewModel,
                    onFoodAdded = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = AppDestinations.ALIMENTO_DETAIL_ROUTE,
            arguments = listOf(navArgument(AppDestinations.Args.ALIMENTO_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val alimentoId = backStackEntry.arguments?.getInt(AppDestinations.Args.ALIMENTO_ID)
            if (alimentoId != null) {
                val detailFactory = AlimentoDetailViewModelFactory(alimentoId, alimentoDao)
                val detailViewModel: AlimentoDetailViewModel = viewModel(factory = detailFactory)
                val uiState by detailViewModel.uiState.collectAsState()

                AlimentoDetailScreen(
                    uiState = uiState,
                    onPortionChange = detailViewModel::updatePortion,
                    onNavigateBack = { navController.popBackStack() },
                    onTitleChange = onTitleChange
                )
            }
        }
    }
}