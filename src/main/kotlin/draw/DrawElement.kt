@file:Suppress("UNUSED")

package top.e404.skiko.draw

import org.jetbrains.skia.*
import top.e404.skiko.Colors
import top.e404.skiko.util.bytes
import kotlin.math.max

interface DrawElement {
    companion object {
        @JvmStatic
        val debugPaint = Paint().apply {
            color = Colors.LIGHT_BLUE.argb
        }
    }

    fun size(minWidth: Int, maxWidth: Int): Pair<Int, Int>
    fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
        debug: Boolean = false
    )
}

private val debugPatin = Paint().apply {
    color = 0x77ff00ff
}

/**
 * 通过绘图组件绘制图片
 *
 * @param imagePadding 外边框
 * @param bgColor 背景颜色
 * @param minWidth 最小宽度
 * @param maxWidth 最大宽度
 * @param radius 图片圆角
 * @return png格式的图片数据
 */
fun List<DrawElement>.toImage(
    imagePadding: Int = 50,
    bgColor: Int = Colors.BG.argb,
    minWidth: Int = 500,
    maxWidth: Int = 1000,
    radius: Float = 50F,
    debug: Boolean = false
): ByteArray {
    var width = 0
    var height = 0
    val sizes = map { it.size(minWidth, maxWidth) }

    for ((w, h) in sizes) {
        width = max(width, w)
        height += h
    }

    val pointer = Pointer(imagePadding, imagePadding)
    val paint = Paint().apply {
        isAntiAlias = true
    }
    val surface = Surface.makeRasterN32Premul(width + 2 * imagePadding, height + 2 * imagePadding)
    surface.canvas.apply {
        // bg
        drawRRect(
            RRect.makeXYWH(
                l = 0f,
                t = 0f,
                w = surface.width.toFloat(),
                h = surface.height.toFloat(),
                radius = radius
            ),
            paint.apply { color = bgColor }
        )

        // debug
        if (debug) drawRect(
            Rect(
                left = imagePadding.toFloat(),
                top = imagePadding.toFloat(),
                right = width.toFloat() + imagePadding,
                bottom = height.toFloat() + imagePadding
            ), debugPatin
        )

        // 内容
        for (drawable in this@toImage) drawable.drawToBoard(
            canvas = this,
            pointer = pointer,
            paint = paint,
            width = width,
            imagePadding = imagePadding,
            debug = debug
        )
    }
    return surface.bytes()
}

data class Pointer(var x: Int, var y: Int)

/**
 * 拆分字符串(先按\n分割, 之后按长度限制分割)
 *
 * @param maxWidth 允许的最大宽度
 * @param font 字体
 * @return <待绘制的文本列表, 最终宽度>
 */
fun String.splitByWidth(maxWidth: Int, font: Font, left: Int): Pair<ArrayList<TextLine>, Int> {
    fun String.s(maxWidth: Int, font: Font): Pair<ArrayList<TextLine>, Int> {
        var width = 0
        val list = ArrayList<TextLine>()
        var text = this
        w@ while (text != "") {
            for (i in text.indices) { // 遍历计算字符串宽度(从小到大)
                // 生成裁剪后的line并判断宽度
                if (TextLine.make(text.substring(0, i), font).width + left > maxWidth) {
                    // 若 line + left 超出最大宽度
                    // 则设置宽度为最大宽度, 并且裁剪
                    width = maxWidth
                    list.add(TextLine.make(text.substring(0, i - 1), font))
                    text = text.substring(i - 1, text.length)
                    continue@w
                }
            }
            // 这一行长度未超过最大宽度
            val line = TextLine.make(text, font)
            list.add(line)
            width = max(width, line.width.toInt())
            text = ""
        }
        return Pair(list, width)
    }

    var width = 0
    val lines = ArrayList(split("\n") // 按换行拆分
        .map { it.s(maxWidth, font) } // 按最大宽度拆分
        .also { width = it.map { pair -> pair.second }.sortedDescending()[0] }
        .flatMap { it.first })
    return Pair(lines, width)
}

/**
 * 生成[TextLine], 有宽度限制, 超出限制部分会省略为`...`
 *
 * @param maxWidth 最大宽度
 * @param font 字体
 * @return TextLine
 */
fun String.toLine(maxWidth: Int, font: Font): TextLine {
    var line: TextLine
    var end = length
    var width: Float
    do {
        var text = substring(0, end--)
        if (text.length != length) text = "$text..."
        line = TextLine.make(text, font)
        width = line.width + font.size
    } while (width > maxWidth)
    return line
}