package de.tarent.androidws.clean.feature.qrscanner.view

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

class FirebaseMLAnalyzer : ImageAnalysis.Analyzer {

    var onSuccess: ((List<String>) -> Unit)? = null

    val detector = FirebaseVision.getInstance()
            .getVisionBarcodeDetector(FirebaseVisionBarcodeDetectorOptions.Builder()
                    .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                    .build())

    private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    override fun analyze(imageProxy: ImageProxy?, degrees: Int) {
        val imageRotation = degreesToFirebaseRotation(degrees)
        imageProxy?.image?.let {
            detect(FirebaseVisionImage.fromMediaImage(it, imageRotation))
        }
    }

    private fun detect(image: FirebaseVisionImage) {
        detector.detectInImage(image)
                .addOnSuccessListener { barcodes ->
                    onSuccess?.invoke(barcodes.mapNotNull { it.rawValue })
                }
    }

}