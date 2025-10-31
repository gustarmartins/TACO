package com.mekki.taco.presentation.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.presentation.ui.components.MacroPieChart
import com.mekki.taco.presentation.ui.components.PieChartData
import com.mekki.taco.presentation.ui.profile.ProfileSheetContent
import com.mekki.taco.presentation.ui.profile.ProfileViewModel
import com.mekki.taco.utils.NutrientCalculator
import kotlinx.coroutines.launch
import java.text.DecimalFormat

// Palette
private val COLOR_TEXT = Color(0xFF222222)
private val COLOR_CARBS = Color(0xFFDCC48E)
private val COLOR_PROTEIN = Color(0xFF2E7A7A)
private val COLOR_FAT = Color(0xFFC97C4A)
private val COLOR_KCAL = Color(0xFFA83C3C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    profileViewModel: ProfileViewModel,
    onNavigateToDietList: () -> Unit,
    onNavigateToDiary: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val state by homeViewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // rola apenas se o conteúdo transbordar
            .padding(16.dp)
    ) {

        QuickSearchCard(
            state = state,
            viewModel = homeViewModel,
            onNavigateToDetail = onNavigateToDetail
        )
        Spacer(modifier = Modifier.height(24.dp))
        DietOverviewCard(state = state, onNavigateToDietList = onNavigateToDietList)
        Spacer(modifier = Modifier.height(24.dp))
        NavigationActionsCard(onNavigateToDietList, onNavigateToDiary)

        // Este Spacer flexível empurra o conteúdo para cima em telas altas,
        // ocupando o espaço vazio e prevenindo a rolagem desnecessária;
        // Mas aparentemente não está funcionando...
        Spacer(Modifier.weight(1f))
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            ProfileSheetContent(
                viewModel = profileViewModel,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun QuickSearchCard(
    state: HomeState,
    viewModel: HomeViewModel,
    onNavigateToDetail: (Int) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Card(
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = state.searchTerm,
                onValueChange = viewModel::onSearchTermChange,
                modifier = Modifier
                    .fillMaxWidth(),
                label = { Text("Buscar um alimento") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                singleLine = true,
                trailingIcon = {
                    if (state.searchTerm.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.cleanSearch()
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar Busca")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                })
            )

            Spacer(Modifier.height(12.dp))

            when {
                state.searchIsLoading -> {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.searchTerm.isEmpty() -> {
                    Column(Modifier.padding(8.dp)) {
                        Text("Escreva o nome de algum alimento da Tabela TACO para buscar informações.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                state.searchResults.isEmpty() -> {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(8.dp))
                        Text("Nenhum resultado encontrado para '${state.searchTerm}'.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                else -> {
                    Column {
                        state.searchResults.forEach { alimento ->
                            SearchItem(
                                alimento = alimento,
                                isExpanded = state.expandedAlimentoId == alimento.id,
                                onToggle = {
                                    viewModel.onAlimentoToggled(alimento.id)
                                    keyboardController?.hide()
                                },
                                onNavigateToDetail = onNavigateToDetail,
                                currentAmount = state.quickAddAmount,
                                onAmountChange = viewModel::onQuickAddAmountChange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchItem(
    alimento: Alimento,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    currentAmount: String,
    onAmountChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle, role = Role.Button)
            .padding(vertical = 4.dp)
            .animateContentSize()
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alimento.nome,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = COLOR_TEXT
                )
                if (!isExpanded) {
                    val subtitle = alimento.subtitleShort()
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (isExpanded) "Recolher" else "Expandir",
                tint = MaterialTheme.colorScheme.outline
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            MacroInfoBubble(
                alimento = alimento,
                currentAmount = currentAmount,
                onAmountChange = onAmountChange,
                onNavigateToDetail = { onNavigateToDetail(alimento.id) }
            )
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun MacroInfoBubble(
    alimento: Alimento,
    currentAmount: String,
    onAmountChange: (String) -> Unit,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isInEditMode by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Converte o valor do texto para Double, tratando casos de erro
    val amountDouble = currentAmount.toDoubleOrNull() ?: 0.0

    // Recalcula os nutrientes apenas quando o alimento ou a quantidade mudam
    val calculatedNutrients = remember(alimento, amountDouble) {
        NutrientCalculator.calcularNutrientesParaPorcao(alimento, amountDouble)
    }

    // Foca o TextField automaticamente quando entra no modo de edição
    LaunchedEffect(isInEditMode) {
        if (isInEditMode) {
            focusRequester.requestFocus()
        }
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
            .clickable(onClick = onNavigateToDetail),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isInEditMode) {
                    OutlinedTextField(
                        value = currentAmount,
                        onValueChange = onAmountChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        label = { Text("Quantidade (g)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            isInEditMode = false
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }),
                        singleLine = true
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isInEditMode = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Valores por ${currentAmount}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar quantidade",
                            modifier = Modifier.size(16.dp).padding(start = 4.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(alimento.categoria, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MacroText("Calorias", calculatedNutrients.energiaKcal, "kcal", COLOR_KCAL)
                MacroText("Carbs", calculatedNutrients.carboidratos, "g", COLOR_CARBS)
                MacroText("Proteínas", calculatedNutrients.proteina, "g", COLOR_PROTEIN)
                MacroText("Gorduras", calculatedNutrients.lipidios?.total, "g", COLOR_FAT)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = {}) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Adicionar à dieta")
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar")
                }

                TextButton(onClick = onNavigateToDetail) {
                    Icon(Icons.Default.Info, contentDescription = "Detalhes")
                    Spacer(Modifier.width(8.dp))
                    Text("Detalhes")
                }
            }
        }
    }
}

@Composable
fun DietOverviewCard(state: HomeState, onNavigateToDietList: () -> Unit) {
    val totals = state.dietTotals
    val rawPieData = listOf(
        PieChartData(totals.totalCarbs.toFloat(), COLOR_CARBS, "Carbs"),
        PieChartData(totals.totalProtein.toFloat(), COLOR_PROTEIN, "Protein"),
        PieChartData(totals.totalFat.toFloat(), COLOR_FAT, "Fat")
    )
    val pieData = rawPieData.map { PieChartData(value = (if (it.value.isFinite()) it.value else 0f).coerceAtLeast(0f), color = it.color, label = it.label) }
    val pieSum = pieData.sumOf { it.value.toDouble() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToDietList, role = Role.Button),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.primaryDiet?.dieta?.nome ?: "Nenhuma Dieta Principal",
                style = MaterialTheme.typography.headlineSmall,
                color = COLOR_TEXT
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.primaryDiet != null && totals.totalKcal > 0) {
                if (pieSum > 0.0) {
                    MacroPieChart(
                        modifier = Modifier
                            .fillMaxWidth(),
                        data = pieData,
                        totalValue = totals.totalKcal,
                        totalUnit = "kcal"
                    )
                } else {
                    Text("Sem macros para exibir.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
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
    Card(elevation = CardDefaults.cardElevation(6.dp), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Text(
                "Funcionalidades",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )
            HorizontalDivider()
            NavigationActionRow(
                title = "Gerenciar Dietas",
                icon = Icons.Default.Book,
                onClick = onNavigateToDietList
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
            .clickable(onClick = onClick, role = Role.Button)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}

private fun Alimento.subtitleShort(): String {
    val df = DecimalFormat("#.#")
    return this.categoria.takeIf { it.isNotBlank() }
        ?: this.proteina?.let { "Proteína: ${df.format(it)} g" }
        ?: this.energiaKcal?.let { "${it.toInt()} kcal" }
        ?: this.codigoOriginal
}

@Composable
fun MacroText(label: String, value: Double?, unit: String, color: Color, df: DecimalFormat = DecimalFormat("#.#")) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
        Text("${df.format(value ?: 0.0)}$unit", style = MaterialTheme.typography.bodyLarge)
    }
}