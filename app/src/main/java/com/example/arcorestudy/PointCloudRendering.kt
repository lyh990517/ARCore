package com.example.arcorestudy

import android.content.Context
import android.opengl.GLES20
import com.example.gllibrary.Scene
import com.example.gllibrary.VertexData
import com.example.gllibrary.readRawTextFile
import glm_.mat4x4.Mat4
import java.nio.FloatBuffer
import android.opengl.GLES30.*
import android.opengl.Matrix
import android.util.Log
import com.example.gllibrary.Program
import com.google.ar.core.PointCloud
import glm_.size

class PointCloudRendering(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String
) : Scene() {
    private lateinit var mVbo: IntArray
    private lateinit var program: Program
    private val mViewMatrix = FloatArray(16)
    private val mProjMatrix = FloatArray(16)
    private var mNumPoints = 0
    private var mPointCloud: PointCloud? = null
    private val vertexShaderString = """uniform mat4 uMvpMatrix;
uniform vec4 uColor;
uniform float uPointSize;
attribute vec4 aPosition;
varying vec4 vColor;
void main() {
   vColor = uColor;
   gl_Position = uMvpMatrix * vec4(aPosition.xyz, 1.0);
   gl_PointSize = uPointSize;
}"""
    private val fragmentShaderString = """precision mediump float;
varying vec4 vColor;
void main() {
    gl_FragColor = vColor;
}"""

    fun update(pointCloud: PointCloud?) {
        mPointCloud = pointCloud
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0])
        mNumPoints = mPointCloud!!.points.remaining() / 4
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, mNumPoints * 16, mPointCloud!!.points)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    override fun draw() {
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0, mViewMatrix, 0)
        program.use()
        val position = program.getAttributeLocation("aPosition")
        val color = program.getUniformLocation( "uColor")
        val mvp = program.getUniformLocation("uMvpMatrix")
        val size = program.getUniformLocation("uPointSize")
        GLES20.glEnableVertexAttribArray(position)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0])
        GLES20.glVertexAttribPointer(position, 4, GLES20.GL_FLOAT, false, 16, 0)
        GLES20.glUniform4f(color, 31.0f / 255.0f, 188.0f / 255.0f, 210.0f / 255.0f, 1.0f)
        GLES20.glUniformMatrix4fv(mvp, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(size, 5.0f)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mNumPoints)
        GLES20.glDisableVertexAttribArray(position)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    override fun init(width: Int, height: Int) {
        mVbo = IntArray(1)
        GLES20.glGenBuffers(1, mVbo, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 1000 * 16, null, GLES20.GL_DYNAMIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        program = Program.create(vertexShaderString, fragmentShaderString)
    }

    fun setProjectionMatrix(projMatrix: FloatArray?) {
        System.arraycopy(projMatrix, 0, mProjMatrix, 0, 16)
    }

    fun setViewMatrix(viewMatrix: FloatArray?) {
        System.arraycopy(viewMatrix, 0, mViewMatrix, 0, 16)
    }

    companion object {

        fun create(context: Context): PointCloudRendering {
            val resource = context.resources
            return PointCloudRendering(
                resource.readRawTextFile(R.raw.pointcloud_vertex),
                resource.readRawTextFile(R.raw.pointcloud_fragment)
            )
        }
    }
}