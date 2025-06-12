package com.mekki.taco.presentation.ui.search

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
// Remova collectAsState e getValue daqui se AlimentoSearchScreen for stateless
// import androidx.compose.runtime.collectAsState
// import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Alimento // Ajuste o import para o seu pacote de modelo
import com.mekki.taco.data.db.entity.Lipidios
import com.mekki.taco.data.db.dao.AlimentoDao // Ajuste o import para o seu DAO
// import com.mekki.taco.ui.theme.TACOTheme // Ou MaterialTheme se você preferir para o preview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Este é o Composable que realmente desenha a UI da busca.
// Ele é "burro" (stateless) - recebe dados e lambdas, não o ViewModel inteiro.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlimentoSearchScreenContent( // Renomeado para indicar que é o conteúdo da UI
    modifier: Modifier = Modifier,
    termoBusca: String,
    onTermoBuscaChange: (String) -> Unit,
    resultados: List<Alimento>,
    isLoading: Boolean,
    onAlimentoClick: (alimentoId: Int) -> Unit, // Lambda para o clique no item
    onPerformSearch: () -> Unit // Lambda para ação de busca (ex: no teclado)
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Log.d("AlimentoSearchScreen", "Recompondo Content: termo='$termoBusca', loading=$isLoading, resultados=${resultados.size}")

    Column(
        modifier = modifier
            .fillMaxSize() // Ocupa o espaço inteiro dado pelo chamador
            .padding(16.dp) // Padding interno da seção
    ) {
        Text(
            text = "Consulta Rápida de Alimentos",
            style = MaterialTheme.typography.headlineSmall, // Ajustado para headlineSmall
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = termoBusca,
            onValueChange = onTermoBuscaChange, // Chama a lambda do ViewModel
            label = { Text("Digite o nome do alimento") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onPerformSearch() // Chama a lambda para ação de busca
                    keyboardController?.hide()
                }
            ),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Ícone de busca") }
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            if (termoBusca.length < 2 && resultados.isEmpty()) {
                Text("Digite ao menos 2 caracteres para iniciar a busca.",
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top=8.dp))
            } else if (resultados.isEmpty() && termoBusca.length >= 2) {
                Text("Nenhum alimento encontrado para \"$termoBusca\".",
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top=8.dp))
            } else if (resultados.isNotEmpty()){
                LazyColumn(
                    // Se esta seção for parte de uma tela maior que rola,
                    // talvez você não queira .fillMaxSize() aqui, ou use .weight(1f)
                    // ou .heightIn() para controlar o tamanho.
                    modifier = Modifier.weight(1f, fill = false).heightIn(min=56.dp, max=300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = resultados,
                        key = { alimento -> alimento.id }
                    ) { alimento ->
                        AlimentoListItem(alimento = alimento) {
                            onAlimentoClick(alimento.id) // Chama a lambda passada
                        }
                    }
                }
            } else {
                // Espaço reservado se nenhuma das condições acima for atendida
                // e a lista de resultados estiver vazia (ex: termoBusca vazio no início).
                Spacer(modifier = Modifier.weight(1f, fill = false).heightIn(min = 56.dp))
            }
        }
    }
}

// AlimentoListItem permanece o mesmo que você já tem
@Composable
fun AlimentoListItem(alimento: Alimento, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = alimento.nome, style = MaterialTheme.typography.titleMedium)
            Text(text = "Categoria: ${alimento.categoria}", style = MaterialTheme.typography.bodySmall)
        }
    }
}


