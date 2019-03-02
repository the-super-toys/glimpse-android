package glimpse.core

import glimpse.core.ArrayUtils.generateEmptyTensor

internal fun Array<FloatArray>.flattened(): FloatArray {
    var flattened = floatArrayOf()
    this.forEach { row ->
        flattened += row
    }
    return flattened
}

internal fun Array<Array<FloatArray>>.flattened(): FloatArray {
    var flattened = floatArrayOf()
    this.forEach { row ->
        flattened += row.map { it[0] }
    }
    return flattened
}

internal fun FloatArray.reshape(rows: Int, cols: Int): Array<Array<Array<FloatArray>>> {
    val newShaped = generateEmptyTensor(1, 1, rows, cols)
    for (i in 0 until rows) {
        for (j in 0 until cols) {
            val value = this[j + i * cols]

            newShaped[0][0][i][j] = value
        }
    }
    return newShaped
}

internal val Pair<Float, Float>.x
    get() = this.first

internal val Pair<Float, Float>.y
    get() = this.second
