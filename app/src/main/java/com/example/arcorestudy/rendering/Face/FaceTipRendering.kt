package com.example.arcorestudy.rendering.Face

import android.content.Context
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
import glm_.vec3.Vec3
import kotlin.math.acos

class FaceTipRendering(
    private val vShader: String,
    private val fShader: String,
    private val diffuse: Texture,
    private val mesh: Mesh
) {
    private var position: Vec3? = null
    private var pose: Pose? = null

    private lateinit var program: Program
    private var proj = Mat4()
    private var view = Mat4()
    private lateinit var renderingData: RenderingData
    private var isInitialized = false
    fun init() {
        isInitialized = true
        program = Program.create(vShader, fShader)
        diffuse.load()
        val buffer = createFloatBuffer(mesh.vertices.capacity() + mesh.texCoords.capacity())
        mesh.vertices.position(0)
        mesh.texCoords.position(0)
        while (mesh.vertices.hasRemaining()) {
            buffer.put(mesh.vertices.get())
            buffer.put(mesh.vertices.get())
            buffer.put(mesh.vertices.get())
            buffer.put(mesh.texCoords.get())
            buffer.put(1 - mesh.texCoords.get())
        }
        renderingData = RenderingData(buffer, mesh.indices, 5).apply {
            addAttribute(program.getAttributeLocation("aPos"), 3, 0)
            addAttribute(program.getAttributeLocation("aTexCoord"), 2, 3)
            bind()
        }
    }

    fun draw() {
        if (!isInitialized) init()
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        program.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, diffuse.getId())
        position?.let { position ->
            glBindVertexArray(renderingData.getVaoId())
            val rotationAngle = 2.0f * acos(pose!!.qw())
            val rotationVector = Vec3(pose!!.qx(), pose!!.qy(), pose!!.qz())
            val model =
                glm.translate(Mat4(), position) * glm.rotate(Mat4(), rotationAngle, rotationVector)
            program.setUniformMat4("mvp", proj * view * model)
            glDrawElements(GL_TRIANGLE_STRIP, mesh.vertices.size, GL_UNSIGNED_INT, 0)
            glBindVertexArray(0)
        }
        glDisable(GL_BLEND)
        position = null
    }


    fun setPose(
        pose: Pose
    ) {
        pose.let {
            this.pose = it
            position = Vec3(it.tx(), it.ty(), it.tz())
        }
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        proj = projMatrix.toMat4().transpose_()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        view = viewMatrix.toMat4().transpose_()
    }

    companion object {
        fun create(context: Context, mesh: Mesh, @RawRes texture: Int): FaceTipRendering {
            val resource = context.resources
            return FaceTipRendering(
                resource.readRawTextFile(R.raw.face_vertex),
                resource.readRawTextFile(R.raw.face_fragment),
                Texture(loadBitmap(context, texture)),
                mesh
            )
        }

    }
}