package com.example.arcorestudy

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.*
import java.lang.UnsupportedOperationException

class MainActivity : Activity() {
    private var mTextString: String? = null
    private var mTextView: TextView? = null
    private var mSurfaceView: GLSurfaceView? = null
    private var mRenderer: MainRenderer? = null
    private var mUserRequestedInstall = true
    private var mSession: Session? = null
    private var mConfig: Config? = null
    private var mCurrentX = 0f
    private var mCurrentY = 0f
    private var mTouched = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBarAndTitleBar()
        setContentView(R.layout.activity_main)
        mTextView = findViewById<View>(R.id.ar_core_text) as TextView?
        mSurfaceView = findViewById<View>(R.id.gl_surface_view) as GLSurfaceView?
        val displayManager: DisplayManager? =
            getSystemService(Context.DISPLAY_SERVICE) as DisplayManager?
        if (displayManager != null) {
            displayManager.registerDisplayListener(object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(displayId: Int) {}
                override fun onDisplayChanged(displayId: Int) {
                    synchronized(this) { mRenderer!!.onDisplayChanged() }
                }

                override fun onDisplayRemoved(displayId: Int) {}
            }, null)
        }
        mRenderer = MainRenderer(object : MainRenderer.RenderCallback {
            override fun preRender() {
                if (mRenderer!!.isViewportChanged) {
                    val display: Display = getWindowManager().getDefaultDisplay()
                    val displayRotation = display.rotation
                    mRenderer!!.updateSession(mSession!!, displayRotation)
                }
                mSession!!.setCameraTextureName(mRenderer!!.textureId)
                val frame = mSession!!.update()
                if (frame.hasDisplayGeometryChanged()) {
                    mRenderer!!.transformDisplayGeometry(frame)
                }
                val pointCloud: PointCloud = frame.acquirePointCloud()
                mRenderer!!.updatePointCloud(pointCloud)
                pointCloud.release()
                if (mTouched) {
                    mTextString = ""
                    val results: List<HitResult> = frame.hitTest(mCurrentX, mCurrentY)
                    var i = 0
                    for (result in results) {
                        val distance: Float = result.getDistance()
                        val pose: Pose = result.getHitPose()
                        val xAxis: FloatArray = pose.getXAxis()
                        val yAxis: FloatArray = pose.getYAxis()
                        val zAxis: FloatArray = pose.getZAxis()
                        mRenderer!!.addPoint(pose.tx(), pose.ty(), pose.tz())
                        mRenderer!!.addLineX(
                            pose.tx(), pose.ty(), pose.tz(),
                            xAxis[0], xAxis[1], xAxis[2]
                        )
                        mRenderer!!.addLineY(
                            pose.tx(), pose.ty(), pose.tz(),
                            yAxis[0], yAxis[1], yAxis[2]
                        )
                        mRenderer!!.addLineZ(
                            pose.tx(), pose.ty(), pose.tz(),
                            zAxis[0], zAxis[1], zAxis[2]
                        )
                        mTextString += """
                            [$i] distance : $distance, Pose : ${pose.toString()}
                            
                            """.trimIndent()
                        i++
                    }
                    runOnUiThread(Runnable { mTextView?.setText(mTextString) })
                    mTouched = false
                }
                val camera = frame.camera
                val projMatrix = FloatArray(16)
                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f)
                val viewMatrix = FloatArray(16)
                camera.getViewMatrix(viewMatrix, 0)
                mRenderer!!.setProjectionMatrix(projMatrix)
                mRenderer!!.updateViewMatrix(viewMatrix)
            }
        })
        mSurfaceView?.setPreserveEGLContextOnPause(true)
        mSurfaceView?.setEGLContextClientVersion(2)
        mSurfaceView?.setRenderer(mRenderer)
    }

    protected override fun onPause() {
        super.onPause()
        mSurfaceView?.onPause()
        mSession!!.pause()
    }

    protected override fun onResume() {
        super.onResume()
        requestCameraPermission()
        try {
            if (mSession == null) {
                when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        mSession = Session(this)
                        Log.d(TAG, "ARCore Session created.")
                    }
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        mUserRequestedInstall = false
                        Log.d(TAG, "ARCore should be installed.")
                    }
                }
            }
        } catch (e: UnsupportedOperationException) {
            Log.e(TAG, e.message!!)
        }
        mConfig = Config(mSession)
        if (!mSession!!.isSupported(mConfig)) {
            Log.d(TAG, "This device is not support ARCore.")
        }
        mSession!!.configure(mConfig)
        mSession!!.resume()
        mSurfaceView?.onResume()
        mSurfaceView?.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                mCurrentX = event.getX()
                mCurrentY = event.getY()
                mTouched = true
            }
        }
        return true
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
    }

    private fun hideStatusBarAndTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}