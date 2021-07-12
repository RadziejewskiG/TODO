package com.radziejewskig.todo.extension

import android.graphics.PorterDuff
import android.os.Bundle
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionSet
import android.transition.Visibility
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import com.radziejewskig.todo.R
import com.radziejewskig.todo.base.BaseFragment
import com.radziejewskig.todo.feature.list.ListFragment.Companion.RETURNED_SHARED_DATA
import com.radziejewskig.todo.utils.data.MessageData
import com.radziejewskig.todo.utils.data.MessageType
import com.radziejewskig.todo.utils.data.ReturnedTransitionData
import com.radziejewskig.todo.utils.data.getMessageString
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun BaseFragment.hideKeyboard() {
    ac()?.hideKeyboard()
}

fun BaseFragment.showKeyboard(view: View) = ac()?.showKeyboard(view)

val BaseFragment.viewLifecycleScope: LifecycleCoroutineScope
    get() = viewLifecycleOwner.lifecycleScope

fun <T> Fragment.getReturnedData(
    valueName: String,
    content: (T) -> Unit,
) {
    val backstackEntry = findNavController().currentBackStackEntry
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME && backstackEntry?.savedStateHandle?.contains(valueName) == true) {
            val value = backstackEntry.savedStateHandle.get<T>(valueName)
            if(value != null) {
                content(value)
                backstackEntry.savedStateHandle.set(valueName, null)
            }
        }
    }
    backstackEntry?.lifecycle?.addObserver(observer)
    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            backstackEntry?.lifecycle?.removeObserver(observer)
        }
    })
}

fun <T> Fragment.observeBackStackValue(
    valueName: String,
    content: (T) -> Unit,
) {
    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<T>(valueName)?.observe(viewLifecycleOwner, content)
}

fun <T> Fragment.sendBackStackValue(valueName: String, value: T) {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(valueName, value)
}

fun <T> Fragment.popWithData(valueName: String, value: T) {
    sendBackStackValue(valueName, value)
    findNavController().popBackStack()
}

fun BaseFragment.navigateSafe(
    directions: NavDirections,
    navOptions: NavOptions? = null,
    canNavigateTurnBackOnDelay: Long = 300
) {
    if(canNavigateSafe(directions, canNavigateTurnBackOnDelay)) {
        findNavController().navigate(directions, navOptions)
    }
}

fun BaseFragment.navigateSafe(
    @IdRes actionId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    canNavigateTurnBackOnDelay: Long = 300
): Boolean {
    return if(canNavigateSafe(actionId, canNavigateTurnBackOnDelay)) {
        findNavController().navigate(actionId, args, navOptions)
        true
    } else false
}

private fun BaseFragment.canNavigateSafe(
    @IdRes resId: Int,
    canNavigateTurnBackOnDelay: Long = 300
): Boolean {
    return if(
        (ac()?.lastNavComponentActionId == null || ac()?.lastNavComponentActionId != resId) &&
        ac()?.canNavigate() == true
    ) {
        if(canNavigateTurnBackOnDelay > 0) {
            ac()?.setCanNavigate(false, canNavigateTurnBackOnDelay)
        }
        ac()?.lastNavComponentActionId = resId
        true
    } else false
}

private fun BaseFragment.canNavigateSafe(
    directions: NavDirections,
    canNavigateTurnBackOnDelay: Long = 300
): Boolean {
    return if(
        findNavController().currentDestination?.getAction(directions.actionId) != null &&
        (ac()?.lastNavComponentActionId == null || ac()?.lastNavComponentActionId != directions.actionId) &&
        ac()?.canNavigate() == true
    ) {
        if(canNavigateTurnBackOnDelay > 0) {
            ac()?.setCanNavigate(false, canNavigateTurnBackOnDelay)
        }
        ac()?.lastNavComponentActionId = directions.actionId
        true
    } else false
}

@ColorInt
fun Fragment.getColorFromAttr(@AttrRes attrColor: Int) = requireContext().getColorFromAttr(attrColor)

@ColorInt
fun Fragment.getColor(@ColorRes colorRes: Int) = requireContext().getColorFromRes(colorRes)

fun BaseFragment.navigateSharedElements(
    directions: NavDirections,
    transitionOnExit: Transition? = Fade().apply { duration = 250 },
    shouldPostponeOnReturn: Boolean = true,
    transitionNamesWithViews: List<Pair<String, View>>,
) {
    if(canNavigateSafe(directions.actionId, 0)) {
        ac()?.setCanNavigate(false)

        viewModel.shouldPostponeOnReturn = shouldPostponeOnReturn
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
                names.clear()
                sharedElements.clear()
                transitionNamesWithViews.forEach {
                    names.add(it.first)
                    sharedElements[it.first] = it.second
                }
            }
        })
        exitTransition = transitionOnExit?.apply {
            transitionNamesWithViews.map { it.second }.forEach {
                excludeTarget(it, true)
            }
        }
        findNavController().navigate(directions, FragmentNavigatorExtras(*transitionNamesWithViews.map { it.second to it.first }.toTypedArray()))
    }
}

