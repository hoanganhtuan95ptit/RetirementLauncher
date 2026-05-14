package com.simple.launcher.retirement.presentation.block

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import com.simple.launcher.retirement.databinding.ActivityBlockBinding
import com.simple.launcher.retirement.presentation.base.BaseActivity
import com.simple.launcher.retirement.presentation.main.MainActivity

class BlockActivity : BaseActivity<ActivityBlockBinding>() {

    override fun inflateBinding(inflater: LayoutInflater) = ActivityBlockBinding.inflate(inflater)

    override fun setupViews(savedInstanceState: Bundle?) {
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
