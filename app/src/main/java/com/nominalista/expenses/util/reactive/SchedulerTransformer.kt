package com.nominalista.expenses.util.reactive

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io

class SchedulerTransformer<T>
    : ObservableTransformer<T, T>,
    SingleTransformer<T, T>,
    CompletableTransformer {

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream.subscribeOn(io()).observeOn(mainThread())
    }

    override fun apply(upstream: Single<T>): SingleSource<T> {
        return upstream.subscribeOn(io()).observeOn(mainThread())
    }

    override fun apply(upstream: Completable): CompletableSource {
        return upstream.subscribeOn(io()).observeOn(mainThread())
    }
}