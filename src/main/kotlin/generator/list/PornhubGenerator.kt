package top.e404.skiko.generator.list

import org.jetbrains.skia.*
import top.e404.skiko.Colors
import top.e404.skiko.ExtraData
import top.e404.skiko.FontType
import top.e404.skiko.generator.ImageGenerator
import top.e404.skiko.handler.StringPairData

object PornhubGenerator : ImageGenerator {
    private const val space = 30F
    private const val height = 170
    private const val radius = 20F
    private val font = FontType.MI_BOLD.getSkijaFont(70F)
    override suspend fun generate(data: ExtraData?): Image {
        val (s1, s2) = data as StringPairData

        val lineLeft = TextLine.make(s1, font)
        val lineRight = TextLine.make(s2, font)
        val lenLeft = lineLeft.width
        val lenRight = lineRight.width
        val width = space * 3.5 + lenLeft + lenRight
        return Surface.makeRasterN32Premul(width.toInt(), height).run {
            canvas.apply {
                val paint = Paint()
                // bg
                drawRRect(
                    RRect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat(), radius),
                    paint.apply { color = Colors.BLACK.value }
                )
                // left
                paint.color = 0xffffffff.toInt()
                drawTextLine(lineLeft, space, 108F, paint)
                // right bg
                paint.color = 0xffff9000.toInt()
                drawRRect(
                    RRect.makeXYWH(45 + lenLeft, 40F, lenRight + 30, 90F, radius),
                    paint
                )
                // right line
                paint.color = Colors.BLACK.value
                drawTextLine(lineRight, space * 2 + lenLeft, 108F, paint)
            }
            makeImageSnapshot()
        }
    }
}