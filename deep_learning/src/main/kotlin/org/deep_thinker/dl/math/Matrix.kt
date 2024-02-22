package org.deep_thinker.dl.math

import java.util.*


/**
 * Careful: not immutable. Most matrix operations are made on same object.
 */
class Matrix(val data: Array<DoubleArray>) {
    private val rows = data.size
    private val cols = data[0].size

    constructor(rows: Int, cols: Int) : this(Array<DoubleArray>(rows) {
        DoubleArray(
            cols
        )
    })

    fun multiply(v: Vec): Vec {
        val out = DoubleArray(rows)
        for (y in 0 until rows) out[y] = Vec(data[y]).dot(v)

        return Vec(out)
    }

    fun map(fn: Function): Matrix {
        for (y in 0 until rows) for (x in 0 until cols) data[y][x] = fn.apply(data[y][x])

        return this
    }

    fun rows(): Int {
        return rows
    }

    fun cols(): Int {
        return cols
    }

    fun mul(s: Double): Matrix {
        return map { value -> s * value }
    }

    fun add(other: Matrix): Matrix {
        assertCorrectDimension(other)

        for (y in 0 until rows) for (x in 0 until cols) data[y][x] += other.data[y][x]

        return this
    }

    fun sub(other: Matrix): Matrix {
        assertCorrectDimension(other)

        for (y in 0 until rows) for (x in 0 until cols) data[y][x] -= other.data[y][x]

        return this
    }

    fun fillFrom(other: Matrix): Matrix {
        assertCorrectDimension(other)

        for (y in 0 until rows) if (cols >= 0) System.arraycopy(other.data[y], 0, data[y], 0, cols)

        return this
    }

    fun average(): Double {
        return Arrays.stream(data).flatMapToDouble { array: DoubleArray? ->
            Arrays.stream(
                array
            )
        }.average().asDouble
    }

    fun variance(): Double {
        val avg = average()
        return Arrays.stream(data).flatMapToDouble { array: DoubleArray? ->
            Arrays.stream(
                array
            )
        }.map { a: Double -> (a - avg) * (a - avg) }.average().asDouble
    }

    // -------------------------------------------------------------------------
    private fun assertCorrectDimension(other: Matrix) {
        require(!(rows != other.rows || cols != other.cols)) {
            String.format(
                "Matrix of different dim: Input is %d x %d, Vec is %d x %d",
                rows,
                cols,
                other.rows,
                other.cols
            )
        }
    }

    fun copy(): Matrix {
        val m = Matrix(rows, cols)
        for (y in 0 until rows) if (cols >= 0) System.arraycopy(data[y], 0, m.data[y], 0, cols)

        return m
    }

    override fun toString(): String {
        return StringJoiner(", ", Matrix::class.java.simpleName + "[", "]")
            .add("data=" + data.contentDeepToString())
            .toString()
    }
}

