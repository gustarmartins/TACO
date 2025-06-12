package com.mekki.taco.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mekki.taco.data.model.Dieta
import kotlinx.coroutines.flow.Flow

// Responsável pelas tarefas de DB de uma dieta
@Dao
interface DietaDao {

    /**
     * Insere uma nova dieta no banco de dados.
     * Se uma dieta com o mesmo ID já existir (improvável com autoGenerate = true,
     * mas OnConflictStrategy.REPLACE lida com isso substituindo), ela será substituída.
     * @param dieta A dieta a ser inserida.
     * @return O ID da linha (rowId) da dieta inserida.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirDieta(dieta: Dieta): Long

    /**
     * Atualiza uma dieta existente no banco de dados.
     * A correspondência é feita pela chave primária da dieta.
     * @param dieta A dieta a ser atualizada.
     */
    @Update
    suspend fun atualizarDieta(dieta: Dieta)

    /**
     * Deleta uma dieta do banco de dados.
     * A correspondência é feita pela chave primária da dieta.
     * Se a dieta for deletada, os ItemDieta associados também serão
     * devido ao onDelete = ForeignKey.CASCADE na entidade ItemDieta.
     * @param dieta A dieta a ser deletada.
     */
    @Delete
    suspend fun deletarDieta(dieta: Dieta)

    /**
     * Busca uma dieta específica pelo seu ID.
     * Retorna um Flow, que permite observar mudanças nos dados automaticamente.
     * @param id O ID da dieta a ser buscada.
     * @return Um Flow contendo a Dieta ou null se não encontrada.
     */
    @Query("SELECT * FROM dietas WHERE id = :id")
    fun buscarDietaPorId(id: Int): Flow<Dieta?>

    /**
     * Busca todas as dietas cadastradas, ordenadas pela data de criação (mais recentes primeiro).
     * Retorna um Flow, que permite observar mudanças nos dados automaticamente.
     * @return Um Flow contendo a lista de todas as dietas.
     */
    @Query("SELECT * FROM dietas ORDER BY dataCriacao DESC")
    fun buscarTodasDietas(): Flow<List<Dieta>>

    /**
     * Busca dietas cujo nome contenha o termo de pesquisa (ignorando maiúsculas/minúsculas).
     * @param nomeBusca O texto a ser procurado no nome das dietas.
     * @return Um Flow contendo a lista de dietas correspondentes.
     */
    @Query("SELECT * FROM dietas WHERE nome LIKE '%' || :nomeBusca || '%' ORDER BY nome ASC")
    fun buscarDietasPorNome(nomeBusca: String): Flow<List<Dieta>>

    /**
     * Deleta todas as dietas da tabela.
     * CUIDADO: Isso também deletará todos os ItemDieta associados devido ao CASCADE.
     */
    @Query("DELETE FROM dietas")
    suspend fun deletarTodasDietas()
}