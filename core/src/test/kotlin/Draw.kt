package top.e404.skiko

import top.e404.skiko.draw.element.*
import top.e404.skiko.draw.toImage
import top.e404.skiko.util.Colors
import java.io.File
import kotlin.test.Test

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
            TextColorful(
                content = mutableListOf(
                    "一个蓝色的多行的text, 一个蓝色的多行的text, 一个蓝色的多行的text\n这是第二行" to Colors.BLUE.argb,
                    "一个红色的多行的text, 一个红色的多行的text, 一个红色的多行的text\n这是第二行" to Colors.RED.argb,
                    "一个紫色的多行的text, 一个紫色的多行的text, 一个紫色的多行的text\n这是第二行" to Colors.PURPLE.argb,
                ),
                font = FontType.LW.getSkiaFont(20F),
                center = false
            ),
            TextWithIcon("Index", FontType.LW.getSkiaFont(20F)),
            TextList(
                listOf(
                    "一个列表项目, index: 1",
                    "一个列表项目, index: 2, 特别长的行, 特别长的行, 特别长的行, 特别长的行, 特别长的行, 特别长的行, 特别长的行, 特别长的行, 特别长的行, 特别长的行, 特别长的行",
                ),
                font = FontType.LW.getSkiaFont(20F),
            ),
            SpacingLine(1F, 0F, 0F),
            Text(
                content = "text text text text text text text text text text text text text text text",
                font = FontType.LW.getSkiaFont(20F),
            ),
            TextLineBlock("BUTTON", font = FontType.LW.getSkiaFont(20F))
        ).toImage(50, debug = false, radius = 0F)
        outPng.writeBytes(bytes)
    }
}
