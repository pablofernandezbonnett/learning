# URL Shortener Exercise

Design a service that accepts a destination URL, returns a short link, and redirects visitors to the destination. Start with the [general system design method](../../topics/system-design/00-general-system-design-method.md). Try the prompts before reading the guide.

## Tasks

1. State who creates links, who follows them, and the first version you would build.
2. Decide whether creation needs an account, whether identical destinations share a code, and whether links expire.
3. Define the create and redirect contracts, including invalid input, unknown code, and expired code.
4. Choose a source of truth and explain how concurrent creation avoids code collisions.
5. Walk through a database outage, retry, abusive creator, and a hot redirect.
6. Set a traffic assumption, identify the first likely bottleneck, and name the measurements that would change your design.

## Solution guide

**First model:** the system stores `code -> destination`. Creation validates a URL, reserves a unique code, stores the mapping, and returns the short link. Resolution looks up the code and redirects only if the record is live. Sharing the link is outside the service.

**One defensible MVP:** accept well-formed HTTPS destinations, assign opaque codes, store the mapping and expiry in a relational database, and return a temporary redirect. A code is never reassigned. Distinguish never-known codes from expired ones only if the product needs that distinction; retaining tombstones has a storage cost.

```text
create(destination, requestKey):
  validate scheme, host, and size; do not fetch the destination
  begin transaction
    if requestKey was completed: return stored outcome
    generate opaque code; insert with a uniqueness constraint
    on collision: generate another code and retry within a bound
  commit and return short URL

resolve(code, now):
  read mapping
  if absent: return not found
  if expired: return expired response
  return temporary redirect with cache lifetime <= remaining link lifetime
```

**Failure and scale checks:** creation must not report success before the mapping is durable. A retry needs a stable request identity if duplicate creation matters. Cache only redirects whose expiry can be honored; monitor hit rate, resolution latency, database load, creation errors, and abuse signals. Add a cache, analytics pipeline, or regional replicas when an observed requirement earns them.

## Review your answer

Can you explain the public contract, the invariant for code uniqueness, the source of truth, expiry during cache hits, retry behavior, and how the service behaves when its store is unavailable? If your solution includes a component without a named requirement or failure it addresses, remove it from the first version.
