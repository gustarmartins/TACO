package com.mekki.taco.presentation.ui.fooddetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.data.db.entity.Aminoacidos
import com.mekki.taco.data.db.entity.Lipidios

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlimentoDetailScreen(
    uiState: AlimentoDetailUiState,
    onPortionChange: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.displayAlimento?.nome ?: "Carregando...", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading || uiState.displayAlimento == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val alimento = uiState.displayAlimento
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item { PrimaryInfoCard(alimento.energiaKcal, uiState.portion, onPortionChange) }
                item { MacronutrientsCard(alimento.proteina, alimento.carboidratos, alimento.lipidios) }
                item { GeneralInfoCard(alimento.fibraAlimentar, alimento.colesterol, alimento.cinzas, alimento.umidade) }
                item { MineralsCard(alimento) }
                item { VitaminsCard(alimento) }
                item { AminoAcidsCard(alimento.aminoacidos) }
            }
        }
    }
}

// --- CARDS ---

@Composable
fun PrimaryInfoCard(calorias: Double?, portion: String, onPortionChange: (String) -> Unit) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoColumn(calorias?.toInt()?.toString() ?: "N/A", "kcal", "Calorias")
            EditablePortion(portion, onPortionChange)
        }
    }
}

// ======================= MODIFIED COMPOSABLE START =======================
@Composable
fun MacronutrientsCard(proteinas: Double?, carboidratos: Double?, lipidios: Lipidios?) {
    var isFatDetailExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (isFatDetailExpanded) 180f else 0f, label = "rotation")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Macronutrientes",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            // This Row is now always visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top // Align to top for better layout
            ) {
                MacroStat("Proteínas", proteinas ?: 0.0, Icons.Default.FitnessCenter, Modifier.weight(1f))
                MacroStat("Carbs", carboidratos ?: 0.0, Icons.Default.LocalFireDepartment, Modifier.weight(1f))
                MacroStat(
                    label = "Gorduras",
                    amount = lipidios?.total ?: 0.0,
                    icon = Icons.Default.OilBarrel,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isFatDetailExpanded = !isFatDetailExpanded },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expandir detalhes de gordura",
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                )
            }

            // Animated visibility for fat details only
            AnimatedVisibility(visible = isFatDetailExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider()
                    DetailRow("Saturadas", lipidios?.saturados, "g")
                    DetailRow("Monoinsaturadas", lipidios?.monoinsaturados, "g")
                    DetailRow("Poliinsaturadas", lipidios?.poliinsaturados, "g")
                }
            }
        }
    }
}
// ======================= MODIFIED COMPOSABLE END =======================

@Composable
fun GeneralInfoCard(fibra: Double?, colesterol: Double?, cinzas: Double?, umidade: Double?) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Informações Gerais", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
            DetailRow("Fibra Alimentar", fibra, "g")
            DetailRow("Colesterol", colesterol, "mg")
            DetailRow("Cinzas", cinzas, "g")
            DetailRow("Umidade", umidade, "%")
        }
    }
}

@Composable
fun MineralsCard(alimento: Alimento) {
    ExpandableCard(title = "Minerais") {
        DetailRow("Cálcio", alimento.calcio, "mg")
        DetailRow("Magnésio", alimento.magnesio, "mg")
        DetailRow("Manganês", alimento.manganes, "mg")
        DetailRow("Fósforo", alimento.fosforo, "mg")
        DetailRow("Ferro", alimento.ferro, "mg")
        DetailRow("Sódio", alimento.sodio, "mg")
        DetailRow("Potássio", alimento.potassio, "mg")
        DetailRow("Cobre", alimento.cobre, "mg")
        DetailRow("Zinco", alimento.zinco, "mg")
    }
}

@Composable
fun VitaminsCard(alimento: Alimento) {
    ExpandableCard(title = "Vitaminas") {
        DetailRow("Retinol", alimento.retinol, "µg")
        DetailRow("RE", alimento.RE, "µg")
        DetailRow("RAE", alimento.RAE, "µg")
        DetailRow("Tiamina (B1)", alimento.tiamina, "mg")
        DetailRow("Riboflavina (B2)", alimento.riboflavina, "mg")
        DetailRow("Piridoxina (B6)", alimento.piridoxina, "mg")
        DetailRow("Niacina (B3)", alimento.niacina, "mg")
        DetailRow("Vitamina C", alimento.vitaminaC, "mg")
    }
}

@Composable
fun AminoAcidsCard(aminoacidos: Aminoacidos?) {
    if (aminoacidos == null) return
    ExpandableCard(title = "Aminoácidos") {
        DetailRow("Triptofano", aminoacidos.triptofano, "mg")
        DetailRow("Treonina", aminoacidos.treonina, "mg")
        DetailRow("Isoleucina", aminoacidos.isoleucina, "mg")
        DetailRow("Leucina", aminoacidos.leucina, "mg")
        DetailRow("Lisina", aminoacidos.lisina, "mg")
        DetailRow("Metionina", aminoacidos.metionina, "mg")
        DetailRow("Cistina", aminoacidos.cistina, "mg")
        DetailRow("Fenilalanina", aminoacidos.fenilalanina, "mg")
        DetailRow("Tirosina", aminoacidos.tirosina, "mg")
        DetailRow("Valina", aminoacidos.valina, "mg")
        DetailRow("Arginina", aminoacidos.arginina, "mg")
        DetailRow("Histidina", aminoacidos.histidina, "mg")
        DetailRow("Alanina", aminoacidos.alanina, "mg")
        DetailRow("Ácido Aspártico", aminoacidos.acidoAspartico, "mg")
        DetailRow("Ácido Glutâmico", aminoacidos.acidoGlutamico, "mg")
        DetailRow("Glicina", aminoacidos.glicina, "mg")
        DetailRow("Prolina", aminoacidos.prolina, "mg")
        DetailRow("Serina", aminoacidos.serina, "mg")
    }
}

// --- HELPER COMPOSABLES ---

@Composable
fun EditablePortion(portion: String, onPortionChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = portion,
            onValueChange = onPortionChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.width(120.dp)
        )
        Text("Porção (g)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ExpandableCard(
    title: String,
    content: @Composable () -> Unit = {},
    expandedContent: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(if (isExpanded) 180f else 0f, label = "rotation")

    Card(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, "Expandir", Modifier.rotate(rotationAngle))
            }
            if (content != {}) {
                Spacer(Modifier.height(12.dp))
                content()
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Divider()
                    Spacer(Modifier.height(8.dp))
                    expandedContent()
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: Double?, unit: String) {
    if (value == null || value == 0.0) return
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "${String.format("%.2f", value)}$unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoColumn(value: String, unit: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        if (unit.isNotBlank()) Text(unit, style = MaterialTheme.typography.bodySmall)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ======================= MODIFIED COMPOSABLE START =======================
@Composable
fun MacroStat(
    label: String,
    amount: Double,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null // Optional icon for the label
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Icon(icon, label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text("${String.format("%.1f", amount)}g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            // Render the trailing icon next to the label if it's provided
            trailingIcon?.invoke()
        }
    }
}
// ======================= MODIFIED COMPOSABLE END =======================