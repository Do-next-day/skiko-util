# skiko-util

skiko工具库, 包括

- [gif编码](skiko-util-gif-codec/src/main/kotlin/gif)(参考[cssxsh/mirai-skia-plugin](https://github.com/cssxsh/mirai-skia-plugin))
- [对图片处理的抽象框架(封装图片编解码的部分)](skiko-util-gif-codec/src/main/kotlin/frame)
- [文本转图片的简单实现](skiko-util-draw/src/main/kotlin)
- [图片处理](skiko-util-core/src/main/kotlin/handler/list)
- [基于输入图片生成表情](skiko-util-core/src/main/kotlin/handler/face)
- [生成图片](skiko-util-core/src/main/kotlin/generator/list)
- [bdf点阵字体解析](skiko-util-bdf-parser/src/main/kotlin)

## 引入依赖

版本请在[release](https://github.com/4o4E/skiko-util/releases)中查看

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("top.e404:skiko-util-core:${version}")
    implementation("top.e404:skiko-util-draw:${version}")
    implementation("top.e404:skiko-util-gif-codec:${version}")
    implementation("top.e404:skiko-util-util:${version}")
}
```
