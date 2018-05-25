package com.nominalista.expenses.automaton

import io.reactivex.Observable

data class Reply<State>(val input: Input,
                        val fromState: State,
                        val toState: State,
                        val output: Observable<Input>?)