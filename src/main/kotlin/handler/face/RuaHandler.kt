package top.e404.skiko.handler.face

import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import top.e404.skiko.*
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.round

object RuaHandler : ImageHandler {
    private const val size = 448
    private val range = 0..4
    private val bgList = range.map { getJarImage("statistic/rua/$it.png") }
    private val ddList = RuaInfo.fromJar()

    override suspend fun handle(bytes: ByteArray, data: ExtraData?, duration: Int?): HandlerResult {
        var frames = bytes.decodeToFrames()
        val result = kotlin.runCatching {
            if (frames.size == 1) frames = (1..5).map { frames[0].clone() }
            frames.apply {
                runBlocking {
                    forEachIndexed { index, frame ->
                        duration?.let { frame.duration = it }
                        launch {
                            frame.handleAsImage(index, frames.size, data, ::handleFrame)
                        }
                    }
                }
            }.encodeToBytes()
        }
        return HandlerResult(
            true,
            result.isSuccess,
            result.getOrNull(),
            result.exceptionOrNull()
        )
    }

    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = Surface.makeRasterN32Premul(size, size).run {
        val i = index % 5
        val face = image.round()
        ddList.face[i].draw(canvas, face)
        ddList.hand[i].draw(canvas, bgList[i])
        makeImageSnapshot()
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