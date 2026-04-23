package learning.livecoding.kotlin

object PaginationMergeExercise : Exercise {
    override val id = "pagination-merge"
    override val title = "Merge Sorted Pages with Two Pointers"
    override val summary =
        "Walk two already-sorted pages with two pointers, take the newer item each time, skip duplicate IDs, and stop at the response limit."

    data class FeedItem(val id: String, val updatedAt: Long, val source: String)

    fun mergePages(
        firstPage: List<FeedItem>,
        secondPage: List<FeedItem>,
        limit: Int,
    ): List<FeedItem> {
        var firstIndex = 0
        var secondIndex = 0
        val merged = mutableListOf<FeedItem>()
        val seenIds = mutableSetOf<String>()

        while (merged.size < limit && (firstIndex < firstPage.size || secondIndex < secondPage.size)) {
            val candidate = when {
                firstIndex >= firstPage.size -> secondPage[secondIndex++]
                secondIndex >= secondPage.size -> firstPage[firstIndex++]
                firstPage[firstIndex].updatedAt >= secondPage[secondIndex].updatedAt -> firstPage[firstIndex++]
                else -> secondPage[secondIndex++]
            }

            if (seenIds.add(candidate.id)) {
                merged += candidate
            }
        }

        return merged
    }

    override fun run() {
        val warehousePage = listOf(
            FeedItem("item-1", updatedAt = 300, source = "warehouse"),
            FeedItem("item-3", updatedAt = 250, source = "warehouse"),
            FeedItem("item-5", updatedAt = 180, source = "warehouse"),
        )
        val storefrontPage = listOf(
            FeedItem("item-2", updatedAt = 280, source = "storefront"),
            FeedItem("item-3", updatedAt = 250, source = "storefront"),
            FeedItem("item-4", updatedAt = 220, source = "storefront"),
        )

        val merged = mergePages(warehousePage, storefrontPage, limit = 4)
        ExerciseSupport.expectEquals(
            "merged-feed-order",
            listOf("item-1", "item-2", "item-3", "item-4"),
            merged.map { it.id },
        )
    }
}
