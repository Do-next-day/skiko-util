package top.e404.skiko

import org.jetbrains.skia.*
import org.junit.Test
import top.e404.skiko.handler.handlers
import top.e404.skiko.util.bytes
import top.e404.skiko.util.round
import top.e404.skiko.util.toImage
import java.io.File
import kotlin.math.sqrt

class Test {
    companion object {
        private val inPng = File("in.jpg").readBytes()
        private val outPng = File("out/out.png")
    }

    @Test
    fun drawText() {
        val font = FontType.YAHEI.getSkiaFont(60F)
        val bytes = Surface.makeRasterN32Premul(120, 150).run {
            canvas.apply {
                drawRect(Rect.makeXYWH(0F, 0F, 120F, 150F), Paint().apply { color = Colors.WHITE.argb })
                font.metrics.apply {
                    println("bottom - top: ${bottom - top}")

                    println("top: $top")
                    val h = 90F - top
                    println("h: $h")
                    drawLine(0F, h, 120F, h, Paint().apply {
                        color = Colors.RED.argb
                        //strokeCap = PaintStrokeCap.ROUND
                        this.mode = PaintMode.FILL
                        //strokeWidth = 3F
                    })
                    println("ascent: $ascent")
                    println("descent: $descent")
                    println("bottom: $bottom")
                    println("leading: $leading")
                    println("avgCharWidth: $avgCharWidth")
                    println("maxCharWidth: $maxCharWidth")
                    println("xMin: $xMin")
                    println("xMax: $xMax")
                    println("xHeight: $xHeight")
                    println("capHeight: $capHeight")
                    println("underlineThickness: $underlineThickness")
                    println("underlinePosition: $underlinePosition")
                    println("strikeoutThickness: $strikeoutThickness")
                    println("strikeoutPosition: $strikeoutPosition")
                }
                val fl = font.metrics.descent
                println("fl: $fl")
                drawTextLine(TextLine.make("fg癛", font), 0F, 150F - fl, Paint())
            }
            bytes()
        }
        outPng.writeBytes(bytes)
    }

    @Test
    fun matrix() {
        val image = Image.makeFromEncoded(inPng)
        val cf = ColorFilter.makeMatrix(
            ColorMatrix(
                1F, 0F, 0F, 0F, 0F,
                0F, 1F, 0F, 0F, 0F,
                0F, 0F, 1F, 0F, 0F,
                0F, 0F, 0F, 0.5F, 0F,
            )
        )
        val result = Surface.makeRaster(image.imageInfo).run {
            canvas.apply {
                drawImage(image, 0F, 0F, Paint().apply {
                    colorFilter = cf
                })
            }
            makeImageSnapshot()
        }.bytes()
        outPng.writeBytes(result)
    }

    @Test
    fun test() {
        val image = Image.makeFromEncoded(inPng)
        outPng.writeBytes(image.round().bytes())
    }

    @Test
    fun list() {
        handlers.forEach {
            println("${it.name}: ${it.regex.pattern}")
        }
    }

    @Test
    fun colorGen() {
        val list = genColor(0x00ffcc, 0xffffff, 12)
        list.forEach {
            println("#${it.toString(16).padStart(6, '0')}")
        }
    }

    @Test
    fun distortingRaised() {
        val image = Image.makeFromEncoded(inPng)
        val bitmap = Bitmap.makeFromImage(image)
        val result = Bitmap().apply { allocPixels(image.imageInfo) }

        val centerX = image.width / 2
        val centerY = image.height / 2

        val r = 100

        fun distance(x1: Int, y1: Int, x2: Int, y2: Int): Int {
            val dx = x1 - x2
            val dy = y1 - y2
            return sqrt((dx * dx + dy * dy).toDouble()).toInt()
        }

        for (x in 0 until image.width) for (y in 0 until image.height) {
            val distance = distance(x, y, centerX, centerY)
            val color = if (distance > r) {
                bitmap.getColor(x, y)
            } else {
                val tx = (x - centerX) * distance / r + centerX
                val ty = (y - centerY) * distance / r + centerY
                bitmap.getColor(tx, ty)
            }
            result.erase(color, IRect.makeXYWH(x, y, 1, 1))
        }

        outPng.writeBytes(result.toImage().bytes())
    }
}