package top.e404.dbf

/**
 * 代表二值的点阵图, 坐标轴原点在左上方, 横向x轴, 竖向y轴
 *
 * @property width 点阵图宽度
 * @property height 点阵图高度
 * @property bytes 点阵图数据
 */
class BitMatrix(
    val width: Int,
    val height: Int,
    private val bytes: ByteArray = ByteArray(width * height)
) {
    companion object {
        operator fun invoke(width: Int, height: Int, string: String): BitMatrix {
            val bytes = ByteArray(width * height * 4)
            for (i in string.indices step 2) {
                bytes[i / 2] = string.substring(i, i + 2).toUByte(16).toByte()
            }
            return BitMatrix(width * 4, height, bytes)
        }

        operator fun invoke(lines: List<String>) = invoke(lines[0].length, lines.size, lines.joinToString(""))
    }

    val xRange get() = 0 until width
    val yRange get() = 0 until height

    /**
     * 获取对应位置的bit
     */
    operator fun get(x: Int, y: Int): Boolean {
        val offset = 7 - x % 8
        val get = y * width / 8 + x / 8
        return bytes[get].toInt() shr offset and 1 != 0
    }

    /**
     * 设置对应位置的bit
     */
    operator fun set(x: Int, y: Int, value: Boolean) {
        val offset = 7 - x % 8
        val get = y * width / 8 + x / 8
        val old = bytes[get].toInt()
        val new =
            if (value) old or (1 shl offset)
            else old and (1 shl offset).inv()
        bytes[get] = new.toUByte().toByte()

    }

    /**
     * 遍历每个bit
     *
     * @param block 处理
     */
    inline fun forEachBit(block: (x: Int, y: Int, bit: Boolean) -> Unit) {
        for (y in yRange) for (x in xRange) {
            block(x, y, get(x, y))
        }
    }
}

/**
 * 横向叠加
 */
operator fun BitMatrix.plus(other: BitMatrix): BitMatrix {
    val new = BitMatrix(width + other.width, height)
    forEachBit { x, y, bit ->
        new[x, y] = bit
    }
    other.forEachBit { x, y, bit ->
        new[x + width, y] = bit
    }
    return new
}
