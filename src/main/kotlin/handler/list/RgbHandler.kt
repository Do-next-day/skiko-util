package top.e404.skiko.handler.list

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Image
import top.e404.skiko.*
import top.e404.skiko.handler.FloatData
import top.e404.skiko.handler.IntData

object RgbHandler : ImageHandler {
    override suspend fun handle(bytes: ByteArray, data: ExtraData?, duration: Int?): HandlerResult {
        val rd = IntData((data as IntData).data)
        var frames = bytes.decodeToFrames()
        val result = kotlin.runCatching {
            if (frames.size == 1) {
                if (rd.data < 2) rd.data = 10
                frames = (1..rd.data).map { frames[0].clone() }
            }
            frames.apply {
                val v = 1F / size
                runBlocking(IO) {
                    forEachIndexed { index, frame ->
                        launch {
                            duration?.let { frame.duration = it }
                            frame.apply {
                                limitAsGif()
                                handleAsImage(index, size, FloatData(v * index), ::handleFrame)
                            }
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
    ) = image.handlePixel(data, handler)

    val handler = fun(pixel: Int, data: ExtraData?): Int {
        val (a, h, s, b) = pixel.ahsb()
        if (a == 0) return pixel
        val (v) = data as FloatData
        return ahsb(a, h + v, s, b)
    }
}