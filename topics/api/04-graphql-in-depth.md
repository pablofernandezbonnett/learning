# GraphQL In Depth

> Primary fit: `Shared core`


The comparison with REST is in `00-rest-vs-graphql.md`. This doc covers the internals that
matter in practice: schema design, resolvers, the N+1 problem, query protection,
pagination, subscriptions, federation, and Spring for GraphQL.

---

## 1. Schema Definition Language (SDL)

GraphQL is contract-first. The schema is the single source of truth, shared between the
backend (resolvers) and the frontend (code generation, autocomplete).

```graphql
# Scalar types: String, Int, Float, Boolean, ID
# ! means non-nullable

type Product {
  id:         ID!
  name:       String!
  priceYen:   Int!
  category:   String!
  stock:      Int!
  reviews:    [Review!]!       # list of non-nullable Review
  store:      Store            # nullable — product may not be in a store yet
}

type Review {
  id:      ID!
  rating:  Int!
  comment: String
  author:  String!
}

type Store {
  id:   ID!
  name: String!
  city: String!
}

# Entry points — every GraphQL API has these roots:
type Query {
  product(id: ID!): Product           # nullable — may not exist
  products(
    category: String
    maxPrice: Int
    first:    Int = 20
    after:    String            # cursor for pagination
  ): ProductConnection!
}

type Mutation {
  createProduct(input: CreateProductInput!): Product!
  adjustStock(productId: ID!, delta: Int!): Product!
}

type Subscription {
  stockUpdated(productId: ID!): StockEvent!  # real-time via WebSocket
}

# Input types — used as mutation arguments (no resolvers, just DTOs)
input CreateProductInput {
  name:     String!
  priceYen: Int!
  category: String!
}

# Relay-style connection (pagination — see §5)
type ProductConnection {
  edges:    [ProductEdge!]!
  pageInfo: PageInfo!
}
type ProductEdge {
  cursor: String!
  node:   Product!
}
type PageInfo {
  hasNextPage:     Boolean!
  endCursor:       String
}
```

---

## 2. Resolvers — How the Execution Works

Each field in the schema has a **resolver function**. The GraphQL engine calls resolvers
in a tree, starting from the root Query/Mutation.

```
Query.products()          ← root resolver runs first
  └─ Product.reviews()   ← field resolver for each product
       └─ Review.author() ← field resolver for each review
```

**Default resolver:** If you don't define a resolver for a field, GraphQL uses the default —
it looks for a property with the same name on the parent object. This is why simple POJO
fields work without explicit resolvers.

**Spring for GraphQL resolver mapping:**
```kotlin
@Controller
class ProductController(
    private val productService: ProductService,
    private val reviewService: ReviewService,
) {

    // Root query resolver
    @QueryMapping                       // maps to Query.product in the schema
    fun product(@Argument id: String): Product? {
        return productService.findById(id)
    }

    // Field resolver — called for each Product to load its reviews
    @SchemaMapping(typeName = "Product", field = "reviews")
    fun reviews(product: Product): List<Review> {
        return reviewService.findByProductId(product.id)
        // WARNING: this triggers N+1 — see §3 for the fix
    }

    // Mutation resolver
    @MutationMapping
    fun createProduct(@Argument input: CreateProductInput): Product {
        return productService.create(input)
    }
}
```

---

## 3. The N+1 Problem in GraphQL (DataLoader)

The most important GraphQL performance topic. It is the same as the JPA N+1 but at the
resolver level, and it is the primary reason GraphQL backends can be slow.

**The Problem:**
```graphql
query {
  products {         # 1 query: SELECT * FROM products → returns 20 products
    name
    reviews {        # 20 queries: SELECT * FROM reviews WHERE product_id = ?
      rating         # One per product — N+1
    }
  }
}
```

**The Solution: DataLoader (batching + caching)**

DataLoader collects all the individual resolver calls within a single request tick and
executes them as a single batched query.

```
Without DataLoader: 1 + N queries (N = number of products)
With DataLoader:    1 + 1 queries (reviews for all products in one IN clause)
```

