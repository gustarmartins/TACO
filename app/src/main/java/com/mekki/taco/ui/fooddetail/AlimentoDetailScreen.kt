package com.mekki.taco.ui.fooddetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.model.Alimento // Ajuste o import conforme sua estrutura
import com.mekki.taco.data.model.Aminoacidos
import com.mekki.taco.data.model.Lipidios
// import com.mekki.taco.ui.theme.TACOTheme
import com.mekki.taco.ui.search.AlimentoDetailViewModel // Importe seu ViewModel

// Composable "inteligente" que usa o ViewModel (usado na navegação real)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlimentoDetailScreen(
    viewModel: AlimentoDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val alimento by viewModel.alimento.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(alimento?.nome ?: "Detalhes do Alimento", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Delega a exibição do conteúdo para um Composable "burro" que recebe o estado
        AlimentoDetailScreenContent(
            alimento = alimento,
            isLoading = isLoading,
            paddingValues = paddingValues
        )
    }
}

// Composable de "conteúdo" que apenas exibe a UI com base no estado recebido
// Este é mais fácil de usar em Previews e testes.
@Composable
private fun AlimentoDetailScreenContent(
    alimento: Alimento?,
    isLoading: Boolean,
    paddingValues: PaddingValues // Padding vindo do Scaffold
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (alimento == null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Alimento não encontrado ou erro ao carregar.")
        }
    } else {
        // Se o alimento foi carregado, exibe os detalhes
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplica o padding do Scaffold
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { NutrientHeader("Informações Gerais (por 100g)") }
            item { NutrientRow("Nome", alimento.nome) }
            item { NutrientRow("Código TACO", alimento.codigoOriginal) }
            item { NutrientRow("Categoria", alimento.categoria) }
            item { NutrientRow("Umidade", alimento.umidade, "%") }

            item { NutrientHeader("Energia e Macronutrientes") }
            item { NutrientRow("Energia", alimento.energiaKcal, "kcal") }
            item { NutrientRow("Energia", alimento.energiaKj, "kJ") }
            item { NutrientRow("Proteína Total", alimento.proteina, "g") }
            item { NutrientRow("Lipídios (Gordura Total)", alimento.lipidios?.total, "g") }
            item { NutrientRow("Carboidrato Total", alimento.carboidratos, "g") }
            item { NutrientRow("Fibra Alimentar", alimento.fibraAlimentar, "g") }
            item { NutrientRow("Colesterol", alimento.colesterol, "mg", "Ausente se N/A") }
            item { NutrientRow("Cinzas (Minerais Totais)", alimento.cinzas, "g") }

            item { NutrientHeader("Minerais") }
            item { NutrientRow("Cálcio", alimento.calcio, "mg") }
            item { NutrientRow("Magnésio", alimento.magnesio, "mg") }
            item { NutrientRow("Manganês", alimento.manganes, "mg") }
            item { NutrientRow("Fósforo", alimento.fosforo, "mg") }
            item { NutrientRow("Ferro", alimento.ferro, "mg") }
            item { NutrientRow("Sódio", alimento.sodio, "mg") }
            item { NutrientRow("Potássio", alimento.potassio, "mg") }
            item { NutrientRow("Cobre", alimento.cobre, "mg") }
            item { NutrientRow("Zinco", alimento.zinco, "mg") }

            item { NutrientHeader("Vitaminas") }
            item { NutrientRow("Retinol", alimento.retinol, "µg") }
            item { NutrientRow("Equivalente de Retinol (RE)", alimento.RE, "µg") }
            item { NutrientRow("Equiv. Atividade de Retinol (RAE)", alimento.RAE, "µg") }
            item { NutrientRow("Tiamina (Vitamina B1)", alimento.tiamina, "mg") }
            item { NutrientRow("Riboflavina (Vitamina B2)", alimento.riboflavina, "mg") }
            item { NutrientRow("Piridoxina (Vitamina B6)", alimento.piridoxina, "mg") }
            item { NutrientRow("Niacina", alimento.niacina, "mg") }
            item { NutrientRow("Vitamina C (Ácido Ascórbico)", alimento.vitaminaC, "mg") }

            alimento.lipidios?.let { lip ->
                if (listOfNotNull(lip.saturados, lip.monoinsaturados, lip.poliinsaturados).any { it != null && it > 0.0 } || lip.total != null) { // Verifica se há dados para exibir
                    item { NutrientHeader("Perfil Lipídico") }
                    if (lip.total != null) item { NutrientRow("Lipídios Totais (repetido)", lip.total, "g") } // Já mostrado acima, mas pode ser útil aqui também
                    item { NutrientRow("Saturados", lip.saturados, "g") }
                    item { NutrientRow("Monoinsaturados", lip.monoinsaturados, "g") }
                    item { NutrientRow("Poliinsaturados", lip.poliinsaturados, "g") }
                }
            }

            alimento.aminoacidos?.let { aa ->
                if (listOfNotNull(aa.triptofano, aa.treonina, aa.isoleucina, aa.leucina, aa.lisina, aa.metionina, aa.cistina, aa.fenilalanina, aa.tirosina, aa.valina, aa.arginina, aa.histidina, aa.alanina, aa.acidoAspartico, aa.acidoGlutamico, aa.glicina, aa.prolina, aa.serina).any { it != null && it > 0.0 }) {
                    item { NutrientHeader("Perfil de Aminoácidos") }
                    item { NutrientRow("Triptofano", aa.triptofano, "g") } // Unidade TACO: g/100g de proteína. Para mostrar em g/100g do alimento, precisaria recalcular ou verificar a fonte. Assumindo g/100g do alimento por simplicidade.
                    item { NutrientRow("Treonina", aa.treonina, "g") }
                    // ... (todos os outros aminoácidos) ...
                    item { NutrientRow("Serina", aa.serina, "g") }
                }
            }
            item { Spacer(Modifier.height(16.dp)) } // Espaço no final da lista
        }
    }
}

