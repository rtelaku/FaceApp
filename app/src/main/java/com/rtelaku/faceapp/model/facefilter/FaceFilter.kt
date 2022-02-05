package com.rtelaku.faceapp.model.facefilter

import android.graphics.*
import com.google.mlkit.vision.face.Face
import com.rtelaku.faceapp.R
import com.rtelaku.faceapp.utils.FaceAppApplication

class FaceFilter(
    overlay: Filter,
    private val face: Face,
    private val imageRect: Rect) : Filter.Graphic(overlay) {

    override fun draw(canvas: Canvas?) {
        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox)

        val mIcon = BitmapFactory.decodeResource(FaceAppApplication.getInstance().resources, R.drawable.mask)
        canvas?.drawBitmap(mIcon, null, rect, null)
    }

}

