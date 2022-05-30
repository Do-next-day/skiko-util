package top.e404.skiko.handler.face

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.jetbrains.skia.Surface
import top.e404.skiko.apt.annotation.ImageHandler
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
    private val bgList = range.map { getJarImage("statistic/peas/$it.png") }
    private val pdList =
        Yaml.default.decodeFromString<List<PeasData>>(readJarFile("statistic/peas/peas.yml"))

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
    ) = frames.handle { round() }.common(args).replenish(count, Frame::limitAsGif).result {
        common(args).pmapIndexed { index ->
            handle {
                Surface.makeRasterN32Premul(w, h).withCanvas {
                    val pd = pdList[index]
                    pd.peas.draw(this, bgList[index])
                    pd.heads.forEach { it.draw(this, this@handle) }
                }
            }
        }
    }
}