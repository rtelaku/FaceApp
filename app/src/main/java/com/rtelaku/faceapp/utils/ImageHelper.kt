package com.rtelaku.faceapp.utils

import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import com.microsoft.projectoxford.face.contract.Face
import java.io.File
import java.io.InputStream

class ImageHelper {

    /**
    Since most phone cameras take picture on landscape mode, portrait pictures get rotated for 90 degrees.
    Here we check the position of the image and return the exact one
     */
    fun getRotatedImage(imageFile: File): Bitmap {
        val myBitmap: Bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val uri = Uri.fromFile(imageFile)
        val input: InputStream? = FaceAppApplication.getInstance().applicationContext.contentResolver.openInputStream(uri)

        val ei: ExifInterface = if (Build.VERSION.SDK_INT > 23) ExifInterface(input!!) else ExifInterface(uri.path!!)

        val bitmap = when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(myBitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(myBitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(myBitmap, 270f)
            else -> myBitmap
        }

        return bitmap
    }

    /**
    Process image rotation and return the rotated image
     */
    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height,
            matrix, true)
    }

    /**
    Draw rectangles on detected faces
     */
    fun drawFaceRectanglesOnBitmap(
        originalBitmap: Bitmap, faces: Array<Face>?): Bitmap {
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = Color.RED
        paint.strokeWidth = 10f

        if (faces != null) {
            for (face in faces) {
                val faceRectangle = face.faceRectangle

                val cX = faceRectangle.left + faceRectangle.width
                val cY = faceRectangle.top + faceRectangle.height

                canvas.drawRect(
                    faceRectangle.left.toFloat(),
                    faceRectangle.top.toFloat(), (
                            faceRectangle.left + faceRectangle.width).toFloat(), (faceRectangle.top + faceRectangle.height).toFloat(), paint)

                drawTextEmotionOnImage(canvas, 100, cX/2 + cX / 5, cY + 100, Color.WHITE, faces, face)
            }

        }
        return bitmap
    }

    /**
    Draw text to detected faces to display face emotions
     */
    private fun drawTextEmotionOnImage(canvas: Canvas, textSize: Int, cX: Int, cY: Int, color: Int, result: Array<Face>, face: Face) {
        val textEmotion = Paint()
        textEmotion.isAntiAlias = true
        textEmotion.style = Paint.Style.FILL
        textEmotion.strokeWidth = 12f
        textEmotion.color = color
        textEmotion.textSize = textSize.toFloat()
        var cy = cY

        val emotionText = processFaceEmotionText(result, face)

        emotionText?.lines()?.forEach {
            canvas.drawText(it, cX.toFloat(), cy.toFloat(), textEmotion)
            cy+=100
        }
    }

    /**
     Return a string builder of emotions of each detected face in the image
     */
    private fun processFaceEmotionText(result: Array<Face>, face: Face) : StringBuilder? {
        val faceResults = result.toList().distinctBy { it.faceId }

        faceResults.forEach {
            if(face.faceId.equals(it.faceId)) {
                val emotionsStringBuilder = StringBuilder()
                val emotion = it.faceAttributes.emotion
                emotionsStringBuilder.writeEmotionValueIfNotZero("Happiness: ", emotion.happiness)
                    .writeEmotionValueIfNotZero("Sadness: ", emotion.sadness)
                    .writeEmotionValueIfNotZero("Surprise: ", emotion.surprise)
                    .writeEmotionValueIfNotZero("Neutral: ", emotion.neutral)
                    .writeEmotionValueIfNotZero("Anger: ", emotion.anger)
                    .writeEmotionValueIfNotZero("Contempt: ", emotion.contempt)
                    .writeEmotionValueIfNotZero("Disgust: ", emotion.disgust)
                    .writeEmotionValueIfNotZero("Fear: ", emotion.fear)

                return emotionsStringBuilder
            }
        }

        return null
    }

    private fun StringBuilder.writeEmotionValueIfNotZero(key: String, value: Double): StringBuilder {
        if (value > 0.0) {
            this.appendLine("$key : ${getPercentageOfValue(value)}%")
        }
        return this
    }

    private fun getPercentageOfValue(value: Double): Double {
        return value * 100
    }
}

