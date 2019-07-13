package com.nominalista.expenses.util.reactive

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposables

class RxLiveDataObserver<T>(private val liveData: LiveData<T>) : ObservableOnSubscribe<T> {

    override fun subscribe(emitter: ObservableEmitter<T>) {
        val observer = Observer<T> { emitter.onNext(it) }

        liveData.observeForever(observer)

        emitter.setDisposable(Disposables.fromAction { liveData.removeObserver(observer) })
    }
}