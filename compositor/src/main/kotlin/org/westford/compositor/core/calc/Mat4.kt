/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.core.calc

import org.westford.compositor.core.Point

/**
 * @param m00 Column 0, Row 0
 * @param m10 Column 1, Row 0
 * @param m20 Column 2, Row 0
 * @param m30 Column 3, Row 0
 * @param m01 Column 0, Row 1
 * @param m11 Column 1, Row 1
 * @param m21 Column 2, Row 1
 * @param m31 Column 3, Row 1
 * @param m02 Column 0, Row 2
 * @param m12 Column 1, Row 2
 * @param m22 Column 2, Row 2
 * @param m32 Column 3, Row 2
 * @param m03 Column 0, Row 3
 * @param m13 Column 1, Row 3
 * @param m23 Column 2, Row 3
 * @param m33 Column 3, Row 3
 */
data class Mat4(val m00: Float,
                val m10: Float,
                val m20: Float,
                val m30: Float,
                val m01: Float,
                val m11: Float,
                val m21: Float,
                val m31: Float,
                val m02: Float,
                val m12: Float,
                val m22: Float,
                val m32: Float,
                val m03: Float,
                val m13: Float,
                val m23: Float,
                val m33: Float) {

    operator fun plus(other: Mat4): Mat4 {
        //@formatter:off
        return Mat4(m00 + other.m00, m10 + other.m10, m20 + other.m20, m30 + other.m30,
                    m01 + other.m01, m11 + other.m11, m21 + other.m21, m31 + other.m31,
                    m02 + other.m02, m12 + other.m12, m22 + other.m22, m32 + other.m32,
                    m03 + other.m03, m13 + other.m13, m23 + other.m23, m33 + other.m33)
        //@formatter:on
    }

    operator fun times(right: Point): Point = times(right.toVec4()).toPoint()

    operator fun times(right: Vec4): Vec4 {
        val rightX = right.x
        val rightY = right.y
        val rightZ = right.z
        val rightW = right.w

        return Vec4(m00 * rightX + m10 * rightY + m20 * rightZ + m30 * rightW,
                    m01 * rightX + m11 * rightY + m21 * rightZ + m31 * rightW,
                    m02 * rightX + m12 * rightY + m22 * rightZ + m32 * rightW,
                    m03 * rightX + m13 * rightY + m23 * rightZ + m33 * rightW)
    }

    operator fun times(right: Mat4): Mat4 {
        val nm00 = this.m00 * right.m00 + this.m10 * right.m01 + this.m20 * right.m02 + this.m30 * right.m03
        val nm01 = this.m01 * right.m00 + this.m11 * right.m01 + this.m21 * right.m02 + this.m31 * right.m03
        val nm02 = this.m02 * right.m00 + this.m12 * right.m01 + this.m22 * right.m02 + this.m32 * right.m03
        val nm03 = this.m03 * right.m00 + this.m13 * right.m01 + this.m23 * right.m02 + this.m33 * right.m03
        val nm10 = this.m00 * right.m10 + this.m10 * right.m11 + this.m20 * right.m12 + this.m30 * right.m13
        val nm11 = this.m01 * right.m10 + this.m11 * right.m11 + this.m21 * right.m12 + this.m31 * right.m13
        val nm12 = this.m02 * right.m10 + this.m12 * right.m11 + this.m22 * right.m12 + this.m32 * right.m13
        val nm13 = this.m03 * right.m10 + this.m13 * right.m11 + this.m23 * right.m12 + this.m33 * right.m13
        val nm20 = this.m00 * right.m20 + this.m10 * right.m21 + this.m20 * right.m22 + this.m30 * right.m23
        val nm21 = this.m01 * right.m20 + this.m11 * right.m21 + this.m21 * right.m22 + this.m31 * right.m23
        val nm22 = this.m02 * right.m20 + this.m12 * right.m21 + this.m22 * right.m22 + this.m32 * right.m23
        val nm23 = this.m03 * right.m20 + this.m13 * right.m21 + this.m23 * right.m22 + this.m33 * right.m23
        val nm30 = this.m00 * right.m30 + this.m10 * right.m31 + this.m20 * right.m32 + this.m30 * right.m33
        val nm31 = this.m01 * right.m30 + this.m11 * right.m31 + this.m21 * right.m32 + this.m31 * right.m33
        val nm32 = this.m02 * right.m30 + this.m12 * right.m31 + this.m22 * right.m32 + this.m32 * right.m33
        val nm33 = this.m03 * right.m30 + this.m13 * right.m31 + this.m23 * right.m32 + this.m33 * right.m33

        //@formatter:off
        return Mat4(nm00, nm10, nm20, nm30,
                    nm01, nm11, nm21, nm31,
                    nm02, nm12, nm22, nm32,
                    nm03, nm13, nm23, nm33)
        //@formatter:on
    }

    fun invert(): Mat4 {
        val matrix2d = Array(4) { FloatArray(4) }
        matrix2d[0][0] = m00
        matrix2d[0][1] = m10
        matrix2d[0][2] = m20
        matrix2d[0][3] = m30
        matrix2d[1][0] = m01
        matrix2d[1][1] = m11
        matrix2d[1][2] = m21
        matrix2d[1][3] = m31
        matrix2d[2][0] = m02
        matrix2d[2][1] = m12
        matrix2d[2][2] = m22
        matrix2d[2][3] = m32
        matrix2d[3][0] = m03
        matrix2d[3][1] = m13
        matrix2d[3][2] = m23
        matrix2d[3][3] = m33

        //FIXME test uninvertable matrix, what will/should happen?
        val matrix2dInverted = invert(matrix2d)
        //@formatter:off
        return Mat4(matrix2dInverted[0][0], matrix2dInverted[0][1], matrix2dInverted[0][2], matrix2dInverted[0][3],
                    matrix2dInverted[1][0], matrix2dInverted[1][1], matrix2dInverted[1][2], matrix2dInverted[1][3],
                    matrix2dInverted[2][0], matrix2dInverted[2][1], matrix2dInverted[2][2], matrix2dInverted[2][3],
                    matrix2dInverted[3][0], matrix2dInverted[3][1], matrix2dInverted[3][2], matrix2dInverted[3][3])
        //@formatter:on
    }

    private fun invert(a: Array<FloatArray>): Array<FloatArray> {
        val n = a.size
        val x = Array(n) { FloatArray(n) }
        val b = Array(n) { FloatArray(n) }
        val index = IntArray(n)
        for (i in 0..n - 1) {
            b[i][i] = 1f
        }

        gaussian(a,
                 index)

        for (i in 0..n - 1 - 1) {
            for (j in i + 1..n - 1) {
                for (k in 0..n - 1) {
                    b[index[j]][k] -= a[index[j]][i] * b[index[i]][k]
                }
            }
        }

        for (i in 0..n - 1) {
            x[n - 1][i] = b[index[n - 1]][i] / a[index[n - 1]][n - 1]
            for (j in n - 2 downTo 0) {
                x[j][i] = b[index[j]][i]
                for (k in j + 1..n - 1) {
                    x[j][i] -= a[index[j]][k] * x[k][i]
                }
                x[j][i] /= a[index[j]][j]
            }
        }
        return x
    }

    private fun gaussian(a: Array<FloatArray>,
                         index: IntArray) {
        val n = index.size
        val c = FloatArray(n)

        for (i in 0..n - 1) {
            index[i] = i
        }

        for (i in 0..n - 1) {
            val c1 = (0..n - 1).map { Math.abs(a[i][it]) }.max() ?: 0f
            c[i] = c1
        }

        var k = 0
        for (j in 0..n - 1 - 1) {
            var pi1 = 0f
            for (i in j..n - 1) {
                var pi0 = Math.abs(a[index[i]][j])
                pi0 /= c[index[i]]
                if (pi0 > pi1) {
                    pi1 = pi0
                    k = i
                }
            }

            val itmp = index[j]
            index[j] = index[k]
            index[k] = itmp
            for (i in j + 1..n - 1) {
                val pj = a[index[i]][j] / a[index[j]][j]
                a[index[i]][j] = pj
                // Modify other elements accordingly
                for (l in j + 1..n - 1) {
                    a[index[i]][l] -= pj * a[index[j]][l]
                }
            }
        }
    }

    fun toArray(): FloatArray {
        //@formatter:off
        return floatArrayOf(m00, m01, m02, m03,
                            m10, m11, m12, m13,
                            m20, m21, m22, m23,
                            m30, m31, m32, m33)
        //@formatter:on
    }

    companion object {
        //@formatter:off
        val IDENTITY = Mat4(1f, 0f, 0f, 0f,
                            0f, 1f, 0f, 0f,
                            0f, 0f, 1f, 0f,
                            0f, 0f, 0f, 1f)
        //@formatter:on
    }
}
