package top.e404.skiko

import org.junit.Test
import top.e404.skiko.draw.element.Text
import top.e404.skiko.draw.toImage
import java.io.File

class Draw {
    private val outPng = File("out/out.png")
    @Test
    fun text() {
        val font = FontType.MI.getSkiaFont(40F)
        val line = "abc def 濱"
        val bytes = listOf(Text(line, font)).toImage(debug = false)
        outPng.writeBytes(bytes)
    }
}