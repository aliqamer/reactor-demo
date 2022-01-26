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

            @Override
            public void request(long n) {
                System.out.println("requesting "+n+" elements");
                for (int i = 0; i < n && index < array.length; i++, index++) {
                    subscriber.onNext(array[index]);
                }
                if(index == array.length) {
                    subscriber.onComplete();
                }
            }

            @Override
            public void cancel() {

            }
        });

    }
}
