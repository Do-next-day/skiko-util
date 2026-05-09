package top.e404.skiko.draw.compose

import org.jetbrains.skia.*

/**
 * UI 元素的基础接口，定义了所有 UI 组件共有的属性和行为。
 */
interface UiElement {
    var width: Float
    var height: Float
    var contentWidth: Float
    var contentHeight: Float
    var x: Float
    var y: Float
    var modifier: Modifier
    val children: MutableList<UiElement>

    /**
     * 第一阶段：测量尺寸。
     */
    fun measure(context: MeasureContext)

    /**
     * 第二阶段：布局位置。
     */
    fun layout(parentX: Float, parentY: Float)

    /**
     * 第三阶段：绘制。
     */
    fun draw(context: DrawContext)
    fun add(element: UiElement) {
        children.add(element)
    }
}

/**
 * Element 接口的基础实现类，提供了通用属性的默认实现。
 */
abstract class BaseElement : UiElement {
    override var width: Float = 0f
    override var height: Float = 0f
    override var contentWidth: Float = 0f
    override var contentHeight: Float = 0f
    override var x: Float = 0f
    override var y: Float = 0f
    override var modifier: Modifier = Modifier
    override val children: MutableList<UiElement> = mutableListOf()

    protected data class ModifierInsets(
        val top: Float = 0f,
        val right: Float = 0f,
        val bottom: Float = 0f,
        val left: Float = 0f
    ) {
        val horizontal: Float get() = left + right
        val vertical: Float get() = top + bottom
    }

    protected data class Bounds(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float
    ) {
        fun inset(insets: ModifierInsets): Bounds = Bounds(
            x = x + insets.left,
            y = y + insets.top,
            width = (width - insets.horizontal).coerceAtLeast(0f),
            height = (height - insets.vertical).coerceAtLeast(0f)
        )
    }

    private fun Padding.asInsets() = ModifierInsets(top, right, bottom, left)

    private fun Border.asInsets() = ModifierInsets(top, right, bottom, left)

    protected fun sizeIn(): SizeIn = modifier.fold(SizeIn()) { acc, m -> m as? SizeIn ?: acc }

    private fun Float.coerceInConstraint(min: Float, max: Float): Float {
        val constrainedMin = min.coerceAtLeast(0f)
        val constrainedMax = if (max.isFinite()) max.coerceAtLeast(constrainedMin) else Float.POSITIVE_INFINITY
        return this.coerceAtLeast(constrainedMin).let {
            if (constrainedMax.isFinite()) it.coerceAtMost(constrainedMax) else it
        }
    }

    protected fun contentBounds(): Bounds {
        var bounds = Bounds(x, y, width, height)
        for (mod in modifier.toList()) {
            bounds = when (mod) {
                is Padding -> bounds.inset(mod.asInsets())
                is Border -> bounds.inset(mod.asInsets())
                else -> bounds
            }
        }
        return bounds
    }

    final override fun measure(context: MeasureContext) {
        // 步骤 1: 先调用 measureContent，让元素（如 Text, Image）计算出其内容的自然尺寸。
        measureContent(context)

        // 步骤 2: 从内到外应用会影响布局尺寸的 Modifier，让 size/padding/border 的顺序和链式嵌套一致。
        var finalWidth = this.contentWidth
        var finalHeight = this.contentHeight

        for (mod in modifier.toList().asReversed()) {
            when (mod) {
                is Size -> {
                    if (!mod.width.isNaN()) finalWidth = mod.width
                    if (!mod.height.isNaN()) finalHeight = mod.height
                }
                is SizeIn -> {
                    finalWidth = finalWidth.coerceInConstraint(mod.minWidth, mod.maxWidth)
                    finalHeight = finalHeight.coerceInConstraint(mod.minHeight, mod.maxHeight)
                }
                is Padding -> {
                    val insets = mod.asInsets()
                    finalWidth += insets.horizontal
                    finalHeight += insets.vertical
                }
                is Border -> {
                    val insets = mod.asInsets()
                    finalWidth += insets.horizontal
                    finalHeight += insets.vertical
                }
            }
        }

        this.width = finalWidth
        this.height = finalHeight
    }

