package top.e404.skiko

import org.jetbrains.skia.*
import org.junit.Test
import top.e404.skiko.handler.handlers
import top.e404.skiko.util.bytes
import top.e404.skiko.util.round
import java.io.File

class Test {
    private val inPng = File("run/in.png").readBytes()
    private val inGif = File("run/in.gif").readBytes()
    private val outPng = File("run/out/out.png")
    private val outGif = File("run/out/out.gif")

    @Test
    fun drawText() {
        val font = FontType.YAHEI.getSkijaFont(60F)
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
}