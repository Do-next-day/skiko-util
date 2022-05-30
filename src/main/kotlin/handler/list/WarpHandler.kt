package top.e404.skiko.handler.list

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.IRect
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.Noise
import top.e404.skiko.util.toBitmap
import top.e404.skiko.util.toImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

@ImageHandler
object WarpHandler : FramesHandler {
    override val name = "扭曲"
    override val regex = Regex("(?i)扭曲|warp")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val x = args["x"]?.toFloatOrNull() ?: 50F
        val y = args["y"]?.toFloatOrNull() ?: 50F
        val t = args["t"]?.toFloatOrNull() ?: 0.15F
        var i = 0
        frames.common(args)
        val fs = (0..20).map {
            i++
            if (i >= frames.size) i = 0
            frames[i].clone()
        }.toMutableList()
        return fs.result {
            withCanvas { image ->
                drawImage(warp(image.toBitmap(), x, y, t).toImage(), 0F, 0F)
            }
        }
    }

    private fun warp(
        src: Bitmap,
        xScale: Float,
        yScale: Float,
        turbulence: Float
    ): Bitmap {
        val sinTable = FloatArray(256)
        val cosTable = FloatArray(256)
        for (i in 0..255) {
            val angle = 2 * PI.toFloat() * i / 256f * turbulence
            sinTable[i] = (-yScale * sin(angle.toDouble())).toFloat()
            cosTable[i] = (yScale * cos(angle.toDouble())).toFloat()
        }

        val width = src.width
        val height = src.height
        val dst = Bitmap().apply {
            allocPixels(src.imageInfo)
            setAlphaType(ColorAlphaType.PREMUL)
        }
        val out = FloatArray(2)

        val noise = Noise()
        for (y in 0 until height) for (x in 0 until width) {
            val displacement = (127 + 127 * noise.noise2(x / xScale, y / xScale))
                .toInt().coerceIn(0..255)
            out[0] = x + sinTable[displacement]
            out[1] = y + cosTable[displacement]
            val srcX = floor(out[0].toDouble()).toInt()
            val srcY = floor(out[1].toDouble()).toInt()
            val xWeight = out[0] - srcX
            val yWeight = out[1] - srcY
            val c = if (srcX in 0..width - 2 && srcY in 0..height - 2) {
                var xx = srcX + 1
                var yy = srcY
                if (xx > width - 2) {
                    xx = 1
                    yy++
                }
                bilinearInterpolate(
                    xWeight, yWeight,
                    src.getColor(srcX, srcY),
                    src.getColor(xx, yy),
                    src.getColor(srcX, srcY + 1),
                    src.getColor(xx, ++yy)
                )
            } else {
                val xx = srcX.coerceIn(0 until width)
                val xp = (srcX + 1).coerceIn(0 until width)
                val yy = srcY.coerceIn(0 until height)
                val yp = (srcY + 1).coerceIn(0 until height)
                bilinearInterpolate(
                    xWeight, yWeight,
                    src.getColor(xx, yy),
                    src.getColor(xp, yy),
                    src.getColor(xx, yp),
                    src.getColor(xp, yp)
                )
            }
            dst.erase(c, IRect.makeXYWH(x, y, 1, 1))
        }
        return dst
    }

    /**
     * ARGB值的双线性插值
     */
    private fun bilinearInterpolate(x: Float, y: Float, nw: Int, ne: Int, sw: Int, se: Int): Int {
        var m0: Float
        var m1: Float
        val a0 = nw shr 24 and 0xff
        val r0 = nw shr 16 and 0xff
        val g0 = nw shr 8 and 0xff
        val b0 = nw and 0xff
        val a1 = ne shr 24 and 0xff
        val r1 = ne shr 16 and 0xff
        val g1 = ne shr 8 and 0xff
        val b1 = ne and 0xff
        val a2 = sw shr 24 and 0xff
        val r2 = sw shr 16 and 0xff
        val g2 = sw shr 8 and 0xff
        val b2 = sw and 0xff
        val a3 = se shr 24 and 0xff
        val r3 = se shr 16 and 0xff
        val g3 = se shr 8 and 0xff
        val b3 = se and 0xff
        val cx = 1.0f - x
        val cy = 1.0f - y
        m0 = cx * a0 + x * a1
        m1 = cx * a2 + x * a3
        val a = (cy * m0 + y * m1).toInt()
        m0 = cx * r0 + x * r1
        m1 = cx * r2 + x * r3
        val r = (cy * m0 + y * m1).toInt()
        m0 = cx * g0 + x * g1
        m1 = cx * g2 + x * g3
        val g = (cy * m0 + y * m1).toInt()
        m0 = cx * b0 + x * b1
        m1 = cx * b2 + x * b3
        val b = (cy * m0 + y * m1).toInt()
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }
}

