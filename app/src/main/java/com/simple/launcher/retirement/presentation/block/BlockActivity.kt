package com.simple.launcher.retirement.presentation.block

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.simple.launcher.retirement.databinding.ActivityBlockBinding
import com.simple.launcher.retirement.presentation.base.BaseActivity
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class BlockActivity : BaseActivity<ActivityBlockBinding>() {

    private val viewModel: BlockViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater) = ActivityBlockBinding.inflate(inflater)

    override fun setupViews(savedInstanceState: Bundle?) {
        binding.btnGoHome.root.setOnSafeClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.btnGoHome.root.performClick()
            }
        })
    }

    override fun observeData() {
        super.observeData()
        viewModel.content.observe(this) { state ->
            binding.tvTitle.setText(state.title)
            binding.tvMessage.setText(state.message)
        }
        // Activity implement LifecycleOwner → dùng overload observe(lifecycleOwner)
        viewModel.action.observe(this) { state ->
            binding.btnGoHome.tvAction.setText(state.text)
            binding.btnGoHome.tvAction.setBackground(state.background)
        }
    }
}
