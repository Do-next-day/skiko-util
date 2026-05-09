package top.e404.skiko.draw.test

import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Typeface
import org.junit.Test
import top.e404.skiko.draw.compose.Box
import top.e404.skiko.draw.compose.CanvasElement
import top.e404.skiko.draw.compose.Column
import top.e404.skiko.draw.compose.ComposeFontManager
import top.e404.skiko.draw.compose.DrawCommand
import top.e404.skiko.draw.compose.DrawContext
import top.e404.skiko.draw.compose.HorizontalAlignment
import top.e404.skiko.draw.compose.IconTheme
import top.e404.skiko.draw.compose.ImageOverflow
import top.e404.skiko.draw.compose.MeasureContext
import top.e404.skiko.draw.compose.Modifier
import top.e404.skiko.draw.compose.RecordingDrawCanvas
import top.e404.skiko.draw.compose.Row
import top.e404.skiko.draw.compose.Shape
import top.e404.skiko.draw.compose.SkiaDrawCanvas
import top.e404.skiko.draw.compose.SkiaTextMeasurer
import top.e404.skiko.draw.compose.Table
import top.e404.skiko.draw.compose.TableRow
import top.e404.skiko.draw.compose.TextMeasurer
import top.e404.skiko.draw.compose.TextMetrics
import top.e404.skiko.draw.compose.TextOverflow
import top.e404.skiko.draw.compose.UiElement
import top.e404.skiko.draw.compose.VerticalAlignment
import top.e404.skiko.draw.compose.antiAlias
import top.e404.skiko.draw.compose.background
import top.e404.skiko.draw.compose.border
import top.e404.skiko.draw.compose.box
import top.e404.skiko.draw.compose.charts.BarTheme
import top.e404.skiko.draw.compose.charts.RadarFixPolicy
import top.e404.skiko.draw.compose.charts.RadarTheme
import top.e404.skiko.draw.compose.charts.bar
import top.e404.skiko.draw.compose.charts.drawRadarChart
import top.e404.skiko.draw.compose.charts.radar
import top.e404.skiko.draw.compose.clip
import top.e404.skiko.draw.compose.column
import top.e404.skiko.draw.compose.debugBaseElement
import top.e404.skiko.draw.compose.drawIcon
import top.e404.skiko.draw.compose.height
import top.e404.skiko.draw.compose.icon
import top.e404.skiko.draw.compose.iconText
import top.e404.skiko.draw.compose.image
import top.e404.skiko.draw.compose.sizeIn
import top.e404.skiko.draw.compose.padding
import top.e404.skiko.draw.compose.render
import top.e404.skiko.draw.compose.renderCommands
import top.e404.skiko.draw.compose.row
import top.e404.skiko.draw.compose.size
import top.e404.skiko.draw.compose.tableRow
import top.e404.skiko.draw.compose.cell
import top.e404.skiko.draw.compose.table
import top.e404.skiko.draw.compose.text
import top.e404.skiko.draw.compose.width
import top.e404.skiko.util.Colors
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

private const val EPSILON = 0.001f

private class FixedTextMeasurer(
    private val charWidth: Float = 10f,
    private val ascent: Float = -8f,
    private val descent: Float = 2f
) : TextMeasurer {
    override fun measureTextWidth(text: String, font: Font, paint: Paint): Float =
        text.length * charWidth

    override fun metrics(font: Font): TextMetrics =
        TextMetrics(ascent, descent)
}

private class CapturingTypefaceTextMeasurer : TextMeasurer {
    var metricsTypefaceId: Int? = null

    override fun measureTextWidth(text: String, font: Font, paint: Paint): Float =
        text.length * 10f

    override fun metrics(font: Font): TextMetrics {
        metricsTypefaceId = font.typeface!!.uniqueId
        return TextMetrics(-8f, 2f)
    }
}

private fun assertFloatEquals(expected: Float, actual: Float) {
    assertTrue(
        abs(expected - actual) <= EPSILON,
        "期望 $expected，实际 $actual"
    )
}

private fun assertElementBounds(element: UiElement, x: Float, y: Float, width: Float, height: Float) {
    assertFloatEquals(x, element.x)
    assertFloatEquals(y, element.y)
    assertFloatEquals(width, element.width)
    assertFloatEquals(height, element.height)
}

