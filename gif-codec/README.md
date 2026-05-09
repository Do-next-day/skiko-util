# gif-codec

基于 Skiko `Codec`/`Bitmap`/`Image` 的 GIF 编码和帧处理模块。

## 引入

```kotlin
dependencies {
    implementation("top.e404.skiko-util:skiko-util-gif-codec:<version>")
}
```

使用时需要确保运行时 classpath 中存在对应平台的 Skiko runtime。

## 核心 API

- `gif(width, height) { ... }`: 使用 DSL 创建 GIF，并返回 Skia `Data`。
- `GIFBuilder.frame(bitmap) { ... }`: 添加一帧并设置帧参数。
- `ByteArray.decodeToFrames()`: 把图片字节解码成帧列表；GIF 会保留多帧。
- `List<Frame>.encodeToBytes()`: 单帧编码为 PNG，多帧编码为 GIF。
- `List<Frame>.handle { image -> ... }`: 并行处理每一帧图片。
- `MutableList<Frame>.common(args)`: 应用通用参数，支持 `d` 持续时间、`w` 宽度、`h` 高度。
- `Image.toFrame()` / `Image.toFrames()`: 把 Skia `Image` 包装成帧。

## 直接创建 GIF

```kotlin
import org.jetbrains.skia.AnimationDisposalMode
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import top.e404.skiko.gif.gif
import java.io.File

private fun makeFrame(color: Int): Bitmap {
    val bitmap = Bitmap()
    bitmap.allocN32Pixels(160, 120)
    Canvas(bitmap).drawCircle(80f, 60f, 42f, Paint().apply {
        this.color = color
        isAntiAlias = true
    })
    return bitmap
}

fun main() {
    val data = gif(width = 160, height = 120) {
        loop(0)
        options {
            duration = 120
            disposalMethod = AnimationDisposalMode.RESTORE_BG_COLOR
        }

        frame(makeFrame(Color.makeRGB(72, 149, 239)))
        frame(makeFrame(Color.makeRGB(247, 127, 0)))
        frame(makeFrame(Color.makeRGB(76, 175, 80)))
    }

    File("out/demo.gif").apply { parentFile.mkdirs() }.writeBytes(data.bytes)
}
```

## 处理已有图片或 GIF

```kotlin
import kotlinx.coroutines.runBlocking
import top.e404.skiko.frame.decodeToFrames
import top.e404.skiko.frame.encodeToBytes
import top.e404.skiko.frame.handle
import top.e404.skiko.util.resize
import java.io.File

fun main() = runBlocking {
    val input = File("input.gif").readBytes().decodeToFrames()

    val output = input
        .handle { image -> image.resize(320, 320, smooth = true) }
        .encodeToBytes()

    File("out/resized.gif").apply { parentFile.mkdirs() }.writeBytes(output)
}
```

## 说明

- `duration` 单位是毫秒。
- `encodeToBytes` 要求至少有一帧；空列表会抛出 `IllegalArgumentException`。
- 单帧会输出 PNG，多帧才会输出 GIF。
- `GIFBuilder` 会根据局部色表、全局色表或自动量化结果写入颜色表。
