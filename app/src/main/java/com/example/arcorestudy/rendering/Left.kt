package com.example.arcorestudy.rendering

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import com.example.arcorestudy.R
import com.example.arcorestudy.tools.DataVertex
import com.example.gllibrary.*
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.size
import glm_.vec3.Vec3
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Left(
    private val vShader: String,
    private val fShader: String,
    private val diffuse: Texture,
) {

    private var faceVertex: FloatBuffer? = null
    private var faceIndices: IntBuffer? = null
    private var facePos: Vec3? = null
    private var faceUVS: FloatBuffer? = null
    private var faceNormals: FloatBuffer? = null

    private lateinit var program: Program
    private var proj = Mat4()
    private var view = Mat4()
    private var vertexData: DataVertex? = null
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
        facePos?.let { vec3 ->
            GLES30.glBindVertexArray(vertexData!!.getVaoId())
            val model = glm.translate(Mat4(), vec3)
            program.setUniformMat4("mvp", proj * view * model)
            GLES20.glDrawElements(
                GLES30.GL_TRIANGLE_STRIP, faceIndices!!.size,
                GLES30.GL_UNSIGNED_INT,0)
            GLES30.glBindVertexArray(0)
        }
        facePos = null
    }

    fun setFace(
        vertex: FloatBuffer,
        indices: IntBuffer,
        pos: Vec3,
        uvs: FloatBuffer,
        normals: FloatBuffer
    ) {
        faceVertex = vertex
        faceIndices = indices
        facePos = pos
        faceUVS = uvs
        faceNormals = normals
        val buffer = createFloatBuffer(vertex.capacity() + uvs.capacity())
        vertex.position(0)
        uvs.position(0)
        while (vertex.hasRemaining()) {
            buffer.put(vertex.get())
            buffer.put(vertex.get())
            buffer.put(vertex.get())
            buffer.put(uvs.get())
            buffer.put(uvs.get())
        }
        vertexData = DataVertex(buffer, indices, 5).apply {
            addAttribute(program.getAttributeLocation("aPos"), 3, 0)
            addAttribute(program.getAttributeLocation("aTexCoord"),2,3)
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
        fun create(context: Context): Left {
            val resource = context.resources
            return Left(
                resource.readRawTextFile(R.raw.face_vertex),
                resource.readRawTextFile(R.raw.face_fragment),
                Texture(loadBitmap(context, R.raw.ear_fur)),
            )
        }
    }
}