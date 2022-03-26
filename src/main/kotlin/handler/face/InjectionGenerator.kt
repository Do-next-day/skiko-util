package top.e404.skiko.handler.face

import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.*
import top.e404.skiko.util.round

object InjectionHandler : ImageHandler {
    private val bg = getJarImage("statistic/injection.png")
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
        val round = image.round()
        canvas.apply {
            drawRect(bgRect, paint)
            val rr = Rect.makeWH(round.width.toFloat(), round.height.toFloat())
            drawImageRect(round, rr, Rect.makeXYWH(150F, 90F, 100F, 105F))
            drawImageRect(round, rr, Rect.makeXYWH(85F, 85F, 110F, 110F))
            drawImage(bg, 0F, 0F)
        }
        makeImageSnapshot()
    }
}