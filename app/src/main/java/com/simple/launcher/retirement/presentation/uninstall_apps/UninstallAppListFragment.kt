package com.simple.launcher.retirement.presentation.uninstall_apps

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentUninstallAppListBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.app_monitoring.AppMonitoringPauser
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.asObjectOrNull
import com.simple.launcher.retirement.utils.exts.observe
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.setText
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull

/**
 * Màn danh sách app cho phép user chọn nhiều app để xoá.
 *
 * Popup xoá của hệ thống được hiện TUẦN TỰ qua [PackageInstaller]:
 * - Bấm "Xoá" → build queue package cần xoá theo thứ tự alphabet.
 * - Gọi [PackageInstaller.uninstall] cho package đầu tiên → hệ thống trả
 *   [PackageInstaller.STATUS_PENDING_USER_ACTION] kèm confirm Intent qua
 *   BroadcastReceiver → mình launch Intent đó để hiện popup xoá.
 * - User confirm/cancel → receiver nhận [PackageInstaller.STATUS_SUCCESS]
 *   hoặc [PackageInstaller.STATUS_FAILURE*] → pop khỏi queue rồi lặp lại
 *   cho package kế tiếp.
 *
 * KHÔNG dùng `startActivity(Intent.ACTION_DELETE)` vì MainActivity là
 * `singleInstance` — activity mới bị đẩy sang task khác và không nổi lên.
 * PackageInstaller được thiết kế để chạy qua system service nên miễn
 * nhiễm với launchMode của app gọi.
 *
 * Cần permission `REQUEST_DELETE_PACKAGES` (normal, auto-grant).
 */
class UninstallAppListFragment : BaseFragment<FragmentUninstallAppListBinding>() {

    private val viewModel: UninstallAppListViewModel by viewModels()

    /** Queue các package đang chờ hiển thị popup uninstall. Package ở đầu là
     *  package đang được xử lý trong popup hiện tại (nếu có). */
    private val pendingQueue: ArrayDeque<String> = ArrayDeque()

    /** Package đang đứng ở popup hiện tại — dùng để verify sau khi popup đóng. */
    private var currentUninstalling: String? = null

    /** True khi đã launch confirm intent (popup xoá của hệ thống đã hiện lên) —
     *  dùng cho onResume fallback: nếu user đóng popup mà receiver không kịp
     *  trả kết quả (ví dụ press Back), onResume vẫn dọn state để lần bấm sau
     *  của user không bị guard chặn. */
    private var awaitingConfirmClose: Boolean = false

    /** Cờ đảm bảo receiver chỉ register/unregister đúng 1 lần. */
    private var receiverRegistered: Boolean = false

