package top.e404.skiko.handler.list

import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.sub

/**
 * 通过xywh裁剪图片
 */
@ImageHandler
object ClipHandler : FramesHandler {

    override val name = "裁剪"
    override val regex = Regex("(?i)裁剪|cj|sub")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val x = args["x"]!!.toInt()
        val y = args["y"]!!.toInt()
        val w = args["w"]!!.toInt()
        val h = args["h"]!!.toInt()
        return frames.result {
            onEach {
                it.image = it.image.sub(x, y, w, h)
            }
        }
    }
}