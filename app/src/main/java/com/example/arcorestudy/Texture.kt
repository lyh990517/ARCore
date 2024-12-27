package com.example.arcorestudy

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import java.nio.IntBuffer

class Texture(private val bitmap: Bitmap) {

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
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, it)
        }

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)


        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
    }
}