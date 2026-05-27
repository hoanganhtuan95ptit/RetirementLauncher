package com.simple.launcher.retirement.presentation.app_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.sendReorderAppsDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentAppListBinding
import com.simple.launcher.retirement.domain.usecase.GetSelectableAppsUseCase
import com.simple.launcher.retirement.domain.usecase.SaveSelectedAppsUseCase
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import kotlinx.coroutines.flow.filterIsInstance

class AppListFragment : BaseFragment<FragmentAppListBinding>() {

    private val viewModel: AppListViewModel by viewModels {
        AppListViewModelFactory(GetSelectableAppsUseCase.instance, SaveSelectedAppsUseCase.instance)
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAppListBinding {
        return FragmentAppListBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.layoutSearch.etSearch.doAfterTextChanged {
            val text = it?.toString() ?: ""
            viewModel.search(text)
            binding.layoutSearch.ivClear.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.layoutSearch.ivClear.setOnSafeClickListener {
            binding.layoutSearch.etSearch.text = null
        }

        binding.btnSave.root.setOnSafeClickListener {
            navigateToReorder()
        }

        viewModel.loadApps()
    }

    override fun observeData() {
        super.observeData()
        viewModel.background.observe(this) { background ->
            binding.root.setBackground(background)
        }

        viewModel.toolbar.observe(this) { state ->
            binding.toolbar.tvTitle.setText(state.title)
            val backIcon = state.backIcon
            if (backIcon != null) {
                binding.toolbar.ivLeft.visibility = View.VISIBLE
                binding.toolbar.ivLeft.setImage(backIcon)
            } else {
                binding.toolbar.ivLeft.visibility = View.GONE
            }
        }

        viewModel.searchState.observe(this) { state ->
            binding.layoutSearch.root.setBackground(state.background)
            binding.layoutSearch.etSearch.hint = state.hint
            binding.layoutSearch.etSearch.setHintTextColor(state.hintColor)
            binding.layoutSearch.etSearch.setTextColor(state.textColor)
            state.clearIcon?.let { binding.layoutSearch.ivClear.setImage(it) }
        }

        viewModel.saveAction.observe(this) { state ->
            binding.btnSave.tvAction.setText(state.text)
            binding.btnSave.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }

        viewModel.items.attachAdapter().observe(this) { (items, adapters) ->
            binding.rvAppList.submitListAndAwait(items, adapters, true)
        }

        AppEventBus.events.filterIsInstance<AppEvent.AppSelected>().observe(this) { event ->
            viewModel.updateItem(event.entity)
        }
    }

    private fun navigateToReorder() {
        binding.layoutSearch.etSearch.setText("")

        val currentSelected = viewModel.getAllSelectedIds()
        if (currentSelected.isEmpty()) {
            Toast.makeText(context, R.string.app_list_empty_error, Toast.LENGTH_SHORT).show()
            return
        }

        // Lấy danh sách đã lưu để giữ đúng thứ tự cũ
        val savedIds = com.simple.launcher.retirement.domain.repository.AppRepository.instance.getSelectedPackages()

        // 1. Giữ lại những app cũ vẫn đang được chọn (đúng thứ tự cũ)
        val orderedIds = savedIds.filter { it in currentSelected }.toMutableList()
        // 2. Thêm những app mới được chọn vào cuối
        orderedIds.addAll(currentSelected.filter { it !in savedIds })

        sendReorderAppsDeeplink(orderedIds)
    }
}

@Deeplink
class AppListDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = DeepLinks.APP_LIST

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AppListFragment())
        
        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }
        
        transaction.commit()
        return true
    }
}
