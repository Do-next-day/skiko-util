package top.e404.skiko.generator.list

import org.jetbrains.skia.*
import top.e404.skiko.util.Colors
import top.e404.skiko.FontType
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.toFrames
import top.e404.skiko.generator.ImageGenerator
import top.e404.skiko.util.autoSize
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.withCanvas
import kotlin.math.min

object GoodNewsGenerator : ImageGenerator {
    private val bg = getJarImage(this::class.java, "statistic/goodnews.jpg")
    private val maxWidth = (bg.width * .9).toInt()
    private const val minSize = 20
    private const val maxSize = 100
    private const val unit = 10
    private val tf = FontType.MI.typeface

    override suspend fun generate(args: MutableMap<String, String>): MutableList<Frame> {
        val text = args["text"]!!
        var size = args["size"]?.toInt()
        var stroke = args["stroke"]?.toInt()
        val texts = text.split("\n")
        if (size == null) size = texts.minOf { autoSize(tf, it, minSize, maxSize, maxWidth, unit) }
        //val heightLimit = bg.height * 10 / (11 * texts.size)
        //size = min(heightLimit, size)
        val spacing = min(size / 3, 30)
        val font = Font(tf, size.toFloat())
        val lines = texts.map { TextLine.make(it, font) }
        val d = font.metrics.descent
        stroke = stroke ?: (size / 10)
        val height = (spacing + size) * texts.size - spacing
        val startY = (bg.height - height) / 2
        val paint = Paint()
        return Surface.makeRaster(bg.imageInfo).withCanvas {
            drawImage(bg, 0F, 0F)
            for ((i, t) in lines.withIndex()) {
                val x = (bg.width - t.width) / 2
                val y = startY + (spacing + size) * (i + 1F) - spacing - d
                drawTextLine(t, x, y, paint.apply {
                    color = Colors.YELLOW.argb
                    strokeWidth = stroke.toFloat()
                    isAntiAlias = true
                    mode = PaintMode.STROKE_AND_FILL
                })
                drawTextLine(t, x, y, paint.apply {
                    mode = PaintMode.FILL
                    color = Colors.RED.argb
                })
            }
        }.toFrames()
    }
}
