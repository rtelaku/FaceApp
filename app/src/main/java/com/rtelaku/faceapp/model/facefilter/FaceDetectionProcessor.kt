package com.rtelaku.faceapp.model.facefilter

import android.graphics.Rect
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException

class FaceDetectionProcessor(private val view: Filter) : FaceImageAnalyzer<List<Face>>() {

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

    override val graphicOverlay: Filter
    get() = view

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun onSuccess(
        results: List<Face>,
        graphicOverlay: Filter,
        rect: Rect) {
        graphicOverlay.clear()
        graphicOverlay.addAll(results.map { FaceFilter(graphicOverlay, it, rect) })
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face detection failed.$e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }
}