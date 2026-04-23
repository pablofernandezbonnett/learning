package learning.examples.algorithms

import learning.examples.common.Console
import learning.examples.common.Topic
import java.util.PriorityQueue

object AlgorithmsPatternsTopic : Topic {
    override val id: String = "algorithms/patterns"
    override val title: String = "Core DSA patterns with backend-flavored examples"
    override val sourceDocs: List<String> = listOf(
        "topics/algorithms/01-core-patterns.md",
    )

    override fun run() {
        hashMapPattern()
        slidingWindowPattern()
        twoPointersPattern()
        linkedListPattern()
        heapPattern()
        graphPattern()
        dynamicProgrammingPattern()
    }

    private fun hashMapPattern() {
        Console.section("Hash map lookup")
        val orderIds = listOf("PAY-1", "PAY-2", "PAY-3", "PAY-2", "PAY-4")
        Console.step("Input order IDs = $orderIds")
        Console.result("first duplicate", findFirstDuplicate(orderIds))
    }

    private fun slidingWindowPattern() {
        Console.section("Sliding window")
        val requestsPerMinute = intArrayOf(10, 25, 30, 5, 15, 40, 25)
        Console.step("Rolling traffic = ${requestsPerMinute.toList()}")
        Console.result("max traffic in 3-minute window", findMaxTrafficInWindow(requestsPerMinute, 3))

        val transactions = intArrayOf(2, 3, 1, 2, 4, 3)
        Console.step("Refund coverage amounts = ${transactions.toList()}")
        Console.result("minimum window with sum >= 7", minWindowSum(transactions, 7))
    }

    private fun twoPointersPattern() {
        Console.section("Two pointers")
        val palindromeCandidate = "A man, a plan, a canal: Panama"
        Console.result("letters-only palindrome", isLetterPalindrome(palindromeCandidate))

        val merged = mergeSortedLogs(
            listOf(1, 4, 7, 10),
            listOf(2, 3, 8, 11),
        )
        Console.result("merged sorted logs", merged)
    }

    private fun linkedListPattern() {
        Console.section("Linked-list basics")
        val head = listOf(1, 2, 3, 4, 5).toLinkedList()
        Console.result("original list", head.toReadableString())
        Console.result("middle node", middleNode(head)?.value)
        Console.result("reversed list", reverse(head).toReadableString())
    }

    private fun heapPattern() {
        Console.section("Heap / priority queue")
        val statuses = listOf("PAID", "FAILED", "PAID", "PENDING", "PAID", "FAILED", "PENDING")
        Console.step("Statuses = $statuses")
        Console.result("top 2 frequent statuses", topKFrequent(statuses, 2))
    }

    private fun graphPattern() {
        Console.section("BFS / DFS")
        val serviceGraph = mapOf(
            "checkout" to listOf("inventory", "payment"),
            "inventory" to listOf("warehouse"),
            "payment" to listOf("fraud-check", "bank"),
            "fraud-check" to listOf("bank"),
            "warehouse" to listOf("shipping"),
            "bank" to emptyList(),
            "shipping" to emptyList(),
        )
        Console.result("BFS shortest path checkout -> bank", bfsShortestPath(serviceGraph, "checkout", "bank"))
        Console.result("DFS reachable from checkout", dfsReachable(serviceGraph, "checkout"))

        val cyclicGraph = mapOf(
            "A" to listOf("B"),
            "B" to listOf("C"),
            "C" to listOf("A"),
        )
        Console.result("cycle detected", hasCycle(cyclicGraph, "A"))
    }

    private fun dynamicProgrammingPattern() {
        Console.section("Dynamic programming")
        val rules = listOf<(Int) -> Int>(
            { amount -> amount - 500 },
            { amount -> if (amount >= 5_000) amount - 700 else amount },
            { amount -> amount - minOf(amount / 10, 1_000) },
        )
        Console.result("best payable amount after campaign combinations", bestCheckoutAmount(10_000, 0, rules))

        val campaigns = listOf(
            Campaign(2, 300_000),
            Campaign(3, 500_000),
            Campaign(1, 150_000),
        )
        Console.result("best campaign mix value with budget 5", chooseBestCampaignMix(campaigns, 5))
    }
}

private fun findFirstDuplicate(orderIds: List<String>): String? {
    val seenIds = mutableSetOf<String>()
    for (id in orderIds) {
        if (!seenIds.add(id)) return id
    }
    return null
}

private fun findMaxTrafficInWindow(requestsPerMinute: IntArray, windowSize: Int): Int {
    var currentTraffic = 0
    for (index in 0 until windowSize) {
        currentTraffic += requestsPerMinute[index]
    }
    var maxTraffic = currentTraffic

    for (index in windowSize until requestsPerMinute.size) {
        currentTraffic += requestsPerMinute[index] - requestsPerMinute[index - windowSize]
        maxTraffic = maxOf(maxTraffic, currentTraffic)
    }
    return maxTraffic
}

