package com.example.timer

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.timer.logic.video_object
import java.io.File

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
        val videoFile: File = copyRawVideoToInternal(
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
    }

    private fun copyRawVideoToInternal(context: Context, rawId: Int, fileName: String): File {
        val outFile = context.getFileStreamPath(fileName)
        if (outFile.exists()) return outFile

        context.resources.openRawResource(rawId).use { input ->
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                val buffer = ByteArray(8 * 1024)
                var bytes: Int
                while (true) {
                    bytes = input.read(buffer)
                    if (bytes == -1) break
                    output.write(buffer, 0, bytes)
                }
            }
        }
        return outFile
    }

    private fun onRunAllClicked(){}

}