private fun testImage(width: Int, height: Int): Image =
    Surface.makeRasterN32Premul(width, height).use { it.makeImageSnapshot() }

class ComposeLayoutUnitTest {
    @Test
    fun columnMeasuresPaddingAndHorizontalAlignment() {
        val root = Column(horizontalAlignment = HorizontalAlignment.Center)
        val first = CanvasElement(20f, 10f) {}
        first.modifier = Modifier.padding(top = 1f, right = 4f, bottom = 2f, left = 3f)
        val second = CanvasElement(40f, 10f) {}

        root.add(first)
        root.add(second)
        root.measure(MeasureContext(FixedTextMeasurer()))
        root.layout(0f, 0f)

        assertFloatEquals(40f, root.width)
        assertFloatEquals(23f, root.height)
        assertElementBounds(first, x = 6.5f, y = 0f, width = 27f, height = 13f)
        assertElementBounds(second, x = 0f, y = 13f, width = 40f, height = 10f)
    }

    @Test
    fun rowUsesSizeOverrideAndVerticalAlignment() {
        val root = Row(verticalAlignment = VerticalAlignment.Center)
        root.modifier = Modifier.size(width = 30f, height = 40f)
        val first = CanvasElement(10f, 10f) {}
        first.modifier = Modifier.padding(top = 5f, bottom = 5f)
        val second = CanvasElement(5f, 30f) {}

        root.add(first)
        root.add(second)
        root.measure(MeasureContext(FixedTextMeasurer()))
        root.layout(0f, 0f)

        assertElementBounds(root, x = 0f, y = 0f, width = 30f, height = 40f)
        assertElementBounds(first, x = 0f, y = 10f, width = 10f, height = 20f)
        assertElementBounds(second, x = 10f, y = 5f, width = 5f, height = 30f)
    }

    @Test
    fun boxAlignsChildrenInsidePaddingAndBorder() {
        val root = Box(
            horizontalAlignment = HorizontalAlignment.Right,
            verticalAlignment = VerticalAlignment.Bottom
        )
        root.modifier = Modifier
            .size(100f, 80f)
            .padding(10f)
            .border(2f, Color.RED)
        val child = CanvasElement(20f, 10f) {}
        child.modifier = Modifier.padding(right = 5f, bottom = 3f)

        root.add(child)
        root.measure(MeasureContext(FixedTextMeasurer()))
        root.layout(0f, 0f)

        assertFloatEquals(100f, root.width)
        assertFloatEquals(80f, root.height)
        assertElementBounds(child, x = 63f, y = 55f, width = 25f, height = 13f)
    }

    @Test
    fun remainingAlignmentBranchesAreCovered() {
        val column = Column(horizontalAlignment = HorizontalAlignment.Right)
        val columnChild = CanvasElement(10f, 10f) {}
        column.modifier = Modifier.size(30f, 10f)
        column.add(columnChild)

        val row = Row(verticalAlignment = VerticalAlignment.Bottom)
        val rowChild = CanvasElement(10f, 10f) {}
        row.modifier = Modifier.size(10f, 30f)
        row.add(rowChild)

        val box = Box(
            horizontalAlignment = HorizontalAlignment.Center,
            verticalAlignment = VerticalAlignment.Center
        )
        val boxChild = CanvasElement(10f, 10f) {}
        box.modifier = Modifier.size(30f, 30f)
        box.add(boxChild)

        listOf(column, row, box).forEach {
            it.measure(MeasureContext(FixedTextMeasurer()))
            it.layout(0f, 0f)
        }

        assertElementBounds(columnChild, x = 20f, y = 0f, width = 10f, height = 10f)
        assertElementBounds(rowChild, x = 0f, y = 20f, width = 10f, height = 10f)
        assertElementBounds(boxChild, x = 10f, y = 10f, width = 10f, height = 10f)
    }

