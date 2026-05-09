package top.e404.skiko.draw.compose

import org.jetbrains.skia.*

class MeasureContext(
    val textMeasurer: TextMeasurer = SkiaTextMeasurer
)

class DrawContext(
    val canvas: DrawCanvas
)

data class TextMetrics(
    val ascent: Float,
    val descent: Float
) {
    val lineHeight: Float get() = descent - ascent
}

interface TextMeasurer {
    fun measureTextWidth(text: String, font: Font, paint: Paint): Float
    fun metrics(font: Font): TextMetrics
}

object SkiaTextMeasurer : TextMeasurer {
    override fun measureTextWidth(text: String, font: Font, paint: Paint): Float =
        font.measureTextWidth(text, paint)

    override fun metrics(font: Font): TextMetrics =
        font.metrics.let { TextMetrics(it.ascent, it.descent) }
}

interface DrawCanvas {
    fun clear(color: Int)
    fun save()
    fun restore()
    fun translate(dx: Float, dy: Float)
    fun scale(sx: Float, sy: Float)
    fun clipPath(path: Path, antiAlias: Boolean = true)
    fun drawRect(rect: Rect, paint: Paint)
    fun drawString(text: String, x: Float, y: Float, font: Font, paint: Paint)
    fun drawTextLine(line: TextLine, x: Float, y: Float, paint: Paint)
    fun drawImageRect(image: Image, src: Rect, dst: Rect, paint: Paint)
    fun drawPath(path: Path, paint: Paint)
    fun drawArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        includeCenter: Boolean,
        paint: Paint
    )
    fun drawCircle(x: Float, y: Float, radius: Float, paint: Paint)
    fun drawLine(x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint)
}

class SkiaDrawCanvas(private val canvas: Canvas) : DrawCanvas {
    override fun clear(color: Int) {
        canvas.clear(color)
    }
    override fun save() {
        canvas.save()
    }
    override fun restore() {
        canvas.restore()
    }
    override fun translate(dx: Float, dy: Float) {
        canvas.translate(dx, dy)
    }
    override fun scale(sx: Float, sy: Float) {
        canvas.scale(sx, sy)
    }
    override fun clipPath(path: Path, antiAlias: Boolean) {
        canvas.clipPath(path, antiAlias)
    }
    override fun drawRect(rect: Rect, paint: Paint) {
        canvas.drawRect(rect, paint)
    }
    override fun drawString(text: String, x: Float, y: Float, font: Font, paint: Paint) {
        canvas.drawString(text, x, y, font, paint)
    }
    override fun drawTextLine(line: TextLine, x: Float, y: Float, paint: Paint) {
        canvas.drawTextLine(line, x, y, paint)
    }
    override fun drawImageRect(image: Image, src: Rect, dst: Rect, paint: Paint) {
        canvas.drawImageRect(image, src, dst, paint)
    }
    override fun drawPath(path: Path, paint: Paint) {
        canvas.drawPath(path, paint)
    }
    override fun drawArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        includeCenter: Boolean,
        paint: Paint
    ) {
        canvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, includeCenter, paint)
    }
    override fun drawCircle(x: Float, y: Float, radius: Float, paint: Paint) {
        canvas.drawCircle(x, y, radius, paint)
    }
    override fun drawLine(x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) {
        canvas.drawLine(x0, y0, x1, y1, paint)
    }
}

data class PaintSnapshot(
    val color: Int,
    val mode: PaintMode,
    val strokeWidth: Float,
    val antiAlias: Boolean
) {
    companion object {
        fun from(paint: Paint) = PaintSnapshot(
            color = paint.color,
            mode = paint.mode,
            strokeWidth = paint.strokeWidth,
            antiAlias = paint.isAntiAlias
        )
    }
}

data class FontSnapshot(val size: Float) {
    companion object {
        fun from(font: Font) = FontSnapshot(size = font.size)
    }
}

data class RectSnapshot(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    companion object {
        fun from(rect: Rect) = RectSnapshot(rect.left, rect.top, rect.right, rect.bottom)
    }
}

data class PathSnapshot(val description: String) {
    companion object {
        fun from(path: Path) = PathSnapshot(path.toString())
    }
}