    /**
     * 第一阶段：测量内容尺寸，由具体元素实现。
     */
    abstract fun measureContent(context: MeasureContext)

    override fun layout(parentX: Float, parentY: Float) {
        this.x = parentX
        this.y = parentY
        val content = contentBounds()
        layoutChildren(content)
    }

    /**
     * 第二阶段：布局子元素，由具体容器元素实现。
     */
    protected abstract fun layoutChildren(content: Bounds)

    final override fun draw(context: DrawContext) {
        val antiAlias = modifier.fold(AntiAlias()) { acc, m -> m as? AntiAlias ?: acc }
        var bounds = Bounds(x, y, width, height)
        var clipCount = 0
        for (mod in modifier.toList()) {
            when (mod) {
                is Padding -> bounds = bounds.inset(mod.asInsets())
                is Background -> {
                    val paint = Paint().apply { color = mod.color; isAntiAlias = antiAlias.enabled }
                    context.canvas.drawRect(Rect.makeXYWH(bounds.x, bounds.y, bounds.width, bounds.height), paint)
                }
                is Border -> {
                    drawBorder(context, bounds, mod, antiAlias)
                    bounds = bounds.inset(mod.asInsets())
                }
                is Clip -> {
                    val clipPath = mod.shape.createPath(bounds.width, bounds.height)
                        .apply { transform(Matrix33.makeTranslate(bounds.x, bounds.y)) }
                    context.canvas.save()
                    context.canvas.clipPath(clipPath, true)
                    clipCount += 1
                }
            }
        }

        try {
            drawContent(context)
            children.forEach { it.draw(context) }
        } finally {
            repeat(clipCount) {
                context.canvas.restore()
            }
        }
    }

    private fun drawBorder(context: DrawContext, bounds: Bounds, border: Border, antiAlias: AntiAlias) {
        if (border.color == Color.TRANSPARENT) return
        val paint = Paint().apply {
            color = border.color
            mode = PaintMode.FILL
            isAntiAlias = antiAlias.enabled
        }
        if (border.top > 0) context.canvas.drawRect(Rect.makeXYWH(bounds.x, bounds.y, bounds.width, border.top), paint)
        if (border.bottom > 0) context.canvas.drawRect(Rect.makeXYWH(bounds.x, bounds.y + bounds.height - border.bottom, bounds.width, border.bottom), paint)
        if (border.left > 0) context.canvas.drawRect(Rect.makeXYWH(bounds.x, bounds.y, border.left, bounds.height), paint)
        if (border.right > 0) context.canvas.drawRect(Rect.makeXYWH(bounds.x + bounds.width - border.right, bounds.y, border.right, bounds.height), paint)
    }

    /**
     * 第三阶段：绘制内容，由具体元素实现。
     */
    abstract fun drawContent(context: DrawContext)
}

class Column(private val horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Left) : BaseElement() {
    override fun measureContent(context: MeasureContext) {
        children.forEach { it.measure(context) }
        contentWidth = children.maxOfOrNull { it.width } ?: 0f
        contentHeight = children.sumOf { it.height.toDouble() }.toFloat()
    }

    override fun layoutChildren(content: Bounds) {
        val parentX = content.x
        val parentY = content.y
        val availableWidth = content.width

        var currentY = parentY
        children.forEach { child ->
            val childStartX = when (horizontalAlignment) {
                HorizontalAlignment.Left -> parentX
                HorizontalAlignment.Center -> parentX + (availableWidth - child.width) / 2
                HorizontalAlignment.Right -> parentX + (availableWidth - child.width)
            }

            child.layout(childStartX, currentY)

            currentY += child.height
        }
    }

    override fun drawContent(context: DrawContext) { /* Column 本身无内容 */ }
}

class Row(private val verticalAlignment: VerticalAlignment = VerticalAlignment.Top) : BaseElement() {
    override fun measureContent(context: MeasureContext) {
        children.forEach { it.measure(context) }
        contentWidth = children.sumOf { it.width.toDouble() }.toFloat()
        contentHeight = children.maxOfOrNull { it.height } ?: 0f
    }

