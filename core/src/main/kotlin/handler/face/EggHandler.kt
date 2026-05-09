package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.replenish
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
    private const val count = 3
    private val range = 0..count
    private val bgList = range.map { getJarImage(this::class.java, "statistic/egg/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/egg/egg.yml")

    override val name = "egg"
    override val regex = Regex("(?i)egg")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.common(args).replenish(count + 1).result {
        pmapIndexed { index ->
            handleImage {
                val src = Rect.makeWH(it.width.toFloat(), it.height.toFloat())
                Surface.makeRasterN32Premul(w, h).withCanvas {
                    ddList[index % 4].draw(this, image, src)
                    drawImage(bgList[index % 4], 0F, 0F)
                }
            }
        }
    }
}
