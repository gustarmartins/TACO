package com.mekki.taco.presentation.ui.diet

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mekki.taco.data.model.ItemDietaComAlimento
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDietScreen(
    viewModel: DietListViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddFood: () -> Unit // New navigation lambda
) {
    val nomeDieta by viewModel.nomeNovaDieta.collectAsState()
    // The temporary list of foods for the new diet
    val temporaryFoodList by viewModel.temporaryFoodList.collectAsState()

    // This effect handles navigating back AFTER the diet and its items are saved.
    LaunchedEffect(key1 = Unit) {
        viewModel.dietaSalvaEvent.collect {
            Log.d("CreateDietScreen", "Event received, navigating back.")
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Nova Dieta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Diet Name Input
            OutlinedTextField(
                value = nomeDieta,
                onValueChange = { viewModel.onNomeNovaDietaChange(it) },
                label = { Text("Nome da Dieta*") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            // "Add Food" Button
            Button(
                onClick = onNavigateToAddFood,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Food Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Adicionar Alimento")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of foods added so far
            Text("Alimentos Adicionados:", style = MaterialTheme.typography.titleMedium)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (temporaryFoodList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum alimento adicionado ainda.", textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(temporaryFoodList, key = { it.itemDieta.id }) { foodItem ->
                        AddedFoodItemRow(
                            item = foodItem,
                            onRemove = {
                                // Add a function to the ViewModel to handle removal
                                // viewModel.removeTemporaryFoodItem(foodItem)
                            }
                        )
                    }
                }
            }


            // Save Button at the bottom
            Button(
                onClick = { viewModel.salvarNovaDieta() }, // This will now save the diet AND the list
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = nomeDieta.isNotBlank() && temporaryFoodList.isNotEmpty()
            ) {
                Text("Salvar Dieta e Alimentos")
            }
        }
    }
}

@Composable
fun AddedFoodItemRow(item: ItemDietaComAlimento, onRemove: () -> Unit) {
    val df = DecimalFormat("#.#")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Corrected line
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.alimento.nome, fontWeight = FontWeight.Bold)
            Text("${df.format(item.itemDieta.quantidadeGramas)} g", fontSize = 14.sp)
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remover Alimento", tint = MaterialTheme.colorScheme.error)
        }
    }
}