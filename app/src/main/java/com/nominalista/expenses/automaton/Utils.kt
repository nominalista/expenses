package com.nominalista.expenses.automaton

import io.reactivex.Observable

fun <Input> maybeCombineOutputs(vararg outputs: Observable<Input>?): Observable<Input>? {
    val filteredOutputs = outputs.filterNotNull()
    return if (filteredOutputs.isEmpty()) null else Observable.merge(filteredOutputs)
}