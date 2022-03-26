package top.e404.skiko.handler.face

import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.*
import top.e404.skiko.handler.IntData
import top.e404.skiko.util.subCenter

object NationalFaceHandler : ImageHandler {
    private val range = 0..5
    private val bgList = range.map { getJarImage("statistic/national/$it.png") }
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }

    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = image.subCenter().let { center ->
        Surface.makeRaster(center.imageInfo).run {
            val (id) = data as IntData
            require(id in range) { "invalid id $id" }
            canvas.apply {
                val w = center.width.toFloat()
                val h = center.height.toFloat()
                drawRect(Rect.makeWH(w, h), paint)
                drawImage(center, 0F, 0F)
                val cover = bgList[id]
                drawImageRect(cover,
                    Rect.makeWH(cover.width.toFloat(), cover.height.toFloat()),
                    Rect.makeWH(w, h))
            }
            makeImageSnapshot()
        }
    }
}