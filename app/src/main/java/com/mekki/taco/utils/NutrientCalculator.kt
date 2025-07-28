// Math.kt
package com.mekki.taco.utils

import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.data.db.entity.Aminoacidos
import com.mekki.taco.data.db.entity.Lipidios

data class NutrientesPorPorcao(
    val nomeOriginal: String, // Nome do alimento base
    val quantidadeGramas: Double, // Quantidade para a qual os nutrientes foram calculados
    val umidade: Double?, // % (geralmente não é escalonado, mas incluído para informação)
    val energiaKcal: Double?,
    val energiaKj: Double?,
    val proteina: Double?,
    val carboidratos: Double?,
    val lipidios: Lipidios?, // Objeto Lipidios com valores calculados
    val fibraAlimentar: Double?,
    val colesterol: Double?, // mg
    val cinzas: Double?, // g
    val calcio: Double?, // mg
    val magnesio: Double?, // mg
    val manganes: Double?, // mg
    val fosforo: Double?, // mg
    val ferro: Double?, // mg
    val sodio: Double?, // mg
    val potassio: Double?, // mg
    val cobre: Double?, // mg
    val zinco: Double?, // mg
    val retinol: Double?, // µg
    val RE: Double?, // µg
    val RAE: Double?, // µg
    val tiamina: Double?, // mg
    val riboflavina: Double?, // mg
    val piridoxina: Double?, // mg
    val niacina: Double?, // mg
    val vitaminaC: Double?, // mg
    val aminoacidos: Aminoacidos? // Objeto Aminoacidos com valores calculados
)

object NutrientCalculator {

    /**
     * Calcula os valores nutricionais para uma quantidade específica de um alimento.
     * Assume que os valores no objeto Alimento são por 100g.
     */
    fun calcularNutrientesParaPorcao(
        alimentoBase: Alimento, // O alimento original com valores por 100g
        quantidadeDesejadaGramas: Double
    ): NutrientesPorPorcao {
        val fator = quantidadeDesejadaGramas / 100.0

        fun calcular(valorPor100g: Double?): Double? {
            return valorPor100g?.let { it * fator }
        }

        val lipidiosCalculados = alimentoBase.lipidios?.let { lip ->
            Lipidios(
                total = calcular(lip.total),
                saturados = calcular(lip.saturados),
                monoinsaturados = calcular(lip.monoinsaturados),
                poliinsaturados = calcular(lip.poliinsaturados)
            )
        }

        val aminoacidosCalculados = alimentoBase.aminoacidos?.let { aa ->
            Aminoacidos(
                triptofano = calcular(aa.triptofano), treonina = calcular(aa.treonina),
                isoleucina = calcular(aa.isoleucina), leucina = calcular(aa.leucina),
                lisina = calcular(aa.lisina), metionina = calcular(aa.metionina),
                cistina = calcular(aa.cistina), fenilalanina = calcular(aa.fenilalanina),
                tirosina = calcular(aa.tirosina), valina = calcular(aa.valina),
                arginina = calcular(aa.arginina), histidina = calcular(aa.histidina),
                alanina = calcular(aa.alanina), acidoAspartico = calcular(aa.acidoAspartico),
                acidoGlutamico = calcular(aa.acidoGlutamico), glicina = calcular(aa.glicina),
                prolina = calcular(aa.prolina), serina = calcular(aa.serina)
            )
        }

        return NutrientesPorPorcao(
            nomeOriginal = alimentoBase.nome,
            quantidadeGramas = quantidadeDesejadaGramas,
            umidade = alimentoBase.umidade, // Umidade geralmente é uma % do total, não escalonada pela porção da mesma forma
            energiaKcal = calcular(alimentoBase.energiaKcal),
            energiaKj = calcular(alimentoBase.energiaKj),
            proteina = calcular(alimentoBase.proteina),
            carboidratos = calcular(alimentoBase.carboidratos),
            lipidios = lipidiosCalculados,
            fibraAlimentar = calcular(alimentoBase.fibraAlimentar),
            colesterol = calcular(alimentoBase.colesterol),
            cinzas = calcular(alimentoBase.cinzas),
            calcio = calcular(alimentoBase.calcio),
            magnesio = calcular(alimentoBase.magnesio),
            manganes = calcular(alimentoBase.manganes),
            fosforo = calcular(alimentoBase.fosforo),
            ferro = calcular(alimentoBase.ferro),
            sodio = calcular(alimentoBase.sodio),
            potassio = calcular(alimentoBase.potassio),
            cobre = calcular(alimentoBase.cobre),
            zinco = calcular(alimentoBase.zinco),
            retinol = calcular(alimentoBase.retinol),
            RE = calcular(alimentoBase.RE),
            RAE = calcular(alimentoBase.RAE),
            tiamina = calcular(alimentoBase.tiamina),
            riboflavina = calcular(alimentoBase.riboflavina),
            piridoxina = calcular(alimentoBase.piridoxina),
            niacina = calcular(alimentoBase.niacina),
            vitaminaC = calcular(alimentoBase.vitaminaC),
            aminoacidos = aminoacidosCalculados
        )
    }
}