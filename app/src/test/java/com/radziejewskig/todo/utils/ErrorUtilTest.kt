package com.radziejewskig.todo.utils

import com.google.common.truth.Truth.assertThat
import com.radziejewskig.todo.R
import org.junit.Test

class ErrorUtilTest {

    @Test
    fun `unhandled throwable returns an_unknown_error_occurred string resource`() {
        val throwable = Throwable("TEST Unknown throwable")
        val result = ErrorUtil.getStringResForException(throwable)
        assertThat(result).isEqualTo(R.string.an_unknown_error_occurred)
    }

    @Test
    fun `null throwable returns an_unknown_error_occurred string resource`() {
        val throwable = null
        val result = ErrorUtil.getStringResForException(throwable)
        assertThat(result).isEqualTo(R.string.an_unknown_error_occurred)
    }

}
