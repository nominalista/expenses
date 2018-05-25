package com.nominalista.expenses.infrastructure.utils

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class DataEvent<T> {

    private val relay = PublishRelay.create<T>()

    fun next(value: T) = relay.accept(value)

    @Suppress("HasPlatformType")
    fun toObservable() = (relay as Observable<T>).observeOn(AndroidSchedulers.mainThread())
}
