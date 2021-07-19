package com.radziejewskig.todo.feature.list

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.radziejewskig.todo.R
import com.radziejewskig.todo.appComponent
import com.radziejewskig.todo.base.BaseFragment
import com.radziejewskig.todo.base.CommonEvent
import com.radziejewskig.todo.data.model.Task
import com.radziejewskig.todo.databinding.FragmentListBinding
import com.radziejewskig.todo.extension.*
import com.radziejewskig.todo.feature.addedit.AddEditFragmentArgs
import com.radziejewskig.todo.utils.data.TaskListItemModel
import com.radziejewskig.todo.utils.viewBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ListFragment: BaseFragment<ListFragmentState, CommonEvent>(R.layout.fragment_list) {

    override val binding by viewBinding(FragmentListBinding::bind)

    override val viewModel: ListFragmentViewModel by viewModels()

    override fun provideSnackbarAnchorView() = binding.fab

    override fun injectDaggerDependencies() {
        appComponent.inject(this)
    }

    private val taskAdapter = TaskAdapter(
        onItemClicked = { t, v ->
           itemClicked(t, v)
        },
        ::itemLongClicked,
        ::itemCheckboxClicked
    )

    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.run {
            with(rv) {
                layoutManager = linearLayoutManager

                if (itemDecorationCount > 0) {
                    removeItemDecorationAt(0)
                }
                ContextCompat.getDrawable(requireContext(), R.drawable.divider_shape)?.let {
                    val decorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
                        setDrawable(it)
                    }
                    addItemDecoration(decorator)
                }

                adapter = taskAdapter.apply {
                    stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                }
                addOnScrollListener(object: RecyclerView.OnScrollListener()  {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)

                        val scrolledToBottom = !recyclerView.canScrollVertically(1)
                        val canScrollTop = recyclerView.canScrollVertically(-1)

                        if (scrolledToBottom && canScrollTop && newState == RecyclerView.SCROLL_STATE_IDLE) {
                            viewModel.tryLoadNewPage()
                        }
                    }
                })

                itemAnimator = DefaultItemAnimator()
            }
            fab.setOnClickListener { fabClicked() }
            topBar.btnRefresh.setOnClickListener { refreshClicked() }
        }

        viewModel.tasks.collectLatestWhenStarted(viewLifecycleScope) { tasks ->
            with(binding) {
                taskAdapter.submitList(tasks) {
                    if (!viewModel.loadingBarShowing) {
                        viewModel.resetLoadingStates()
                        val changedItemId = viewModel.changedItemId
                        if(changedItemId != null) {
                            viewModel.resetChangedItemId()
                            tasks.firstOrNull { it is TaskListItemModel.TaskItem && it.task.id == changedItemId }?.let { item ->
                                val position = tasks.indexOf(item)
                                taskAdapter.notifyItemChanged(position, true)
                            }
                        }
                    } else {
                        // Scroll rv to the bottom to fully show loading bar
                        rv.stopScroll()
                        rv.scrollBy(0, 250)
                    }
                }
                if(viewModel.canShowEmptyImage) {
                    emptyLl.alpha = if(tasks.isEmpty()) 1f else 0f
                } else {
                    emptyLl.alpha = 0f
                    viewModel.setCanShowEmptyImage(true)
                }
            }
        }

        viewModel.state.collectLatestWhenStartedAutoCancelling(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state.isUpdating
        }

    }

    private fun itemLongClicked(task: Task) {
        if(!currentState().isUpdating) {
            dialogDepository()?.showInfoDialog(
                title = getString(R.string.deleting_task_title),
                message = getString(R.string.deleting_task_message, task.title),
                okBtnText = getString(R.string.delete),
                cancelBtnText = getString(R.string.cancel),
            ) {
                viewModel.deleteTask(task)
            }
        }
    }

    private fun itemCheckboxClicked(task: Task) {
        viewModel.changeTaskIsCompleted(task)
    }

    private fun itemClicked(task: Task, view: View) {
        if(!currentState().isUpdating) {
            navigateSharedElements(
                directions = ListFragmentDirections.actionListToAddeditNoAnim(task.copy()),
                transitionNamesWithViews = listOf(view.transitionName to view)
            )
        }
    }

    override fun getArgsData() {
        super.getArgsData()
        setupReturnedContinuousSharedElements(
            onMapSharedElements = { data, sharedElementsNames, sharedElements ->
                val transitionNamesWithViews: MutableList<Pair<String, View>> =
                    try {
                        val list = mutableListOf<Pair<String, View>>()
                        viewModel.previousList.firstOrNull { it.id == data.value }?.let { task ->
                            viewModel.previousList.indexOf(task).let { position ->
                                binding.rv.findViewHolderForAdapterPosition(position)?.itemView?.let {
                                    val containerView = it.findViewById(R.id.taskItemContent) as View
                                    list.add(containerView.transitionName to containerView)
                                }
                            }
                        }
                        list
                    } catch (e: Exception) {
                        mutableListOf()
                    }
                transitionNamesWithViews.forEachIndexed { index, pair ->
                    sharedElements[sharedElementsNames[index]] = pair.second
                }
            }
        )
    }

    private fun fabClicked() {
        if(!currentState().isUpdating) {
            binding.rv.stopScroll()
            navigateSafe(
                R.id.action_listFragment_to_addEditFragment,
                AddEditFragmentArgs(null).toBundle()
            )
        }
    }

    private fun refreshClicked() {
        binding.rv.stopScroll()
        viewModel.refresh()
    }

    override fun windowInsetsChanged(navigationBarHeight: Int, statusBarHeight: Int) {
        super.windowInsetsChanged(navigationBarHeight, statusBarHeight)
        binding.run {
            container.updatePadding(bottom = navigationBarHeight)
            topBar.content.updatePadding(top = statusBarHeight + resources.getDimensionPixelSize(R.dimen.dp16))
        }
    }

    companion object {
        const val RETURNED_SHARED_DATA = "returned_shared_data"
    }

}
