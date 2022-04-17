package top.e404.skiko.handler.list

import top.e404.bot.botutil.pmapIndexed
import top.e404.skiko.ahsb
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handlePixel
import top.e404.skiko.util.bytes
import java.io.File

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
    ): HandleResult {
        frames.replenish(10)
        val v = 1F / frames.size
        return frames.result {
            common(args).pmapIndexed { index ->
                val vv = v * index
                handle {
                    handlePixel { pixel ->
                        val (a, h, s, b) = pixel.ahsb()
                        if (a == 0) 0
                        else {
                            var e = h + vv
                            if (e > 1) e--
                            ahsb(a, e, s, b)
                        }
                    }
                }
            }
        }
    }
}