    @Test
    fun heatmapStyleLayoutUsesSingleAxisSizeAndHorizontalVerticalPadding() {
        val root = Column()
        val fontName = ComposeFontManager.register("unit-heatmap-empty", Typeface.makeEmpty())

        root.apply {
            row(Modifier.padding(top = 20f)) {
                column(Modifier.padding(right = 10f), horizontalAlignment = HorizontalAlignment.Right) {
                    text(" ", fontSize = 10f, fontFamily = fontName)
                    text("Mon", Modifier.padding(horizontal = 3f, vertical = 0f), fontSize = 10f, fontFamily = fontName)
                    box(Modifier.height(16f))
                    text("Wed", fontSize = 10f, fontFamily = fontName)
                }
                column {
                    box(
                        Modifier
                            .padding(3f)
                            .size(15f)
                            .background(Color.RED)
                            .border(.5f, Color.BLACK)
                            .clip(Shape.RoundedRect(3f))
                    )
                }
            }
        }

        root.measure(MeasureContext(FixedTextMeasurer()))
        root.layout(0f, 0f)

        val row = assertIs<Row>(root.children.single())
        val labels = assertIs<Column>(row.children[0])
        val heatmap = assertIs<Column>(row.children[1])
        assertElementBounds(row, x = 0f, y = 0f, width = 67f, height = 66f)
        assertElementBounds(labels.children[2], x = 36f, y = 40f, width = 0f, height = 16f)
        assertElementBounds(heatmap.children.single(), x = 46f, y = 20f, width = 21f, height = 21f)
    }
}

class ComposeModifierCommandUnitTest {
    @Test
    fun backgroundAndBorderRecordExpectedRects() {
        val root = Box()
        root.modifier = Modifier
            .size(10f, 20f)
            .background(Color.BLUE)
            .border(top = 1f, right = 2f, bottom = 3f, left = 4f, color = Color.RED)
        root.measure(MeasureContext(FixedTextMeasurer()))
        root.layout(0f, 0f)
        val recorder = RecordingDrawCanvas()

        root.draw(DrawContext(recorder))

        val rects = recorder.commands.filterIsInstance<DrawCommand.Rect>()
        assertEquals(5, rects.size)
        assertEquals(Color.BLUE, rects[0].paint.color)
        assertFloatEquals(10f, rects[0].rect.width)
        assertFloatEquals(20f, rects[0].rect.height)
        assertEquals(Color.RED, rects[1].paint.color)
        assertFloatEquals(1f, rects[1].rect.height)
        assertFloatEquals(3f, rects[2].rect.height)
        assertFloatEquals(4f, rects[3].rect.width)
        assertFloatEquals(2f, rects[4].rect.width)
    }

    @Test
    fun orderedPaddingBackgroundAndBorderUseNestedBounds() {
        val root = Box()
        root.modifier = Modifier
            .padding(2f)
            .background(Color.BLUE)
            .padding(top = 1f, right = 4f, bottom = 2f, left = 3f)
            .border(1f, Color.RED)
            .size(10f, 8f)
        root.measure(MeasureContext(FixedTextMeasurer()))
        root.layout(0f, 0f)
        val recorder = RecordingDrawCanvas()

        root.draw(DrawContext(recorder))

        assertElementBounds(root, x = 0f, y = 0f, width = 23f, height = 17f)
        val rects = recorder.commands.filterIsInstance<DrawCommand.Rect>()
        assertEquals(5, rects.size)

        val background = rects[0]
        assertEquals(Color.BLUE, background.paint.color)
        assertFloatEquals(2f, background.rect.left)
        assertFloatEquals(2f, background.rect.top)
        assertFloatEquals(19f, background.rect.width)
        assertFloatEquals(13f, background.rect.height)

        val topBorder = rects[1]
        assertEquals(Color.RED, topBorder.paint.color)
        assertFloatEquals(5f, topBorder.rect.left)
        assertFloatEquals(3f, topBorder.rect.top)
        assertFloatEquals(12f, topBorder.rect.width)
        assertFloatEquals(1f, topBorder.rect.height)
    }

    @Test
    fun sizeOrderChangesMeasuredBoundsLikeNestedModifiers() {
        val outerSize = Box().apply {
            modifier = Modifier
                .size(10f)
                .padding(2f)
        }
        val innerSize = Box().apply {
            modifier = Modifier
                .padding(2f)
                .size(10f)
        }

        listOf(outerSize, innerSize).forEach {
            it.measure(MeasureContext(FixedTextMeasurer()))
            it.layout(0f, 0f)
        }

        assertElementBounds(outerSize, x = 0f, y = 0f, width = 10f, height = 10f)
        assertElementBounds(innerSize, x = 0f, y = 0f, width = 14f, height = 14f)
    }

