package com.mekki.taco.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "itens_dieta",
    foreignKeys = [
        ForeignKey(
            entity = Dieta::class,
            parentColumns = ["id"], // Coluna 'id' da tabela 'dietas'
            childColumns = ["dietaId"], // Coluna 'dietaId' desta tabela ('itens_dieta')
            onDelete = ForeignKey.CASCADE // Se uma dieta for deletada, seus itens também serão
        ),
        ForeignKey(
            entity = Alimento::class,
            parentColumns = ["id"], // Coluna 'id' da tabela 'alimento' (PK do Room)
            childColumns = ["alimentoId"], // Coluna 'alimentoId' desta tabela
            onDelete = ForeignKey.CASCADE // Se um alimento for deletado (raro, mas possível), o item da dieta também será
        )
    ],
    // Índices para otimizar buscas por dietaId e alimentoId
    indices = [Index(value = ["dietaId"]), Index(value = ["alimentoId"])]
)
data class ItemDieta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Chave primária do item da dieta

    val dietaId: Int, // Chave estrangeira referenciando o ID da Dieta
    val alimentoId: Int, // Chave estrangeira referenciando o ID interno do Alimento na tabela 'alimento'

    val quantidadeGramas: Double, // Quantidade do alimento em gramas (ex: 150.0 para 150g)
    val tipoRefeicao: String? = null) // Opcional: "Café da Manhã", "Almoço", "Jantar", "Lanche"
    //  adicionar um campo para a data específica, caso a dieta não seja sempre a mesma