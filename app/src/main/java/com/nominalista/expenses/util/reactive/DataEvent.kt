package com.nominalista.expenses.util.reactive

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class DataEvent<T> {

    private val relay = PublishRelay.create<T>()

    fun next(value: T) = relay.accept(value)

    @Suppress("HasPlatformType")
    fun toObservable() = (relay as Observable<T>).observeOn(AndroidSchedulers.mainThread())

    fun subscribe(block: (T) -> Unit): Disposable = toObservable().subscribe { block(it) }
}
