package top.e404.skiko.handler.face

import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.*
import top.e404.skiko.util.subCenter

object LongMingHandler : ImageHandler {
    private val bg = getJarImage("statistic/lm.png")
    private val bgRect = Rect.makeWH(bg.width.toFloat(), bg.height.toFloat())
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }

    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = Surface.makeRaster(bg.imageInfo).run {
        canvas.apply {
            drawRect(bgRect, paint)
            val face = image.subCenter()
            drawImageRect(face,
                Rect.makeWH(face.width.toFloat(), face.height.toFloat()),
                Rect.makeXYWH(228F, 126F, 234F, 234F))
            drawImage(bg, 0F, 0F)
        }
        makeImageSnapshot()
    }
}