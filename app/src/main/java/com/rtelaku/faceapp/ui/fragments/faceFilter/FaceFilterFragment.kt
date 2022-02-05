package com.rtelaku.faceapp.ui.fragments.faceFilter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rtelaku.faceapp.databinding.FragmentFaceFilterBinding
import com.rtelaku.faceapp.model.facefilter.FaceDetectionProcessor
import com.rtelaku.faceapp.viewModel.FaceFragmentSharedViewModel

class FaceFilterFragment : Fragment() {

    private lateinit var binding: FragmentFaceFilterBinding
    private val TAG = "FaceFilterFragment"
    private val CAMERAX_REQUEST_CODE = 123

    private lateinit var cameraSelector: CameraSelector
    private lateinit var faceFragmentSharedViewModel: FaceFragmentSharedViewModel
    private lateinit var faceAnalysis: ImageAnalysis.Analyzer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?, ): View {

        binding = FragmentFaceFilterBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        faceFragmentSharedViewModel = ViewModelProvider(this)[FaceFragmentSharedViewModel::class.java]

        faceAnalysis = FaceDetectionProcessor(binding.graphicOverlayFinder)

        observeViewModel()
        showCameraPreview()
        onClickSwitchCamera()

    }

    private fun showCameraPreview() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA),
                CAMERAX_REQUEST_CODE)
        } else {
            faceFragmentSharedViewModel.startCamera(this, faceAnalysis, cameraSelector)
        }
    }

    private fun observeViewModel() {
        faceFragmentSharedViewModel.getCameraSelector().observe(requireActivity(), {
            cameraSelector = it
            binding.graphicOverlayFinder.toggleSelector(cameraSelector)
            faceFragmentSharedViewModel.startCamera(this, faceAnalysis, cameraSelector)
        })

        //get camera preview
        faceFragmentSharedViewModel.previewValue.observe(this, {
            it.setSurfaceProvider(binding.cameraXPreview.surfaceProvider)
        })

        faceFragmentSharedViewModel.cameraSwitchButtonState.observe(this, {
            if(it) {
                binding.switchCameraButton.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "There is no camera available!", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun onClickSwitchCamera() {
        binding.switchCameraButton.setOnClickListener {
            faceFragmentSharedViewModel.switchCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERAX_REQUEST_CODE && grantResults[0] != PackageManager.PERMISSION_DENIED) {
            faceFragmentSharedViewModel.startCamera(this, faceAnalysis, cameraSelector)
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        faceFragmentSharedViewModel.startCamera(this, faceAnalysis, cameraSelector)
        binding.graphicOverlayFinder.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        faceFragmentSharedViewModel.shutDownExecutor()
    }

}