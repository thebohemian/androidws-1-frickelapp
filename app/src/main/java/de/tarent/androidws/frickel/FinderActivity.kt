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
import androidx.camera.core.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.BasePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_finder.*
import java.util.concurrent.Executors

class FinderActivity : AppCompatActivity() {

    private val executor = Executors.newSingleThreadExecutor()

    private fun newPermissionListener(permissionListener: PermissionListener): PermissionListener = object : BasePermissionListener() {
        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
            super.onPermissionGranted(response)

            viewFinder.post { startCamera() }
        }

        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
            permissionListener.onPermissionDenied(response)

            finish()
        }

        override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
            permissionListener.onPermissionRationaleShouldBeShown(permission, token)
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

        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateViewFinderTransform()
        }
    }

    private fun startCamera() {
        // Image Preview
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(viewFinder.width, viewFinder.height))
        }.build()

        val previewUseCase = Preview(previewConfig).apply {
            // Every time the viewfinder is updated, recompute layout
            setOnPreviewOutputUpdateListener { output ->

                // To update the SurfaceTexture, we have to remove it and re-add it
                (viewFinder.parent as ViewGroup).apply {
                    removeView(viewFinder)
                    addView(viewFinder, 0)
                }

                viewFinder.surfaceTexture = output.surfaceTexture
                updateViewFinderTransform()
            }
        }

        // Image Analysis
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            setTargetResolution(Size(480, 360))
            setImageReaderMode(
                    ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer(executor, FirebaseMLAnalyzer(::onDetectionSuccess))
        }

        CameraX.bindToLifecycle(this, previewUseCase, analyzerUseCase)
    }

    private fun updateViewFinderTransform() {
        with(viewFinder) {
            // Compute the center of the view finder
            val centerX = width / 2f
            val centerY = height / 2f

            // Correct preview output to account for display rotation
            val rotationDegrees = when (display.rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> return
            }

            // Finally, apply transformations to our TextureView
            setTransform(Matrix().apply {
                postRotate(-rotationDegrees.toFloat(), centerX, centerY)
            })

        }

    }

    private fun onDetectionSuccess(texts: List<String>) {
        texts.forEach {
            val msg = "barcode says: $it"
            Log.d(TAG, msg)
            Toast.makeText(this@FinderActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val TAG = "FinderAct"
    }

}