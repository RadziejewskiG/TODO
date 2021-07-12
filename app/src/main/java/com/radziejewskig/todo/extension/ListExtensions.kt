package com.radziejewskig.todo.extension

infix fun <T> List<T>?.sameContentAs(otherList: List<T>?): Boolean = when {
    this == null && otherList == null -> true
    (this == null && otherList != null) || (otherList == null && this != null) -> false
    else -> this!!.size == otherList!!.size && this.containsAll(otherList)
}