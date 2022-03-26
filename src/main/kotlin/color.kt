@file:Suppress("UNUSED")

package top.e404.skiko

import org.jetbrains.skia.Bitmap
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
    LIGHT_BLUE(0xFF0077FF.toInt()),
    BLUE(0xFF00FFFF.toInt()),
    PURPLE(0xFF7700FF.toInt()),
    MAGENTA(0xFFFF00FF.toInt()),
    PURPLE_RED(0xFFFF0077.toInt());
}

fun Image.handlePixel(data: ExtraData?, handler: (Int, ExtraData?) -> Int): Image {
    val bitmap = Bitmap.makeFromImage(this)
    for (x in 0 until bitmap.width) for (y in 0 until bitmap.height) {
        val color = handler.invoke(bitmap.getColor(x, y), data)
        bitmap.erase(color, IRect.makeXYWH(x, y, 1, 1))
    }
    return Image.makeFromBitmap(bitmap)
}

private val range = 0..255
fun Double.limit() = this.toInt().coerceIn(range)
fun Int.limit() = coerceIn(range)

data class Argb(var a: Int, var r: Int, var g: Int, var b: Int)

fun Int.argb() = Argb(alpha(), red(), green(), blue())
fun Int.rgb() = Triple(red(), green(), blue())
fun Int.alpha() = (toLong() shr 24).toInt()
fun Int.red() = this and 0xff0000 shr 16
fun Int.green() = this and 0xff00 shr 8
fun Int.blue() = this and 0xff

fun rgb(r: Int, g: Int, b: Int) = (r shl 16) or (g shl 8) or b
fun argb(a: Int, r: Int, g: Int, b: Int) = (a shl 24) or (r shl 16) or (g shl 8) or b
fun argb(a: Int, r: Double, g: Double, b: Double) = argb(a, r.limit(), g.limit(), b.limit())
fun ahsb(a: Int, h: Float, s: Float, v: Float) = Color.HSBtoRGB(h, s, v) or (a shl 24)

data class Ahsb(var a: Int, var h: Float, var s: Float, var b: Float)

fun Int.ahsb(): Ahsb {
    val (a, r, g, b) = argb()
    return Color.RGBtoHSB(r, g, b, null).let {
        Ahsb(a, it[0], it[1], it[2])
    }
}
