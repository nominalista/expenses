package com.nominalista.expenses.infrastructure.automaton

import io.reactivex.Observable

interface Mapper<State, Input> {

    fun map(state: State, input: Input): Pair<State, Observable<Input>?>
}