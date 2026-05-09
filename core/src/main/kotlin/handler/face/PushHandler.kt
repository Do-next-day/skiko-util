package top.e404.skiko.handler.face

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.util.Colors
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.rotateKeepSize
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas

/**
 * 推
 */
@ImageHandler
object PushHandler : FramesHandler {
    private const val size = 300
    private const val count = 14
    private val range = 0..count
    private val bgList = range.map { getJarImage(this::class.java, "statistic/push/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/push/push.yml")
    private val bgRect = Rect.makeWH(size.toFloat(), size.toFloat())
    private val paint = Paint().apply { color = Colors.WHITE.argb }

    override val name = "推"
    override val regex = Regex("(?i)推|tui|push")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        return frames.common(args).handle { it.round() }.replenish(count + 1).result {
            handleIndexed { index, image ->
                val src = Rect.makeWH(image.width.toFloat(), image.height.toFloat())
                Surface.makeRasterN32Premul(
                    this@PushHandler.size,
                    this@PushHandler.size
                ).withCanvas {
                    val angel = index * 360F / size
                    val i = index % 15
                    val face = image.rotateKeepSize(angel)
                    drawRect(bgRect, paint)
                    ddList[i].draw(this, face, src)
                    drawImageRect(bgList[i], bgRect)
                }
            }
        }
    }
}
