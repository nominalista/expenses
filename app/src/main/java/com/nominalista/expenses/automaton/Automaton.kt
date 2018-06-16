package com.nominalista.expenses.automaton

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

open class Automaton<State, Input>(state: State, private val mapper: Mapper<State, Input>) {

    val state = BehaviorSubject.createDefault(state)
    val replies get() = replySubject as Observable<Reply<State, Input>>

    private val replySubject = PublishSubject.create<Reply<State, Input>>()
    private val inputSubject = PublishSubject.create<Input>()
    private var disposable: Disposable? = null

    fun start() {
        disposable = recurReply(inputSubject).subscribe { reply ->
            state.onNext(reply.toState)
            replySubject.onNext(reply)
        }
    }

    // Recurs `inputObservable` to emit inputs and outputs produced from `mapping`.
    private fun recurReply(inputObservable: Observable<Input>): Observable<Reply<State, Input>> {
        val replyObservable = inputObservable
                .map { input: Input ->
                    val fromState = state.value
                    val (toState, output) = mapper.map(fromState, input)
                    Reply(input, fromState, toState, output)
                }
                // Shares events for two observers.
                .share()

        // Recurs successfully mapped replies.
        val successObservable = replyObservable
                .filter { it.output != null }
                .switchMap { recurReply(it.output!!).startWith(it) }

        // Emits replies without output.
        val failureObservable = replyObservable
                .filter { it.output == null }

        // `successObservable` and `failureObservable` both emit `Reply<State>`.
        return Observable.merge(successObservable, failureObservable)
    }

    fun stop() {
        disposable?.dispose()
        disposable = null
    }

    fun send(input: Input) {
        inputSubject.onNext(input)
    }
}