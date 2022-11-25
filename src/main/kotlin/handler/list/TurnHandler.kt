package top.e404.skiko.handler.list

import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.rotateKeepSize
import top.e404.skiko.util.round
import kotlin.math.abs

/**
 * 生成转动gif
 */
@ImageHandler
object TurnHandler : FramesHandler {
    override val name = "转动"
    override val regex = Regex("转动?|(?i)zhuan|turn|zd")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val count = args["text"]?.toIntOrNull() ?: 10
        return frames.common(args).replenish(abs(count), Frame::limitAsGif).result {
            val v = (360F / size).let { if (count < 0) -it else it }
            handleIndexed { index ->
                round().rotateKeepSize(index * v)
            }
        }
    }
}