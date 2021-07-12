package com.radziejewskig.todo.feature

import androidx.activity.viewModels
import com.radziejewskig.todo.R
import com.radziejewskig.todo.appComponent
import com.radziejewskig.todo.base.BaseActivity
import com.radziejewskig.todo.databinding.ActivityMainBinding
import com.radziejewskig.todo.utils.viewBinding

class MainActivity: BaseActivity() {

    override val binding by viewBinding(ActivityMainBinding::inflate)
    override val viewModel: MainActivityViewModel by viewModels()

    override fun injectDaggerDependencies() {
        appComponent.inject(this)
    }

    override fun themeToOverride(): Int = R.style.Theme_App_TranslucentStatus

    fun navigationAndStatusBarHeight() = viewModel.navigationAndStatusBarHeight

    override fun windowInsetsChanged(navigationBarHeight: Int, statusBarHeight: Int) {
        super.windowInsetsChanged(navigationBarHeight, statusBarHeight)
        viewModel.navigationAndStatusBarHeight.value = navigationBarHeight to statusBarHeight
    }

}
