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
 * 顶
 */
@ImageHandler
object UpHandler : FramesHandler {
    private const val w = 480
    private const val h = 400
    private val range = 0..9
    private val bgList = range.map { getJarImage("statistic/up/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/up/up.yml")

    override val name = "up"
    override val regex = Regex("(?i)up")

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
                duration = 80
                handle {
                    Surface.makeRasterN32Premul(w, h).withCanvas {
                        ddList.getOrNull(index)?.draw(this, image)
                        drawImage(bgList[index], 0F, 0F)
                    }
                }
            }
        }
    }
}