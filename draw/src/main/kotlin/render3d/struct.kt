package top.e404.skiko.draw.render3d

import kotlin.math.*

// --- 基础数学扩展 ---

fun Float.clamp(min: Float, max: Float) = max(min, min(this, max))
fun Int.clamp(min: Int, max: Int) = max(min, min(this, max))

/**
 * 线性插值
 */
fun mix(a: Float, b: Float, t: Float): Float = a + (b - a) * t

// --- 向量与矩阵类保持不变，增加部分功能 ---

data class Vec2(val u: Float, val v: Float)

data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)
    operator fun times(other: Vec3) = Vec3(x * other.x, y * other.y, z * other.z)
    operator fun div(scalar: Float) = Vec3(x / scalar, y / scalar, z / scalar)
    operator fun unaryMinus() = Vec3(-x, -y, -z)

    fun cross(other: Vec3) = Vec3(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
    fun dot(other: Vec3) = x * other.x + y * other.y + z * other.z
    fun length() = sqrt(x * x + y * y + z * z)
    fun normalized() = if (length() > 0) this / length() else this

    // 反射向量：R = I - 2(N·I)N
    fun reflect(normal: Vec3): Vec3 {
        val d = this.dot(normal)
        return this - normal * (2f * d)
    }

    // 旋转相关方法保持不变...
    fun rotateX(angle: Float): Vec3 {
        val cos = cos(angle); val sin = sin(angle)
        return Vec3(x, y * cos - z * sin, y * sin + z * cos)
    }
    fun rotateY(angle: Float): Vec3 {
        val cos = cos(angle); val sin = sin(angle)
        return Vec3(x * cos + z * sin, y, -x * sin + z * cos)
    }
    fun rotateZ(angle: Float): Vec3 {
        val cos = cos(angle); val sin = sin(angle)
        return Vec3(x * cos - y * sin, x * sin + y * cos, z)
    }
    fun rotate(rotation: Transformation.Rotate): Vec3 {
        val radX = Math.toRadians(rotation.x.toDouble()).toFloat()
        val radY = Math.toRadians(rotation.y.toDouble()).toFloat()
        val radZ = Math.toRadians(rotation.z.toDouble()).toFloat()
        return this.rotateZ(radZ).rotateY(radY).rotateX(radX)
    }
    fun scale(scaling: Transformation.Scale): Vec3 = Vec3(x * scaling.x, y * scaling.y, z * scaling.z)
    fun translate(translation: Transformation.Translate): Vec3 = this.plus(Vec3(translation.x, translation.y, translation.z))
}

data class Vec4(val x: Float, val y: Float, val z: Float, val w: Float)

data class Mat4(val m: FloatArray = FloatArray(16) { if (it % 5 == 0) 1f else 0f }) {
    operator fun times(other: Mat4): Mat4 {
        val result = Mat4(FloatArray(16))
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                var sum = 0f
                for (k in 0 until 4) sum += this.m[i + k * 4] * other.m[k + j * 4]
                result.m[i + j * 4] = sum
            }
        }
        return result
    }

    fun transform(v: Vec3): Vec4 {
        val x = m[0] * v.x + m[4] * v.y + m[8] * v.z + m[12]
        val y = m[1] * v.x + m[5] * v.y + m[9] * v.z + m[13]
        val z = m[2] * v.x + m[6] * v.y + m[10] * v.z + m[14]
        val w = m[3] * v.x + m[7] * v.y + m[11] * v.z + m[15]
        return Vec4(x, y, z, w)
    }

    companion object {
        fun perspective(fov: Float, aspect: Float, near: Float, far: Float): Mat4 {
            val f = 1.0f / tan(fov / 2.0f)
            return Mat4(floatArrayOf(
                f / aspect, 0f, 0f, 0f,
                0f, f, 0f, 0f,
                0f, 0f, (far + near) / (near - far), -1f,
                0f, 0f, (2 * far * near) / (near - far), 0f
            ))
        }

        fun lookAt(eye: Vec3, center: Vec3, up: Vec3): Mat4 {
            val f = (center - eye).normalized()
            val s = f.cross(up).normalized()
            val u = s.cross(f)
            return Mat4(floatArrayOf(
                s.x, u.x, -f.x, 0f,
                s.y, u.y, -f.y, 0f,
                s.z, u.z, -f.z, 0f,
                -s.dot(eye), -u.dot(eye), f.dot(eye), 1f
            ))
        }

        fun orthographic(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Mat4 {
            val m = FloatArray(16) { 0f }
            m[0] = 2f / (right - left)
            m[5] = 2f / (top - bottom)
            m[10] = -2f / (far - near)
            m[12] = -(right + left) / (right - left)
            m[13] = -(top + bottom) / (top - bottom)
            m[14] = -(far + near) / (far - near)
            m[15] = 1f
            return Mat4(m)
        }
    }

    override fun equals(other: Any?) = (other is Mat4) && m.contentEquals(other.m)
    override fun hashCode() = m.contentHashCode()
}

// --- 数据结构 ---

data class Vertex(val position: Vec3, val uv: Vec2)
data class Face(val indices: List<Int>, val baseColor: Int)
data class Mesh(
    val vertices: List<Vertex>,
    val faces: List<Face>,
    val texture: org.jetbrains.skia.Bitmap? = null,
)

/**
 * 着色顶点：增加了 worldPos 用于光照计算
 */
data class ShadedVertex(
    val screenPos: Vec3, // 屏幕空间坐标 (x, y, z=depth)
    val worldPos: Vec3,  // 世界空间坐标 (用于光照和阴影查找)
    val oneOverW: Float, // 透视校正因子
    val uv: Vec2         // 纹理坐标
)

sealed class Transformation {
    data class Rotate(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f) : Transformation()
    data class Scale(val x: Float = 1f, val y: Float = 1f, val z: Float = 1f) : Transformation()
    data class Translate(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f) : Transformation()
}