@file:Suppress("UNUSED")

package top.e404.skiko.util

import org.jetbrains.skia.*
import java.awt.Color
import kotlin.math.PI
import kotlin.random.Random

fun getJarFileStream(cls: Class<*>, path: String) = cls.classLoader.getResourceAsStream(path)
    ?: throw IllegalArgumentException("Jar file: $path is null")

fun getJarFile(cls: Class<*>, path: String) = getJarFileStream(cls, path).use { it.readBytes() }
fun getJarImage(cls: Class<*>, path: String) = Image.makeFromEncoded(getJarFile(cls, path))
fun readJarFile(cls: Class<*>, path: String) = getJarFileStream(cls, path).use { String(it.readBytes()) }

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
fun Image.newSurface() = Surface.makeRaster(imageInfo)

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

fun String.asColor(): Int? {
    if (startsWith("#")) {
        val s = removePrefix("#")
        return when (s.length) {
            3 -> buildString {
                for (c in s) append(c).append(c)
            }.toIntOrNull(16)?.let {
                (it + 0xff000000).toInt()
            }

            4 -> buildString {
                for (c in s) append(c).append(c)
            }.toUIntOrNull(16)?.toInt()

            6 -> s.toIntOrNull(16)?.let {
                (it + 0xff000000).toInt()
            }

            8 -> s.toUIntOrNull(16)?.toInt()

            else -> null
        }
    }
    return when (this.lowercase()) {
        "白", "white" -> Color.WHITE.rgb
        "淡灰", "lightGray", "LIGHT_GRAY" -> Color.LIGHT_GRAY.rgb
        "深灰", "darkGray", "DARK_GRAY" -> Color.DARK_GRAY.rgb
        "黑", "black" -> Color.BLACK.rgb
        "红", "red" -> Color.RED.rgb
        "粉", "pink" -> Color.PINK.rgb
        "橘黄", "orange" -> Color.ORANGE.rgb
        "黄", "yellow" -> Color.YELLOW.rgb
        "绿", "green" -> Color.GREEN.rgb
        "品红", "magenta" -> Color.MAGENTA.rgb
        "青", "cyan" -> Color.CYAN.rgb
        "蓝", "blue" -> Color.BLUE.rgb
        else -> null
    }
}

val grayMatrix = ColorFilter.makeMatrix(
    ColorMatrix(
        0.33F, 0.38F, 0.29F, 0F, 0F,
        0.33F, 0.38F, 0.29F, 0F, 0F,
        0.33F, 0.38F, 0.29F, 0F, 0F,
        0F, 0F, 0F, 1F, 0F,
    )
)

fun String?.intOrPercentage(default: Int) = when {
    this == null -> default
    endsWith("%") -> -removeSuffix("%").toInt()
    isBlank() -> default
    else -> toInt()
}

fun String?.floatOrPercentage(default: Float) = when {
    this == null -> default
    endsWith("%") -> -removeSuffix("%").toFloat()
    isBlank() -> default
    else -> toFloat()
}

fun String?.doubleOrPercentage(default: Double) = when {
    this == null -> default
    endsWith("%") -> -removeSuffix("%").toDouble()
    isBlank() -> default
    else -> toDouble()
}

fun String?.doubleOrPercentage(default: Double? = null) = when {
    this == null -> default
    endsWith("%") -> -removeSuffix("%").toDouble()
    trim() == "" -> default
    else -> toDouble()
}