private fun minWindowSum(amounts: IntArray, target: Int): Int {
    var left = 0
    var currentSum = 0
    var minLength = Int.MAX_VALUE

    for (right in amounts.indices) {
        currentSum += amounts[right]
        while (currentSum >= target) {
            minLength = minOf(minLength, right - left + 1)
            currentSum -= amounts[left]
            left++
        }
    }

    return if (minLength == Int.MAX_VALUE) 0 else minLength
}

private fun isLetterPalindrome(text: String): Boolean {
    var left = 0
    var right = text.lastIndex

    while (left < right) {
        while (left < right && !text[left].isLetter()) left++
        while (left < right && !text[right].isLetter()) right--
        if (text[left].lowercaseChar() != text[right].lowercaseChar()) return false
        left++
        right--
    }
    return true
}

private fun mergeSortedLogs(list1: List<Int>, list2: List<Int>): List<Int> {
    val merged = mutableListOf<Int>()
    var left = 0
    var right = 0

    while (left < list1.size && right < list2.size) {
        if (list1[left] <= list2[right]) {
            merged.add(list1[left++])
        } else {
            merged.add(list2[right++])
        }
    }

    while (left < list1.size) merged.add(list1[left++])
    while (right < list2.size) merged.add(list2[right++])
    return merged
}

private data class ListNode(
    val value: Int,
    var next: ListNode? = null,
)

private fun List<Int>.toLinkedList(): ListNode? {
    var head: ListNode? = null
    var tail: ListNode? = null
    for (value in this) {
        val node = ListNode(value)
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

private fun ListNode?.toReadableString(): String {
    val values = mutableListOf<Int>()
    var current = this
    while (current != null) {
        values.add(current.value)
        current = current.next
    }
    return values.joinToString(" -> ")
}

private fun middleNode(head: ListNode?): ListNode? {
    var slow = head
    var fast = head

    while (fast?.next != null) {
        slow = slow?.next
        fast = fast.next?.next
    }
    return slow
}

private fun reverse(head: ListNode?): ListNode? {
    var previous: ListNode? = null
    var current = head

    while (current != null) {
        val next = current.next
        current.next = previous
        previous = current
        current = next
    }
    return previous
}

private fun topKFrequent(statuses: List<String>, k: Int): List<String> {
    val count = statuses.groupingBy { it }.eachCount()
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

private fun bfsShortestPath(
    graph: Map<String, List<String>>,
    start: String,
    end: String,
): List<String> {
    val queue = ArrayDeque<List<String>>()
    val visited = mutableSetOf(start)
    queue.add(listOf(start))

    while (queue.isNotEmpty()) {
        val path = queue.removeFirst()
        val node = path.last()
        if (node == end) return path

        for (neighbor in graph[node] ?: emptyList()) {
            if (visited.add(neighbor)) {
                queue.add(path + neighbor)
            }
        }
    }
    return emptyList()
}

private fun dfsReachable(
    graph: Map<String, List<String>>,
    start: String,
    visited: MutableSet<String> = mutableSetOf(),
): Set<String> {
    visited.add(start)
    for (neighbor in graph[start] ?: emptyList()) {
        if (neighbor !in visited) dfsReachable(graph, neighbor, visited)
    }
    return visited
}

private fun hasCycle(
    graph: Map<String, List<String>>,
    node: String,
    visited: MutableSet<String> = mutableSetOf(),
    inStack: MutableSet<String> = mutableSetOf(),
): Boolean {
    visited.add(node)
    inStack.add(node)

    for (neighbor in graph[node] ?: emptyList()) {
        if (neighbor in inStack) return true
        if (neighbor !in visited && hasCycle(graph, neighbor, visited, inStack)) return true
    }

    inStack.remove(node)
    return false
}

private fun bestCheckoutAmount(
    amountYen: Int,
    index: Int,
    rules: List<(Int) -> Int>,
    memo: MutableMap<Pair<Int, Int>, Int> = hashMapOf(),
): Int {
    if (index == rules.size) return amountYen

    val state = amountYen to index
    memo[state]?.let { return it }

    val applyRule = rules[index](bestCheckoutAmount(amountYen, index + 1, rules, memo))
    val skipRule = bestCheckoutAmount(amountYen, index + 1, rules, memo)
    return minOf(applyRule, skipRule).also { memo[state] = it }
}

private data class Campaign(
    val budgetCostUnits: Int,
    val expectedExtraPaymentVolumeYen: Int,
)

private fun chooseBestCampaignMix(campaigns: List<Campaign>, monthlyBudgetUnits: Int): Int {
    val dp = Array(campaigns.size + 1) { IntArray(monthlyBudgetUnits + 1) }

    for (index in 1..campaigns.size) {
        val campaign = campaigns[index - 1]
        for (budget in 0..monthlyBudgetUnits) {
            dp[index][budget] = dp[index - 1][budget]
            if (campaign.budgetCostUnits <= budget) {
                dp[index][budget] = maxOf(
                    dp[index][budget],
                    dp[index - 1][budget - campaign.budgetCostUnits] + campaign.expectedExtraPaymentVolumeYen,
                )
            }
        }
    }

    return dp[campaigns.size][monthlyBudgetUnits]
}
