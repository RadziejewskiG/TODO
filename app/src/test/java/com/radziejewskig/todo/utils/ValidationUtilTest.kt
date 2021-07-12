package com.radziejewskig.todo.utils

import com.google.common.truth.Truth.assertThat
import com.radziejewskig.todo.data.model.Task
import org.junit.Test

class ValidationUtilTest {

    @Test
    fun `valid task returns true`() {
        val result = ValidationUtil.checkTaskValid(
            Task(
                id = "4fm84jds",
                title = "Some title",
                description = "Some description",
                iconUrl = "something.com/some.jpeg",
                completed = false,
                createdAt = null
            )
        )
        assertThat(result).isTrue()
    }

    @Test
    fun `empty task title returns false`() {
        val result = ValidationUtil.checkTaskValid(
            Task(
                id = "4fm84jds",
                title = "",
                description = "Some description",
                iconUrl = "something.com/some.jpeg",
                completed = false,
                createdAt = null
            )
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `too long task title returns false`() {
        val result = ValidationUtil.checkTaskValid(
            Task(
                id = "4fm84jds",
                title = "This title is too long to pass the test",
                description = "Some description",
                iconUrl = "something.com/some.jpeg",
                completed = false,
                createdAt = null
            )
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `too long task description returns false`() {
        val result = ValidationUtil.checkTaskValid(
            Task(
                id = "4fm84jds",
                title = "Some title",
                description = """
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                    Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
                    Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
                    Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
                """.trimIndent(),
                iconUrl = "something.com/some.jpeg",
                completed = false,
                createdAt = null
            )
        )
        assertThat(result).isFalse()
    }

}
