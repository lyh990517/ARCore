package com.example.arcorestudy.rendering

import android.content.Context
import android.opengl.GLES30
import com.example.arcorestudy.R
import com.example.arcorestudy.tools.FaceMesh
import com.example.arcorestudy.tools.Mesh
import com.example.gllibrary.*
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class FaceRendering(
    private val vShader: String,
    private val fShader: String,
    private val diffuse: Texture,
    private val specular: Texture
) {

    private var faceVertex: FloatBuffer? = null
    private var faceIndices: ShortBuffer? = null
    private var facePos: Vec3? = null
    private var faceUVS: FloatBuffer? = null
    private var faceNormals: FloatBuffer? = null
    private var faceMesh: FaceMesh? = null

    private var facePosition = mutableListOf<Vec3>()
    private val program = Program.create(vShader, fShader)
    private var proj = Mat4()
    private var view = Mat4()
    fun init() {
    }

    fun draw() {
        diffuse.load()
        specular.load()
        program.use()
        faceMesh?.bind(program)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, diffuse.getId())
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, specular.getId())
        facePos?.let {
            val model =
                glm.translate(Mat4(), it) * glm.scale(Mat4(), Vec3(0.05, 0.05, 0.05))
            program.setUniformMat4("mvp", proj * view * model)
            faceMesh!!.draw()
        }
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
        faceMesh = FaceMesh(faceVertex!!, faceNormals!!, faceUVS!!, faceIndices!!)
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
                resource.readRawTextFile(R.raw.asset_vertex),
                resource.readRawTextFile(R.raw.asset_fragment),
                Texture(loadBitmap(context, R.raw.diffuse)),
                Texture(loadBitmap(context, R.raw.specular))
            )
        }
    }
}