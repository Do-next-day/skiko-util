// 参考自 Mirai Skija Plugin by cssxsh https://github.com/cssxsh/mirai-skija-plugin
@file:Suppress("UNUSED")

package top.e404.skiko.gif

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Data
import java.nio.ByteBuffer

internal fun Int.asUnsignedShort(): Short {
    check(this in 0..0xFFFF)
    return toShort()
}

internal fun Int.asUnsignedByte(): Byte {
    check(this in 0..0xFF)
    return toByte()
}

internal fun Int.asRGBBytes(): ByteArray {
    return byteArrayOf(
        (this and 0xFF0000 shr 16).toByte(),
        (this and 0x00FF00 shr 8).toByte(),
        (this and 0x0000FF).toByte()
    )
}

fun Bitmap.toColorTable() = ColorTable(OctTreeQuantizer.quantize(this, 256))

fun gif(width: Int, height: Int, block: GIFBuilder.() -> Unit): Data {
    return GIFBuilder(width, height)
        .apply(block)
        .data()
}

fun buildGif(width: Int, height: Int, block: GIFBuilder.() -> Unit): ByteArray {
    val buffer = ByteBuffer.allocate(1 shl 23)
    GIFBuilder(width, height)
        .apply(block)
        .build(buffer = buffer)
    return buffer.flip().array()
}