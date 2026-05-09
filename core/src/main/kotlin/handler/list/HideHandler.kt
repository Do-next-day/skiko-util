package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.util.argb
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.fail
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.handlePixel

/**
 * 隐藏
 */
@ImageHandler
object HideHandler : FramesHandler {
    override val name = "隐藏"
    override val regex = Regex("(?i)hide|隐藏")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = if (frames.size != 1) fail("hide不支持处理gif")
    else frames.result {
        common(args).handle { it.handlePixel(handler) }
    }

    private val handler = fun(pixel: Int): Int {
        val (_, r, g, b) = pixel.argb()
        return ((0.299 * r + 0.587 * g + 0.114 * b).toLong() shl 24 or 0xffffff).toInt()
    }
}
