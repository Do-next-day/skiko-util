# Compose 渲染抽象实施计划

目标版本：`2.0.0`

本文档描述 `draw` 模块中 Compose 风格绘图 DSL 的一次破坏性改造：把文本测量和绘制输出从 Skiko 原生 API 中抽出来，形成可替换、可记录、可 mock 的渲染接口。目标是让布局、文本换行、省略号、绘制命令等行为可以自动测试，而不是只靠人工查看生成图片。

## 目标

- 移除 Compose 测量阶段对 `Surface` 的直接依赖。
- 移除 Compose 元素绘制阶段对 `Canvas` 的直接依赖。
- 把 `Font.measureTextWidth`、`Font.metrics` 等影响布局的调用收口到 `TextMeasurer`。
- 把 `Canvas.draw*`、`save`、`restore`、`clip` 等绘制调用收口到 `DrawCanvas`。
- 提供 Skiko 后端实现，用于真实渲染。
- 提供 recording/fake 后端，用于单元测试中断言绘制命令。
- 保留少量 golden image 测试，验证 Skiko 后端实际渲染效果。

第一阶段的目标是隔离 `Surface`/`Canvas` 和可控文本测量，不是让所有单元测试完全不接触 Skiko 类型。`Font`、`Paint`、`Path`、`Rect`、`Image` 可以在第一阶段继续作为参数类型存在；测试应通过 fake 测量器和 recording 命令快照减少对 Skiko native 绘制行为的依赖。

## 非目标

- 不在第一阶段完全包装所有 Skiko 类型。
- 不把 `Image`、`Font`、`Paint`、`Path`、`Rect` 一次性全部替换成项目自定义类型。
- 不保证 1.x 的 `CanvasElement` 和图表主题 API 二进制兼容；本次允许破坏性变更。

## 当前问题

当前 Compose 绘图路径里，测量和绘制都直接依赖 Skiko：

- `UiElement.measure(surface: Surface)`
- `UiElement.draw(canvas: Canvas)`
- `Text.measureContent` 直接调用 `font.measureTextWidth(...)` 和 `font.metrics`
- `BaseElement.drawBehind` 直接调用 `canvas.drawRect(...)`
- `Text.drawContent` 直接调用 `canvas.drawString(...)`
- `ImageElement.drawContent` 直接调用 `canvas.drawImageRect(...)`
- `CanvasElement` 直接暴露 Skiko `Canvas`
- `charts` 和 `icon` 中也直接操作 `Canvas`、`Paint`、`Path`

这些调用导致测试很难替换底层行为，只能生成图片后人工查看或做脆弱的像素比较。

## 新架构

### MeasureContext

测量阶段只依赖文本测量能力。

示例 API：

```kotlin
class MeasureContext(
    val textMeasurer: TextMeasurer = SkiaTextMeasurer
)
```

`UiElement` 的测量签名改为：

```kotlin
fun measure(context: MeasureContext)
```

### TextMeasurer

`TextMeasurer` 负责所有影响文本布局的测量行为。

示例 API：

```kotlin
interface TextMeasurer {
    fun measureTextWidth(text: String, font: Font, paint: Paint): Float
    fun metrics(font: Font): TextMetrics
}

data class TextMetrics(
    val ascent: Float,
    val descent: Float
) {
    val lineHeight: Float get() = descent - ascent
}
```

生产实现：

```kotlin
object SkiaTextMeasurer : TextMeasurer {
    override fun measureTextWidth(text: String, font: Font, paint: Paint): Float =
        font.measureTextWidth(text, paint)

    override fun metrics(font: Font): TextMetrics =
        font.metrics.let { TextMetrics(it.ascent, it.descent) }
}
```

测试实现示例：

```kotlin
class FixedTextMeasurer(
    private val charWidth: Float = 10f,
    private val ascent: Float = -16f,
    private val descent: Float = 4f
) : TextMeasurer {
    override fun measureTextWidth(text: String, font: Font, paint: Paint): Float =
        text.length * charWidth

    override fun metrics(font: Font): TextMetrics =
        TextMetrics(ascent, descent)
}
```

