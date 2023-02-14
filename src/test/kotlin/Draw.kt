package top.e404.skiko

import org.junit.Test
import top.e404.skiko.draw.element.*
import top.e404.skiko.draw.toImage
import java.io.File

class Draw {

    init {
        FontType.fontDir = "font"
    }

    private val outPng = File("out/out.png")

    @Test
    fun text() {
        val bytes = listOf(
            Text(
                content = "一个多行的text, 一个多行的text, 一个多行的text, 一个多行的text\n这是第二行",
                font = FontType.LW.getSkiaFont(20F),
                center = false
            ),
            TextWithIcon("Index", FontType.LW.getSkiaFont(20F)),
            TextList(
                listOf(
                    "一个列表项目, index: 1",
                    "一个列表项目, index: 2",
                    "一个列表项目, index: 3",
                    "一个列表项目, index: 4",
                ),
                font = FontType.LW.getSkiaFont(20F),
            ),
            SpacingLine(1F, 15F, 15F),
            Text(
                content = "text text text text text text text text text text text text text text text",
                font = FontType.LW.getSkiaFont(20F),
            ),
            TextLineBlock("BUTTON", font = FontType.LW.getSkiaFont(20F))
        ).toImage(30, debug = false)
        outPng.writeBytes(bytes)
    }
}