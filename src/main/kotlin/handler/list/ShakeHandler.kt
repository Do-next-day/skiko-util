package top.e404.skiko.handler.filter

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.*
import top.e404.skiko.handler.IntData

object ShakeHandler : ImageHandler {
    override suspend fun handle(bytes: ByteArray, data: ExtraData?, duration: Int?): HandlerResult {
        var frames = bytes.decodeToFrames()
        val result = kotlin.runCatching {
            if (frames.size == 1) frames = (1..12).map { frames[0].clone() }
            frames.apply {
                runBlocking {
                    forEachIndexed { index, frame ->
                        launch {
                            duration?.let { frame.duration = it }
                            frame.apply {
                                limitAsGif()
                                handleAsImage(index, size, data, ::handleFrame)
                            }
                        }
                    }
                }
            }.encodeToBytes()
        }
        return HandlerResult(
            frames.size > 1,
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
    ): Image {
        val (v) = data as IntData
        val w = image.width.toFloat()
        val h = image.height.toFloat()
        val rect = when (index % 4) {
            0 -> Rect.makeXYWH(0F, 0F, w, h)
            1 -> Rect.makeXYWH(v.toFloat(), 0F, w, h)
            2 -> Rect.makeXYWH(0F, v.toFloat(), w, h)
            else -> Rect.makeXYWH(v.toFloat(), v.toFloat(), w, h)
        }
        return Surface.makeRasterN32Premul(image.width + v, image.height + v).run {
            canvas.drawImageRect(image, rect)
            makeImageSnapshot()
        }
    }
}