    @Test
    fun sizeInConstrainsGenericElementMeasurement() {
        val shrink = Box().apply {
            modifier = Modifier
                .sizeIn(maxWidth = 30f, maxHeight = 40f)
                .size(50f)
        }
        val expand = Box().apply {
            modifier = Modifier
                .sizeIn(minWidth = 20f, minHeight = 30f)
                .size(10f)
        }

        listOf(shrink, expand).forEach {
            it.measure(MeasureContext(FixedTextMeasurer()))
            it.layout(0f, 0f)
        }

        assertElementBounds(shrink, x = 0f, y = 0f, width = 30f, height = 40f)
        assertElementBounds(expand, x = 0f, y = 0f, width = 20f, height = 30f)
    }

    @Test
    fun clipWrapsElementDrawingWithSaveAndRestore() {
        val commands = renderCommands(
            backgroundColor = Color.TRANSPARENT,
            measureContext = MeasureContext(FixedTextMeasurer())
        ) {
            box(
                modifier = Modifier
                    .size(20f)
                    .clip(top.e404.skiko.draw.compose.Shape.Circle)
                    .background(Color.RED)
            )
        }

        assertTrue(commands[1] is DrawCommand.Save)
        assertTrue(commands[2] is DrawCommand.ClipPath)
        assertTrue(commands[3] is DrawCommand.Rect)
        assertTrue(commands[4] is DrawCommand.Restore)
    }

    @Test
    fun roundedRectClipAndAntiAliasAreRecorded() {
        val commands = renderCommands {
            box(
                modifier = Modifier
                    .size(20f)
                    .antiAlias(false)
                    .clip(Shape.RoundedRect(4f))
                    .background(Color.RED)
            )
        }

        assertTrue(commands.any { it is DrawCommand.ClipPath })
        val rect = commands.filterIsInstance<DrawCommand.Rect>().single()
        assertEquals(false, rect.paint.antiAlias)
    }

    @Test
    fun wakatimeStyleProgressBarRecordsBackgroundAndForegroundBars() {
        val commands = renderCommands {
            box(Modifier.width(100f)) {
                box(
                    Modifier
                        .width(100f)
                        .height(10f)
                        .clip(Shape.RoundedRect(50f))
                        .background(Color.makeRGB(128, 128, 128))
                )
                box(
                    Modifier
                        .width(25f)
                        .height(10f)
                        .clip(Shape.RoundedRect(50f))
                        .background(Color.GREEN)
                )
            }
        }

        val rects = commands.filterIsInstance<DrawCommand.Rect>()
        assertEquals(2, rects.size)
        assertFloatEquals(100f, rects[0].rect.width)
        assertFloatEquals(25f, rects[1].rect.width)
        assertTrue(commands.filterIsInstance<DrawCommand.ClipPath>().size >= 2)
    }
}

class ComposeTextUnitTest {
    @Test
    fun fontFamilyUsesRegisteredTypefaceName() {
        val typeface = Typeface.makeEmpty()
        val fontName = ComposeFontManager.register("unit-text-empty", typeface)
        val measurer = CapturingTypefaceTextMeasurer()

        renderCommands(measureContext = MeasureContext(measurer)) {
            text("abc", fontSize = 12f, fontFamily = fontName)
        }

        assertEquals(typeface.uniqueId, measurer.metricsTypefaceId)
    }

    @Test
    fun fontManagerResolvesSystemFamilyAndFallback() {
        val families = ComposeFontManager.systemFamilies()
        if (families.isNotEmpty()) {
            ComposeFontManager.registerSystem("unit-system-font", families.first())
            assertTrue(ComposeFontManager.resolve("unit-system-font").uniqueId >= 0)
        }

        assertTrue(ComposeFontManager.resolve("unit-missing-font").uniqueId >= 0)
    }

