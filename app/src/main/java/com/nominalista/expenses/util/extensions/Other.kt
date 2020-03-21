package com.nominalista.expenses.util.extensions

private const val TAG_MAX_LENGTH = 23
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= TAG_MAX_LENGTH) tag else tag.substring(0, TAG_MAX_LENGTH)
    }