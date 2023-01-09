package com.example.arcorestudy

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
import android.opengl.GLSurfaceView.Renderer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.arcorestudy.databinding.ActivityMainBinding
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mSurfaceView: GLSurfaceView
    private var mSession: Session? = null
    private val mRenderer = Renderer()
    private var listener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}

        override fun onDisplayChanged(displayId: Int) {
            synchronized(this){
                mRenderer.onDisplayChanged()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        hideStatusBarAndTitleBar()
        setContentView(binding.root)
        mSurfaceView = binding.glSurfaceView
        mSurfaceView.apply {
            preserveEGLContextOnPause = true
            setEGLContextClientVersion(3)
            setRenderer(mRenderer)
            renderMode = RENDERMODE_CONTINUOUSLY
        }
    }

    private fun hideStatusBarAndTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    override fun onResume() {
        super.onResume()
        requestPermission()
        if (mSession == null) {
            when (ArCoreApk.getInstance().requestInstall(this, true)) {
                ArCoreApk.InstallStatus.INSTALLED -> {
                    mSession = Session(this)
                    Log.e("Main", "Created")
                }
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    Log.e("Main", "install request")
                }
                else -> {}
            }
        }
        mSession?.resume()
    }

    override fun onPause() {
        super.onPause()
        mSession?.pause()
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
    }
}