    @Test
    fun ellipsisTruncatesTextToFitMaxWidth() {
        val commands = renderCommands(
            measureContext = MeasureContext(FixedTextMeasurer())
        ) {
            text(
                "abcdef",
                Modifier
                    .sizeIn(maxWidth = 30f),
                fontSize = 20f,
                textOverflow = TextOverflow.Ellipsis
            )
        }

        val command = commands.filterIsInstance<DrawCommand.Text>().single()
        assertEquals("ab…", command.text)
        assertFloatEquals(8f, command.baselineY)
        assertFloatEquals(20f, command.font.size)
    }

    @Test
    fun wrapSplitsWordsAndAppliesMaxHeightEllipsis() {
        val commands = renderCommands(
            measureContext = MeasureContext(FixedTextMeasurer())
        ) {
            text(
                "aa bbbb c",
                Modifier
                    .sizeIn(maxWidth = 40f, maxHeight = 20f)
            )
        }

        val texts = commands.filterIsInstance<DrawCommand.Text>()
        assertEquals(listOf("aa", "bbb…"), texts.map { it.text })
        assertFloatEquals(8f, texts[0].baselineY)
        assertFloatEquals(18f, texts[1].baselineY)
    }

    @Test
    fun textDrawPositionIncludesPaddingAndBorder() {
        val commands = renderCommands(
            measureContext = MeasureContext(FixedTextMeasurer())
        ) {
            text(
                "hi",
                Modifier
                    .padding(left = 3f, top = 4f)
                    .border(left = 2f, top = 1f, color = Color.RED),
                textColor = Color.GREEN
            )
        }

        val command = commands.filterIsInstance<DrawCommand.Text>().single()
        assertFloatEquals(5f, command.x)
        assertFloatEquals(13f, command.baselineY)
        assertEquals(Color.GREEN, command.paint.color)
    }
}

class ComposeImageUnitTest {
    @Test
    fun imageScaleKeepsAspectRatioAndDoesNotCrop() {
        val image = testImage(100, 50)
        val commands = renderCommands {
            image(
                image,
                Modifier
                    .sizeIn(maxWidth = 50f, maxHeight = 50f),
                imageOverflow = ImageOverflow.Scale
            )
        }

        val command = commands.filterIsInstance<DrawCommand.ImageRect>().single()
        assertFloatEquals(100f, command.src.width)
        assertFloatEquals(50f, command.src.height)
        assertFloatEquals(50f, command.dst.width)
        assertFloatEquals(25f, command.dst.height)
    }

    @Test
    fun imageCropUsesCenteredSourceRect() {
        val image = testImage(100, 50)
        val commands = renderCommands {
            image(
                image,
                Modifier
                    .sizeIn(maxWidth = 40f, maxHeight = 30f),
                imageOverflow = ImageOverflow.Crop
            )
        }

        val command = commands.filterIsInstance<DrawCommand.ImageRect>().single()
        assertFloatEquals(30f, command.src.left)
        assertFloatEquals(10f, command.src.top)
        assertFloatEquals(40f, command.src.width)
        assertFloatEquals(30f, command.src.height)
        assertFloatEquals(40f, command.dst.width)
        assertFloatEquals(30f, command.dst.height)
    }

    @Test
    fun imageWithoutSizeInUsesOriginalSizeAndDslEntry() {
        val image = testImage(12, 8)
        val root = Column()

        root.apply {
            image(image)
        }
        root.measure(MeasureContext())
        root.layout(0f, 0f)

        assertFloatEquals(12f, root.width)
        assertFloatEquals(8f, root.height)
    }
}

class ComposeTableUnitTest {
    @Test
    fun tableUsesMaxColumnWidthsAndRowSpacing() {
        val table = Table(columnSpacing = 2f, rowSpacing = 3f)
        table.apply {
            tableRow {
                cell { text("aa") }
                cell { text("b") }
            }
            tableRow {
                cell { text("c") }
                cell { text("dddd") }
            }
        }

        table.measure(MeasureContext(FixedTextMeasurer()))
        table.layout(0f, 0f)

        assertFloatEquals(62f, table.width)
        assertFloatEquals(23f, table.height)
        assertElementBounds(table.rows[0].cells[0], x = 0f, y = 0f, width = 20f, height = 10f)
        assertElementBounds(table.rows[0].cells[1], x = 22f, y = 0f, width = 40f, height = 10f)
        assertElementBounds(table.rows[1].cells[0], x = 0f, y = 13f, width = 20f, height = 10f)
        assertElementBounds(table.rows[1].cells[1], x = 22f, y = 13f, width = 40f, height = 10f)
    }

