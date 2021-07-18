package com.radziejewskig.todo.feature.addedit

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.radziejewskig.todo.R
import com.radziejewskig.todo.appComponent
import com.radziejewskig.todo.base.BaseFragment
import com.radziejewskig.todo.databinding.FragmentAddEditBinding
import com.radziejewskig.todo.extension.*
import com.radziejewskig.todo.feature.list.TaskAdapter.Companion.getTaskEditSharedElementName
import com.radziejewskig.todo.utils.viewBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

@ExperimentalCoroutinesApi
class AddEditFragment: BaseFragment<AddEditFragmentState, AddEditTaskSingleEvent>(R.layout.fragment_add_edit) {

    override val binding by viewBinding(FragmentAddEditBinding::bind)

    override val viewModel: AddEditFragmentViewModel by viewModels()

    private val args: AddEditFragmentArgs by navArgs()

    override fun injectDaggerDependencies() {
        appComponent.inject(this)
    }

    private var iconUrlDebounceJob: Job? = null

    private var fragmentInitialized = AtomicBoolean(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {

            container.setOnClickListener {
                hideKeyboard()
            }

            if(!currentState.isEditing && !currentState.changeOccurred) {
                etTitle.requestFocus()
                showKeyboard(etTitle)
            }

            etTitle.doAfterTextChanged {
                viewModel.titleChanged(it?.toString() ?: "")
            }

            etIcon.doAfterTextChanged {
                val text = if(it == null || it.isEmpty()) null else it.toString()
                iconUrlDebounceJob?.cancel()
                iconUrlDebounceJob = launchLifecycleScopeWhenStarted {
                    delay(300)
                    viewModel.iconUrlChanged(text)
                }
            }

            etDescription.doAfterTextChanged {
                val text = if(it == null || it.isEmpty()) null else it.toString()
                viewModel.descriptionChanged(text)
            }

            tilTitle.counterMaxLength = TITLE_MAX_LENGTH
            tilDescription.counterMaxLength = DESCRIPTION_MAX_LENGTH

            btnSave.setOnClickListener {
                hideKeyboard()
                viewModel.addEditTask()
            }

            btnMakePhoto.setOnClickListener {
                hideKeyboard()
                navigateSafe(R.id.action_addEditFragment_to_takePhotoFragment)
            }

            btnSelectPhoto.setOnClickListener {
                hideKeyboard()
                navigateSafe(R.id.action_addEditFragment_to_selectPhotoFragment)
            }
        }

        viewModel.state.collectLatestWhenStartedAutoCancelling(viewLifecycleOwner) { state ->
            val task = state.task
            binding.run {
                btnSave.isEnabled = state.changeOccurred
                container.transitionName = getTaskEditSharedElementName(task)

                titleTv.text = getString(if(state.isEditing) R.string.edit_task else R.string.add_task)

                if (fragmentInitialized.compareAndSet(false, true)) {
                    etTitle.setTextIfDiffers(task.title)
                    etIcon.setTextIfDiffers(task.iconUrl)
                    etDescription.setTextIfDiffers(task.description)
                }

                taskIcon.loadImage(
                    context = requireContext(),
                    imageUrl = task.iconUrl
                ) {
                    startPostponedEnterTransition()
                }

                tilTitle.error = null
                tilDescription.error = null
                tilIcon.error = null
            }
        }
    }

    override fun handleSingleEvent(event: AddEditTaskSingleEvent?) {
        super.handleSingleEvent(event)
        when(event) {
            is AddEditTaskSingleEvent.ShowErrors -> {
                checkFields(event.editTaskErrors)
            }
            is AddEditTaskSingleEvent.NavigateBack -> {
                navigateBack()
            }
        }
    }

    private fun checkFields(data: EditTaskErrors) {
        binding.tilTitle.error =
            when {
                data.showTitleEmptyError -> {
                    getString(R.string.title_empty_error)
                }
                data.showTitleTooLongError -> {
                    getString(R.string.title_too_long_error)
                }
                else -> null
            }
        binding.tilDescription.error =
            if(data.showDescriptionTooLongError) {
                getString(R.string.description_too_long_error)
            } else null
    }

    private fun showChangeOccurredDialog() {
        dialogDepository()?.showInfoDialog(
            title = getString(R.string.discarding_changes_title),
            message = getString(R.string.discarding_changes_message),
            okBtnText = getString(R.string.yes),
            cancelBtnText = getString(R.string.no),
        ) {
            navigateBack()
        }
    }

    private fun navigateBack() {
        hideKeyboard()
        if(currentState.isEditing) {
            popSharedElements()
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onBackPressed(): Boolean {
        if(currentState.changeOccurred) {
            showChangeOccurredDialog()
        } else {
            navigateBack()
        }
        return true
    }

    private fun popSharedElements() {
        val id = currentState.task.id
        popSharedElements(id)
    }

    override fun getArgsData() {
        viewModel.setup(args.task)
        if(args.task != null) {
            setupSharedElements(
                transitionSet = SharedElementsOptions(
                    duration = SHARED_ELEMENTS_ENTER_DURATION,
                    transitions = listOf(SharedTransition.CHANGE_TRANSFORM, SharedTransition.CHANGE_BOUNDS, SharedTransition.CHANGE_CLIP_BOUNDS)
                ).toTransitionSet(),
            ) { sharedElementsNames, sharedElements, transitionOnReturn ->
                runCatching {
                    val transitionNamesWithViews: List<Pair<String, View>> = listOf(binding.container.transitionName to binding.container)

                    transitionNamesWithViews.forEachIndexed { index, pair ->
                        sharedElements[sharedElementsNames[index]] = pair.second
                    }

                    returnTransition = transitionOnReturn?.apply {
                        transitionNamesWithViews.map { it.second }.forEach {
                            excludeTarget(it, true)
                        }
                    }
                }
            }
        }
        getReturnedData<String>(IMAGE_URL_ARG) {
            viewModel.iconUrlChanged(it)
        }
    }

    override fun windowInsetsChanged(navigationBarHeight: Int, statusBarHeight: Int) {
        super.windowInsetsChanged(navigationBarHeight, statusBarHeight)
        binding.run {
            container.updatePadding(bottom = navigationBarHeight, top = statusBarHeight)
        }
    }

    override fun onDestroyView() {
        iconUrlDebounceJob?.cancel()
        super.onDestroyView()
    }

    companion object {
        const val IMAGE_URL_ARG = "image_url_arg"

        const val TITLE_MAX_LENGTH = 30
        const val DESCRIPTION_MAX_LENGTH = 200

        private const val SHARED_ELEMENTS_ENTER_DURATION: Long = 300
    }

}
