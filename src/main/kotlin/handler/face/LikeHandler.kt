package top.e404.skiko.handler.face

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import top.e404.skiko.Colors
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.subCenter
import top.e404.skiko.util.toSurface
import top.e404.skiko.util.withCanvas

@ImageHandler
object LikeHandler : FramesHandler {
    private val cover by lazy { getJarImage("statistic/like.png") }
    private const val size = 370
    private val faceRect = Rect.makeXYWH(402F, 364F, size.toFloat(), size.toFloat())
    private val imgRect = Rect.makeWH(cover.width.toFloat(), cover.height.toFloat())
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }

    override val name = "喜欢"
    override val regex = Regex("(?i)喜欢|like")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            cover.toSurface().withCanvas {
                drawRect(imgRect, paint)
                drawImageRect(subCenter(), faceRect)
                drawImage(cover, 0F, 0F)
            }
        }
    }
}