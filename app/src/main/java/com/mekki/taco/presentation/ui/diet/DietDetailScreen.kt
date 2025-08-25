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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.input.KeyboardType
import com.mekki.taco.data.db.entity.ItemDieta
import com.mekki.taco.data.model.ItemDietaComAlimento
import com.mekki.taco.presentation.ui.components.NutrientRow
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
                        FoodItemRow(
                            item = dietItem,
                            onUpdate = viewModel::updateItem,
                            onDelete = viewModel::deleteItem
                        )
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
            HorizontalDivider()
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
fun MealHeader(mealType: String) {
    Text(
        text = mealType,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun FoodItemRow(
    item: ItemDietaComAlimento,
    onUpdate: (ItemDieta) -> Unit,
    onDelete: (ItemDieta) -> Unit
) {
    val df = DecimalFormat("#.#")
    var isExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
        ) {
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
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onDelete(item.itemDieta) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Deletar")
                }
            }
        }
    }

    if (showEditDialog) {
        EditFoodItemDialog(
            item = item,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedItem ->
                onUpdate(updatedItem)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFoodItemDialog(
    item: ItemDietaComAlimento,
    onDismiss: () -> Unit,
    onConfirm: (ItemDieta) -> Unit
) {
    var quantity by remember { mutableStateOf(item.itemDieta.quantidadeGramas.toString()) }
    val mealTypes = listOf("Café da Manhã", "Almoço", "Jantar", "Lanche")
    var selectedMealType by remember { mutableStateOf(item.itemDieta.tipoRefeicao ?: mealTypes[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar ${item.alimento.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantidade (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Dropdown for meal type would go here
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedItem = item.itemDieta.copy(
                    quantidadeGramas = quantity.toDoubleOrNull() ?: item.itemDieta.quantidadeGramas,
                    tipoRefeicao = selectedMealType
                )
                onConfirm(updatedItem)
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
