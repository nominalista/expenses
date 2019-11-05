package com.nominalista.expenses.util.reactive

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposables

class ReactiveDocumentSnapshotEventListener(
    private val documentReference: DocumentReference
) : ObservableOnSubscribe<DocumentSnapshot> {

    override fun subscribe(emitter: ObservableEmitter<DocumentSnapshot>) {
        val listener = EventListener<DocumentSnapshot> { snapshot, error ->
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

        val registration = documentReference.addSnapshotListener(listener)

        emitter.setDisposable(Disposables.fromAction { registration.remove() })
    }
}