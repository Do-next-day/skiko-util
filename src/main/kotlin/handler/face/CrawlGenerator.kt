package top.e404.skiko.handler.face

import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import top.e404.skiko.*
import top.e404.skiko.handler.ImageData
import top.e404.skiko.util.round

object CrawlGenerator : ImageHandler {
    private const val path = "statistic/crawl/{i}.jpg"
    private val range = 1..73
    private val map = HashMap<Int, Image>()

    private fun getBg(index: Int) = map.getOrPut(index) {
        getJarImage(path.replace("{i}", index.toString()))
    }

    override suspend fun handle(bytes: ByteArray, data: ExtraData?, duration: Int?): HandlerResult {
        return handleFrames(bytes.decodeToFrames(), ImageData(getBg(range.random())), duration)
    }

    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ): Image {
        val (bg) = data as ImageData
        val size = bg.height / 5
        val face = image.round(size)
        return Surface.makeRaster(bg.imageInfo).run {
            canvas.apply {
                drawImage(bg, 0F, 0F)
                drawImage(face, 0F, height - size.toFloat())
            }
            makeImageSnapshot()
        }
    }
}