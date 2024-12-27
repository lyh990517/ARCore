package com.example.arcorestudy.rendering

import android.content.Context
import android.opengl.GLES30.GL_ARRAY_BUFFER
import android.opengl.GLES30.GL_DYNAMIC_DRAW
import android.opengl.GLES30.GL_FLOAT
import android.opengl.GLES30.GL_POINTS
import android.opengl.GLES30.glBindBuffer
import android.opengl.GLES30.glBufferData
import android.opengl.GLES30.glBufferSubData
import android.opengl.GLES30.glDisableVertexAttribArray
import android.opengl.GLES30.glDrawArrays
import android.opengl.GLES30.glEnableVertexAttribArray
import android.opengl.GLES30.glGenBuffers
import android.opengl.GLES30.glUniform1f
import android.opengl.GLES30.glUniform4f
import android.opengl.GLES30.glVertexAttribPointer
import com.example.arcorestudy.Program
import com.example.arcorestudy.R
import com.example.arcorestudy.readRawTextFile
import com.example.arcorestudy.toMat4
import com.google.ar.core.PointCloud
import glm_.mat4x4.Mat4
import java.nio.IntBuffer

class PointCloudRendering(
) {
    private var mNumPoints = 0
    private var mPointCloud: PointCloud? = null
    private var vboId: Int = -1


    companion object {

        fun create(context: Context): PointCloudRendering {
            val resource = context.resources
            return PointCloudRendering(
            )
        }
    }
}