    override fun layoutChildren(content: Bounds) {
        val parentX = content.x
        val parentY = content.y
        val availableHeight = content.height

        var currentX = parentX
        children.forEach { child ->
            val childStartY = when (verticalAlignment) {
                VerticalAlignment.Top -> parentY
                VerticalAlignment.Center -> parentY + (availableHeight - child.height) / 2
                VerticalAlignment.Bottom -> parentY + (availableHeight - child.height)
            }

            child.layout(currentX, childStartY)

            currentX += child.width
        }
    }

    override fun drawContent(context: DrawContext) { /* Row 本身无内容 */ }
}

class Box(
    private val horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Left,
    private val verticalAlignment: VerticalAlignment = VerticalAlignment.Top
) : BaseElement() {
    override fun measureContent(context: MeasureContext) {
        children.forEach { it.measure(context) }
        contentWidth = children.maxOfOrNull { it.width } ?: 0f
        contentHeight = children.maxOfOrNull { it.height } ?: 0f
    }

    override fun layoutChildren(content: Bounds) {
        val parentX = content.x
        val parentY = content.y
        val availableWidth = content.width
        val availableHeight = content.height

        children.forEach { child ->
            val childStartX = when (horizontalAlignment) {
                HorizontalAlignment.Left -> parentX
                HorizontalAlignment.Center -> parentX + (availableWidth - child.width) / 2
                HorizontalAlignment.Right -> parentX + (availableWidth - child.width)
            }
            val childStartY = when (verticalAlignment) {
                VerticalAlignment.Top -> parentY
                VerticalAlignment.Center -> parentY + (availableHeight - child.height) / 2
                VerticalAlignment.Bottom -> parentY + (availableHeight - child.height)
            }
            child.layout(childStartX, childStartY)
        }
    }

    override fun drawContent(context: DrawContext) { /* Box 本身无内容 */ }
}

/**
 * 画布元素，类似于 Box，用于更自由的布局场景，固定尺寸
 */
class CanvasElement(
    override var width: Float,
    override var height: Float,
    val draw: CanvasElement.(DrawCanvas) -> Unit
) : BaseElement() {
    internal var parentX: Float = 0f
    internal var parentY: Float = 0f
    override var contentWidth = width
    override var contentHeight = height

    override fun measureContent(context: MeasureContext) {}
    override fun layoutChildren(content: Bounds) {
        this.parentX = content.x
        this.parentY = content.y
    }
    override fun drawContent(context: DrawContext) {
        draw.invoke(this, context.canvas)
    }
}

/**
 * 单元格元素，用于包裹表格中的具体内容。
 */
class TableCell(
    private val horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Left,
    private val verticalAlignment: VerticalAlignment = VerticalAlignment.Top
) : BaseElement() {
    val content: UiElement?
        get() = children.firstOrNull()

    override fun measureContent(context: MeasureContext) {
        content?.measure(context)
        contentWidth = content?.width ?: 0f
        contentHeight = content?.height ?: 0f
    }

    // 关键修改：像 Box 一样，在自己的可用空间内对齐子元素
    override fun layoutChildren(content: Bounds) {
        val parentX = content.x
        val parentY = content.y
        val availableWidth = content.width
        val availableHeight = content.height

        this.content?.let { child ->
            val childStartX = when (horizontalAlignment) {
                HorizontalAlignment.Left -> parentX
                HorizontalAlignment.Center -> parentX + (availableWidth - child.width) / 2
                HorizontalAlignment.Right -> parentX + (availableWidth - child.width)
            }
            val childStartY = when (verticalAlignment) {
                VerticalAlignment.Top -> parentY
                VerticalAlignment.Center -> parentY + (availableHeight - child.height) / 2
                VerticalAlignment.Bottom -> parentY + (availableHeight - child.height)
            }
            child.layout(childStartX, childStartY)
        }
    }

    override fun drawContent(context: DrawContext) {
        // TableCell 本身不绘制，只作为容器
    }
}

/**
 * 表格行元素，管理一行内的单元格。
 */
class TableRow : BaseElement() {
    val cells: List<TableCell>
        get() = children.filterIsInstance<TableCell>()

    override fun measureContent(context: MeasureContext) {
        // 在 Table 的协调下进行测量，这里只是一个备用实现
        children.forEach { it.measure(context) }
        contentWidth = children.sumOf { it.width.toDouble() }.toFloat()
        contentHeight = children.maxOfOrNull { it.height } ?: 0f
    }

