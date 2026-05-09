package top.e404.skiko

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.*
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.encodeToBytes
import top.e404.skiko.util.resize
import top.e404.skiko.util.sub
import top.e404.skiko.util.withCanvas
import java.io.File
import kotlin.test.Test

class TestJump {
    @Test
    fun jump() = runBlocking(Dispatchers.IO) {
        // 16*16
        val center = File("MACHINE_WHITE.png").readBytes().let { Image.makeFromEncoded(it) }
        val side = File("MACHINE_WHITE_SIDE.png").readBytes().let { Image.makeFromEncoded(it) }

        gen(center, side).let {
            File("out.gif").writeBytes(it)
        }
    }

    private val compressList = listOf(0, 1, 2, 3, 3, 4, 4, 4, 4, 4, 4, 3, 3, 2, 1, 0)
    private fun gen(img1: Image, img2: Image): ByteArray {
        val size = 4
        val left = 20F

        fun surface() = Surface.makeRasterN32Premul(120, 120)
        fun Canvas.shadow(w: Int, h: Int) = drawOval(Rect.makeXYWH((120 - w) / 2F, 100F - h, w.toFloat(), h.toFloat()), Paint().apply { color = 0xff777777.toInt() })
        val rotateW = 54
        val rotate6 = surface().withCanvas {
            val top = 10F
            val r = 12
            // 侧面
            drawImage(img2.resize(r, 64, true), left + r / 2 - 2 + 5, top)
            // 正面
            drawImage(img1.resize(rotateW, 64, true), left + r / 2 + r - 2 + 5, top)
            // 阴影
            shadow(68, 12)
        }
        val rotate5 = surface().withCanvas {
            val top = 5F
            val r = 8
            // 侧面
            drawImage(img2.resize(r, 64, true), left + r / 2 + 5, top)
            // 正面
            drawImage(img1.resize(rotateW, 64, true), left + r / 2 + r + 5, top)
            // 阴影
            shadow(66, 11)
        }
        val rotate4 = surface().withCanvas {
            val top = 3F
            val r = 4
            // 侧面
            drawImage(img2.resize(r, 64, true), left + r / 2 + 5, top)
            // 正面
            drawImage(img1.resize(rotateW, 64, true), left + r / 2 + r + 5, top)
            // 阴影
            shadow(64, 10)
        }
        val rotate3 = surface().withCanvas {
            val top = 3F
            val r = 12
            // 正面
            drawImage(img1.resize(rotateW, 64, true), left + r / 2 + 5, top)
            // 侧面
            drawImage(img2.resize(16 - r, 64, true), left + r / 2 + rotateW + 5, top)
            // 阴影
            shadow(64, 10)
        }
        val rotate2 = surface().withCanvas {
            val top = 5F
            val r = 8
            // 正面
            drawImage(img1.resize(rotateW, 64, true), left + r / 2 + 5, top)
            // 侧面
            drawImage(img2.resize(16 - r, 64, true), left + r / 2 + rotateW + 5, top)
            // 阴影
            shadow(66, 11)
        }
        val rotate1 = surface().withCanvas {
            val top = 10F
            val r = 4
            // 正面
            drawImage(img1.resize(rotateW, 64, true), left + r / 2 + 2 + 5, top)
            // 侧面
            drawImage(img2.resize(16 - r, 64, true), left + r / 2 + 2 + rotateW + 5, top)
            // 阴影
            shadow(68, 12)
        }

        val normalLeft = surface().withCanvas {
            val top = 15F
            // 正面
            drawImage(img1.resize(64, 64, true), left, top)
            // 侧面
            drawImage(img2.resize(16, 64, true), left + 16 * size, top)
            // 阴影
            shadow(80, 13)
        }
        val normalDownLeft = surface().withCanvas {
            val top = 15F
            // 正面
            drawImage(img1.resize(64, 64, true).run {
                Surface.makeRasterN32Premul(64, 70).withCanvas {
                    drawImage(sub(0, 0, 20, 64), 0F, 0F)
                    drawImage(sub(20, 0, 20, 64), 20F, 3F)
                    drawImage(sub(40, 0, 24, 64), 40F, 6F)
                }
            }, left, top - 6)
            // 侧面
            drawImage(img2.resize(16, 64, true), left + 16 * size, top)
            // 阴影
            shadow(80, 13)
        }
        val fat1Left = surface().withCanvas {
            val top = 20F
            // 正面
            drawImage(img1.resize(64, 64, true), left, top)
            // 侧面
            drawImage(img2.resize(16, 64, true), left + 16 * size, top)
            // 阴影
            shadow(80, 13)
        }
        val fat1DownLeft = surface().withCanvas {
            val top = 20F
            // 正面
            // drawImage(img1.resize(68, 64, true), left - 5, top)
            drawImage(img1.resize(64, 64, true).run {
                Surface.makeRasterN32Premul(64, 73).withCanvas {
                    drawImage(sub(0, 0, 16, 64), 0F, 0F)
                    drawImage(sub(16, 0, 16, 64), 16F, 3F)
                    drawImage(sub(32, 0, 16, 64), 32F, 6F)
                    drawImage(sub(48, 0, 16, 64), 48F, 9F)
                }
            }, left, top - 9)
            // 侧面
            drawImage(img2.resize(16, 64, true), left + 16 * size, top)
            // 阴影
            shadow(80, 13)
        }
        val fat2Left = surface().withCanvas {
            val top = 35F
            // 正面
            drawImage(img1.resize(64, 64, true), left, top)
            // 侧面
            drawImage(img2.resize(16, 64, true), left + 16 * size, top)
        }
        val fat2DownLeft = surface().withCanvas {
            val top = 35F
            // 正面
            // drawImage(img1.resize(74, 64, true), left - 5, top)
            drawImage(img1.resize(64, 64, true).run {
                Surface.makeRasterN32Premul(64, 70).withCanvas {
                    drawImage(sub(0, 0, 6, 64), 0F, 0F)
                    drawImage(sub(6, 0, 10, 64), 6F, 3F)
                    drawImage(sub(16, 0, 48, 64), 16F, 6F)
                }
            }, left, top - 6)
            // 侧面
            drawImage(img2.resize(16, 64, true), left + 16 * size, top)
        }
        val compressLeft1 = surface().withCanvas {
            val top = 35F
            val c = 1
            val h = size - c
            val times = 0.5F
            for ((y, xw) in compressList.withIndex()) {
                // 正面
                drawImage(img1.sub(0, y, 16, 1).resize(74 + xw * 2, h, true), left - 5 - xw * times, top + 16 * c + y * h)
                // 侧面
                drawImage(img2.sub(0, y, 16, 1).resize(16, h, true), left + 64 + 5 + xw * times, top + 16 * c + y * h)
            }
        }
        val compressLeft2 = surface().withCanvas {
            val top = 35F
            val c = 2
            val h = size - c
            val times = 1
            for ((y, xw) in compressList.withIndex()) {
                // 正面
                drawImage(img1.sub(0, y, 16, 1).resize(74 + xw * 2 * times, h, true), left - 5 - xw * times, top + 16 * c + y * h)
                // 侧面
                drawImage(img2.sub(0, y, 16, 1).resize(16, h, true), left + 64 + 5 + xw * times, top + 16 * c + y * h)
            }
        }
        val compressLeft3 = surface().withCanvas {
            val top = 35F
            val c = 2
            val h = size - c
            val times = 2
            for ((y, xw) in compressList.withIndex()) {
                // 正面
                drawImage(img1.sub(0, y, 16, 1).resize(74 + xw * 2 * times, h, true), left - 5 - xw * times, top + 16 * c + y * h)
                // 侧面
                drawImage(img2.sub(0, y, 16, 1).resize(16, h, true), left + 64 + 5 + xw * times, top + 16 * c + y * h)
            }
        }
        val normalRight = surface().withCanvas {
            val top = 15F
            // 侧面
            drawImage(img2.resize(16, 64, true), left, top)
            // 正面
            drawImage(img1.resize(64, 64, true), left + 16, top)
            // 阴影
            shadow(80, 13)
        }
        val normalDownRight = surface().withCanvas {
            val top = 15F
            // 侧面
            drawImage(img2.resize(16, 64, true), left, top)
            // 正面
            // drawImage(img1.resize(64, 64, true), left + 16, top)
            drawImage(img1.resize(64, 64, true).run {
                Surface.makeRasterN32Premul(64, 70).withCanvas {
                    drawImage(sub(0, 0, 24, 64), 0F, 6F)
                    drawImage(sub(24, 0, 24, 64), 24F, 3F)
                    drawImage(sub(44, 0, 20, 64), 44F, 0F)
                }
            }, left + 16, top - 6)
            // 阴影
            shadow(80, 13)
        }
        val fat1Right = surface().withCanvas {
            val top = 20F
            // 侧面
            drawImage(img2.resize(16, 64, true), left, top)
            // 正面
            drawImage(img1.resize(64, 64, true), left + 16, top)
            // 阴影
            shadow(80, 13)
        }
        val fat1DownRight = surface().withCanvas {
            val top = 20F
            // 侧面
            drawImage(img2.resize(16, 64, true), left, top)
            // 正面
            // drawImage(img1.resize(68, 64, true), left + 16 - 5, top)
            drawImage(img1.resize(64, 64, true).run {
                Surface.makeRasterN32Premul(64, 73).withCanvas {
                    drawImage(sub(0, 0, 16, 64), 0F, 9F)
                    drawImage(sub(16, 0, 16, 64), 16F, 6F)
                    drawImage(sub(32, 0, 16, 64), 32F, 3F)
                    drawImage(sub(48, 0, 16, 64), 48F, 0F)
                }
            }, left + 16, top - 9)
            // 阴影
            shadow(80, 13)
        }
        val fat2Right = surface().withCanvas {
            val top = 35F
            // 侧面
            drawImage(img2.resize(16, 64, true), left, top)
            // 正面
            drawImage(img1.resize(64, 64, true), left + 16, top)
        }
        val fat2DownRight = surface().withCanvas {
            val top = 35F
            // 侧面
            drawImage(img2.resize(16, 64, true), left, top)
            // 正面
            // drawImage(img1.resize(74, 64, true), left + 16 - 5, top)
            drawImage(img1.resize(64, 64, true).run {
                Surface.makeRasterN32Premul(74, 70).withCanvas {
                    drawImage(sub(0, 0, 48, 64), 0F, 6F)
                    drawImage(sub(48, 0, 10, 64), 48F, 3F)
                    drawImage(sub(58, 0, 6, 64), 58F, 0F)
                }
            }, left + 16, top - 6)
        }
        val compressRight1 = surface().withCanvas {
            val top = 35F
            val c = 1
            val h = size - c
            val times = 0.5F
            for ((y, xw) in compressList.withIndex()) {
                // 侧面
                drawImage(img2.sub(0, y, 16, 1).resize(16, h, true), left - 5 - xw * times, top + 16 * c + y * h)
                // 正面
                drawImage(img1.sub(0, y, 16, 1).resize((74 + xw * times * 2).toInt(), h, true), left + 16 - 5 - xw * times, top + 16 * c + y * h)
            }
        }
        val compressRight2 = surface().withCanvas {
            val top = 35F
            val c = 2
            val h = size - c
            val times = 1
            for ((y, xw) in compressList.withIndex()) {
                // 侧面
                drawImage(img2.sub(0, y, 16, 1).resize(16, h, true), left - 5 - xw * times, top + 16 * c + y * h)
                // 正面
                drawImage(img1.sub(0, y, 16, 1).resize(74 + xw * times * 2, h, true), left + 16 - 5 - xw * times, top + 16 * c + y * h)
            }
        }
        val compressRight3 = surface().withCanvas {
            val top = 35F
            val c = 2
            val h = size - c
            val times = 2
            for ((y, xw) in compressList.withIndex()) {
                // 侧面
                drawImage(img2.sub(0, y, 16, 1).resize(16, h, true), left - 5 - xw * times, top + 16 * c + y * h)
                // 正面
                drawImage(img1.sub(0, y, 16, 1).resize(74 + xw * times * 2, h, true), left + 16 - 5 - xw * times, top + 16 * c + y * h)
            }
        }
        val images = listOf(
            normalDownLeft, fat1DownLeft, fat2DownLeft, compressLeft1, compressLeft2, compressLeft3, compressLeft2, compressLeft1, fat2Left, fat1Left, normalLeft,
            rotate1, rotate2, rotate3, rotate4, rotate5, rotate6,
            normalDownRight, fat1DownRight, fat2DownRight, compressRight1, compressRight2, compressRight3, compressRight2, compressRight1, fat2Right, fat1Right, normalRight,
            rotate6, rotate5, rotate4, rotate3, rotate2, rotate1
        )
        return images.map {
            Frame(60, it)
        }.encodeToBytes()
    }
}
