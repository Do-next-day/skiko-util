package top.e404.skiko.handler.face

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.jetbrains.skia.Surface
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.readJarFile
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas

@ImageHandler
object RuaHandler : FramesHandler {
    private const val size = 448
    private val bgList = (0..4).map { getJarImage("statistic/rua/$it.png") }
    private val ddList = RuaInfo.fromJar()

    override val name = "Rua"
    override val regex = Regex("(?i)rua")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.common(args).handle { round() }.replenish(5).result {
        handleIndexed { index ->
            Surface.makeRasterN32Premul(this@RuaHandler.size, this@RuaHandler.size).withCanvas {
                val i = index % 5
                ddList.face[i].draw(this, this@handleIndexed)
                ddList.hand[i].draw(this, bgList[i])
            }
        }
    }

    @Serializable
    data class RuaInfo(
        val hand: List<DrawData>,
        val face: List<DrawData>,
    ) {
        companion object {
            fun fromJar(): RuaInfo {
                val text = readJarFile("statistic/rua/rua.yml")
                return Yaml.default.decodeFromString(text)
            }
        }
    }
}