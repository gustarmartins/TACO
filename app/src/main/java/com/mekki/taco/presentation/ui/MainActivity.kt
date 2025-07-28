package com.mekki.taco

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // Correto
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// Imports do Room e DAO
import com.mekki.taco.data.db.dao.AlimentoDao // Import para AlimentoDao
import com.mekki.taco.data.db.database.AppDatabase
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.db.dao.ItemDietaDao
// Imports da UI e ViewModel
import com.mekki.taco.presentation.navigation.AppNavHost // Importe seu AppNavHost (verifique o pacote)
import com.mekki.taco.presentation.ui.search.AlimentoViewModelFactory // Ajuste o pacote se necessário (ex: com.mekki.taco.presentation.ui.search.AlimentoViewModelFactory)
// Imports do Navigation
import androidx.navigation.compose.rememberNavController
// Imports do Coroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    companion object { private const val TAG = "MainActivity_TACO" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Iniciando MainActivity.")

        enableEdgeToEdge()

        // 1. Inicializa o banco e obtém o DAO
        val appDatabase = AppDatabase.getDatabase(applicationContext, applicationScope)
        val alimentoDao: AlimentoDao = appDatabase.alimentoDao() // Explicitando o tipo para clareza
        val dietaDao = appDatabase.dietaDao()
        val itemDietaDao = appDatabase.itemDietaDao()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        alimentoDao = alimentoDao,
                        dietaDao = dietaDao,
                        itemDietaDao = itemDietaDao
                    )
                }
            }
        }
        Log.d(TAG, "onCreate: setContent com AppNavHost chamado.")
    }
}