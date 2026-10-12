# How GraphQL Runs

Read [REST or GraphQL?](./00-rest-vs-graphql.md) for the contract choice. This note explains the GraphQL mechanisms that affect correctness and operating cost.

## Schema and Execution

A schema names types, fields, and the operations clients may request. A query selects fields from that schema; the response follows the selected shape. Resolvers fetch or compute the values. A typed schema helps clients validate queries, but it does not decide whether a particular caller may see a value.

```graphql
type Product {
  id: ID!
  name: String!
  reviews(first: Int!): [Review!]!
}

type Review {
  id: ID!
  rating: Int!
}

type Query {
  product(id: ID!): Product
}
```

`!` means a value is non-null in the schema. A nullable `product` allows “not found.” Changing a field from nullable to non-null, removing a field, or changing its meaning can affect clients; evolve the schema with real client usage in view.

## Avoid Repeated Data Fetches

Suppose a query returns 20 products and each product's reviews. A naive resolver can make one product query plus 20 review queries. This is the N+1 problem: the number of data fetches grows with the returned items.

Batching groups review IDs into one data fetch, then maps the results back to products. Request-scoped caching can avoid fetching the same ID twice during one operation. Check the actual query plan or data-source calls; adding a batching library without measuring can hide another bottleneck.

## Bound Query Cost

The client can choose a shape, but the server must bound its work. A deeply nested query, a large page size, or repeated expensive fields can exhaust a service even when the query is valid.

Set limits that fit the data source: pagination bounds, depth or complexity rules, execution time budgets, and rate limits where needed. Apply authorization to the business data a resolver exposes, including nested fields. Test both normal and deliberately expensive queries.

For lists, use a stable ordering and a cursor or other pagination method that remains predictable as data changes. Define what happens when an item is inserted or deleted between pages.

## Caching and Delivery

GraphQL commonly uses one HTTP endpoint. Queries are often sent with `POST`; a server may also accept `GET` for read-only queries. `GET` plus suitable response headers can enable HTTP caching. Persisted query identifiers can keep URLs short. Client object caches and server response caches solve different problems; include identity, variables, and authorization scope in any cache key that needs them.

Subscriptions add a long-lived delivery channel for updates. Use them only when the product needs live changes and the service can manage connections, authorization changes, and reconnect behavior. A normal query plus refresh may be simpler.

## Combining Services

A GraphQL boundary may read from several services. This does not make remote calls free: a single client query can fan out, fail partly, or wait on the slowest dependency. Name the owner of each field, set time budgets, and observe downstream calls. Federation is one way to compose schemas across teams, with extra coordination over shared entities and schema changes; use it when separate ownership justifies that cost.

## Review Questions

- Who may read each sensitive field and nested relationship?
- How many data-source calls does a typical and worst allowed query make?
- What limits page size, nesting, and total work?
- How do schema changes reach existing clients safely?
- Which cache is useful, and what keeps it scoped to the right caller?
- What happens when one downstream service is slow or unavailable?

## Further Reading

- [GraphQL schema design](https://graphql.org/learn/schema-design/): types and compatible evolution.
- [GraphQL over HTTP](https://graphql.org/learn/serving-over-http/): methods and HTTP behavior.
- [GraphQL performance](https://graphql.org/learn/performance/): batching, cost, and caching.
