package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.util.round

@ImageHandler
object RoundHandler : FramesHandler {
    override val name = "Round"
    override val regex = Regex("(?i)round")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).onEach { frame ->
            frame.handleImage { image -> image.round() }
        }
    }
}
