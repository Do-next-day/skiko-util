package top.e404.skiko.handler.face

import org.jetbrains.skia.Surface
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.pmapIndexed
import top.e404.skiko.util.withCanvas

/**
 * 磕蛋
 */
@ImageHandler
object EggHandler : FramesHandler {
    private const val w = 272
    private const val h = 212
    private val range = 0..3
    private val bgList = range.map { getJarImage("statistic/egg/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/egg/egg.yml")

    override val name = "egg"
    override val regex = Regex("(?i)egg")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        var i = 0
        val fs = range.map {
            i++
            if (i >= frames.size) i = 0
            frames[i].clone()
        }.toMutableList()
        return fs.result {
            common(args).pmapIndexed { index ->
                handle {
                    Surface.makeRasterN32Premul(w, h).withCanvas {
                        ddList[index].draw(this, image)
                        drawImage(bgList[index], 0F, 0F)
                    }
                }
            }
        }
    }
}