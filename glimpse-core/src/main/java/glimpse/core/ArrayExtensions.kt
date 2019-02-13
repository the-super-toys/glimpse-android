package glimpse.core

import glimpse.core.ArrayUtils.generateEmptyTensor
import kotlin.math.exp
import kotlin.math.ln

fun Array<FloatArray>.flattened(): FloatArray {
    var flattened = floatArrayOf()
    this.forEach { row ->
        flattened += row
    }
    return flattened
}

fun FloatArray.reshape(rows: Int, cols: Int): Array<Array<Array<FloatArray>>> {
    val newShaped = generateEmptyTensor(1, 1, rows, cols)
    for (i in 0 until rows) {
        for (j in 0 until cols) {
            val value = this[j + i * cols]

            newShaped[0][0][i][j] = value
        }
    }
    return newShaped
}

fun FloatArray.temper(temperature: Float) = this.map {
    ln(it) / temperature
}.map {
    exp(it)
}.let { a ->
    val sum = a.sum()
    a.map { it / sum }
}.toFloatArray()

val Pair<Float, Float>.x
    get() = this.first

val Pair<Float, Float>.y
    get() = this.second
