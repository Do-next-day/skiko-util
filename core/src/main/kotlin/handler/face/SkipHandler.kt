package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
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
    private const val count = 7
    private val range = 0..count
    private val bgList = range.map { getJarImage(this::class.java, "statistic/skip/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/skip/skip.yml")

    override val name = "skip"
    override val regex = Regex("(?i)skip")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { it.round() }.common(args).replenish(count + 1).result {
        pmapIndexed { index ->
            handleImage {
                val src = Rect.makeWH(it.width.toFloat(), it.height.toFloat())
                Surface.makeRasterN32Premul(w, h).withCanvas {
                    drawImage(bgList[index % 8], 0F, 0F)
                    ddList[index % 8].draw(this, image, src)
                }
            }
        }
    }
}
