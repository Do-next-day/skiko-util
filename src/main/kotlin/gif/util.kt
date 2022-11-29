package top.e404.skiko.gif

import top.e404.skiko.blue
import top.e404.skiko.green
import top.e404.skiko.red

internal fun Int.asUnsignedShort(): Short {
    check(this in 0..0xFFFF)
    return toShort()
}

internal fun Int.asUnsignedByte(): Byte {
    check(this in 0..0xFF)
    return toByte()
}

internal fun Int.asRGBBytes() = byteArrayOf(red().toByte(), green().toByte(), blue().toByte())

fun gif(
    width: Int,
    height: Int,
    block: GIFBuilder.() -> Unit
) = GIFBuilder(width, height)
    .apply(block)
    .buildToData()