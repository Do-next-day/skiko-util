@file:Suppress("UNUSED")

package top.e404.skiko

import org.jetbrains.skia.*
import kotlin.math.PI
import kotlin.random.Random

fun getJarFileStream(path: String) = FontType::class.java.classLoader.getResourceAsStream(path)
    ?: throw IllegalArgumentException("Jar file: $path is null")

fun getJarFile(path: String) = getJarFileStream(path).use { it.readBytes() }
fun getJarImage(path: String) = Image.makeFromEncoded(getJarFile(path))
fun readJarFile(path: String) = getJarFileStream(path).use { String(it.readBytes()) }

fun Double.toRadian() = this * PI / 180
fun Float.toRadian() = this * PI / 180
fun Long.toRadian() = this * PI / 180
fun Int.toRadian() = this * PI / 180

fun <T> List<T>.choose() = get(Random.Default.nextInt(size))
fun <T> MutableList<T>.takeRandom() = removeAt(Random.Default.nextInt(size))

fun Font.descent() = metrics.descent
fun Font.top() = metrics.top
fun Font.height() = metrics.run { size + descent }

fun Bitmap.toImage() = Image.makeFromBitmap(this)
fun Image.toBitmap() = Bitmap.makeFromImage(this)

fun Bitmap.forEach(block: (x: Int, y: Int) -> Boolean) {
    for (x in 0 until width) for (y in 0 until height) {
        if (block(x, y)) return
    }
}

fun Bitmap.forEachColor(block: (color: Int) -> Boolean) {
    return forEach { x, y ->
        block(getColor(x, y))
    }
}

fun Bitmap.any(block: (color: Int) -> Boolean): Boolean {
    for (x in 0 until width) for (y in 0 until height) {
        if (block(getColor(x, y))) return true
    }
    return false
}

/**
 * 自动计算适合的宽度
 *
 * @param tf Typeface
 * @param text 文本
 * @param minSize 最小字体大小
 * @param maxSize 最大字体大小
 * @param maxWidth 最大宽度
 * @param unit 最小的增减单位
 * @return 文本宽度
 */
fun autoSize(
    tf: Typeface,
    text: String,
    minSize: Int,
    maxSize: Int,
    maxWidth: Int,
    unit: Int,
): Int {
    var size = minSize.toFloat()
    var line = TextLine.make(text, Font(tf, size))
    while (true) {
        if (size >= maxSize) return size.toInt()
        if (line.width >= maxWidth) return size.toInt() - unit
        size += unit
        line = TextLine.make(text, Font(tf, size))
    }
}