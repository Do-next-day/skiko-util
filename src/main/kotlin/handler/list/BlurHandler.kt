@file:Suppress("UNUSED")

package top.e404.skiko.handler.filter

import org.jetbrains.skia.*
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler

object BlurHandler : ImageHandler {
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = Surface.makeRasterN32Premul(image.width, image.height).run {
        val (sigmaX, sigmaY) = data as BlurData
        canvas.drawImage(image, 0F, 0F, Paint().apply {
            imageFilter = ImageFilter.makeBlur(sigmaX, sigmaY, FilterTileMode.REPEAT)
        })
        makeImageSnapshot()
    }

    data class BlurData(
        val sigmaX: Float,
        val sigmaY: Float,
    ) : ExtraData
}

