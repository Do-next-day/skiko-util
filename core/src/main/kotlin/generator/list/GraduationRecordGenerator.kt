package top.e404.skiko.generator.list

import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import top.e404.skiko.FontType
import top.e404.skiko.frame.toFrames
import top.e404.skiko.generator.ImageGenerator
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.newSurface
import top.e404.skiko.util.withCanvas

object GraduationRecordGenerator : ImageGenerator {
    private val bg by lazy { getJarImage(this::class.java, "statistic/record.png") }
    private const val fontSize = 42F
    private val font = FontType.HEI.getSkiaFont(fontSize)
    val paint = Paint().apply { color = 0xFF979797.toInt() }
    override suspend fun generate(
        args: MutableMap<String, String>,
    ) = bg.newSurface().withCanvas {
        drawImage(bg, 0f, 0f)
        args["text"]!!.lines().forEachIndexed { index, s ->
            val line = TextLine.make(s, font)
            val w = line.width
            drawTextLine(line, bg.width - w - 80F, 490F + index * 138, paint)
        }
    }.toFrames()
}
