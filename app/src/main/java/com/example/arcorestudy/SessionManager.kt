package com.example.arcorestudy

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.view.Display
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import com.example.arcorestudy.tools.DepthTexture
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import java.lang.UnsupportedOperationException

class SessionManager(private val context: Context) {
    var mSession: Session? = null
    private var mConfig: Config? = null
    var isViewportChanged = false
    private val displayListener = object :
        DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            synchronized(this) {
                isViewportChanged = true
            }
        }

        override fun onDisplayRemoved(displayId: Int) {}
    }

    val mCamera: CameraTextureRendering = CameraTextureRendering.create(context)
    val mPointCloud: PointCloudRendering = PointCloudRendering.create(context)
    val cubeScene: CubeRendering = CubeRendering.create(context)
    val arObjectScene: ArObjectRendering = ArObjectRendering.create(context)
    val depthTexture = DepthTexture()
    fun create() {
        getSystemService(context, DisplayManager::class.java)!!.registerDisplayListener(
            displayListener,
            null
        )
    }

    fun resume() {
        try {
            if (isSupported()) {
                Log.e("session", "support device")
                mSession = Session(context)
                mConfig = Config(mSession)
                if (isDepthSupported()) {
                    mConfig?.depthMode = Config.DepthMode.AUTOMATIC
                    Log.e("session", "support DepthMode")
                }
                mSession!!.configure(mConfig)
                mSession!!.resume()
            } else {
                Log.e("session", "install arcore")
            }
        } catch (_: UnsupportedOperationException) {

        }
    }

    fun destroy() {
        getSystemService(context, DisplayManager::class.java)!!.unregisterDisplayListener(
            displayListener
        )
        mSession?.close()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun updateSession(width: Int, height: Int) {
        if (isViewportChanged) {
            val display =
                context.getSystemService(DisplayManager::class.java).displays[Display.DEFAULT_DISPLAY]
            mSession?.setDisplayGeometry(display.rotation, width, height)
            isViewportChanged = false
        }
    }

    fun isSupported() = ArCoreApk.getInstance()
        .checkAvailability(context) == ArCoreApk.Availability.SUPPORTED_INSTALLED
    fun isDepthSupported() = mSession!!.isDepthModeSupported(Config.DepthMode.AUTOMATIC)
    companion object {
        fun create(context: Context) = SessionManager(context)
    }
}