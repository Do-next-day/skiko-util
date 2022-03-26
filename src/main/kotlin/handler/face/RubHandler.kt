package top.e404.skiko.handler.face

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.*
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.rotateKeepSize
import top.e404.skiko.util.round

object RubHandler : ImageHandler {
    private const val w = 332
    private const val h = 336
    private val range = 0..4
    private val bgList = range.map { getJarImage("statistic/rub/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/rub/rub.yml")
    private val bgRect = Rect.makeWH(w.toFloat(), h.toFloat())
    private val paint = Paint().apply { color = Colors.WHITE.argb }

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
    ) = Surface.makeRasterN32Premul(w, h).run {
        val i = index % 5
        val face = image.round().rotateKeepSize(i * 360 / 5F)
        canvas.apply {
            drawRect(bgRect, paint)
            ddList[i].draw(this, face)
            bgList[i].let {
                drawImageRect(it,
                    Rect.makeWH(it.width.toFloat(), it.height.toFloat()),
                    bgRect
                )
            }
        }
        makeImageSnapshot()
    }
}