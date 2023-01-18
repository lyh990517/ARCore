package com.example.arcorestudy

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
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
    var model = glm.translate(Mat4(), Vec3(0,0,-2)) * glm.scale(Mat4(),Vec3(0.2f,0.2f,0.2f))
    private lateinit var program: Program

    fun init() {
        program = Program.create(vShader, fShader)
        mesh.data.bind()
        mesh.data.addAttribute(program.getAttributeLocation("aPos"), 3, 0)
        mesh.data.addAttribute(program.getAttributeLocation("aTexCoord"), 2, 3)
        mesh.bindIndices()
    }

    fun draw() {
        diffuse.load()
        specular.load()
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, diffuse.getId())
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, specular.getId())
        program.setUniformMat4("mvp", model)
        program.setInt("diffuse", 0)
        program.setInt("bump", 1)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mesh.data.getVBO())
        mesh.data.applyAttributes()
        GLES20.glDrawElements(GLES30.GL_TRIANGLES, mesh.indices.capacity(), GLES30.GL_UNSIGNED_INT, 0)
        mesh.data.disabledAttributes()
    }

    companion object {
        fun create(context: Context): ArObjectRendering {
            val resources = context.resources
            return ArObjectRendering(
                fromAssets(context, "backpack.obj"),
                resources.readRawTextFile(R.raw.asset_vertex),
                resources.readRawTextFile(R.raw.asset_fragment),
                Texture(loadBitmap(context, R.raw.diffuse)),
                Texture(loadBitmap(context, R.raw.specular))
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