### DrawContext

绘制阶段只依赖抽象画布。

示例 API：

```kotlin
class DrawContext(
    val canvas: DrawCanvas
)
```

`UiElement` 的绘制签名改为：

```kotlin
fun draw(context: DrawContext)
```

### DrawCanvas

`DrawCanvas` 是 Compose DSL 的绘制出口。第一阶段可以保留 Skiko 的 `Rect`、`Path`、`Paint`、`Image`、`Font` 作为参数，重点先隔离 `Canvas`。

示例 API：

```kotlin
interface DrawCanvas {
    fun clear(color: Int)
    fun save()
    fun restore()
    fun translate(dx: Float, dy: Float)
    fun scale(sx: Float, sy: Float)
    fun clipPath(path: Path, antiAlias: Boolean = true)

    fun drawRect(rect: Rect, paint: Paint)
    fun drawString(text: String, x: Float, y: Float, font: Font, paint: Paint)
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
```

生产实现 `SkiaDrawCanvas` 只负责转发到底层 Skiko `Canvas`。

测试实现 `RecordingDrawCanvas` 只记录命令，不真正绘图。

### 共享渲染流程

`render` 和 `renderCommands` 必须走同一套内部流程，只允许最后的绘制后端不同，避免测试路径和真实渲染路径漂移。

建议抽出内部函数：

```kotlin
private fun prepareRenderTree(
    root: UiElement,
    measureContext: MeasureContext,
    content: Composable
): PreparedRenderTree
```

共享流程：

1. `root.apply(content)`
2. `root.measure(measureContext)`
3. 校验根节点最终尺寸。
4. `root.layout(0f, 0f)`
5. 执行当前 `render` 已有的子元素边界修正逻辑，或把该逻辑重命名为明确的 `normalizeLayoutBounds(root, finalWidth, finalHeight)`。
6. 返回包含 `root`、`width`、`height` 的 `PreparedRenderTree`。

`render` 使用 `SkiaDrawCanvas` 执行绘制，`renderCommands` 使用 `RecordingDrawCanvas` 执行绘制。两者都必须通过 `DrawCanvas.clear(backgroundColor)` 开始绘制。

## 记录命令设计

测试不应该直接比较 `Paint` 对象实例。`RecordingDrawCanvas` 应记录稳定字段，例如颜色、模式、线宽、抗锯齿和几何参数。

`RecordingDrawCanvas` 不允许保存可变 Skiko 对象引用。每次绘制方法被调用时，都必须立即把传入的 `Paint`、`Font`、`Rect`、`Path` 等对象转换为不可变快照，避免后续代码复用并修改同一个对象时污染已记录命令。

示例快照：

```kotlin
data class PaintSnapshot(
    val color: Int,
    val mode: PaintMode,
    val strokeWidth: Float,
    val antiAlias: Boolean
)

data class FontSnapshot(
    val size: Float
)

data class RectSnapshot(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}
```

`Path` 的快照可以分阶段处理：

- 第一阶段允许记录 `PathSnapshot(description = path.toString())` 或记录 `DrawCommand.Path` 的存在和 paint 快照，只用于粗粒度断言。
- 需要精确断言图标和复杂图表时，再引入语义命令或自定义 path builder。

示例命令：

```kotlin
sealed interface DrawCommand {
    data class Clear(val color: Int) : DrawCommand
    data class Save(val depth: Int) : DrawCommand
    data class Restore(val depth: Int) : DrawCommand
    data class Rect(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val color: Int,
        val antiAlias: Boolean
    ) : DrawCommand
    data class Text(
        val text: String,
        val x: Float,
        val baselineY: Float,
        val fontSize: Float,
        val color: Int
    ) : DrawCommand
}
```

### Transform 记录语义

`RecordingDrawCanvas` 第一阶段采用“原始命令流”策略：

- `save`、`restore`、`translate`、`scale` 都作为独立命令记录。
- 后续 `drawRect`、`drawPath`、`drawCircle` 等命令记录调用时传入的原始参数，不应用当前 transform 得到最终坐标。
- 测试应断言局部命令序列，或者使用辅助函数查找某段 `save -> transform -> draw -> restore`。

