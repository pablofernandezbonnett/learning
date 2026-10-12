# The Network Path of a Request

A correct handler can still fail because the client cannot resolve a name,
connect, complete TLS, reach a ready instance, or get a response before its
deadline. Trace the path before changing application code.

## Follow One Request

```text
client -> DNS -> network connection -> TLS -> edge or load balancer
       -> application -> dependency -> response
```

- **DNS** resolves a name to an address for the next network hop. A cached
  answer can outlive an endpoint change, so names are not a guarantee of
  reachability.
- **IP and port** identify where a connection goes. **TCP** provides an
  ordered byte stream; **UDP** sends datagrams without built-in delivery or
  ordering guarantees. Higher-level protocols can add their own guarantees.
- **TLS** authenticates the intended endpoint and protects traffic in
  transit. Check where TLS ends and whether later hops also need protection.
- **HTTP** gives request and response semantics. A response may come from an
  edge, proxy, or application; knowing which one matters when diagnosing an
  error.

An application timeout may include connection setup, waiting for a free
connection, server work, and response transfer. Measure the phase that is
slow instead of raising every timeout together.

## Routing and Load Balancing

A layer 4 balancer routes using connection information such as address and
port. A layer 7 proxy can inspect HTTP host, path, or headers and apply
route-specific policy. Either may sit in front of changing application
instances. A gateway can also apply shared authentication checks or rate
limits. Client-specific composition is a separate decision; see [edge
patterns](./17-gateway-vs-bff-vs-edge-patterns.md).

For internal services, a stable name must eventually resolve or route to
current instances. [Service discovery](./11-service-discovery.md) explains
how those instances enter and leave the routing set.

## Choose the Application Protocol

| Need | Common fit | Tradeoff |
| --- | --- | --- |
| Broad client compatibility and ordinary request/response APIs | HTTP with a documented JSON contract | Payloads and contracts need deliberate versioning |
| Typed internal calls or streaming between controlled clients | gRPC with an explicit schema | Generated clients and protocol support add tooling and deployment work |
| Ongoing two-way messages with a connected client | WebSocket | Long-lived connections need capacity and reconnect handling |

HTTP/2 can carry multiple concurrent streams on one connection. That helps
connection use, but it does not remove application deadlines or a slow
dependency. A protocol change is justified by measured call shape or
contract needs, not by service count alone.

For HTTP methods, status codes, retries, and API contracts, see [API
design](../api/01-api-design.md). For request trust and transport security,
see [HTTP and HTTPS for AppSec](../appsec/01-http-and-https.md).

## Diagnose by Phase

| Symptom | First place to look |
| --- | --- |
| Name cannot resolve | DNS configuration and resolver response |
| Connection refused or times out | Address, port, listener, route, and network policy |
| TLS handshake fails | Endpoint identity, certificate, and TLS configuration |
| Edge returns an error before the app sees the request | Proxy routing, limits, and upstream health |
| App sees the request but responds slowly | Handler, database, and outbound dependency timing |
| Client times out after sending a write | Check the server-side outcome before retrying the effect |

The last case is especially important: a lost response does not imply a
failed write. [Resilience patterns](./02-resiliency-patterns.md) cover safe
timeouts and retries.

## Decision Rule

Map each hop and its owner. Separate name resolution, connection, TLS,
routing, and application time so a failure at one layer is not mistaken for
another.

## Further Reading

- [HTTP/2 specification](https://www.rfc-editor.org/rfc/rfc9113.html): streams and multiplexing.
- [gRPC core concepts](https://grpc.io/docs/what-is-grpc/core-concepts/): contracts and call shapes.
