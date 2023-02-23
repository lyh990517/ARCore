package com.example.arcorestudy

import android.content.Context
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
    val cubeScene: CubeRendering = CubeRendering.create(context)
    val arObjectScene: ArObjectRendering = ArObjectRendering.create(context)
    var noseRendering: FaceTipRendering =
        FaceTipRendering.create(context, fromAssets("NOSE.obj"), R.raw.nose_fur)
    var rightEarRendering: FaceTipRendering =
        FaceTipRendering.create(context, fromAssets("FOREHEAD_RIGHT.obj"), R.raw.ear_fur)
    var leftEarRendering: FaceTipRendering =
        FaceTipRendering.create(context, fromAssets("FOREHEAD_LEFT.obj"), R.raw.ear_fur)
    var faceObjectRendering: FaceObjectRendering =
        FaceObjectRendering.create(context, fromAssets("PlagueMask_sketchfab.obj"), R.raw.plagmask)
    var faceFilterRendering: FaceFilterRendering =
        FaceFilterRendering.create(context, R.raw.freckles)

    fun selectFace(
        type: String,
        objPath: String? = null,
        @RawRes objId: Int? = null,
        nosePath: String? = null,
        rightEarPath: String? = null,
        leftEarPath: String? = null,
        @RawRes nose: Int? = null,
        @RawRes rightEar: Int? = null,
        @RawRes leftEar: Int? = null
    ) {
        when (type) {
            "faceMask" -> {
                faceFilterRendering = FaceFilterRendering.create(context, objId!!)
            }
            "faceObject" -> {
                faceObjectRendering =
                    FaceObjectRendering.create(context, fromAssets(objPath!!), objId!!)
            }
            "faceTips" -> {
                noseRendering = FaceTipRendering.create(context, fromAssets(nosePath!!), nose!!)
                rightEarRendering =
                    FaceTipRendering.create(context, fromAssets(rightEarPath!!), rightEar!!)
                leftEarRendering =
                    FaceTipRendering.create(context, fromAssets(leftEarPath!!), leftEar!!)
            }
        }
    }

    private fun fromAssets(assetPath: String): Mesh {
        val obj = context.assets.open(assetPath)
            .let { stream -> ObjReader.read(stream) }
            .let { objStream -> ObjUtils.convertToRenderable(objStream) }
        return Mesh(
            indices = ObjData.getFaceVertexIndices(obj),
            vertices = ObjData.getVertices(obj),
            normals = ObjData.getNormals(obj),
            texCoords = ObjData.getTexCoords(obj, 2)
        )
    }
    companion object {
        fun create(context: Context) = RenderingManager(context)
    }
}