**Spring for GraphQL — built-in DataLoader support:**
```kotlin
@Controller
class ProductController(private val reviewService: ReviewService) {

    // Instead of @SchemaMapping calling reviewService directly,
    // use @BatchMapping — Spring wraps this in a DataLoader automatically.
    @BatchMapping(typeName = "Product", field = "reviews")
    fun reviews(products: List<Product>): Map<Product, List<Review>> {
        // Called ONCE per request with ALL products — not once per product
        val allProductIds = products.map { it.id }
        val allReviews = reviewService.findByProductIdIn(allProductIds)
            .groupBy { it.productId }

        return products.associateWith { product ->
            allReviews[product.id] ?: emptyList()
        }
    }
    // Result: 1 query for products + 1 query for all their reviews = 2 total
}
```

**Key points to remember:**
- DataLoader batches requests **within the same request execution context** (not across requests).
- DataLoader also **deduplicates**: if two fields request the same review ID, only one DB
  call is made.
- Spring for GraphQL's `@BatchMapping` handles the DataLoader lifecycle automatically —
  no manual DataLoaderRegistry wiring needed.

---

## 4. Query Protection (Depth, Complexity, Timeouts)

The doc `01-rest-vs-graphql.md` flags that malicious queries can crash the DB. Here is
how you actually prevent that.

**The attack:**
```graphql
# Infinitely nested query if schema allows cycles
{
  user {
    friends {
      friends {
        friends {
          friends { name }  # continues forever
        }
      }
    }
  }
}
```

**Defense 1 — Query Depth Limiting:**
Reject queries whose nesting exceeds a maximum depth.

```kotlin
// Spring for GraphQL — add to application.yml
spring:
  graphql:
    schema:
      inspection:
        enabled: true

// Or programmatically via GraphQlSource customization:
@Bean
fun graphQlSourceBuilderCustomizer(): GraphQlSourceBuilderCustomizer =
    GraphQlSourceBuilderCustomizer { builder ->
        builder.configureGraphQl { graphQlBuilder ->
            graphQlBuilder.instrumentation(
                MaxQueryDepthInstrumentation(10)  // reject queries deeper than 10 levels
            )
        }
    }
```

**Defense 2 — Query Complexity:**
Assign a cost to each field. Reject queries whose total cost exceeds a budget.

```kotlin
// Each field has a cost weight. Lists cost more (they multiply).
// product: cost 1, reviews: cost 5, author: cost 1
// Query cost = 1 + (5 * N) — reject if above 100

graphQlBuilder.instrumentation(
    MaxQueryComplexityInstrumentation(100)
)
```

**Defense 3 — Persisted Queries:**
Only allow a pre-registered set of queries (their hashes). Arbitrary queries from unknown
clients are rejected. Used by Shopify and GitHub for their public APIs.

**Defense 4 — Timeout:**
Treat GraphQL like any other HTTP call — set a server-side request timeout. A stuck resolver
should not hold the thread indefinitely.

---

## 5. Pagination — Relay Connection Pattern

The standard for production GraphQL APIs. Cursor-based (keyset) pagination with a typed
`Connection` wrapper. Same advantages as keyset pagination in SQL (see `../databases/08-query-optimization.md`).

```graphql
query {
  products(first: 20, after: "cursor_abc") {
    edges {
      cursor
      node {
        id
        name
        priceYen
      }
    }
    pageInfo {
      hasNextPage
      endCursor      # pass this as 'after' in the next request
    }
  }
}
```

**Why this shape?**
- `edges` + `cursor` per item allows per-edge metadata (e.g., relevance score in search).
- `pageInfo.endCursor` is the cursor of the last item — use it as `after` for the next page.
- Backed by keyset SQL under the hood — O(log N) regardless of depth.

**Spring for GraphQL — Relay pagination support:**
```kotlin
@QueryMapping
fun products(
    @Argument category: String?,
    @Argument first: Int,
    @Argument after: String?,   // decoded cursor
): Connection<Product> {
    val scrollPosition = after?.let { ScrollPosition.forward(decodeCursor(it)) }
        ?: ScrollPosition.keyset()

    val window = productRepository.findBy(
        ProductSpecifications.inCategory(category),
        { q -> q.limit(first).scroll(scrollPosition) }
    )
    return DefaultConnectionFactory.create(window)  // Spring wraps Window<T> into Connection<T>
}
```

