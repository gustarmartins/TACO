package com.mekki.taco

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.db.dao.ItemDietaDao
import com.mekki.taco.data.db.database.AppDatabase
import com.mekki.taco.data.repository.UserProfileRepository
import com.mekki.taco.presentation.navigation.AppNavHost
import com.mekki.taco.presentation.ui.profile.ProfileSheetContent
import com.mekki.taco.presentation.ui.profile.ProfileViewModel
import com.mekki.taco.presentation.ui.profile.ProfileViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    companion object { private const val TAG = "MainActivity_TACO" }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Iniciando MainActivity.")

        enableEdgeToEdge()

        val appDatabase = AppDatabase.getDatabase(applicationContext, applicationScope)
        val alimentoDao: AlimentoDao = appDatabase.alimentoDao()
        val dietaDao: DietaDao = appDatabase.dietaDao()
        val itemDietaDao: ItemDietaDao = appDatabase.itemDietaDao()

        setContent {
            MaterialTheme {
                // 1. INICIALIZAÇÃO DOS VIEWMODELS E ESTADOS
                val navController = rememberNavController()
                var fab: @Composable (() -> Unit)? by remember { mutableStateOf(null) }
                var screenTitle by rememberSaveable { mutableStateOf("NutriTACO") }

                // ViewModel e Estados da BottomSheet
                val context = LocalContext.current
                val profileRepository = remember { UserProfileRepository(context) }
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(profileRepository))
                val sheetState = rememberModalBottomSheetState()
                val scope = rememberCoroutineScope()
                var showBottomSheet by remember { mutableStateOf(false) }

                // 2. LÓGICA DA UI PERSISTENTE
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val canNavigateBack = navController.previousBackStackEntry != null

                DisposableEffect(navBackStackEntry) {
                    val defaultTitle = when {
                        currentRoute?.startsWith("alimento_detail") == true -> "Detalhes do Alimento"
                        currentRoute?.startsWith("diet_detail") == true -> "Detalhes da Dieta"
                        currentRoute == "diet_list" -> "Minhas Dietas"
                        currentRoute == "create_diet" -> "Criar Nova Dieta"
                        else -> "NutriTACO"
                    }
                    screenTitle = defaultTitle

                    onDispose{
                    }
                }

                // 3. SCAFFOLD PRINCIPAL
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = screenTitle) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            navigationIcon = {
                                if (canNavigateBack) {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Voltar"
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { showBottomSheet = true }) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Abrir Perfil"
                                    )
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        fab?.invoke()
                    }
                ) { innerPadding ->
                    // 4. CONTEÚDO DA NAVEGAÇÃO
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        onTitleChange = { newTitle -> screenTitle = newTitle },
                        onFabChange = { newFab -> fab = newFab },
                        alimentoDao = alimentoDao,
                        dietaDao = dietaDao,
                        itemDietaDao = itemDietaDao
                    )
                }

                // 5. ABA DE PERFIL
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        ProfileSheetContent(
                            viewModel = profileViewModel,
                            onDismiss = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        Log.d(TAG, "onCreate: setContent com AppNavHost chamado.")
    }
}