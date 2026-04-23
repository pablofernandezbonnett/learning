package learning.livecoding.java;

import java.util.HashMap;
import java.util.Map;

public final class LruCacheExercise implements Exercise {
    @Override
    public String id() {
        return "lru-cache";
    }

    @Override
    public String title() {
        return "LRU Cache";
    }

    @Override
    public String summary() {
        return "Use a map for O(1) lookup and a doubly linked list for O(1) recency updates and eviction.";
    }

    static final class LruCache {
        private static final class Node {
            final int key;
            int value;
            Node prev;
            Node next;

            Node(int key, int value) {
                this.key = key;
                this.value = value;
            }
        }

        private final int capacity;
        private final Map<Integer, Node> nodes = new HashMap<>();
        private final Node head = new Node(-1, -1);
        private final Node tail = new Node(-1, -1);

        LruCache(int capacity) {
            this.capacity = capacity;
            head.next = tail;
            tail.prev = head;
        }

        int get(int key) {
            Node node = nodes.get(key);
            if (node == null) {
                return -1;
            }
            moveToFront(node);
            return node.value;
        }

        void put(int key, int value) {
            Node existing = nodes.get(key);
            if (existing != null) {
                existing.value = value;
                moveToFront(existing);
                return;
            }

            Node inserted = new Node(key, value);
            nodes.put(key, inserted);
            attachAfterHead(inserted);

            if (nodes.size() > capacity) {
                Node lru = tail.prev;
                detach(lru);
                nodes.remove(lru.key);
            }
        }

        private void moveToFront(Node node) {
            detach(node);
            attachAfterHead(node);
        }

        private void attachAfterHead(Node node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
        }

        private void detach(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
    }

    @Override
    public void run() {
        LruCache cache = new LruCache(2);
        cache.put(1, 10);
        cache.put(2, 20);
        ExerciseSupport.expectEquals("get-1", 10, cache.get(1));
        cache.put(3, 30);
        ExerciseSupport.expectEquals("evicted-2", -1, cache.get(2));
        cache.put(4, 40);
        ExerciseSupport.expectEquals("evicted-1", -1, cache.get(1));
        ExerciseSupport.expectEquals("get-3", 30, cache.get(3));
        ExerciseSupport.expectEquals("get-4", 40, cache.get(4));
    }
}
