package top.e404.skiko.handler.face

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 附魔特效
 */
@ImageHandler
object EnchantHandler : FramesHandler {
    private val bg by lazy { getJarImage(this::class.java, "statistic/enchant.png") }

    override val name = "enchant"
    override val regex = Regex("附魔|(?i)enchant")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val count = if (frames.size != 1) frames.size
        else args["c"]?.toIntOrNull()?.coerceIn(2, 100) ?: 40
        return frames.common(args).replenish(count, Frame::limitAsGif).result {
            val image = frames.first().image
            val size = min(max(image.width, image.height), 400) * 3
            val resize = bg.resize(size, size)
            val unit = size / (count - 1)
            pmapIndexed { index ->
                limitAsGif(400F)
                also {
                    it.image = it.image.newSurface().withCanvas {
                        drawImage(this@pmapIndexed.image, 0F, 0F)
                        drawImage(resize, 0f, unit * (count - 1 - index).toFloat())
                        drawImage(resize, 0f, unit * (count - 1 - index).toFloat() - size)
                    }
                }
            }
        }
    }
}
