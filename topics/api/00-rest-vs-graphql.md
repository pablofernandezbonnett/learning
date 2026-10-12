# REST or GraphQL?

Use this comparison when clients need data from an API and the contract shape is still a choice. Both approaches need clear authorization, compatible evolution, performance limits, and monitoring.

## Smallest Useful Model

- A resource-oriented HTTP API gives each resource or operation a URL. The server defines the response shape; ordinary HTTP methods, status codes, and caching rules help clients use it.
- GraphQL exposes a typed schema. A client selects fields and can compose related data in one query. The server still decides which data a caller may see and how much work a query may cause.

For example, a product screen might call `GET /products/12` and `GET /products/12/reviews` through a resource API. A GraphQL query could select the product name and three reviews together. The latter gives the client a flexible shape, but the server must control the cost of resolving it.

## Choose by the Client's Need

Start with a resource-oriented HTTP API when resources and operations are clear, response shapes are stable enough, and HTTP caching or a simple public contract matters. Add a purpose-built endpoint when one screen would otherwise make several awkward calls.

Consider GraphQL when several clients need substantially different views of related data and maintaining a separate endpoint for each view would create more cost than operating a query layer. It can be useful at a frontend aggregation boundary, but it is not required there.

Before choosing GraphQL, decide who owns:

- field authorization and schema evolution
- query depth or cost limits and pagination
- batching of data fetches to avoid repeated downstream calls
- caching at the client, server, or HTTP edge

GraphQL supports additive schema evolution, but removing or changing fields can still break clients. GraphQL queries usually use `POST`; servers may support `GET` for read-only queries, which can enable HTTP caching with suitable headers. A single endpoint does not make every response uncacheable.

## Strong Default

Use the simpler contract that fits the actual clients. Measure round trips, response size, resolver work, and cache behavior before changing styles. Keep the general [API design method](./01-api-design.md) and [security boundaries](../security/02-web-and-api-security.md) regardless of the query style.

## Further Reading

- [GraphQL over HTTP](https://graphql.org/learn/serving-over-http/): methods and caching options.
- [GraphQL schema design](https://graphql.org/learn/schema-design/): evolution and compatibility.
- [GraphQL performance](https://graphql.org/learn/performance/): query cost and caching.
