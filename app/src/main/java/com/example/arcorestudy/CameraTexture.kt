package com.example.arcorestudy

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES30
import android.opengl.GLUtils
import java.nio.IntBuffer

class CameraTexture(private val bitmap: Bitmap, private val internalFormat: Int? = null) {

    private var id: Int? = null

    fun getId() = id ?: throw IllegalStateException("Call load() before using texture")

    fun load() {
        if (id != null) {
            return
        }

        val id = IntBuffer.allocate(1)
        GLES30.glGenTextures(1, id)

        id[0].also {
            this.id = it
            GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, it)
        }

        GLES30.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST)

        internalFormat?.also {
            GLUtils.texImage2D(GL_TEXTURE_EXTERNAL_OES, 0, it, bitmap, 0)
        } ?: run {
            GLUtils.texImage2D(GL_TEXTURE_EXTERNAL_OES, 0, bitmap, 0)
        }

        GLES30.glGenerateMipmap(GL_TEXTURE_EXTERNAL_OES)
    }
}