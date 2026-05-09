package top.e404.skiko.draw.test

import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.junit.Test
import top.e404.skiko.draw.compose.Column
import top.e404.skiko.draw.compose.DrawCommand
import top.e404.skiko.draw.compose.MeasureContext
import top.e404.skiko.draw.compose.Modifier
import top.e404.skiko.draw.compose.RecordingDrawCanvas
import top.e404.skiko.draw.compose.TextMeasurer
import top.e404.skiko.draw.compose.TextMetrics
import top.e404.skiko.draw.compose.renderCommands
import top.e404.skiko.draw.compose.text
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ComposeRenderAbstractionTest {
    private class FixedTextMeasurer(
        private val charWidth: Float = 10f,
        private val ascent: Float = -16f,
        private val descent: Float = 4f
    ) : TextMeasurer {
        override fun measureTextWidth(text: String, font: Font, paint: Paint): Float =
            text.length * charWidth

        override fun metrics(font: Font): TextMetrics =
            TextMetrics(ascent, descent)
    }

    @Test
    fun layoutUsesInjectedTextMeasurer() {
        val root = Column()

        root.apply {
            text("hello", fontSize = 20f)
        }
        root.measure(MeasureContext(FixedTextMeasurer()))
        root.layout(0f, 0f)

        assertEquals(50f, root.width)
        assertEquals(20f, root.height)
    }

    @Test
    fun renderCommandsRecordsClearAndText() {
        val commands = renderCommands(
            backgroundColor = Color.WHITE,
            measureContext = MeasureContext(FixedTextMeasurer())
        ) {
            text("hi", fontSize = 20f, textColor = Color.RED)
        }

        assertEquals(DrawCommand.Clear(Color.WHITE), commands.first())
        val text = commands.filterIsInstance<DrawCommand.Text>().single()
        assertEquals("hi", text.text)
        assertEquals(0f, text.x)
        assertEquals(16f, text.baselineY)
        assertEquals(20f, text.font.size)
        assertEquals(Color.RED, text.paint.color)
    }

    @Test
    fun recordingCanvasSnapshotsPaintAtCallTime() {
        val recorder = RecordingDrawCanvas()
        val paint = Paint().apply {
            color = Color.RED
            isAntiAlias = true
        }

        recorder.drawCircle(1f, 2f, 3f, paint)
        paint.color = Color.BLUE

        val command = assertIs<DrawCommand.Circle>(recorder.commands.single())
        assertEquals(Color.RED, command.paint.color)
        assertEquals(true, command.paint.antiAlias)
    }

    @Test
    fun recordingCanvasRecordsRawTransformCommands() {
        val recorder = RecordingDrawCanvas()

        recorder.save()
        recorder.translate(10f, 20f)
        recorder.scale(2f, 2f)
        recorder.restore()

        assertTrue(recorder.commands[0] is DrawCommand.Save)
        assertEquals(DrawCommand.Translate(10f, 20f), recorder.commands[1])
        assertEquals(DrawCommand.Scale(2f, 2f), recorder.commands[2])
        assertTrue(recorder.commands[3] is DrawCommand.Restore)
    }
}
