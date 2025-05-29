package com.mekki.taco.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Precisa ser finalizado
data class Dieta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nome: String,
    val dataCriacao: Long,
    val objetivoCalorias: Double? = null

)