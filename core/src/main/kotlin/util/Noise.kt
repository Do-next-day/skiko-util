package top.e404.skiko.util

import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 柏林噪声
 */
class Noise {
    companion object {
        private const val B = 0x100
        private const val BM = 0xff
        private const val N = 0x1000
    }

    private var p = IntArray(B + B + 2)
    private var g3 = Array(B + B + 2) { FloatArray(3) }
    private var g2 = Array(B + B + 2) { FloatArray(2) }
    private var g1 = FloatArray(B + B + 2)
    private var start = true
    private fun sCurve(t: Float) = t * t * (3.0f - 2.0f * t)

    /**
     * 计算二维柏林噪声
     *
     * @param x x 坐标
     * @param y y 坐标
     * @return (x,y)的噪声数值
     */
    fun noise2(x: Float, y: Float): Float {
        if (start) {
            start = false
            init()
        }
        var t = x + N
        val bx0 = t.toInt() and BM
        val bx1 = bx0 + 1 and BM
        val rx0 = t - t.toInt()
        val rx1 = rx0 - 1.0f
        t = y + N
        val by0 = t.toInt() and BM
        val by1 = by0 + 1 and BM
        val ry0 = t - t.toInt()
        val ry1 = ry0 - 1.0f
        val i = p[bx0]
        val j = p[bx1]
        val b00 = p[i + by0]
        val b10 = p[j + by0]
        val b01 = p[i + by1]
        val b11 = p[j + by1]
        val sx = sCurve(rx0)
        val sy = sCurve(ry0)
        var q = g2[b00]
        var u = rx0 * q[0] + ry0 * q[1]
        q = g2[b10]
        var v = rx1 * q[0] + ry0 * q[1]
        val a = lerp(sx, u, v)
        q = g2[b01]
        u = rx0 * q[0] + ry1 * q[1]
        q = g2[b11]
        v = rx1 * q[0] + ry1 * q[1]
        val b = lerp(sx, u, v)
        return 1.5f * lerp(sy, a, b)
    }

    private fun lerp(t: Float, a: Float, b: Float) = a + t * (b - a)

    private fun normalize2(v: FloatArray) {
        val s = sqrt((v[0] * v[0] + v[1] * v[1]).toDouble()).toFloat()
        v[0] = v[0] / s
        v[1] = v[1] / s
    }

    private fun normalize3(v: FloatArray) {
        val s = sqrt((v[0] * v[0] + v[1] * v[1] + v[2] * v[2]).toDouble()).toFloat()
        v[0] = v[0] / s
        v[1] = v[1] / s
        v[2] = v[2] / s
    }

    private fun random() = Random.nextInt() and 0x7fffffff

    private fun init() {
        var j: Int
        var k: Int
        var i = 0
        while (i < B) {
            p[i] = i
            g1[i] = (random() % (B + B) - B).toFloat() / B
            j = 0
            while (j < 2) {
                g2[i][j] = (random() % (B + B) - B).toFloat() / B
                j++
            }
            normalize2(g2[i])
            j = 0
            while (j < 3) {
                g3[i][j] = (random() % (B + B) - B).toFloat() / B
                j++
            }
            normalize3(g3[i])
            i++
        }
        i = B - 1
        while (i >= 0) {
            k = p[i]
            p[i] = p[random() % B.also {
                j = it
            }]
            p[j] = k
            i--
        }
        i = 0
        while (i < B + 2) {
            p[B + i] = p[i]
            g1[B + i] = g1[i]
            j = 0
            while (j < 2) {
                g2[B + i][j] = g2[i][j]
                j++
            }
            j = 0
            while (j < 3) {
                g3[B + i][j] = g3[i][j]
                j++
            }
            i++
        }
    }
}
