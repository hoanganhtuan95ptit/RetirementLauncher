package com.simple.launcher.retirement.presentation.contact_list

import android.Manifest
import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.provider.ContactsContract
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.databinding.FragmentAppListBinding
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.model.SelectableContactEntity
import com.simple.launcher.retirement.presentation.default_launcher.DefaultLauncherBottomSheet
import com.simple.launcher.retirement.presentation.permissions.BlockPermissionBottomSheet
import com.simple.launcher.retirement.presentation.permissions.FilePermissionBottomSheet

class ContactListFragment : Fragment() {

    private var _binding: FragmentAppListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: AppRepositoryImpl
    private val contacts = mutableListOf<SelectableContactEntity>()
    private lateinit var adapter: ContactListAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            loadContacts()
        } else {
            Toast.makeText(context, R.string.contact_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = AppRepositoryImpl(requireContext())

        binding.toolbar.title = getString(R.string.contact_list_title)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        adapter = ContactListAdapter(contacts)
        binding.rvAppList.adapter = adapter

        binding.btnSave.setOnClickListener {
            checkPermissionsAndSave()
        }

        checkPermissionAndLoad()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkPermissionsAndSave() {
        if (!hasFilePermission()) {
            FilePermissionBottomSheet {
                checkBlockPermissions()
            }.show(childFragmentManager, FilePermissionBottomSheet.TAG)
            return
        }
        checkBlockPermissions()
    }

    private fun checkBlockPermissions() {
        if (!hasUsageStatsPermission() || !hasOverlayPermission()) {
            BlockPermissionBottomSheet {
                if (hasUsageStatsPermission() && hasOverlayPermission()) {
                    checkDefaultLauncher()
                } else {
                    Toast.makeText(context, "Bạn cần cấp đủ cả 2 quyền để tính năng hoạt động", Toast.LENGTH_SHORT).show()
                }
            }.show(childFragmentManager, BlockPermissionBottomSheet.TAG)
            return
        }
        checkDefaultLauncher()
    }

    private fun checkDefaultLauncher() {
        if (!isDefaultLauncher()) {
            DefaultLauncherBottomSheet {
                saveAndExit()
            }.show(childFragmentManager, DefaultLauncherBottomSheet.TAG)
            return
        }
        saveAndExit()
    }

    private fun saveAndExit() {
        val selected = contacts.filter { it.isSelected }.map { it.contact }
        repository.saveSelectedContacts(selected)
        Toast.makeText(context, R.string.contact_list_saved, Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun hasFilePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            requireContext().packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(requireContext())
    }

    private fun isDefaultLauncher(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(Context.ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        } else {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = requireContext().packageManager.resolveActivity(intent, 0)
            resolveInfo?.activityInfo?.packageName == requireContext().packageName
        }
    }

    private fun checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            loadContacts()
        }
    }

    private fun loadContacts() {
        val selectedIds = repository.getSelectedContacts().map { it.id }.toSet()
        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        contacts.clear()
        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

            val processedIds = mutableSetOf<String>()

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                if (processedIds.contains(id)) continue
                
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                val photoUri = it.getString(photoIndex)

                val contact = ContactEntity(id, name, number, photoUri)
                contacts.add(SelectableContactEntity(contact, selectedIds.contains(id)))
                processedIds.add(id)
            }
        }
        adapter.notifyDataSetChanged()
    }
}
