package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.util.argb
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.util.handlePixel

/**
 * 反相
 */
@ImageHandler
object ReverseHandler : FramesHandler {
    override val name = "反相"
    override val regex = Regex("(?i)反相|fx|reverse")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).onEach { frame ->
            frame.handleImage { image -> image.handlePixel(::handle) }
        }
    }

    fun handle(pixel: Int) = pixel.argb().run {
        argb(a, 255 - r, 255 - g, 255 - b)
    }
}
