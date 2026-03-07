package com.miruai.app.ui.texttovideo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miruai.app.data.VideoRepository
import com.miruai.app.data.VideoResult
import com.miruai.app.util.PreferencesManager
import kotlinx.coroutines.launch

class TextToVideoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application)
    private val prefs = PreferencesManager(application)

    private val _generationState = MutableLiveData<GenerationState>()
    val generationState: LiveData<GenerationState> = _generationState

    fun generateVideo(prompt: String, style: String, durationSeconds: Int) {
        if (!prefs.hasApiKey()) {
            _generationState.value = GenerationState.Error("Please add your API key in Settings")
            return
        }

        if (prompt.isBlank()) {
            _generationState.value = GenerationState.Error("Please enter a description")
            return
        }

        _generationState.value = GenerationState.Loading

        viewModelScope.launch {
            val result = repository.generateFromText(
                apiKey = prefs.apiKey,
                prompt = prompt,
                style = style,
                durationSeconds = durationSeconds
            )

            _generationState.value = when (result) {
                is VideoResult.Success -> {
                    prefs.saveRecentVideo(result.videoUrl)
                    GenerationState.Success(result.videoUrl)
                }
                is VideoResult.Error -> GenerationState.Error(result.message)
                is VideoResult.Processing -> GenerationState.Loading
            }
        }
    }
}

sealed class GenerationState {
    object Loading : GenerationState()
    data class Success(val videoPath: String) : GenerationState()
    data class Error(val message: String) : GenerationState()
}
