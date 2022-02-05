package com.rtelaku.faceapp.viewModel

import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.ListenableFuture
import com.microsoft.projectoxford.face.FaceServiceClient.FaceAttributeType
import com.microsoft.projectoxford.face.contract.Face
import com.rtelaku.faceapp.model.singleliveevent.SingleLiveEvent
import com.rtelaku.faceapp.utils.FaceAppApplication
import com.rtelaku.faceapp.utils.FaceServiceClientHelper
import com.rtelaku.faceapp.utils.ImageHelper
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceFragmentSharedViewModel() : ViewModel() {

    private var currentPhotoPath: String = ""
    private val TAG = "MainViewModel"
    private val imageHelper = ImageHelper()
    private val faceServiceClient = FaceServiceClientHelper.getFaceServiceClient()

    private var errorValue = SingleLiveEvent<Boolean>()
    private var dialogState = SingleLiveEvent<Boolean>()
    private var mCameraSelector = MutableLiveData<CameraSelector>()

    var bitmapValue = MutableLiveData<Bitmap>()
    var previewValue = SingleLiveEvent<Preview>()
    var cameraSwitchButtonState = SingleLiveEvent<Boolean>()

    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var cameraProvider: ProcessCameraProvider
    private var cameraExecutor: ExecutorService
    private var imageAnalyzer: ImageAnalysis? = null
    private val preview = Preview.Builder().build()
    private var hasFrontCamera = false

    init {
        mCameraSelector.value = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(FaceAppApplication.getInstance().applicationContext)
        cameraProvider = cameraProviderFuture.get()

        hasFrontCamera = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
    }

    /**
    For better quality of the image, create a temporary file to pass it to the camera,
    in order to store the taken picture to that file and access it directly from there
     */
    fun createTemporaryFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = FaceAppApplication.getInstance().applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    /**
    Get selected photo file from gallery
     */
    fun getImageFromGallery(data: Intent?) {
        val selectedImageURI: Uri? = data?.data
        val imageFile = File(getPathFromURIForGallery(selectedImageURI))
        setImageFile(imageFile)
    }

    /**
    Get taken image file from camera
     */
    fun getCapturedImage() {
        val imageFile = File(currentPhotoPath)
        setImageFile(imageFile)
    }

    /**
    Get image file of the incoming image(either from camera or gallery) and convert it to bitmap
     */
    private fun setImageFile(imageFile: File) {
        dialogState.postValue(true)
        viewModelScope.launch {
            if (imageFile.exists()) {
                val fileInputStream = FileInputStream(imageFile)
                try {
                    val rotatedBitmap = imageHelper.getRotatedImage(imageFile)
                    bitmapValue.postValue(rotatedBitmap)
                    val detectEmotionResult = getFaceEmotionResult(fileInputStream)

                    postImageOnUI(rotatedBitmap, detectEmotionResult)

                    dialogState.postValue(false)
                } catch (e: Exception) {
                    checkErrorType(e)
                }
            }
        }
    }

    private fun checkErrorType(e : Exception) {
        if(e is IOException) {
            errorValue.postValue(true)
        }

        Log.e(TAG, "Error on setImage", e)
    }

    /**
    Check if there is any face detected in the image and post the image with emotion results to UI,
    otherwise only display the image without results and notify user that there is no face detected
     */
    private fun postImageOnUI(rotatedBitmap : Bitmap, detectEmotionResult : Array<Face>) {
        if(detectEmotionResult.isEmpty()) {
            Toast.makeText(FaceAppApplication.getInstance().applicationContext,
                "Couldn't detect any face!", Toast.LENGTH_SHORT).show()
            bitmapValue.postValue(rotatedBitmap)
        } else {
            val bitmap = imageHelper.drawFaceRectanglesOnBitmap(rotatedBitmap, detectEmotionResult)
            bitmapValue.postValue(bitmap)
        }
    }

    /**
    Get the exact path URI of image chosen from gallery
     */
    private fun getPathFromURIForGallery(uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = FaceAppApplication.getInstance().contentResolver.query(uri, projection, null,
            null, null)!!
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }

    /**
     Get the array of face detection results
     */
    private suspend fun getFaceEmotionResult(inputStream: InputStream): Array<Face> {
        val deferredEmotion = viewModelScope.async {
                withContext(Dispatchers.IO) {

                    return@withContext faceServiceClient.detect(
                        inputStream,
                        true,
                        true, arrayOf(
                            FaceAttributeType.Emotion)
                    )
                }
            }

        return deferredEmotion.await()
    }

    fun getErrorValue(): SingleLiveEvent<Boolean> {
        return errorValue
    }

    fun getDialogState(): SingleLiveEvent<Boolean> {
        return dialogState
    }

    fun getCameraSelector() : MutableLiveData<CameraSelector> {
        if(!hasFrontCamera) {
            mCameraSelector.value = CameraSelector.DEFAULT_BACK_CAMERA
            return mCameraSelector
        }
        return mCameraSelector
    }

    fun switchCamera() {
        mCameraSelector.value = if (mCameraSelector.value == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    fun startCamera(lifecycleOwner: LifecycleOwner, faceImageAnalyzer: ImageAnalysis.Analyzer, cameraSelector: CameraSelector) {

        cameraProviderFuture.addListener({

            previewValue.postValue(preview)

            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, faceImageAnalyzer)
                }

            try {
                displaySwitchCameraButton()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.d(TAG, "Camera failed")
            }
        }, ContextCompat.getMainExecutor(FaceAppApplication.getInstance().applicationContext))
    }

    /**
     * This method checks if the device has both front and back camera available in order to display the switch camera button
     */
    private fun displaySwitchCameraButton() {
        val hasBackCamera = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        if (hasBackCamera && hasFrontCamera) {
            cameraSwitchButtonState.postValue(true)
        } else if (!hasBackCamera && !hasFrontCamera) {
            cameraSwitchButtonState.postValue(false)
        }
    }

    fun shutDownExecutor() {
        cameraExecutor.shutdown()
    }
}