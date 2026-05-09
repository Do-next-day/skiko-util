package top.e404.skiko.handler.face

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.util.Colors
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas

@ImageHandler
object RubHandler : FramesHandler {
    private const val w = 332
    private const val h = 336
    private const val count = 4
    private val bgList = (0..count).map { getJarImage(this::class.java, "statistic/rub/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/rub/rub.yml")
    private val bgRect = Rect.makeWH(w.toFloat(), h.toFloat())
    private val paint = Paint().apply { color = Colors.WHITE.argb }

    override val name = "搓"
    override val regex = Regex("(?i)rub|搓|cuo")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.common(args).handle { it.round() }.replenish(count + 1).result {
        handleIndexed { index, image ->
            val src = Rect.makeWH(image.width.toFloat(), image.height.toFloat())
            val i = index % 5
            Surface.makeRasterN32Premul(w, h).withCanvas {
                drawRect(bgRect, paint)
                ddList[i].draw(this, image, src)
                drawImageRect(bgList[i], bgRect)
            }
        }
    }
}
