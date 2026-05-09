package top.e404.skiko.lowpoly

import org.jetbrains.skia.Bitmap
import top.e404.skiko.util.rgb
import kotlin.math.sqrt

object Sobel {
    private val kernelX = arrayOf(intArrayOf(-1, 0, 1), intArrayOf(-2, 0, 2), intArrayOf(-1, 0, 1))
    private val kernelY = arrayOf(intArrayOf(-1, -2, -1), intArrayOf(0, 0, 0), intArrayOf(1, 2, 1))
    private fun Array<IntArray>.getPixel(
        bitmap: Bitmap,
        x: Int,
        y: Int
    ) = this[0][0] * bitmap.getAvg(x - 1, y - 1) +
            this[0][1] * bitmap.getAvg(x, y - 1) +
            this[0][2] * bitmap.getAvg(x + 1, y - 1) +
            this[1][0] * bitmap.getAvg(x - 1, y) +
            this[1][1] * bitmap.getAvg(x, y) +
            this[1][2] * bitmap.getAvg(x + 1, y) +
            this[2][0] * bitmap.getAvg(x - 1, y + 1) +
            this[2][1] * bitmap.getAvg(x, y + 1) +
            this[2][2] * bitmap.getAvg(x + 1, y + 1)

    fun sobel(
        bitmap: Bitmap,
        callback: (magnitude: Int, x: Int, y: Int) -> Unit
    ) {
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            val pixelX = kernelX.getPixel(bitmap, x, y)
            val pixelY = kernelY.getPixel(bitmap, x, y)
            val magnitude = sqrt((pixelX * pixelX + pixelY * pixelY).toDouble()).toInt()
            callback.invoke(magnitude, x, y)
        }
    }

    /**
     * 计算该点颜色的rgb平均值
     *
     * @param x     x
     * @param y     y
     * @return rpg平均值
     */
    private fun Bitmap.getAvg(x: Int, y: Int): Int {
        if (x < 0
            || y < 0
            || x >= width
            || y >= height
        ) return 0
        val (r, g, b) = getColor(x, y).rgb()
        return (r + g + b) / 3
    }
}
