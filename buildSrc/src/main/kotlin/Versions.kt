object Versions {
    const val group = "top.e404"
    const val version = "1.1.0"
    const val kotlin = "1.8.20"
    const val skiko = "0.7.37"
}

fun kotlinx(id: String, version: String = Versions.kotlin) = "org.jetbrains.kotlinx:kotlinx-$id:$version"
fun skiko(module: String, version: String = Versions.skiko) = "org.jetbrains.skiko:skiko-awt-runtime-$module:$version"
