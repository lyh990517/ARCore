package com.example.arcorestudy

import android.graphics.Color
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Line(
    startX: Float,
    startY: Float,
    startZ: Float,
    endX: Float,
    endY: Float,
    endZ: Float,
    lineWidth: Int,
    color: Int
) {
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
    private var mLineWidth = 1.0f

    init {
        val vertices = floatArrayOf(startX, startY, startZ, endX, endY, endZ)
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f
        val a = Color.alpha(color) / 255f
        val colors = floatArrayOf(r, g, b, a, r, g, b, a)
        val indices = shortArrayOf(0, 1)
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
        mLineWidth = lineWidth.toFloat()
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
        GLES20.glLineWidth(mLineWidth)
        GLES20.glDrawElements(
            GLES20.GL_LINES,
            mIndices.capacity(),
            GLES20.GL_UNSIGNED_SHORT,
            mIndices
        )
        GLES20.glLineWidth(1.0f)
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
        private val TAG = Line::class.java.simpleName
        const val RED = 0
        const val GREEN = 1
        const val BLUE = 2
        const val ALPHA = 3
    }
}