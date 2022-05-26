package top.e404.skiko.handler.face

import org.jetbrains.skia.Surface
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.pmapIndexed
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas

/**
 * 浪
 */
@ImageHandler
object SkipHandler : FramesHandler {
    private const val w = 316
    private const val h = 178
    private val range = 0..7
    private val bgList = range.map { getJarImage("statistic/skip/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/skip/skip.yml")

    override val name = "skip"
    override val regex = Regex("(?i)skip")

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
                handle {
                    Surface.makeRasterN32Premul(w, h).withCanvas {
                        drawImage(bgList[index], 0F, 0F)
                        ddList[index].draw(this, image)
                    }
                }
            }
        }
    }
}