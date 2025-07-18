package com.mekki.taco.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// Imports para os modelos de dados e DAO (ajuste os caminhos se forem diferentes)
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.data.db.entity.Lipidios
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.presentation.ui.search.AlimentoViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Este Composable é o que será chamado pelo AppNavHost
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    alimentoViewModel: AlimentoViewModel,
    modifier: Modifier = Modifier,
    onAlimentoClick: (alimentoId: Int) -> Unit,
    onPlanejarDietaClick: () -> Unit,
    onDiarioAlimentarClick: () -> Unit
) {
    val termoBusca by alimentoViewModel.termoBusca.collectAsState()
    val resultados by alimentoViewModel.resultadosBusca.collectAsState()
    val isLoading by alimentoViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("TACO Nutri App") })
        },
        modifier = modifier
    ) { innerPadding ->
        MainScreenContent(
            paddingValues = innerPadding,
            termoBusca = termoBusca,
            onTermoBuscaChange = { novoTermo -> alimentoViewModel.onTermoBuscaChange(novoTermo) },
            resultados = resultados,
            isLoading = isLoading,
            onAlimentoClick = onAlimentoClick,
            onPlanejarDietaClick = onPlanejarDietaClick,
            onDiarioAlimentarClick = onDiarioAlimentarClick
        )
    }
}

// Este é o Composable que contém a UI e recebe o estado diretamente (bom para previews)
@Composable
private fun MainScreenContent(
    paddingValues: PaddingValues,
    termoBusca: String,
    onTermoBuscaChange: (String) -> Unit,
    resultados: List<Alimento>,
    isLoading: Boolean,
    onAlimentoClick: (alimentoId: Int) -> Unit,
    onPlanejarDietaClick: () -> Unit,
    onDiarioAlimentarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        AlimentoSearchSectionContent(
            modifier = Modifier.fillMaxWidth(),
            termoBusca = termoBusca,
            onTermoBuscaChange = onTermoBuscaChange,
            resultados = resultados,
            isLoading = isLoading,
            onAlimentoClick = onAlimentoClick
        )

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        NavigationActionsSection(
            modifier = Modifier.fillMaxWidth(),
            onPlanejarDietaClicked = onPlanejarDietaClick,
            onDiarioAlimentarClicked = onDiarioAlimentarClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlimentoSearchSectionContent(
    modifier: Modifier = Modifier,
    termoBusca: String,
    onTermoBuscaChange: (String) -> Unit,
    resultados: List<Alimento>,
    isLoading: Boolean,
    onAlimentoClick: (alimentoId: Int) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Log.d("AlimentoSearchSection", "Recompondo: termo='$termoBusca', loading=$isLoading, resultados=${resultados.size}")

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Consulta Rápida à Tabela TACO",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = termoBusca,
                onValueChange = onTermoBuscaChange,
                label = { Text("Buscar alimento...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Ícone de busca") }
            )
            Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscando...")
            }
        } else {
            // Condicionais para exibir mensagens ou a lista
            if (termoBusca.length < 2 && resultados.isEmpty() && !isLoading) {
                Text("Digite ao menos 2 caracteres para buscar.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally))
            } else if (resultados.isEmpty() && termoBusca.length >= 2 && !isLoading) {
                Text("Nenhum alimento encontrado para \"$termoBusca\".", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally))
            } else if (resultados.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false) // Permite que os botões abaixo apareçam
                        .heightIn(min = 56.dp, max = 250.dp), // Ajuste a altura conforme necessário
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = resultados, key = { it.id }) { alimento ->
                        AlimentoListItem(alimento = alimento) {
                            Log.d("AlimentoSearchSection", "Item clicado: ${alimento.nome} (ID: ${alimento.id})")
                            onAlimentoClick(alimento.id)
                        }
                    }
                }
            } else {
                // Se não estiver carregando, termo for válido mas resultados ainda vazios (pode acontecer brevemente)
                // ou se termo for válido mas realmente não houver resultados.
                // O Spacer abaixo é para manter algum espaço se a lista estiver vazia e nenhuma mensagem for mostrada.
                Spacer(modifier = Modifier.weight(1f, fill = false).heightIn(min = 56.dp))
            }
        }
    }
}

@Composable
fun NavigationActionsSection(
    modifier: Modifier = Modifier,
    onPlanejarDietaClicked: () -> Unit,
    onDiarioAlimentarClicked: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Funcionalidades Principais",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(onClick = onPlanejarDietaClicked, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.ListAlt, contentDescription = "Planejar dieta", modifier = Modifier.padding(end = 8.dp))
            Text("Planejar / Ver Dietas")
        }
        Button(onClick = onDiarioAlimentarClicked, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Fastfood, contentDescription = "Diário alimentar", modifier = Modifier.padding(end = 8.dp))
            Text("Meu Diário Alimentar")
        }
    }
}