    @Test
    fun tableCellAlignsContentAndTableRowFallbackMeasuresChildren() {
        val row = TableRow()
        row.cell(
            modifier = Modifier.size(30f, 30f),
            horizontalAlignment = HorizontalAlignment.Right,
            verticalAlignment = VerticalAlignment.Bottom
        ) {
            box(Modifier.size(10f))
        }

        row.measure(MeasureContext(FixedTextMeasurer()))
        row.layout(0f, 0f)

        val cell = row.cells.single()
        cell.layout(0f, 0f)
        val child = cell.content!!
        assertFloatEquals(30f, row.width)
        assertFloatEquals(30f, row.height)
        assertElementBounds(child, x = 20f, y = 20f, width = 10f, height = 10f)
    }
}

class ComposeCanvasAndChartUnitTest {
    @Test
    fun canvasElementReceivesParentPositionAndRecordsCustomDraw() {
        val commands = renderCommands(
            measureContext = MeasureContext(FixedTextMeasurer())
        ) {
            val element = CanvasElement(10f, 10f) { canvas ->
                canvas.drawCircle(parentX + 1f, parentY + 2f, 3f, Paint().apply { color = Color.RED })
            }
            element.modifier = Modifier.padding(left = 4f, top = 5f)
            add(element)
        }

        val command = commands.filterIsInstance<DrawCommand.Circle>().single()
        assertFloatEquals(5f, command.x)
        assertFloatEquals(7f, command.y)
        assertFloatEquals(3f, command.radius)
        assertEquals(Color.RED, command.paint.color)
    }

    @Test
    fun barChartRecordsClipArcsAndOutlineCircles() {
        val commands = renderCommands {
            bar(
                theme = BarTheme(
                    outerRadius = 10f,
                    innerRadius = 5f,
                    strokeColor = Color.WHITE,
                    strokeWidth = 1f
                ),
                data = listOf(Color.RED to 1f, Color.BLUE to 3f)
            )
        }

        assertTrue(commands.any { it is DrawCommand.ClipPath })
        val arcs = commands.filterIsInstance<DrawCommand.Arc>()
        assertEquals(4, arcs.size)
        assertFloatEquals(-90f, arcs[0].startAngle)
        assertFloatEquals(90f, arcs[0].sweepAngle)
        assertEquals(PaintMode.FILL, arcs[0].paint.mode)
        assertFloatEquals(0f, arcs[2].startAngle)
        assertFloatEquals(270f, arcs[2].sweepAngle)
        val circles = commands.filterIsInstance<DrawCommand.Circle>()
        assertEquals(2, circles.size)
        assertFloatEquals(9f, circles[0].radius)
        assertFloatEquals(5f, circles[1].radius)
    }

    @Test
    fun radarChartRecordsPathsLinesAndTextLines() {
        val recorder = RecordingDrawCanvas()
        val theme = RadarTheme(
            width = 100f,
            height = 100f,
            radius = 30f,
            gridCount = 2,
            gridFontProvider = { "g$it" },
            labelFixPolicy = RadarFixPolicy.NONE,
            labelFont = Font(Typeface.makeEmpty(), 10f),
            gridFont = Font(Typeface.makeEmpty(), 8f)
        )

        drawRadarChart(
            recorder,
            parentX = 0f,
            parentY = 0f,
            data = listOf("a" to 0.2f, "b" to 0.6f, "c" to 1f),
            theme = theme
        )

        assertEquals(5, recorder.commands.filterIsInstance<DrawCommand.Path>().size)
        assertEquals(3, recorder.commands.filterIsInstance<DrawCommand.Line>().size)
        assertEquals(5, recorder.commands.filterIsInstance<DrawCommand.TextLine>().size)
    }

