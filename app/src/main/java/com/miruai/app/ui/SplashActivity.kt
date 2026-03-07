package com.miruai.app.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.miruai.app.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animateLogo()

        lifecycleScope.launch {
            delay(2500)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun animateLogo() {
        // Fade in logo
        binding.tvLogo.alpha = 0f
        binding.tvAppName.alpha = 0f
        binding.tvTagline.alpha = 0f
        binding.ivLogoBg.scaleX = 0.5f
        binding.ivLogoBg.scaleY = 0.5f

        val bgScaleX = ObjectAnimator.ofFloat(binding.ivLogoBg, View.SCALE_X, 0.5f, 1f)
        val bgScaleY = ObjectAnimator.ofFloat(binding.ivLogoBg, View.SCALE_Y, 0.5f, 1f)
        val bgAlpha = ObjectAnimator.ofFloat(binding.ivLogoBg, View.ALPHA, 0f, 0.15f)

        val logoFade = ObjectAnimator.ofFloat(binding.tvLogo, View.ALPHA, 0f, 1f)
        val nameFade = ObjectAnimator.ofFloat(binding.tvAppName, View.ALPHA, 0f, 1f)
        val taglineFade = ObjectAnimator.ofFloat(binding.tvTagline, View.ALPHA, 0f, 1f)

        val bgSet = AnimatorSet().apply {
            playTogether(bgScaleX, bgScaleY, bgAlpha)
            duration = 600
            interpolator = DecelerateInterpolator()
        }

        val textSet = AnimatorSet().apply {
            playSequentially(logoFade, nameFade, taglineFade)
            duration = 300
        }

        AnimatorSet().apply {
            playSequentially(bgSet, textSet)
            start()
        }
    }
}
