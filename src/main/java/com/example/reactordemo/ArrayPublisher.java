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
            @Override
            public void request(long n) {
                System.out.println("requesting "+n+" elements");
            }

            @Override
            public void cancel() {

            }
        });
        for (int i = 0; i < array.length; i++) {
            subscriber.onNext(array[i]);
        }
        subscriber.onComplete();
    }
}
