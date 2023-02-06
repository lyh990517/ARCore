package com.example.arcorestudy.rendering

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30.*
import android.opengl.GLES30
import android.util.Log
import com.example.arcorestudy.R
import com.example.arcorestudy.tools.DataVertex
import com.example.arcorestudy.tools.FaceMesh
import com.example.arcorestudy.tools.Mesh
import com.example.arcorestudy.tools.VBOData
import com.example.gllibrary.*
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.size
import glm_.vec3.Vec3
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class FaceRendering(
    private val vShader: String,
    private val fShader: String,
) {

    private var faceVertex: FloatBuffer? = null
    private var faceIndices: ShortBuffer? = null
    private var facePos: Vec3? = null
    private var faceUVS: FloatBuffer? = null
    private var faceNormals: FloatBuffer? = null

    private var facePosition = mutableListOf<Vec3>()
    private lateinit var program: Program
    private var proj = Mat4()
    private var view = Mat4()
    private var vertexData: DataVertex? = null
    fun init() {
        program = Program.create(vShader, fShader)
    }

    fun draw() {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        program.use()
        facePos?.let {
            vertexData?.let {
                glBindVertexArray(it.getVaoId())
            }
            val model = glm.translate(Mat4(), it)
            program.setUniformMat4("mvp", proj * view * model)
            GLES20.glDrawElements(GL_TRIANGLE_STRIP,faceIndices?.size ?: 0, GL_UNSIGNED_SHORT,0)
            glBindVertexArray(0)
        }
        facePos = null
    }

    fun setFace(
        vertex: FloatBuffer,
        indices: ShortBuffer,
        pos: Vec3,
        uvs: FloatBuffer,
        normals: FloatBuffer
    ) {
        faceVertex = vertex
        faceIndices = indices
        facePos = pos
        faceUVS = uvs
        faceNormals = normals
        vertexData = DataVertex(vertex,indices,3)
        vertexData?.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        vertexData?.bind()
        Log.e("face", "${facePos}")
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        proj = projMatrix.toMat4().transpose_()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        view = viewMatrix.toMat4().transpose_()
    }

    companion object {
        fun create(context: Context): FaceRendering {
            val resource = context.resources
            return FaceRendering(
                resource.readRawTextFile(R.raw.face_vertex),
                resource.readRawTextFile(R.raw.face_fragment)
            )
        }
    }
}