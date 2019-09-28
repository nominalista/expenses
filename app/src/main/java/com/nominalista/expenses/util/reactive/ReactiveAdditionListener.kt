package com.nominalista.expenses.util.reactive

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import io.reactivex.CompletableEmitter
import io.reactivex.CompletableOnSubscribe
import io.reactivex.disposables.Disposables

class ReactiveAdditionListener(
    private val collectionReference: CollectionReference,
    private val document: Any
) : CompletableOnSubscribe {

    override fun subscribe(emitter: CompletableEmitter) {
        val successListener = OnSuccessListener<DocumentReference> {
            if (!emitter.isDisposed) {
                emitter.onComplete()
            }
        }
        val failureListener = OnFailureListener { error ->
            if (!emitter.isDisposed) {
                emitter.onError(error)
            }
        }

        collectionReference.add(document)
            .addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener)

        emitter.setDisposable(Disposables.empty())
    }
}