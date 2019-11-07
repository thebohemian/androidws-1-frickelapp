package de.tarent.androidws.clean.feature.qrscanner.view

import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import java.util.concurrent.Executor
import kotlin.properties.Delegates

class QRCodeDetector(
        private val executor: Executor,
        private val analyzer: FirebaseMLAnalyzer) {

    val analyzerUseCase = ImageAnalysis(ImageAnalysisConfig.Builder().apply {
        setTargetResolution(Size(480, 360))
        setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
    }.build())

    init {
        analyzer.onSuccess = { rawValues ->
            onDetected?.let {
                if (isDetecting) {
                    isDetecting = false

                    it.invoke(rawValues)
                }
            }

        }
    }

    var onDetected: ((List<String>) -> Unit)? = null

    var isDetecting by Delegates.observable(false) { _, _, newValue ->
        if (analyzerUseCase.analyzer == null && newValue) {
            analyzerUseCase.setAnalyzer(executor, analyzer)
        } else {
            analyzerUseCase.removeAnalyzer()
        }
    }

    companion object {
        private val TAG = "QRCodeDetector"
    }
}