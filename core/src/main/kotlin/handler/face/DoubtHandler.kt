package top.e404.skiko.handler.face

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import top.e404.skiko.util.Colors
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.*

@ImageHandler
object DoubtHandler : FramesHandler {
    private val cover = getJarImage(this::class.java, "statistic/doubt.png")
    private const val size = 167
    private val faceRect = Rect.makeXYWH(86F, 272F, size.toFloat(), size.toFloat())
    private val imgRect = Rect.makeWH(cover.width.toFloat(), cover.height.toFloat())
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }

    override val name = "疑惑"
    override val regex = Regex("(?i)疑惑|yh|doubt")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            cover.newSurface().withCanvas {
                drawRect(imgRect, paint)
                val center = it.subCenter()
                drawImageRectNearest(center, Rect.makeWH(center.width.toFloat(), center.height.toFloat()), faceRect)
                drawImage(cover, 0F, 0F)
            }
        }
    }
}