    /**
     * Nhận callback từ [PackageInstaller.uninstall]:
     * - STATUS_PENDING_USER_ACTION: hệ thống trả về Intent xác nhận → launch để hiện popup.
     * - STATUS_SUCCESS: xoá xong → remove khỏi selection.
     * - STATUS_FAILURE*: user cancel hoặc lỗi → giữ nguyên trong selection.
     * Trong mọi trường hợp terminal (không phải PENDING) → pop queue rồi launch popup kế.
     */
    private val statusReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(ctx: Context?, intent: Intent?) {

            intent ?: return
            val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
            val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                ?: currentUninstalling

            when (status) {

                PackageInstaller.STATUS_PENDING_USER_ACTION -> launchConfirmIntent(intent, packageName)
                PackageInstaller.STATUS_SUCCESS -> onUninstallFinished(packageName, success = true)
                else -> onUninstallFinished(packageName, success = false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (!receiverRegistered) {

            val filter = IntentFilter(ACTION_UNINSTALL_RESULT)
            ContextCompat.registerReceiver(
                requireContext(),
                statusReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            receiverRegistered = true
        }
    }

    override fun onStart() {

        super.onStart()
        // Pause monitoring khi màn Xoá app đang hiển thị. Popup uninstall của hệ
        // thống là dialog-themed → không đẩy activity xuống STOPPED nên pause
        // vẫn còn hiệu lực trong lúc popup mở. Khi user rời màn (Home/Back),
        // onStop() release để monitor bảo vệ các màn khác như bình thường.
        AppMonitoringPauser.acquire(PAUSER_KEY)
    }

    override fun onStop() {

        super.onStop()
        AppMonitoringPauser.release(PAUSER_KEY)
    }

    override fun onResume() {

        super.onResume()
        // Fallback: nếu user đóng popup xoá bằng Back / tap outside và receiver
        // KHÔNG nhận status callback (đã thấy trên vài OEM), state sẽ kẹt:
        // currentUninstalling != null → startUninstallFlow bị guard chặn ở lần
        // bấm kế → user tưởng nút chết. Ở đây khi fragment resume mà đang chờ
        // popup đóng, tự dọn state + verify kết quả rồi launch popup kế trong queue.
        if (awaitingConfirmClose) {

            awaitingConfirmClose = false
            val processed = currentUninstalling
            currentUninstalling = null
            pendingQueue.removeFirstOrNull()

            if (processed != null && !isPackageInstalled(processed)) {

                viewModel.onPackageUninstalled(processed)
            }

            showNextUninstallDialog()
        }
    }

    override fun onDestroy() {

        if (receiverRegistered) {

            try {

                requireContext().unregisterReceiver(statusReceiver)
            } catch (_: Exception) {

                // Ignore — có thể bị unregister trước bởi Context bị destroy.
            }
            receiverRegistered = false
        }
        super.onDestroy()
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentUninstallAppListBinding {

        return FragmentUninstallAppListBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {

        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        binding.toolbar.ivLeft.setOnSafeClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.layoutSearch.etSearch.doAfterTextChanged { editable ->

            val text = editable?.toString() ?: ""
            viewModel.search(text)
            binding.layoutSearch.ivClear.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.layoutSearch.ivClear.setOnSafeClickListener {

            binding.layoutSearch.etSearch.text = null
        }

        binding.btnDelete.root.setOnSafeClickListener {

            startUninstallFlow()
        }
    }

    override fun observeData() {

        super.observeData()

        viewModel.background.filterNotNull().observe(this) { background ->

            val binding = binding ?: return@observe
            binding.root.setBackground(background)
        }

        viewModel.toolbar.observe(this) { state -> renderToolbar(state) }

        viewModel.searchState.observe(this) { state ->

            val binding = binding ?: return@observe
            binding.layoutSearch.root.setBackground(state.background)
            binding.layoutSearch.etSearch.hint = state.hint
            binding.layoutSearch.etSearch.setHintTextColor(state.hintColor)
            binding.layoutSearch.etSearch.setTextColor(state.textColor)
            state.clearIcon?.let { binding.layoutSearch.ivClear.setImage(it) }
        }

        viewModel.deleteAction.observe(this) { state ->

            val binding = binding ?: return@observe
            binding.btnDelete.tvAction.setText(state.text)
            binding.btnDelete.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
            binding.btnDelete.root.isEnabled = state.isEnabled
            binding.btnDelete.root.alpha = if (state.isEnabled) 1f else 0.5f
        }

        viewModel.items.attachAdapter().observe(this) { (items, adapters) ->

            val binding = binding ?: return@observe
            binding.rvUninstallList.submitListAndAwait(items, adapters, true)
        }

        AppEventBus.events.filterIsInstance<AppEvent.UninstallAppToggled>().observe(this) { event ->

            viewModel.toggle(event.entity)
        }
    }

    private fun renderToolbar(state: ToolbarState) {

        val binding = binding ?: return
        binding.toolbar.tvTitle.setText(state.title)
        val backIcon = state.backIcon
        binding.toolbar.ivLeft.visibility = if (backIcon != null) View.VISIBLE else View.GONE
        if (backIcon != null) binding.toolbar.ivLeft.setImage(backIcon)
    }

    // ── Sequential uninstall ────────────────────────────────────────────────

    private fun startUninstallFlow() {

        val selected = viewModel.getSelectedPackagesOrdered()
        if (selected.isEmpty()) {

            Toast.makeText(context, R.string.uninstall_apps_empty_error, Toast.LENGTH_SHORT).show()
            return
        }

        // Nếu đang có 1 luồng chạy dở → không cho user bấm phát nữa.
        if (currentUninstalling != null || pendingQueue.isNotEmpty()) return

        pendingQueue.clear()
        pendingQueue.addAll(selected)

        showNextUninstallDialog()
    }

    private fun showNextUninstallDialog() {

        val next = pendingQueue.firstOrNull()
        if (next == null) {

            currentUninstalling = null
            return
        }

        currentUninstalling = next

        val context = context ?: return
        val packageInstaller = context.packageManager.packageInstaller

        // Broadcast intent phải setPackage để chỉ receiver của app mình nhận —
        // đồng thời tương thích với Android 14+ (implicit broadcast requires target).
        val callback = Intent(ACTION_UNINSTALL_RESULT)
            .setPackage(context.packageName)
            .putExtra(EXTRA_TARGET_PACKAGE, next)

        // FLAG_MUTABLE cần cho API 31+ vì hệ thống ghi thêm EXTRA_STATUS vào intent này.
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {

            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, next.hashCode(), callback, flags)

        try {

            packageInstaller.uninstall(next, pendingIntent.intentSender)
        } catch (e: Exception) {

            // Không launch được (SecurityException, IllegalArgumentException…) → skip.
            Log.w(TAG, "packageInstaller.uninstall failed for $next: ${e.message}")
            onUninstallFinished(next, success = false)
        }
    }

    /**
     * Nhận từ [statusReceiver] khi status = [PackageInstaller.STATUS_PENDING_USER_ACTION].
     * Hệ thống đóng gói Intent xác nhận trong [Intent.EXTRA_INTENT] — mình chỉ việc
     * launch. Đây là popup do system dựng nên không bị ảnh hưởng bởi launchMode
     * `singleInstance` của MainActivity.
     */
    private fun launchConfirmIntent(intent: Intent, packageName: String?) {

        val confirm = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
        if (confirm == null) {

            Log.w(TAG, "STATUS_PENDING_USER_ACTION missing EXTRA_INTENT for $packageName")
            onUninstallFinished(packageName, success = false)
            return
        }

        try {

            confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            awaitingConfirmClose = true
            startActivity(confirm)
        } catch (e: Exception) {

            Log.w(TAG, "launch confirm intent failed for $packageName: ${e.message}")
            awaitingConfirmClose = false
            onUninstallFinished(packageName, success = false)
        }
    }

    /**
     * Được gọi khi popup terminal (SUCCESS / FAILURE / lỗi):
     * - success = true → verify package đã bị xoá → cập nhật selection.
     * - success = false → giữ nguyên selection.
     * Trong cả 2 trường hợp pop queue rồi launch popup kế tiếp.
     */
    private fun onUninstallFinished(packageName: String?, success: Boolean) {

        val processed = packageName ?: currentUninstalling
        currentUninstalling = null
        awaitingConfirmClose = false
        pendingQueue.removeFirstOrNull()

        if (processed != null && success && !isPackageInstalled(processed)) {

            viewModel.onPackageUninstalled(processed)
        }

        showNextUninstallDialog()
    }

    private fun isPackageInstalled(packageName: String): Boolean {

        val pm = context?.packageManager ?: return true
        return try {

            pm.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {

            false
        }
    }

    companion object {

        private const val TAG = "UninstallAppList"
        private const val ACTION_UNINSTALL_RESULT = "com.simple.launcher.retirement.UNINSTALL_RESULT"
        private const val EXTRA_TARGET_PACKAGE = "target_package"
        private const val PAUSER_KEY = "UninstallAppListFragment"
    }
}

@Deeplink
class UninstallAppListDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.UNINSTALL_APPS

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {

        val fragment = UninstallAppListFragment()

        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (extras?.get("addToBackStack") == true) {

            transaction.addToBackStack(DeepLinks.UNINSTALL_APPS)
        }

        transaction.commit()
        return true
    }
}