示例命令：

```kotlin
sealed interface DrawCommand {
    data class Translate(val dx: Float, val dy: Float) : DrawCommand
    data class Scale(val sx: Float, val sy: Float) : DrawCommand
}
```

选择原始命令流的原因：

- 实现简单。
- 更贴近实际 `Canvas` 调用顺序。
- 对 `Path`、`clipPath` 等复杂几何不需要自己实现矩阵变换。

如果后续测试更关心最终几何结果，可以新增 `TransformResolvingRecordingDrawCanvas`，但不要混用两种语义。

如果后续要进一步减少 Skiko 泄漏，可以把 `Paint` 映射为项目内的 `DrawStyle`：

```kotlin
data class DrawStyle(
    val color: Int,
    val mode: DrawPaintMode = DrawPaintMode.Fill,
    val strokeWidth: Float = 1f,
    val antiAlias: Boolean = true
)
```

该步骤可以作为第二阶段，不必阻塞第一阶段落地。

## 入口 API 变化

### render

当前：

```kotlin
fun render(backgroundColor: Int = Color.TRANSPARENT, root: UiElement = Column(), content: Composable): Image
```

建议：

```kotlin
fun render(
    backgroundColor: Int = Color.TRANSPARENT,
    root: UiElement = Column(),
    measureContext: MeasureContext = MeasureContext(),
    content: Composable
): Image
```

内部流程：

1. `root.apply(content)`
2. `root.measure(measureContext)`
3. 创建 Skiko `Surface`
4. 用 `SkiaDrawCanvas(surface.canvas)` 创建 `DrawContext`
5. `drawContext.canvas.clear(backgroundColor)`
6. `root.layout(0f, 0f)`
7. `root.draw(drawContext)`
8. 返回 `surface.makeImageSnapshot()`

### renderCommands

新增测试专用入口，用于不生成图片，只得到绘制命令。

示例：

```kotlin
fun renderCommands(
    backgroundColor: Int = Color.TRANSPARENT,
    root: UiElement = Column(),
    measureContext: MeasureContext,
    content: Composable
): List<DrawCommand>
```

该入口用于单元测试：

```kotlin
val commands = renderCommands(
    backgroundColor = Color.WHITE,
    measureContext = MeasureContext(FixedTextMeasurer())
) {
    text(
        "hello",
        Modifier
            .fontSize(20f)
            .textColor(Color.WHITE)
    )
}

assertEquals(
    listOf(
        DrawCommand.Clear(Color.WHITE),
        DrawCommand.Text("hello", x = 0f, baselineY = 16f, fontSize = 20f, color = Color.WHITE)
    ),
    commands
)
```

`renderCommands` 必须和 `render` 一样执行完整的 measure/layout/normalize/draw 流程。它不应该有独立的简化渲染流程。

## 破坏性变更

### UiElement

当前：

```kotlin
fun measure(surface: Surface)
fun draw(canvas: Canvas)
```

变更为：

```kotlin
fun measure(context: MeasureContext)
fun draw(context: DrawContext)
```

影响：

- 自定义 `UiElement` 实现需要更新方法签名。
- 原来在 `measureContent(surface)` 中使用 Skiko surface 的代码需要改用 context。

### BaseElement

当前：

```kotlin
abstract fun measureContent(surface: Surface)
abstract fun drawContent(canvas: Canvas)
```

变更为：

```kotlin
abstract fun measureContent(context: MeasureContext)
abstract fun drawContent(context: DrawContext)
```

### CanvasElement

当前直接暴露 Skiko `Canvas`：

```kotlin
class CanvasElement(
    override var width: Float,
    override var height: Float,
    val draw: CanvasElement.(Canvas) -> Unit
)
```

变更为暴露 `DrawCanvas`：

```kotlin
class CanvasElement(
    override var width: Float,
    override var height: Float,
    val draw: CanvasElement.(DrawCanvas) -> Unit
)
```

