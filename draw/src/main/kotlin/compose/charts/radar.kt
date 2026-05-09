package top.e404.skiko.draw.compose.charts

import org.jetbrains.skia.*
import top.e404.skiko.draw.compose.*
import top.e404.skiko.util.Colors
import kotlin.math.*

/**
 * 在 UiElement 上添加雷达图
 *
 * @param theme 雷达图主题
 * @param data 数据列表，包含标签和对应的数值, second是在整个轴上的占比
 */
fun UiElement.radar(theme: RadarTheme, data: List<Pair<String, Float>>) = add(
    CanvasElement(
        theme.width,
        theme.height
    ) { canvas ->
        drawRadarChart(canvas, parentX, parentY, data, theme)
    }
)

/**
 * Theme 数据类，用于定义图表外观
 */
data class RadarTheme(
    /** 雷达图的宽度 */
    val width: Float,
    /** 雷达图高度 */
    val height: Float,
    /** 雷达图半径 即使设置了gridStart尺寸依然不变 */
    val radius: Float = 200f,

    /** 数据填充轮廓颜色 */
    val fillOutlineColor: Int = Colors.LIGHT_BLUE.argb,
    val fillOutlinePaint: Paint = Paint().apply {
        color = fillOutlineColor
        strokeWidth = 2f
        mode = PaintMode.STROKE
        isAntiAlias = true
    },

    /** 数据填充颜色 */
    val fillColor: Int = fillOutlineColor and 0xFFFFFF or (0x66 shl 24), // 半透明
    val fillPaint: Paint = Paint().apply {
        color = fillColor
        isAntiAlias = true
    },

    /** 背景颜色 填充网格间的空白部分 */
    val bgColor: Int = Color.TRANSPARENT,
    val bgPaint: Paint = Paint().apply {
        color = bgColor
        isAntiAlias = true
    },

    /** 网格数量 */
    val gridCount: Int = 5,
    /** 网格线颜色 */
    val gridLineColor: Int = 0xFFCCCCCC.toInt(),
    val gridLinePaint: Paint = Paint().apply {
        color = gridLineColor
        strokeWidth = 1f
        isAntiAlias = true
        mode = PaintMode.STROKE
        pathEffect = PathEffect.makeDash(floatArrayOf(10f, 10f), 0f)
    },
    /** 网格坐标生成逻辑 */
    val gridFontProvider: (Int) -> String? = { it.toString() },
    /** 网格坐标字体 */
    val gridFont: Font = Font(ComposeFontManager.resolve(), 12f),
    /** 网格坐标字体颜色 */
    val gridFontColor: Int = Color.WHITE,
    val gridFontPaint: Paint = Paint().apply {
        color = gridFontColor
        isAntiAlias = true
    },

    val labelOuterLength: Float = 10F,
    /** 标签位置修正 处理标签覆盖坐标的问题 */
    val labelFixPolicy: RadarFixPolicy = RadarFixPolicy.RATED_FIX,
    /** 标签字体 */
    val labelFont: Font = Font(ComposeFontManager.resolve(), 25f),
    /** 标签字体颜色 */
    val labelFontColor: Int = Color.WHITE,
    val labelFontPaint: Paint = Paint().apply {
        color = labelFontColor
        isAntiAlias = true
    }
) {
    /** 每个网格的单位距离 */
    val gridUnit = radius / gridCount
}