---

## 6. Subscriptions (Real-Time)

Subscriptions use WebSocket (or SSE) to push data to the client when an event occurs.

```graphql
subscription {
  stockUpdated(productId: "J001") {
    productId
    newStock
    updatedAt
  }
}
```

**Spring for GraphQL — subscription mapping:**
```kotlin
@Controller
class StockController(private val stockEventPublisher: StockEventPublisher) {

    @SubscriptionMapping
    fun stockUpdated(@Argument productId: String): Flux<StockEvent> {
        // Returns a reactive stream — Spring bridges it to WebSocket frames
        return stockEventPublisher.subscribe(productId)
    }
}
```

**When to use subscriptions vs polling:**
- Subscriptions: high-frequency real-time data (stock ticker, live order status, chat).
- Polling: low-frequency updates where small delay is acceptable. Simpler, no WebSocket
  connection management.
- SSE (Server-Sent Events): one-way server push. Simpler than WebSocket for read-only streams.

---

## 7. GraphQL Federation (Microservices)

When you have multiple backend services, each owns a slice of the schema. A **Gateway** stitches
them together into a unified graph for the client.

```
Client → Gateway (Apollo Router / Spring Cloud Gateway)
             ├─ Product Service  → owns Product, Category types
             ├─ Order Service    → owns Order, OrderItem types
             └─ User Service     → owns User, Address types
```

Each service publishes its own **subgraph schema**. The gateway merges them. A query spanning
`User` and `Order` is automatically split and executed in parallel against the two services.

**The `@key` directive** declares what uniquely identifies an entity across subgraphs:
```graphql
# In User Service subgraph
type User @key(fields: "id") {
  id:    ID!
  name:  String!
  email: String!
}

# In Order Service subgraph — can extend User with order-related fields
extend type User @key(fields: "id") {
  id:     ID! @external   # declared in User service, referenced here
  orders: [Order!]!       # Order service resolves this field
}
```

**Interview framing:**
"Federation is the GraphQL answer to the same problem API Gateway solves for REST — a single
entry point that aggregates multiple downstream services. The key difference is that with
Federation, the schema itself is distributed: each service owns and evolves its slice
independently, and the gateway composes them. The risk is increased gateway complexity and
the need for all subgraphs to agree on entity `@key` contracts."

---

## 8. Caching in GraphQL

HTTP-level caching (CDN) does not work for `POST /graphql` requests. The alternatives:

| Strategy | How | When |
|---|---|---|
| **Persisted Queries** | Client sends a hash; server resolves to the full query. GET request = CDN-cacheable | Public storefronts, Shopify |
| **Response caching** | Cache the full query response in Redis by query hash + variables | Read-heavy, low-variability queries |
| **DataLoader caching** | Per-request cache — deduplicates resolver calls within one request | Always (use @BatchMapping) |
| **Field-level TTL** | Annotate schema fields with cache hints (`@cacheControl(maxAge: 60)`) | Apollo Router with cache headers |
| **Client-side cache** | Apollo Client normalizes results into an entity cache by `id` | Frontend concern |

---

## Interview Answer — GraphQL Trade-offs

**"We're building a new API for our mobile app and internal dashboard. Should we use GraphQL?"**

"I'd use GraphQL as a BFF (Backend for Frontend) layer between the clients and our internal
REST/gRPC microservices. The mobile app has bandwidth constraints and highly variable data
needs across screens — GraphQL eliminates over-fetching and avoids round trips.

The three things I'd make sure we have from day one: DataLoader via `@BatchMapping` to prevent
N+1 queries at the resolver level, query depth and complexity limits to protect the backend
from malicious or accidental expensive queries, and cursor-based pagination following the
Relay Connection spec.

I would NOT use GraphQL for service-to-service communication between microservices — REST or
gRPC is simpler and more appropriate there. And I'd be cautious about subscriptions unless
we have a clear real-time requirement — WebSocket connection management adds operational
overhead."
