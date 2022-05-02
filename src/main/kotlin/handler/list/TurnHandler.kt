package top.e404.skiko.handler.list

import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.rotateKeepSize
import top.e404.skiko.util.round

/**
 * 生成转动gif
 */
@ImageHandler
object TurnHandler : FramesHandler {
    override val name = "转动"
    override val regex = Regex("(?i)转动|turn|zd")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val count = args["text"]?.toIntOrNull() ?: 10
        return frames
            .common(args)
            .replenish(count, Frame::limitAsGif)
            .result {
                val v = 360F / size
                handleIndexed { index ->
                    round().rotateKeepSize(index * v)
                }
            }
    }
}