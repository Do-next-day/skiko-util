package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.replenish
import top.e404.skiko.util.*

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
    ) = frames.common(args).result {
        val f = args.containsKey("f") // 灰度模式
        val size = if (size == 1) args["text"]?.toIntOrNull()?.coerceIn(2, 50) ?: 10 else size
        val unit = 1F / size // rgb颜色渐变的单位
        val replenish = replenish(size, Frame::limitAsGif)
        // 针对灰度图进行处理
        if (f) {
            val ss = args["s"]?.toFloatOrNull() ?: 0.5F
            val bb = args["b"]?.toFloatOrNull() ?: 0.3F
            return@result replenish.pmapIndexed { index ->
                val (hr, hg, hb) = hsb(unit * index, ss, bb).rgb()
                handleImage {
                    it.handlePixel { pixel ->
                        val (a, r, g, b) = pixel.argb()
                        if (a == 0) return@handlePixel 0
                        argb(a, (r + hr).limit(), (g + hg).limit(), (b + hb).limit())
                    }
                }
            }
        }
        // 正常hsb处理
        replenish.pmapIndexed { index ->
            handleImage {
                it.handlePixel { pixel ->
                    val (a, h, s, b) = pixel.ahsb()
                    if (a == 0) return@handlePixel 0
                    var e = h + unit * index
                    if (e > 1) e--
                    ahsb(a, e, s, b)
                }
            }
        }
    }
}
