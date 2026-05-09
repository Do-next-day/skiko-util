package top.e404.skiko.handler.list

import org.jetbrains.skia.*
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.Noise
import top.e404.skiko.util.*
import kotlin.math.*

@ImageHandler
object WarpHandler : FramesHandler {
    override val name = "扭曲"
    override val regex = Regex("扭曲|(?i)warp")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val s = args["s"].floatOrPercentage(-10F)
        val t = args["t"].floatOrPercentage(0.15F)
        val n = args["n"]?.toFloatOrNull() ?: 50F
        val count = args["text"]?.toIntOrNull()?.coerceIn(2, 50) ?: 10
        return frames.common(args).replenish(count, Frame::limitAsGif).result {
            handle { warp(it.toBitmap(), s, t, n) }
        }
    }

    private fun warp(
        src: Bitmap,
        scale: Float,
        turbulence: Float,
        noiseScale: Float
    ): Image {
        val w = src.width
        val h = src.height
        val dst = src.newBitmap()

        val sinTable = FloatArray(256)
        val cosTable = FloatArray(256)
        val s = if (scale < 0) -scale * min(src.height, src.height) / 100 else scale
        for (i in 0..255) {
            val angle = 2 * PI.toFloat() * i / 256f * turbulence
            sinTable[i] = s * sin(angle)
            cosTable[i] = s * cos(angle)
        }

        val out = FloatArray(2)
        val noise = Noise()
        for (y in 0 until h) for (x in 0 until w) {
            val displacement = (127 + 127 * noise.noise2(x / noiseScale, y / noiseScale))
                .toInt().coerceIn(0..255)
            out[0] = x + sinTable[displacement]
            out[1] = y + cosTable[displacement]
            val srcX = floor(out[0]).toInt()
            val srcY = floor(out[1]).toInt()
            val xWeight = out[0] - srcX
            val yWeight = out[1] - srcY
            val c = if (srcX in 0..w - 2 && srcY in 0..h - 2) {
                bilinearInterpolate(
                    xWeight, yWeight,
                    src.getColor(srcX, srcY),
                    src.getColor(srcX + 1, srcY),
                    src.getColor(srcX, srcY + 1),
                    src.getColor(srcX + 1, srcY + 1)
                )
            } else {
                val xx = srcX.coerceIn(0 until w)
                val xp = (srcX + 1).coerceIn(0 until w)
                val yy = srcY.coerceIn(0 until h)
                val yp = (srcY + 1).coerceIn(0 until h)
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
        val si = s.toInt()
        return dst.toImage().sub(0, 0, w - si, h - si)
    }

    /**
     * ARGB值的双线性插值
     */
    private fun bilinearInterpolate(
        x: Float,
        y: Float,
        nw: Int,
        ne: Int,
        sw: Int,
        se: Int
    ): Int {
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

