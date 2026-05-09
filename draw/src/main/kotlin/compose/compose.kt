package top.e404.skiko.draw.compose

import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface

@DslMarker
annotation class UiDsl

data class PreparedRenderTree(
    val root: UiElement,
    val width: Int,
    val height: Int
)

private fun prepareRenderTree(
    root: UiElement,
    measureContext: MeasureContext,
    content: Composable
): PreparedRenderTree {
    root.apply(content)
    root.measure(measureContext)
    val finalWidth = root.width.toInt()
    val finalHeight = root.height.toInt()
    if (finalWidth <= 0 || finalHeight <= 0) {
        error("计算尺寸无效 (width=$finalWidth, height=$finalHeight)，无法渲染图片")
    }
    root.layout(0f, 0f)
    normalizeLayoutBounds(root, finalWidth, finalHeight)
    return PreparedRenderTree(root, finalWidth, finalHeight)
}

private fun normalizeLayoutBounds(root: UiElement, finalWidth: Int, finalHeight: Int) {
    // 保持既有渲染行为：根节点的直接子元素会被裁剪到根节点边界内。
    for (children in root.children) {
        if (children.x + children.width > finalWidth) {
            children.width = finalWidth - children.x
        }
        if (children.y + children.height > finalHeight) {
            children.height = finalHeight - children.y
        }
    }
}

fun render(
    backgroundColor: Int = Color.TRANSPARENT,
    root: UiElement = Column(),
    measureContext: MeasureContext = MeasureContext(),
    content: Composable
): Image {
    val prepared = prepareRenderTree(root, measureContext, content)
    return Surface.makeRasterN32Premul(prepared.width, prepared.height).use { surface ->
        val drawContext = DrawContext(SkiaDrawCanvas(surface.canvas))
        drawContext.canvas.clear(backgroundColor)
        prepared.root.draw(drawContext)
        surface.makeImageSnapshot()
    }
}

fun render(backgroundColor: Int, root: UiElement, content: Composable): Image =
    render(backgroundColor, root, MeasureContext(), content)

fun renderCommands(
    backgroundColor: Int = Color.TRANSPARENT,
    root: UiElement = Column(),
    measureContext: MeasureContext = MeasureContext(),
    content: Composable
): List<DrawCommand> {
    val prepared = prepareRenderTree(root, measureContext, content)
    val recorder = RecordingDrawCanvas()
    val drawContext = DrawContext(recorder)
    drawContext.canvas.clear(backgroundColor)
    prepared.root.draw(drawContext)
    return recorder.commands
}

fun debugBaseElement(layer: Int, el: UiElement, sb: StringBuilder) {
    sb.append("  ".repeat(layer).let {
        if (it.isEmpty()) it
        else it.removeSuffix("  ") + "- "
    }).appendLine("type: ${el.javaClass.name}")
    sb.append("  ".repeat(layer)).appendLine("width: ${el.width}")
    sb.append("  ".repeat(layer)).appendLine("height: ${el.height}")
    sb.append("  ".repeat(layer)).appendLine("contentWidth: ${el.contentWidth}")
    sb.append("  ".repeat(layer)).appendLine("contentHeight: ${el.contentHeight}")
    sb.append("  ".repeat(layer)).appendLine("x: ${el.x}")
    sb.append("  ".repeat(layer)).appendLine("y: ${el.y}")
    val modifiers = el.modifier.toList()
    if (modifiers.isNotEmpty()) {
        sb.append("  ".repeat(layer)).appendLine("modifiers:")
        for (modifier in modifiers) {
            sb.append("  ".repeat(layer)).append("- ").appendLine("""'${modifier}'""")
        }
    }
    if (el.children.isNotEmpty()) {
        sb.append("  ".repeat(layer)).appendLine("children:")
        for (element in el.children) {
            debugBaseElement(layer + 1, element, sb)
        }
    }
}
