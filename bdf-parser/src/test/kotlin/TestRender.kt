package top.e404.dbf

import kotlin.test.Test

class TestRender {
    private fun BitMatrix.printMatrix() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                print(if (get(x, y)) "⬛" else "⬜️")
            }
            println()
        }
    }

    @Test
    fun test() {
        val url = TestRender::class.java.classLoader.getResource("unifont-15.0.03.bdf")!!
        val bdfFont = BdfParser.parse(url.readText())
        bdfFont.getBitmaps("疯狂星期四").forEach {
            it!!.bitMatrix.printMatrix()
        }
    }
}