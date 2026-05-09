package top.e404.skiko

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.*
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.encodeToBytes
import top.e404.skiko.handler.handlerSet
import top.e404.skiko.util.*
import java.io.File
import kotlin.math.sqrt
import kotlin.test.Test

class Test {

    init {
        FontType.fontDir = "font"
    }

    companion object {
        private val inPng by lazy { File("in/0.png").readBytes() }
        private val outPng by lazy { File("out/out.png") }
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
        handlerSet.forEach {
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

    @Test
    fun hyTk() {
        val in1 = File("in/1.jpg").readBytes()
        val in2 = File("in/2.jpg").readBytes()
        val paint = Paint().apply {
            colorFilter = grayMatrix
        }
        val bm1 = Image.makeFromEncoded(in1).run {
            newSurface().withCanvas {
                drawImage(this@run, 0f, 0f, paint)
            }.toBitmap()
        }
        val bm2 = Image.makeFromEncoded(in2).run {
            newSurface().withCanvas {
                drawImage(this@run, 0f, 0f, paint)
            }.toBitmap()
        }
        val w1 = bm1.width
        val w2 = bm2.width
        if (w1 != w2) throw Exception("图片w不一致")

        val h1 = bm1.height
        val h2 = bm2.height
        if (h1 != h2) throw Exception("图片h不一致")

        val result = Bitmap().apply {
            allocPixels(bm1.imageInfo)
            setAlphaType(ColorAlphaType.PREMUL)
        }
        for (x in 0 until w1) for (y in 0 until h1) {
            val c1 = bm1.getColor(x, y) // 表 白
            val c2 = bm2.getColor(x, y) // 里 黑
            val (r1, g1, b1) = c1.rgb()
            val (r2, g2, b2) = c2.rgb()
            val gray1 = gray(r1, g1, b1)
            val gray2 = gray(r2, g2, b2)
            if (gray1 == 0) {
                result.erase(gray2 shl 24 or 0xffffff, IRect.makeXYWH(x, y, 1, 1))
                continue
            }
            if (gray1 == 0xff) {
                result.erase(gray2 shl 24 or 0xffffff, IRect.makeXYWH(x, y, 1, 1))
                continue
            }
            if (gray1 == gray2) {
                result.erase(gray2 shl 24 or 0xffffff, IRect.makeXYWH(x, y, 1, 1))
                continue
            }
            val a = 160 - gray1 + gray2
            if (a == 0) {
                println(
                    """x: $x
                        |y: $y
                        |c1: ${c1.formatAsColor()}
                        |c2: ${c2.formatAsColor()}
                        |gray1: $gray1
                        |gray2: $gray2
                    """.trimMargin()
                )
            }
            val p = gray2 / a
//            val color = p or (a shl 24)
            result.erase((gray2 / gray1 * 255) shl 24 or 0xffffff, IRect.makeXYWH(x, y, 1, 1))
        }
        File("out/out.png").writeBytes(result.toImage().bytes())
    }

    private fun Int.formatAsColor(): String {
        if (this < 0) return "#${(this + 0xffffffff + 1).toString(16)}"
        return "#${toString(16)}"
    }

    @Test
    fun p() {
        val image = Image.makeFromEncoded(inPng)
        val result = image.newSurface().withCanvas {
            drawImage(image, 0f, 0f, Paint().apply {
                colorFilter = ColorFilter.makeMatrix(
                    ColorMatrix(
                        0.1F, 0.1F, 0.1F, 0F, 0F,
                        0.1F, 0.1F, 0.1F, 0F, 0F,
                        .5F, .5F, .5F, 0F, 0F,
                        0F, 0F, 0F, 1F, 0F,
                    )
                )
            })
        }
        outPng.writeBytes(result.bytes())
    }

    @Test
    fun dtmh() {
        val image = Image.makeFromEncoded(inPng)
        val src = image.toBitmap()
        val dst = Bitmap().apply { allocPixels(image.imageInfo) }
        val centerX = src.width / 2
        val centerY = src.height / 2
        for (x in 0 until src.width) for (y in 0 until src.height) {
            val c = src.getColor(x, y)
        }
    }


    val dir = File("F:\\D\\公式飞出屏幕\\dir")
    val out = File("F:\\D\\公式飞出屏幕\\out")

    @Test
    fun rename() {
        dir.listFiles()!!
            .sortedBy { it.name }
            .forEachIndexed { index, f ->
                f.renameTo(f.parentFile.resolve("${index.toString().padStart(3, '0')}.png"))
            }
    }

    @Test
    fun rename2() {
        dir.listFiles()!!
            .map { it.name.removeSuffix(".png").toInt() to it }
            .sortedBy { it.first }
            .forEachIndexed { index, (_, f) ->
                f.renameTo(f.parentFile.resolve("$index.png"))
            }
    }

    @Test
    fun reduce() {
        dir.listFiles()!!
            .map { it.name.removeSuffix(".png").toInt() to it }
            .sortedBy { it.first }
            .forEach { (id, f) -> if (id % 8 != 0) f.delete() }
    }

    @Test
    fun color() {
        runBlocking(Dispatchers.Default) {
            dir.listFiles()!!.forEach { file ->
                launch {
                    Image.makeFromEncoded(file.readBytes()).resize(300 * 16 / 9, 300).handlePixel {
                        val (r, g, b) = it.rgb()
                        if (r < 64 && g > 64 && b < 64) return@handlePixel argb(0, 0, 0, 0)
                        val x = (r + b) / 2
                        argb((r + g + b - 0xff * 2).coerceIn(0, 0xff), x, x, x)
                    }.bytes().let {
                        out.resolve(file.name).writeBytes(it)
                    }
                    println(file.name)
                }
            }
        }
    }

    @Test
    fun encode() {
        dir.listFiles()!!.map { file ->
            Frame(0, Image.makeFromEncoded(file.readBytes()))
        }.encodeToBytes().let {
            out.parentFile.resolve("out.gif").writeBytes(it)
        }
    }

    @Test
    fun encodeColor() {
        out.listFiles()!!.toMutableList()
            .sortedBy { it.name.removeSuffix(".png").toInt() }
            .map { Frame(0, Image.makeFromEncoded(it.readBytes())) }
            .encodeToBytes()
            .let { out.parentFile.resolve("color.gif").writeBytes(it) }
    }
}
