package top.e404.skiko.handler.face

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.jetbrains.skia.Surface
import top.e404.skiko.apt.annotation.ImageHandler
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
    private val range = 0..7
    private val list by lazy { Yaml.default.decodeFromString<List<BoxingData>>(readJarFile("statistic/boxing/boxing.yml")) }
    private val hand by lazy { getJarImage("statistic/boxing/fisted-hand.png") }

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
    ): HandleResult {
        var i = 0
        frames.handle { round() }
        val fs = range.map {
            i++
            if (i >= frames.size) i = 0
            frames[i].clone()
        }.toMutableList()
        return fs.result {
            common(args).pmapIndexed { index ->
                duration = 60
                handle {
                    Surface.makeRasterN32Premul(
                        BoxingHandler.size,
                        BoxingHandler.size
                    ).withCanvas {
                        list[index].head.draw(this, image)
                        list[index].left.draw(this, hand)
                        list[index].right.draw(this, hand)
                    }
                }
            }
        }
    }
}