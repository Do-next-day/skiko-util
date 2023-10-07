package top.e404.skiko

import kotlinx.coroutines.runBlocking
import top.e404.skiko.frame.encodeToBytes
import top.e404.skiko.generator.ImageGenerator
import top.e404.skiko.generator.list.*
import java.io.File
import kotlin.test.Test

class TestGenerator {

    init {
        FontType.fontDir = "font"
        BdfType.dir = "bdf"
    }

    private val outPng = File("out/out.png")
    private val outGif = File("out/out.gif")
    private fun testGenerator(generator: ImageGenerator, args: MutableMap<String, String>) {
        runBlocking {
            val frames = generator.generate(args)
            val f = if (frames.size == 1) outPng else outGif
            f.writeBytes(frames.encodeToBytes())
        }
    }

    @Test
    fun testShakeTextGenerator() {
        testGenerator(ShakeTextGenerator, mutableMapOf(
            "text" to "哼哼哼啊啊啊啊啊啊啊啊啊啊啊啊",
            "color" to "#0f7",
            "bg" to "#1F1B1D",
            "size" to "20",
            "count" to "12"
        ))
    }

    @Test
    fun testCardGenerator() {
        testGenerator(CardGenerator, mutableMapOf("b" to "awa", "s" to "QwQ"))
    }

    @Test
    fun testPornhubGenerator() {
        testGenerator(PornhubGenerator, mutableMapOf("s1" to "awa", "s2" to "QwQ"))
    }

    @Test
    fun testGoodNewsGenerator() {
        testGenerator(GoodNewsGenerator, mutableMapOf(
            "text" to "你的账号被风控了\n你的账号被风控了\n你的账号被风控了",
            "size" to "100"
        ))
    }

    @Test
    fun testCodeGenerator() {
        testGenerator(CodeGenerator, mutableMapOf(
            "text" to "Administrator"
        ))
    }

    @Test
    fun testWhisperGenerator() {
        testGenerator(WhisperGenerator, mutableMapOf(
            "text" to "Administrator"
        ))
    }

    @Test
    fun testGraduationRecordGenerator() {
        testGenerator(GraduationRecordGenerator, mutableMapOf(
            "text" to """带专
                |民科
                |知乎大学
                |律业
                |2022年6月
            """.trimMargin()
        ))
    }

    @Test
    fun testDotMatrixCharImageGenerator() {
        testGenerator(DotMatrixCharImageGenerator, mutableMapOf(
            "text" to "肯德基疯狂星期四\nV我50"
        ))
    }
}
