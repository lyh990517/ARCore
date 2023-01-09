package com.example.arcorestudy

import android.Manifest
import android.app.Activity
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
import java.util.*

class MainActivity : Activity() {
    private var mTextString: String? = null
    private var mTextView: TextView? = null
    private var mSurfaceView: GLSurfaceView? = null
    private var mRenderer: MainRenderer? = null
    private var mUserRequestedInstall = true
    private var mSession: Session? = null
    private var mConfig: Config? = null
    private val mPoints: MutableList<FloatArray> = ArrayList()
    private var mLastX = 0f
    private var mLastY = 0f
    private var mPointAdded = false
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBarAndTitleBar()
        setContentView(R.layout.activity_main)
        mTextView = findViewById<View>(R.id.ar_core_text) as TextView?
        mSurfaceView = findViewById<View>(R.id.gl_surface_view) as GLSurfaceView?
        (getSystemService(DISPLAY_SERVICE) as DisplayManager?)?.registerDisplayListener(object :
            DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {}
            override fun onDisplayChanged(displayId: Int) {
                synchronized(this) { mRenderer!!.onDisplayChanged() }
            }

            override fun onDisplayRemoved(displayId: Int) {}
        }, null)
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
                if (mPointAdded) {
                    val results: List<HitResult> = frame.hitTest(mLastX, mLastY)
                    for (result in results) {
                        val pose: Pose = result.getHitPose()
                        val points = floatArrayOf(pose.tx(), pose.ty(), pose.tz())
                        mPoints.add(points)
                        mRenderer!!.addPoint(points)
                        updateDistance()
                    }
                    mPointAdded = false
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
        mSurfaceView?.preserveEGLContextOnPause = true
        mSurfaceView?.setEGLContextClientVersion(2)
        mSurfaceView?.setRenderer(mRenderer)
        mSurfaceView?.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
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
                when (ArCoreApk.getInstance().requestInstall(
                    this,
                    mUserRequestedInstall
                )) {
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
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                mLastX = event.getX()
                mLastY = event.getY()
                mPointAdded = true
            }
        }
        return true
    }

    fun onRemoveButtonClick(view: View?) {
        if (!mPoints.isEmpty()) {
            mPoints.removeAt(mPoints.size - 1)
            mRenderer!!.removePoint()
            updateDistance()
        }
    }

    fun updateDistance() {
        runOnUiThread(Runnable {
            var totalDistance = 0.0
            if (mPoints.size >= 2) {
                for (i in 0 until mPoints.size - 1) {
                    val start = mPoints[i]
                    val end = mPoints[i + 1]
                    val distance = Math.sqrt(
                        (
                                (start[0] - end[0]) * (start[0] - end[0]) + (start[1] - end[1]) * (start[1] - end[1]) + (start[2] - end[2]) * (start[2] - end[2])).toDouble()
                    )
                    totalDistance += distance
                }
            }
            val distanceString = String.format(
                Locale.getDefault(),
                "%.2f", totalDistance
            ) + getString(R.string.distance_unit_text)
            mTextView?.text = distanceString
        })
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