    @Test
    fun radarFixPoliciesUsedByConsumersProduceFiniteLabelPositions() {
        val line = org.jetbrains.skia.TextLine.make("Commit", Font(Typeface.makeEmpty(), 10f))
        val theme = RadarTheme(width = 100f, height = 100f, radius = 30f, labelOuterLength = 10f)

        listOf(
            RadarFixPolicy.NONE,
            RadarFixPolicy.MOVE_OUTSIDE,
            RadarFixPolicy.TILT
        ).forEach { policy ->
            val (x, y) = policy.fix(
                -Math.PI / 2,
                1.5,
                line,
                50f,
                50f,
                theme
            )
            assertTrue(x.isFinite(), "$policy x 应为有限值")
            assertTrue(y.isFinite(), "$policy y 应为有限值")
        }
    }

    @Test
    fun radarDslAddsCanvasElementWithThemeSize() {
        val root = Column()

        root.apply {
            radar(
                RadarTheme(width = 80f, height = 60f, radius = 20f),
                listOf("a" to 1f, "b" to 0.5f, "c" to 0.2f)
            )
        }
        root.measure(MeasureContext())

        assertFloatEquals(80f, root.width)
        assertFloatEquals(60f, root.height)
    }
}

class ComposeIconUnitTest {
    @Test
    fun drawIconRecordsTransformAndPathCommands() {
        val recorder = RecordingDrawCanvas()
        val path = Path().apply { moveTo(0f, 0f); lineTo(10f, 0f) }

        drawIcon(
            canvas = recorder,
            path = path,
            viewBox = Rect.makeXYWH(0f, 0f, 10f, 20f),
            theme = IconTheme(size = 40f, scale = 1f, color = Color.RED),
            parentX = 3f,
            parentY = 4f
        )

        assertTrue(recorder.commands[0] is DrawCommand.Save)
        assertEquals(DrawCommand.Translate(13f, 4f), recorder.commands[1])
        assertEquals(DrawCommand.Scale(2f, 2f), recorder.commands[2])
        val originTranslate = assertIs<DrawCommand.Translate>(recorder.commands[3])
        assertFloatEquals(0f, originTranslate.dx)
        assertFloatEquals(0f, originTranslate.dy)
        val drawPath = assertIs<DrawCommand.Path>(recorder.commands[4])
        assertEquals(Color.RED, drawPath.paint.color)
        assertTrue(recorder.commands[5] is DrawCommand.Restore)
    }

    @Test
    fun iconDslParsesSvgAndDrawsPath() {
        val svg = """<svg viewBox="0 0 10 10"><path d="M 0 0 L 10 0 L 10 10 Z"/></svg>"""
        val commands = renderCommands {
            icon(IconTheme(size = 20f), svg)
        }

        assertTrue(commands.any { it is DrawCommand.Path })
        assertTrue(commands.any { it is DrawCommand.Scale })
    }

    @Test
    fun githubFooterStyleIconAndTextRowRecordsIconPathAndText() {
        val svg = """<svg viewBox="0 0 10 10"><path d="M 0 0 L 10 0 L 10 10 Z"/></svg>"""
        val fontName = ComposeFontManager.register("unit-footer-empty", Typeface.makeEmpty())
        val commands = renderCommands(measureContext = MeasureContext(FixedTextMeasurer())) {
            row(Modifier.padding(horizontal = 50f), VerticalAlignment.Center) {
                icon(IconTheme(40f, color = Color.WHITE), svg)
                text(
                    "42",
                    Modifier
                        .padding(left = 20f, right = 50f),
                    fontSize = 40f,
                    textColor = Color.WHITE,
                    fontFamily = fontName
                )
            }
        }

        assertTrue(commands.any { it is DrawCommand.Path })
        val text = commands.filterIsInstance<DrawCommand.Text>().single()
        assertEquals("42", text.text)
        assertFloatEquals(110f, text.x)
    }
}

class ComposeDslAndRenderUnitTest {
    @Test
    fun dslEntriesBuildExpectedTree() {
        val root = Column()

        root.apply {
            column(Modifier.width(20f)) {
                row(Modifier.height(10f)) {
                    box(Modifier.size(5f))
                }
            }
            table(Modifier.padding(horizontal = 1f, vertical = 2f), columnSpacing = 1f, rowSpacing = 1f) {
                tableRow {
                    cell { text("x") }
                }
            }
        }

        assertEquals(2, root.children.size)
        assertIs<Column>(root.children[0])
        assertIs<Table>(root.children[1])
    }

