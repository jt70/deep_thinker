package org.deep_thinker.dl.math

import java.util.*
import java.util.stream.DoubleStream


class Vec {
    val data: DoubleArray

    constructor(data: DoubleArray) {
        this.data = data
    }

    constructor(vararg data: Int) : this(Arrays.stream(data).asDoubleStream().toArray())

    constructor(size: Int) {
        data = DoubleArray(size)
    }

    fun dimension(): Int {
        return data.size
    }

    fun dot(u: Vec): Double {
        assertCorrectDimension(u.dimension())

        var sum = 0.0
        for (i in data.indices) sum += data[i] * u.data[i]

        return sum
    }

    fun map(fn: Function): Vec {
        val result = DoubleArray(data.size)
        for (i in data.indices) result[i] = fn.apply(data[i])
        return Vec(result)
    }

    override fun toString(): String {
        return "Vec{" + "data=" + data.contentToString() + '}'
    }

    fun indexOfLargestElement(): Int {
        var ixOfLargest = 0
        for (i in data.indices) if (data[i] > data[ixOfLargest]) ixOfLargest = i
        return ixOfLargest
    }

    fun sub(u: Vec): Vec {
        assertCorrectDimension(u.dimension())

        val result = DoubleArray(u.dimension())

        for (i in data.indices) result[i] = data[i] - u.data[i]

        return Vec(result)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val vec = o as Vec

        return data.contentEquals(vec.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }


    fun mul(s: Double): Vec {
        return map { value -> s * value }
    }

    fun outerProduct(u: Vec): Matrix {
        val result = Array(u.dimension()) {
            DoubleArray(
                dimension()
            )
        }

        for (i in data.indices) for (j in u.data.indices) result[j][i] = data[i] * u.data[j]

        return Matrix(result)
    }

    fun elementProduct(u: Vec): Vec {
        assertCorrectDimension(u.dimension())

        val result = DoubleArray(u.dimension())

        for (i in data.indices) result[i] = data[i] * u.data[i]

        return Vec(result)
    }

    fun add(u: Vec): Vec {
        assertCorrectDimension(u.dimension())

        val result = DoubleArray(u.dimension())

        for (i in data.indices) result[i] = data[i] + u.data[i]

        return Vec(result)
    }

    fun mul(m: Matrix): Vec {
        assertCorrectDimension(m.rows())

        val mData: Array<DoubleArray> = m.data
        val result = DoubleArray(m.cols())

        for (col in 0 until m.cols()) for (row in 0 until m.rows()) result[col] += mData[row][col] * data[row]

        return Vec(result)
    }


    private fun assertCorrectDimension(inpDim: Int) {
        require(dimension() == inpDim) {
            String.format(
                "Different dimensions: Input is %d, Vec is %d", inpDim, dimension()
            )
        }
    }

    fun max(): Double {
        return DoubleStream.of(*data).max().asDouble
    }

    fun sub(a: Double): Vec {
        val result = DoubleArray(dimension())

        for (i in data.indices) result[i] = data[i] - a

        return Vec(result)
    }

    fun sumElements(): Double {
        return DoubleStream.of(*data).sum()
    }
}
