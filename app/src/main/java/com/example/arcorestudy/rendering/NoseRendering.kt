package com.example.arcorestudy.rendering

import android.content.Context
import android.opengl.GLES30
import android.util.Log
import com.example.arcorestudy.R
import com.example.arcorestudy.tools.Mesh
import com.example.gllibrary.*
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjUtils
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

class NoseRendering(
    private val mesh: Mesh,
    private val vShader: String,
    private val fShader: String,
    private val diffuse: Texture,
    private val specular: Texture
) {
    private var view = Mat4()
    private var proj = Mat4()
    private lateinit var program: Program
    private var nosePosition : Vec3? = null

    fun init() {
        program = Program.create(vShader, fShader)
        diffuse.load()
        specular.load()
        mesh.bind(program)
    }

    fun draw() {
        try {
            program.use()
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, diffuse.getId())
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, specular.getId())
            program.setInt("diffuse", 0)
            program.setInt("bump", 1)
            try {
                nosePosition?.let {
                    Log.e("draw","$it")
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

    fun updatePosition(vec3: Vec3){
        nosePosition = vec3
    }
    fun setProjectionMatrix(projMatrix: FloatArray) {
        proj = projMatrix.toMat4().transpose_()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        view = viewMatrix.toMat4().transpose_()
    }

    companion object {
        fun create(context: Context): NoseRendering {
            val resources = context.resources
            return NoseRendering(
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
                texCoords = ObjData.getTexCoords(obj, 2)
            )
        }
    }
}