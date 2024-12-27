package com.example.arcorestudy.rendering

import android.content.Context
import android.opengl.GLES30.GL_ARRAY_BUFFER
import android.opengl.GLES30.GL_DYNAMIC_DRAW
import android.opengl.GLES30.GL_FLOAT
import android.opengl.GLES30.GL_POINTS
import android.opengl.GLES30.glBindBuffer
import android.opengl.GLES30.glBufferData
import android.opengl.GLES30.glBufferSubData
import android.opengl.GLES30.glDisableVertexAttribArray
import android.opengl.GLES30.glDrawArrays
import android.opengl.GLES30.glEnableVertexAttribArray
import android.opengl.GLES30.glGenBuffers
import android.opengl.GLES30.glUniform1f
import android.opengl.GLES30.glUniform4f
import android.opengl.GLES30.glVertexAttribPointer
import com.example.arcorestudy.Program
import com.example.arcorestudy.R
import com.example.arcorestudy.readRawTextFile
import com.example.arcorestudy.toMat4
import com.google.ar.core.PointCloud
import glm_.mat4x4.Mat4
import java.nio.IntBuffer

class PointCloudRendering(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String
) {
    private lateinit var program: Program
    private var mViewMatrix: Mat4 = Mat4()
    private var mProjMatrix: Mat4 = Mat4()
    private var mNumPoints = 0
    private var mPointCloud: PointCloud? = null
    private var vboId: Int = -1


    fun update(pointCloud: PointCloud?) {
        mPointCloud = pointCloud
        mNumPoints = mPointCloud!!.points.remaining() / 4
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        glBufferSubData(GL_ARRAY_BUFFER, 0, mNumPoints * 16, mPointCloud!!.points)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    fun draw() {
        program.use()
        val position = program.getAttributeLocation("aPosition")
        val color = program.getUniformLocation("uColor")
        val size = program.getUniformLocation("uPointSize")
        glEnableVertexAttribArray(position)
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        program.setUniformMat4("proj", mProjMatrix)
        program.setUniformMat4("view", mViewMatrix)
        glVertexAttribPointer(position, 4, GL_FLOAT, false, 16, 0)
        glUniform4f(color, 1f, 0f, 0.5f, 1f)
        glUniform1f(size, 5f)
        glDrawArrays(GL_POINTS, 0, mNumPoints)
        glDisableVertexAttribArray(position)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    fun init() {
        program = Program.create(vertexShaderCode, fragmentShaderCode)
        bindOnlyVBO()
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        mProjMatrix = projMatrix.toMat4().transpose()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        mViewMatrix = viewMatrix.toMat4().transpose()
    }

    private fun bindOnlyVBO() {
        val vbo = IntBuffer.allocate(1)
        glGenBuffers(1, vbo)
        vbo[0].let {
            vboId = it
            glBindBuffer(GL_ARRAY_BUFFER, it)
        }
        glBufferData(GL_ARRAY_BUFFER, 1000 * 16, null, GL_DYNAMIC_DRAW)
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