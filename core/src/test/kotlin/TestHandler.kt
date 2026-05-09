package top.e404.skiko

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.decodeToFrames
import top.e404.skiko.frame.encodeToBytes
import top.e404.skiko.handler.face.*
import top.e404.skiko.handler.list.*
import java.io.File
import kotlin.test.Test

class TestHandler {

    init {
        FontType.fontDir = "font"
    }

    private val inDir = File("in")
    private val outDir = File("out")
    private val emptyArgs = mutableMapOf<String, String>()

    private fun testHandler(handler: FramesHandler, args: MutableMap<String, String>) {
        runBlocking(Dispatchers.IO) {
            outDir.listFiles()?.forEach { it.delete() }
            inDir.listFiles()?.forEach {
                launch {
                    val fr1 = it.readBytes().decodeToFrames()
                    val result = handler.handleFrames(fr1, args)
                    if (!result.success) {
                        println("${it.name} - fail, msg: ${result.failMsg}")
                        result.throwable?.printStackTrace()
                    } else {
                        result.let { handleResult ->
                            val suffix = if (handleResult.result!!.size == 1) ".png" else ".gif"
                            outDir.resolve("${it.name}$suffix")
                                .writeBytes(handleResult.getOrThrow().encodeToBytes())
                        }
                        println("${it.name} - done")
                    }
                }
            }
        }
    }

    @Test
    fun testBlur() {
        testHandler(
            BlurHandler, mutableMapOf(
                "size" to "10"
            )
        )
    }

    @Test
    fun testRotate() {
        testHandler(RotateHandler, mutableMapOf("text" to "45"))
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
        testHandler(
            ResizeHandler,
            mutableMapOf(
                "w" to "-50",
                "h" to "-50",
            )
        )
    }

    @Test
    fun testRgbFilter() {
        testHandler(
            RgbHandler, mutableMapOf(
                "text" to "10",
                "f" to "",
            )
        )
    }

    @Test
    fun testRoundFilter() {
        testHandler(RoundHandler, emptyArgs)
    }

    @Test
    fun testShakeHandler() {
        testHandler(
            ShakeHandler, mutableMapOf(
                "size" to "20",
            )
        )
    }

    @Test
    fun testClipHandler() {
        testHandler(
            ClipHandler, mutableMapOf(
                "x" to "10%",
                "y" to "10%",
                "w" to "80%",
                "h" to "80%",
            )
        )
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
        testHandler(
            TurnHandler, mutableMapOf(
                "text" to "50",
                "r" to "",
            )
        )
    }

    @Test
    fun testWriteHandler() {
        testHandler(WriteHandler, mutableMapOf("text" to "400"))
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
        testHandler(
            LatticeHandler,
            mutableMapOf(
                "rate" to "8",
                "spacing" to "3",
                "bg" to "#000"
            )
        )
    }

    @Test
    fun testCharImageHandler() {
        testHandler(
            CharImageHandler,
            mutableMapOf(
                "text" to "好耶",
                "c" to ""
            )
        )
    }

    @Test
    fun testSwapHandler() {
        testHandler(SwapHandler, emptyArgs)
    }

    @Test
    fun testHideHandler() {
        testHandler(HideHandler, emptyArgs)
    }

    @Test
    fun testPatMelonHandler() {
        testHandler(PatMelonHandler, emptyArgs)
    }

    @Test
    fun testRaiseHandler() {
        testHandler(RaiseHandler, emptyArgs)
    }

    @Test
    fun testLikeHandler() {
        testHandler(LikeHandler, emptyArgs)
    }

    @Test
    fun testShinyHandler() {
        testHandler(ShinyHandler, emptyArgs)
    }

    @Test
    fun testBoxingHandler() {
        testHandler(BoxingHandler, emptyArgs)
    }

    @Test
    fun testSkipHandler() {
        testHandler(SkipHandler, emptyArgs)
    }

    @Test
    fun testEatHandler() {
        testHandler(EatHandler, emptyArgs)
    }

    @Test
    fun testUpHandler() {
        testHandler(UpHandler, emptyArgs)
    }

    @Test
    fun testPeasHandler() {
        testHandler(PeasHandler, emptyArgs)
    }