@Suppress("UNUSED")
enum class RadarFixPolicy(val fix: (
    angle: Double,
    a: Double,
    line: TextLine,
    centerX: Float,
    centerY: Float,
    theme: RadarTheme
) -> Pair<Float, Float>) {
    /** 不进行修正，标签起点始终在图表外侧固定位置 */
    NONE({ angle, _, line, centerX, centerY, theme ->
        val x = centerX + (theme.radius + theme.labelOuterLength) * cos(angle).toFloat()
        val y = centerY + (theme.radius + theme.labelOuterLength) * sin(angle).toFloat()
        x - line.width / 2 to y
    }),
    /** 对于两边的标签向外移动 */
    MOVE_OUTSIDE({ angle, a, line, centerX, centerY, theme ->
        val r = theme.radius + theme.labelOuterLength

        val x = centerX + r * cos(angle).toFloat()
        val y = centerY + r * sin(angle).toFloat()

        val location = when {
            abs(a - 1.5) < 1e-6 -> Location.TOP
            abs(a - 0.5) < 1e-6 -> Location.BOTTOM
            a in 0.5..1.5 -> Location.LEFT
            else -> Location.RIGHT
        }

        val px = when (location) {
            Location.LEFT -> x - line.width
            Location.RIGHT -> x
            else -> x - line.width / 2
        }
        px to y
    }),
    /** 按比例修正 标签依然除了顶部和底部都向外偏移 */
    RATED_FIX({ angle, a, line, centerX, centerY, theme ->
        // 距离上下顶点越近, 修正越大
        val v = abs((a % 1) - 0.5)
        val fix = (1 - v).toFloat() * theme.labelOuterLength * 1.5f
        // 修正起点, 保持标签和图表的距离
        val r = theme.radius + theme.labelOuterLength + fix

        val x = centerX + r * cos(angle).toFloat()
        val y = centerY + r * sin(angle).toFloat()


        val location = when {
            abs(a - 1.5) < 1e-6 -> Location.TOP
            abs(a - 0.5) < 1e-6 -> Location.BOTTOM
            a in 0.5..1.5 -> Location.LEFT
            else -> Location.RIGHT
        }

        val px = when (location) {
            Location.LEFT -> x - line.width
            Location.RIGHT -> x
            else -> x - line.width / 2
        }
        px to y
    }),
    /** 倾斜 */
    TILT({ angle, _, line, centerX, centerY, theme ->
        /**
         * 计算从文本中心 (tx,ty) 指向雷达中心 (cx,cy) 的线段
         * 在矩形 (left, top, right, bottom) 内的长度（从中心到盒子边界的距离）。
         */
        fun computeTextCenterToRadarInsideFontBoxLength(
            tx: Float, ty: Float,
            cx: Float, cy: Float,
            left: Float, top: Float, right: Float, bottom: Float
        ): Float {
            val dx = cx - tx
            val dy = cy - ty
            val eps = 1e-6f

            // 如果方向向量几乎为零，返回 0
            if (abs(dx) < eps && abs(dy) < eps) return 0f

            val candidates = mutableListOf<Float>()

            // 与竖直边相交 (x = left 或 x = right)
            if (abs(dx) > eps) {
                val tLeft = (left - tx) / dx
                if (tLeft > 0f) {
                    val yAtT = ty + tLeft * dy
                    if (yAtT >= top - eps && yAtT <= bottom + eps) candidates.add(tLeft)
                }
                val tRight = (right - tx) / dx
                if (tRight > 0f) {
                    val yAtT = ty + tRight * dy
                    if (yAtT >= top - eps && yAtT <= bottom + eps) candidates.add(tRight)
                }
            }

            // 与水平边相交 (y = top 或 y = bottom)
            if (abs(dy) > eps) {
                val tTop = (top - ty) / dy
                if (tTop > 0f) {
                    val xAtT = tx + tTop * dx
                    if (xAtT >= left - eps && xAtT <= right + eps) candidates.add(tTop)
                }
                val tBottom = (bottom - ty) / dy
                if (tBottom > 0f) {
                    val xAtT = tx + tBottom * dx
                    if (xAtT >= left - eps && xAtT <= right + eps) candidates.add(tBottom)
                }
            }

            if (candidates.isEmpty()) return 0f

            // 选择最近的正交交点 (最小正 t)
            val tMin = candidates.minOrNull() ?: return 0f
            val dist = sqrt(dx * dx + dy * dy) * tMin
            return dist
        }

        val x = centerX + (theme.radius + theme.labelOuterLength) * cos(angle).toFloat()
        val y = centerY + (theme.radius + theme.labelOuterLength) * sin(angle).toFloat()

        val boxLeft = line.width / 2
        val boxTop = line.ascent
        val boxRight = line.width / 2
        val boxBottom = line.descent
        // 计算文本中心点连接雷达图中心点的线在字体盒中的长度
        val distance = computeTextCenterToRadarInsideFontBoxLength(
            x, y,
            centerX, centerY,
            boxLeft, boxTop,
            boxRight, boxBottom
        )

        val r = theme.radius + theme.labelOuterLength - distance + line.run { sqrt(width * width + height * height) } / 2

        val dx = centerX + r * cos(angle).toFloat()
        val dy = centerY + r * sin(angle).toFloat()

        val dpx = dx - line.width / 2
        val dpy = dy - line.descent / 2
        dpx to dpy
    });
}

