package com.example.reactordemo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.LongStream;

public class ArrayPublisherTest {

    @Test
    public void everyMethodInSubscriberShouldBeExecutedInParticularOrder() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ArrayList<String> observedSignals = new ArrayList<>();
        ArrayPublisher<Long> arrayPublisher = new ArrayPublisher<>(generate(5));

        arrayPublisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                observedSignals.add("onSubscribe()");
                subscription.request(10);
            }

            @Override
            public void onNext(Long item) {
                observedSignals.add("onNext(" + item + ")");
            }

            @Override
            public void onError(Throwable throwable) {
                observedSignals.add("onError()");
            }

            @Override
            public void onComplete() {
                observedSignals.add("onComplete()");
                latch.countDown();
            }
        });

        Assertions.assertThat(latch.await(1000, TimeUnit.MILLISECONDS)).isTrue();

        Assertions.assertThat(observedSignals)
                .containsExactly(
                        "onSubscribe()",
                        "onNext(0)",
                        "onNext(1)",
                        "onNext(2)",
                        "onNext(3)",
                        "onNext(4)",
                        "onComplete()"
                );
    }

    @Test
    public void mustSupportBackpressureControl() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ArrayList<Long> collected = new ArrayList<>();
        long toRequest = 5;
        Long[] array = generate(toRequest);
        ArrayPublisher<Long> publisher = new ArrayPublisher<>(array);
        Flow.Subscription[] subscriptions = new Flow.Subscription[1];

        publisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscriptions[0] = subscription;
            }

            @Override
            public void onNext(Long item) {
                collected.add(item);
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete()");
                latch.countDown();
            }
        });

        Assertions.assertThat(collected).isEmpty();

        subscriptions[0].request(1);
        Assertions.assertThat(collected).containsExactly(0L);

        subscriptions[0].request(1);
        Assertions.assertThat(collected).containsExactly(0L, 1L);

        subscriptions[0].request(2);
        Assertions.assertThat(collected).containsExactly(0L, 1L, 2L, 3L);

        subscriptions[0].request(20);

        Assertions.assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();

        Assertions.assertThat(collected).containsExactly(array);
    }

    @Test
    public void mustSendNPENormally() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Long[] array = new Long[] { null };
        AtomicReference<Throwable> error = new AtomicReference<>();
        ArrayPublisher<Long> publisher = new ArrayPublisher<>(array);

        publisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(5);
            }

            @Override
            public void onNext(Long item) {

            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
                latch.countDown();
            }

            @Override
            public void onComplete() {

            }
        });

        latch.await(1, TimeUnit.SECONDS);
        Assertions.assertThat(error.get()).isInstanceOf(NullPointerException.class);
    }

    static Long[] generate(long num) {
        return LongStream.range(0, num >= Integer.MAX_VALUE ? 1000000 : num)
                .boxed()
                .toArray(Long[]::new);
    }
}
