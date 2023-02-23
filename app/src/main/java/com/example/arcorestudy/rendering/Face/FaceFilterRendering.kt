package com.example.arcorestudy.rendering.Face

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Log
import androidx.annotation.RawRes
import com.example.arcorestudy.R
import com.example.arcorestudy.tools.RenderingData
import com.example.arcorestudy.tools.RenderingDataShort
import com.example.gllibrary.*
import com.google.ar.core.Pose
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.size
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
    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f

    fun init() {
        program = Program.create(vShader, fShader)
        diffuse.load()
    }
    fun draw() {
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        program.use()
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, diffuse.getId())
        facePos?.let {
            GLES30.glBindVertexArray(vertexData!!.getVaoId())
            val rotationAngle = 2.0f * acos(pose!!.qw())
            val rotationVector = Vec3(pose!!.qx(), pose!!.qy(), pose!!.qz())
            val model =
                glm.translate(Mat4(), it) * glm.rotate(Mat4(), rotationAngle, rotationVector)
            program.setUniformMat4("mvp", proj * view * model)
            GLES20.glDrawElements(
                GLES30.GL_TRIANGLES, faceIndices?.size ?: 0,
                GLES30.GL_UNSIGNED_SHORT, 0
            )
            GLES30.glBindVertexArray(0)
        }
        facePos = null
    }

    fun setFace(
        vertex: FloatBuffer,
        indices: ShortBuffer,
        uvs: FloatBuffer,
        normals: FloatBuffer,
        pose: Pose
    ) {
        faceVertex = vertex
        faceIndices = indices
        faceUVS = uvs
        faceNormals = normals
        facePos = Vec3(pose.tx() + x, pose.ty() + y, pose.tz() + z)
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
        proj = projMatrix.toMat4().transpose_()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        view = viewMatrix.toMat4().transpose_()
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