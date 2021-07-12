package com.radziejewskig.todo.utils

import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST")
object CommonViewHelper {
    fun <Type> getTypeArgument(genericTarget: Class<*>, clazz: Class<Type>) =
        (genericTarget.genericSuperclass as ParameterizedType)
            .actualTypeArguments
            .asIterable()
            .first { clazz.isAssignableFrom(it as Class<Type>) }
                as Class<Type>
}
