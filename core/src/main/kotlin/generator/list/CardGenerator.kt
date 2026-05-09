package top.e404.skiko.generator.list

import org.jetbrains.skia.*
import top.e404.skiko.util.Colors
import top.e404.skiko.FontType
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.toFrames
import top.e404.skiko.generator.ImageGenerator
import top.e404.skiko.util.withCanvas
import kotlin.math.max

object CardGenerator : ImageGenerator {
    // 背景颜色
    private const val bgColor = 0xfff31e3e.toInt()

    // 点的颜色
    private const val pointColor = 0xffff899a.toInt()

    // 阴影颜色
    private const val shadowColor = 0x77333333

    // 四周边距
    private const val padding = 80

    // 点的间距
    private const val pointSpacing = 120

    // 点的大小
    private const val pointSize = 8F

    // 大字颜色
    private const val bgFontColor = 0xfff5526d.toInt()

    // 大字尺寸
    private const val bgFontSize = 800F

    // 大字字体
    private val bgFont = FontType.ZHONG_SONG.getSkiaFont(bgFontSize)

    // 小字颜色
    private val fontColor = Colors.WHITE.argb

    // 小字尺寸
    private const val focusFontSize = 180F

    // 小字字体
    private val font = FontType.LI_HEI.getSkiaFont(focusFontSize)

    override suspend fun generate(args: MutableMap<String, String>): MutableList<Frame> {
        val b = args["b"]!!
        val s = args["s"]!!
        val line = TextLine.make(s, font)
        val bgLine = TextLine.make(b, bgFont)
        // 计算图片尺寸
        val bgWidth = bgLine.width
        val width = line.width
        val w = max(bgWidth, width) + 2 * padding
        val h = bgFontSize + 2 * padding
        return Surface.makeRasterN32Premul(w.toInt(), h.toInt()).withCanvas {
            // 绘制背景和大字
            drawBgFont(w, h, bgLine)
            // 绘制点
            drawPoints(w, h)
            // 绘制小字
            drawFont(w, h, line)
        }.toFrames()
    }

    private val bgFontPaint = Paint().apply { color = bgFontColor }
    private fun Canvas.drawBgFont(w: Float, h: Float, bgLine: TextLine) {
        // 绘制背景
        val paint = Paint().apply { color = bgColor }
        drawRect(Rect.makeXYWH(0F, 0F, w, h), paint)
        // 绘制大字
        drawTextLine(
            bgLine,
            (w - bgLine.width) / 2,
            bgFontSize - 120,
            bgFontPaint
        )
    }

    private val pointPaint = Paint().apply { color = pointColor }
    private fun Canvas.drawPoints(w: Float, h: Float) {
        val wAmount = (w.toInt() - padding) / pointSpacing
        val hAmount = (h.toInt() - padding) / pointSpacing
        val xStart = (w - wAmount * pointSpacing) / 2
        val yStart = (h - hAmount * pointSpacing) / 2
        for (x in 0..wAmount) for (y in 0..hAmount) {
            drawOval(
                Rect.makeXYWH(xStart + x * pointSpacing, yStart + y * pointSpacing, pointSize, pointSize),
                pointPaint
            )
        }
    }

    private val shadowPaint = Paint().apply { color = shadowColor }
    private val fontPaint = Paint().apply { color = fontColor }
    private fun Canvas.drawFont(w: Float, h: Float, line: TextLine) {
        // 绘制小字
        val x = (w - line.width) / 2
        // 绘制小字阴影
        drawTextLine(line, x + 20, h / 2 + 70, shadowPaint)
        // 绘制小字
        drawTextLine(line, x, h / 2 + 50, fontPaint)
    }
}
