package com.mekki.taco.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.data.db.entity.Dieta
import com.mekki.taco.data.db.entity.ItemDieta

data class DietaComItens(
    @Embedded
    val dieta: Dieta,

    @Relation(
        parentColumn = "id", // From Dieta
        entityColumn = "dietaId", // From ItemDieta
        entity = ItemDieta::class // Specify the entity for the list items
    )
    val itens: List<ItemDietaComAlimento>
)
