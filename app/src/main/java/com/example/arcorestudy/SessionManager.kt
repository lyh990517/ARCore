package com.example.arcorestudy

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.view.Display
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
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
                Log.e("session", "rotate!!")
            }
        }

        override fun onDisplayRemoved(displayId: Int) {}
    }
    val mCamera: CameraTextureRendering?
    val mPointCloud: PointCloudRendering?
    val cubeScene: CubeScene?

    init {
        mCamera = CameraTextureRendering.create(context)
        mPointCloud = PointCloudRendering.create(context)
        cubeScene = CubeScene.create(context)
    }

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
            } else {
                Log.e("session", "install arcore")
            }
        } catch (_: UnsupportedOperationException) {

        }
        mConfig = Config(mSession)
        mSession!!.configure(mConfig)
        mSession!!.resume()
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

    companion object {
        fun create(context: Context) = SessionManager(context)
    }
}