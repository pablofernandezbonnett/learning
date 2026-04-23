# API Design: REST vs GraphQL

> Primary fit: `Shared core`


When designing frontend-to-backend communication, especially for web or mobile clients, choosing between REST and GraphQL is a major architectural decision.

GraphQL is widely used for client-driven data fetching. Most backend APIs still use REST. This note helps compare the tradeoffs in a practical way.

---

## 1. REST (Representational State Transfer)

REST is the standard. It treats data as "resources" bound to unique URLs (e.g., `/users/123`).

### How it works:
You make specific requests to specific endpoints, using HTTP verbs (GET, POST, PUT, DELETE) to signify the action.

*   `GET /users/12` -> Returns user profile.
*   `GET /users/12/orders` -> Returns user orders.

### Pros of REST
*   **Simplicity & Standardization:** Everyone knows how it works. HTTP status codes (200, 404, 500) have universal meaning.
*   **Excellent Caching:** Because URLs represent specific resources, CDNs (Cloudflare) and browsers can cache responses easily. `GET /products/shoes` is highly cacheable.
*   **Decoupled:** The frontend requests what it wants based on the endpoint, the backend controls the shape of the data returned.

### Cons of REST
*   **Over-fetching:** If a mobile app only needs the user's `name` and `avatar`, but `GET /users/12` returns 50 fields (address, age, preferences), you are wasting bandwidth.
*   **Under-fetching (N+1 Problem):** If a UI needs a user profile *and* their most recent orders, you often have to make two sequential requests (`GET /users/12` then `GET /users/12/orders`).
*   **Versioning is messy:** You often end up with `/api/v1/users` and `/api/v2/users` when data models change radically.

---

## 2. GraphQL

Created by Facebook, GraphQL is a query language for APIs. Instead of multiple endpoints, you usually have a **single endpoint** (e.g., `POST /graphql`).

### How it works:
The client sends a query specifying *exactly* the data it wants. The response mirrors the shape of the query.

**Client Query:**
```graphql
query {
  user(id: "12") {
    name
    avatarUrl
    orders(limit: 2) {
      id
      totalAmount
    }
  }
}
```

**JSON Response:**
```json
{
  "data": {
    "user": {
      "name": "Pablo",
      "avatarUrl": "https://...",
      "orders": [
        { "id": "A1", "totalAmount": 50 },
        { "id": "B2", "totalAmount": 120 }
      ]
    }
  }
}
```

### Pros of GraphQL
*   **No Over-fetching or Under-fetching:** The client gets exactly what it asks for in a single round trip. This is huge for mobile networks and massive SPAs.
*   **Strongly Typed Definition:** GraphQL has a defined schema (in the backend). Tools on the frontend can auto-complete and validate against it.
*   **No Versioning:** You simply add new fields to the schema or deprecate old ones without breaking existing queries.

### Cons of GraphQL
*   **Complexity:** It's fundamentally harder to implement securely on the backend. You have to write "Resolvers" for every field.
*   **Caching is Hard:** Because everything goes to a single `POST /graphql` endpoint, HTTP-level caching (CDNs) doesn't work out of the box. You have to cache at the application or database level, which is much more complex.
*   **Performance Risks:** Malicious clients can crash your database by requesting deeply nested recursive queries (e.g., User -> Friends -> Friends -> Friends).

---

## When to use which? (The Interview Answer)

**Use REST when:**
*   You have simple, resource-based data (e.g., simple CRUD apps).
*   You are building public APIs for third-party developers (Stripe, Twilio use REST because it's predictable).
*   Microservices are communicating with *each other* internally.
*   Heavy HTTP edge caching (CDN) is required for performance.

**Use GraphQL when:**
*   You are building a Backend-for-Frontend (BFF), sitting between microservices and a complex Next.js/React or Mobile app.
*   The frontend requires highly variable shapes of data across different views.
*   Bandwidth optimization is critical (mobile apps).

**The Architectural Sweet Spot:**
Many companies (like Shopify) use GraphQL as the public-facing API for their storefronts (so the UI only fetches exactly what it needs), but their internal microservices talk to each other using REST or gRPC.
