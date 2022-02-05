package com.rtelaku.faceapp.utils

import com.microsoft.projectoxford.face.FaceServiceClient
import com.microsoft.projectoxford.face.FaceServiceRestClient

object FaceServiceClientHelper {
    private lateinit var faceServiceClient: FaceServiceClient

    fun getFaceServiceClient(): FaceServiceClient {
        if (!this::faceServiceClient.isInitialized) {
            faceServiceClient = FaceServiceRestClient(apiEndPoint, apiKey)
        }
        return faceServiceClient
    }

    private const val apiEndPoint = "https://rigeita.cognitiveservices.azure.com/face/v1.0"
    private const val apiKey = "70aad4aa57ce479eb880991efb527370"
}