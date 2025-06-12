package com.mekki.taco.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dietas") // Nome da tabela no banco de dados
data class Dieta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                 // Chave primária, auto-gerada pelo Room

    val nome: String,                // Nome da dieta, ex: "Dieta Hipercalórica"
    val dataCriacao: Long,           // Data/hora da criação (timestamp em milissegundos)
    val objetivoCalorias: Double? = null // Meta de calorias (opcional, por isso Double?)
    // Default é null se não for fornecido
)