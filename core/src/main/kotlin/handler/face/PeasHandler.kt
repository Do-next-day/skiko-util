package top.e404.skiko.handler.face

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.*

/**
 * 嫌弃
 */
@ImageHandler
object PeasHandler : FramesHandler {
    private const val w = 320
    private const val h = 164
    private const val count = 7
    private val range = 0..count
    private val bgList = range.map { getJarImage(this::class.java, "statistic/peas/$it.png") }
    private val pdList = Yaml.default.decodeFromString<List<PeasData>>(readJarFile(this::class.java, "statistic/peas/peas.yml"))
    private val bgSrcList = bgList.map { Rect.makeWH(it.width.toFloat(), it.height.toFloat()) }

    @Serializable
    private data class PeasData(
        val peas: DrawData,
        val heads: List<DrawData>,
    )

    override val name = "peas"
    override val regex = Regex("(?i)peas")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { it.round() }.common(args).replenish(count + 1).result {
        pmapIndexed { index ->
            handleImage { img ->
                val src = Rect.makeWH(img.width.toFloat(), img.height.toFloat())
                Surface.makeRasterN32Premul(w, h).withCanvas {
                    val pd = pdList[index % 8]
                    pd.peas.draw(this, bgList[index % 8], bgSrcList[index % 8])
                    pd.heads.forEach { it.draw(this, img, src) }
                }
            }
        }
    }
}
