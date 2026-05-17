package com.simple.launcher.retirement.presentation.reorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentAppListBinding
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.AppEventBus
import kotlinx.coroutines.flow.filterIsInstance
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class ReorderFragment : BaseFragment<FragmentAppListBinding>() {

    // Flag để tránh xử lý PermissionAccept từ các bottom sheet không liên quan
    private var awaitingPermission = false

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
            checkPermissionsAndSave()
        }

        viewModel.loadItems()
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
            binding.btnSave.tvAction.setBackground(state.background)
        }

        viewModel.items.attachAdapter().observe(this) { (items, adapters) ->
            binding.rvAppList.submitListAndAwait(items, adapters, false)
        }

        // Lắng nghe kết quả từ các permission bottom sheet
        AppEventBus.events.filterIsInstance<AppEventBus.PermissionResult>().observe(this) { event ->
            if (!awaitingPermission) return@observe
            awaitingPermission = false
            if (event is AppEventBus.PermissionAccept) {
                checkAppPermissions()
            }
            // Nếu PermissionCancel: dừng lại, không làm gì thêm
        }
    }

    private fun checkPermissionsAndSave() {
        val context = requireContext()
        // Here we can either save first or check permissions first.
        // The user said "xử lý khi người dùng ấn lưu và trước khi xin quyền".
        // This might mean save the order then check permissions.
        
        // Actually, if we save the order now, and the user cancels permissions, 
        // the order is still saved. This seems correct.
        
        if (type == ReorderType.APPS) {
            AppRepository.instance.saveSelectedPackages(viewModel.getFinalIds())
        } else {
            // For contacts, we need to save the actual ContactEntity list in order.
            // Since we don't have the full list in ReorderViewModel yet (only IDs), 
            // we should probably have loaded them or just use what's in repo if IDs match.
            // But wait, the order might have changed.
            
            val finalIds = viewModel.getFinalIds()
            val currentSelected = ContactRepository.instance.getSelectedContacts()
            val orderedContacts = finalIds.mapNotNull { id -> currentSelected.find { it.id == id } }
            ContactRepository.instance.saveSelectedContacts(orderedContacts)
        }

        if (type == ReorderType.APPS) {
            checkAppPermissions()
        } else {
            // For contacts, usually just contact permission is needed, which was already granted.
            onSaveSuccess()
        }
    }

    private fun checkAppPermissions() {
        if (!PermissionManager.hasFilePermission(requireContext())) {
            awaitingPermission = true
            sendDeeplink("app://FilePermission")
            return
        }
        checkBlockPermissions()
    }

    private fun checkBlockPermissions() {
        val context = requireContext()
        if (!PermissionManager.hasUsageStatsPermission(context)) {
            awaitingPermission = true
            sendDeeplink("app://UsageStatsPermission")
            return
        }
        if (!PermissionManager.hasOverlayPermission(context)) {
            awaitingPermission = true
            sendDeeplink("app://OverlayPermission")
            return
        }
        checkDefaultLauncher()
    }

    private fun checkDefaultLauncher() {
        if (!PermissionManager.isDefaultLauncher(requireContext())) {
            awaitingPermission = true
            sendDeeplink("app://DefaultLauncher")
            return
        }
        onSaveSuccess()
    }

    private fun onSaveSuccess() {
        val messageRes = if (type == ReorderType.APPS) R.string.app_list_saved else R.string.contact_list_saved
        android.widget.Toast.makeText(context, messageRes, android.widget.Toast.LENGTH_SHORT).show()

        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}

@Deeplink
class ReorderDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = "app://reorder"

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
