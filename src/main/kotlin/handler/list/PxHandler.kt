package top.e404.skiko.handler.list

import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.util.resize

@ImageHandler
object PxHandler : FramesHandler {
    override val name = "像素化"
    override val regex = Regex("(?i)像素化|px|pixel")
    private val fail = HandleResult.fail("scale应为大于0的整数")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val scale = args["scale"]?.toIntOrNull() ?: return fail
        return frames.result {
            common(args).onEach { frame ->
                frame.image = frame.image.run {
                    resize(
                        width / scale,
                        height / scale,
                        false
                    ).resize(
                        width,
                        height,
                        false
                    )
                }
            }
        }
    }
}