迁移示例：

```kotlin
CanvasElement(100f, 100f) { canvas ->
    canvas.drawRect(Rect.makeXYWH(parentX, parentY, 100f, 100f), paint)
}
```

如果用户确实需要原生 Skiko `Canvas`，可以提供一个显式的逃生入口：

```kotlin
class SkiaCanvasElement(
    override var width: Float,
    override var height: Float,
    val draw: SkiaCanvasElement.(Canvas) -> Unit
)
```

该类型不参与 command recording 测试，只用于高级自定义绘制。

### 图表和图标

`bar`、`radar`、`icon` 应从直接接收 `Canvas` 改为接收 `DrawCanvas`。

当前：

```kotlin
fun drawDonutChart(canvas: Canvas, ...)
fun drawRadarChart(canvas: Canvas, ...)
fun drawIcon(canvas: Canvas, ...)
```

变更为：

```kotlin
fun drawDonutChart(canvas: DrawCanvas, ...)
fun drawRadarChart(canvas: DrawCanvas, ...)
fun drawIcon(canvas: DrawCanvas, ...)
```

第一阶段图表主题中的 `Paint` 可以暂时保留。第二阶段再把主题字段改为 `DrawStyle`，让 recording 命令更稳定。

## 实施步骤

### 1. 新增上下文和接口

新增文件建议：

- `draw/src/main/kotlin/compose/rendering/TextMeasurer.kt`
- `draw/src/main/kotlin/compose/rendering/DrawCanvas.kt`
- `draw/src/main/kotlin/compose/rendering/SkiaDrawCanvas.kt`
- `draw/src/main/kotlin/compose/rendering/RecordingDrawCanvas.kt`
- `draw/src/main/kotlin/compose/rendering/DrawCommand.kt`

包名可以使用：

```kotlin
package top.e404.skiko.draw.compose.rendering
```

如果希望用户导入更少，可以在 `top.e404.skiko.draw.compose` 中 re-export 常用类型，或者直接把接口放在该包下。

### 2. 改造测量签名

更新：

- `UiElement.measure`
- `BaseElement.measure`
- 所有 `measureContent`
- `Column`、`Row`、`Box`、`Table`、`Text`、`ImageElement`、`CanvasElement`

重点：

- 容器元素只把 context 传给子元素。
- `Text` 使用 `context.textMeasurer.measureTextWidth(...)`。
- `Text` 使用 `context.textMeasurer.metrics(font)`。
- `ImageElement` 只依赖 `image.width`、`image.height`，不需要 text measurer。

### 3. 改造绘制签名

更新：

- `UiElement.draw`
- `BaseElement.draw`
- `drawBehind`
- 所有 `drawContent`

重点替换：

- `canvas.save()` -> `context.canvas.save()`
- `canvas.restore()` -> `context.canvas.restore()`
- `canvas.clipPath(...)` -> `context.canvas.clipPath(...)`
- `canvas.drawRect(...)` -> `context.canvas.drawRect(...)`
- `canvas.drawString(...)` -> `context.canvas.drawString(...)`
- `canvas.drawImageRect(...)` -> `context.canvas.drawImageRect(...)`

### 4. 改造 render 入口

`render` 内部不再创建 1x1 `Surface` 作为测量参数。测量只需要：

```kotlin
root.measure(measureContext)
```

真实绘制时才创建 `Surface`：

```kotlin
val drawContext = DrawContext(SkiaDrawCanvas(surface.canvas))
```

同时新增共享的 prepare 流程，供 `render` 和 `renderCommands` 复用：

```kotlin
val prepared = prepareRenderTree(root, measureContext, content)
```

`render` 伪流程：

```kotlin
val prepared = prepareRenderTree(root, measureContext, content)
return Surface.makeRasterN32Premul(prepared.width, prepared.height).use { surface ->
    val drawContext = DrawContext(SkiaDrawCanvas(surface.canvas))
    drawContext.canvas.clear(backgroundColor)
    prepared.root.draw(drawContext)
    surface.makeImageSnapshot()
}
```

