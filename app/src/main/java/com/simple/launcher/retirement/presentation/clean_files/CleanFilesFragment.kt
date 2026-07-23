package com.simple.launcher.retirement.presentation.clean_files

/*class CleanFilesFragment : BaseFragment<FragmentCleanFilesBinding>() {

    private val viewModel: CleanFilesViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCleanFilesBinding {
        return FragmentCleanFilesBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        binding.toolbar.ivLeft.setOnSafeClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnClean.ivAction.isVisible = true
        binding.btnClean.tvAction.updatePadding(top = DP.DP_24, bottom = DP.DP_24)
        binding.btnClean.root.setOnSafeClickListener {

            viewModel.startScan()
        }
    }

    override fun observeData() {
        super.observeData()
        with(viewModel) {
            background.observe(this@CleanFilesFragment) { background ->
                val binding = binding ?: return@observe
                binding.root.setBackground(background)
            }

            toolbar.observe(this@CleanFilesFragment) { state ->
                val binding = binding ?: return@observe

                binding.toolbar.tvTitle.setText(state.title)

                val backIcon = state.backIcon
                if (backIcon != null) {
                    binding.toolbar.ivLeft.visibility = View.VISIBLE
                    binding.toolbar.ivLeft.setImage(backIcon)
                } else {
                    binding.toolbar.ivLeft.visibility = View.GONE
                }
            }

            action.observe(this@CleanFilesFragment) { state ->
                val binding = binding ?: return@observe

                binding.btnClean.tvAction.setText(state.text)
                binding.btnClean.ivAction.setImage(state.image)
                binding.btnClean.tvAction.parent.asObjectOrNull<View>()?.setBackground(state.background)
            }

            screenState.observe(this@CleanFilesFragment) {
                val binding = binding ?: return@observe

                binding.scannerRing.ringState = if (it is CleanFilesViewModel.ClearState.IDLE) {
                    RingState.IDLE
                } else if (it is CleanFilesViewModel.ClearState.Scanning) {
                    RingState.SCANNING
                } else {
                    RingState.DONE
                }

                if (it !is CleanFilesViewModel.ClearState.Done) return@observe

                delay(1000)

                binding.animationView.isVisible = true
                binding.animationView.playAnimation()
            }

            screenViewData.observe(this@CleanFilesFragment) {
                val binding = binding ?: return@observe

                TransitionManager.beginDelayedTransition(binding.frameContent, AutoTransition())

                bindingRing(it.ringViewData)

                binding.tvStatus.setText(it.status)

                bindingCategory(it.categoryViewDataList)
                bindingResult(it.resultViewData)
            }
        }
    }

    private fun bindingRing(ringViewData: CleanFilesViewModel.RingViewData) {
        val binding = binding ?: return
        binding.ivRingIcon.setImage(ringViewData.icon)
        binding.ivRingIcon.isVisible = ringViewData.showIcon

        binding.tvRingCount.setText(ringViewData.text)
        binding.tvRingCount.isVisible = !ringViewData.showIcon
    }

    private fun bindingCategory(categoryViewDataList: List<CleanFilesViewModel.CategoryViewData>) {
        val binding = binding ?: return
        categoryViewDataList.forEachIndexed { index, data ->

            val itemBinding = if (binding.llCategories.childCount == categoryViewDataList.size) {
                ItemCleanCategoryBinding.bind(binding.llCategories.getChildAt(index))
            } else {
                ItemCleanCategoryBinding.inflate(LayoutInflater.from(requireContext()), binding.llCategories, true)
            }

            itemBinding.ivCatIcon.setImage(data.image)
            itemBinding.ivCatIcon.setBackground(data.imageBackground)
            itemBinding.tvCatName.setText(data.label)
            itemBinding.tvCatCount.setText(data.numberFile)
            itemBinding.ivCatCheck.isVisible = data.showSelected
        }
    }

    private fun bindingResult(resultViewData: CleanFilesViewModel.ResultViewData) {
        val binding = binding ?: return
        binding.cardResult.isVisible = resultViewData.show

        binding.tvResultTitle.setText(resultViewData.title)

        binding.tvResultFiles.setText(resultViewData.resultFilesLabel)
        binding.ivResultFiles.setImage(resultViewData.resultFilesImage)
        binding.frameResultFiles.setBackground(resultViewData.resultFilesBackground)

        binding.tvResultSpace.setText(resultViewData.resultSpaceLabel)
        binding.ivResultSpace.setImage(resultViewData.resultSpaceImage)
        binding.frameResultSpace.setBackground(resultViewData.resultSpaceBackground)
    }
}*/

/*
@Deeplink
class CleanFilesDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.CLEAN_FILES

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CleanFilesFragment())

        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
        return true
    }
}
*/
