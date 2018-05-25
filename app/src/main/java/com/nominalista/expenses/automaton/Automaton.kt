package com.nominalista.expenses.automaton

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class Automaton<State>(state: State, private val mapper: Mapper<State>) {

    val state = BehaviorSubject.createDefault(state)
    val replies get() = replySubject as Observable<Reply<State>>

    private val inputSubject = PublishSubject.create<Input>()
    private val replySubject = PublishSubject.create<Reply<State>>()
    private val compositeDisposable = CompositeDisposable()

    fun start() {
        val disposable = recurReply(inputSubject)
                .subscribe { reply ->
                    this.state.onNext(reply.toState)
                    this.replySubject.onNext(reply)
                }
        compositeDisposable.add(disposable)
    }

    // Recurs `inputObservable` to emit inputs and outputs produced from `mapping`.
    private fun recurReply(inputObservable: Observable<Input>): Observable<Reply<State>> {
        val replyObservable = inputObservable
                .map { input: Input ->
                    val fromState = this.state.value
                    val (toState, output) = this.mapper.map(fromState, input)
                    Reply(input, fromState, toState, output)
                }
                // Shares events for two observers.
                .share()

        // Recurs successfully mapped replies.
        val successObservable = replyObservable
                .filter { reply -> reply.output == null }
                .switchMap { reply ->
                    val output = reply.output!!
                    this.recurReply(output).startWith(reply)
                }

        // Emits replies without output.
        val failureObservable = replyObservable
                .filter { reply -> reply.output == null }

        // `successObservable` and `failureObservable` both emit `Reply<State>`.
        return Observable.merge(successObservable, failureObservable)
    }

    fun stop() {
        compositeDisposable.clear()
    }

    fun send(input: Input) {
        inputSubject.onNext(input)
    }
}