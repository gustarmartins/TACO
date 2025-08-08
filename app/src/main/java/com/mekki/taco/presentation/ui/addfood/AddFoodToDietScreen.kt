package com.mekki.taco.presentation.ui.addfood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mekki.taco.utils.NutrientCalculator
import com.mekki.taco.presentation.ui.components.NutrientRow
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodToDietScreen(
    viewModel: AddFoodToDietViewModel,
    dietId: Int,
    foodId: Int,
    onNavigateBack: () -> Unit,
    onFoodAdded: () -> Unit
) {
    // State for the quantity input field
    var quantity by remember { mutableStateOf("") }
    var calculatedCalories by remember { mutableStateOf(0.0) }
    var calculatedProtein by remember { mutableStateOf(0.0) }
    var calculatedCarbs by remember { mutableStateOf(0.0) }
    var calculatedFat by remember { mutableStateOf(0.0) }
    val decimalFormat = remember { DecimalFormat("#.##") }

    // Meal type selection state. These should match what you expect in your database.
    val mealTypes = listOf("Café da Manhã", "Almoço", "Jantar", "Lanche")
    var selectedMealType by remember { mutableStateOf(mealTypes[0]) }
    var isMealDropdownExpanded by remember { mutableStateOf(false) }

    // Fetch the food details when the screen is first composed
    LaunchedEffect(foodId) {
        viewModel.loadAlimento(foodId)
    }

    val alimento by viewModel.alimento.collectAsState()

    // Recalculate calories in real-time whenever quantity or food data changes
    LaunchedEffect(quantity, alimento) {
        val food = alimento
        val quant = quantity.toDoubleOrNull()
        if (food != null && quant != null) {
            val nutrients = NutrientCalculator.calcularNutrientesParaPorcao(food, quant)
            calculatedCalories = nutrients.energiaKcal ?: 0.0
            calculatedProtein = nutrients.proteina ?: 0.0
            calculatedCarbs = nutrients.carboidratos ?: 0.0
            calculatedFat = nutrients.lipidios?.total ?: 0.0
        } else {
            calculatedCalories = 0.0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Alimento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            alimento?.let { food ->
                // Card showing details of the food being added
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = food.nome,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Valores por 100g:", style = MaterialTheme.typography.titleMedium)
                        Text("Calorias: ${food.energiaKcal ?: 0.0} kcal")
                        Text("Proteínas: ${food.proteina ?: 0.0} g")
                        Text("Carboidratos: ${food.carboidratos ?: 0.0} g")
                    }
                }

                // Input field for the quantity
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        // Allow only numbers and a single decimal point
                        quantity = it.filter { char -> char.isDigit() || char == '.' }
                    },
                    label = { Text("Quantidade (em gramas)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text("g") }
                )

                // Display nutrients based on the input quantity
                if (quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0) {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Nutrientes na Porção:",
                                style = MaterialTheme.typography.titleMedium
                            )
                            NutrientRow("Calorias", calculatedCalories, "kcal")
                            NutrientRow("Proteínas", calculatedProtein, "g")
                            NutrientRow("Carboidratos", calculatedCarbs, "g")
                            NutrientRow("Gorduras", calculatedFat, "g")
                        }
                    }
                }

                // Dropdown menu to select the meal type
                ExposedDropdownMenuBox(
                    expanded = isMealDropdownExpanded,
                    onExpandedChange = { isMealDropdownExpanded = !isMealDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedMealType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Refeição") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMealDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor() // This is important for the dropdown to anchor correctly
                    )
                    ExposedDropdownMenu(
                        expanded = isMealDropdownExpanded,
                        onDismissRequest = { isMealDropdownExpanded = false }
                    ) {
                        mealTypes.forEach { meal ->
                            DropdownMenuItem(
                                text = { Text(meal) },
                                onClick = {
                                    selectedMealType = meal
                                    isMealDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Button to confirm and add the food to the diet
                Button(
                    onClick = {
                        val quantityValue = quantity.toDoubleOrNull()
                        if (quantityValue != null && quantityValue > 0) {
                            viewModel.addFoodToDiet(
                                dietId,
                                foodId,
                                quantityValue,
                                selectedMealType
                            ) {
                                onFoodAdded() // Navigate back after adding
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    // The button is only enabled when a valid quantity is entered
                    enabled = quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0
                ) {
                    Text("Adicionar à Dieta")
                }
            } ?: run {
                // Show a loading indicator while food details are being fetched
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
