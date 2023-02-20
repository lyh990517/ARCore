package com.example.arcorestudy.rendering.Face

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30.*
import androidx.annotation.RawRes
import com.example.arcorestudy.R
import com.example.arcorestudy.tools.*
import com.example.arcorestudy.tools.Mesh
import com.example.gllibrary.*
import com.google.ar.core.Pose
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.size
import glm_.toDouble
import glm_.toFloat
import glm_.vec3.Vec3

class FaceRendering(
    private val vShader: String,
    private val fShader: String,
    private val diffuse: Texture,
    private val noseMesh: Mesh
) {
    private var nosePosition: Vec3? = null
    private var pose: Pose? = null

    private lateinit var program: Program
    private var proj = Mat4()
    private var view = Mat4()
    private var vertexData: DataVertex? = null
    fun init() {
        program = Program.create(vShader, fShader)
        diffuse.load()
        val buffer = createFloatBuffer(noseMesh.vertices.capacity() + noseMesh.texCoords.capacity())
        noseMesh.vertices.position(0)
        noseMesh.texCoords.position(0)
        while (noseMesh.vertices.hasRemaining()) {
            buffer.put(noseMesh.vertices.get())
            buffer.put(noseMesh.vertices.get())
            buffer.put(noseMesh.vertices.get())
            buffer.put(noseMesh.texCoords.get())
            buffer.put(1 - noseMesh.texCoords.get())
        }
        vertexData = DataVertex(buffer, noseMesh.indices, 5).apply {
            addAttribute(program.getAttributeLocation("aPos"), 3, 0)
            addAttribute(program.getAttributeLocation("aTexCoord"), 2, 3)
            bind()
        }
    }

    fun draw() {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        program.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, diffuse.getId())
        nosePosition?.let { position ->
            glBindVertexArray(vertexData!!.getVaoId())
            val model = glm.translate(Mat4(), position) *
                    glm.rotate(Mat4(), pose!!.qx() * glm.PIf, Vec3(1, 0, 0)) *
                    glm.rotate(Mat4(), pose!!.qy() * glm.PIf, Vec3(0, 1, 0)) *
                    glm.rotate(Mat4(), getAngle(), Vec3(0, 0, 1))
            program.setUniformMat4("mvp", proj * view * model)
            GLES20.glDrawElements(GL_TRIANGLE_STRIP, noseMesh.vertices.size, GL_UNSIGNED_INT, 0)
            glBindVertexArray(0)
        }
        nosePosition = null
    }

    private fun getAngle() =
        if (pose!!.qz() <= 0) (2 * kotlin.math.cos(pose!!.qz().toDouble)).toFloat * glm.PIf else -(2 * kotlin.math.cos(
            pose!!.qz().toDouble
        )).toFloat * glm.PIf

    fun setPose(
        pose: Pose
    ) {
        this.pose = pose
        nosePosition = Vec3(pose.tx(), pose.ty(), pose.tz())
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        proj = projMatrix.toMat4().transpose_()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        view = viewMatrix.toMat4().transpose_()
    }

    companion object {
        fun create(context: Context, mesh: Mesh, @RawRes texture: Int): FaceRendering {
            val resource = context.resources
            return FaceRendering(
                resource.readRawTextFile(R.raw.face_vertex),
                resource.readRawTextFile(R.raw.face_fragment),
                Texture(loadBitmap(context, texture)),
                mesh
            )
        }

    }
}