@Composable
fun AlimentoListItem(alimento: Alimento, onClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(text = alimento.nome, style = MaterialTheme.typography.titleMedium)
            Text(text = "Categoria: ${alimento.categoria}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

// --- Início da Seção para Preview ---

// Defina o FakeAlimentoDaoPreview aqui para que o Preview o encontre.
// Certifique-se de que os imports para Alimento, Lipidios, Aminoacidos e AlimentoDao
// DENTRO desta classe FakeAlimentoDaoPreview estejam corretos para sua estrutura de pacotes.
class FakeAlimentoDaoPreview : AlimentoDao {
    val previewAlimentos = listOf(
        Alimento(id=1, codigoOriginal="PREVIEW001", nome="Maçã Fuji (Preview)", categoria="Frutas", energiaKcal=56.0, energiaKj=232.0, proteina=0.3, colesterol=0.0, carboidratos=15.2, fibraAlimentar=1.3, cinzas=0.2, calcio=2.0, magnesio=2.0, manganes=0.03, fosforo=9.0, ferro=0.1, sodio=0.0, potassio=75.0, cobre=0.06, zinco=0.0, retinol=null, RE=4.0, RAE=2.0, tiamina=0.0, riboflavina=0.0, piridoxina=0.03, niacina=0.0, vitaminaC=2.4, umidade=84.3, lipidios=Lipidios(total=0.0,saturados=0.0,monoinsaturados=0.0,poliinsaturados=0.0), aminoacidos=null),
        Alimento(id=2, codigoOriginal="PREVIEW002", nome="Arroz Cozido (Preview)", categoria="Cereais", energiaKcal=124.0, energiaKj=517.0, proteina=2.6, colesterol=null, carboidratos=25.8, fibraAlimentar=2.7, cinzas=0.5, calcio=5.0, magnesio=59.0, manganes=0.63, fosforo=106.0, ferro=0.3, sodio=1.0, potassio=75.0, cobre=0.02, zinco=0.7, retinol=null, RE=null, RAE=null, tiamina=0.08, riboflavina=0.0, piridoxina=0.08, niacina=0.0, vitaminaC=null, umidade=70.1, lipidios=Lipidios(total=1.0,saturados=0.3,monoinsaturados=0.4,poliinsaturados=0.3), aminoacidos=null),
        Alimento(id=3, codigoOriginal="PREVIEW003", nome="Pão Francês (Preview)", categoria="Pães", energiaKcal=289.0, energiaKj=1209.0, proteina=8.0, colesterol=null, carboidratos=58.6, fibraAlimentar=2.3, cinzas=1.9, calcio=19.0, magnesio=21.0, manganes=0.35, fosforo=83.0, ferro=0.8, sodio=586.0, potassio=101.0, cobre=0.14, zinco=0.6, retinol=null, RE=null, RAE=null, tiamina=0.05, riboflavina=0.25, piridoxina=0.03, niacina=3.3, vitaminaC=null, umidade=29.2, lipidios=Lipidios(total=1.7,saturados=0.4,monoinsaturados=0.3,poliinsaturados=0.6), aminoacidos=null)
    )
    override suspend fun inserirAlimento(alimento: Alimento): Long = 0L
    override suspend fun inserirAlimentos(alimentos: List<Alimento>) {}
    override suspend fun atualizarAlimento(alimento: Alimento): Int = 0
    override suspend fun deletarAlimento(alimento: Alimento) {}
    override suspend fun deletarTodosAlimentos() {}
    override fun buscarAlimentoPorId(id: Int): Flow<Alimento?> = flowOf(previewAlimentos.find { it.id == id })
    override fun buscarAlimentoPorCodigoOriginal(codigoOriginal: String): Flow<Alimento?> = flowOf(previewAlimentos.find { it.codigoOriginal == codigoOriginal })
    override fun buscarTodosAlimentos(): Flow<List<Alimento>> = flowOf(previewAlimentos)
    override fun buscarAlimentosPorNome(termoBusca: String): Flow<List<Alimento>> = flowOf(
        if (termoBusca.isBlank() || termoBusca.length < 2) {
            emptyList()
        } else {
            previewAlimentos.filter { it.nome.contains(termoBusca, ignoreCase = true) }
        }
    )
    override fun buscarAlimentosPorCategoria(categoria: String): Flow<List<Alimento>> = flowOf(previewAlimentos.filter { it.categoria.equals(categoria, ignoreCase = true) })
    override fun buscarTodasCategorias(): Flow<List<String>> = flowOf(previewAlimentos.map { it.categoria }.distinct().sorted())
    override suspend fun contarAlimentos(): Int = previewAlimentos.size
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Main Screen Preview", widthDp = 380, heightDp = 800)
@Composable
fun MainScreenPreview() {
    MaterialTheme { // Usando MaterialTheme como você pediu
        // Criamos uma lista de resultados fake para o preview da busca
        val fakeResultadosBusca = FakeAlimentoDaoPreview().previewAlimentos.filter {
            it.nome.contains("arr", ignoreCase = true) // Simula uma busca por "arr"
        }

        Scaffold(
            topBar = { CenterAlignedTopAppBar(title = { Text("TACO Nutri App (Preview)") }) }
        ) { innerPadding ->
            // Chamamos MainScreenContent diretamente, passando os estados
            MainScreenContent(
                paddingValues = innerPadding,
                termoBusca = "arr", // Simula um termo de busca
                onTermoBuscaChange = { Log.d("Preview", "Termo busca alterado para: $it") },
                resultados = fakeResultadosBusca, // Passa os resultados fakes
                isLoading = false, // Simula que não está carregando
                onAlimentoClick = { alimentoId -> Log.d("Preview", "Alimento $alimentoId clicado") },
                onPlanejarDietaClick = { Log.d("Preview", "Botão Planejar Dieta Clicado") },
                onDiarioAlimentarClick = { Log.d("Preview", "Botão Diário Alimentar Clicado") }
            )
        }
    }
}

// --- Fim da Seção para Preview ---