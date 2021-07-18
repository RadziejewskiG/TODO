package com.radziejewskig.todo.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.radziejewskig.todo.di.InjectingSavedStateViewModelFactory
import com.radziejewskig.todo.extension.*
import com.radziejewskig.todo.feature.MainActivity
import com.radziejewskig.todo.utils.DialogDepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseFragment<BaseState: CommonState, BaseEvent: CommonEvent>(@LayoutRes layoutRes: Int): Fragment(layoutRes), HasDefaultViewModelProviderFactory {

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

    abstract val viewModel: FragmentViewModel<BaseState, BaseEvent>

    abstract val binding: ViewBinding

    open fun provideSnackbarAnchorView(): View? = null

    fun ac(): MainActivity? = (activity as MainActivity?)

    fun canNavigate() = ac()?.canNavigate() ?: true

    protected fun dialogDepository(): DialogDepository? = ac()?.dialogDepository

    open val isStatusBarLight: Boolean = true

    protected val currentState: BaseState by lazy { viewModel.currentState }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFragmentTransitions()

        getArgsData()

        viewModel.start()

        setupShouldPostpone()

        setupEvents()

        setupMessageEvent()

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

    private fun setupShouldPostpone() {
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
    }

    open fun setupFragmentTransitions() {
        enterTransition = null
        exitTransition = null
        reenterTransition = null
        returnTransition = null
    }

    /**
     * Use this method to get the data from args and (when needed) pass it to vm
     */
    open fun getArgsData() = Unit

    private fun setupEvents() {
        viewModel.events.collectWhenStartedAutoCancelling(
            lifecycleScope,
            lifecycle
        ) {
            handleSingleEvent(it.getContentIfNotHandled())
        }
    }

    private fun setupMessageEvent() {
        viewModel.messageEvent.collectWhenStartedAutoCancelling(
            lifecycleScope,
            lifecycle
        ) {
            it.getContentIfNotHandled()?.let { data ->
                showMessage(data.messageData)
            }
        }
    }

    open fun handleSingleEvent(event: BaseEvent?) = Unit

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

        ac()?.setLastNavComponentActionId(null)

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
