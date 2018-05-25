package com.nominalista.expenses.infrastructure.utils

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

class Variable<T>(defaultValue: T) {

    var value: T
        get() = relay.value
        set(value) = relay.accept(value)

    private val relay = BehaviorRelay.createDefault(defaultValue)

    @Suppress("HasPlatformType")
    fun toObservable() = (relay as Observable<T>).observeOn(mainThread())
}