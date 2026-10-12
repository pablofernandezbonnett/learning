# Hexagonal Architecture: Protect the Use Case

Hexagonal architecture keeps business behavior separate from transport,
storage, and provider details. Its useful rule is dependency direction: code
that makes a business decision should not need to know how HTTP, a database,
or an SDK implements it.

## Ports and Adapters

- An **inbound adapter** translates an external request into a use-case call:
  for example, an HTTP controller, message consumer, or CLI command.
- An **inbound port** describes what that use case offers. It may be an
  interface or simply a clear application method; another interface is not
  required for every use case.
- An **outbound port** describes what the use case needs from outside, such as
  finding an order or sending a notification. The application owns its shape.
- An **outbound adapter** implements that port using a database, queue, or
  provider API.

The flow is:

```text
HTTP / message / CLI -> adapter -> use case -> outbound port -> database / provider adapter
```

Adapters convert external data and errors at the boundary. Keep transport
objects, persistence entities, and SDK types out of the use-case contract
when they would couple the business decision to that technology.

## Small Example

A use case that reads an order needs only an order lookup. In Java-like
pseudocode:

```java
interface OrderReader {                 // outbound port, owned by the application
    Optional<Order> find(OrderId id);
}

final class GetOrder {                  // use case
    private final OrderReader orders;

    OrderView get(OrderId id) {
        Order order = orders.find(id).orElseThrow(OrderNotFound::new);
        return OrderView.from(order);
    }
}
```

An HTTP adapter parses the ID, calls `GetOrder`, and maps the result or
`OrderNotFound` to an HTTP response. A database adapter implements
`OrderReader` and maps stored rows to the model. The use case depends on
neither adapter. This example focuses on dependency direction; a use case
that writes to several systems also needs an explicit partial-failure and
recovery design. See [distributed workflows](./03-distributed-transactions-and-events.md).

## Apply It Where It Helps

1. Pick a use case whose rules are obscured by a controller, repository,
   framework annotation, or provider SDK.
2. Move the decision into application or domain code and name its external
   needs as ports.
3. Keep protocol and storage mapping in adapters.
4. Test the decision with a fake port; test each adapter against its real
   boundary where mapping, queries, or error handling matter.

Do not create an interface and adapter for every trivial helper. The boundary
earns its place when it keeps a meaningful rule independent or makes a change
safer. [DDD](./09-domain-driven-design.md) helps choose the business model and
its boundaries; hexagonal architecture controls dependencies around it.
