package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.util.argb
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.util.handlePixel

/**
 * 老化
 */
@ImageHandler
object OldHandler : FramesHandler {
    override val name = "旧照片滤镜"
    override val regex = Regex("(?i)old|旧照片(滤镜)?")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).onEach { frame ->
            frame.image = frame.image.handlePixel(handler)
        }
    }

    private val handler = fun(pixel: Int): Int {
        val (a, r, g, b) = pixel.argb()
        if (a == 0) return 0
        val nb = 0.272 * r + 0.543 * g + 0.131 * b
        val ng = 0.349 * r + 0.686 * g + 0.168 * b
        val nr = 0.393 * r + 0.769 * g + 0.189 * b
        return argb(a, nr, ng, nb)
    }
}
