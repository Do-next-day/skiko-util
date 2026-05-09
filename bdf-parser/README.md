# bdf-parser

解析 BDF(Bitmap Distribution Format) 点阵字体，并把字符映射为可访问的位图矩阵。

> 注意：当前模块源码包名是 `top.e404.dbf`，使用时请按实际包名导入。

## 引入

```kotlin
dependencies {
    implementation("top.e404.skiko-util:skiko-util-bdf-parser:<version>")
}
```

## 核心 API

- `BdfParser.parse(file: File)`: 从文件解析 BDF 字体。
- `BdfParser.parse(text: String)`: 从字符串解析 BDF 字体。
- `BdfParser.parse(reader: BufferedReader)`: 从 reader 解析 BDF 字体。
- `BdfFont.header`: 字体头信息，包含版本、字体名、尺寸、全局包围盒和字符数量。
- `BdfFont.getBitmap(text: String)`: 获取字符串首字符的点阵。
- `BdfFont.getBitmaps(text: String)`: 获取字符串中每个字符的点阵。
- `BitMatrix[x, y]`: 读取或设置某个坐标上的 bit。
- `BitMatrix.forEachBit { x, y, bit -> ... }`: 遍历点阵。

## 示例

```kotlin
import top.e404.dbf.BdfParser
import top.e404.dbf.BitMatrix
import java.io.File

private fun BitMatrix.printMatrix() {
    for (y in yRange) {
        for (x in xRange) {
            print(if (this[x, y]) "#" else ".")
        }
        println()
    }
}

fun main() {
    val font = BdfParser.parse(File("unifont-15.0.03.bdf"))

    font.getBitmaps("你好").forEach { char ->
        char?.bitMatrix?.printMatrix()
        println()
    }
}
```

## 说明

- 坐标原点在左上角，`x` 向右，`y` 向下。
- `getBitmaps` 返回值里的元素可能为 `null`，表示 BDF 字体里不存在对应字符。
- `BitMatrix.plus(other)` 会把两个点阵横向拼接，适合把多个字形组合成一行。
