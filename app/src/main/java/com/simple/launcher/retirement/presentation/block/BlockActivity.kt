package com.simple.launcher.retirement.presentation.block

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.simple.launcher.retirement.databinding.ActivityBlockBinding
import com.simple.launcher.retirement.presentation.base.BaseActivity
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.ui.precompute.text.setText

class BlockActivity : BaseActivity<ActivityBlockBinding>() {

    private val viewModel: BlockViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater) = ActivityBlockBinding.inflate(inflater)

    override fun setupViews(savedInstanceState: Bundle?) {
        // Đọc tên app bị chặn từ Intent extra (truyền từ AppMonitoringService)
        val appName = intent.getStringExtra(EXTRA_APP_NAME)
        viewModel.setAppName(appName)

        val binding = binding ?: return

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

                val binding = binding ?: return

                binding.btnGoHome.root.performClick()
            }
        })
    }

    override fun observeData() {
        super.observeData()

        // Apply background từ theme — không hardcode màu trong XML
        viewModel.background.observe(this) { background ->

            val binding = binding ?: return@observe

            binding.root.setBackground(background)
        }

        viewModel.content.observe(this) { state ->

            val binding = binding ?: return@observe

            binding.tvTitle.setText(state.title)
            binding.tvMessage.setText(state.message)

            // Hiển thị pill tên app nếu có
            if (state.appName != null) {
                binding.tvAppName.visibility = View.VISIBLE
                binding.tvAppName.text = state.appName
            } else {
                binding.tvAppName.visibility = View.GONE
            }
        }

        viewModel.action.observe(this) { state ->

            val binding = binding ?: return@observe

            binding.btnGoHome.tvAction.setText(state.text)
            binding.btnGoHome.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }
    }

    companion object {
        const val EXTRA_APP_NAME = "extra_app_name"
    }
}
