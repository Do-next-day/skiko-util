package top.e404.skiko.handler.face

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import org.jetbrains.skia.Surface
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.*
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.readJarFile

/**
 * reo
 */
@ImageHandler
object ReoHandler : FramesHandler {
    private const val w = 480
    private const val h = 270
    private val range = 0..36
    private val bgList by lazy { range.map { getJarImage("statistic/reo/$it.png") } }
    private val ddList by lazy { Yaml.default.decodeFromString<List<List<DrawData>>>(readJarFile("statistic/reo/reo.yml")) }

    override val name = "reo"
    override val regex = Regex("(?i)reo")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        var i = 0
        frames.handle { round(27) }
        val fs = range.map {
            i++
            if (i >= frames.size) i = 0
            frames[i].clone()
        }.toMutableList()
        return fs.result {
            common(args).pmapIndexed { index ->
                duration = 80
                handle {
                    Surface.makeRasterN32Premul(w, h).withCanvas {
                        drawImage(bgList[index], 0F, 0F)
                        ddList[index].forEach { it.draw(this, image) }
                    }
                }
            }
        }
    }
}