enum class Location { LEFT, TOP, RIGHT, BOTTOM }

fun drawRadarChart(canvas: DrawCanvas, parentX: Float, parentY: Float, data: List<Pair<String, Float>>, theme: RadarTheme) {
    val centerX = parentX + theme.width / 2f
    val centerY = parentY + theme.height / 2f
    val n = data.size
    val angleStep = 2 * Math.PI / n

    // 绘制雷达图背景
    val path = Path().run {
        for (i in 0 until n) {
            val angle = i * angleStep - Math.PI / 2
            val x = centerX + theme.radius * cos(angle).toFloat()
            val y = centerY + theme.radius * sin(angle).toFloat()
            if (i == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        closePath()
    }
    canvas.drawPath(path, theme.bgPaint)

    // 绘制网格线 和 网格坐标
    for (ringIndex in 1..theme.gridCount) {
        val r = theme.radius * ringIndex / theme.gridCount
        val gridPath = Path()
        for (j in 0 until n) {
            val angle = j * angleStep - Math.PI / 2
            val x = centerX + r * cos(angle).toFloat()
            val y = centerY + r * sin(angle).toFloat()
            if (j == 0) {
                gridPath.moveTo(x, y)
            } else {
                gridPath.lineTo(x, y)
            }
        }
        gridPath.closePath()
        canvas.drawPath(gridPath, theme.gridLinePaint)
        theme.gridFontProvider.invoke(ringIndex - 1)?.let { gridText ->
            canvas.drawTextLine(
                TextLine.make(gridText, theme.gridFont),
                centerX + 3f,
                centerY - r - 3f,
                theme.gridFontPaint
            )
        }
    }

    // 绘制放射线
    for (i in 0 until n) {
        val angle = i * angleStep - Math.PI / 2
        val tx = centerX + theme.radius * cos(angle).toFloat()
        val ty = centerY + theme.radius * sin(angle).toFloat()
        val fx = centerX + theme.gridUnit * cos(angle).toFloat()
        val fy = centerY + theme.gridUnit * sin(angle).toFloat()
        canvas.drawLine(fx, fy, tx, ty, theme.gridLinePaint)
    }

    // 绘制数据层
    val dataPath = Path().run {
        data.forEachIndexed { i, (_, rate) ->
            val r = (theme.radius - theme.gridUnit) * rate + theme.gridUnit
            val angle = i * angleStep - Math.PI / 2
            val x = centerX + r * cos(angle).toFloat()
            val y = centerY + r * sin(angle).toFloat()
            if (i == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        closePath()
    }

    canvas.drawPath(dataPath, theme.fillPaint)
    canvas.drawPath(dataPath, theme.fillOutlinePaint)

    // 绘制标签
    data.forEachIndexed { i, (label) ->
        val angle = (i * angleStep + Math.PI / 2 * 3) % (2 * Math.PI)
        val line = TextLine.make(label, theme.labelFont)
        val a = angle / Math.PI

        theme.labelFixPolicy.fix(angle, a, line, centerX, centerY, theme).let { (px, py) ->
            canvas.drawTextLine(line, px, py, theme.labelFontPaint)
        }
    }
}
