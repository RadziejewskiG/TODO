package com.radziejewskig.todo.base

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.radziejewskig.todo.di.InjectingSavedStateViewModelFactory
import com.radziejewskig.todo.extension.*
import com.radziejewskig.todo.feature.MainActivity
import com.radziejewskig.todo.utils.DialogDepository
import com.radziejewskig.todo.utils.data.MessageData
import com.zhuinden.liveeventsample.utils.observe
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseFragment(@LayoutRes layoutRes: Int): Fragment(layoutRes), HasDefaultViewModelProviderFactory {

    /**
     * dagger.Lazy used here, so that injection is request when [getDefaultViewModelProviderFactory] is called
     */
    @Inject
    lateinit var defaultViewModelFactory: dagger.Lazy<InjectingSavedStateViewModelFactory>

    /**
     * This method androidx uses for `by viewModels` method.
     * We can set out injecting factory here and therefore don't touch it again
     */
    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory = defaultViewModelFactory.get().create(this, arguments)

    abstract fun injectDaggerDependencies()

    abstract val viewModel: FragmentViewModel<out CommonState>

    abstract val binding: ViewBinding

    open fun provideSnackbarAnchorView(): View? = null

    fun ac(): MainActivity? = (activity as MainActivity?)

    fun canNavigate() = ac()?.canNavigate() ?: true
    fun canNavigateFlow() = ac()?.canNavigateFlow()

    protected fun dialogDepository(): DialogDepository? = ac()?.dialogDepository

    open var isStatusBarLight: Boolean = true

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFragmentTransitions()

        getArgsData()

        viewModel.start()

        if(viewModel.shouldPostponeOnReturn) {
            viewModel.shouldPostponeOnReturn = false
            /** Start postponed transition after 300 ms no matter what - this prevents app from being paused for too long */
            postponeEnterTransition(300, TimeUnit.MILLISECONDS)
        } else if(viewModel.enterTransitionAlreadyPostponed && viewModel.shouldPostponeOnEnter) {
            viewModel.shouldPostponeOnEnter = false
            /** Start postponed transition after 300 ms no matter what - this prevents app from being paused for too long */
            postponeEnterTransition(300, TimeUnit.MILLISECONDS)
        } else{
            startPostponedEnterTransition()
        }

        setupEvents()

        viewModel.isProcessingData.collectLatestWhenStarted(viewLifecycleScope) { isProcessing ->
            if(isProcessing) {
                showLoadingDialog()
            } else{
                hideDialog()
            }
        }

        ac()?.navigationAndStatusBarHeight()?.collectLatestWhenStarted(viewLifecycleScope) { navigationAndStatusBarHeight ->
            windowInsetsChanged(navigationAndStatusBarHeight.first, navigationAndStatusBarHeight.second)
        }
    }

    open fun setupFragmentTransitions() {
        enterTransition = null
        exitTransition = null
        reenterTransition = null
        returnTransition = null
    }

    /**
     * Use this to get data from args and (when needed) pass it to vm
     */
    open fun getArgsData() = Unit

    private fun setupEvents() {
        viewModel.events.observe(viewLifecycleOwner) {
            handleSingleEvent(it.getContentIfNotHandled(), it.data)
        }
    }

    @CallSuper
    open fun handleSingleEvent(singleEvent: SingleEvent?, data: Parcelable) {
        when(singleEvent) {
            CommonFragmentSingleEvent.SHOW_MESSAGE -> {
                val showMessageData = data as MessageData
                showMessage(showMessageData)
            }
        }
    }

    fun showLoadingDialog() {
        dialogDepository()?.showLoadingDialog()
    }

    fun hideDialog() {
        dialogDepository()?.hideDialog()
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        runCatching { viewModel.saveToBundle() }
        super.onSaveInstanceState(outState)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()

        ac()?.setLightStatusBarAppearance(isStatusBarLight)

        ac()?.lastNavComponentActionId = null

        launchLifecycleScopeWhenStarted {
            if(viewModel.isProcessingData.value) {
                showLoadingDialog()
            }
        }
    }

    open fun onBackPressed(): Boolean = false

    open fun windowInsetsChanged(navigationBarHeight: Int, statusBarHeight: Int) = Unit

    @CallSuper
    override fun onDestroyView() {
        hideDialog()
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        injectDaggerDependencies()
        super.onAttach(context)
    }

}
