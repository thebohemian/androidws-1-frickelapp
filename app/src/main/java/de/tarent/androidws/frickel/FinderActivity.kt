package de.tarent.androidws.frickel

import android.Manifest
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.Toast
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

    private var lastAdjustedOrientation = -1

    private val orientationCheckClosure = ::orientationCheck

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

    /**
     * Checks whether the last orientation update and the current display
     * rotation are different and calls the transformation update if necessary.
     *
     * This is a workaround for devices where a 180 degree orientation change
     * is not causing a configuration change.
     */
    private fun orientationCheck() {
        with (cameraTextureView) {
            display?.let {
                if (lastAdjustedOrientation != NOT_YET_SET
                        && lastAdjustedOrientation != it.rotation) {
                    updateCameraViewTransform()
                }
            }

            postDelayed(orientationCheckClosure, ORIENTATION_CHECK_DELAY_MS)
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

        cameraTextureView.postDelayed(orientationCheckClosure, ORIENTATION_CHECK_DELAY_MS)
    }

    override fun onStop() {
        cameraTextureView.removeCallbacks(orientationCheckClosure)

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

                lastAdjustedOrientation = display.rotation
            }
        }
    }

    private fun onDetected(rawValues: List<String>) {
        rawValues.elementAtOrNull(0)?.let {

            val msg = "barcode says: $it"
            Log.d(TAG, msg)

            Toast.makeText(this@FinderActivity, msg, Toast.LENGTH_SHORT)
                    .show()

            // TODO: Return value somehow
            finish()
        }

        cameraTextureView.postDelayed({
            qrCodeDetector.isDetecting = true
        }, DETECTOR_REENABLE_DELAY_MS)
    }

    companion object {
        private val TAG = "FinderAct"

        private const val NOT_YET_SET = -1

        private const val ORIENTATION_CHECK_DELAY_MS = 1000L

        private const val DETECTOR_REENABLE_DELAY_MS = 1500L
    }

}