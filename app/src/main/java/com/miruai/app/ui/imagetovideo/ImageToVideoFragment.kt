package com.miruai.app.ui.imagetovideo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.miruai.app.databinding.FragmentImageToVideoBinding
import com.miruai.app.ui.preview.VideoPreviewActivity
import com.miruai.app.ui.texttovideo.GenerationState

class ImageToVideoFragment : Fragment() {

    private var _binding: FragmentImageToVideoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ImageToVideoViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleImageSelected(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageToVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImagePicker()
        setupMotionSlider()
        setupGenerateButton()
        observeViewModel()
    }

    private fun setupImagePicker() {
        binding.flImageContainer.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        binding.btnChangeImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun setupMotionSlider() {
        binding.sbMotion.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvMotionValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun handleImageSelected(uri: Uri) {
        selectedImageUri = uri
        binding.layoutUploadPrompt.visibility = View.GONE
        binding.ivSelectedImage.visibility = View.VISIBLE
        binding.btnChangeImage.visibility = View.VISIBLE

        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.ivSelectedImage)
    }

    private fun setupGenerateButton() {
        binding.btnAnimate.setOnClickListener {
            val imageUri = selectedImageUri
            if (imageUri == null) {
                Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val motionStrength = binding.sbMotion.progress
            val motionPrompt = binding.etMotionPrompt.text.toString().takeIf { it.isNotBlank() }

            viewModel.generateVideo(imageUri, motionStrength, motionPrompt)
        }
    }

    private fun observeViewModel() {
        viewModel.generationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is GenerationState.Loading -> {
                    binding.btnAnimate.isEnabled = false
                    binding.layoutLoading.visibility = View.VISIBLE
                }
                is GenerationState.Success -> {
                    binding.btnAnimate.isEnabled = true
                    binding.layoutLoading.visibility = View.GONE
                    openVideoPreview(state.videoPath)
                }
                is GenerationState.Error -> {
                    binding.btnAnimate.isEnabled = true
                    binding.layoutLoading.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openVideoPreview(videoPath: String) {
        val intent = Intent(requireContext(), VideoPreviewActivity::class.java).apply {
            putExtra(VideoPreviewActivity.EXTRA_VIDEO_PATH, videoPath)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
