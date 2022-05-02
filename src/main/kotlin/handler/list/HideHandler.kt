package top.e404.skiko.handler.list

import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.argb
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.handlePixel

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
    ) = frames.result {
        common(args).handle { handlePixel(handler) }
    }

    private val handler = fun(pixel: Int): Int {
        val (_, r, g, b) = pixel.argb()
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt() shl 24 or 0xffffff
    }
}