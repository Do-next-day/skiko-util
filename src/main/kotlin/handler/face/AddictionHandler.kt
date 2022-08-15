package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
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
object AddictionHandler : FramesHandler {
    private val cover by lazy { getJarImage("statistic/addiction.png") }
    private const val size = 350
    private val faceRect = Rect.makeXYWH(0F, 0F, size.toFloat(), size.toFloat())

    override val name = "上瘾"
    override val regex = Regex("(?i)上瘾|addiction")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            cover.toSurface().withCanvas {
                drawImage(cover, 0F, 0F)
                drawImageRect(subCenter(), faceRect)
            }
        }
    }
}