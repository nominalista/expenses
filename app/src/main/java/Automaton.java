import com.nominalista.expenses.automaton.Mapper;
import com.nominalista.expenses.automaton.Reply;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import kotlin.Pair;

class Automaton<State, Input> {

    public BehaviorSubject<State> state;
    private Mapper<State, Input> mapper;

    private PublishSubject<Reply<State, Input>> replySubject = PublishSubject.create();
    private PublishSubject<Input> inputSubject = PublishSubject.create();
    private Disposable disposable = null;

    public Automaton(State state, Mapper<State, Input> mapper) {
        this.state = BehaviorSubject.createDefault(state);
        this.mapper = mapper;
    }

    public void start() {
        disposable = recurReply(inputSubject).subscribe(new Consumer<Reply<State, Input>>() {
            @Override
            public void accept(Reply<State, Input> reply) {
                state.onNext(reply.getToState());
                replySubject.onNext(reply);
            }
        });
    }

    private Observable<Reply<State, Input>> recurReply(Observable<Input> inputObservable) {
        Observable<Reply<State, Input>> replyObservable = inputObservable
                .map(new Function<Input, Reply<State, Input>>() {
                    @Override
                    public Reply<State, Input> apply(Input input) {
                        State fromState = state.getValue();
                        Pair<State, Observable<Input>> result = mapper.map(fromState, input);
                        return new Reply<State, Input>(input, fromState, result.getFirst(), result.getSecond());
                    }
                })
                .share();

        Observable<Reply<State, Input>> successObservable = replyObservable
                .filter(new Predicate<Reply<State, Input>>() {
                    @Override
                    public boolean test(Reply<State, Input> reply) {
                        return reply.getOutput() != null;
                    }
                })
                .switchMap(new Function<Reply<State, Input>, ObservableSource<? extends Reply<State, Input>>>() {
                    @Override
                    public ObservableSource<? extends Reply<State, Input>> apply(Reply<State, Input> reply) {
                        return recurReply(reply.getOutput()).startWith(reply);
                    }
                });

        Observable<Reply<State, Input>> failureObservable = replyObservable
                .filter(new Predicate<Reply<State, Input>>() {
                    @Override
                    public boolean test(Reply<State, Input> reply) throws Exception {
                        return reply.getOutput() == null;
                    }
                });

        return Observable.merge(successObservable, failureObservable);
    }

    public void stop() {
        if (disposable != null) disposable.dispose();
        disposable = null;
    }


    public void send(Input input) {
        inputSubject.onNext(input);
    }
}
