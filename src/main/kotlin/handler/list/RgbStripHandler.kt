package top.e404.skiko.handler.list

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.IRect
import top.e404.skiko.ahsb
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.replenish
import top.e404.skiko.util.pmapIndexed
import top.e404.skiko.util.toBitmap
import top.e404.skiko.util.toImage

/**
 * 带状RGB
 */
@ImageHandler
object RgbStripHandler : FramesHandler {
    override val name = "RgbStrip"
    override val regex = Regex("(?i)rgbstrip")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.common(args).replenish(10, Frame::limitAsGif).result {
        val img = first().image
        val wid = img.width
        val hei = img.height
        val unitWidth = wid / size // 变换的单位宽度
        val horizontal = args.containsKey("h") // 水平
        val reverse = args.containsKey("r") // 反向
        pmapIndexed { index ->
            val uw = unitWidth * (if (reverse) size - index else index) // 变换起点宽度
            handleImage {
                val bitmap = it.toBitmap()
                val result = Bitmap().apply {
                    allocPixels(it.imageInfo)
                    setAlphaType(ColorAlphaType.PREMUL)
                }
                if (horizontal) for (y in 0 until hei) {
                    val uy = (y + uw) % hei
                    val uh = y.toFloat() / hei
                    for (x in 0 until wid) {
                        val pixel = bitmap.getColor(x, uy)
                        var (a, h, s, b) = pixel.ahsb()
                        if (a == 0) {
                            result.erase(0, IRect.makeXYWH(x, uy, 1, 1))
                            continue
                        }
                        h = (h + uh) % 1
                        result.erase(ahsb(a, h, s, b), IRect.makeXYWH(x, uy, 1, 1))
                    }
                } else for (x in 0 until wid) {
                    val ux = (x + uw) % wid
                    val uh = x.toFloat() / wid
                    for (y in 0 until hei) {
                        val pixel = bitmap.getColor(ux, y)
                        var (a, h, s, b) = pixel.ahsb()
                        if (a == 0) {
                            result.erase(0, IRect.makeXYWH(ux, y, 1, 1))
                            continue
                        }
                        h = (h + uh) % 1
                        result.erase(ahsb(a, h, s, b), IRect.makeXYWH(ux, y, 1, 1))
                    }
                }
                result.toImage()
            }
        }
    }
}