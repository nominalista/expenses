package com.nominalista.expenses.util.reactive

import io.reactivex.Observable
import io.reactivex.Single

@Suppress("UNCHECKED_CAST")
fun <T> Iterable<Observable<T>>.combineLatest() =
    Observable.combineLatest(this) { array -> array.map { it as T } }

@Suppress("UNCHECKED_CAST")
fun <T> Iterable<Single<T>>.zip() =
    Single.zip(this) { array -> array.map { it as T } }