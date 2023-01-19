package com.example.arcorestudy.tools

import android.opengl.GLES30.*
import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.exceptions.NotYetAvailableException
import java.nio.IntBuffer

class DepthTexture {
    private var depthTextureId = -1
    private var depthTextureWidth = -1
    private var depthTextureHeight = -1

    fun createTexture() {
        val textureId = IntArray(1)
        glGenTextures(1, textureId, 0)
        depthTextureId = textureId[0]
        glBindTexture(GL_TEXTURE_2D, depthTextureId)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    }

    fun update(frame: Frame) {
        try {
            val depthImage = frame.acquireDepthImage16Bits()
            depthTextureWidth = depthImage.width
            depthTextureHeight = depthImage.height
            glBindTexture(GL_TEXTURE_2D, depthTextureId)
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RG8,
                depthTextureWidth,
                depthTextureHeight,
                0,
                GL_RG,
                GL_UNSIGNED_BYTE,
                depthImage.planes[0].buffer
            )
            depthImage.close()
        } catch (e: NotYetAvailableException) {
            Log.e("error","${e.message}")
        }
    }

    fun getDepthTexture() = depthTextureId
    fun getDepthWidth() = depthTextureWidth
    fun getDepthHeight() = depthTextureHeight
}