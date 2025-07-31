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
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.data.db.entity.ItemDieta
import com.mekki.taco.data.model.ItemDietaComAlimento
import com.mekki.taco.presentation.ui.diet.DietListViewModel
import kotlinx.coroutines.flow.firstOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodToNewDietScreen(
    foodId: Int,
    alimentoDao: AlimentoDao,
    dietListViewModel: DietListViewModel,
    onFoodAdded: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    val mealTypes = listOf("Café da Manhã", "Almoço", "Jantar", "Lanche")
    var selectedMealType by remember { mutableStateOf(mealTypes[0]) }
    var isMealDropdownExpanded by remember { mutableStateOf(false) }

    // State to hold the food details once loaded
    var alimento by remember { mutableStateOf<Alimento?>(null) }

    // Load the food details from the database
    LaunchedEffect(foodId) {
        alimento = alimentoDao.buscarAlimentoPorId(foodId).firstOrNull()
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
                Text(
                    text = food.nome,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Quantidade (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

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
                        modifier = Modifier.fillMaxWidth().menuAnchor()
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

                Button(
                    onClick = {
                        val quantityValue = quantity.toDoubleOrNull()
                        if (quantityValue != null && quantityValue > 0) {
                            // Create the temporary item and add it to the ViewModel's list
                            val tempItem = ItemDietaComAlimento(
                                itemDieta = ItemDieta(
                                    // Using a temporary negative ID to be a unique key in the lazy list
                                    id = -(System.currentTimeMillis().toInt()),
                                    dietaId = 0, // Not yet assigned
                                    alimentoId = food.id,
                                    quantidadeGramas = quantityValue,
                                    tipoRefeicao = selectedMealType
                                ),
                                alimento = food
                            )
                            dietListViewModel.addFoodToTemporaryList(tempItem)
                            onFoodAdded() // Navigate back
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0
                ) {
                    Text("Adicionar à Nova Dieta")
                }
            } ?: run {
                CircularProgressIndicator()
            }
        }
    }
}
