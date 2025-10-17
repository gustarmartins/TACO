@file:OptIn(ExperimentalMaterial3Api::class)

package com.mekki.taco.presentation.ui.diet

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mekki.taco.data.db.entity.Dieta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietListScreen(
    viewModel: DietListViewModel,
    onNavigateToCreateDiet: () -> Unit,
    onNavigateToDietDetail: (dietId: Int) -> Unit,
    onFabChange: (@Composable (() -> Unit)?) -> Unit
) {
    val dietas by viewModel.dietas.collectAsState()

    DisposableEffect(Unit) {
        onFabChange {
            FloatingActionButton(onClick = onNavigateToCreateDiet) {
                Icon(Icons.Filled.Add, contentDescription = "Criar Nova Dieta")
            }
        }
        // Limpa o FAB quando a tela sai
        onDispose {
            onFabChange(null)
        }
    }

    if (dietas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Nenhuma dieta criada ainda.\nClique no '+' para adicionar!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items = dietas, key = { dieta -> dieta.id }) { dieta ->
                DietListItem(
                    dieta = dieta,
                    onClick = { onNavigateToDietDetail(dieta.id) },
                    onDeleteClick = { viewModel.deletarDieta(dieta) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietListItem(
    dieta: Dieta,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)) {
                Text(dieta.nome, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Criada em: ${formatarDataTimestamp(dieta.dataCriacao)}",
                    style = MaterialTheme.typography.bodySmall
                )
                dieta.objetivoCalorias?.let {
                    Text("Meta: ${it.toInt()} kcal", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Deletar Dieta",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatarDataTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}