package com.mekki.taco.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mekki.taco.data.model.Alimento
import com.mekki.taco.data.model.Dieta
import com.mekki.taco.data.model.ItemDieta
import kotlinx.coroutines.CoroutineScope

// para produção o exportSchema deve ser true
@Database(
    entities = [
        Alimento::class,
        Dieta::class,
        ItemDieta::class
    ],
    // aumentar conforme atualizações da Tabela TACO oficial
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun alimentoDao(): AlimentoDao
    abstract fun dietaDao(): DietaDao
    abstract fun itemDietaDao(): ItemDietaDao

    companion object {
        // A anotação @Volatile garante que a variável INSTANCE seja sempre atualizada
        // e visível para todas as threads, prevenindo problemas de concorrência.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            // Retorna a instância existente se já foi criada (padrão Singleton).
            // Caso contrário, cria a instância do banco de dados de forma segura para threads.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taco_database"
                )
                    // popula o banco na primeira chamada da DB - apenas caso não exista.
                    .addCallback(AppDatabaseCallback(context.applicationContext, scope))
                    // deprecated?
                    // destrói o banco ao mudar versões
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}