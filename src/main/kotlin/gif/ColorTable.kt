// 参考自 Mirai Skija Plugin by cssxsh https://github.com/cssxsh/mirai-skija-plugin
@file:Suppress("UNUSED")

package top.e404.skiko.gif

import java.nio.*

class ColorTable(
    val colors: IntArray,
    val sort: Boolean = false,
    val background: Int = (colors.size - 1).coerceAtLeast(0),
) {
    companion object {
        private val SizeList = listOf(0, 2, 4, 8, 16, 32, 64, 128, 256)
        val Empty: ColorTable = ColorTable(IntArray(0))
    }

    init {
        check(colors.size in SizeList) { "Size Not" }
        check(colors.isEmpty() || background in colors.indices) { "Background Not" }
    }

    fun write(buffer: ByteBuffer) {
        for (color in colors) {
            buffer.put(color.asRGBBytes())
        }
    }

    fun exists(): Boolean = colors.isNotEmpty()

    fun size(): Int = when (colors.size) {
        256 -> 0x07
        128 -> 0x06
        64 -> 0x05
        32 -> 0x04
        16 -> 0x03
        8 -> 0x02
        4 -> 0x01
        2 -> 0x00
        0 -> 0x00
        else -> throw IllegalArgumentException("....")
    }
}