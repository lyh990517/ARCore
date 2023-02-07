package com.example.arcorestudy

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.view.Display
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import com.example.arcorestudy.rendering.*
import com.google.ar.core.*
import com.google.ar.core.Session.Feature
import com.google.ar.core.exceptions.UnsupportedConfigurationException
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
    val faceRendering: FaceRendering = FaceRendering.create(context)
    val noseRendering: NoseRendering = NoseRendering.create(context)

    fun create() {
        getSystemService(context, DisplayManager::class.java)!!.registerDisplayListener(
            displayListener,
            null
        )
    }

    fun resume(feature: Set<Feature>) {
        try {
            if (isSupported()) {
                Log.e("session", "support device")
                if (feature.isEmpty()) {
                    Log.e("session", "feature empty")
                    mSession = Session(context)
                } else {
                    Log.e("session", "set feature")
                    mSession = Session(context, feature)
                }
                mConfig = Config(mSession)
                if (mSession!!.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    mConfig?.depthMode = Config.DepthMode.AUTOMATIC
                    Log.e("session", "support DepthMode")
                }
                mSession!!.configure(mConfig)
                if (feature.isNotEmpty()) {
                    mSession!!.configure(config(mSession!!))
                }
                mSession!!.resume()
            } else {
                Log.e("session", "install arcore")
            }
        } catch (_: UnsupportedOperationException) {

        }
    }

    fun config(session: Session): Config = Config(session).apply {
        try {
            updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
            focusMode = Config.FocusMode.AUTO
        } catch (e: UnsupportedConfigurationException) {

        }
    }

    fun destroy() {
        getSystemService(context, DisplayManager::class.java)!!.unregisterDisplayListener(
            displayListener
        )
        mSession?.close()
    }

    fun pause() {
        mSession?.pause()
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