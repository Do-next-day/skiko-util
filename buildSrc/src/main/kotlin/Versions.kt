object Versions {
    const val kotlin = "1.6.21"
    const val skiko = "0.7.20"
}

fun kotlinx(id: String, version: String = Versions.kotlin) = "org.jetbrains.kotlinx:kotlinx-$id:$version"
fun skiko(module: String, version: String = Versions.skiko) = "org.jetbrains.skiko:skiko-awt-runtime-$module:$version"
