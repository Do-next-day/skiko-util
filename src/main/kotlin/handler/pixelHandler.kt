package top.e404.skiko.handler

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.IRect
import org.jetbrains.skia.Image
import top.e404.skiko.ExtraData
import java.awt.Color


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
fun Int.alpha() = (toLong() shr 24).toInt()
fun Int.red() = this and 0xff0000 shr 16
fun Int.green() = this and 0xff00 shr 8
fun Int.blue() = this and 0xff

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

