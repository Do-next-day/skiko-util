@file:Suppress("UNUSED")

package top.e404.skiko.draw

import org.jetbrains.skia.*
import top.e404.skiko.util.Colors
import top.e404.skiko.util.bytes
import top.e404.skiko.util.withCanvas
import kotlin.math.max

/**
 * 一个可以被绘制的元素, 非线程安全, 不建议复用对象
 *
 * 使用时先调用[size]再调用[drawToBoard]
 */
interface DrawElement {
    companion object {
        @JvmStatic
        val debugPaint = Paint().apply {
            color = Colors.LIGHT_BLUE.argb
        }
    }

    /**
     * 计算该元素的尺寸
     *
     * @param minWidth 该元素允许的最小宽度
     * @param maxWidth 该元素允许的最大宽度
     * @return 最终计算的尺寸
     */
    fun size(minWidth: Int, maxWidth: Int): Pair<Float, Float>

    /**
     * 实际绘制到canvas
     *
     * @param canvas 绘制的canvas
     * @param pointer 传入的当前位置
     * @param paint 传入的Paint
     * @param width 宽度
     * @param imagePadding padding
     * @param debug 是否开启debug
     */
    fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
        debug: Boolean = false,
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
fun Iterable<DrawElement>.toImage(
    imagePadding: Int = 50,
    bgColor: Int = Colors.BG.argb,
    minWidth: Int = 500,
    maxWidth: Int = 1000,
    radius: Float = 50F,
    debug: Boolean = false,
): ByteArray {
    var width = 0
    var height = 0
    val sizes = map { it.size(minWidth, maxWidth) }

    for ((w, h) in sizes) {
        width = max(width, w.toInt())
        height += h.toInt()
    }

    val pointer = Pointer(imagePadding.toFloat(), imagePadding.toFloat())
    val paint = Paint().apply {
        isAntiAlias = true
    }
    val surface = Surface.makeRasterN32Premul(width + 2 * imagePadding, height + 2 * imagePadding)
    return surface.withCanvas {
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
        for (drawable in this@toImage) {
            if (debug) {
                val before = pointer.copy()
                println("\nbefore: $before")
                drawable.drawToBoard(
                    canvas = this,
                    pointer = pointer,
                    paint = paint,
                    width = width,
                    imagePadding = imagePadding,
                    debug = true
                )
                println("after: $pointer")
                println("move: ${pointer.copy() - before}")
                continue
            }
            drawable.drawToBoard(
                canvas = this,
                pointer = pointer,
                paint = paint,
                width = width,
                imagePadding = imagePadding,
                debug = false
            )
        }
    }.bytes()
}

data class Pointer(var x: Float, var y: Float) {
    operator fun plus(other: Pointer) = apply {
        x += other.x
        y += other.y
    }

    operator fun minus(other: Pointer) = apply {
        x -= other.x
        y -= other.y
    }
}

/**
 * 拆分字符串(先按\n分割, 之后按长度限制分割)
 *
 * @param maxWidth 允许的最大宽度
 * @param font 字体
 * @return <待绘制的文本列表, 最终宽度>
 */
fun String.splitByWidth(maxWidth: Int, font: Font, left: Int): Pair<MutableList<TextLine>, Float> {
    fun String.s(maxWidth: Int, font: Font): Pair<MutableList<TextLine>, Float> {
        var width = 0F
        val list = ArrayList<TextLine>()
        var text = this
        w@ while (text != "") {
            for (i in text.indices) { // 遍历计算字符串宽度(从小到大)
                // 生成裁剪后的line并判断宽度
                if (TextLine.make(text.substring(0, i), font).width + left > maxWidth) {
                    // 若 line + left 超出最大宽度
                    // 则设置宽度为最大宽度, 并且裁剪
                    width = maxWidth.toFloat()
                    list.add(TextLine.make(text.substring(0, i - 1), font))
                    text = text.substring(i - 1, text.length)
                    continue@w
                }
            }
            // 这一行长度未超过最大宽度
            val line = TextLine.make(text, font)
            list.add(line)
            width = max(width, line.width)
            text = ""
        }
        return list to width
    }

    var width = 0F
    val lines = split("\n") // 按换行拆分
        .map { it.s(maxWidth, font) } // 按最大宽度拆分
        .also { width = it.map { pair -> pair.second }.sortedDescending()[0] }
        .flatMap { it.first }
        .toMutableList()
    return lines to width
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
