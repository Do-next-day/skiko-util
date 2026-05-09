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
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.readJarFile
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas

@ImageHandler
object RuaHandler : FramesHandler {
    private const val size = 448
    private const val count = 4
    private val bgList = (0..count).map { getJarImage(this::class.java, "statistic/rua/$it.png") }
    private val ddList = RuaInfo.fromJar()
    private val bgSrc = Rect.makeWH(bgList[0].width.toFloat(), bgList[0].height.toFloat())

    override val name = "rua"
    override val regex = Regex("(?i)rua")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.common(args).handle { it.round() }.replenish(count + 1).result {
        handleIndexed { index, image ->
            val src = Rect.makeWH(image.width.toFloat(), image.height.toFloat())
            Surface.makeRasterN32Premul(this@RuaHandler.size, this@RuaHandler.size).withCanvas {
                val i = index % 5
                ddList.face[i].draw(this, image, src)
                ddList.hand[i].draw(this, bgList[i], bgSrc)
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
                val text = readJarFile(this::class.java, "statistic/rua/rua.yml")
                return Yaml.default.decodeFromString(text)
            }
        }
    }
}
