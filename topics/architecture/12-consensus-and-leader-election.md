# Consensus & Leader Election

## Why You Need It

Many distributed systems need exactly one node to do a specific job at a time:
- One scheduler running cron jobs (don't charge customers twice)
- One primary database accepting writes (don't split-brain)
- One cache warmer running at startup (don't flood the DB)
- One Kafka partition leader handling producer writes

Without coordination, two nodes might both believe they are the leader simultaneously — **split-brain** — causing duplicate processing, data corruption, or conflicting writes.

**Leader election** is the mechanism by which distributed nodes agree on a single leader. The algorithm that makes this agreement reliable under network failures is called **consensus**.

---

## The Core Problem: Split-Brain

```
Normal:                          Network partition:

  Node A (Leader) ←──────→ Node B    Node A (Leader) | Node B (thinks A is dead)
  Node A (Leader) ←──────→ Node C    Node A (Leader) | Node C (thinks A is dead)

                                      B and C elect Node B as new leader.
                                      Now BOTH A and B think they are leaders.
                                      → Split-brain
```

A robust consensus algorithm prevents split-brain by requiring a **quorum** (majority vote) before any leader is elected or any decision is committed. With 3 nodes, a quorum is 2. With 5 nodes, a quorum is 3. A partition that isolates 2 of 5 nodes cannot elect a new leader — it doesn't have a majority.

---

## Raft — The Readable Consensus Algorithm

Raft is the consensus algorithm used by etcd (Kubernetes), CockroachDB, and TiKV. It was designed to be understandable (unlike its predecessor, Paxos).

### Roles

Every node is always in one of three states:
- **Follower**: passive; receives log entries from the leader
- **Candidate**: running for election
- **Leader**: the single active leader; handles all writes; replicates to followers

### Election Process

1. Initially all nodes are Followers.
2. Each Follower has a random **election timeout** (e.g., 150–300ms). When the timeout expires without receiving a heartbeat from a leader, the Follower becomes a Candidate.
3. The Candidate increments its **term** number, votes for itself, and sends `RequestVote` RPCs to all other nodes.
4. Nodes grant a vote if: the candidate's term is ≥ their own, and they haven't voted in this term yet.
5. If the Candidate gets votes from a **majority** (quorum), it becomes the new Leader.
6. The new Leader sends heartbeats immediately to reset all Followers' election timers.

### Log Replication

Once elected, the Leader handles all client writes:
1. Leader appends the entry to its local log.
2. Leader sends `AppendEntries` RPC to all Followers in parallel.
3. Once a **majority** of nodes have written the entry, the Leader commits it.
4. Leader sends the commit index to Followers, who apply the entry to their state machines.

**Why majority?** If the leader crashes after committing to 3 of 5 nodes, any newly elected leader must have been voted in by a majority — at least one of those 3 nodes is in the majority, so the new leader will always have the committed entry.

### Term Number

A term is a monotonically increasing integer. If any node receives a message with a higher term, it immediately reverts to Follower. This prevents stale leaders from causing split-brain — an old leader that was partitioned and recovers will see a higher term and stand down.

---

## etcd — Raft in Production

etcd is the key-value store that Kubernetes uses to store all cluster state (which Pods are running, which nodes exist, ConfigMaps, Secrets). It runs as a 3 or 5-node Raft cluster.

**You interact with etcd through Kubernetes — you never call etcd directly.** But it's the foundation.

For application-level leader election, you use etcd (or its abstraction in Kubernetes) via a lease/lock mechanism.

### Kubernetes Lease — Application Leader Election

Spring Integration and many frameworks implement leader election using a Kubernetes Lease object — a lightweight CRD that only one Pod can hold at a time.

```kotlin
// Spring Integration Kubernetes Leader Election
// build.gradle.kts
implementation("org.springframework.integration:spring-integration-kubernetes")

// Configuration
@Configuration
class LeaderConfig {
    @Bean
    fun leaderInitiator(
        kubernetesClient: KubernetesClient,
        eventPublisher: ApplicationEventPublisher,
    ): LeaderInitiator = LeaderInitiator(
        kubernetesClient,
        DefaultLeaderElectionConfiguration("my-app-leader"),
    ).apply {
        setCandidate(object : Candidate {
            override fun getRole() = "scheduler"
            override fun getId() = System.getenv("POD_NAME")
            override fun onGranted(ctx: Context) {
                eventPublisher.publishEvent(OnGrantedEvent(this, ctx, role))
                // start the job that only one instance should run
            }
            override fun onRevoked(ctx: Context) {
                eventPublisher.publishEvent(OnRevokedEvent(this, ctx, role))
                // stop the job
            }
        })
    }
}

// Listener
@Component
class SchedulerService {
    private var isLeader = false

    @EventListener(OnGrantedEvent::class)
    fun onLeaderGranted() {
        isLeader = true
        startInventorySyncJob()
    }

    @EventListener(OnRevokedEvent::class)
    fun onLeaderRevoked() {
        isLeader = false
        stopInventorySyncJob()
    }
}
```

### Redis-Based Leader Election (Simpler, Non-Kubernetes)

For non-Kubernetes environments, Redis `SET NX PX` is the simplest leader election mechanism. It's not true consensus (Redis single node has no quorum), but it's practical for many use cases.

```kotlin
@Service
class RedisLeaderElection(private val redisTemplate: StringRedisTemplate) {
    private val instanceId = UUID.randomUUID().toString()
    private val leaderKey = "app:leader"
    private val ttlSeconds = 30L

    fun tryAcquireLeadership(): Boolean {
        // SET app:leader <instanceId> NX PX 30000
        // NX = only set if key doesn't exist
        // PX = expiry in milliseconds
        val acquired = redisTemplate.opsForValue()
            .setIfAbsent(leaderKey, instanceId, Duration.ofSeconds(ttlSeconds))
        return acquired == true
    }

    fun renewLeadership(): Boolean {
        // Only renew if we still hold the lock (Lua script for atomicity)
        val script = """
            if redis.call("get", KEYS[1]) == ARGV[1] then
                return redis.call("pexpire", KEYS[1], ARGV[2])
            else
                return 0
            end
        """.trimIndent()
        val result = redisTemplate.execute(
            RedisScript.of(script, Long::class.java),
            listOf(leaderKey),
            instanceId,
            (ttlSeconds * 1000).toString()
        )
        return result == 1L
    }

    @Scheduled(fixedDelay = 10_000)
    fun leaderHeartbeat() {
        if (!renewLeadership()) {
            // Lost leadership — another instance took over
            onLeadershipLost()
        }
    }
}
```

**Limitation:** If the Redis node goes down, the lock is lost and all instances race to become leader simultaneously. For strong guarantees, use Redlock (multi-node Redis) or etcd.

---

## ZooKeeper — The Original Distributed Coordinator

ZooKeeper (Apache) predates etcd and uses a Paxos-variant (Zab). Still widely used in:
- Apache Kafka (older versions used ZooKeeper for broker leader election; Kafka 3.x+ uses KRaft — Kafka's own Raft)
- Apache HBase, Apache Solr
- Legacy Netflix infrastructure

**ZooKeeper ephemeral nodes** are the classic leader election pattern:
1. All candidates create an ephemeral sequential node under `/election/candidate-`.
2. The node with the **lowest sequence number** is the leader.
3. Each follower watches the node just below it in sequence.
4. When the leader's session expires (it dies), ZooKeeper deletes its ephemeral node.
5. The candidate watching that node is notified and becomes the new leader.

```
ZooKeeper znodes:
/election/candidate-0001 ← Leader (lowest)
/election/candidate-0002 ← watches 0001
/election/candidate-0003 ← watches 0002
```

Modern applications prefer etcd or Kubernetes Leases over ZooKeeper for new systems, but you'll see ZooKeeper references in Kafka context.

---

## Kafka Leader Election (KRaft)

Every Kafka topic-partition has exactly one **partition leader** that handles all reads and writes. Followers replicate from the leader. If the leader broker fails, one of the in-sync replicas (ISR) is elected as the new leader.

**Before Kafka 3.3:** ZooKeeper managed broker and partition leader election.
**After Kafka 3.3 (KRaft mode):** Kafka uses its own built-in Raft implementation. No external ZooKeeper needed. One Kafka broker acts as the **active controller** — elected via Raft — and manages partition leader assignments.

From a Spring Boot developer perspective, this is transparent — you configure `bootstrap.servers` and the Kafka client handles the rest.

---

## CAP Theorem and Consensus

Consensus algorithms make a specific CAP trade-off: **Consistency + Partition tolerance over Availability** (CP systems).

- etcd/Raft: if the cluster loses quorum (network partition isolates majority), it **stops accepting writes** rather than risk split-brain. Correct but unavailable during the partition.
- Redis (single node): always available, but **no partition tolerance** — if Redis goes down, no coordination.
- Cassandra (Dynamo-style): AP — always available, eventually consistent. Not suitable for leader election.

**Interview phrasing:** "We use etcd for leader election because it's a CP system — it gives us strong consistency guarantees. We accept that if we lose quorum (2 of 3 etcd nodes) we can't elect a new leader, but we'll never have split-brain. For caching we use Redis, which is AP — availability is more important than perfect consistency for a cache miss."

---

## Retail Interview Scenario

**Question:** "You have a scheduled job that syncs inventory from the warehouse system every 5 minutes. Your service runs 3 replicas in Kubernetes. How do you prevent the sync from running 3 times simultaneously?"

**Answer:**

> Three options depending on constraints:
>
> **Option A — Kubernetes CronJob** (simplest): Define the sync as a Kubernetes CronJob, not a scheduled job inside the service. Kubernetes launches exactly one Pod per schedule. No leader election needed. Best for jobs that aren't time-critical to the second.
>
> **Option B — Kubernetes Lease** (inside the service): Use Spring Integration's Kubernetes leader election. Only the Pod that holds the Lease runs the scheduler. When that Pod crashes, another Pod acquires the Lease (Kubernetes etcd-backed, so it's safe) and takes over within seconds. No duplicate runs.
>
> **Option C — Redis distributed lock** (if not on Kubernetes): At the start of each sync, try `SET sync:lock <instanceId> NX PX 300000` (5-minute TTL). Only the instance that acquires the lock proceeds. The lock expires automatically if the instance crashes mid-sync. Simpler but less rigorous than Raft-backed consensus.
>
> I'd go with Option B for a production service running on Kubernetes — the Lease approach gives us proper leader lifecycle events (onGranted/onRevoked) and the etcd backing means we never have split-brain.

---

## Quick Reference

| Tool | Algorithm | Use case |
|---|---|---|
| etcd | Raft | Kubernetes cluster state; application leader election via Lease |
| ZooKeeper | Zab (Paxos-variant) | Legacy Kafka, HBase, Solr |
| Kafka KRaft | Raft | Kafka broker controller election (Kafka 3.3+) |
| Redis SET NX | No consensus | Simple distributed lock / election (non-critical) |
| Redlock | Multi-node Redis | Stronger Redis-based locking (still not Raft) |

| Concept | One-liner |
|---|---|
| Quorum | Majority vote required — prevents split-brain |
| Term (Raft) | Monotonically increasing epoch — detects stale leaders |
| Ephemeral node (ZooKeeper) | Auto-deleted when session expires — natural leader failover |
| Split-brain | Two leaders simultaneously — prevented by quorum requirement |
| CP vs AP | Leader election is always CP — consistency over availability |