    override fun layoutChildren(content: Bounds) {
        // 布局也由 Table 统一处理
    }

    override fun drawContent(context: DrawContext) {
        // TableRow 本身不绘制
    }
}

/**
 * 表格主元素，负责协调列宽计算和整体布局。
 */
class Table(
    private val columnSpacing: Float = 0f,
    private val rowSpacing: Float = 0f
) : BaseElement() {
    private lateinit var columnWidths: List<Float>

    val rows: List<TableRow>
        get() = children.filterIsInstance<TableRow>()

    override fun measureContent(context: MeasureContext) {
        // --- 阶段一：发现列宽 ---
        val naturalColumnWidths = mutableMapOf<Int, Float>()
        rows.forEach { row ->
            row.cells.forEachIndexed { index, cell ->
                cell.measure(context)
                val currentMaxWidth = naturalColumnWidths.getOrDefault(index, 0f)
                naturalColumnWidths[index] = maxOf(currentMaxWidth, cell.width)
            }
        }
        val maxColumns = naturalColumnWidths.keys.maxOrNull()?.let { it + 1 } ?: 0
        this.columnWidths = (0 until maxColumns).map { naturalColumnWidths.getOrDefault(it, 0f) }

        // --- 阶段二：重新测量并设置最终尺寸 ---
        var totalHeight = 0f
        rows.forEach { row ->
            var maxRowHeight = 0f
            row.cells.forEachIndexed { cellIndex, cell ->
                val constrainedWidth = columnWidths[cellIndex]
                val content = cell.content
                if (content != null) {
                    val originalModifier = content.modifier
                    content.modifier = content.modifier.sizeIn(maxWidth = constrainedWidth)
                    content.measure(context)
                    content.modifier = originalModifier
                }

                cell.measure(context)
                maxRowHeight = maxOf(maxRowHeight, cell.height)
            }

            row.height = maxRowHeight
            row.contentHeight = maxRowHeight
            val rowWidth = (0 until row.cells.size).sumOf { columnWidths[it].toDouble() }.toFloat() + maxOf(0, row.cells.size - 1) * columnSpacing
            row.width = rowWidth
            row.contentWidth = rowWidth

            // =================================================================================
            // 关键新增：强制为每个 Cell 设置由 Table 计算出的最终尺寸
            // =================================================================================
            row.cells.forEachIndexed { cellIndex, cell ->
                cell.width = columnWidths[cellIndex]
                cell.height = row.height
            }
            // =================================================================================

            totalHeight += row.height
        }

        contentWidth = columnWidths.sum() + (maxOf(0, columnWidths.size - 1) * columnSpacing)
        contentHeight = totalHeight + (maxOf(0, rows.size - 1) * rowSpacing)
    }

    override fun layoutChildren(content: Bounds) {
        var currentY = content.y
        rows.forEach { row ->
            var currentX = content.x
            row.cells.forEachIndexed { index, cell ->
                // 使用最终的列宽来布局单元格
                cell.layout(currentX, currentY)
                currentX += columnWidths[index] + columnSpacing
            }
            currentY += row.height + rowSpacing
        }
    }

    override fun drawContent(context: DrawContext) {
        // Table 本身不绘制
    }
}

