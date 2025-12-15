package com.example.timer

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.timer.logic.video_object
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ROI = intArrayOf(110 , 120 , 320 , 330)
        val videoFile: File? = copyRawVideoToInternal(
            context = this,
            rawId = R.raw.test_video,
            fileName = "test_video.mp4"
        )
        val video = video_object(videoFile, ROI)

        val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)
        val button4 = findViewById<Button>(R.id.button4)

        button1.setOnClickListener { video.compareAllFramesByRGB() }
        button2.setOnClickListener { video.compareAllFramesByWBC() }
        button3.setOnClickListener { video.processImages() }
        button4.setOnClickListener { onRunAllClicked() }

        val recordButton = findViewById<Button>(R.id.newRecordButton)

        recordButton.setOnClickListener { startActivity(Intent(this, RecordActivity::class.java)) }
    }

    fun copyRawVideoToInternal(
        context: Context,
        @RawRes rawId: Int,
        fileName: String
    ): File? {
        return try {
            val outFile = File(context.filesDir, fileName)

            // Only copy once; reuse if it already exists
            if (!outFile.exists()) {
                context.resources.openRawResource(rawId).use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

            if (outFile.exists()) outFile else null
        } catch (e: Resources.NotFoundException) {
            // rawId invalid (shouldn’t happen if it compiles)
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun onRunAllClicked(){}

}