fun BaseFragment.setupSharedElements(
    // THIS MUST BE android.transition AND NOT androidx.transition!!!
    transitionSet: TransitionSet,
    transitionOnReturn: Transition? = Fade().apply {
        duration = 250
        mode = Visibility.MODE_OUT
    },
    shouldPostponeOnEnter: Boolean = true,
    onTransitionEnded: () -> Unit = {},
    onMapSharedElements: (MutableList<String>, MutableMap<String, View>, Transition?) -> Unit
) {
    if(shouldPostponeOnEnter) {
        if(!viewModel.enterTransitionAlreadyPostponed) {
            viewModel.enterTransitionAlreadyPostponed = true
            viewModel.shouldPostponeOnEnter = true
        }
    }

    setEnterSharedElementCallback(object : SharedElementCallback() {
        override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
            onMapSharedElements(names, sharedElements, transitionOnReturn)
        }
    })

    sharedElementEnterTransition = transitionSet.apply {
        addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition?) {}
            override fun onTransitionEnd(transition: Transition?) {
                onTransitionEnded()
                ac()?.setCanNavigate(true)
                removeListener(this)
            }
            override fun onTransitionCancel(transition: Transition?) {}
            override fun onTransitionPause(transition: Transition?) {}
            override fun onTransitionResume(transition: Transition?) {}
        })
    }
}

@ExperimentalCoroutinesApi
fun BaseFragment.setupReturnedContinuousSharedElements(
    transitionOnReturn: Transition = Fade().apply { duration = 250 },
    dataReturned: (ReturnedTransitionData) -> Unit = {},
    onMapSharedElements: (ReturnedTransitionData, MutableList<String>, MutableMap<String, View>) -> Unit
) = getReturnedData<ReturnedTransitionData>(RETURNED_SHARED_DATA) { data ->
    ac()?.setCanNavigate(false)
    dataReturned(data)

    setExitSharedElementCallback(object : SharedElementCallback() {
        override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
            onMapSharedElements(data, names, sharedElements)
        }
    })
    reenterTransition = transitionOnReturn.apply {
        runCatching {
            addListener(object : Transition.TransitionListener {
                override fun onTransitionStart(transition: Transition?) {}
                override fun onTransitionEnd(transition: Transition?) {
                    ac()?.setCanNavigate(true)
                    removeListener(this)
                    setExitSharedElementCallback(null)
                }
                override fun onTransitionCancel(transition: Transition?) {}
                override fun onTransitionPause(transition: Transition?) {}
                override fun onTransitionResume(transition: Transition?) {}
            })
        }
    }
}

@ExperimentalCoroutinesApi
fun BaseFragment.popSharedElements(
    value: String,
) {
    if(canNavigate()) {
        ac()?.setCanNavigate(false)
        sendBackStackValue(
            RETURNED_SHARED_DATA,
            ReturnedTransitionData(
                value
            )
        )
        findNavController().popBackStack()
    }
}

fun BaseFragment.showMessage(messageData: MessageData) {

    val context = requireContext()

    val bgColor = when (messageData.type) {
        MessageType.ERROR -> R.color.red
        MessageType.SUCCESS -> R.color.green
        MessageType.WARNING -> R.color.beige
    }

    val defaultMessage = when (messageData.type) {
        MessageType.ERROR -> getString(R.string.an_unknown_error_occurred)
        MessageType.SUCCESS -> getString(R.string.success)
        MessageType.WARNING -> getString(R.string.warning)
    }

    val message = messageData.getMessageString(context)

    val snackbar = Snackbar.make(
        provideSnackbarAnchorView() ?: binding.root,
        if (message.isEmpty()) defaultMessage else message,
        if (messageData.durationShort) Snackbar.LENGTH_SHORT else Snackbar.LENGTH_LONG
    )

    val drawable = when(messageData.type) {
        MessageType.ERROR -> ContextCompat.getDrawable(context, R.drawable.error_icon)
        MessageType.SUCCESS -> ContextCompat.getDrawable(context, R.drawable.check_icon)
        MessageType.WARNING -> ContextCompat.getDrawable(context, R.drawable.warning_icon)
    }

    drawable?.setTint(context.getColorFromRes(android.R.color.white))
    drawable?.setTintMode(PorterDuff.Mode.SRC_ATOP)

    snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
    snackbar.setBackgroundTint(context.getColorFromRes(bgColor))

    val tv = snackbar.view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
    tv.setTextColor(context.getColorFromRes(android.R.color.white))
    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    drawable?.let {
        val img = ImageView(context)
        img.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val layImageParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        img.setImageDrawable(it)

        (tv.parent as SnackbarContentLayout).addView(img, 0, layImageParams)
    }

    hideKeyboard()

    snackbar.show()
}
