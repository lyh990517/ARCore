package com.example.arcorestudy.rendering.Face

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import androidx.annotation.RawRes
import com.example.arcorestudy.R
import com.example.arcorestudy.tools.RenderingData
import com.example.arcorestudy.tools.RenderingDataShort
import com.example.arcorestudy.tools.toFloatArray
import com.example.gllibrary.*
import com.google.ar.core.Pose
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.size
import glm_.vec3.Vec3
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.acos

class FaceObjectRendering(
    private val vShader: String,
    private val fShader: String,
    private val diffuse: Texture,
    private val mesh: com.example.arcorestudy.tools.Mesh? = null
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
    private lateinit var renderingData: RenderingData
    private var pose: Pose? = null
    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f
    private var size: Float = 0f


    private var light: FloatArray = FloatArray(4)
    private val LIGHT_DIRECTION = floatArrayOf(0.250f, 0.866f, 0.433f, 0.0f)
    private val OBJECT_COLOR = floatArrayOf(139.0f, 195.0f, 74.0f, 255.0f)
    private var viewLightDirection = FloatArray(4)
    private var lightingParametersUniform = 0
    private var colorCorrectionParameterUniform = 0
    private var colorUniform = 0
    private var materialParametersUniform = 0
    private var textureUniform = 0
    private val ambient = 0.3f
    private val diffuser = 1.0f
    private val specular = 1.0f
    private val specularPower = 6.0f


    fun init() {
        program = Program.create(vShader, fShader)
        diffuse.load()
    }

    fun initMesh() {
        program = Program.create(vShader, fShader)
        lightingParametersUniform =
            GLES20.glGetUniformLocation(program.getProgram(), "u_LightingParameters")
        colorCorrectionParameterUniform =
            GLES20.glGetUniformLocation(program.getProgram(), "u_ColorCorrectionParameters")
        colorUniform = GLES20.glGetUniformLocation(program.getProgram(), "u_ObjColor")
        materialParametersUniform = GLES20.glGetUniformLocation(program.getProgram(), "u_MaterialParameters")
        textureUniform = GLES20.glGetUniformLocation(program.getProgram(), "texture1")
        diffuse.load()
        mesh?.let {
            Log.e("mesh","${mesh.normals.size}")
            val buffer = createFloatBuffer(mesh.vertices.capacity() + mesh.texCoords.capacity() + mesh.normals.capacity())
            mesh.vertices.position(0)
            mesh.texCoords.position(0)
            mesh.normals.position(0)
            while (mesh.vertices.hasRemaining()) {
                buffer.put(mesh.vertices.get())
                buffer.put(mesh.vertices.get())
                buffer.put(mesh.vertices.get())
                buffer.put(mesh.texCoords.get())
                buffer.put(1 - mesh.texCoords.get())
                buffer.put(mesh.normals.get())
                buffer.put(mesh.normals.get())
                buffer.put(mesh.normals.get())
            }
            renderingData = RenderingData(buffer, mesh.indices, 8).apply {
                addAttribute(0, 3, 0)
                addAttribute(1, 2, 3)
                addAttribute(2, 3, 5)
                bind()
            }
        }
    }

    fun drawMesh() {
        program.use()
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, diffuse.getId())
        GLES20.glUniform1i(textureUniform, 0)
        facePos?.let { position ->
            GLES30.glBindVertexArray(renderingData.getVaoId())
            val rotationAngle = 2.0f * acos(pose!!.qw())
            val rotationVector = Vec3(pose!!.qx(), pose!!.qy(), pose!!.qz())
            val model =
                glm.translate(Mat4(), position) * glm.rotate(
                    Mat4(),
                    rotationAngle,
                    rotationVector
                ) * glm.scale(Mat4(), Vec3(1.0f + size, 1.0f + size, 1.0f + size))
            Matrix.multiplyMV(
                viewLightDirection,
                0,
                (view * model).toFloatArray(),
                0,
                LIGHT_DIRECTION,
                0
            )
            normalizeVec3(viewLightDirection)
            GLES30.glUniform4f(
                lightingParametersUniform,
                viewLightDirection[0],
                viewLightDirection[1],
                viewLightDirection[2],
                1f
            )
            GLES30.glUniform4fv(colorCorrectionParameterUniform,1,light,0)
            GLES20.glUniform4fv(colorUniform, 1, OBJECT_COLOR, 0)

            // Set the object material properties.
            GLES20.glUniform4f(materialParametersUniform, ambient, diffuser, specular, specularPower)
            program.setUniformMat4("mvp", proj * view * model)
            GLES20.glDrawElements(
                GLES30.GL_TRIANGLES, mesh!!.vertices.size,
                GLES30.GL_UNSIGNED_INT, 0
            )
            GLES30.glBindVertexArray(0)
        }
        facePos = null
    }

    private fun normalizeVec3(v: FloatArray) {
        val reciprocalLength =
            1.0f / Math.sqrt((v[0] * v[0] + v[1] * v[1] + v[2] * v[2]).toDouble()).toFloat()
        v[0] *= reciprocalLength
        v[1] *= reciprocalLength
        v[2] *= reciprocalLength
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
            addAttribute(0, 3, 0)
            addAttribute(1, 2, 3)
            addAttribute(2, 3, 5)
            bind()
        }
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        proj = projMatrix.toMat4().transpose_()
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        view = viewMatrix.toMat4().transpose_()
    }

    fun getXYZ(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun setSize(size: Float) {
        this.size = size
    }

    fun setLight(light: FloatArray) {
        this.light = light
    }

    companion object {
        fun create(
            context: Context,
            mesh: com.example.arcorestudy.tools.Mesh,
            @RawRes texture: Int
        ): FaceObjectRendering {
            val resource = context.resources
            return FaceObjectRendering(
                resource.readRawTextFile(R.raw.face_vertex),
                resource.readRawTextFile(R.raw.face_fragment),
                Texture(loadBitmap(context, texture)),
                mesh
            )
        }
    }
}