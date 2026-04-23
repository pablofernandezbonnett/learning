package learning.livecoding.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PaginationMergeExercise implements Exercise {
    @Override
    public String id() {
        return "pagination-merge";
    }

    @Override
    public String title() {
        return "Merge Sorted Pages with Two Pointers";
    }

    @Override
    public String summary() {
        return "Walk two already-sorted pages with two pointers, take the newer item each time, skip duplicate IDs, and stop at the response limit.";
    }

    record FeedItem(String id, long updatedAt, String source) {
    }

    static List<FeedItem> mergePages(List<FeedItem> firstPage, List<FeedItem> secondPage, int limit) {
        int firstIndex = 0;
        int secondIndex = 0;
        List<FeedItem> merged = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        while (merged.size() < limit && (firstIndex < firstPage.size() || secondIndex < secondPage.size())) {
            FeedItem candidate;
            if (firstIndex >= firstPage.size()) {
                candidate = secondPage.get(secondIndex++);
            } else if (secondIndex >= secondPage.size()) {
                candidate = firstPage.get(firstIndex++);
            } else if (firstPage.get(firstIndex).updatedAt() >= secondPage.get(secondIndex).updatedAt()) {
                candidate = firstPage.get(firstIndex++);
            } else {
                candidate = secondPage.get(secondIndex++);
            }

            if (seenIds.add(candidate.id())) {
                merged.add(candidate);
            }
        }

        return merged;
    }

    @Override
    public void run() {
        List<FeedItem> warehousePage = List.of(
            new FeedItem("item-1", 300, "warehouse"),
            new FeedItem("item-3", 250, "warehouse"),
            new FeedItem("item-5", 180, "warehouse")
        );
        List<FeedItem> storefrontPage = List.of(
            new FeedItem("item-2", 280, "storefront"),
            new FeedItem("item-3", 250, "storefront"),
            new FeedItem("item-4", 220, "storefront")
        );

        List<FeedItem> merged = mergePages(warehousePage, storefrontPage, 4);
        ExerciseSupport.expectEquals(
            "merged-feed-order",
            List.of("item-1", "item-2", "item-3", "item-4"),
            merged.stream().map(FeedItem::id).toList()
        );
    }
}