class Text(
    private val text: String,
    private val fontSize: Float? = null,
    private val textColor: Int? = null,
    private val fontFamily: String? = null,
    private val textOverflow: TextOverflow? = null,
    private val textOverflowPlaceholder: String? = null
) : BaseElement() {
    private lateinit var font: Font
    private lateinit var paint: Paint
    private var textMetrics: TextMetrics = TextMetrics(0f, 0f)
    private var lines: List<String> = listOf()
    private var overflow = TextOverflow.Wrap
    private var overflowPlaceholder = TextDefaults.OVERFLOW_PLACEHOLDER

    private fun applyModifiers() {
        val finalColor = textColor ?: Color.WHITE
        val finalSize = fontSize ?: 24f
        val finalFamily = fontFamily ?: ComposeFontManager.defaultFamily
        overflow = textOverflow ?: TextOverflow.Wrap
        overflowPlaceholder = textOverflowPlaceholder ?: TextDefaults.OVERFLOW_PLACEHOLDER
        val antiAlias = modifier.fold(AntiAlias()) { acc, m -> m as? AntiAlias ?: acc }
        font = Font(ComposeFontManager.resolve(finalFamily), finalSize)
        paint = Paint().apply { color = finalColor; isAntiAlias = antiAlias.enabled }
    }

    override fun measureContent(context: MeasureContext) {
        applyModifiers()

        val sizeIn = sizeIn()
        val measurer = context.textMeasurer
        val metrics = measurer.metrics(font)
        textMetrics = metrics
        val lineHeight = metrics.lineHeight

        if (overflow == TextOverflow.Wrap && sizeIn.maxWidth.isFinite()) {
            val words = text.split(Regex("\\s+"))
            val builtLines = mutableListOf<String>()
            var current = StringBuilder()

            fun commitLine() {
                if (current.isNotEmpty()) {
                    builtLines.add(current.toString())
                    current = StringBuilder()
                }
            }

            for (w in words) {
                if (w.isEmpty()) continue
                val attempt = if (current.isEmpty()) w else "$current $w"
                val widthAttempt = measurer.measureTextWidth(attempt, font, paint)
                if (widthAttempt <= sizeIn.maxWidth) {
                    if (current.isEmpty()) current.append(w) else current.append(" ").append(w)
                } else {
                    if (current.isEmpty()) {
                        var acc = ""
                        for (ch in w) {
                            val tryAcc = acc + ch
                            if (measurer.measureTextWidth(tryAcc, font, paint) <= sizeIn.maxWidth) {
                                acc = tryAcc
                            } else {
                                if (acc.isNotEmpty()) builtLines.add(acc)
                                acc = ch.toString()
                            }
                        }
                        if (acc.isNotEmpty()) current.append(acc)
                    } else {
                        commitLine()
                        current.append(w)
                    }
                }
            }
            commitLine()

            // 受 maxHeight 限制：计算可显示最大行数
            if (sizeIn.maxHeight.isFinite()) {
                val maxLines = (sizeIn.maxHeight / lineHeight).toInt().coerceAtLeast(1)
                if (builtLines.size > maxLines) {
                    // 截断尾行并添加省略号
                    val visible = builtLines.subList(0, maxLines).toMutableList()
                    // 对最后一行做 Ellipsis 处理
                    val lastIndex = visible.lastIndex
                    val last = visible[lastIndex]
                    var truncated = last
                    if (measurer.measureTextWidth("$truncated$overflowPlaceholder", font, paint) <= sizeIn.maxWidth) {
                        truncated += overflowPlaceholder
                    } else {
                        // 逐字符去掉直到放得下
                        var t = truncated
                        while (t.isNotEmpty() && measurer.measureTextWidth("$t$overflowPlaceholder", font, paint) > sizeIn.maxWidth) {
                            t = t.dropLast(1)
                        }
                        truncated = if (t.isEmpty()) overflowPlaceholder else t + overflowPlaceholder
                    }
                    visible[lastIndex] = truncated
                    lines = visible
                } else {
                    lines = builtLines
                }
            } else {
                lines = builtLines
            }

            // 计算内容宽高
            contentWidth = if (lines.isEmpty()) 0f else lines.maxOf { measurer.measureTextWidth(it, font, paint) }
            contentHeight = (lines.size * lineHeight)
        } else {
            // Ellipsis 或者无 maxWidth 的情况：默认单行测量，必要时截断
            val measuredWidth = measurer.measureTextWidth(text, font, paint)
            if (overflow == TextOverflow.Ellipsis && sizeIn.maxWidth.isFinite() && measuredWidth > sizeIn.maxWidth) {
                // 需要截断为能放下省略号的最长子串
                var lo = 0
                var hi = text.length
                var best = ""
                while (lo <= hi) {
                    val mid = (lo + hi) / 2
                    val candidate = text.substring(0, mid)
                    if (measurer.measureTextWidth("$candidate$overflowPlaceholder", font, paint) <= sizeIn.maxWidth) {
                        best = candidate; lo = mid + 1
                    } else {
                        hi = mid - 1
                    }
                }
                val finalLine = if (best.isEmpty()) overflowPlaceholder else best + overflowPlaceholder
                lines = listOf(finalLine)
                contentWidth = measurer.measureTextWidth(finalLine, font, paint)
            } else {
                lines = listOf(text)
                contentWidth = measuredWidth
            }

            // maxHeight 限制：如果有限且比单行高小则裁剪高度为 maxHeight（外部布局会看到该高度）
            val lineHeightSingle = metrics.lineHeight
            contentHeight = lineHeightSingle
            if (sizeIn.maxHeight.isFinite()) {
                contentHeight = contentHeight.coerceAtMost(sizeIn.maxHeight)
                // 如果 maxHeight 小于单行高度可以强制置为 maxHeight ，显示可能被裁剪
            }
        }
    }

    override fun layoutChildren(content: Bounds) {}

    override fun drawContent(context: DrawContext) {
        val content = contentBounds()
        val drawX = content.x
        val drawY = content.y

        val lineHeight = textMetrics.lineHeight
        var yCursor = drawY - textMetrics.ascent // 第一行基线
        // 绘制每一行（可能已经包括了省略号）
        for (line in lines) {
            context.canvas.drawString(line, drawX, yCursor, font, paint)
            yCursor += lineHeight
        }
    }
}

