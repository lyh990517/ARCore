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

    private var faceVertex: MutableList<FloatBuffer> = mutableListOf()
    private var faceIndices: MutableList<ShortBuffer> = mutableListOf()
    private var facePos: MutableList<Vec3> = mutableListOf()
    private var faceUVS: MutableList<FloatBuffer> = mutableListOf()
    private var faceNormals: MutableList<FloatBuffer> = mutableListOf()

    private lateinit var program: Program
    private var proj = Mat4()
    private var view = Mat4()
    private var vertexData: MutableList<DataVertex> = mutableListOf()
    fun init() {
        program = Program.create(vShader, fShader)
    }

    fun draw() {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        program.use()
        facePos.forEachIndexed { index, vec3 ->
            glBindVertexArray(vertexData[index].getVaoId())
            val model = glm.translate(Mat4(), vec3)
            program.setUniformMat4("mvp", proj * view * model)
            GLES20.glDrawElements(GL_TRIANGLE_STRIP,faceIndices[index].size, GL_UNSIGNED_SHORT,0)
            glBindVertexArray(0)
        }
        facePos.clear()
    }

    fun setFace(
        vertex: FloatBuffer,
        indices: ShortBuffer,
        pos: Vec3,
        uvs: FloatBuffer,
        normals: FloatBuffer
    ) {

        faceVertex.add(vertex)
        faceIndices.add(indices)
        facePos.add(pos)
        faceUVS.add(uvs)
        faceNormals.add(normals)
        vertexData.add(DataVertex(vertex,indices,3).apply {
            addAttribute(program.getAttributeLocation("aPos"), 3, 0)
            bind()
        })
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