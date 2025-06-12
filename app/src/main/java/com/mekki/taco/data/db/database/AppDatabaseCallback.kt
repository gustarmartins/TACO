package com.mekki.taco.data.local

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.charset.StandardCharsets

class AppDatabaseCallback(
    private val context: Context,
    private val scope: CoroutineScope // tarefas em background
) : RoomDatabase.Callback() {

    companion object {
        private const val TAG = "AppDB_Callback"
    }

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d(TAG, "onCreate FOI CHAMADO! Populando o banco de dados...")
        scope.launch(Dispatchers.IO) { // scope injetado
            populateDatabaseFromSqlFile(context, db)
        }
    }

    // suspend para realizar operações de IO longas
    private suspend fun populateDatabaseFromSqlFile(context: Context, db: SupportSQLiteDatabase) {
        Log.d(TAG, "populateDatabaseFromSqlFile INICIADA.")
        val sqlFileName = "taco_preload.sql"

        try {
            context.assets.open(sqlFileName).bufferedReader(StandardCharsets.UTF_8).useLines { lines ->
                db.beginTransaction()
                var statementCount = 0
                try {
                    val statementBuilder = StringBuilder()
                    lines.forEach { line ->
                        val trimmedLine = line.trim()
                        if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("--")) {
                            statementBuilder.append(trimmedLine)
                            if (!trimmedLine.endsWith(";")) {
                                statementBuilder.append(" ")
                            }

                            if (trimmedLine.endsWith(";")) {
                                try {
                                    db.execSQL(statementBuilder.toString())
                                    statementCount++
                                } catch (e: Exception) {
                                    Log.e(TAG, "Erro ao executar SQL: '${statementBuilder.toString()}' - ${e.message}", e)
                                }
                                statementBuilder.clear()
                            }
                        }
                    }
                    if (statementBuilder.isNotBlank()) {
                        try {
                            db.execSQL(statementBuilder.toString())
                            statementCount++
                        } catch (e: Exception) {
                            Log.e(TAG, "Erro ao executar SQL final: '${statementBuilder.toString()}' - ${e.message}", e)
                        }
                    }
                    db.setTransactionSuccessful()
                    Log.d(TAG, "Banco de dados populado com sucesso! $statementCount statements executados a partir de $sqlFileName.")
                } catch (e: Exception) { // Captura exceções durante o loop ou transação
                    Log.e(TAG, "ERRO GERAL durante a execução dos comandos SQL em $sqlFileName: ${e.message}", e)
                } finally {
                    db.endTransaction()
                }
            }
        } catch (e: IOException) { // Captura erros de IO (ex: arquivo não encontrado)
            Log.e(TAG, "ERRO ao abrir ou ler o arquivo $sqlFileName dos assets: ${e.message}", e)
        } catch (e: Exception) { // Captura outros erros inesperados
            Log.e(TAG, "ERRO inesperado em populateDatabaseFromSqlFile: ${e.message}", e)
        }
    }
}