sealed interface DrawCommand {
    data class Clear(val color: Int) : DrawCommand
    data class Save(val depth: Int) : DrawCommand
    data class Restore(val depth: Int) : DrawCommand
    data class Translate(val dx: Float, val dy: Float) : DrawCommand
    data class Scale(val sx: Float, val sy: Float) : DrawCommand
    data class ClipPath(val path: PathSnapshot, val antiAlias: Boolean) : DrawCommand
    data class Rect(val rect: RectSnapshot, val paint: PaintSnapshot) : DrawCommand
    data class Text(
        val text: String,
        val x: Float,
        val baselineY: Float,
        val font: FontSnapshot,
        val paint: PaintSnapshot
    ) : DrawCommand
    data class TextLine(
        val width: Float,
        val height: Float,
        val x: Float,
        val y: Float,
        val paint: PaintSnapshot
    ) : DrawCommand
    data class ImageRect(
        val imageWidth: Int,
        val imageHeight: Int,
        val src: RectSnapshot,
        val dst: RectSnapshot,
        val paint: PaintSnapshot
    ) : DrawCommand
    data class Path(val path: PathSnapshot, val paint: PaintSnapshot) : DrawCommand
    data class Arc(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        val startAngle: Float,
        val sweepAngle: Float,
        val includeCenter: Boolean,
        val paint: PaintSnapshot
    ) : DrawCommand
    data class Circle(val x: Float, val y: Float, val radius: Float, val paint: PaintSnapshot) : DrawCommand
    data class Line(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val paint: PaintSnapshot) : DrawCommand
}

class RecordingDrawCanvas : DrawCanvas {
    val commands: MutableList<DrawCommand> = mutableListOf()
    private var depth = 0

    override fun clear(color: Int) {
        commands += DrawCommand.Clear(color)
    }
    override fun save() {
        depth += 1
        commands += DrawCommand.Save(depth)
    }
    override fun restore() {
        commands += DrawCommand.Restore(depth)
        depth = (depth - 1).coerceAtLeast(0)
    }
    override fun translate(dx: Float, dy: Float) {
        commands += DrawCommand.Translate(dx, dy)
    }
    override fun scale(sx: Float, sy: Float) {
        commands += DrawCommand.Scale(sx, sy)
    }
    override fun clipPath(path: Path, antiAlias: Boolean) {
        commands += DrawCommand.ClipPath(PathSnapshot.from(path), antiAlias)
    }
    override fun drawRect(rect: Rect, paint: Paint) {
        commands += DrawCommand.Rect(RectSnapshot.from(rect), PaintSnapshot.from(paint))
    }
    override fun drawString(text: String, x: Float, y: Float, font: Font, paint: Paint) {
        commands += DrawCommand.Text(text, x, y, FontSnapshot.from(font), PaintSnapshot.from(paint))
    }
    override fun drawTextLine(line: TextLine, x: Float, y: Float, paint: Paint) {
        commands += DrawCommand.TextLine(line.width, line.height, x, y, PaintSnapshot.from(paint))
    }
    override fun drawImageRect(image: Image, src: Rect, dst: Rect, paint: Paint) {
        commands += DrawCommand.ImageRect(
            image.width,
            image.height,
            RectSnapshot.from(src),
            RectSnapshot.from(dst),
            PaintSnapshot.from(paint)
        )
    }
    override fun drawPath(path: Path, paint: Paint) {
        commands += DrawCommand.Path(PathSnapshot.from(path), PaintSnapshot.from(paint))
    }
    override fun drawArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        includeCenter: Boolean,
        paint: Paint
    ) {
        commands += DrawCommand.Arc(
            left,
            top,
            right,
            bottom,
            startAngle,
            sweepAngle,
            includeCenter,
            PaintSnapshot.from(paint)
        )
    }
    override fun drawCircle(x: Float, y: Float, radius: Float, paint: Paint) {
        commands += DrawCommand.Circle(x, y, radius, PaintSnapshot.from(paint))
    }
    override fun drawLine(x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) {
        commands += DrawCommand.Line(x0, y0, x1, y1, PaintSnapshot.from(paint))
    }
}
