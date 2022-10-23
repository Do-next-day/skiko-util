package top.e404.skiko.handler.list

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.IRect
import org.jetbrains.skia.ImageInfo
import top.e404.skiko.Argb
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.argb
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.limit
import top.e404.skiko.util.toBitmap
import top.e404.skiko.util.toImage
import kotlin.math.abs

/**
 * 彩色边缘效果
 */
@ImageHandler
object ColorfulEdgeHandler : FramesHandler {
    override val name = "彩色边缘"
    override val regex = Regex("(?i)彩色边缘|ColorfulEdge|ce")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            val rst = Bitmap().also {
                it.allocPixels(
                    ImageInfo(
                        width = imageInfo.width - 2,
                        height = imageInfo.height - 2,
                        colorType = imageInfo.colorType,
                        alphaType = imageInfo.colorAlphaType,
                        colorSpace = imageInfo.colorSpace
                    )
                )
            }
            val ic = ImageColors(toBitmap())
            for (x in 1 until width - 1) for (y in 1 until height - 1) {
                rst.erase(fd(ic, x, y), IRect.makeXYWH(x - 1, y - 1, 1, 1))
            }
            rst.toImage()
        }
    }

    private class ImageColors(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val argb = ArrayList<Argb>(width * height).also {
            for (y in 0 until height) for (x in 0 until width) {
                it.add(bitmap.getColor(x, y).argb())
            }
        }

        operator fun get(x: Int, y: Int) = argb[y * width + x]
    }

    /*
     * int r1 = this.img.red[i-1+(j+1)*w]
     *   +2*this.img.red[i+(j+1)*w]
     *   +this.img.red[i+1+(j+1)*w]
     *   -this.img.red[i-1+(j-1)*w]
     *   -2*this.img.red[i+(j-1)*w]
     *   -this.img.red[i+1+(j-1)*w];
     * int r2 = this.img.red[i+1+(j-1)*w]
     *   +2*this.img.red[i+1+(j)*w]
     *   +this.img.red[i+1+(j+1)*w]
     *   -this.img.red[i-1+(j-1)*w]
     *   -2*this.img.red[i-1+(j)*w]
     *   -this.img.red[i-1+(j+1)*w];
     * int g1 = this.img.green[i-1+(j+1)*w]+2*this.img.green[i+(j+1)*w]+this.img.green[i+1+(j+1)*w]-this.img.green[i-1+(j-1)*w]-2*this.img.green[i+(j-1)*w]-this.img.green[i+1+(j-1)*w];
     * int g2 = this.img.green[i+1+(j-1)*w]+2*this.img.green[i+1+(j)*w]+this.img.green[i+1+(j+1)*w]-this.img.green[i-1+(j-1)*w]-2*this.img.green[i-1+(j)*w]-this.img.green[i-1+(j+1)*w];
     * int b1 = this.img.blue[i-1+(j+1)*w]+2*this.img.blue[i+(j+1)*w]+this.img.blue[i+1+(j+1)*w]-this.img.blue[i-1+(j-1)*w]-2*this.img.blue[i+(j-1)*w]-this.img.blue[i+1+(j-1)*w];
     * int b2 = this.img.blue[i+1+(j-1)*w]+2*this.img.blue[i+1+(j)*w]+this.img.blue[i+1+(j+1)*w]-this.img.blue[i-1+(j-1)*w]-2*this.img.blue[i-1+(j)*w]-this.img.blue[i-1+(j+1)*w];
     *
     * int tr = (Math.abs(r1)+Math.abs(r2));
     * int tg = (Math.abs(g1)+Math.abs(g2));
     * int tb = (Math.abs(b1)+Math.abs(b2));
     *
     * this.img.data[i + j * this.img.w] = (255 << 24) | (math.st(tr) << 16) | (math.st(tg) << 8) | math.st(tb);
     */
    private fun fd(ic: ImageColors, x: Int, y: Int): Int {
        val xl = x - 1
        val xr = x + 1
        val yu = y - 1
        val yd = y + 1
        val lu = ic[xl, yu]
        val cu = ic[x, yu]
        val ru = ic[xr, yu]
        val lc = ic[xl, y]
        val rc = ic[xr, y]
        val ld = ic[xl, yd]
        val cd = ic[x, yd]
        val rd = ic[xr, yd]
        val r1 = abs(ld.r + cd.r + lc.r + rd.r - lu.r - cu.r - rc.r - ru.r)
        val g1 = abs(ld.g + cd.g + lc.g + rd.g - lu.g - cu.g - rc.g - ru.g)
        val b1 = abs(ld.b + cd.b + lc.b + rd.b - lu.b - cu.b - rc.b - ru.b)
        val r2 = abs(ru.r + cu.r + lc.r + lu.r - rd.r - cd.r - rc.r - ld.r)
        val g2 = abs(ru.g + cu.g + lc.g + lu.g - rd.g - cd.g - rc.g - ld.g)
        val b2 = abs(ru.b + cu.b + lc.b + lu.b - rd.b - cd.b - rc.b - ld.b)

        return argb(
            ic[x, y].a,
            (r1 + r2).limit(),
            (g1 + g2).limit(),
            (b1 + b2).limit(),
        )
    }
}