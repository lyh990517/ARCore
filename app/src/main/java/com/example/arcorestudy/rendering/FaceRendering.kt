package com.example.arcorestudy.rendering

import android.content.Context
import android.opengl.GLES30.*
import android.opengl.GLES30
import android.util.Log
import com.example.arcorestudy.R
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
    private var data: com.example.arcorestudy.tools.VBOData? = null
    private var proj = Mat4()
    private var view = Mat4()
    fun init() {
        program = Program.create(vShader, fShader)
    }

    fun draw() {
        data?.bind()
        data?.addAttribute(program.getAttributeLocation("aPos"),3,0)
        data?.draw()
        program.use()
        facePos?.let {
            val model = glm.translate(Mat4(), it)
            program.setUniformMat4("mvp",proj * view * model)
            glDrawArrays(GL_TRIANGLE_STRIP,0,500)
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
        data = VBOData(vertex,GL_STATIC_DRAW,5)
        Log.e("face", "${facePos}")
    }

    fun updatePosition(vec3: Vec3) {
        facePosition.add(vec3)
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