package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.rotate

/**
 * 旋转图片
 */
@ImageHandler
object RotateHandler : FramesHandler {
    override val name = "旋转"
    override val regex = Regex("旋转|(?i)xz|rotate")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val angel = args["text"]?.toFloatOrNull() ?: return HandleResult.fail("旋转角度应为数字")
        return frames.result { common(args).handle { it.rotate(angel) } }
    }
}
