package com.miruai.app.ui.preview

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.miruai.app.R
import com.miruai.app.databinding.ActivityVideoPreviewBinding
import java.io.File

class VideoPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPreviewBinding
    private var videoPath: String? = null
    private var isPlaying = false

    companion object {
        const val EXTRA_VIDEO_PATH = "extra_video_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoPath = intent.getStringExtra(EXTRA_VIDEO_PATH)
        setupVideo()
        setupControls()
    }

    private fun setupVideo() {
        videoPath?.let { path ->
            val uri = Uri.fromFile(File(path))
            binding.videoView.setVideoURI(uri)

            binding.videoView.setOnPreparedListener { mp ->
                binding.pbVideoLoading.visibility = View.GONE
                binding.ivPlayPause.visibility = View.VISIBLE
                mp.isLooping = true
            }

            binding.videoView.setOnErrorListener { _, _, _ ->
                Toast.makeText(this, "Error loading video", Toast.LENGTH_SHORT).show()
                true
            }

            binding.videoView.requestFocus()
        }
    }

    private fun setupControls() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.ivPlayPause.setOnClickListener {
            togglePlayback()
        }

        binding.btnPlayPause.setOnClickListener {
            togglePlayback()
        }

        binding.btnReplay.setOnClickListener {
            binding.videoView.seekTo(0)
            binding.videoView.start()
            isPlaying = true
            updatePlayPauseIcon()
        }

        binding.btnSave.setOnClickListener {
            saveVideoToGallery()
        }

        binding.btnShare.setOnClickListener {
            shareVideo()
        }
    }

    private fun togglePlayback() {
        if (isPlaying) {
            binding.videoView.pause()
            isPlaying = false
        } else {
            binding.videoView.start()
            isPlaying = true
        }
        updatePlayPauseIcon()
    }

    private fun updatePlayPauseIcon() {
        val iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        binding.ivPlayPause.setImageResource(iconRes)
        binding.btnPlayPause.setImageResource(iconRes)
    }

    private fun saveVideoToGallery() {
        val path = videoPath ?: return

        Toast.makeText(this, "Saving video...", Toast.LENGTH_SHORT).show()

        try {
            val sourceFile = File(path)
            val fileName = "MiruAI_${System.currentTimeMillis()}.mp4"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/MiruAI")
                }

                val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let { contentUri ->
                    contentResolver.openOutputStream(contentUri)?.use { output ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    Toast.makeText(this, "✅ Video saved to gallery!", Toast.LENGTH_SHORT).show()
                }
            } else {
                val destFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "MiruAI/$fileName"
                )
                destFile.parentFile?.mkdirs()
                sourceFile.copyTo(destFile, overwrite = true)
                Toast.makeText(this, "✅ Video saved to gallery!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareVideo() {
        val path = videoPath ?: return
        val file = File(path)

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Created with Miru AI ✨")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share your video"))
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            binding.videoView.pause()
            isPlaying = false
            updatePlayPauseIcon()
        }
    }
}
