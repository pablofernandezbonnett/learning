package learning.livecoding.kotlin

object LruCacheExercise : Exercise {
    override val id = "lru-cache"
    override val title = "LRU Cache"
    override val summary = "Use a map for O(1) lookup and a doubly linked list for O(1) recency updates and eviction."

    class LruCache(private val capacity: Int) {
        private data class Node(val key: Int, var value: Int, var prev: Node? = null, var next: Node? = null)

        private val nodes = mutableMapOf<Int, Node>()
        private val head = Node(-1, -1)
        private val tail = Node(-1, -1)

        init {
            head.next = tail
            tail.prev = head
        }

        fun get(key: Int): Int {
            val node = nodes[key] ?: return -1
            moveToFront(node)
            return node.value
        }

        fun put(key: Int, value: Int) {
            val existing = nodes[key]
            if (existing != null) {
                existing.value = value
                moveToFront(existing)
                return
            }

            val inserted = Node(key, value)
            nodes[key] = inserted
            attachAfterHead(inserted)

            if (nodes.size > capacity) {
                val lru = tail.prev!!
                detach(lru)
                nodes.remove(lru.key)
            }
        }

        private fun moveToFront(node: Node) {
            detach(node)
            attachAfterHead(node)
        }

        private fun attachAfterHead(node: Node) {
            node.next = head.next
            node.prev = head
            head.next!!.prev = node
            head.next = node
        }

        private fun detach(node: Node) {
            node.prev!!.next = node.next
            node.next!!.prev = node.prev
        }
    }

    override fun run() {
        val cache = LruCache(2)
        cache.put(1, 10)
        cache.put(2, 20)
        ExerciseSupport.expectEquals("get-1", 10, cache.get(1))
        cache.put(3, 30)
        ExerciseSupport.expectEquals("evicted-2", -1, cache.get(2))
        cache.put(4, 40)
        ExerciseSupport.expectEquals("evicted-1", -1, cache.get(1))
        ExerciseSupport.expectEquals("get-3", 30, cache.get(3))
        ExerciseSupport.expectEquals("get-4", 40, cache.get(4))
    }
}
