package learning.examples.algorithms

import learning.examples.common.Console
import learning.examples.common.Topic
import java.util.PriorityQueue

object AlgorithmsDrillsTopic : Topic {
    override val id: String = "algorithms/drills"
    override val title: String = "Timed coding drills with edge cases"
    override val sourceDocs: List<String> = listOf(
        "topics/algorithms/02-coding-round-drills.md",
    )

    override fun run() {
        hashMapDrill()
        windowDrill()
        twoPointerAndSearchDrill()
        linkedListDrill()
        stackAndHeapDrill()
        intervalAndGraphDrill()
    }

    private fun hashMapDrill() {
        Console.section("Hash map drills")
        Console.result(
            "first unique order ID",
            firstUniqueOrderId(listOf("A1", "B2", "A1", "C3", "B2")),
        )
        Console.result(
            "two products within budget 4980",
            twoProductsBudget(listOf(2990, 4990, 1990, 5990), 4980),
        )
    }

    private fun windowDrill() {
        Console.section("Sliding-window drills")
        Console.result(
            "minimum transactions covering refund 7",
            minTransactionsCoveringRefund(intArrayOf(2, 3, 1, 2, 4, 3), 7),
        )
        Console.result(
            "longest session without repeated error code",
            longestSessionNoRepeat("ABCABCBB"),
        )
    }

    private fun twoPointerAndSearchDrill() {
        Console.section("Two-pointer and binary-search drills")
        Console.result(
            "two-sum sorted",
            twoSumSorted(listOf(1000, 2000, 3000, 4000, 5000), 7000),
        )
        Console.result(
            "first occurrence of 200",
            firstOccurrence(intArrayOf(100, 200, 200, 200, 500), 200),
        )
    }

    private fun linkedListDrill() {
        Console.section("Linked-list drills")
        val head = listOf(1, 2, 3, 4, 5).toDrillLinkedList()
        Console.result("original list", head.toReadableString())
        Console.result("remove 2nd node from end", removeKthFromEnd(head, 2).toReadableString())
    }

    private fun stackAndHeapDrill() {
        Console.section("Stack / heap drills")
        Console.result("properly nested brackets", isProperlyNested("({[]})"))

        val minStack = MinStack()
        minStack.push(5)
        minStack.push(2)
        minStack.push(4)
        minStack.push(2)
        Console.result("current min after pushes", minStack.getMin())
        minStack.pop()
        Console.result("current min after one pop", minStack.getMin())

        Console.result(
            "top 2 frequent events",
            topKFrequentEvents(listOf("PAYMENT", "ORDER", "PAYMENT", "REFUND", "PAYMENT", "ORDER"), 2),
        )
    }

    private fun intervalAndGraphDrill() {
        Console.section("Intervals and graphs")
        val merged = mergeTimeWindows(
            listOf(
                TimeWindow(1, 3),
                TimeWindow(2, 6),
                TimeWindow(8, 10),
                TimeWindow(9, 12),
            ),
        )
        Console.result("merged windows", merged)

        val graph = mapOf(
            "checkout" to listOf("inventory", "payment"),
            "payment" to listOf("fraud-check", "bank"),
            "inventory" to listOf("warehouse"),
            "fraud-check" to listOf("bank"),
        )
        Console.result("shortest call chain checkout -> bank", shortestCallChain(graph, "checkout", "bank"))
        Console.result(
            "circular dependency exists",
            hasCircularDependency(
                mapOf(
                    "A" to listOf("B"),
                    "B" to listOf("C"),
                    "C" to listOf("A"),
                ),
            ),
        )
    }
}

private fun firstUniqueOrderId(orderIds: List<String>): String? {
    val count = linkedMapOf<String, Int>()
    for (id in orderIds) {
        count[id] = (count[id] ?: 0) + 1
    }
    return count.entries.firstOrNull { it.value == 1 }?.key
}

private fun twoProductsBudget(prices: List<Int>, budget: Int): Pair<Int, Int> {
    val seen = mutableMapOf<Int, Int>()
    for ((index, price) in prices.withIndex()) {
        val complement = budget - price
        val previousIndex = seen[complement]
        if (previousIndex != null) return previousIndex to index
        seen[price] = index
    }
    error("No solution found")
}

private fun minTransactionsCoveringRefund(amounts: IntArray, target: Int): Int {
    var left = 0
    var sum = 0
    var minLen = Int.MAX_VALUE

    for (right in amounts.indices) {
        sum += amounts[right]
        while (sum >= target) {
            minLen = minOf(minLen, right - left + 1)
            sum -= amounts[left]
            left++
        }
    }
    return if (minLen == Int.MAX_VALUE) 0 else minLen
}

private fun longestSessionNoRepeat(log: String): Int {
    val lastSeen = mutableMapOf<Char, Int>()
    var left = 0
    var maxLen = 0

    for ((right, code) in log.withIndex()) {
        val previousIndex = lastSeen[code]
        if (previousIndex != null && previousIndex >= left) {
            left = previousIndex + 1
        }
        lastSeen[code] = right
        maxLen = maxOf(maxLen, right - left + 1)
    }
    return maxLen
}

