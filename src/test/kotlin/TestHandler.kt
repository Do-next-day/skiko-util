package top.e404.skiko

import kotlinx.coroutines.runBlocking
import org.junit.Test
import top.e404.skiko.handler.FloatData
import top.e404.skiko.handler.IntData
import top.e404.skiko.handler.IntPairData
import top.e404.skiko.handler.face.*
import top.e404.skiko.handler.filter.*
import top.e404.skiko.handler.list.*
import java.io.File

class TestHandler {
    private val inPng = File("run/in.png").readBytes()
    private val inGif = File("run/in.gif").readBytes()
    private val out = File("run/out")
    private fun testHandler(handler: ImageHandler, data: ExtraData?) {
        runBlocking {
            handler.handle(inGif, data).run {
                out.resolve(if (isGif) "1.gif" else "1.png").writeBytes(getOrThrow())
            }
            handler.handle(inPng, data).run {
                out.resolve(if (isGif) "2.gif" else "2.png").writeBytes(getOrThrow())
            }
        }
    }

    @Test
    fun testBlur() {
        testHandler(BlurHandler, BlurHandler.BlurData(10F, 10F))
    }

    @Test
    fun testRotate() {
        testHandler(RotateHandler, FloatData(320F))
    }

    @Test
    fun testEmboss() {
        testHandler(EmbossHandler, null)
    }

    @Test
    fun testOld() {
        testHandler(OldHandler, null)
    }

    @Test
    fun testReverse() {
        testHandler(ReverseHandler, null)
    }

    @Test
    fun testHorizontalFlip() {
        testHandler(FlipHorizontalHandler, null)
    }

    @Test
    fun testVerticalFlip() {
        testHandler(FlipVerticalHandler, null)
    }

    @Test
    fun testPx() {
        testHandler(PxHandler, IntData(10))
    }

    @Test
    fun testResizeHandler() {
        testHandler(ResizeHandler, IntPairData(-10, -10))
    }

    @Test
    fun testRgbFilter() {
        testHandler(RgbHandler, IntData(10))
    }

    @Test
    fun testRoundFilter() {
        testHandler(RoundHandler, null)
    }

    @Test
    fun testShakeHandler() {
        testHandler(ShakeHandler, IntData(50))
    }

    @Test
    fun testClipHandler() {
        testHandler(ClipHandler, IntPairData(-20, -20))
    }

    @Test
    fun testDemandHandler() {
        testHandler(DemandGenerator, null)
    }

    @Test
    fun testCrawlHandler() {
        testHandler(CrawlGenerator, null)
    }

    @Test
    fun testDislikeHandler() {
        testHandler(DislikeGenerator, null)
    }

    @Test
    fun testDoubtGenerator() {
        testHandler(DoubtHandler, null)
    }

    @Test
    fun testInjectionHandler() {
        testHandler(InjectionHandler, null)
    }

    @Test
    fun testJszzHandler() {
        testHandler(JszzHandler, null)
    }

    @Test
    fun testKnockHandler() {
        testHandler(KnockHandler, null)
    }

    @Test
    fun testLongMingHandler() {
        testHandler(LongMingHandler, null)
    }

    @Test
    fun testNationalFaceHandler() {
        testHandler(NationalFaceHandler, IntData(0))
    }

    @Test
    fun testPatHandler() {
        testHandler(PatHandler, IntData(0))
    }

    @Test
    fun testRuaHandler() {
        testHandler(RuaHandler, IntData(0))
    }

    @Test
    fun testRubHandler() {
        testHandler(RubHandler, IntData(0))
    }

    @Test
    fun testThrowHandler() {
        testHandler(ThrowHandler, IntData(0))
    }
}