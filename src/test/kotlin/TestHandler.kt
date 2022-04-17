package top.e404.skiko

import kotlinx.coroutines.runBlocking
import org.junit.Test
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.decodeToFrames
import top.e404.skiko.frame.encodeToBytes
import top.e404.skiko.handler.face.*
import top.e404.skiko.handler.list.*
import java.io.File

class TestHandler {
    private val inPng = File("run/in.png").readBytes()
    private val inGif = File("run/in.gif").readBytes()
    private val out = File("run/out")
    private val emptyArgs = mutableMapOf<String, String>()

    private fun testHandler(handler: FramesHandler, args: MutableMap<String, String>) {
        runBlocking {
            val fr1 = inPng.decodeToFrames()
            handler.handleFrames(fr1, args).run {
                out.resolve(if (gif) "1.gif" else "1.png")
                    .writeBytes(getOrThrow().encodeToBytes())
            }
            println(1)
            val fr2 = inGif.decodeToFrames()
            handler.handleFrames(fr2, args).run {
                out.resolve(if (gif) "2.gif" else "2.png")
                    .writeBytes(getOrThrow().encodeToBytes())
            }
            println(2)
        }
    }

    @Test
    fun testBlur() {
        testHandler(BlurHandler, mutableMapOf(
            "size" to "10"
        ))
    }

    @Test
    fun testRotate() {
        testHandler(RotateHandler, mutableMapOf("angel" to "45"))
    }

    @Test
    fun testEmboss() {
        testHandler(EmbossHandler, emptyArgs)
    }

    @Test
    fun testOld() {
        testHandler(OldHandler, emptyArgs)
    }

    @Test
    fun testReverse() {
        testHandler(ReverseHandler, emptyArgs)
    }

    @Test
    fun testHorizontalFlip() {
        testHandler(FlipHorizontalHandler, emptyArgs)
    }

    @Test
    fun testVerticalFlip() {
        testHandler(FlipVerticalHandler, emptyArgs)
    }

    @Test
    fun testPx() {
        testHandler(PxHandler, mutableMapOf("scale" to "10"))
    }

    @Test
    fun testResizeHandler() {
        testHandler(ResizeHandler, mutableMapOf(
            "w" to "-10",
            "h" to "-10",
        ))
    }

    @Test
    fun testRgbFilter() {
        testHandler(RgbHandler, mutableMapOf(
            "count" to "10",
        ))
    }

    @Test
    fun testRoundFilter() {
        testHandler(RoundHandler, emptyArgs)
    }

    @Test
    fun testShakeHandler() {
        testHandler(ShakeHandler, mutableMapOf(
            "size" to "20",
        ))
    }

    @Test
    fun testClipHandler() {
        testHandler(ClipHandler, mutableMapOf(
            "x" to "20",
            "y" to "20",
            "w" to "60",
            "h" to "60",
        ))
    }

    @Test
    fun testDemandHandler() {
        testHandler(DemandHandler, emptyArgs)
    }

    @Test
    fun testCrawlHandler() {
        testHandler(CrawlHandler, mutableMapOf("change" to "", "count" to "10"))
    }

    @Test
    fun testDislikeHandler() {
        testHandler(DislikeHandler, emptyArgs)
    }

    @Test
    fun testDoubtGenerator() {
        testHandler(DoubtHandler, emptyArgs)
    }

    @Test
    fun testInjectionHandler() {
        testHandler(InjectionHandler, emptyArgs)
    }

    @Test
    fun testJszzHandler() {
        testHandler(JszzHandler, emptyArgs)
    }

    @Test
    fun testKnockHandler() {
        testHandler(KnockHandler, emptyArgs)
    }

    @Test
    fun testLongMingHandler() {
        testHandler(LongMingHandler, emptyArgs)
    }

    @Test
    fun testNationalFaceHandler() {
        testHandler(NationalFaceHandler, mutableMapOf("index" to "1"))
    }

    @Test
    fun testPatHandler() {
        testHandler(PatHandler, emptyArgs)
    }

    @Test
    fun testRuaHandler() {
        testHandler(RuaHandler, emptyArgs)
    }

    @Test
    fun testRubHandler() {
        testHandler(RubHandler, emptyArgs)
    }

    @Test
    fun testThrowHandler() {
        testHandler(ThrowHandler, emptyArgs)
    }

    @Test
    fun testBeatHandler() {
        testHandler(BeatHandler, emptyArgs)
    }

    @Test
    fun testTouchHandler() {
        testHandler(TouchHandler, emptyArgs)
    }

    @Test
    fun testPushHandler() {
        testHandler(PushHandler, emptyArgs)
    }

    @Test
    fun testTurnHandler() {
        testHandler(TurnHandler, mutableMapOf("count" to "10"))
    }

    @Test
    fun testRotateHandler() {
        testHandler(RotateHandler, mutableMapOf("angel" to "45"))
    }

    @Test
    fun testWriteHandler() {
        testHandler(WriteHandler, mutableMapOf("text" to "靐靐靐\n靐靐靐"))
    }

    @Test
    fun testHold1Handler() {
        testHandler(Hold1Handler, emptyArgs)
    }

    @Test
    fun testHold2Handler() {
        testHandler(Hold2Handler, emptyArgs)
    }

    @Test
    fun testLatticeHandler() {
        testHandler(LatticeHandler, mutableMapOf(
            "rate" to "8",
            "spacing" to "3",
            "bg" to "#000")
        )
    }

    @Test
    fun testCharImageHandler() {
        testHandler(CharImageHandler, mutableMapOf(
            "text" to "好耶",
            "c" to ""
        ))
    }
}