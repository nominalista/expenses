package com.nominalista.expenses.util.reactive

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.flow

class Event {

    private val relay = PublishRelay.create<Any>()

    fun next() = relay.accept(Any())

    @Suppress("HasPlatformType")
    fun toObservable() = (relay as Observable<Any>).observeOn(mainThread())

    fun subscribe(block: () -> Unit): Disposable = toObservable().subscribe { block() }
}