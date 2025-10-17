package com.mekki.taco.presentation.ui.addfood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Dieta
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.presentation.ui.components.NutrientRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    viewModel: AddFoodViewModel,
    onFoodAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val alimento = uiState.alimento
    val calculatedNutrients = uiState.calculatedNutrients
    val selectedDiet = uiState.selectedDiet

    var showNewDietDialog by remember { mutableStateOf(false) }

    // Meal type selection state
    val mealTypes = listOf("Café da Manhã", "Almoço", "Jantar", "Lanche")
    var selectedMealType by remember { mutableStateOf(mealTypes[0]) }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (alimento == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Alimento não encontrado.")
        }
        return
    }

    // New Diet Dialog
    if (showNewDietDialog) {
        var newDietName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewDietDialog = false },
            title = { Text("Criar Nova Dieta") },
            text = {
                OutlinedTextField(
                    value = newDietName,
                    onValueChange = { newDietName = it },
                    label = { Text("Nome da Dieta") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addItemToNewDiet(newDietName, selectedMealType)
                        showNewDietDialog = false
                        onFoodAdded()
                    },
                    enabled = newDietName.isNotBlank()
                ) {
                    Text("Criar e Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewDietDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Visually appealing food header
        FoodHeaderCard(alimento = alimento)

        // 2. Portion Selector
        OutlinedTextField(
            value = uiState.portion,
            onValueChange = viewModel::onPortionChange,
            label = { Text("Quantidade") },
            suffix = { Text("g") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Real-time nutrient preview
        if (calculatedNutrients != null) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Nutrientes na Porção", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // If nutrient is null, show 0.0 to satisfy NutrientRow signature.
                    NutrientRow("Calorias", calculatedNutrients.energiaKcal ?: 0.0, "kcal")
                    NutrientRow("Proteínas", calculatedNutrients.proteina ?: 0.0, "g")
                    NutrientRow("Carboidratos", calculatedNutrients.carboidratos ?: 0.0, "g")
                    NutrientRow("Gorduras", calculatedNutrients.lipidios?.total ?: 0.0, "g")
                }
            }
        }

        // 4. Unified Diet Selector (driven by ViewModel selectedDiet)
        var dietDropdownExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = dietDropdownExpanded,
            onExpandedChange = { dietDropdownExpanded = !dietDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedDiet?.nome ?: "Selecione uma Dieta",
                onValueChange = {},
                readOnly = true,
                label = { Text("Adicionar a") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dietDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = dietDropdownExpanded,
                onDismissRequest = { dietDropdownExpanded = false }
            ) {
                uiState.allDiets.forEach { diet ->
                    DropdownMenuItem(
                        text = { Text(diet.nome) },
                        onClick = {
                            viewModel.selectDiet(diet.id)
                            dietDropdownExpanded = false
                        }
                    )
                }
                Divider()
                DropdownMenuItem(
                    text = { Text("Criar Nova Dieta...") },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = {
                        showNewDietDialog = true
                        dietDropdownExpanded = false
                    }
                )
            }
        }

        // Add to Diet Button
        Button(
            onClick = {
                uiState.selectedDiet?.let {
                    viewModel.addItemToExistingDiet(it.id, selectedMealType)
                    onFoodAdded()
                }
            },
            enabled = uiState.selectedDiet != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Adicionar à Dieta")
        }
    }
}

@Composable
fun FoodHeaderCard(alimento: Alimento) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.RestaurantMenu,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = alimento.nome,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
