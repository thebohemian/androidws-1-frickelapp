package de.tarent.androidws.clean.feature.qrscanner

import android.Manifest
import android.graphics.Matrix
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.BasePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import de.tarent.androidws.clean.R
import kotlinx.android.synthetic.main.component_fragment_finder.*

class FinderFragment : Fragment() {

    private val sharedViewModel: FinderSharedViewModel by navGraphViewModels(R.id.nav_graph)

    private val qrCodeDetector = QRCodeDetector(
            analyzer = FirebaseMLAnalyzer())

    private fun newPermissionListener(permissionListener: PermissionListener): PermissionListener = object : BasePermissionListener() {
        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
            super.onPermissionGranted(response)

            cameraTextureView.post { startCamera() }
        }

        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
            permissionListener.onPermissionDenied(response)

            findNavController().popBackStack()
        }

        override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
            permissionListener.onPermissionRationaleShouldBeShown(permission, token)
        }
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) {
            cameraTextureView?.let {
                updateCameraViewTransform()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.component_fragment_finder, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.let { nonNullContext ->
            Dexter.withActivity(activity)
                    .withPermission(Manifest.permission.CAMERA)
                    .withListener(newPermissionListener(DialogOnDeniedPermissionListener.Builder
                            .withContext(nonNullContext)
                            .withTitle("Camera Permission")
                            .withMessage("We need your camera to scan QR codes.")
                            .withIcon(R.drawable.ic_camera_black_24dp)
                            .withButtonText("Ok")
                            .build()))
                    .check()

            ContextCompat.getSystemService(nonNullContext, DisplayManager::class.java)?.apply {
                registerDisplayListener(displayListener, null)
            }
        }

        view.setOnClickListener {
            handleNameDetected("asia kitchen")
        }
    }

    override fun onStop() {
        context?.let { nonNullContext ->
            ContextCompat.getSystemService(nonNullContext, DisplayManager::class.java)?.apply {
                unregisterDisplayListener(displayListener)
            }
        }

        super.onStop()
    }

    private fun startCamera() {
        // Image Preview
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(cameraTextureView.width, cameraTextureView.height))
        }.build()

        val previewUseCase = Preview(previewConfig).apply {
            // Every time the viewfinder is updated, recompute layout
            setOnPreviewOutputUpdateListener { output ->
                (cameraTextureView.parent as? ViewGroup)?.apply {
                    removeView(cameraTextureView)
                    addView(cameraTextureView, 0)
                }

                cameraTextureView.surfaceTexture = output.surfaceTexture

                updateCameraViewTransform()
            }
        }

        Log.d(TAG, "start camera")
        qrCodeDetector.onDetected = ::onDetected
        qrCodeDetector.isDetecting = true

        CameraX.bindToLifecycle(this, previewUseCase, qrCodeDetector.analyzerUseCase)
    }

    private fun updateCameraViewTransform() {
        // Correct preview output to account for display rotation
        with(cameraTextureView) {
            when (display.rotation) {
                Surface.ROTATION_0 -> Triple(0f, 1f, 1f)
                Surface.ROTATION_90 -> Triple(90f, width / height.toFloat(), height / width.toFloat())
                Surface.ROTATION_180 -> Triple(180f, 1f, 1f)
                Surface.ROTATION_270 -> Triple(270f, width / height.toFloat(), height / width.toFloat())
                else -> return
            }.let { matrixParms ->
                Log.d(TAG, "update view transform: ${matrixParms.first}")

                // Compute the center of the view finder
                val centerX = width / 2f
                val centerY = height / 2f

                // Finally, apply transformations to our TextureView
                setTransform(Matrix().apply {
                    postRotate(-matrixParms.first, centerX, centerY)

                    postScale(matrixParms.second, matrixParms.third, centerX, centerY)
                })
            }
        }
    }

    private fun handleNameDetected(name: String) {
        Log.d(TAG, "Sending $name back")
        sharedViewModel.put(name)
        findNavController().popBackStack()
    }

    private fun onDetected(rawValues: List<String>) {
        rawValues.elementAtOrNull(0)?.let {
            handleNameDetected(it)
        }

        // We might have gone to another view, which would
        // invalidate cameraTextureView
        cameraTextureView?.postDelayed({
            qrCodeDetector.isDetecting = true
        }, DETECTOR_REENABLE_DELAY_MS)
    }

    companion object {

        private val TAG = "FinderAct"

        private const val DETECTOR_REENABLE_DELAY_MS = 1500L
    }

}