@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.Pointer
import top.e404.skiko.util.Colors

/**
 * 代表一个支持不同文本不同颜色的文本对象
 *
 * @property content 文本及其颜色
 * @property font 字体
 * @property color 字体颜色
 * @property udPadding 行间距, 上一个元素到首行以及末行到下一个元素的间距为1/2行间距
 * @property left 左侧边距
 * @property textIndent 若为true则启用行首缩进(两个空格的宽度)
 * @property center 居中, 若居中则忽略left
 */
open class TextColorful(
    var content: MutableList<Pair<String, Int>>,
    var font: Font,
    var color: Int = Colors.WHITE.argb,
    var udPadding: Int = 20,
    var left: Int = 0,
    var textIndent: Boolean = false,
    var center: Boolean = true
) : DrawElement {
    private val lines = mutableListOf<LineInfo>()

    data class LineInfo(val line: String, val color: Int, var next: Boolean) {
        /**
         * 从左到右截取一段绘制长度不超过[widthLimit]的最长的字符, 在调用此方法前应处理换行符
         *
         * @param widthLimit 宽度
         * @param font 字体
         * @return 剩下的字符, 若截取全部则second为null
         */
        fun splitByWidth(widthLimit: Float, font: Font): Pair<LineInfo, LineInfo?> {
            // 长度足够
            if (font.measureTextWidth(line) < widthLimit) return this to null

            val index = getSplitIndex(widthLimit, font, 0, line.length)
            return LineInfo(line.substring(0, index), color, true) to LineInfo(line.substring(index), color, next)
        }

        private fun getSplitIndex(widthLimit: Float, font: Font, startIndex: Int, endIndex: Int): Int {
            if (startIndex == endIndex) return startIndex
            val half = startIndex + (endIndex - startIndex) / 2
            if (half == startIndex || half == endIndex) return startIndex
            val current = font.measureTextWidth(line.substring(0, half))
            return if (current > widthLimit) getSplitIndex(widthLimit, font, startIndex, half)
            else getSplitIndex(widthLimit, font, half, endIndex)
        }
    }

    override fun size(minWidth: Int, maxWidth: Int): Pair<Float, Float> {
        if (!center && textIndent) content.add(0, "　　" to 0)
        val left = if (center) 0 else left

        var currentMaxWidth = 0F

        // 拆分换行符
        for ((text, color) in content) {
            val list = text.split("\n").map { LineInfo(it, color, true) }
            list.last().next = false
            lines.addAll(list)
        }

        // 按长度拆分
        var i = 0
        var w = maxWidth.toFloat()
        while (i < lines.size - 1) {
            i++
            val c = lines[i]
            val (l1, l2) = c.splitByWidth(w, font)
            // 若l2为空, 则上一行无需拆分
            if (l2 == null) {
                w -= font.measureTextWidth(l1.line)
                if (maxWidth - w > currentMaxWidth) currentMaxWidth = maxWidth - w
                continue
            }
            // 上一行拆分了, 把当前行加入到下一行
            lines.add(i, l1)
            // 重置宽度
            w = maxWidth.toFloat()
        }

        val height = lines.count { it.next } * (font.metrics.run { descent - ascent } + udPadding)
        // 计算位置


        return currentMaxWidth + left to height
//        // 计算每段的位置
//        for ((text, color) in content) {
//
//            // 处理有换行符的
//            if ('\n' in text) {
//                // 没有超过最大长度, 直接绘制
//                val w = font.measureTextWidth(text)
//                if (pointer.x + w < currentMaxWidth) {
//                    cache.add(LineCache(pointer.x, pointer.y, TextLine.make(text, font), color, true).also { println(it) })
//                    if (w > currentMaxWidth) currentMaxWidth = w
//                    pointer.x += w
//                    // 额外换行
//                    continue
//                }
//
//                // 超过最大长度 拆分
//                var startIndex = 0
//                var endIndex = text.length
//                var loop = 0
//                while (true) {
//                    // 从后往前减字符, 直到绘制的下
//                    val weight = font.measureTextWidth(text.substring(startIndex, endIndex))
//                    if (pointer.x + weight > currentMaxWidth) {
//                        endIndex--
//                        loop++
//                        continue
//                    }
//                    // 缓存
//                    val line = TextLine.make(text.substring(startIndex, endIndex), font)
//                    cache.add(LineCache(pointer.x, pointer.y, line, color, endIndex == text.length).also { println(it) })
//                    if (w > currentMaxWidth) currentMaxWidth = w
//                    // 完成
//                    if (endIndex == text.length) break
//
//                    // 设置下一段字符串范围
//                    startIndex = endIndex
//                    endIndex = text.length
//                    // 位置换行
//                    if (loop != 0) {
//                        pointer.x = left.toFloat()
//                        pointer.y += font.metrics.run { descent - ascent }
//                        loop = 0
//                    } else {
//                        pointer.x += weight
//                    }
//                }
//                continue
//            }
//
//            // 没有超过最大长度, 直接绘制
//            val w = font.measureTextWidth(text)
//            if (pointer.x + w < currentMaxWidth) {
//                cache.add(LineCache(pointer.x, pointer.y, TextLine.make(text, font), color, false).also { println(it) })
//                if (w > currentMaxWidth) currentMaxWidth = w
//                pointer.x += w
//                continue
//            }
//
//            // 超过最大长度 拆分
//            var startIndex = 0
//            var endIndex = text.length
//            var loop = 0
//            while (true) {
//                // 从后往前减字符, 直到绘制的下
//                val weight = font.measureTextWidth(text.substring(startIndex, endIndex))
//                if (pointer.x + weight > currentMaxWidth) {
//                    endIndex--
//                    loop++
//                    continue
//                }
//                // 缓存
//                val line = TextLine.make(text.substring(startIndex, endIndex), font)
//                cache.add(LineCache(pointer.x, pointer.y, line, color, false).also { println(it) })
//                if (w > currentMaxWidth) currentMaxWidth = w
//                // 完成
//                if (endIndex == text.length) break
//                // 设置下一段字符串范围
//                startIndex = endIndex
//                endIndex = text.length
//                // 位置换行
//                if (loop != 0) {
//                    pointer.x = left.toFloat()
//                    pointer.y += font.metrics.run { descent - ascent }
//                    loop = 0
//                } else {
//                    pointer.x += weight
//                }
//            }
//        }
//
//        val height = pointer.y
//        return currentMaxWidth + left to height
    }

    override fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
        debug: Boolean
    ) {
        val x = pointer.x
        pointer.y += udPadding / 2 - font.metrics.ascent
        for ((text, color, next) in lines) {
            val line = TextLine.make(text, font)
            canvas.drawTextLine(
                line,
                if (center) (width + imagePadding * 2 - line.width) / 2 else pointer.x + left,
                pointer.y,
                paint.also { it.color = color }
            )
            if (next) {
                pointer.x = x
                pointer.y += font.metrics.run { descent - ascent } + udPadding
            } else {
                pointer.x += line.width
            }
        }
        pointer.x = x
        pointer.y += udPadding / 2 + font.metrics.ascent
    }
}
