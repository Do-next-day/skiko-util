package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.util.resize

@ImageHandler
object PxHandler : FramesHandler {
    override val name = "像素画"
    override val regex = Regex("(?i)px|pixel")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val scale = args["text"]?.toIntOrNull() ?: 10
        return frames.result {
            common(args).onEach { frame ->
                frame.image = frame.image.run {
                    resize(
                        width / scale,
                        height / scale,
                        true
                    ).resize(
                        width,
                        height,
                        true
                    )
                }
            }
        }
    }
}