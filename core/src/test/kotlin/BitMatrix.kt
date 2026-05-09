package top.e404.skiko

import org.jetbrains.skia.*
import top.e404.skiko.dot.binary
import top.e404.skiko.dot.generator
import top.e404.skiko.util.Colors
import top.e404.skiko.util.bytes
import top.e404.skiko.util.withCanvas
import java.io.File
import kotlin.test.Test

class BitMatrix {
    private fun test(file: File) = binary(Image.makeFromEncoded(file.readBytes())).generator(1, 1)

    @Test
    fun t1() {
        File("in").listFiles()!!.forEach {
            if (it.name.endsWith(".gif")) return@forEach
            println(test(it))
        }
    }

    @Test
    fun t2() {
        println(test(File("F:/D/1.jpg")))
    }

    @Test
    fun t3() {
        FontType.fontDir = "font"
        val tf = FontType.GNU_UNIFONT.typeface
        val font = Font(tf, 24F)
        font.metrics.ascent
        val line = TextLine.make("肯德基", font)
        val paint = Paint().apply {
            color = Colors.BLACK.argb
            isAntiAlias = false
        }
        Surface.makeRasterN32Premul(line.width.toInt(), (line.descent - line.ascent).toInt()).withCanvas {
            drawTextLine(line, 0f, -line.ascent, paint)
        }.bytes().let {
            File("F:/D/1.png").writeBytes(it)
        }
    }
}
