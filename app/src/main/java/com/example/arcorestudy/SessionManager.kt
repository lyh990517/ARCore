package com.example.arcorestudy

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.Display
import androidx.annotation.RequiresApi
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import java.lang.UnsupportedOperationException

class SessionManager(private val context: Context,private val activity: Activity) {
    var mSession: Session? = null
    private var mConfig: Config? = null
    private var mUserRequestedInstall = true
    var isViewportChanged = false

    val mCamera: CameraPreview?

    init {
        mCamera = CameraPreview()
    }

    fun resume(){
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
    @RequiresApi(Build.VERSION_CODES.R)
    fun updateSession(displayRotation: Int, width: Int, height: Int) {
        if (isViewportChanged) {
            mSession?.setDisplayGeometry(displayRotation, width, height)
            isViewportChanged = false
        }
    }
}