    @Test
    fun iconTextRequiresFontSizeAndBuildsRow() {
        val fontName = ComposeFontManager.register("unit-icon-text-empty", Typeface.makeEmpty())
        val commands = renderCommands(measureContext = MeasureContext(FixedTextMeasurer())) {
            iconText("ok", fontSize = 20f, fontFamily = fontName)
        }

        val clipIndex = commands.indexOfFirst { it is DrawCommand.ClipPath }
        val iconRectIndex = commands.indexOfFirst { it is DrawCommand.Rect && it.paint.color == Colors.LIGHT_BLUE.argb }
        assertTrue(clipIndex >= 0, "应记录 iconText 图标裁剪命令")
        assertTrue(iconRectIndex > clipIndex, "图标背景应在裁剪之后绘制")
        assertEquals("ok", commands.filterIsInstance<DrawCommand.Text>().single().text)
    }

    @Test
    fun invalidRenderSizeThrowsAndOverflowChildrenAreNormalized() {
        val error = assertFailsWith<IllegalStateException> {
            renderCommands(measureContext = MeasureContext(FixedTextMeasurer())) {}
        }
        assertTrue(error.message!!.contains("计算尺寸无效"))

        val root = Column().apply {
            modifier = Modifier.size(10f, 10f)
        }
        val commands = renderCommands(root = root) {
            val element = CanvasElement(20f, 30f) { canvas ->
                canvas.drawRect(Rect.makeXYWH(parentX, parentY, width, height), Paint())
            }
            add(element)
        }

        val rect = commands.filterIsInstance<DrawCommand.Rect>().single()
        assertFloatEquals(10f, rect.rect.width)
        assertFloatEquals(10f, rect.rect.height)
    }

    @Test
    fun renderAndSkiaBackendsAreExercised() {
        val image = render(backgroundColor = Color.WHITE) {
            box(Modifier.size(2f))
        }
        assertTrue(image.width > 0)
        assertTrue(image.height > 0)

        val font = Font(Typeface.makeEmpty(), 12f)
        val paint = Paint()
        assertFloatEquals(font.measureTextWidth("abc", paint), SkiaTextMeasurer.measureTextWidth("abc", font, paint))
        assertFloatEquals(font.metrics.descent - font.metrics.ascent, SkiaTextMeasurer.metrics(font).lineHeight)

        Surface.makeRasterN32Premul(20, 20).use { surface ->
            val canvas = SkiaDrawCanvas(surface.canvas)
            val path = Path().apply { moveTo(0f, 0f); lineTo(10f, 10f) }
            val skiaImage = testImage(2, 2)
            canvas.clear(Color.TRANSPARENT)
            canvas.save()
            canvas.translate(1f, 1f)
            canvas.scale(1f, 1f)
            canvas.clipPath(path)
            canvas.drawRect(Rect.makeXYWH(0f, 0f, 1f, 1f), paint)
            canvas.drawString("x", 0f, 10f, font, paint)
            canvas.drawImageRect(skiaImage, Rect.makeXYWH(0f, 0f, 2f, 2f), Rect.makeXYWH(0f, 0f, 2f, 2f), paint)
            canvas.drawPath(path, paint)
            canvas.drawArc(0f, 0f, 10f, 10f, 0f, 90f, true, paint)
            canvas.drawCircle(5f, 5f, 2f, paint)
            canvas.drawLine(0f, 0f, 10f, 10f, paint)
            canvas.restore()
        }
    }

    @Test
    fun debugBaseElementPrintsTreeAndModifiers() {
        val root = Column()
        root.apply {
            text("x", Modifier.padding(1f), fontSize = 12f)
        }
        root.measure(MeasureContext(FixedTextMeasurer()))
        root.layout(0f, 0f)

        val output = buildString { debugBaseElement(0, root, this) }

        assertTrue(output.contains("children:"))
        assertTrue(output.contains("modifiers:"))
        assertTrue(output.contains("contentWidth:"))
    }
}
