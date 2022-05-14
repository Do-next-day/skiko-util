package top.e404.skiko

import org.junit.Test
import top.e404.skiko.draw.element.TextWithIndex
import top.e404.skiko.draw.toImage
import java.io.File

class Draw {
    private val outPng = File("out/out.png")

    @Test
    fun text() {
        val line = "任何字形边界框原点"
        val cf = FontType.LW.getSkiaFont(80F)
        val bytes = listOf(
            TextWithIndex(
                content = line,
                font = cf,
                udPadding = 10,
                index = "1."
            )
        ).toImage(10, debug = true)
        outPng.writeBytes(bytes)
    }
}