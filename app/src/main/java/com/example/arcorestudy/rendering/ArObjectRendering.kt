package com.example.arcorestudy.rendering

import android.content.Context
import android.opengl.GLES30.*
import android.util.Log
import com.example.arcorestudy.R
import com.example.gllibrary.*
import glm_.glm
import glm_.mat4x4.Mat4
import com.example.arcorestudy.tools.Mesh
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjUtils
import glm_.vec3.Vec3

class ArObjectRendering(
    private val mesh: Mesh,
    private val vShader: String,
    private val fShader: String,
    private val diffuse: Texture,
    private val specular: Texture
) {
    private var view = Mat4()
    private var proj = Mat4()
    private lateinit var program: Program
    private var objPosition = mutableListOf<Vec3>()

    fun init() {
        program = Program.create(vShader, fShader)
        diffuse.load()
        specular.load()
        mesh.bind(program)
    }

    fun draw() {
        try {
            program.use()
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, diffuse.getId())
            glActiveTexture(GL_TEXTURE1)
            glBindTexture(GL_TEXTURE_2D, specular.getId())
            glUniform1i(glGetUniformLocation(program.getProgram(),"diffuse"),0)
            glUniform1i(glGetUniformLocation(program.getProgram(),"bump"),1)
            try {
                objPosition.forEach {
                    val model =
                        glm.translate(Mat4(), it)
                    program.setUniformMat4("mvp", proj * view * model)
                    mesh.draw()
                }
            } catch (e: Exception) {

            }
        } finally {
            //nothing
        }
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        proj = projMatrix.toMat4().transpose_()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        view = viewMatrix.toMat4().transpose_()
    }

    fun addPosition(vec3: Vec3) {
        objPosition.add(vec3)
    }

    fun clear() {
        objPosition.clear()
    }

    companion object {
        fun create(context: Context): ArObjectRendering {
            val resources = context.resources
            return ArObjectRendering(
                fromAssets(context, "NOSE.obj"),
                resources.readRawTextFile(R.raw.asset_vertex),
                resources.readRawTextFile(R.raw.asset_fragment),
                Texture(loadBitmap(context, R.raw.nose_fur)),
                Texture(loadBitmap(context, R.raw.nose_fur))
            )
        }

        private fun fromAssets(context: Context, assetPath: String): Mesh {
            val obj = context.assets.open(assetPath)
                .let { stream -> ObjReader.read(stream) }
                .let { objStream -> ObjUtils.convertToRenderable(objStream) }
            return Mesh(
                indices = ObjData.getFaceVertexIndices(obj),
                vertices = ObjData.getVertices(obj),
                normals = ObjData.getNormals(obj),
                texCoords = ObjData.getTexCoords(obj,2)
            )
        }
    }
}