package top.e404.skiko

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.*
import org.junit.Test
import top.e404.skiko.generator.ImageGenerator
import top.e404.skiko.generator.list.CardGenerator
import top.e404.skiko.generator.list.PornhubGenerator
import top.e404.skiko.handler.*
import top.e404.skiko.handler.filter.*
import java.io.File

class Test {
    private fun testHandler(handler: ImageHandler, data: ExtraData?) {
        runBlocking {
            var bytes = File("run/in.gif").readBytes()
            var result = handler.handle(bytes, data).getOrThrow()
            File("run/out.gif").writeBytes(result)


            bytes = File("run/in.png").readBytes()
            result = handler.handle(bytes, data).getOrThrow()
            File("run/out.png").writeBytes(result)
        }
    }

    private fun testGenerator(generator: ImageGenerator, data: ExtraData?) {
        runBlocking {
            out.writeBytes(generator.generate(data).encodeToData(EncodedImageFormat.PNG)!!.bytes)
        }
    }

    private val bytes = File("run/in.png").readBytes()
    private val out = File("run/out.png")

    @Test
    fun t() {
        //val (a, r, g, b) = 0xffaabbcc.toInt().toArgb()
        //println(argb(0xFF, 0xAA, 0xBB, 0xCC).let {
        //    it.toLong() + Int.MAX_VALUE + 1
        //}.toString(16))
        val (a, r, g, b) = ((0xFF shl 24) or (0xAA shl 16) or (0xBB shl 8) or (0xCC)).argb()
        println("""a: ${a.toString(16)}
            |r: ${r.toString(16)}
            |g: ${g.toString(16)}
            |b: ${b.toString(16)}
        """.trimMargin())
    }

    @Test
    fun testBlur() {
        testHandler(BlurFilter, BlurFilter.BlurData(10F, 10F))
    }

    @Test
    fun testRotate() {
        testHandler(RotateFilter, FloatData(320F))
    }

    @Test
    fun testEmboss() {
        testHandler(EmbossFilter, null)
    }

    @Test
    fun testOld() {
        testHandler(OldFilter, null)
    }

    @Test
    fun testReverse() {
        testHandler(ReverseFilter, null)
    }

    @Test
    fun testHorizontalFlip() {
        testHandler(FlipHorizontalFilter, null)
    }

    @Test
    fun testVerticalFlip() {
        testHandler(FlipVerticalFilter, null)
    }

    @Test
    fun testPx() {
        testHandler(PxFilter, IntData(10))
    }

    @Test
    fun testResizeHandler() {
        testHandler(ResizeHandler, IntPairData(-10, -10))
    }

    @Test
    fun testRgbFilter() {
        testHandler(RgbFilter, IntData(10))
    }

    @Test
    fun testCardGenerator() {
        testGenerator(CardGenerator, StringPairData("awa", "QwQ"))
    }

    @Test
    fun testPornhubGenerator() {
        testGenerator(PornhubGenerator, StringPairData("awa", "QwQ"))
    }

    @Test
    fun drawText() {
        val font = FontType.YAHEI.getSkijaFont(60F)
        val bytes = Surface.makeRasterN32Premul(120, 150).run {
            canvas.apply {
                drawRect(Rect.makeXYWH(0F, 0F, 120F, 150F), Paint().apply { color = Colors.WHITE.value })
                font.metrics.apply {
                    println("bottom - top: ${bottom - top}")

                    println("top: $top")
                    val h = 90F - top
                    println("h: $h")
                    drawLine(0F, h, 120F, h, Paint().apply {
                        color = Colors.RED.value
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
        out.writeBytes(bytes)
    }

    @Test
    fun matrix() {
        val image = Image.makeFromEncoded(bytes)
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
        out.writeBytes(result)
    }
}