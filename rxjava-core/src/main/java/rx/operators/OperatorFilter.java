/**
 * Copyright 2014 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.operators;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.observables.GroupedObservable;
import rx.util.functions.Func1;

/**
 * Filters an Observable by discarding any items it emits that do not meet some test.
 * <p>
 * <img width="640" src="https://github.com/Netflix/RxJava/wiki/images/rx-operators/filter.png">
 */
public final class OperatorFilter<T> implements Operator<T, T> {

    private final Func1<? super T, Boolean> predicate;

    public OperatorFilter(Func1<? super T, Boolean> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> child) {
        return new Subscriber<T>(child) {

            @Override
            public void onCompleted() {
                child.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                child.onError(e);
            }

            @Override
            public void onNext(T value) {
                try {
                    if (predicate.call(value)) {
                        child.onNext(value);
                    } else {
                        /*
                         * Special casing of GroupedObservable since GroupedObservable ***MUST*** be subscribed to
                         * otherwise it will block the GroupBy operator. 
                         * 
                         * See https://github.com/Netflix/RxJava/issues/844
                         */
                        if (value instanceof GroupedObservable) {
                            System.out.println("value is GroupedObservable");
                            @SuppressWarnings("rawtypes")
                            GroupedObservable go = (GroupedObservable) value;
                            System.out.println("********* unsubscribe from go");
                            go.take(0).subscribe();
                        }
                    }
                } catch (Throwable ex) {
                    child.onError(ex);
                }
            }

        };
    }

}
