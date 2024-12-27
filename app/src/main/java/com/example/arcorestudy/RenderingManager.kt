package com.example.arcorestudy

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import com.example.arcorestudy.rendering.ArObjectRendering
import com.example.arcorestudy.rendering.CameraTextureRendering
import com.example.arcorestudy.rendering.CubeRendering
import com.example.arcorestudy.rendering.Face.FaceFilterRendering
import com.example.arcorestudy.rendering.Face.FaceObjectRendering
import com.example.arcorestudy.rendering.Face.FaceTipRendering
import com.example.arcorestudy.rendering.PointCloudRendering
import com.example.arcorestudy.tools.Mesh
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjUtils

class RenderingManager(private val context: Context) {

    val mCamera: CameraTextureRendering = CameraTextureRendering.create(context)
    val mPointCloud: PointCloudRendering = PointCloudRendering.create(context)
    val arObjectScene: ArObjectRendering = ArObjectRendering.create(context)
    companion object {
        fun create(context: Context) = RenderingManager(context)
    }
}