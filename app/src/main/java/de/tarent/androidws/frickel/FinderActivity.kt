package de.tarent.androidws.frickel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Matrix
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.BasePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_finder.*

class FinderActivity : AppCompatActivity() {

    private val qrCodeDetector = QRCodeDetector(
            analyzer = FirebaseMLAnalyzer())

    private fun newPermissionListener(permissionListener: PermissionListener): PermissionListener = object : BasePermissionListener() {
        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
            super.onPermissionGranted(response)

            cameraTextureView.post { startCamera() }
        }

        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
            permissionListener.onPermissionDenied(response)

            finish()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_finder)

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(newPermissionListener(DialogOnDeniedPermissionListener.Builder
                        .withContext(this)
                        .withTitle("Camera Permission")
                        .withMessage("We need your camera to scan QR codes.")
                        .withIcon(R.drawable.ic_camera_black_24dp)
                        .withButtonText("Ok")
                        .build()))
                .check()

        (getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager)?.apply {
            registerDisplayListener(displayListener, null)
        }
    }

    override fun onStop() {
        (getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager)?.apply {
            unregisterDisplayListener(displayListener)
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

    private fun onDetected(rawValues: List<String>) {
        rawValues.elementAtOrNull(0)?.let {

            // Old school
            startActivity(Intent(this, MainActivity::class.java).apply {
                action = MainActivity.INTENT_ACTION_SCANNED_NAME
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.INTENT_EXTRA_NAME_KEY, it)
            })
        }

        cameraTextureView.postDelayed({
            qrCodeDetector.isDetecting = true
        }, DETECTOR_REENABLE_DELAY_MS)
    }

    companion object {

        private val TAG = "FinderAct"

        private const val DETECTOR_REENABLE_DELAY_MS = 1500L
    }

}