`renderCommands` 伪流程：

```kotlin
val prepared = prepareRenderTree(root, measureContext, content)
val recorder = RecordingDrawCanvas()
val drawContext = DrawContext(recorder)
drawContext.canvas.clear(backgroundColor)
prepared.root.draw(drawContext)
return recorder.commands
```

### 5. 改造 CanvasElement、icon、charts

先把自定义绘制入口迁移到 `DrawCanvas`。如果某些高级能力暂时没有抽象方法，可以在 `DrawCanvas` 上补最小方法，而不是把 Skiko `Canvas` 暴露回去。

对于图表：

- `bar.kt` 需要 `drawArc`、`drawCircle`、`clipPath`、`save`、`restore`
- `radar.kt` 需要 `drawPath`、`drawTextLine` 或替代的文本绘制方法、`drawLine`
- `icon.kt` 需要 `save`、`restore`、`translate`、`scale`、`drawPath`

`drawTextLine` 可以暂时用一个专门方法：

```kotlin
fun drawTextLine(line: TextLine, x: Float, y: Float, paint: Paint)
```

`drawTextLine` 只作为迁移期兼容路径，不作为新代码推荐 API。新图表和新组件应优先使用 `TextMeasurer + drawString`，这样文本测量和绘制都可以被 fake/recording 后端稳定测试。`radar.kt` 应在迁移后优先改为 `drawString`，只有在短期保持行为一致有困难时才临时保留 `drawTextLine`。

### 6. 新增测试

推荐测试类型：

#### 布局测试

使用 fake text measurer，让文本宽度和行高完全可预测。

示例断言：

```kotlin
val root = Column()
root.apply {
    text("hello", Modifier.fontSize(20f))
}

root.measure(MeasureContext(FixedTextMeasurer(charWidth = 10f)))
root.layout(0f, 0f)

assertEquals(50f, root.width)
assertEquals(20f, root.height)
```

#### 绘制命令测试

使用 recording canvas，断言绘制命令。

示例断言：

```kotlin
val recorder = RecordingDrawCanvas()
root.draw(DrawContext(recorder))

assertContains(
    recorder.commands,
    DrawCommand.Text(
        text = "hello",
        x = 0f,
        baselineY = 16f,
        fontSize = 20f,
        color = Color.WHITE
    )
)
```

命令断言应使用专门辅助函数处理浮点误差和局部序列匹配：

```kotlin
assertCommandContains(
    recorder.commands,
    DrawCommand.Text(
        text = "hello",
        x = 0f,
        baselineY = 16f,
        fontSize = 20f,
        color = Color.WHITE
    ),
    epsilon = 0.01f
)
```

对包含 `translate`、`scale` 的绘制，优先断言原始命令流：

```kotlin
assertCommandSequence(
    recorder.commands,
    listOf(
        DrawCommand.Save(depth = 1),
        DrawCommand.Translate(dx = 10f, dy = 20f),
        DrawCommand.Scale(sx = 2f, sy = 2f),
        DrawCommand.Path(...),
        DrawCommand.Restore(depth = 1)
    )
)
```

#### Golden image 测试

保留少量端到端测试，用真实 `SkiaTextMeasurer` 和 `SkiaDrawCanvas` 渲染图片，然后和基准图做带容差的像素比较。

覆盖场景建议：

- 基础文本
- padding/margin/border/background
- clip
- wrap/ellipsis
- image scale/crop
- table
- charts

## 迁移示例

### 自定义元素迁移

1.x：

```kotlin
class Badge : BaseElement() {
    override fun measureContent(surface: Surface) {
        contentWidth = 100f
        contentHeight = 32f
    }

    override fun drawContent(canvas: Canvas) {
        canvas.drawRect(Rect.makeXYWH(x, y, width, height), paint)
    }
}
```

2.x：

```kotlin
class Badge : BaseElement() {
    override fun measureContent(context: MeasureContext) {
        contentWidth = 100f
        contentHeight = 32f
    }

    override fun drawContent(context: DrawContext) {
        context.canvas.drawRect(Rect.makeXYWH(x, y, width, height), paint)
    }
}
```

