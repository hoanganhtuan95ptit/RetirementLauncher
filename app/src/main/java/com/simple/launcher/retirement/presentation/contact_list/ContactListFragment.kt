package com.simple.launcher.retirement.presentation.contact_list

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentAppListBinding
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.presentation.sendReorderContactsDeeplink
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.setText
import kotlinx.coroutines.flow.filterIsInstance

class ContactListFragment : BaseFragment<FragmentAppListBinding>() {

    private val viewModel: ContactListViewModel by viewModels {
        ContactListViewModelFactory(ContactRepository.instance)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.loadContacts(requireContext())
        } else {
            Toast.makeText(context, R.string.contact_permission_denied, Toast.LENGTH_SHORT).show()
        }
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

        checkPermissionAndLoad()
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
            binding.rvAppList.scrollToPosition(0)
        }

        AppEventBus.events.filterIsInstance<AppEvent.ContactSelected>().observe(this) { event ->
            viewModel.updateItem(event.entity)
        }
    }

    private fun navigateToReorder() {
        binding.layoutSearch.etSearch.setText("")

        val currentSelected = viewModel.getAllSelectedIds()
        if (currentSelected.isEmpty()) {
            Toast.makeText(context, R.string.contact_list_empty_error, Toast.LENGTH_SHORT).show()
            return
        }

        // Giữ đúng thứ tự cũ cho các contact đã lưu, thêm mới vào cuối
        val savedIds = ContactRepository.instance.getSelectedContacts().map { it.id }
        val orderedIds = savedIds.filter { it in currentSelected }.toMutableList()
        orderedIds.addAll(currentSelected.filter { it !in savedIds })

        sendReorderContactsDeeplink(orderedIds)
    }

    private fun checkPermissionAndLoad() {
        if (!PermissionManager.hasContactPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            viewModel.loadContacts(requireContext())
        }
    }
}

@Deeplink
class ContactListDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = DeepLinks.CONTACT_LIST

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ContactListFragment())
        
        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }
        
        transaction.commit()
        return true
    }
}
