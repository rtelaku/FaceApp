package com.rtelaku.faceapp.ui.fragments.faceEmotion

import  android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rtelaku.faceapp.databinding.FragmentFaceEmotionBinding
import com.rtelaku.faceapp.ui.activities.dialog.CustomProgressDialog
import com.rtelaku.faceapp.viewModel.FaceFragmentSharedViewModel
import com.rtelaku.faceapp.utils.FaceAppApplication

class FaceEmotionFragment() : Fragment() {

    private lateinit var binding : FragmentFaceEmotionBinding
    private lateinit var faceFragmentSharedViewModel: FaceFragmentSharedViewModel
    private lateinit var dialog : Dialog

    private val CAPTURED_IMAGE_REQUEST_CODE = 42
    private val GALLERY_IMAGE_REQUEST_CODE = 100
    private val CAMERA_REQUEST_CODE = 123
    private val READ_STORAGE_PERMISSION_REQUEST_CODE = 41

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = FragmentFaceEmotionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        faceFragmentSharedViewModel = ViewModelProvider(this)[FaceFragmentSharedViewModel::class.java]

        dialog = CustomProgressDialog.progressDialog(requireContext())

        setUpClickListeners()
        observeMainViewModel()
    }

    private fun setUpClickListeners() {
        binding.takePhoto.setOnClickListener {
            checkCameraPermissionAndTakePhoto()
        }

        binding.buttonLoadPicture.setOnClickListener {
            checkStoragePermissionAndLoadGallery()
        }
    }

    private fun observeMainViewModel() {
        faceFragmentSharedViewModel.bitmapValue.observe(requireActivity(), {
            binding.image.setImageBitmap(it)
        })

        faceFragmentSharedViewModel.getDialogState().observe(this, { state ->
            if(state) {
                dialog.show()
            } else {
                dialog.dismiss()
            }
        })

        faceFragmentSharedViewModel.getErrorValue().observe(this, Observer {
            if(it) {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Check your internet connection!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
    Start camera intent and pass a file location to save captured image
     */
    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        faceFragmentSharedViewModel.createTemporaryFile().also {
            val fileProvider = FileProvider.getUriForFile(FaceAppApplication.getInstance().applicationContext, "com.rtelaku.fileprovider", it)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            startActivityForResult(takePictureIntent, CAPTURED_IMAGE_REQUEST_CODE)
        }
    }

    /**
    Open gallery intent to select an image
     */
    private fun loadPhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), GALLERY_IMAGE_REQUEST_CODE)
    }

    private fun checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE)
        } else {
            takePhoto()
        }
    }

    private fun checkStoragePermissionAndLoadGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_REQUEST_CODE)
        } else {
            loadPhotoFromGallery()
        }
    }

    /**
    If storage permission is allowed from the user then you can access gallery data
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            READ_STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults[0] != PackageManager.PERMISSION_DENIED) {
                    loadPhotoFromGallery()
                }
            }

            CAMERA_REQUEST_CODE -> {
                if (grantResults[0] != PackageManager.PERMISSION_DENIED) {
                    takePhoto()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAPTURED_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            faceFragmentSharedViewModel.getCapturedImage()
        } else if(requestCode == GALLERY_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            faceFragmentSharedViewModel.getImageFromGallery(data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}