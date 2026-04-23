package learning.livecoding.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EventDedupExercise implements Exercise {
    @Override
    public String id() {
        return "event-dedup";
    }

    @Override
    public String title() {
        return "Webhook/Event Deduplication";
    }

    @Override
    public String summary() {
        return "Use a hash set for O(1) average event-ID lookups so retries or broker re-delivery do not trigger the side effect twice.";
    }

    record WebhookEvent(String eventId, String orderId, String type) {
    }

    static final class EventDeduplicator {
        private final Set<String> processedEventIds = new HashSet<>();

        List<String> process(List<WebhookEvent> events) {
            List<String> appliedSideEffects = new ArrayList<>();
            for (WebhookEvent event : events) {
                if (processedEventIds.add(event.eventId())) {
                    appliedSideEffects.add(event.type() + ":" + event.orderId());
                }
            }
            return appliedSideEffects;
        }

        int processedCount() {
            return processedEventIds.size();
        }
    }

    @Override
    public void run() {
        EventDeduplicator deduplicator = new EventDeduplicator();
        List<WebhookEvent> events = List.of(
            new WebhookEvent("evt-1", "order-100", "payment-captured"),
            new WebhookEvent("evt-1", "order-100", "payment-captured"),
            new WebhookEvent("evt-2", "order-100", "receipt-sent"),
            new WebhookEvent("evt-3", "order-101", "payment-captured"),
            new WebhookEvent("evt-3", "order-101", "payment-captured")
        );

        ExerciseSupport.expectEquals(
            "applied-side-effects",
            List.of(
                "payment-captured:order-100",
                "receipt-sent:order-100",
                "payment-captured:order-101"
            ),
            deduplicator.process(events)
        );
        ExerciseSupport.expectEquals("unique-event-ids", 3, deduplicator.processedCount());
    }
}
