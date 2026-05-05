package com.simple.launcher.retirement.presentation.block

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simple.launcher.retirement.databinding.ActivityBlockBinding
import com.simple.launcher.retirement.presentation.main.MainActivity

import androidx.activity.OnBackPressedCallback

class BlockActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGoHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.btnGoHome.performClick()
            }
        })
    }
}
