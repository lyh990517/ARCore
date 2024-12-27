package com.example.arcorestudy.rendering.Face

import android.content.Context
import android.opengl.GLES30.*
import androidx.annotation.RawRes
import com.example.arcorestudy.Program
import com.example.arcorestudy.R
import com.example.arcorestudy.Texture
import com.example.arcorestudy.createFloatBuffer
import com.example.arcorestudy.loadBitmap
import com.example.arcorestudy.readRawTextFile
import com.example.arcorestudy.toMat4
import com.example.arcorestudy.tools.RenderingDataShort
import com.google.ar.core.Pose
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.acos

class FaceFilterRendering (
    private val vShader: String,
    private val fShader: String,
    private val diffuse: Texture,
) {

    private var faceVertex: FloatBuffer? = null
    private var faceIndices: ShortBuffer? = null
    private var facePos: Vec3? = null
    private var faceUVS: FloatBuffer? = null
    private var faceNormals: FloatBuffer? = null

    private lateinit var program: Program
    private var proj = Mat4()
    private var view = Mat4()
    private var vertexData: RenderingDataShort? = null
    private var pose: Pose? = null
    private var isInitialized = false

    fun init() {
        isInitialized = true
        program = Program.create(vShader, fShader)
        diffuse.load()
    }
    fun draw() {
        if(!isInitialized) init()
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        program.use()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, diffuse.getId())
        facePos?.let {
            glBindVertexArray(vertexData!!.getVaoId())
            val rotationAngle = 2.0f * acos(pose!!.qw())
            val rotationVector = Vec3(pose!!.qx(), pose!!.qy(), pose!!.qz())
            val model =
                glm.translate(Mat4(), it) * glm.rotate(Mat4(), rotationAngle, rotationVector)
            program.setUniformMat4("mvp", proj * view * model)
//            glDrawElements(
//                GL_TRIANGLES, faceIndices?.size ?: 0,
//                GL_UNSIGNED_SHORT, 0
//            )
            glBindVertexArray(0)
        }
        glDisable(GL_BLEND)
        facePos = null
    }

    fun setFace(
        vertex: FloatBuffer,
        indices: ShortBuffer,
        uvs: FloatBuffer,
        normals: FloatBuffer,
        pose: Pose
    ) {
        if(!isInitialized) init()
        faceVertex = vertex
        faceIndices = indices
        faceUVS = uvs
        faceNormals = normals
        facePos = Vec3(pose.tx(), pose.ty(), pose.tz())
        this.pose = pose
        val buffer = createFloatBuffer(vertex.capacity() + uvs.capacity())
        vertex.position(0)
        uvs.position(0)
        while (vertex.hasRemaining()) {
            buffer.put(vertex.get())
            buffer.put(vertex.get())
            buffer.put(vertex.get())
            buffer.put(uvs.get())
            buffer.put(1 - uvs.get())
        }
        vertexData = RenderingDataShort(buffer, indices, 5).apply {
            addAttribute(program.getAttributeLocation("aPos"), 3, 0)
            addAttribute(program.getAttributeLocation("aTexCoord"), 2, 3)
            bind()
        }
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        proj = projMatrix.toMat4().transpose()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        view = viewMatrix.toMat4().transpose()
    }


    companion object {
        fun create(context: Context, @RawRes texture: Int): FaceFilterRendering {
            val resource = context.resources
            return FaceFilterRendering(
                resource.readRawTextFile(R.raw.face_vertex),
                resource.readRawTextFile(R.raw.face_fragment),
                Texture(loadBitmap(context, texture))
            )
        }
    }
}