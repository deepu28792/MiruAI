package com.miruai.app.ui.texttovideo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.miruai.app.databinding.FragmentTextToVideoBinding
import com.miruai.app.ui.preview.VideoPreviewActivity

class TextToVideoFragment : Fragment() {

    private var _binding: FragmentTextToVideoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TextToVideoViewModel by viewModels()
    private lateinit var stylesAdapter: StylesAdapter
    private var selectedStyle = "Cinematic"
    private var selectedDuration = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextToVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStylesRecycler()
        setupDurationSelection()
        setupCharCounter()
        setupGenerateButton()
        observeViewModel()
    }

    private fun setupStylesRecycler() {
        val styles = listOf("Cinematic", "Anime", "Realistic", "Abstract", "3D Render", "Watercolor")
        stylesAdapter = StylesAdapter(styles, selectedStyle) { style ->
            selectedStyle = style
        }
        binding.rvStyles.apply {
            adapter = stylesAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupDurationSelection() {
        binding.rgDuration.setOnCheckedChangeListener { _, checkedId ->
            selectedDuration = when (checkedId) {
                binding.rb3sec.id -> 3
                binding.rb5sec.id -> 5
                binding.rb7sec.id -> 7
                else -> 3
            }
        }
    }

    private fun setupCharCounter() {
        binding.etPrompt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.tvCharCount.text = "${s?.length ?: 0} / 1000"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupGenerateButton() {
        binding.btnGenerate.setOnClickListener {
            val prompt = binding.etPrompt.text.toString()
            viewModel.generateVideo(prompt, selectedStyle, selectedDuration)
        }
    }

    private fun observeViewModel() {
        viewModel.generationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is GenerationState.Loading -> {
                    binding.btnGenerate.isEnabled = false
                    binding.layoutLoading.visibility = View.VISIBLE
                }
                is GenerationState.Success -> {
                    binding.btnGenerate.isEnabled = true
                    binding.layoutLoading.visibility = View.GONE
                    openVideoPreview(state.videoPath)
                }
                is GenerationState.Error -> {
                    binding.btnGenerate.isEnabled = true
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
