# Worked Case: Search Hotel Offers

An agency wants to show bookable hotel offers, then reserve one chosen by a traveller. The two moments have different guarantees: search helps someone compare options; reservation attempts to commit an offer that may already have changed.

## Start with the contract

Assume authenticated agency users, an internal offer index, and possibly live supplier availability. The agency ID comes from the authenticated session. A caller cannot choose another agency by adding an ID to the request.

```http
GET /v1/hotel-offers?destination=Tokyo&checkIn=2027-04-12&checkOut=2027-04-15&adults=2&limit=20
Authorization: Bearer ...
```

Validate the destination, date order and allowed booking window, occupancy, filters, sort order, and a capped `limit` before querying suppliers. Treat unknown filters as invalid input rather than silently changing the search. The response contains only what the comparison screen needs:

```json
{
  "offers": [
    {
      "offerId": "off_123",
      "hotelId": "hotel_42",
      "totalPrice": { "amount": "42000", "currency": "JPY" },
      "cancellation": "non_refundable",
      "validUntil": "2027-04-12T09:05:00Z"
    }
  ],
  "nextCursor": "opaque-cursor-or-null"
}
```

The amount is illustrative; real money contracts must define units and rounding. `validUntil` states when this quote should stop being displayed as current. It does **not** hold inventory or guarantee that booking will succeed. Avoid an exact `totalCount` unless the UI needs it and the query can afford it.

## Decide what another page means

Use a stable sort with a unique tie-breaker, such as price plus offer ID. Bind the opaque cursor to the original agency, filters, sort, and page size so it cannot be reused for a different search.

- **Live pages:** each page reads current data. This is cheap to start with, but changed prices or availability can move an offer between pages. Document this behavior.
- **Short-lived snapshot:** keep one ordered result set for the search. Paging is more consistent, but the snapshot costs storage, has an expiry, and still does not reserve the offers.

Start with live pages if occasional movement is acceptable. Add a snapshot only if observed user behavior needs stable traversal. If suppliers return their own cursors, the application must decide how to combine them; an internal cursor is not automatically a consistent view of all suppliers.

## Reserve through a separate write

```http
POST /v1/reservations
Idempotency-Key: agency-generated-unique-key
Content-Type: application/json

{"offerId":"off_123","acceptedTotal":{"amount":"42000","currency":"JPY"}}
```

The server checks the authenticated agency, rechecks availability and the current price, and commits only if the accepted terms still match. A successful create returns `201` with the reservation ID. If terms changed, return an actionable conflict such as `409` with the new terms or a fresh-search instruction. The idempotency key lets a caller retry after an uncertain network outcome without creating two bookings; its scope and retention period must be defined. A search response alone never authorizes this write.

## What changes under load or failure?

| Observation | Decision to make |
| --- | --- |
| One supplier stalls | Bound each supplier's timeout and concurrent calls. Decide whether to return clearly marked partial results or fail the whole search. |
| Search becomes slow | Measure database time, supplier latency, and result size before adding a cache or separate search index. |
| Many quotes expire before selection | Shorten the displayed freshness window or fetch a new quote before confirmation; booking still rechecks. |
| Booking conflicts rise | Measure conflict rate by supplier and quote age, then improve quote freshness or the user flow. |

Track search latency, supplier errors and timeouts, validation rejections, cursor failures, and reservation conflicts with request IDs. Keep personal data and credentials out of routine logs. These observations tell you which complexity is actually justified.

For the general method behind this case, see [API design](./01-api-design.md). For choosing between synchronous calls and later completion, see [sync vs async](./07-sync-vs-async-integration-choice.md).
