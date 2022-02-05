package com.rtelaku.faceapp.utils

import android.app.Application

class FaceAppApplication : Application() {
    override fun onCreate() {
        faceAppInstance = this
        super.onCreate()
    }

    companion object {
        private lateinit var faceAppInstance: FaceAppApplication
        fun getInstance(): FaceAppApplication {
            return faceAppInstance
        }
    }
}