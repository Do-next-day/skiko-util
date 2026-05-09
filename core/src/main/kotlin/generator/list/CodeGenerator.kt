package top.e404.skiko.generator.list

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface
import org.jetbrains.skia.TextLine
import top.e404.skiko.util.Argb
import top.e404.skiko.util.Colors
import top.e404.skiko.FontType
import top.e404.skiko.util.argb
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.toFrames
import top.e404.skiko.generator.ImageGenerator
import top.e404.skiko.util.choose
import top.e404.skiko.util.fill
import top.e404.skiko.util.withCanvas
import kotlin.math.abs
import kotlin.random.Random

object CodeGenerator : ImageGenerator {
    private const val padding = 50
    private const val fontSize = 150
    private const val fontSpacing = 30
    private val font = FontType.MINECRAFT.getSkiaFont(fontSize.toFloat())
    private val chars = "qwertyuipadfghjkzxcvbnmWERTYUIPADFGHJKLZXCVBNM23478".toCharArray().toList()

    fun genCodeText(length: Int = 4): String {
        require(length > 0) { "length must > 0" }
        return buildString {
            repeat(length) { append(chars.choose()) }
        }
    }

    fun Argb.similar(other: Argb) = abs(r - other.r) + abs(g - other.g) + abs(b - other.b) < 150

    fun genColor(blackList: MutableList<Argb>): Argb {
        var r: Int
        var g: Int
        var b: Int
        var c: Argb
        do {
            r = Random.nextInt(255)
            g = Random.nextInt(255)
            b = Random.nextInt(255)
            c = Argb(0xff, r, g, b)
        } while (c.similar(blackList))
        blackList.add(c)
        return c
    }

    /**
     * 检查相似颜色
     *
     * @param blackList 颜色列表
     * @return 若存在相似则返回true
     */
    private fun Argb.similar(blackList: List<Argb>): Boolean {
        if (blackList.any { similar(it) }) return true
        return false
    }

    override suspend fun generate(args: MutableMap<String, String>): MutableList<Frame> {
        val text = args["text"] ?: genCodeText(4)
        val width = text.length * (fontSize + fontSpacing) - fontSpacing + 2 * padding
        val height = fontSize + 2 * padding
        val paint = Paint()
        return Surface.makeRasterN32Premul(width, height).apply {
            fill(Colors.WHITE.argb)
        }.withCanvas {
            val black = mutableListOf(Colors.WHITE.argb.argb())
            for ((i, c) in text.withIndex()) {
                val color = genColor(black).run { argb(a, r, g, b) }
                save()
                val dx = padding * 2 + i * (fontSize + fontSpacing) - fontSpacing
                val dy = padding + fontSize
                translate(dx.toFloat(), dy.toFloat())
                rotate(Random.nextFloat() * 60 - 30)
                val line = TextLine.make(c.toString(), font)
                drawTextLine(line, 0F, 0F, paint.apply { this.color = color })
                restore()
            }
        }.toFrames()
    }
}
