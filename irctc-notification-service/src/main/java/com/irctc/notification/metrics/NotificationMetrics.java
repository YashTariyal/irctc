package com.irctc.notification.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationMetrics {

    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();

    public NotificationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementEmailSuccess() { increment("notifications.email.success"); }
    public void incrementEmailFailure() { increment("notifications.email.failure"); }

    public void incrementSmsSuccess() { increment("notifications.sms.success"); }
    public void incrementSmsFailure() { increment("notifications.sms.failure"); }

    public void incrementPushSuccess() { increment("notifications.push.success"); }
    public void incrementPushFailure() { increment("notifications.push.failure"); }

    public void incrementConsumedEvent(String topic) {
        increment("notifications.events.consumed", "topic", topic);
    }

    public void incrementConsumerError(String topic) {
        increment("notifications.events.error", "topic", topic);
    }

    public Timer.Sample startTimer(String name) {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String name, String... tags) {
        String key = name + String.join("|", tags);
        Timer timer = timers.computeIfAbsent(key, k -> Timer.builder(name).tags(tags).register(meterRegistry));
        sample.stop(timer);
    }

    private void increment(String name, String... tags) {
        String key = name + String.join("|", tags);
        Counter counter = counters.computeIfAbsent(key, k -> Counter.builder(name).tags(tags).register(meterRegistry));
        counter.increment();
    }
}
