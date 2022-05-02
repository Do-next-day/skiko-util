package top.e404.skiko.handler.face

import org.jetbrains.skia.*
import top.e404.skiko.Colors
import top.e404.skiko.FontType
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.handle
import top.e404.skiko.handler.face.WriteHandler.Location.*
import top.e404.skiko.util.*
import kotlin.math.min

@ImageHandler
object WriteHandler : FramesHandler {
    private const val minSize = 20
    private const val maxSize = 1000
    private const val unit = 10
    private val tf = FontType.MI.getSkiaTypeface()

    override val name = "write"
    override val regex = Regex("(?i)写|write")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val text = args["text"]!!
        var size = args["size"]?.toIntOrNull()
        var stroke = args["stroke"]?.toIntOrNull()
        val bgColor = args["bgColor"]?.asColor() ?: Colors.WHITE.argb
        val textColor = args["textColor"]?.asColor() ?: Colors.WHITE.argb
        val strokeColor = args["strokeColor"]?.asColor() ?: Colors.BLACK.argb
        val location = args["location"]?.let { Location.valueOf(it.uppercase()) } ?: OUTSIDE_BOTTOM
        val texts = text.split("\n")
        val w = frames[0].image.width
        val maxWidth = w - min(20, w * 10 / 9)
        size = size ?: texts.minOf {
            autoSize(tf, it, minSize, maxSize, maxWidth, unit)
        }
        stroke = stroke ?: (size / 10)
        val spacing = min(size / 3, 30)
        val font = Font(tf, size.toFloat())
        val lines = texts.map { TextLine.make(it, font) }
        return frames.result {
            this@result.handle {
                val paint = Paint()
                val height = lines.sumOf { it.descent.toDouble() - it.ascent }.toInt() + (lines.size - 1) * spacing
                when (location) {
                    CENTER -> toSurface().withCanvas {
                        var y = (this@handle.height - height) / 2F
                        drawImage(this@handle, 0F, 0F)
                        for (line in lines) {
                            val x = (this@handle.width - line.width) / 2
                            y -= line.ascent
                            drawTextLine(line, x, y, paint.apply {
                                color = strokeColor
                                strokeWidth = stroke.toFloat()
                                isAntiAlias = true
                                mode = PaintMode.STROKE_AND_FILL
                            })
                            drawTextLine(line, x, y, paint.apply {
                                mode = PaintMode.FILL
                                color = textColor
                            })
                            y += line.descent + spacing
                        }
                    }
                    INSIDE_TOP -> toSurface().withCanvas {
                        var y = 0F
                        drawImage(this@handle, 0F, 0F)
                        for (line in lines) {
                            val x = (this@handle.width - line.width) / 2
                            y -= line.ascent
                            drawTextLine(line, x, y, paint.apply {
                                color = strokeColor
                                strokeWidth = stroke.toFloat()
                                isAntiAlias = true
                                mode = PaintMode.STROKE_AND_FILL
                            })
                            drawTextLine(line, x, y, paint.apply {
                                mode = PaintMode.FILL
                                color = textColor
                            })
                            y += spacing + line.descent
                        }
                    }
                    INSIDE_BOTTOM -> toSurface().withCanvas {
                        var y = this@handle.height.toFloat() - spacing
                        drawImage(this@handle, 0F, 0F)
                        for (line in lines) {
                            val x = (this@handle.width - line.width) / 2
                            y -= line.descent
                            drawTextLine(line, x, y, paint.apply {
                                color = strokeColor
                                strokeWidth = stroke.toFloat()
                                isAntiAlias = true
                                mode = PaintMode.STROKE_AND_FILL
                            })
                            drawTextLine(line, x, y, paint.apply {
                                mode = PaintMode.FILL
                                color = textColor
                            })
                            y -= spacing - line.ascent
                        }
                    }
                    OUTSIDE_TOP -> Surface.makeRasterN32Premul(
                        this@handle.width,
                        this@handle.height + height
                    ).fill(bgColor).withCanvas {
                        var y = 0F
                        drawImage(this@handle, 0F, height.toFloat())
                        for (line in lines) {
                            val x = (this@handle.width - line.width) / 2
                            y -= line.ascent
                            drawTextLine(line, x, y, paint.apply {
                                color = strokeColor
                                strokeWidth = stroke.toFloat()
                                isAntiAlias = true
                                mode = PaintMode.STROKE_AND_FILL
                            })
                            drawTextLine(line, x, y, paint.apply {
                                mode = PaintMode.FILL
                                color = textColor
                            })
                            y += spacing + line.descent
                        }
                    }
                    OUTSIDE_BOTTOM -> Surface.makeRasterN32Premul(
                        this@handle.width,
                        this@handle.height + height
                    ).fill(bgColor).withCanvas {
                        var y = this@handle.height.toFloat()
                        drawImage(this@handle, 0F, 0F)
                        for (line in lines) {
                            val x = (this@handle.width - line.width) / 2
                            y -= line.ascent
                            drawTextLine(line, x, y, paint.apply {
                                color = strokeColor
                                strokeWidth = stroke.toFloat()
                                isAntiAlias = true
                                mode = PaintMode.STROKE
                            })
                            drawTextLine(line, x, y, paint.apply {
                                mode = PaintMode.FILL
                                color = textColor
                            })
                            y += spacing + line.descent
                        }
                    }
                }
            }
        }
    }

    private enum class Location {
        CENTER, // 中间
        INSIDE_TOP, // 内侧顶部
        INSIDE_BOTTOM, // 内侧底部
        OUTSIDE_TOP, // 外侧顶部
        OUTSIDE_BOTTOM // 外侧底部
    }
}