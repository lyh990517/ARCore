package com.example.arcorestudy

import android.graphics.Color
import com.example.arcorestudy.Sphere
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Sphere(radius: Float, color: Int) {
    private val POINT_COUNT = 20
    private val vertexShaderString = """attribute vec3 aPosition;
attribute vec4 aColor;
uniform mat4 uMvpMatrix; 
varying vec4 vColor;
void main() {
  vColor = aColor;
  gl_Position = uMvpMatrix * vec4(aPosition.x, aPosition.y, aPosition.z, 1.0);
}"""
    private val fragmentShaderString = """precision mediump float;
varying vec4 vColor;
void main() {
  gl_FragColor = vColor;
}"""
    var isInitialized = false
        private set
    private var mProgram = 0
    private val mModelMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mProjMatrix = FloatArray(16)
    private val mVertices: FloatBuffer
    private val mColors: FloatBuffer
    private val mIndices: ShortBuffer
    private val mColor = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    init {
        val vertices = FloatArray(POINT_COUNT * POINT_COUNT * 3)
        for (i in 0 until POINT_COUNT) {
            for (j in 0 until POINT_COUNT) {
                val theta = i * Math.PI.toFloat() / (POINT_COUNT - 1)
                val phi = j * 2 * Math.PI.toFloat() / (POINT_COUNT - 1)
                val x = (radius * Math.sin(theta.toDouble()) * Math.cos(phi.toDouble())).toFloat()
                val y = (radius * Math.cos(theta.toDouble())).toFloat()
                val z = -(radius * Math.sin(theta.toDouble()) * Math.sin(phi.toDouble())).toFloat()
                val index = i * POINT_COUNT + j
                vertices[3 * index] = x
                vertices[3 * index + 1] = y
                vertices[3 * index + 2] = z
            }
        }
        mColor[RED] = Color.red(color) / 255f
        mColor[GREEN] = Color.green(color) / 255f
        mColor[BLUE] = Color.blue(color) / 255f
        mColor[ALPHA] = Color.alpha(color) / 255f
        val colors = FloatArray(POINT_COUNT * POINT_COUNT * 4)
        for (i in 0 until POINT_COUNT) {
            for (j in 0 until POINT_COUNT) {
                val index = i * POINT_COUNT + j
                colors[4 * index + 0] = mColor[RED]
                colors[4 * index + 1] = mColor[GREEN]
                colors[4 * index + 2] = mColor[BLUE]
                colors[4 * index + 3] = mColor[ALPHA]
            }
        }
        val numIndices = 2 * (POINT_COUNT - 1) * POINT_COUNT
        val indices = ShortArray(numIndices)
        var index: Short = 0
        for (i in 0 until POINT_COUNT - 1) {
            if (i and 1 == 0) {
                for (j in 0 until POINT_COUNT) {
                    indices[index++.toInt()] = (i * POINT_COUNT + j).toShort()
                    indices[index++.toInt()] = ((i + 1) * POINT_COUNT + j).toShort()
                }
            } else {
                for (j in POINT_COUNT - 1 downTo 0) {
                    indices[index++.toInt()] = ((i + 1) * POINT_COUNT + j).toShort()
                    indices[index++.toInt()] = (i * POINT_COUNT + j).toShort()
                }
            }
        }
        mVertices = ByteBuffer.allocateDirect(vertices.size * java.lang.Float.SIZE / 8).order(
            ByteOrder.nativeOrder()
        ).asFloatBuffer()
        mVertices.put(vertices)
        mVertices.position(0)
        mColors = ByteBuffer.allocateDirect(colors.size * java.lang.Float.SIZE / 8)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mColors.put(colors)
        mColors.position(0)
        mIndices = ByteBuffer.allocateDirect(indices.size * java.lang.Short.SIZE / 8).order(
            ByteOrder.nativeOrder()
        ).asShortBuffer()
        mIndices.put(indices)
        mIndices.position(0)
    }

    fun init() {
        val vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vShader, vertexShaderString)
        GLES20.glCompileShader(vShader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(vShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile vertex shader.")
            GLES20.glDeleteShader(vShader)
        }
        val fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fShader, fragmentShaderString)
        GLES20.glCompileShader(fShader)
        GLES20.glGetShaderiv(fShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile fragment shader.")
            GLES20.glDeleteShader(fShader)
        }
        mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, vShader)
        GLES20.glAttachShader(mProgram, fShader)
        GLES20.glLinkProgram(mProgram)
        val linked = IntArray(1)
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            Log.e(TAG, "Could not link program.")
        }
        this.isInitialized = true
    }

    fun draw() {
        GLES20.glUseProgram(mProgram)
        val position = GLES20.glGetAttribLocation(mProgram, "aPosition")
        val color = GLES20.glGetAttribLocation(mProgram, "aColor")
        val mvp = GLES20.glGetUniformLocation(mProgram, "uMvpMatrix")
        val mvMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0, mvMatrix, 0)
        GLES20.glUniformMatrix4fv(mvp, 1, false, mvpMatrix, 0)
        GLES20.glEnableVertexAttribArray(position)
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 4 * 3, mVertices)
        GLES20.glEnableVertexAttribArray(color)
        GLES20.glVertexAttribPointer(color, 4, GLES20.GL_FLOAT, false, 4 * 4, mColors)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLE_STRIP,
            mIndices.capacity(),
            GLES20.GL_UNSIGNED_SHORT,
            mIndices
        )
        GLES20.glDisableVertexAttribArray(position)
    }

    fun setModelMatrix(modelMatrix: FloatArray?) {
        System.arraycopy(modelMatrix, 0, mModelMatrix, 0, 16)
    }

    fun setProjectionMatrix(projMatrix: FloatArray?) {
        System.arraycopy(projMatrix, 0, mProjMatrix, 0, 16)
    }

    fun setViewMatrix(viewMatrix: FloatArray?) {
        System.arraycopy(viewMatrix, 0, mViewMatrix, 0, 16)
    }

    companion object {
        private val TAG = Sphere::class.java.simpleName
        const val RED = 0
        const val GREEN = 1
        const val BLUE = 2
        const val ALPHA = 3
    }
}