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
object Hold2Handler : FramesHandler {
    private val cover = getJarImage("statistic/hold/2.png")
    private const val size = 220
    private val faceRect = Rect.makeXYWH(148F, 296F, 220F, 220F)
    private val imgRect = Rect.makeWH(cover.width.toFloat(), cover.height.toFloat())
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }

    override val name = "Hold2"
    override val regex = Regex("(?i)Hold2")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            cover.toSurface().withCanvas {
                drawRect(imgRect, paint)
                drawImageRect(subCenter(this@Hold2Handler.size), faceRect)
                drawImage(cover, 0F, 0F)
            }
        }
    }
}