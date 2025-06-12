package com.mekki.taco.ui.fooddetail

import androidx.compose.foundation.layout.*
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Alimento // Ajuste o import
import com.mekki.taco.data.db.entity.Aminoacidos
import com.mekki.taco.data.db.entity.Lipidios
// import com.mekki.taco.ui.theme.TACOTheme // Ou MaterialTheme
import com.mekki.taco.ui.search.AlimentoDetailViewModel // Ajuste o import para o ViewModel
import com.mekki.taco.util.NutrientesPorPorcao
import com.mekki.taco.util.NutrientCalculator
// Importe a data class de porção
// Para o Preview
import com.mekki.taco.data.db.dao.AlimentoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlimentoDetailScreen(
    viewModel: AlimentoDetailViewModel,
    onNavigateBack: () -> Unit
) {
    // Coleta o estado dos nutrientes calculados para a porção selecionada
    val nutrientesPorcao by viewModel.nutrientesParaPorcao.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val quantidadeSelecionada by viewModel.quantidadeSelecionadaGramas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nutrientesPorcao?.nomeOriginal ?: "Detalhes do Alimento", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        AlimentoDetailScreenContent(
            paddingValues = paddingValues,
            isLoading = isLoading,
            nutrientesPorcao = nutrientesPorcao,
            quantidadeSelecionada = quantidadeSelecionada,
            onQuantidadeChange = { novaQuantidade ->
                viewModel.atualizarQuantidade(novaQuantidade)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlimentoDetailScreenContent(
    paddingValues: PaddingValues,
    isLoading: Boolean,
    nutrientesPorcao: NutrientesPorPorcao?,
    quantidadeSelecionada: Double,
    onQuantidadeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (isLoading && nutrientesPorcao == null) { // Mostra loading apenas se os dados iniciais ainda não chegaram
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (nutrientesPorcao == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Alimento não encontrado ou erro ao carregar.")
            }
        } else {
            // Campo para alterar a quantidade
            OutlinedTextField(
                value = DecimalFormat("#.##").format(quantidadeSelecionada), // Formata para exibir
                onValueChange = { novoValor ->
                    // Permite que o usuário digite vírgula ou ponto como decimal
                    onQuantidadeChange(novoValor.replace(',', '.'))
                },
                label = { Text("Quantidade (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item { NutrientHeader("Informações para ${DecimalFormat("#.##").format(nutrientesPorcao.quantidadeGramas)}g de ${nutrientesPorcao.nomeOriginal}") }
                item { NutrientRow("Umidade", nutrientesPorcao.umidade, "%") } // Umidade (base 100g)

                item { NutrientHeader("Energia e Macronutrientes") }
                item { NutrientRow("Energia", nutrientesPorcao.energiaKcal, "kcal") }
                item { NutrientRow("Energia", nutrientesPorcao.energiaKj, "kJ") }
                item { NutrientRow("Proteína Total", nutrientesPorcao.proteina, "g") }
                item { NutrientRow("Lipídios (Gordura Total)", nutrientesPorcao.lipidios?.total, "g") }
                item { NutrientRow("Carboidrato Total", nutrientesPorcao.carboidratos, "g") }
                item { NutrientRow("Fibra Alimentar", nutrientesPorcao.fibraAlimentar, "g") }
                item { NutrientRow("Colesterol", nutrientesPorcao.colesterol, "mg", "Ausente se N/A") }
                item { NutrientRow("Cinzas (Minerais Totais)", nutrientesPorcao.cinzas, "g") }

                item { NutrientHeader("Minerais") }
                item { NutrientRow("Cálcio", nutrientesPorcao.calcio, "mg") }
                item { NutrientRow("Magnésio", nutrientesPorcao.magnesio, "mg") }
                item { NutrientRow("Manganês", nutrientesPorcao.manganes, "mg") }
                item { NutrientRow("Fósforo", nutrientesPorcao.fosforo, "mg") }
                item { NutrientRow("Ferro", nutrientesPorcao.ferro, "mg") }
                item { NutrientRow("Sódio", nutrientesPorcao.sodio, "mg") }
                item { NutrientRow("Potássio", nutrientesPorcao.potassio, "mg") }
                item { NutrientRow("Cobre", nutrientesPorcao.cobre, "mg") }
                item { NutrientRow("Zinco", nutrientesPorcao.zinco, "mg") }

                item { NutrientHeader("Vitaminas") }
                item { NutrientRow("Retinol", nutrientesPorcao.retinol, "µg") }
                item { NutrientRow("Equivalente de Retinol (RE)", nutrientesPorcao.RE, "µg") }
                item { NutrientRow("Equiv. Atividade de Retinol (RAE)", nutrientesPorcao.RAE, "µg") }
                item { NutrientRow("Tiamina (Vitamina B1)", nutrientesPorcao.tiamina, "mg") }
                item { NutrientRow("Riboflavina (Vitamina B2)", nutrientesPorcao.riboflavina, "mg") }
                item { NutrientRow("Piridoxina (Vitamina B6)", nutrientesPorcao.piridoxina, "mg") }
                item { NutrientRow("Niacina", nutrientesPorcao.niacina, "mg") }
                item { NutrientRow("Vitamina C (Ácido Ascórbico)", nutrientesPorcao.vitaminaC, "mg") }

                nutrientesPorcao.lipidios?.let { lip ->
                    if (listOfNotNull(lip.saturados, lip.monoinsaturados, lip.poliinsaturados).any { it != null } || lip.total != null) {
                        item { NutrientHeader("Perfil Lipídico") }
                        // if (lip.total != null) item { NutrientRow("Lipídios Totais (detalhe)", lip.total, "g") } // Já está nos macros
                        item { NutrientRow("Saturados", lip.saturados, "g") }
                        item { NutrientRow("Monoinsaturados", lip.monoinsaturados, "g") }
                        item { NutrientRow("Poliinsaturados", lip.poliinsaturados, "g") }
                    }
                }

                nutrientesPorcao.aminoacidos?.let { aa ->
                    if (listOfNotNull(aa.triptofano, aa.treonina, aa.isoleucina, aa.leucina, aa.lisina, aa.metionina, aa.cistina, aa.fenilalanina, aa.tirosina, aa.valina, aa.arginina, aa.histidina, aa.alanina, aa.acidoAspartico, aa.acidoGlutamico, aa.glicina, aa.prolina, aa.serina).any { it != null }) {
                        item { NutrientHeader("Perfil de Aminoácidos") }
                        item { NutrientRow("Triptofano", aa.triptofano, "g") }
                        item { NutrientRow("Treonina", aa.treonina, "g") }
                        item { NutrientRow("Isoleucina", aa.isoleucina, "g") }
                        item { NutrientRow("Leucina", aa.leucina, "g") }
                        item { NutrientRow("Lisina", aa.lisina, "g") }
                        item { NutrientRow("Metionina", aa.metionina, "g") }
                        item { NutrientRow("Cistina", aa.cistina, "g") }
                        item { NutrientRow("Fenilalanina", aa.fenilalanina, "g") }
                        item { NutrientRow("Tirosina", aa.tirosina, "g") }
                        item { NutrientRow("Valina", aa.valina, "g") }
                        item { NutrientRow("Arginina", aa.arginina, "g") }
                        item { NutrientRow("Histidina", aa.histidina, "g") }
                        item { NutrientRow("Alanina", aa.alanina, "g") }
                        item { NutrientRow("Ácido Aspártico", aa.acidoAspartico, "g") }
                        item { NutrientRow("Ácido Glutâmico", aa.acidoGlutamico, "g") }
                        item { NutrientRow("Glicina", aa.glicina, "g") }
                        item { NutrientRow("Prolina", aa.prolina, "g") }
                        item { NutrientRow("Serina", aa.serina, "g") }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// Composable auxiliar para exibir um cabeçalho de seção (como antes)
@Composable
fun NutrientHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
}

// Composable auxiliar para exibir uma linha de nutriente (como antes, mas sempre exibe)
@Composable
fun NutrientRow(label: String, value: Any?, unit: String = "", observacao: String? = null) {
    val df = DecimalFormat("#.###") // Formato para até 3 casas decimais
    val displayValue = value?.let { if (it is Double) df.format(it) else it.toString() } ?: "N/A"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = if (displayValue == "N/A" && observacao != null) observacao else "$displayValue ${if(displayValue != "N/A" && value != null) unit else ""}",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End
        )
    }
    Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

// --- Preview ---
// Fake DAO para o Preview da Tela de Detalhes
class FakeAlimentoDaoPreviewDetail : AlimentoDao { /* ... (implementação como na sua última mensagem ou na minha anterior) ... */
    private val previewAlimento = Alimento(id = 1, codigoOriginal = "TACO001", nome = "Arroz, integral, cozido (Preview)", categoria = "Cereais e derivados", umidade = 70.1, energiaKcal = 124.0, energiaKj = 517.0, proteina = 2.6, colesterol = null, carboidratos = 25.8, fibraAlimentar = 2.7, cinzas = 0.5, calcio = 5.0, magnesio = 59.0, manganes = 0.63, fosforo = 106.0, ferro = 0.3, sodio = 1.0, potassio = 75.0, cobre = 0.02, zinco = 0.7, retinol = null, RE = null, RAE = null, tiamina = 0.08, riboflavina = 0.00, piridoxina = 0.08, niacina = 0.90, vitaminaC = null,
        lipidios = Lipidios(total = 1.0, saturados = 0.3, monoinsaturados = 0.4, poliinsaturados = 0.3),
        aminoacidos = Aminoacidos(triptofano = 0.028, treonina = 0.087, isoleucina = 0.114, leucina = 0.200, lisina = 0.100, metionina = 0.058, cistina = 0.037, fenilalanina = 0.131, tirosina = 0.087, valina = 0.153, arginina = 0.180, histidina = 0.061, alanina = 0.149, acidoAspartico = 0.230, acidoGlutamico = 0.490, glicina = 0.127, prolina = 0.115, serina = 0.130)
    )
    override suspend fun inserirAlimento(alimento: Alimento): Long = 0L
    override suspend fun inserirAlimentos(alimentos: List<Alimento>) {}
    override suspend fun atualizarAlimento(alimento: Alimento): Int = 0
    override suspend fun deletarAlimento(alimento: Alimento) {}
    override suspend fun deletarTodosAlimentos() {}
    override fun buscarAlimentoPorId(id: Int): Flow<Alimento?> = flowOf(if(id == 1) previewAlimento else null)
    override fun buscarAlimentoPorCodigoOriginal(codigoOriginal: String): Flow<Alimento?> = flowOf(if(codigoOriginal == "TACO001") previewAlimento else null)
    override fun buscarTodosAlimentos(): Flow<List<Alimento>> = flowOf(listOf(previewAlimento))
    override fun buscarAlimentosPorNome(termoBusca: String): Flow<List<Alimento>> = flowOf(emptyList())
    override fun buscarAlimentosPorCategoria(categoria: String): Flow<List<Alimento>> = flowOf(emptyList())
    override fun buscarTodasCategorias(): Flow<List<String>> = flowOf(emptyList())
    override suspend fun contarAlimentos(): Int = 1
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "id:pixel_6", name = "Tela de Detalhes")
@Composable
fun AlimentoDetailScreenPreview() {
    MaterialTheme { // Ou MaterialTheme
        val fakeAlimentoBase = Alimento(
            id = 1, codigoOriginal = "PREVIEW001", nome = "Maçã Gala (Preview)",
            categoria = "Frutas", umidade = 85.0, energiaKcal = 58.0, energiaKj = 243.0,
            proteina = 0.3, colesterol = null, carboidratos = 15.0, fibraAlimentar = 2.0,
            cinzas = 0.3, calcio = 5.0, magnesio = 4.0, manganes = 0.04, fosforo = 7.0,
            ferro = 0.1, sodio = 1.0, potassio = 100.0, cobre = 0.04, zinco = 0.05,
            retinol = null, RE = null, RAE = null, tiamina = 0.02, riboflavina = 0.02,
            piridoxina = 0.03, niacina = 0.1, vitaminaC = 3.0,
            lipidios = Lipidios(total = 0.2, saturados = 0.0, monoinsaturados = 0.0, poliinsaturados = 0.1),
            aminoacidos = Aminoacidos(triptofano = 0.003, treonina = 0.008, isoleucina = 0.009, leucina = 0.017, lisina = 0.012, metionina = 0.002, cistina = 0.002, fenilalanina = 0.009, tirosina = 0.003, valina = 0.012, arginina = 0.007, histidina = 0.003, alanina = 0.010, acidoAspartico = 0.060, acidoGlutamico = 0.030, glicina = 0.010, prolina = 0.008, serina = 0.011)
        )
        val fakeNutrientesPorcao = NutrientCalculator.calcularNutrientesParaPorcao(fakeAlimentoBase, 150.0) // Ex: para 150g

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(fakeNutrientesPorcao.nomeOriginal, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = { /* Preview: ação de voltar */ }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                )
            }
        ) { paddingValues ->
            AlimentoDetailScreenContent(
                paddingValues = paddingValues,
                isLoading = false,
                nutrientesPorcao = fakeNutrientesPorcao,
                quantidadeSelecionada = 150.0,
                onQuantidadeChange = { Log.d("Preview", "Quantidade alterada para: $it") }
            )
        }
    }
}