### 文本测量迁移

1.x：

```kotlin
val width = font.measureTextWidth(text, paint)
val lineHeight = font.metrics.descent - font.metrics.ascent
```

2.x：

```kotlin
val width = context.textMeasurer.measureTextWidth(text, font, paint)
val metrics = context.textMeasurer.metrics(font)
val lineHeight = metrics.lineHeight
```

### 自定义 canvas 迁移

1.x：

```kotlin
CanvasElement(120f, 80f) { canvas ->
    canvas.drawCircle(parentX + 40f, parentY + 40f, 24f, paint)
}
```

2.x：

```kotlin
CanvasElement(120f, 80f) { canvas ->
    canvas.drawCircle(parentX + 40f, parentY + 40f, 24f, paint)
}
```

调用形式不变，但参数类型从 `Canvas` 变为 `DrawCanvas`。大部分基础绘制调用可以保持同名。

## 优化空间

### 抽出 DrawStyle

第一阶段保留 `Paint` 能减少改造成本，但测试命令里不应该直接比较 `Paint`。第二阶段可以引入 `DrawStyle`，逐步替代公开 API 中的 `Paint`。

适合优先替换：

- `Background`
- `Border`
- `TextColor`
- `IconTheme`
- `BarTheme`
- `RadarTheme`

### 抽出 ImageRef

如果后续希望在测试中完全不加载真实图片，可以引入：

```kotlin
interface ImageRef {
    val width: Int
    val height: Int
}
```

生产实现包装 `Image`，测试实现只提供尺寸。第一阶段不必做，除非 image 布局测试需要大量 fake image。

### 把 shape 转为语义 clip

当前 `Clip` 依赖 `Shape.createPath(width, height): Path`。Recording canvas 如果只记录 `Path` 不够可读，可以新增语义命令：

```kotlin
data class ClipShape(
    val shape: Shape,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
) : DrawCommand
```

真实 backend 再把 shape 转为 Skiko path。

### 稳定浮点比较

测试里不要用严格 `assertEquals` 比较复杂浮点。建议提供：

```kotlin
assertFloatEquals(expected, actual, epsilon = 0.01f)
assertCommandEquals(expected, actual, epsilon = 0.01f)
```

### 分层测试

最终测试金字塔建议：

- 大量 layout tests：不创建 Skiko surface，不生成图片。
- 中量 command tests：验证绘制命令、坐标、颜色、clip/save/restore。
- 少量 golden image 测试：验证真实 Skiko 渲染链路。

## 风险

- `CanvasElement` 的自由度较高，抽象接口可能初期不够用，需要补方法。
- `charts/radar.kt` 使用 `TextLine` 做标签测量和绘制，迁移时要决定保留 `TextLine` 包装方法，还是统一改用 `TextMeasurer + drawString`。
- 直接记录 `Paint`、`Rect`、`Font`、`Path` 等可变或 native 对象会导致测试脆弱，recording 命令必须保存不可变快照。
- `RecordingDrawCanvas` 第一阶段记录原始 transform 命令，不计算最终几何；测试作者需要按命令流语义写断言。
- 破坏性变更会影响所有自定义 `UiElement` 用户，需要在 `draw/README.md` 和 release note 中说明。

## 完成标准

- `render` 使用真实 Skiko backend 后输出行为与 1.x 保持一致。
- `render` 和 `renderCommands` 共用同一个 measure/layout/normalize/draw 准备流程。
- Compose 核心元素不再直接依赖 `Surface` 测量。
- Compose 核心元素不再直接依赖 `Canvas` 绘制。
- 文本 wrap/ellipsis 可以用 fake 测量器做稳定单元测试。
- `RecordingDrawCanvas` 对 `Paint`、`Rect`、`Font` 等参数保存不可变快照。
- `RecordingDrawCanvas` 的 transform 语义有测试覆盖。
- background/border/text/image 至少有 command recording 测试。
- 保留至少一组 golden image 测试验证 Skiko backend。