class ImageElement(
    private val image: Image,
    private val overflow: ImageOverflow = ImageOverflow.Scale
) : BaseElement() {
    // 记录测量后的目标绘制尺寸与源裁剪 Rect（如果需要）
    private var targetWidth: Float = 0f
    private var targetHeight: Float = 0f
    private var srcRect: Rect? = null

    override fun measureContent(context: MeasureContext) {
        // 默认原图尺寸
        val iw = image.width.toFloat()
        val ih = image.height.toFloat()

        val sizeIn = sizeIn()

        if (sizeIn.maxWidth.isFinite() || sizeIn.maxHeight.isFinite()) {
            if (overflow == ImageOverflow.Scale) {
                // 按比例缩放以适配 sizeIn（保持纵横比）
                val wLimit = if (sizeIn.maxWidth.isFinite()) sizeIn.maxWidth else iw
                val hLimit = if (sizeIn.maxHeight.isFinite()) sizeIn.maxHeight else ih
                val scale = minOf(wLimit / iw, hLimit / ih, 1f)
                targetWidth = iw * scale
                targetHeight = ih * scale
                contentWidth = targetWidth
                contentHeight = targetHeight
                srcRect = null
            } else {
                // Crop：目标尺寸受限于 sizeIn，但不放大图片；从中心裁剪源图
                val dstW = if (sizeIn.maxWidth.isFinite()) minOf(sizeIn.maxWidth, iw) else iw
                val dstH = if (sizeIn.maxHeight.isFinite()) minOf(sizeIn.maxHeight, ih) else ih
                // 计算源裁剪区域（中心裁剪）
                val srcLeft = ((iw - dstW) / 2f).coerceAtLeast(0f)
                val srcTop = ((ih - dstH) / 2f).coerceAtLeast(0f)
                srcRect = Rect.makeXYWH(srcLeft, srcTop, dstW, dstH)
                targetWidth = dstW
                targetHeight = dstH
                contentWidth = targetWidth
                contentHeight = targetHeight
            }
        } else {
            // 无限制
            targetWidth = iw
            targetHeight = ih
            contentWidth = iw
            contentHeight = ih
            srcRect = null
        }
    }

    override fun layoutChildren(content: Bounds) {}

    override fun drawContent(context: DrawContext) {
        val content = contentBounds()
        val drawX = content.x
        val drawY = content.y

        val dstRect = Rect.makeXYWH(drawX, drawY, targetWidth, targetHeight)
        if (srcRect != null) {
            // 裁剪显示源图的一部分到目标区域（crop）
            context.canvas.drawImageRect(image, srcRect!!, dstRect, Paint())
        } else {
            // 直接缩放绘制或原尺寸绘制（scale）
            val fullSrc = Rect.makeXYWH(0f, 0f, image.width.toFloat(), image.height.toFloat())
            context.canvas.drawImageRect(image, fullSrc, dstRect, Paint())
        }
    }
}
