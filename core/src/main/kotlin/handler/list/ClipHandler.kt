package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.intOrPercentage
import top.e404.skiko.util.sub

/**
 * 通过xywh裁剪图片
 */
@ImageHandler
object ClipHandler : FramesHandler {

    override val name = "裁剪"
    override val regex = Regex("(?i)(裁剪|sub|cj)(图片|img)?")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val x = args["x"]!!.intOrPercentage(-100)
        val y = args["y"]!!.intOrPercentage(-100)
        val w = args["w"]!!.intOrPercentage(-100)
        val h = args["h"]!!.intOrPercentage(-100)
        val rx = if (x < 0) frames[0].image.width * x / -100 else x
        val ry = if (y < 0) frames[0].image.height * y / -100 else y
        val rw = if (w < 0) frames[0].image.width * w / -100 else w
        val rh = if (h < 0) frames[0].image.height * h / -100 else h
        return frames.result {
            onEach {
                it.image = it.image.sub(rx, ry, rw, rh)
            }
        }
    }
}