private fun twoSumSorted(amounts: List<Int>, target: Int): Pair<Int, Int> {
    var left = 0
    var right = amounts.lastIndex

    while (left < right) {
        val sum = amounts[left] + amounts[right]
        when {
            sum == target -> return left to right
            sum < target -> left++
            else -> right--
        }
    }
    error("No solution found")
}

private fun firstOccurrence(sorted: IntArray, target: Int): Int {
    var left = 0
    var right = sorted.lastIndex
    var answer = -1

    while (left <= right) {
        val mid = left + (right - left) / 2
        when {
            sorted[mid] == target -> {
                answer = mid
                right = mid - 1
            }
            sorted[mid] < target -> left = mid + 1
            else -> right = mid - 1
        }
    }
    return answer
}

private data class DrillListNode(
    val value: Int,
    var next: DrillListNode? = null,
)

private fun List<Int>.toDrillLinkedList(): DrillListNode? {
    var head: DrillListNode? = null
    var tail: DrillListNode? = null
    for (value in this) {
        val node = DrillListNode(value)
        if (head == null) {
            head = node
            tail = node
        } else {
            tail!!.next = node
            tail = node
        }
    }
    return head
}

private fun DrillListNode?.toReadableString(): String {
    val values = mutableListOf<Int>()
    var current = this
    while (current != null) {
        values.add(current.value)
        current = current.next
    }
    return values.joinToString(" -> ")
}

private fun removeKthFromEnd(head: DrillListNode?, k: Int): DrillListNode? {
    val dummy = DrillListNode(0, head)
    var fast: DrillListNode = dummy
    var slow: DrillListNode = dummy

    repeat(k) {
        fast = fast.next ?: return dummy.next
    }

    while (fast.next != null) {
        fast = fast.next!!
        slow = slow.next!!
    }

    slow.next = slow.next?.next
    return dummy.next
}

private fun isProperlyNested(text: String): Boolean {
    val stack = ArrayDeque<Char>()
    val matching = mapOf(')' to '(', ']' to '[', '}' to '{')

    for (character in text) {
        when (character) {
            '(', '[', '{' -> stack.addLast(character)
            ')', ']', '}' -> {
                if (stack.isEmpty()) return false
                if (stack.removeLast() != matching[character]) return false
            }
        }
    }

    return stack.isEmpty()
}

private class MinStack {
    private val values = ArrayDeque<Int>()
    private val mins = ArrayDeque<Int>()

    fun push(value: Int) {
        values.addLast(value)
        if (mins.isEmpty() || value <= mins.last()) mins.addLast(value)
    }

    fun pop(): Int {
        val removed = values.removeLast()
        if (removed == mins.last()) mins.removeLast()
        return removed
    }

    fun getMin(): Int = mins.last()
}

private fun topKFrequentEvents(events: List<String>, k: Int): List<String> {
    val count = events.groupingBy { it }.eachCount()
    val heap = PriorityQueue(compareBy<Map.Entry<String, Int>> { it.value })

    for (entry in count.entries) {
        heap.add(entry)
        if (heap.size > k) heap.poll()
    }

    val result = mutableListOf<String>()
    while (heap.isNotEmpty()) {
        result.add(heap.poll().key)
    }
    return result.asReversed()
}

private data class TimeWindow(
    var start: Int,
    var end: Int,
)

private fun mergeTimeWindows(windows: List<TimeWindow>): List<TimeWindow> {
    if (windows.isEmpty()) return emptyList()
    val sorted = windows.sortedBy { it.start }
    val merged = mutableListOf(TimeWindow(sorted[0].start, sorted[0].end))

    for (window in sorted.drop(1)) {
        val last = merged.last()
        if (window.start <= last.end) {
            last.end = maxOf(last.end, window.end)
        } else {
            merged.add(TimeWindow(window.start, window.end))
        }
    }
    return merged
}

private fun shortestCallChain(
    graph: Map<String, List<String>>,
    from: String,
    to: String,
): Int {
    if (from == to) return 0

    val queue = ArrayDeque<Pair<String, Int>>()
    val visited = mutableSetOf(from)
    queue.add(from to 0)

    while (queue.isNotEmpty()) {
        val (node, distance) = queue.removeFirst()
        for (neighbor in graph[node] ?: emptyList()) {
            if (neighbor == to) return distance + 1
            if (visited.add(neighbor)) queue.add(neighbor to distance + 1)
        }
    }
    return -1
}

private fun hasCircularDependency(graph: Map<String, List<String>>): Boolean {
    val visited = mutableSetOf<String>()
    val inStack = mutableSetOf<String>()

    fun dfs(node: String): Boolean {
        visited.add(node)
        inStack.add(node)

        for (neighbor in graph[node] ?: emptyList()) {
            if (neighbor in inStack) return true
            if (neighbor !in visited && dfs(neighbor)) return true
        }

        inStack.remove(node)
        return false
    }

    return graph.keys.any { it !in visited && dfs(it) }
}
