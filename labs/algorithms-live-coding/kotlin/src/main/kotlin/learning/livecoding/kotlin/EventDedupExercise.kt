package learning.livecoding.kotlin

object EventDedupExercise : Exercise {
    override val id = "event-dedup"
    override val title = "Webhook/Event Deduplication"
    override val summary =
        "Use a hash set for O(1) average event-ID lookups so retries or broker re-delivery do not trigger the side effect twice."

    data class WebhookEvent(val eventId: String, val orderId: String, val type: String)

    private class EventDeduplicator {
        private val processedEventIds = mutableSetOf<String>()

        fun process(events: List<WebhookEvent>): List<String> {
            val appliedSideEffects = mutableListOf<String>()
            for (event in events) {
                if (processedEventIds.add(event.eventId)) {
                    appliedSideEffects += "${event.type}:${event.orderId}"
                }
            }
            return appliedSideEffects
        }

        fun processedCount(): Int = processedEventIds.size
    }

    override fun run() {
        val deduplicator = EventDeduplicator()
        val events = listOf(
            WebhookEvent("evt-1", "order-100", "payment-captured"),
            WebhookEvent("evt-1", "order-100", "payment-captured"),
            WebhookEvent("evt-2", "order-100", "receipt-sent"),
            WebhookEvent("evt-3", "order-101", "payment-captured"),
            WebhookEvent("evt-3", "order-101", "payment-captured"),
        )

        ExerciseSupport.expectEquals(
            "applied-side-effects",
            listOf(
                "payment-captured:order-100",
                "receipt-sent:order-100",
                "payment-captured:order-101",
            ),
            deduplicator.process(events),
        )
        ExerciseSupport.expectEquals("unique-event-ids", 3, deduplicator.processedCount())
    }
}
