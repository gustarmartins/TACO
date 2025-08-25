package com.mekki.taco.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheetContent(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val df = remember { DecimalFormat("#") }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Meu Perfil", style = MaterialTheme.typography.headlineSmall) }

        // --- Entradas de Dados ---
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.userProfile.weight?.toString() ?: "",
                    onValueChange = viewModel::onWeightChange,
                    label = { Text("Peso (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.userProfile.height?.toString() ?: "",
                    onValueChange = viewModel::onHeightChange,
                    label = { Text("Altura (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.userProfile.age?.toString() ?: "",
                    onValueChange = viewModel::onAgeChange,
                    label = { Text("Idade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Seletor de Sexo
        item { SexSelector(selectedSex = uiState.userProfile.sex, onSexSelected = viewModel::onSexChange) }

        // Nível de Atividade
        item { ActivityLevelSelector(selectedLevel = uiState.activityLevel, onLevelSelected = viewModel::onActivityLevelChange) }

        // Resultados Calculados
        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
        item {
            Text("Estimativas Diárias", style = MaterialTheme.typography.titleLarge)
            Text("Taxa Metabólica Basal (TMB): ${df.format(uiState.tmb)} kcal")
            Text("Gasto Calórico Total (GET): ${df.format(uiState.tdee)} kcal", style = MaterialTheme.typography.titleMedium)
        }

        // Metas
        // TODO: Adicionar campos para metas de proteína, etc. em breve OwO

        item {
            Button(
                onClick = {
                    viewModel.saveProfile()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }
        }
    }
}

// Composable para o seletor de sexo
@Composable
fun SexSelector(selectedSex: String?, onSexSelected: (String) -> Unit) {
    val options = listOf("Masculino", "Feminino")
    Column {
        Text("Sexo", style = MaterialTheme.typography.bodyLarge)
        Row(Modifier.selectableGroup()) {
            options.forEach { text ->
                Row(
                    Modifier
                        .weight(1f)
                        .selectable(
                            selected = (text == selectedSex),
                            onClick = { onSexSelected(text) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == selectedSex),
                        onClick = null // A ação de clique é controlada pelo Row
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLevelSelector(
    selectedLevel: ActivityLevel,
    onLevelSelected: (ActivityLevel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedLevel.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Nível de Atividade Física") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ActivityLevel.values().forEach { level ->
                DropdownMenuItem(
                    text = { Text(level.displayName) },
                    onClick = {
                        onLevelSelected(level)
                        expanded = false
                    }
                )
            }
        }
    }
}
