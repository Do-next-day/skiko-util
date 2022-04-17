package top.e404.skiko.handler.face

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import top.e404.skiko.Colors
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.subCenter
import top.e404.skiko.util.toSurface
import top.e404.skiko.util.withCanvas

@ImageHandler
object Hold1Handler : FramesHandler {
    private val cover = getJarImage("statistic/hold/1.png")
    private const val size = 160
    private val faceRect = Rect.makeXYWH(28F, 215F, 160F, 160F)
    private val imgRect = Rect.makeWH(cover.width.toFloat(), cover.height.toFloat())
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }

    override val name = "抱1"
    override val regex = Regex("(?i)抱1?|bao1?")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            cover.toSurface().withCanvas {
                drawRect(imgRect, paint)
                drawImageRect(subCenter(this@Hold1Handler.size), faceRect)
                drawImage(cover, 0F, 0F)
            }
        }
    }
}