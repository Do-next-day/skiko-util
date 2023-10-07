package top.e404.skiko.generator.list

import org.jetbrains.skia.*
import top.e404.skiko.util.Colors
import top.e404.skiko.FontType
import top.e404.skiko.draw.Pointer
import top.e404.skiko.frame.Frame
import top.e404.skiko.generator.ImageGenerator
import top.e404.skiko.util.asColor
import top.e404.skiko.util.withCanvas
import kotlin.random.Random

object ShakeTextGenerator : ImageGenerator {
    private const val fontSpace = 5
    private const val padding = 10
    private const val fontSize = 60F
    private val font = FontType.YAHEI.getSkiaFont(fontSize)

    /**
     * 生成抖动gif
     *
     * @param s 文字
     * @param fontColor 文字颜色
     * @param bgColor 背景颜色
     * @param shakeSize 抖动幅度
     * @param f 生成的gif的总帧数
     * @return gif
     */
    private fun shakeGif(
        s: String,
        fontColor: Int,
        bgColor: Int,
        shakeSize: Int,
        f: Int,
    ) = (0..f).map {
        Frame(60, shake(s, fontColor, bgColor, shakeSize))
    }.toMutableList()

    /**
     * 获得一张抖动过的图片
     *
     * @param text 文本输入
     * @param fontColor 文字颜色
     * @param bgColor 背景颜色
     * @return 图片
     */
    private fun shake(text: String, fontColor: Int, bgColor: Int, shakeSize: Int): Image {
        val map = text.map {
            TextLine.make(it.toString(), font)
        }.associateWith {
            it.width
        }
        val w = fontSpace + map.values.sumOf { it.toDouble() + fontSpace }.toInt()
        return Surface.makeRasterN32Premul(
            w + 2 * (padding + shakeSize),
            fontSize.toInt() + 2 * (padding + shakeSize)
        ).run {
            val p = Pointer(padding + 5F, padding + fontSize - 10)
            val paint = Paint().apply { color = bgColor }
            withCanvas {
                drawRect(Rect.makeXYWH(0F, 0F, width.toFloat(), height.toFloat()), paint)
                for (c in map.keys) {
                    drawTextLine(c, p.x + random(shakeSize), p.y + random(shakeSize), paint.apply {
                        color = fontColor
                    })
                    p.x += map[c]!!.toInt() + fontSpace
                }
            }
        }
    }

    private fun random(shakeSize: Int) = Random.Default.nextInt(shakeSize * 2).toFloat()

    override suspend fun generate(args: MutableMap<String, String>): MutableList<Frame> {
        val text = args["text"]!!
        val color = args["color"]?.asColor() ?: Colors.BLUE_GREEN.argb
        val bg = args["bg"]?.asColor() ?: Colors.BG.argb
        val size = args["size"]?.toIntOrNull() ?: 20
        val count = args["count"]?.toIntOrNull() ?: 10
        return shakeGif(text, color, bg, size.coerceIn(1, 100), count.coerceIn(1, 20))
    }
}
