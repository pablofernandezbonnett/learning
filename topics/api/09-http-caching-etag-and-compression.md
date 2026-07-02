# HTTP Caching, ETag, and Compression

Use this note when an API is already functionally correct, but you need it to
behave better under repeat reads, revalidation, and network cost.

This note stays HTTP-first on purpose. Header and cache behavior matter more
than framework-specific Java or Kotlin snippets here.

Why this matters:

- many backend APIs waste bandwidth and latency by returning the same payloads repeatedly
- teams often add caching or compression without deciding what kind of staleness or validation they can tolerate
- `ETag` helps with conditional requests, and compression helps with transfer size, but both need clear boundaries

## Smallest Useful Mental Model

Three different questions live here:

- caching: may a response be reused?
- validation: can a cached copy be confirmed instead of re-downloaded?
- compression: can the same content be transferred in fewer bytes?

These are related, but they are not the same thing.

The HTTP caching spec distinguishes between private caches and shared caches.
That matters because browser-local reuse is not the same as CDN or proxy reuse.

## Bad Mental Model vs Better Mental Model

Bad mental model:

- turn caching on everywhere
- add `ETag` because it sounds modern
- compress every response because smaller is always better

Better mental model:

- first decide which responses are safe to reuse
- then decide whether freshness or validation is the stronger fit
- then compress only where transfer savings beat the CPU or risk cost

## Small Concrete Example

Imagine `GET /products/sku-123`.

This is often a strong candidate for:

- short public caching
- `ETag` validation
- compression for larger JSON responses

A reasonable shape could be:

```http
HTTP/1.1 200 OK
Cache-Control: public, max-age=60
ETag: "product-sku-123-v42"
Content-Encoding: gzip
```

Then a later client request can send:

```http
GET /products/sku-123 HTTP/1.1
If-None-Match: "product-sku-123-v42"
```

If the representation did not change, the server can return:

```http
HTTP/1.1 304 Not Modified
```

That saves bytes without pretending the data lives forever.

## Best Approach or Strong Default

Strong defaults:

- cache read-heavy, low-risk responses first
- keep personalized or fast-changing responses much more conservative
- use `Cache-Control` intentionally instead of relying on guesswork
- use `ETag` when conditional revalidation or optimistic update protection helps
- compress larger text responses where the byte savings are meaningful

Good fits for caching and validation:

- product catalog reads
- static reference data
- versioned documentation responses
- public metadata that changes infrequently

Poor fits for broad caching:

- user-specific account data
- final inventory truth
- payment or authorization state
- any response where stale reuse would create business confusion or risk

## Where ETag Helps Most

`ETag` is useful for two common backend cases:

- conditional `GET` requests with `If-None-Match`
- optimistic concurrency with `If-Match` on updates

Plain-English version:

- an `ETag` is a version fingerprint for one representation

That means it can help you avoid sending the same bytes twice, and it can help
stop one client from overwriting a newer change silently.

## Where Compression Helps Most

Compression is most useful when:

- responses are text-heavy
- payloads are large enough to matter
- network cost or latency matters more than a little extra CPU

Compression is not always a free win.

RFC 9110 explicitly calls out security risks from compression when sensitive
data and attacker-controlled input share the same compression context.

Strong default:

- keep compression on for normal text payloads when it helps
- be much more careful on sensitive responses that mix secrets with attacker-controlled content

## Main Tradeoff or Failure Mode

The main failures are:

- stale data from sloppy cache rules
- weak `ETag` usage with no real revalidation plan
- compressing sensitive mixed-content responses without thinking about side channels

Another common failure is trying to cache around weak domain boundaries.

Example:

- caching live inventory or payment state at the API layer instead of fixing the source-of-truth model first

Caching should make a good read path cheaper.
It should not hide a bad truth model.

## Practical Rule

Ask these questions in order:

1. may this response be reused safely?
2. if yes, for how long?
3. if freshness is uncertain, should the client validate with `ETag`?
4. does compression save enough bytes to matter here?
5. is the response sensitive enough that compression or shared caching needs caution?

## Reusable Takeaway

> HTTP caching, `ETag`, and compression are not knobs to turn on blindly. They
> are reuse, validation, and transfer-cost decisions that only work well when
> response correctness stays explicit.

## Further Reading

- RFC 9111 HTTP Caching: https://www.rfc-editor.org/info/rfc9111/
- RFC 9110 HTTP Semantics: https://www.rfc-editor.org/rfc/rfc9110
