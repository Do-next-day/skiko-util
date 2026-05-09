package top.e404.skiko.handler.face

import org.jetbrains.skia.*
import top.e404.skiko.util.Colors
import top.e404.skiko.FontType
import top.e404.skiko.annotation.ImageHandler
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
    private const val MIN_SIZE = 20
    private const val MAX_SIZE = 1000
    private const val UNIT = 10
    private val tf = FontType.MI.typeface

    override val name = "drawstring"
    override val regex = Regex("(?i)写|(d(raw)?s(tring)?|write)")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val text = args["text"]!!
        var padding = args["padding"]?.intOrPercentage(-100)
        var stroke = args["stroke"]?.toIntOrNull()
        val bgColor = args["bgColor"]?.asColor() ?: Colors.WHITE.argb
        val textColor = args["textColor"]?.asColor() ?: Colors.WHITE.argb
        val strokeColor = args["strokeColor"]?.asColor() ?: Colors.BLACK.argb
        val location = args["location"]?.let { Location.valueOf(it.uppercase()) } ?: OUTSIDE_BOTTOM
        val texts = text.split("\n")
        val w = frames[0].image.width

        padding = padding?.let {
            if (it > 0) it else w * -it / 100
        } ?: min(w / 20, 100)
        val maxWidth = w - padding * 2
        val size = texts.minOf {
            autoSize(tf, it, MIN_SIZE, MAX_SIZE, maxWidth, UNIT)
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
                    CENTER -> it.newSurface().withCanvas {
                        var y = (it.height - height) / 2F
                        drawImage(it, 0F, 0F)
                        for (line in lines) {
                            val x = (it.width - line.width) / 2
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

                    INSIDE_TOP -> it.newSurface().withCanvas {
                        var y = 0F
                        drawImage(it, 0F, 0F)
                        for (line in lines) {
                            val x = (it.width - line.width) / 2
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

                    INSIDE_BOTTOM -> it.newSurface().withCanvas {
                        var y = it.height.toFloat() - spacing
                        drawImage(it, 0F, 0F)
                        for (line in lines) {
                            val x = (it.width - line.width) / 2
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
                        it.width,
                        it.height + height
                    ).fill(bgColor).withCanvas {
                        var y = 0F
                        drawImage(it, 0F, height.toFloat())
                        for (line in lines) {
                            val x = (it.width - line.width) / 2
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
                        it.width,
                        it.height + height
                    ).fill(bgColor).withCanvas {
                        var y = it.height.toFloat()
                        drawImage(it, 0F, 0F)
                        for (line in lines) {
                            val x = (it.width - line.width) / 2
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
