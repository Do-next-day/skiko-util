@file:Suppress("UNUSED")

package top.e404.skiko.util

import org.jetbrains.skia.IRect
import org.jetbrains.skia.Image
import java.awt.Color

enum class Colors(val argb: Int) {
    BG(0xFF1F1B1D.toInt()),

    WHITE(0xFFFFFFFF.toInt()),
    LIGHT_GRAY(0xFFC0C0C0.toInt()),
    GRAY(0xFF808080.toInt()),
    DARK_GRAY(0xFF404040.toInt()),
    BLACK(0xFF000000.toInt()),

    PINK(0xFFFFAFAF.toInt()),
    RED(0xFFFF0000.toInt()),
    ORANGE(0xFFFF7700.toInt()),
    YELLOW(0xFFFFFF00.toInt()),
    YELLOW_GREEN(0xFF77FF00.toInt()),
    GREEN(0xFF00FF00.toInt()),
    BLUE_GREEN(0xFF00FF77.toInt()),
    CYAN(0xFF00FFFF.toInt()),
    LIGHT_BLUE(0xff00A6B3.toInt()),
    BLUE(0xFF0000FF.toInt()),
    PURPLE(0xFF7700FF.toInt()),
    MAGENTA(0xFFFF00FF.toInt()),
    PURPLE_RED(0xFFFF0077.toInt());
}

fun Image.handlePixel(block: (Int) -> Int): Image {
    val bitmap = toBitmap()
    val result = newBitmap()
    for (x in 0 until result.width) for (y in 0 until result.height) {
        val color = block(bitmap.getColor(x, y))
        result.erase(color, IRect.makeXYWH(x, y, 1, 1))
    }
    return result.toImage()
}

private val pixelColorRange = 0..255
fun Double.limit() = toInt().coerceIn(pixelColorRange)
fun Int.limit() = coerceIn(pixelColorRange)

data class Argb(var a: Int, var r: Int, var g: Int, var b: Int)

fun Int.argb() = Argb(alpha(), red(), green(), blue())
fun Int.rgb() = Triple(red(), green(), blue())
fun Int.alpha() = (toLong() shr 24).toInt()
fun Int.red() = this and 0xff0000 shr 16
fun Int.green() = this and 0xff00 shr 8
fun Int.blue() = this and 0xff

fun Triple<Int, Int, Int>.toFloat() = Triple(first.toFloat(), second.toFloat(), third.toFloat())

fun rgb(r: Int, g: Int, b: Int) = (r shl 16) or (g shl 8) or b
fun argb(a: Int, r: Int, g: Int, b: Int) = (a shl 24) or (r shl 16) or (g shl 8) or b
fun argb(a: Int, r: Double, g: Double, b: Double) = argb(a, r.limit(), g.limit(), b.limit())
fun hsb(h: Float, s: Float, b: Float) = Color.HSBtoRGB(h, s, b)
fun ahsb(a: Int, h: Float, s: Float, b: Float) = Color.HSBtoRGB(h, s, b) or (a shl 24)

data class Ahsb(var a: Int, var h: Float, var s: Float, var b: Float) {
    fun editSaturation(block: (Float) -> Float) = ahsb(a, h, block(s), b)
}

data class Hsb(var h: Float, var s: Float, var b: Float) {
    fun editSaturation(block: (Float) -> Float) = hsb(h, block(s), b)
}

fun Int.ahsb(): Ahsb {
    val (a, r, g, b) = argb()
    return Color.RGBtoHSB(r, g, b, null).let {
        Ahsb(a, it[0], it[1], it[2])
    }
}

fun Int.hsb(): Hsb {
    val (r, g, b) = rgb()
    return Color.RGBtoHSB(r, g, b, null).let {
        Hsb(it[0], it[1], it[2])
    }
}

fun gray(r: Int, g: Int, b: Int) = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

fun genColor(start: Int, end: Int, count: Int): MutableList<Int> {
    val (sr, sg, sb) = start.rgb()
    val (er, eg, eb) = end.rgb()
    val rUnit = (er - sr) / (count - 1)
    val gUnit = (eg - sg) / (count - 1)
    val bUnit = (eb - sb) / (count - 1)
    return ArrayList<Int>(count).apply {
        add(start)
        for (i in 1..(count - 2)) {
            val r = sr + rUnit * i
            val g = sg + gUnit * i
            val b = sb + bUnit * i
            add(rgb(r, g, b))
        }
        add(end)
    }
}