// Composable auxiliar para exibir um cabeçalho de seção (como antes)
@Composable
fun NutrientHeader(text: String) { /* ... como antes ... */ }

// Composable auxiliar para exibir uma linha de nutriente (como antes)
@Composable
fun NutrientRow(label: String, value: Any?, unit: String = "", observacao: String? = null) { /* ... como antes ... */ }


// Preview para AlimentoDetailScreen
@OptIn(ExperimentalMaterial3Api::class) // Para o Scaffold e TopAppBar no Preview
@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun AlimentoDetailScreenPreview() {
    MaterialTheme {
        // Crie um objeto Alimento falso com todos os campos que você quer testar no preview
        val fakeAlimento = Alimento(
            id = 1, codigoOriginal = "PREVIEW001", nome = "Arroz, integral, cozido (Preview)",
            categoria = "Cereais e derivados", umidade = 70.1, energiaKcal = 124.0, energiaKj = 517.0,
            proteina = 2.6, colesterol = null, carboidratos = 25.8, fibraAlimentar = 2.7,
            cinzas = 0.5, calcio = 5.0, magnesio = 59.0, manganes = 0.63, fosforo = 106.0,
            ferro = 0.3, sodio = 1.0, potassio = 75.0, cobre = 0.02, zinco = 0.7,
            retinol = null, RE = null, RAE = null, tiamina = 0.08, riboflavina = 0.00,
            piridoxina = 0.08, niacina = 0.90, vitaminaC = null,
            lipidios = Lipidios(total = 1.0, saturados = 0.3, monoinsaturados = 0.4, poliinsaturados = 0.3),
            aminoacidos = Aminoacidos(triptofano = 0.028, treonina = 0.087, isoleucina = 0.114, leucina = 0.200, lisina = 0.100, metionina = 0.058, cistina = 0.037, fenilalanina = 0.131, tirosina = 0.087, valina = 0.153, arginina = 0.180, histidina = 0.061, alanina = 0.149, acidoAspartico = 0.230, acidoGlutamico = 0.490, glicina = 0.127, prolina = 0.115, serina = 0.130)
        )

        // O Scaffold é usado para fornecer o padding correto para o AlimentoDetailScreenContent
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(fakeAlimento.nome, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = { /* Ação vazia para preview */ }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                )
            }
        ) { innerPadding ->
            AlimentoDetailScreenContent(
                alimento = fakeAlimento,
                isLoading = false, // Simula que o carregamento terminou
                paddingValues = innerPadding // Passa o padding do Scaffold
            )
        }
    }
}