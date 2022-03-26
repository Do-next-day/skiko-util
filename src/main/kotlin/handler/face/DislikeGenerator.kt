package top.e404.skiko.handler.face

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import top.e404.skiko.*
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.round

object DislikeGenerator : ImageHandler {
    private const val w = 307
    private const val h = 414
    private const val duration = 60
    private val range = 0..30
    private val bgList = range.map { getJarImage("statistic/dislike/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/dislike/dislike.yml")

    override suspend fun handleFrames(frames: List<Frame>, data: ExtraData?, duration: Int?): HandlerResult {
        var i = 0
        frames.forEach {
            it.bitmap = it.bitmap.toImage().round().toBitmap()
            it.duration = duration ?: this.duration
        }
        val fs = range.map {
            i++
            if (i >= frames.size) i = 0
            frames[i].clone()
        }
        return kotlin.runCatching {
            runBlocking {
                fs.forEachIndexed { index, frame ->
                    duration?.let { frame.duration = it }
                    launch {
                        frame.handleAsImage(index, frames.size, data, ::handleFrame)
                    }
                }
            }
            fs.encodeToBytes()
        }.run {
            HandlerResult(
                fs.size > 1,
                isSuccess,
                getOrNull(),
                exceptionOrNull()
            )
        }
    }

    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ): Image {
        return Surface.makeRasterN32Premul(w, h).run {
            canvas.apply {
                drawImage(bgList[index], 0F, 0F)
                ddList[index].draw(this, image)
            }
            makeImageSnapshot()
        }
    }
}