package com.nominalista.expenses.util.reactive

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.reactivex.CompletableEmitter
import io.reactivex.CompletableOnSubscribe
import io.reactivex.SingleEmitter
import io.reactivex.SingleOnSubscribe
import io.reactivex.disposables.Disposables

class ReactiveTaskListener<T>(
    private val task: Task<T>
) : SingleOnSubscribe<T>, CompletableOnSubscribe {

    override fun subscribe(emitter: SingleEmitter<T>) {
        val successListener = OnSuccessListener<T> {
            if (!emitter.isDisposed) {
                emitter.onSuccess(it)
            }
        }
        val failureListener = OnFailureListener { error ->
            if (!emitter.isDisposed) {
                emitter.onError(error)
            }
        }

        task.addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener)

        emitter.setDisposable(Disposables.empty())
    }

    override fun subscribe(emitter: CompletableEmitter) {
        val successListener = OnSuccessListener<T> {
            if (!emitter.isDisposed) {
                emitter.onComplete()
            }
        }
        val failureListener = OnFailureListener { error ->
            if (!emitter.isDisposed) {
                emitter.onError(error)
            }
        }

        task.addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener)

        emitter.setDisposable(Disposables.empty())
    }
}