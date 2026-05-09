package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import top.e404.skiko.util.Colors
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.*

@ImageHandler
object NationalFaceHandler : FramesHandler {
    private val range = 0..5
    private val coverList = range.map { getJarImage(this::class.java, "statistic/national/$it.png") }

    override val name = "国庆头像"
    override val regex = Regex("(?i)(国庆|gq)(头像|face|tx)")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val i = args["text"]?.toIntOrNull() ?: 0
        val cover = coverList[i]
        return frames.result {
            common(args).handle {
                val center = it.subCenter()
                center.newSurface().apply {
                    fill(Colors.WHITE.argb)
                }.withCanvas {
                    val w = center.width.toFloat()
                    val h = center.height.toFloat()
                    drawImage(center, 0F, 0F)
                    drawImageRect(
                        cover,
                        Rect.makeWH(cover.width.toFloat(), cover.height.toFloat()),
                        Rect.makeWH(w, h)
                    )
                }
            }
        }
    }
}
