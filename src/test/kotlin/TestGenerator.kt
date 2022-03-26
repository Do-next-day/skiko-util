package top.e404.skiko

import kotlinx.coroutines.runBlocking
import org.junit.Test
import top.e404.skiko.generator.ImageGenerator
import top.e404.skiko.generator.list.CardGenerator
import top.e404.skiko.generator.list.GoodNewsGenerator
import top.e404.skiko.generator.list.PornhubGenerator
import top.e404.skiko.generator.list.ShakeTextGenerator
import top.e404.skiko.handler.StringPairData
import top.e404.skiko.handler.TextData
import java.io.File

class TestGenerator {
    private val inPng = File("run/in.png").readBytes()
    private val inGif = File("run/in.gif").readBytes()
    private val outPng = File("run/out/out.png")
    private val outGif = File("run/out/out.gif")
    private fun testGenerator(generator: ImageGenerator, data: ExtraData?) {
        runBlocking {
            this@TestGenerator.outGif.writeBytes(generator.generate(data))
        }
    }

    @Test
    fun testShakeTextGenerator() {
        testGenerator(ShakeTextGenerator,
            ShakeTextGenerator.ShakeTextData(
                "哼哼哼啊啊啊啊啊啊啊啊啊啊啊啊",
                Colors.BLUE_GREEN.argb,
                Colors.BG.argb,
                50,
                12
            ))
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
    fun testGoodNewsGenerator() {
        testGenerator(GoodNewsGenerator, TextData("你的账号被风控了", null, null))
    }
}