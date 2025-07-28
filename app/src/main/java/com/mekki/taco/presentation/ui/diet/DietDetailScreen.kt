// File: app/src/main/java/com/mekki/taco/presentation/ui/diet/DietDetailScreen.kt

package com.mekki.taco.presentation.ui.diet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mekki.taco.data.model.ItemDietaComAlimento
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietDetailScreen(
    viewModel: DietDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddFood: () -> Unit
) {
    val dietDetails by viewModel.dietDetails.collectAsState()
    val groupedItems by viewModel.groupedItems.collectAsState()
    val totals by viewModel.dietTotals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dietDetails?.dieta?.nome ?: "Carregando Dieta...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddFood) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Alimento")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card with Totals
            item {
                TotalsCard(totals = totals, goal = dietDetails?.dieta?.objetivoCalorias)
            }

            // Display food items grouped by meal
            if (groupedItems.isEmpty() && dietDetails != null) {
                item {
                    Text(
                        text = "Esta dieta ainda não tem alimentos. Toque no botão '+' para começar a adicionar.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                groupedItems.forEach { (mealType, items) ->
                    item {
                        MealHeader(mealType)
                    }
                    items(items, key = { it.itemDieta.id }) { dietItem ->
                        FoodItemRow(dietItem)
                    }
                }
            }
        }
    }
}

@Composable
fun TotalsCard(totals: DietTotals, goal: Double?) {
    val df = DecimalFormat("#.#")
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo Nutricional", style = MaterialTheme.typography.titleLarge)
            Divider()
            // Calories Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Calorias", fontWeight = FontWeight.Bold)
                Text(
                    "${df.format(totals.totalKcal)} kcal" + (goal?.let { " / ${df.format(it)} kcal" } ?: ""),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            // Macronutrient Rows
            NutrientRow("Proteínas", totals.totalProtein, "g")
            NutrientRow("Carboidratos", totals.totalCarbs, "g")
            NutrientRow("Gorduras", totals.totalFat, "g")
        }
    }
}

@Composable
fun NutrientRow(label: String, value: Double, unit: String) {
    val df = DecimalFormat("#.#")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text("${df.format(value)} $unit")
    }
}

@Composable
fun MealHeader(mealType: String) {
    Text(
        text = mealType,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun FoodItemRow(item: ItemDietaComAlimento) {
    val df = DecimalFormat("#.#")
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.alimento.nome, fontWeight = FontWeight.Bold)
                Text("${df.format(item.itemDieta.quantidadeGramas)} g", fontSize = 14.sp)
            }
            // add calculated calories per item here soon
        }
    }
}
