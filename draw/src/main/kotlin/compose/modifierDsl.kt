package top.e404.skiko.draw.compose

fun Modifier.sizeIn(
    minWidth: Float = 0f,
    maxWidth: Float = Float.POSITIVE_INFINITY,
    minHeight: Float = 0f,
    maxHeight: Float = Float.POSITIVE_INFINITY
): Modifier = this.then(SizeIn(minWidth, maxWidth, minHeight, maxHeight))

fun Modifier.widthIn(min: Float = 0f, max: Float = Float.POSITIVE_INFINITY): Modifier =
    this.then(SizeIn(minWidth = min, maxWidth = max))

fun Modifier.heightIn(min: Float = 0f, max: Float = Float.POSITIVE_INFINITY): Modifier =
    this.then(SizeIn(minHeight = min, maxHeight = max))

fun Modifier.size(width: Float, height: Float): Modifier = this.then(Size(width, height))
fun Modifier.size(all: Float): Modifier = this.then(Size(all, all))
fun Modifier.width(width: Float): Modifier = this.then(Size(width = width))
fun Modifier.height(height: Float): Modifier = this.then(Size(height = height))

fun Modifier.background(color: Int): Modifier = this.then(Background(color))
fun Modifier.clip(shape: Shape): Modifier = this.then(Clip(shape))

fun Modifier.padding(all: Float): Modifier = this.then(Padding(all, all, all, all))
fun Modifier.padding(horizontal: Float = 0f, vertical: Float = 0f): Modifier =
    this.then(Padding(vertical, horizontal, vertical, horizontal))

fun Modifier.padding(top: Float = 0f, right: Float = 0f, bottom: Float = 0f, left: Float = 0f): Modifier =
    this.then(Padding(top, right, bottom, left))

fun Modifier.border(all: Float, color: Int): Modifier = this.then(Border(all, all, all, all, color))
fun Modifier.border(horizontal: Float = 0f, vertical: Float = 0f, color: Int): Modifier =
    this.then(Border(vertical, horizontal, vertical, horizontal, color))

fun Modifier.border(top: Float = 0f, right: Float = 0f, bottom: Float = 0f, left: Float = 0f, color: Int): Modifier =
    this.then(Border(top, right, bottom, left, color))

fun Modifier.antiAlias(enabled: Boolean = true) = this.then(AntiAlias(enabled))


