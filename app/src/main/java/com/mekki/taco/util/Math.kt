package com.mekki.taco.util

import com.mekki.taco.data.model.Alimento
import com.mekki.taco.data.model.Aminoacidos // Importe suas data classes
import com.mekki.taco.data.model.Lipidios

// Data class para armazenar os valores calculados de um alimento para uma certa quantidade
data class NutrientesPorPorcao(
    val nome: String,
    val quantidadeGramas: Double,
    val energiaKcal: Double?,
    val energiaKj: Double?,
    val proteina: Double?,
    val carboidratos: Double?,
    val lipidios: Lipidios?, // Lipidios calculados
    val fibraAlimentar: Double?,
    val colesterol: Double?,
    val calcio: Double?,
    val cinzas: Double?,
    val magnesio: Double?,
    val manganes: Double?,
    val fosforo: Double?,
    val ferro: Double?,
    val sodio: Double?,
    val potassio: Double?,
    val cobre: Double?,
    val zinco: Double?,
    val retinol: Double?,
    val RE: Double?,
    val RAE: Double?,
    val tiamina: Double?,
    val riboflavina: Double?,
    val piridoxina: Double?,
    val niacina: Double?,
    val vitaminaC: Double?,
    val aminoacidos: Aminoacidos? // Aminoacidos calculados
    // Adicione outros campos que você queira calcular especificamente
)

object NutrientCalculator {

    /**
     * Calcula os valores nutricionais para uma quantidade específica de um alimento.
     * Assume que os valores no objeto Alimento são por 100g.
     */
    fun calcularNutrientesParaPorcao(alimento: Alimento, quantidadeDesejadaGramas: Double): NutrientesPorPorcao {
        val fator = quantidadeDesejadaGramas / 100.0

        // Função auxiliar para calcular um valor individual, tratando nulos
        fun calcular(valorPor100g: Double?): Double? {
            return valorPor100g?.let { it * fator }
        }

        val lipidiosCalculados = alimento.lipidios?.let {
            Lipidios(
                total = calcular(it.total),
                saturados = calcular(it.saturados),
                monoinsaturados = calcular(it.monoinsaturados),
                poliinsaturados = calcular(it.poliinsaturados)
            )
        }

        val aminoacidosCalculados = alimento.aminoacidos?.let {
            Aminoacidos(
                triptofano = calcular(it.triptofano), treonina = calcular(it.treonina),
                isoleucina = calcular(it.isoleucina), leucina = calcular(it.leucina),
                lisina = calcular(it.lisina), metionina = calcular(it.metionina),
                cistina = calcular(it.cistina), fenilalanina = calcular(it.fenilalanina),
                tirosina = calcular(it.tirosina), valina = calcular(it.valina),
                arginina = calcular(it.arginina), histidina = calcular(it.histidina),
                alanina = calcular(it.alanina), acidoAspartico = calcular(it.acidoAspartico),
                acidoGlutamico = calcular(it.acidoGlutamico), glicina = calcular(it.glicina),
                prolina = calcular(it.prolina), serina = calcular(it.serina)
            )
        }

        return NutrientesPorPorcao(
            nome = alimento.nome,
            quantidadeGramas = quantidadeDesejadaGramas,
            energiaKcal = calcular(alimento.energiaKcal),
            energiaKj = calcular(alimento.energiaKj),
            proteina = calcular(alimento.proteina),
            colesterol = calcular(alimento.colesterol),
            carboidratos = calcular(alimento.carboidratos),
            fibraAlimentar = calcular(alimento.fibraAlimentar),
            cinzas = calcular(alimento.cinzas),
            calcio = calcular(alimento.calcio),
            magnesio = calcular(alimento.magnesio),
            manganes = calcular(alimento.manganes),
            fosforo = calcular(alimento.fosforo),
            ferro = calcular(alimento.ferro),
            sodio = calcular(alimento.sodio),
            potassio = calcular(alimento.potassio),
            cobre = calcular(alimento.cobre),
            zinco = calcular(alimento.zinco),
            retinol = calcular(alimento.retinol),
            RE = calcular(alimento.RE),
            RAE = calcular(alimento.RAE),
            tiamina = calcular(alimento.tiamina),
            riboflavina = calcular(alimento.riboflavina),
            piridoxina = calcular(alimento.piridoxina),
            niacina = calcular(alimento.niacina),
            vitaminaC = calcular(alimento.vitaminaC),
            lipidios = lipidiosCalculados,
            aminoacidos = aminoacidosCalculados
        )
    }

    // TODO: Adicionar funções para somar nutrientes de uma lista de NutrientesPorPorcao (para uma refeição ou dieta)
    // Ex: fun calcularTotaisDieta(itens: List<NutrientesPorPorcao>): MacrosCalculados
}