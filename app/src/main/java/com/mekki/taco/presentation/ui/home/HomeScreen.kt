package com.mekki.taco.presentation.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.presentation.ui.components.MacroPieChart
import com.mekki.taco.presentation.ui.components.PieChartData
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDietList: () -> Unit,
    onNavigateToDiary: () -> Unit // only a placeholder for now
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("TACO Nutri App") }) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Quick Search Section ---
            item { QuickSearchCard(state = state, viewModel = viewModel) }

            // --- Main Diet Overview Section ---
            item { DietOverviewCard(state = state, onNavigateToDietList = onNavigateToDietList) }

            // --- Navigation Actions Section ---
            item { NavigationActionsCard(onNavigateToDietList, onNavigateToDiary) }
        }
    }
}

@Composable
fun QuickSearchCard(state: HomeState, viewModel: HomeViewModel) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = state.searchTerm,
                onValueChange = viewModel::onSearchTermChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar Alimento Rápido") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            // Search results list
            if (state.searchIsLoading) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                state.searchResults.forEach { alimento ->
                    SearchItem(
                        alimento = alimento,
                        isExpanded = state.expandedAlimentoId == alimento.id,
                        onToggle = { viewModel.onAlimentoToggled(alimento.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchItem(alimento: Alimento, isExpanded: Boolean, onToggle: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onToggle)) {
        Text(
            text = alimento.nome,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        )
        // The "balloon" that expands and collapses
        AnimatedVisibility(visible = isExpanded) {
            MacroInfoBubble(alimento = alimento)
        }
        Divider()
    }
}

@Composable
fun MacroInfoBubble(alimento: Alimento) {
    val df = DecimalFormat("#.#")
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Valores por 100g", style = MaterialTheme.typography.bodySmall)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MacroText("Carbs", alimento.carboidratos, "g", Color(0xFF4FC3F7))
                MacroText("Proteínas", alimento.proteina, "g", Color(0xFF81C784))
                MacroText("Gorduras", alimento.lipidios?.total, "g", Color(0xFFFFD54F))
            }
        }
    }
}

@Composable
fun DietOverviewCard(state: HomeState, onNavigateToDietList: () -> Unit) {
    val totals = state.dietTotals
    val pieData = listOf(
        PieChartData(totals.totalCarbs.toFloat(), Color(0xFF4FC3F7), "Carbs"),
        PieChartData(totals.totalProtein.toFloat(), Color(0xFF81C784), "Protein"),
        PieChartData(totals.totalFat.toFloat(), Color(0xFFFFD54F), "Fat")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onNavigateToDietList,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.primaryDiet?.dieta?.nome ?: "Nenhuma Dieta Principal",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state.primaryDiet != null && totals.totalKcal > 0) {
                MacroPieChart(
                    modifier = Modifier.fillMaxWidth(),
                    data = pieData,
                    totalValue = totals.totalKcal,
                    totalUnit = "kcal"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    pieData.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(10.dp)) { drawCircle(it.color) }
                            Text(it.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
            } else {
                Text("Crie uma dieta para ver o resumo aqui.", modifier = Modifier.padding(vertical = 24.dp))
            }
        }
    }
}

@Composable
fun NavigationActionsCard(
    onNavigateToDietList: () -> Unit,
    onNavigateToDiary: () -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Text(
                "Funcionalidades",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )
            Divider()
            NavigationActionRow(
                title = "Gerenciar Dietas",
                icon = Icons.Default.Book,
                onClick = onNavigateToDietList
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            NavigationActionRow(
                title = "Diário Alimentar",
                icon = Icons.Default.EditCalendar,
                onClick = onNavigateToDiary
            )
        }
    }
}

@Composable
fun NavigationActionRow(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}

// Reusable MacroText from previous version
@Composable
fun MacroText(label: String, value: Double?, unit: String, color: Color) {
    val df = DecimalFormat("#.#")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
        Text("${df.format(value ?: 0.0)}$unit", style = MaterialTheme.typography.bodyLarge)
    }
}