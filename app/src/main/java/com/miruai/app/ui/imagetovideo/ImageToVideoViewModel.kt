package com.miruai.app.ui.imagetovideo

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miruai.app.data.VideoRepository
import com.miruai.app.data.VideoResult
import com.miruai.app.ui.texttovideo.GenerationState
import com.miruai.app.util.PreferencesManager
import kotlinx.coroutines.launch

class ImageToVideoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application)
    private val prefs = PreferencesManager(application)

    private val _generationState = MutableLiveData<GenerationState>()
    val generationState: LiveData<GenerationState> = _generationState

    fun generateVideo(imageUri: Uri, motionStrength: Int, motionPrompt: String?) {
        if (!prefs.hasApiKey()) {
            _generationState.value = GenerationState.Error("Please add your API key in Settings")
            return
        }

        _generationState.value = GenerationState.Loading

        viewModelScope.launch {
            val result = repository.generateFromImage(
                apiKey = prefs.apiKey,
                imageUri = imageUri,
                motionStrength = motionStrength,
                motionPrompt = motionPrompt
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
