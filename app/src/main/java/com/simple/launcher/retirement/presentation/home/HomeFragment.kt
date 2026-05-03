package com.simple.launcher.retirement.presentation.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.domain.model.HomeItem
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase
import com.simple.launcher.retirement.presentation.settings.SettingsFragment

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels {
        val repository = AppRepositoryImpl(requireContext())
        val getHomeAppsUseCase = GetHomeAppsUseCase(repository)
        HomeViewModelFactory(getHomeAppsUseCase)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvApps = view.findViewById<RecyclerView>(R.id.rvApps)
        val btnSettings = view.findViewById<ImageButton>(R.id.btnSettings)

        btnSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            rvApps.adapter = HomeAdapter(items) { item ->
                when (item) {
                    is HomeItem.App -> {
                        val launchIntent = requireContext().packageManager.getLaunchIntentForPackage(item.entity.packageName)
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        }
                    }
                    is HomeItem.Contact -> {
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:${item.entity.phoneNumber}")
                        try {
                            startActivity(callIntent)
                        } catch (e: Exception) {
                            val dialIntent = Intent(Intent.ACTION_DIAL)
                            dialIntent.data = Uri.parse("tel:${item.entity.phoneNumber}")
                            startActivity(dialIntent)
                        }
                    }
                }
            }
        }

        viewModel.loadApps()
    }
}