    @Test
    fun testEnchantHandler() {
        testHandler(EnchantHandler, emptyArgs)
    }

    @Test
    fun testBatHandler() {
        testHandler(BatHandler, emptyArgs)
    }

    @Test
    fun testPercent0Handler() {
        testHandler(
            Percent0Handler,
            mutableMapOf(
                "text" to "-10%"
            )
        )
    }

    @Test
    fun testX64Handler() {
        testHandler(X64Handler, emptyArgs)
    }

    @Test
    fun testQunQingHandler() {
        testHandler(
            QunQingHandler,
            //emptyArgs
            mutableMapOf(
                "right" to "90%",
                "top" to "50%",
            )
        )
    }

    @Test
    fun testLowPolyHandler() {
        testHandler(
            LowPolyHandler, mutableMapOf(
                "acc" to "30",
                "pc" to "1000"
            )
        )
    }

    @Test
    fun testReoHandler() {
        testHandler(ReoHandler, emptyArgs)
    }

    @Test
    fun testFlashHandler() {
        testHandler(FlashHandler, emptyArgs)
    }

    @Test
    fun testEggHandler() {
        testHandler(EggHandler, emptyArgs)
    }

    @Test
    fun testWarpHandler() {
        testHandler(WarpHandler, emptyArgs)
    }

    @Test
    fun testPaneHandler() {
        testHandler(PaneHandler, emptyArgs)
    }

    @Test
    fun testFondleHandler() {
        testHandler(FondleHandler, emptyArgs)
    }

    @Test
    fun testYgoHandler() {
        testHandler(
            YgoHandler, mutableMapOf(
                "c" to "魔法",
                "t" to "永续",
                "name" to "+1",
                "desc" to "這個卡名的效果1回合只能使用1次",
                "id" to "1433223",
            )
        )
    }

    @Test
    fun testBwHandler() {
        testHandler(
            BwHandler, mutableMapOf(
                "g" to "",
                //"l1" to "什么都没有",
                //"l2" to "何もありません何もありません何もありません何もありません何もありません何もありません何もありません",
            )
        )
    }

    @Test
    fun testRgbStripHandler() {
        testHandler(
            RgbStripHandler,
            mutableMapOf(
                "f" to "",
                "h" to "",
                "r" to "",
            )
        )
    }

    @Test
    fun testNotRespondingHandler() {
        testHandler(
            NotRespondingHandler,
            mutableMapOf(
                //"h" to "",
                "text" to "Minecraft* 1.18.2 - 多人游戏（第三方服务器）（未响应）",
                "bg" to "#ffffff",
                "font" to "#000000",
            )
        )
    }

    @Test
    fun testTrashHandler() {
        testHandler(
            TrashHandler,
            emptyArgs
        )
    }

    @Test
    fun testAddictionHandler() {
        testHandler(
            AddictionHandler,
            emptyArgs
        )
    }

    @Test
    fun testCyberpunkHandler() {
        testHandler(
            CyberpunkHandler,
            emptyArgs
        )
    }

    @Test
    fun testEmbossColor() {
        testHandler(
            EmbossColorHandler,
            emptyArgs
        )
    }

    @Test
    fun testColorfulEdgeHandler() {
        testHandler(
            ColorfulEdgeHandler,
            emptyArgs
        )
    }

    @Test
    fun testHold3Handler() {
        testHandler(
            Hold3Handler,
            emptyArgs
        )
    }

    @Test
    fun testDpxHandler() {
        testHandler(
            DpxHandler,
            mutableMapOf("r" to "")
        )
    }

    @Test
    fun testDRaiseHandler() {
        testHandler(
            DRaiseHandler,
            mutableMapOf(
                "text" to "20",
                "start" to "50%",
                "end" to "100%",
                "r" to "",
            )
        )
    }

    @Test
    fun testFormulaHandler() {
        testHandler(
            FormulaHandler,
            mutableMapOf(
                "r" to ""
            )
        )
    }

    @Test
    fun testLickHandler() {
        testHandler(
            LickHandler,
            mutableMapOf()
        )
    }

    @Test
    fun testDotMatrixHandler() {
        testHandler(
            DotMatrixHandler,
            mutableMapOf()
        )
    }
}
