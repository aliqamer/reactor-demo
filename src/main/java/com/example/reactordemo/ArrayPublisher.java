package com.example.reactordemo;

import java.util.concurrent.Flow;

public class ArrayPublisher<T> implements Flow.Publisher<T> {

    private final T[] array;

    public ArrayPublisher(T[] array) {
        this.array = array;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new Flow.Subscription() {
            int index;
            long requested;
            boolean cancelled = false;

            @Override
            public void request(long n) {
//                System.out.println("requesting "+n+" elements");

                long initialRequested = requested;
                requested += n;

                //if we are already iterating
                if(initialRequested != 0) {
                    return;
                }

                int sent = 0;

                for (; sent < requested && index < array.length; sent++, index++) {

                    if(cancelled) {
                        return;
                    }

                    T element = array[index];

                    if(element == null) {
                        subscriber.onError(new NullPointerException());
                        return;
                    }
                    subscriber.onNext(element);
                }
                if(index == array.length) {
                    subscriber.onComplete();
                }

                requested -= sent;
            }

            @Override
            public void cancel() {
                cancelled = true;
            }
        });

    }
}
