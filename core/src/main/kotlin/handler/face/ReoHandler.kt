package top.e404.skiko.handler.face

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.*

/**
 * reo
 */
@ImageHandler
object ReoHandler : FramesHandler {
    private const val w = 480
    private const val h = 270
    private const val count = 36
    private val range = 0..count
    private val bgList by lazy { range.map { getJarImage(this::class.java, "statistic/reo/$it.png") } }
    private val ddList by lazy { Yaml.default.decodeFromString<List<List<DrawData>>>(readJarFile(this::class.java, "statistic/reo/reo.yml")) }

    override val name = "Reo"
    override val regex = Regex("(?i)reo")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { it.round(27) }.common(args).replenish(count + 1).result {
        pmapIndexed { index ->
            duration = 80
            handleImage {
                val src = Rect.makeWH(it.width.toFloat(), it.height.toFloat())
                Surface.makeRasterN32Premul(w, h).withCanvas {
                    drawImage(bgList[index], 0F, 0F)
                    ddList[index].forEach { it.draw(this, image, src) }
                }
            }
        }
    }
}