// --- INÍCIO: Código para o Preview ---
// Coloque esta classe FakeAlimentoDaoPreview no final do seu arquivo AlimentoSearchScreen.kt
// ou em um arquivo de utilidades de debug/preview e importe-a.
// Certifique-se que os imports DENTRO dela estão corretos para sua estrutura de pacotes.
class FakeAlimentoDaoPreview : AlimentoDao {
    val previewAlimentos = listOf(
        Alimento(id=1, codigoOriginal="PREVIEW001", nome="Maçã Fuji (Preview)", categoria="Frutas", energiaKcal=56.0, energiaKj=232.0, proteina=0.3, colesterol=0.0, carboidratos=15.2, fibraAlimentar=1.3, cinzas=0.2, calcio=2.0, magnesio=2.0, manganes=0.03, fosforo=9.0, ferro=0.1, sodio=0.0, potassio=75.0, cobre=0.06, zinco=0.0, retinol=null, RE=4.0, RAE=2.0, tiamina=0.0, riboflavina=0.0, piridoxina=0.03, niacina=0.0, vitaminaC=2.4, umidade=84.3, lipidios=Lipidios(total=0.0,saturados=0.0,monoinsaturados=0.0,poliinsaturados=0.0), aminoacidos=null),
        Alimento(id=2, codigoOriginal="PREVIEW002", nome="Arroz Cozido (Preview)", categoria="Cereais", energiaKcal=124.0, energiaKj=517.0, proteina=2.6, colesterol=null, carboidratos=25.8, fibraAlimentar=2.7, cinzas=0.5, calcio=5.0, magnesio=59.0, manganes=0.63, fosforo=106.0, ferro=0.3, sodio=1.0, potassio=75.0, cobre=0.02, zinco=0.7, retinol=null, RE=null, RAE=null, tiamina=0.08, riboflavina=0.0, piridoxina=0.08, niacina=0.0, vitaminaC=null, umidade=70.1, lipidios=Lipidios(total=1.0,saturados=0.3,monoinsaturados=0.4,poliinsaturados=0.3), aminoacidos=null)
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
        if (termoBusca.isBlank() || termoBusca.length < 2) emptyList()
        else previewAlimentos.filter { it.nome.contains(termoBusca, ignoreCase = true) }
    )
    override fun buscarAlimentosPorCategoria(categoria: String): Flow<List<Alimento>> = flowOf(previewAlimentos.filter { it.categoria.equals(categoria, ignoreCase = true) })
    override fun buscarTodasCategorias(): Flow<List<String>> = flowOf(previewAlimentos.map { it.categoria }.distinct().sorted())
    override suspend fun contarAlimentos(): Int = previewAlimentos.size
}

@Preview(showBackground = true, widthDp = 380, name = "Tela de Busca (Resultados)")
@Composable
fun AlimentoSearchScreenContentPreview_ComResultados() {
    MaterialTheme { // Usando MaterialTheme
        AlimentoSearchScreenContent(
            termoBusca = "arr",
            onTermoBuscaChange = { Log.d("Preview", "Busca: $it") },
            resultados = FakeAlimentoDaoPreview().previewAlimentos.filter { it.nome.contains("arr",ignoreCase = true) },
            isLoading = false,
            onAlimentoClick = { id -> Log.d("Preview", "Clicou no alimento ID: $id") },
            onPerformSearch = { Log.d("Preview", "Perform Search Clicado") }
        )
    }
}

@Preview(showBackground = true, widthDp = 380, name = "Tela de Busca (Carregando)")
@Composable
fun AlimentoSearchScreenContentPreview_Carregando() {
    MaterialTheme {
        AlimentoSearchScreenContent(
            termoBusca = "arroz",
            onTermoBuscaChange = {},
            resultados = emptyList(),
            isLoading = true, // Simulando carregamento
            onAlimentoClick = {},
            onPerformSearch = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 380, name = "Tela de Busca (Sem Resultados)")
@Composable
fun AlimentoSearchScreenContentPreview_SemResultados() {
    MaterialTheme {
        AlimentoSearchScreenContent(
            termoBusca = "xyz123",
            onTermoBuscaChange = {},
            resultados = emptyList(),
            isLoading = false,
            onAlimentoClick = {},
            onPerformSearch = {}
        )
    }
}
// --- FIM da Seção para Preview ---