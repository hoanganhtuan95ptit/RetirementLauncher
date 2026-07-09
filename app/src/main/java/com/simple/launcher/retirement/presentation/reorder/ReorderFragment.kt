package com.simple.launcher.retirement.presentation.reorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentAppListBinding
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.setText
import kotlinx.coroutines.launch

class ReorderFragment : BaseFragment<FragmentAppListBinding>() {

    companion object {
        fun newInstance(type: ReorderType, ids: List<String>): ReorderFragment {
            return ReorderFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("type", type)
                    putStringArrayList("ids", ArrayList(ids))
                }
            }
        }
    }

    private val type: ReorderType by lazy {
        arguments?.getSerializable("type") as? ReorderType ?: ReorderType.APPS
    }

    private val initialIds: List<String> by lazy {
        arguments?.getStringArrayList("ids") ?: emptyList()
    }

    private val viewModel: ReorderViewModel by viewModels {
        ReorderViewModelFactory(type, initialIds, AppRepository.instance, ContactRepository.instance)
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAppListBinding {
        return FragmentAppListBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Reorder screen doesn't need search
        binding.layoutSearch.root.visibility = View.GONE

        binding.rvAppList.layoutManager = LinearLayoutManager(requireContext())
        
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                viewModel.moveItem(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(binding.rvAppList)

        binding.btnSave.root.setOnSafeClickListener {
            lifecycleScope.launch { checkPermissionsAndSave() }
        }

        viewModel.loadItems(requireContext())
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

        viewModel.doneAction.observe(this) { state ->
            binding.btnSave.tvAction.setText(state.text)
            binding.btnSave.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
        }

        viewModel.items.attachAdapter().observe(this) { (items, adapters) ->
            binding.rvAppList.submitListAndAwait(items, adapters, false)
        }
    }

    private suspend fun checkPermissionsAndSave() {
        // Lưu thứ tự trước khi xin quyền
        if (type == ReorderType.APPS) {
            AppRepository.instance.saveSelectedPackages(viewModel.getFinalIds())
        } else {
            ContactRepository.instance.saveSelectedContacts(viewModel.getFinalContacts())
        }

        // Xin quyền tuần tự — nếu bất kỳ quyền nào bị từ chối thì dừng
        if (type == ReorderType.APPS) {
            if (!PermissionManager.requireFilePermission()) return
            if (!PermissionManager.requireUsageStatsPermission()) return
            if (!PermissionManager.requireOverlayPermission()) return
        }
        if (!PermissionManager.requireDefaultLauncher()) return

        onSaveSuccess()
    }

    private fun onSaveSuccess() {
        // Kích hoạt tự động xóa APK và giám sát ứng dụng sau khi thiết lập xong
        activateAutoFeatures()

        val messageRes = if (type == ReorderType.APPS) R.string.app_list_saved else R.string.contact_list_saved
        android.widget.Toast.makeText(context, messageRes, android.widget.Toast.LENGTH_SHORT).show()

        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    /**
     * Tự động bật cấu hình xóa APK và giám sát ứng dụng khi thiết lập
     * danh sách app hoặc liên hệ nhanh hoàn tất.
     * Việc start/stop service thực tế do AppMonitoringSettingService
     * và FileCleanupSettingService xử lý qua Flow.
     */
    private fun activateAutoFeatures() {
        val repository = PreferenceRepository.instance

        if (!repository.isFileCleanupEnabled()) {
            repository.setFileCleanupEnabled(true)
        }

        if (!repository.isAppBlockEnabled()) {
            repository.setAppBlockEnabled(true)
        }
    }
}

@Deeplink
class ReorderDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = DeepLinks.REORDER

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        val type = when (extras?.get("type") as? String) {
            "contacts" -> ReorderType.CONTACTS
            else -> ReorderType.APPS
        }
        val ids = extras?.get("ids") as? List<String> ?: emptyList()
        
        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ReorderFragment.newInstance(type, ids))
        
        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }
        
        transaction.commit()
        return true
    }
}
