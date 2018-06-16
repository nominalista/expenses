package com.nominalista.expenses.automaton

import io.reactivex.Observable

data class Reply<State, Input>(
        val input: Input,
        val fromState: State,
        val toState: State,
        val output: Observable<Input>?
)