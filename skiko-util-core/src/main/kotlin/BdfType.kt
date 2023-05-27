package top.e404.skiko

import top.e404.dbf.BdfParser
import java.io.File

enum class BdfType(val path: String) {
    UNI_FONT("unifont-15.0.03.bdf");

    companion object {
        var dir = "data/bdf"
    }

    val file by lazy { File("$dir/$path") }
    val font by lazy { BdfParser.parse(file) }
}
