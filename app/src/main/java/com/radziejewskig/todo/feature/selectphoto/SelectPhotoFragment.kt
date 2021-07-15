package com.radziejewskig.todo.feature.selectphoto

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.radziejewskig.todo.R
import com.radziejewskig.todo.appComponent
import com.radziejewskig.todo.base.BaseFragment
import com.radziejewskig.todo.base.CommonEvent
import com.radziejewskig.todo.databinding.FragmentSelectPhotoBinding
import com.radziejewskig.todo.extension.collectLatestWhenStarted
import com.radziejewskig.todo.extension.popWithData
import com.radziejewskig.todo.extension.viewLifecycleScope
import com.radziejewskig.todo.feature.addedit.AddEditFragment
import com.radziejewskig.todo.utils.viewBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class SelectPhotoFragment: BaseFragment<CommonEvent>(R.layout.fragment_select_photo) {

    override val binding by viewBinding(FragmentSelectPhotoBinding::bind)

    override val viewModel: SelectPhotoFragmentViewModel by viewModels()

    override fun injectDaggerDependencies() {
        appComponent.inject(this)
    }

    override var isStatusBarLight: Boolean = false

    private val photosAdapter = PhotoAdapter(
        ::photoClicked
    )

    private lateinit var gridLayoutManager: GridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridLayoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
        binding.run {
            with(photosRv) {
                layoutManager = gridLayoutManager
                adapter = photosAdapter.apply {
                    stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                }
            }
            viewModel.photos.collectLatestWhenStarted(viewLifecycleScope) { urls ->
                photosAdapter.submitList(urls)
            }
        }
    }

    private fun photoClicked(imageUrl: String) {
        popWithData(
            AddEditFragment.IMAGE_URL_ARG,
            imageUrl
        )
    }

    override fun windowInsetsChanged(navigationBarHeight: Int, statusBarHeight: Int) {
        super.windowInsetsChanged(navigationBarHeight, statusBarHeight)
        binding.run {
            container.updatePadding(bottom = navigationBarHeight)
            topLl.updatePadding(top = statusBarHeight + resources.getDimensionPixelSize(R.dimen.dp16))
        }
    }

}
