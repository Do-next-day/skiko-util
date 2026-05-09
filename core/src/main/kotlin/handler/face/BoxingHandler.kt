package top.e404.skiko.handler.face

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.*

/**
 * 拳击
 */
@ImageHandler
object BoxingHandler : FramesHandler {
    private const val size = 500
    private const val count = 8
    private val list by lazy { Yaml.default.decodeFromString<List<BoxingData>>(readJarFile(this::class.java, "statistic/boxing/boxing.yml")) }
    private val hand by lazy { getJarImage(this::class.java, "statistic/boxing/fisted-hand.png") }
    private val handSrc by lazy { Rect.makeWH(hand.width.toFloat(), hand.height.toFloat()) }

    @Serializable
    private data class BoxingData(
        val head: DrawData,
        val left: DrawData,
        val right: DrawData,
    )

    override val name = "boxing"
    override val regex = Regex("(?i)boxing")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { it.round() }.common(args).replenish(count, Frame::limitAsGif).result {
        pmapIndexed { index ->
            duration = 60
            handleImage {
                val headSrc = Rect.makeWH(it.width.toFloat(), it.height.toFloat())
                Surface.makeRasterN32Premul(
                    BoxingHandler.size,
                    BoxingHandler.size
                ).withCanvas {
                    list[index % 8].head.draw(this, image, headSrc)
                    list[index % 8].left.draw(this, hand, handSrc)
                    list[index % 8].right.draw(this, hand, handSrc)
                }
            }
        }
    }
}
