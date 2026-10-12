# HTTP Caching, ETag, and Compression

HTTP caches reuse responses in a browser or intermediary. This note covers
the protocol controls. For deciding what may be stale and how an application
cache handles misses, see [cache strategy](../architecture/07-caching-strategies.md).

## Decide Who May Reuse the Response

| Directive | Practical meaning |
| --- | --- |
| `Cache-Control: public, max-age=60` | A shared cache may reuse a suitable response while it is fresh for up to 60 seconds |
| `Cache-Control: private` | A shared cache must not store the response; a private cache may |
| `Cache-Control: no-store` | Caches must not store the response |

Choose the directive from the response's data and audience. A public product
description can be shared; a personalized account response needs stricter
handling. If a response varies by a request header, such as language, make
that variation visible to the cache with `Vary`. Do not assume an application
or CDN will infer the right user scope automatically.

## Revalidate With ETag

An `ETag` identifies one representation version. A client with a cached copy
can ask whether it is still current:

```http
GET /products/sku-123 HTTP/1.1
If-None-Match: "product-42"
```

If the selected representation still matches, the server can return `304 Not
Modified` without sending the body. Otherwise it returns the new response
and its new `ETag`. This saves transfer, though the validation request still
reaches a server or cache.

For a write, `If-Match` can require that the client's version is still
current. Rejecting a stale version with `412 Precondition Failed` avoids
silently overwriting a newer representation. That is a concurrency control
decision, distinct from speeding up reads.

## Compress Only Where It Helps

Compression reduces transfer size for suitable responses but uses CPU. It
usually helps larger text payloads more than tiny or already compressed
content. Measure the actual bytes and latency before adding complexity.

Do not compress confidential content together with attacker-controlled text
without considering length-based information leaks. The [HTTP semantics
specification](https://www.rfc-editor.org/rfc/rfc9110.html#section-17.6)
explains this risk.

## Review Questions

1. Can a shared cache store this response, or is it specific to one user?
2. What is the allowed freshness window?
3. Does `Vary` cover request headers that change the representation?
4. Would `ETag` save enough transfer or prevent a lost update?
5. Does compression reduce real cost without exposing sensitive data?

## Further Reading

- [HTTP caching specification](https://www.rfc-editor.org/rfc/rfc9111.html).
- [HTTP conditional requests](https://www.rfc-editor.org/rfc/rfc9110.html#section-13).
