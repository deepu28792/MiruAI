package com.miruai.app.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.miruai.app.R
import com.miruai.app.databinding.FragmentHomeBinding
import com.miruai.app.util.PreferencesManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())

        setupClickListeners()
        checkApiKey()
    }

    private fun setupClickListeners() {
        binding.cardTextToVideo.setOnClickListener {
            findNavController().navigate(R.id.nav_text_to_video)
        }

        binding.cardImageToVideo.setOnClickListener {
            findNavController().navigate(R.id.nav_image_to_video)
        }

        binding.ivSettings.setOnClickListener {
            showApiKeyDialog()
        }
    }

    private fun checkApiKey() {
        if (!prefs.hasApiKey()) {
            showApiKeyDialog()
        }
    }

    private fun showApiKeyDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_api_key, null)
        val etApiKey = dialogView.findViewById<EditText>(R.id.et_api_key)
        etApiKey.setText(prefs.apiKey)

        AlertDialog.Builder(requireContext())
            .setTitle("⚙️ Stability AI API Key")
            .setMessage("Enter your Stability AI API key to start generating videos.\n\nGet your key at platform.stability.ai")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val key = etApiKey.text.toString().trim()
                if (key.isNotEmpty()) {
                    prefs.apiKey = key
                    Toast.makeText(requireContext(), "✅ API key saved!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
