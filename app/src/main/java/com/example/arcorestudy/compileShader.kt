package com.example.arcorestudy

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.opengl.GLES20.*
import android.util.Log
import androidx.annotation.RawRes
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjUtils
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

fun compileShader(type: Int, code: String) = glCreateShader(type).also { shader ->
    glShaderSource(shader, code)
    glCompileShader(shader)
    val result = intArrayOf(-99999)
    glGetShaderiv(shader, GL_COMPILE_STATUS, result, 0)
    val isSuccess = if (result[0] == 1) "Success" else "Fail"
    when (type) {
        GL_VERTEX_SHADER -> Log.e("shader compile", "Vertex Shader Compile $isSuccess")
        GL_FRAGMENT_SHADER -> Log.e("shader compile", "Fragment Shader Compile $isSuccess")
    }
}

fun fromAssets(context: Context, assetPath: String): Mesh {
    val obj = context.assets.open(assetPath)
        .let { stream -> ObjReader.read(stream) }
        .let { objStream -> ObjUtils.convertToRenderable(objStream) }
    return Mesh(
        indices = ObjData.getFaceVertexIndices(obj),
        vertices = ObjData.getVertices(obj),
        normals = ObjData.getNormals(obj),
        texCoords = ObjData.getTexCoords(obj, 2)
    )
}

var deviceSize = Pair(0, 0)
var angle = 0f
fun toFloatArray(mat4: Mat4): FloatArray {
    var a: FloatArray = floatArrayOf()
    with(mat4) {
        a = floatArrayOf(
            a0, a1, a2, a3,
            b0, b1, b2, b3,
            c0, c1, c2, c3,
            d0, d1, d2, d3
        )
    }
    return a
}

fun FloatArray.toMat4(): Mat4 {
    return if (this.size != 16)
        Mat4()
    else
        Mat4(
            Vec4(this[0], this[4], this[8], this[12]),
            Vec4(this[1], this[5], this[9], this[13]),
            Vec4(this[2], this[6], this[10], this[14]),
            Vec4(this[3], this[7], this[11], this[15])
        )
}

fun rotateX() = glm.rotate(Mat4(), glm.PIf * 0.5f, Vec3(1, 0, 0))
fun rotateY() = glm.rotate(Mat4(), glm.PIf * 0.5f, Vec3(0, 1, 0))
fun rotateZ() = glm.rotate(Mat4(), glm.PIf * 0.5f, Vec3(0, 0, 1))

fun scaleUp() = glm.scale(Mat4(), Vec3(2f, 2f, 2f))
fun scaleDown() = glm.scale(Mat4(), Vec3(0.5f, 0.5f, 0.5f))

fun product(mat1: Mat4, mat2: Mat4) = glm.matrixCompMult(mat1, mat2)

fun createFloatBuffer(capacity: Int): FloatBuffer =
    ByteBuffer.allocateDirect(capacity * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder()).asFloatBuffer()

fun FloatArray.toFloatBuffer(): FloatBuffer = ByteBuffer
    .allocateDirect(this.size * Float.SIZE_BYTES)
    .order(ByteOrder.nativeOrder())
    .asFloatBuffer().also {
        it.put(this).position(0)
    }

fun IntArray.toIntBuffer(): IntBuffer = ByteBuffer
    .allocateDirect(this.size * Int.SIZE_BYTES)
    .order(ByteOrder.nativeOrder())
    .asIntBuffer().also {
        it.put(this).position(0)
    }

fun loadBitmap(context: Context, @RawRes textureId: Int) = BitmapFactory.decodeResource(
    context.resources,
    textureId,
    BitmapFactory.Options().apply { inScaled = false })

fun Resources.readRawTextFile(@RawRes id: Int) =
    openRawResource(id).bufferedReader().use { it.readText() }