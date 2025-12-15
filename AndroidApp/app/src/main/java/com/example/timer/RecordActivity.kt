package com.example.timer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RecordActivity : AppCompatActivity() {

    private lateinit var cameraExecutor : ExecutorService

    private var videoCapture : VideoCapture<Recorder>? = null
    private var activeRecording : Recording? = null
    private var isRecording : Boolean = false
    private var currentVideoFile : File? = null

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val recordButton = findViewById<ImageButton>(R.id.recordButton)

        cameraExecutor = Executors.newSingleThreadExecutor()

        backButton.setOnClickListener { finish() }
        recordButton.setOnClickListener {
            if (isRecording){ stopRecording() }
            else { startRecording() }
        }
        checkCameraPermissionAndStart()
    }
    private fun checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {startCamera()}
        else {cameraPermissionLauncher.launch(Manifest.permission.CAMERA)}
    }

    private fun RecordActivity.startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val previewView = findViewById<PreviewView>(R.id.cameraPreview)
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.FHD)).build()

            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
            }catch (e: Exception) {
                Log.e("RecordActivity", "Use case binding failed", e)
                Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun getVideoDirectory(): File{
        val dir = File(filesDir, "videos")
        if (!dir.exists()) {dir.mkdirs()}
        return dir
    }

    private fun createVideoFile(): File {
        return File(getVideoDirectory(), "record_${System.currentTimeMillis()}.mp4")
    }

    private fun RecordActivity.startRecording() {
        val videoCapture = this.videoCapture
        if (videoCapture == null) {
            Toast.makeText(this, "Camera is not ready yet", Toast.LENGTH_SHORT).show()
            return
        }

        activeRecording?.stop()
        activeRecording = null

        val file = createVideoFile()
        currentVideoFile = file

        val outputOptions = FileOutputOptions.Builder(file).build()

        activeRecording = videoCapture.output.prepareRecording(this, outputOptions)
                                            .start(ContextCompat.getMainExecutor(this)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        isRecording = true
                        Log.d("RecordActivity", "Recording started: ${file.absolutePath}")
                    }
                    is VideoRecordEvent.Finalize -> {
                        isRecording = false
                        activeRecording = null

                        if (event.error == VideoRecordEvent.Finalize.ERROR_NONE) {
                            Log.d("RecordActivity", "Recording finalized: ${file.absolutePath}")
                            Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("Record Activity", "Recording error: ${event.error}", event.cause)
                            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show()
                            if (file.exists()) {
                                file.delete()
                                currentVideoFile = null
                            }
                        }
                    }
                }
            }
    }

    private fun RecordActivity.stopRecording() {
        if (!isRecording || activeRecording == null){
            Toast.makeText(this, "Not recording", Toast.LENGTH_SHORT).show()
            return
        }
        activeRecording?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
