package com.example.arcorestudy

import android.content.Context
import android.opengl.GLES30.*
import com.example.gllibrary.*
import com.google.ar.core.PointCloud
import glm_.glm
import glm_.mat4x4.Mat4
import java.nio.IntBuffer

class PointCloudRendering(
    private val vertexShaderCode: String,
    private val fragmentShaderCode: String
) : Scene() {
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

    override fun draw() {
        program.use()
        val position = program.getAttributeLocation("aPosition")
        val color = program.getUniformLocation("uColor")
        val size = program.getUniformLocation("uPointSize")
        glEnableVertexAttribArray(position)
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        program.setUniformMat4("proj", mProjMatrix)
        program.setUniformMat4("view", mViewMatrix)
        glVertexAttribPointer(position, 4, GL_FLOAT, false, 16, 0)
        val red = glm.sin(timer.sinceStartSecs() * 0.5).toFloat()
        val green = glm.sin(timer.sinceStartSecs() * 0.2).toFloat()
        val blue = glm.sin(timer.sinceStartSecs() * 0.3).toFloat()
        glUniform4f(color, red, green, blue, 1f)
        glUniform1f(size, 5f)
        glDrawArrays(GL_POINTS, 0, mNumPoints)
        glDisableVertexAttribArray(position)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    override fun init(width: Int, height: Int) {
        bindOnlyVBO()
        program = Program.create(vertexShaderCode, fragmentShaderCode)
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        mProjMatrix = projMatrix.toMat4().transpose_()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        mViewMatrix = viewMatrix.toMat4().transpose_()
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