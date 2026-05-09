package top.e404.skiko.draw.compose

import org.jetbrains.skia.*
import kotlin.math.min

fun UiElement.icon(theme: IconTheme, svg: String) = add(
    CanvasElement(
        theme.size,
        theme.size
    ) { canvas ->
        val pathData = svg.substringAfter("<path d=\"").substringBefore("\"")
        val viewBox = svg.substringAfter("viewBox=\"")
            .substringBefore("\"")
            .split(" ")
            .map { it.toFloat() }
            .let {
                val (l, t, w, h) = it
                Rect.makeXYWH(l, t, w, h)
            }
        val skiaPath = Path.makeFromSVGString(pathData)
        drawIcon(canvas, skiaPath, viewBox, theme, this.parentX, this.parentY)
    }
)

/**
 * Theme 数据类，用于定义图标的绘制细节
 * @param size 图标的尺寸（宽度和高度相等）
 * @param color 图标的主要颜色
 * @param paintMode 绘制模式，默认为填充模式
 * @param strokeColor 描边的颜色
 * @param strokeWidth 描边的宽度，默认为 2f
 */
data class IconTheme(
    val size: Float,
    val scale: Float = .95f,
    val color: Int = Color.WHITE,
    val paintMode: PaintMode = PaintMode.FILL,
    val strokeColor: Int = Color.BLACK,
    val strokeWidth: Float = 2f,
)

/**
 * 通用的SVG路径绘制方法
 *
 * @param canvas Canvas对象
 * @param path Skia的Path对象
 * @param viewBox SVG的视图盒子(Rect)
 * @param theme 图标主题
 * @param parentX 父元素的X坐标偏移
 * @param parentY 父元素的Y坐标偏移
 */
fun drawIcon(
    canvas: DrawCanvas,
    path: Path,
    viewBox: Rect,
    theme: IconTheme,
    parentX: Float,
    parentY: Float
) {
    // 创建画笔并根据参数进行配置
    val paint = Paint().apply {
        this.color = theme.color
        this.mode = theme.paintMode
        if (theme.paintMode == PaintMode.STROKE) {
            this.strokeWidth = strokeWidth
        }
        isAntiAlias = true // 开启抗锯齿使图像更平滑
    }

    // 计算缩放比例，以确保图标能完整地放入目标尺寸内
    val scaleX = theme.size / viewBox.width
    val scaleY = theme.size / viewBox.height
    val scale = min(scaleX, scaleY) * theme.scale

    // 计算位移，使图标在画布中居中
    val scaledWidth = viewBox.width * scale
    val scaledHeight = viewBox.height * scale
    val translateX = (theme.size - scaledWidth) / 2f + parentX
    val translateY = (theme.size - scaledHeight) / 2f + parentY

    // 保存当前画布状态
    canvas.save()

    // 应用变换：先平移到目标位置，再进行缩放
    canvas.translate(translateX, translateY)
    canvas.scale(scale, scale)
    // 将路径从其原始坐标系的原点(0, -960)移动到画布的(0,0)点
    canvas.translate(-viewBox.left, -viewBox.top)

    // 使用配置好的画笔绘制路径
    canvas.drawPath(path, paint)

    // 恢复画布状态
    canvas.restore()
}
