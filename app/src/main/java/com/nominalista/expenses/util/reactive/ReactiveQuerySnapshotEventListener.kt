package com.nominalista.expenses.util.reactive

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposables

class ReactiveQuerySnapshotEventListener(
    private val collectionReference: CollectionReference
) : ObservableOnSubscribe<QuerySnapshot> {

    override fun subscribe(emitter: ObservableEmitter<QuerySnapshot>) {
        val listener = EventListener<QuerySnapshot> { snapshot, error ->
            if (snapshot == null || error != null) {
                if (!emitter.isDisposed) {
                    emitter.onError(error ?: Error("Snapshot is null."))
                }
            } else {
                if (!emitter.isDisposed) {
                    emitter.onNext(snapshot)
                }
            }
        }

        val registration = collectionReference.addSnapshotListener(listener)

        emitter.setDisposable(Disposables.fromAction { registration.remove() })
    }
}