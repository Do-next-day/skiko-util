package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.resize

@ImageHandler
object DpxHandler : FramesHandler {
    override val name = "动态像素画"
    override val regex = Regex("(?i)dpx|dPixel")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val count = args["text"]?.toIntOrNull() ?: 10
        if (args.containsKey("r")) return frames.onEach {
            it.limitAsGif()
        }.result {
            val replenish = common(args).replenish(count * 2 - 1, Frame::limitAsGif)
            val size = replenish.size
            replenish.onEachIndexed { index, frame ->
                val by = if (index < size / 2) 1 + index else size - index
                frame.image = frame.image.run {
                    resize(
                        width / by,
                        height / by,
                        true
                    ).resize(
                        width,
                        height,
                        true
                    )
                }
            }
        }
        return frames.onEach {
            it.limitAsGif()
        }.result {
            common(args).replenish(count).onEachIndexed { index, frame ->
                frame.image = frame.image.run {
                    resize(
                        width / (1 + index),
                        height / (1 + index),
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