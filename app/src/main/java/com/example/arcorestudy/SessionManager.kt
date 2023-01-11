package com.example.arcorestudy

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import androidx.annotation.RequiresApi
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import java.lang.UnsupportedOperationException

class SessionManager(private val context: Context) {
    var mSession: Session? = null
    private var mConfig: Config? = null
    private var mUserRequestedInstall = true
    var isViewportChanged = false
    val displayListener = object :
        DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            synchronized(this) { isViewportChanged = true }
        }

        override fun onDisplayRemoved(displayId: Int) {}
    }
    val mCamera: CameraPreview?

    init {
        mCamera = CameraPreview()
    }

    fun resume(activity: Activity) {
        try {
            if (mSession == null) {
                when (ArCoreApk.getInstance().requestInstall(activity, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        mSession = Session(context)
                    }
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        mUserRequestedInstall = false
                    }
                }
            }
        } catch (_: UnsupportedOperationException) {

        }
        mConfig = Config(mSession)
        if (!mSession!!.isSupported(mConfig)) {

        }
        mSession!!.configure(mConfig)
        mSession!!.resume()
    }
}