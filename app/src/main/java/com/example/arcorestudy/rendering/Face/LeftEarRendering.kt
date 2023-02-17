package com.example.arcorestudy.rendering.Face

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import com.example.arcorestudy.R
import com.example.arcorestudy.tools.DataVertex
import com.example.arcorestudy.tools.Mesh
import com.example.gllibrary.*
import com.google.ar.core.Pose
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.size
import glm_.toDouble
import glm_.toFloat
import glm_.vec3.Vec3
import java.nio.FloatBuffer
import java.nio.IntBuffer

class LeftEarRendering(
    private val vShader: String,
    private val fShader: String,
    private val diffuse: Texture,
    private val leftEarMesh: Mesh
) {
    private var leftEarPosition: Vec3? = null
    private var pose: Pose? = null

    private lateinit var program: Program
    private var proj = Mat4()
    private var view = Mat4()
    private var vertexData: DataVertex? = null
    fun init() {
        program = Program.create(vShader, fShader)
        diffuse.load()
        val buffer = createFloatBuffer(leftEarMesh.vertices.capacity() + leftEarMesh.texCoords.capacity())
        leftEarMesh.vertices.position(0)
        leftEarMesh.texCoords.position(0)
        while (leftEarMesh.vertices.hasRemaining()) {
            buffer.put(leftEarMesh.vertices.get())
            buffer.put(leftEarMesh.vertices.get())
            buffer.put(leftEarMesh.vertices.get())
            buffer.put(leftEarMesh.texCoords.get())
            buffer.put(1 - leftEarMesh.texCoords.get())
        }
        vertexData = DataVertex(buffer, leftEarMesh.indices, 5).apply {
            addAttribute(program.getAttributeLocation("aPos"), 3, 0)
            addAttribute(program.getAttributeLocation("aTexCoord"), 2, 3)
            bind()
        }
    }

    fun draw() {
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        program.use()
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, diffuse.getId())
        leftEarPosition?.let { position ->
            GLES30.glBindVertexArray(vertexData!!.getVaoId())
            val model = glm.translate(Mat4(), position) *
                    glm.rotate(Mat4(), pose!!.qx() * glm.PIf, Vec3(1, 0, 0)) *
                    glm.rotate(Mat4(), pose!!.qy() * glm.PIf, Vec3(0, 1, 0)) *
                    glm.rotate(Mat4(), getAngle(), Vec3(0, 0, 1))
            program.setUniformMat4("mvp", proj * view * model)
            GLES20.glDrawElements(
                GLES30.GL_TRIANGLE_STRIP, leftEarMesh.vertices.size,
                GLES30.GL_UNSIGNED_INT, 0)
            GLES30.glBindVertexArray(0)
        }
        leftEarPosition = null
    }

    private fun getAngle() =
        if (pose!!.qz() <= 0) (2 * kotlin.math.cos(pose!!.qz().toDouble)).toFloat * glm.PIf else -(2 * kotlin.math.cos(
            pose!!.qz().toDouble
        )).toFloat * glm.PIf

    fun setLeftEarPose(
        pose: Pose
    ) {
        this.pose = pose
        leftEarPosition = Vec3(pose.tx(), pose.ty(), pose.tz())
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        proj = projMatrix.toMat4().transpose_()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        view = viewMatrix.toMat4().transpose_()
    }

    companion object {
        fun create(context: Context, mesh: Mesh): LeftEarRendering {
            val resource = context.resources
            return LeftEarRendering(
                resource.readRawTextFile(R.raw.face_vertex),
                resource.readRawTextFile(R.raw.face_fragment),
                Texture(loadBitmap(context, R.raw.ear_fur)),
                mesh
            )
        }

    }
}