package top.e404.skiko.handler.list

import top.e404.skiko.ahsb
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.replenish
import top.e404.skiko.handlePixel
import top.e404.skiko.util.pmapIndexed

/**
 * RGB
 */
@ImageHandler
object RgbHandler : FramesHandler {
    override val name = "Rgb"
    override val regex = Regex("(?i)rgb")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.common(args).result {
        val size = if (size == 1) args["text"]
            ?.toIntOrNull()
            ?.let { if (it < 2) null else it }
            ?: 10 else size
        val unit = 1F / size // rgb颜色渐变的单位
        replenish(size, Frame::limitAsGif).pmapIndexed { index ->
            val addH = unit * index // 在当前帧增加的h
            handleImage {
                it.handlePixel { pixel ->
                    val (a, h, s, b) = pixel.ahsb()
                    if (a == 0) 0
                    else {
                        var e = h + addH
                        if (e > 1) e--
                        ahsb(a, e, s, b)
                    }
                }
            }
        }
    }
}