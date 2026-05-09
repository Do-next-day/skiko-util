package top.e404.skiko.handler.face

import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import top.e404.skiko.util.Colors
import top.e404.skiko.FontType
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.draw.splitByWidth
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.autoSize
import top.e404.skiko.util.fill
import top.e404.skiko.util.grayMatrix
import top.e404.skiko.util.withCanvas

@ImageHandler
object BwHandler : FramesHandler {
    override val name = "Bw"
    override val regex = Regex("(?i)bw")

    private val tf by lazy { FontType.MI_BOLD.typeface }
    private const val rate = 0.8

    private val grayPaint by lazy { Paint().apply { colorFilter = grayMatrix } }
    private val blackPaint by lazy { Paint().apply { color = Colors.BLACK.argb } }

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val l1 = args["text"] ?: "什么都没有"
        val l2 = args["s"] ?: "何もありません"
        val first = frames.first().image
        val w = first.width
        val h = first.height
        val maxW = w * 9 / 10
        // 计算字体大小
        val min = h / 14
        val max = h / 8
        var s1 = autoSize(tf, l1, min, max, maxW, 5)
        var s2 = autoSize(tf, l2, min, max, maxW, 5)

        val ss1 = s1 * rate
        val ss2 = s2 / rate
        // ss1为小字尺寸
        if (ss1 < s2) s2 = ss1.toInt()
        // ss2 为大字尺寸
        else s1 = ss2.toInt()

        val f1 = Font(tf, s1.toFloat())
        val f2 = Font(tf, s2.toFloat())
        // 按宽度拆分
        val (lines1, _) = l1.splitByWidth(maxW, f1, 0)
        val (lines2, _) = l2.splitByWidth(maxW, f2, 0)

        val spacing = s1 / 2
        val h1 = (lines1.size - 1) * (s1 / 10) + lines1.sumOf { (it.descent - it.ascent).toDouble() }.toInt()
        val h2 = (lines2.size - 1) * (s2 / 10) + lines2.sumOf { (it.descent - it.ascent).toDouble() }.toInt()
        val ph = h1 + h2 + spacing * 3

        val fh1 = f1.metrics.run { descent - ascent }
        val fh2 = f2.metrics.run { descent - ascent }

        val g = args.containsKey("g")

        return frames.result {
            common(args).handle { image ->
                Surface.makeRasterN32Premul(w, h + ph).fill(Colors.BLACK.argb).withCanvas {
                    drawImage(image, 0F, 0F, if (g) grayPaint else null)
                    lines1.forEachIndexed { index, line ->
                        val x = (w - line.width) / 2
                        val y = h + index * (fh1 + spacing) + spacing - line.ascent
                        drawTextLine(line, x, y, Paint().apply {
                            color = Colors.WHITE.argb
                            strokeWidth = s1 / 10F
                            isAntiAlias = true
                            mode = PaintMode.STROKE_AND_FILL
                        })
                        drawTextLine(line, x, y, blackPaint)
                    }
                    lines2.forEachIndexed { index, line ->
                        val x = (w - line.width) / 2
                        val y = h + lines1.size * (fh1 + spacing) + spacing + index * fh2 - line.ascent
                        drawTextLine(line, x, y, Paint().apply {
                            color = Colors.WHITE.argb
                            strokeWidth = s2 / 10F
                            isAntiAlias = true
                            mode = PaintMode.STROKE_AND_FILL
                        })
                        drawTextLine(line, x, y, blackPaint)
                    }
                }
            }
        }
    }
}

