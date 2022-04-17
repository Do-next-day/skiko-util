package top.e404.skiko.handler.list

import top.e404.skiko.*
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common

/**
 * 老化
 */
@ImageHandler
object OldHandler : FramesHandler {
    override val name = "老化"
    override val regex = Regex("(?i)老化|旧照片|old")
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