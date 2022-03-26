package top.e404.skiko.handler.filter

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.IRect
import org.jetbrains.skia.Image
import top.e404.skiko.*

object EmbossHandler : ImageHandler {
    override suspend fun handleFrame(index: Int, count: Int, image: Image, data: ExtraData?, frame: Frame): Image {
        val old = Bitmap.makeFromImage(image)
        val new = Bitmap.makeFromImage(image)
        for (x in 1 until old.width - 1) for (y in 1 until old.height - 1) {
            new.erase(old.fd(x, y), IRect.makeXYWH(x, y, 1, 1))
        }
        return Image.makeFromBitmap(new)
    }

    private fun Bitmap.fd(x: Int, y: Int): Int {
        val c = getColor(x, y)
        val a = c shr 24
        if (a == 0) return c
        val (_, pr, pg, pb) = getColor(x - 1, y - 1).argb()
        val (_, nr, ng, nb) = getColor(x + 1, y + 1).argb()
        return argb(
            a,
            (nr - pr + 128).limit(),
            (ng - pg + 128).limit(),
            (nb - pb + 128).limit(),
        )
    }
}