# ML and AI Pipelines for JVM Backend Engineers

Use this note when `AI` or `ML` (`machine learning`) enters the conversation
and you need the backend-engineer view, not the data-scientist view.
For the current repo direction, this is a growth note, not the first thing to
study.

---

## Why This Matters

Many teams now say they are "doing AI".

For a backend engineer, the real question is usually not:

- how do I invent or train the best model from scratch

It is closer to:

- where does the model live
- how does data reach it
- how do we serve it safely
- how do we keep the rest of the platform reliable when AI is slow, expensive, or wrong

That is why `AI pipelines` and `ML pipelines` are worth studying from your
profile. They are often backend and platform problems wrapped around a model.

This is not a full professional reset.
For a strong Java backend engineer, it is usually a value-adding layer on top
of the same foundations you already care about:

- APIs
- async boundaries
- cloud runtime choices
- observability
- security
- controlled business workflows

---

## Smallest Useful Mental Model

Split the system into two big paths:

- `offline path`: prepare data, train or update the model, evaluate it, package it, and deploy it
- `online path`: receive a live request, call or run the model, apply product rules, and return a result

Practical translation:

- offline path improves the model
- online path serves the user

Most production failures come from mixing these jobs badly or pretending the
model alone is the system.

---

## Bad Mental Model vs Better Mental Model

Bad mental model:

- AI pipeline means one fancy workflow tool
- Java is not very relevant because Python does the AI part
- once the model answers, the business action can happen directly

Better mental model:

- the pipeline is the full path from data to deployed behavior
- Java and Kotlin stay very relevant in ingestion, API serving, orchestration boundaries, auth, rate limiting, streaming, and observability
- the model output is usually one step inside a larger controlled backend workflow

Small word example:

- weak approach: "the checkout service sends the dispute to the model and auto-refunds if the answer looks confident"
- better approach: "the model classifies the dispute and produces structured output, but policy checks and refund authority stay in deterministic backend rules"

---

## The Pipeline Shape That Matters Most

You do not need every pipeline buzzword first.
You do need to recognize the common stages:

1. data ingestion or collection
2. cleaning, transformation, and feature preparation
3. training or fine-tuning
4. evaluation
5. packaging and model versioning
6. deployment
7. online inference
8. monitoring, feedback, and retraining

Short rule:

> training makes the artifact; serving makes the product behavior

That line sounds obvious, but it prevents a lot of confusion.

---

## Where Java and Kotlin Actually Fit

For your profile, the high-value JVM roles are usually these:

### 1. Serving the model through a normal backend

This is the most obvious fit.

Examples:

- Spring Boot API receives the user request
- backend validates auth, tenant, and input shape
- backend calls the model service or local runtime
- backend applies policy rules and returns the final answer

This is still backend engineering:

- contracts
- latency budgets
- retries
- fallbacks
- rate limits
- caching
- auditability

### 2. Running inference inside the JVM

This is useful when you want to avoid a separate Python process and you already
have a packaged model artifact.

Common patterns:

- load an `ONNX` model into a Java runtime
- use a Java-friendly inference framework such as `DJL` (`Deep Java Library`)

Practical use:

- lower-latency local inference for some models
- easier integration with existing JVM services
- one less service boundary in some deployments

Main caution:

- do not force this just because "one language is cleaner"
- the model format, runtime support, memory profile, and ops burden still decide whether this is a good idea

### 3. Data and event plumbing around the model

Even when training stays outside the JVM, Java/Kotlin may still own:

- event ingestion
- CDC (`change data capture`) consumers
- request enrichment
- workflow state transitions
- downstream writes after human or policy approval

### 4. Orchestration boundaries

The model team may train in Python.
That does not remove the need for:

- scheduled jobs
- backfills
- containerized workflow steps
- artifact promotion
- deployment coordination

---

## Strong Default: Hybrid Architecture

For most backend engineers and most product teams, the strongest default is:

1. train or fine-tune in Python when the ecosystem advantage is real
2. package the result into a stable artifact or separate model service
3. integrate it from Java/Kotlin through a clear boundary
4. keep risky business actions behind deterministic backend checks

That hybrid shape usually gives the best trade:

- Python keeps its ML ecosystem advantage
- JVM backend keeps its strength in APIs, concurrency, product workflows, and operational discipline

Short rule:

> use Python where model work is strongest; use the JVM where product-serving discipline is strongest

---

## Three Good Integration Patterns

### Pattern 1. Java calls a separate model service

Shape:

- train and package model elsewhere
- expose it as HTTP or gRPC
- Java service calls it like any other dependency

Good when:

- model team and backend team move separately
- runtime isolation matters
- GPU or model memory pressure should stay outside the main API process

Tradeoff:

- extra network hop
- another service to operate

### Pattern 2. Java loads a portable model artifact

Shape:

- model is exported to a portable format such as `ONNX`
- JVM service loads it and runs inference directly

Good when:

- the model type is supported well by the runtime
- low-latency in-process inference is valuable
- you want fewer moving parts than a separate model microservice

Tradeoff:

- runtime support decides what is realistic
- model upgrades and memory sizing move into the JVM service boundary

### Pattern 3. Java backend calls a local model runtime

Shape:

- a local model server such as `Ollama` runs separately
- Spring Boot or another JVM service calls it over HTTP
- optional streaming returns partial output to the client

Good when:

- you want fast local experimentation
- you want provider decoupling at the backend boundary
- the model process should fail separately from the main API

Tradeoff:

- local model resource pressure still exists
- you still need request budgets, admission control, and fallbacks

---

## Security And Abuse Controls For AI Serving

This part matters more than many teams expect.

An AI endpoint is still a backend endpoint.
It just has extra failure modes:

- prompt injection
- data leakage through context or logs
- expensive abuse through repeated calls
- unsafe tool or workflow execution
- unsafe trust in model output

Strong default:

- treat model output as untrusted until your backend applies policy or validation
- rate-limit and budget AI endpoints like any other expensive path
- keep secrets, tokens, and internal URLs out of model-visible context unless they are truly needed
- log enough for audit, but do not leak private prompts, documents, or responses carelessly
- never let a model trigger money movement, privileged writes, or sensitive reads without explicit backend checks

Small practical review loop:

1. what data is the model allowed to see?
2. what actions can the model influence?
3. what expensive path could be abused?
4. what deterministic control still stands between model output and the real side effect?

Short rule:

> secure AI serving the same way you secure any dangerous backend path, then add one more rule: never trust the model as the final policy engine

---

## A Practical Ollama Shape

One simple learning setup looks like this:

- web or mobile client calls your Java backend
- your Java backend calls `Ollama`
- `Ollama` runs the local model process
- your backend streams or returns the answer

Why this is a strong learning pattern:

- you keep the model process outside the JVM app
- you can swap local and hosted providers more easily later
- you can study AI integration without pretending the backend should own GPU details directly

Small code example from the Spring side:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: mistral
```

```java
@RestController
class ChatController {

    private final OllamaChatModel chatModel;

    ChatController(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    Map<String, String> generate(@RequestParam String message) {
        return Map.of("generation", chatModel.call(message));
    }
}
```

The code is not the main lesson.
The lesson is the boundary:

- provider runtime outside
- backend policy and API inside

---

## Orchestration: Airflow vs Argo in Plain Language

You do not need to become a workflow-platform specialist first.
You do need a simple map:

### Airflow

Useful mental model:

- workflow orchestration defined as code
- tasks arranged as a `DAG` (`directed acyclic graph`)
- strong fit for scheduled and batch-oriented workflows

Typical use:

- nightly data prep
- retraining jobs
- evaluation jobs
- artifact promotion steps

### Argo Workflows

Useful mental model:

- container-native workflow engine for Kubernetes
- each step is usually a containerized job
- strong fit when the platform already runs heavily on Kubernetes

Typical use:

- containerized training or evaluation jobs
- GPU job orchestration on Kubernetes
- multi-step data or ML workflows inside a K8s-heavy platform

Short rule:

> Airflow is the classic batch workflow answer; Argo is the Kubernetes-native workflow answer

---

## Where Python Fits Without Owning The Whole System

For your profile, Python is usually the best adjacent language here because it
fits naturally into the parts that are not your main long-lived backend:

- experiments
- eval scripts
- data prep
- lightweight model-serving helpers
- orchestration code around workflow tools

That does not mean:

- move your whole product backend into Python

It means:

- let Python do fast glue work where the ecosystem advantage is real
- let Java and Kotlin keep owning the stronger product-serving boundary where that is still the better fit

If you build a small Python API for model serving, keep the same bar you would
expect elsewhere:

- typed request and response models
- explicit auth boundary
- obvious side effects
- rate limits and request size limits
- tests for malformed and abusive input

Pair this note with:

- [../python/README.md](../python/README.md)
- [../python/04-project-shape-and-quality.md](../python/04-project-shape-and-quality.md)

---

## Why This Reinforces Your Cloud Learning

AI and ML systems often become cloud and platform problems very quickly.

Why:

- model artifacts need storage and versioning
- online inference needs runtime sizing and traffic control
- batch and retraining jobs need orchestration
- expensive workloads need stronger observability and cost awareness
- private data and service-to-service access need cleaner identity boundaries

That means your cloud path is not separate from this topic.
It directly reinforces it.

Plain-English version:

- learning AI pipelines well will make your cloud learning more concrete
- learning cloud well will make your AI answers more production-shaped

Pair this note with:

- [../cloud/README.md](../cloud/README.md)
- [../cloud/08-aws-for-backend-engineers.md](../cloud/08-aws-for-backend-engineers.md)
- [../security/02-web-and-api-security.md](../security/02-web-and-api-security.md)
- [../security/03-spring-and-jvm-appsec.md](../security/03-spring-and-jvm-appsec.md)

---

## What Good Looks Like In Practice

In practice, the shape you want to internalize is:

- offline model work stays separate from the online request path
- the integration boundary is explicit
- the backend still owns auth, rate limits, request budgets, fallbacks, observability, and business-rule enforcement around model output

Plain-English version:

- the model can be built elsewhere
- the production responsibility around it still behaves like backend engineering

---

## Main Tradeoffs

AI and ML integration usually adds:

- higher latency variance
- cost variance
- new failure modes outside normal CRUD paths
- model/version drift
- harder testing and rollback

That is why strong defaults matter:

- explicit boundaries
- structured outputs where possible
- evals before trust
- deterministic product rules around high-risk actions

---

## Reusable Takeaway

> For a backend engineer, an AI or ML pipeline is not just model training. It is the full path from data and deployment to safe online serving. The best default is usually hybrid: let the model ecosystem do model work, and let the JVM backend own contracts, integration, reliability, and business control.

---

## Further Reading

- ONNX Runtime overview: https://onnxruntime.ai/
- Deep Java Library overview: https://github.com/deepjavalibrary/djl
- Spring AI Ollama chat reference: https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html
- Apache Airflow overview: https://airflow.apache.org/docs/apache-airflow/stable/index.html
- Argo Workflows overview: https://argo-workflows.readthedocs.io/
- Google Cloud MLOps pipeline guidance: https://cloud.google.com/solutions/machine-learning/mlops-continuous-delivery-and-automation-pipelines-in-machine-learning
