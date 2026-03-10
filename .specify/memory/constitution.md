<!--
  Sync Impact Report
  ===================
  Version change: 2.0.0 → 3.0.0

  Modified principles: Complete restructure based on AGENTS.md

  Structure now mirrors AGENTS.md sections:
    - Core Responsibilities (from AGENTS.md header)
    - Documentation Strategy (AGENTS.md section)
    - Component Architecture (AGENTS.md section)
    - Critical Patterns & Rules (AGENTS.md section with code examples)
    - Critical Gotchas (AGENTS.md DON'T/DO lists)
    - Self-Review Checklist (AGENTS.md checklist)

  Removed sections:
    - Previous principle-based organization (I-VI)
    - Development Standards (merged into Component Architecture)
    - Quality Gates (merged into Self-Review Checklist)

  Templates requiring updates:
    - .specify/templates/plan-template.md: ✅ compatible
    - .specify/templates/spec-template.md: ✅ compatible
    - .specify/templates/tasks-template.md: ✅ compatible

  Follow-up TODOs: None

  Rationale for MAJOR bump: Complete structural reorganization from principle-based
  to AGENTS.md-mirrored format. This ensures constitution and AGENTS.md stay in sync.
-->

# Akka SDK Constitution

This constitution codifies the rules and patterns as the governing
standard for all Akka SDK development in this project.

## Core Responsibilities

Generate production-ready, best-practice-compliant Akka components with correct:
- Package structure (domain, application, api)
- Naming conventions
- Complete test coverage
- Akka SDK patterns and conventions

**Technology Requirements:**
- Akka SDK version 3.4 or later
- Java 21+ with records for all data structures
- Maven for build and dependency management
- JUnit 5 with AssertJ for assertions

## Documentation Strategy

### Available Documentation

Reference documentation resides in `akka-context/` directory:

| Component | Documentation Path |
|-----------|-------------------|
| Agents | `akka-context/sdk/agents.html.md` |
| Agent Prompts | `akka-context/sdk/agents/prompt.html.md` |
| Agent Calling | `akka-context/sdk/agents/calling.html.md` |
| Agent Memory | `akka-context/sdk/agents/memory.html.md` |
| Agent Structured Responses | `akka-context/sdk/agents/structured.html.md` |
| Agent Failures | `akka-context/sdk/agents/failures.html.md` |
| Agent Tools | `akka-context/sdk/agents/extending.html.md` |
| Agent Streaming | `akka-context/sdk/agents/streaming.html.md` |
| Agent Orchestration | `akka-context/sdk/agents/orchestrating.html.md` |
| Agent Guardrails | `akka-context/sdk/agents/guardrails.html.md` |
| Agent Evaluation | `akka-context/sdk/agents/evaluating.html.md` |
| Agent Testing | `akka-context/sdk/agents/testing.html.md` |
| Dynamic Agent Teams | `akka-context/getting-started/planner-agent/dynamic-team.html.md` |
| Event Sourced Entities | `akka-context/sdk/event-sourced-entities.html.md` |
| Key Value Entities | `akka-context/sdk/key-value-entities.html.md` |
| Views | `akka-context/sdk/views.html.md` |
| Workflows | `akka-context/sdk/workflows.html.md` |
| Consumers & Topics | `akka-context/sdk/consuming-producing.html.md` |
| HTTP Endpoints | `akka-context/sdk/http-endpoints.html.md` |
| gRPC Endpoints | `akka-context/sdk/grpc-endpoints.html.md` |
| Timed Actions | `akka-context/sdk/timed-actions.html.md` |
| Setup & DI | `akka-context/sdk/setup-and-dependency-injection.html.md` |
| Best Practices | `akka-context/sdk/ai-coding-assistant-guidelines.html.md` |

### When to Read Documentation

**MANDATORY - Read documentation BEFORE coding for:**
- **Workflows** - ALWAYS read `workflows.html.md` first time in session
- **Agents** - ALWAYS read `agents.html.md` first time in session
- **First-time component** - Read relevant doc for ANY component type not yet created
- **User-specified features** you're uncertain about
- **API mismatches or errors**

**Use memorized patterns for:**
- Similar components already created in session
- Simple CRUD patterns for entities
- Patterns matching Component Architecture below

## Component Architecture

### Package Structure

```
com.{org}.{app-name}.domain/
  - State records, event interfaces/impls
  - Domain logic (validation, calculations)
  - NO Akka dependencies

com.{org}.{app-name}.application/
  - Entities, Views, Workflows, Agents
  - Consumers, Timed Actions

com.{org}.{app-name}.api/
  - HTTP/gRPC Endpoints
  - Request/Response records

src/main/proto/
  - Protobuf definitions
```

### Naming Conventions

| Component | Pattern | Example |
|-----------|---------|---------|
| Agent | `{Purpose}Agent` | `ActivityAgent` |
| Entity | `{DomainName}Entity` | `CreditCardEntity` |
| View | `{DomainName}{ByQueryField}View` | `CreditCardsByCardholderView` |
| Workflow | `{ProcessName}Workflow` | `BankTransferWorkflow` |
| Consumer | `{Purpose}Consumer` | `CounterEventsConsumer` |
| Timed Action | `{DomainName}TimedAction` | `OrderTimedAction` |
| HTTP Endpoint | `{DomainName}Endpoint` | `CreditCardEndpoint` |
| gRPC Endpoint | `{DomainName}GrpcEndpointImpl` | `CustomerGrpcEndpointImpl` |
| Events | `{DomainName}Event` sealed interface | `CreditCardEvent` |
| State | `{DomainName}` or `{DomainName}State` | `CreditCard` |

### Component Types & Key Characteristics

**Agent**
- Extends `Agent`, has `@Component(id = "...")`
- Exactly ONE command handler per agent (enforces single responsibility)
- Returns `Effect<T>` or `StreamEffect` (for streaming responses)
- Use `effects().systemMessage().userMessage().thenReply()`
- Three types of tools: `@FunctionTool` (agent methods), external tools via `.tools()`, MCP tools via `.mcpTools()`
- Stateless design (no mutable state in agent class)
- Session memory automatic via session ID (shared across agents with same session ID)
- Session ID typically UUID for new interactions, or workflow ID for orchestration
- Control memory with `MemoryProvider.none()`, `.limitedWindow()`, or `.limitedWindow().readLast(N)`
- Structured responses: use `responseConformsTo(Class)` (preferred) or `responseAs(Class)` with manual JSON instructions
- Model config: prefer default in config, override with `.model(ModelProvider.openAi()...)` if needed
- Error handling with `.onFailure(throwable -> fallbackValue)`
- When calling from workflow: use long timeouts (60s), limited retries (maxRetries(2))

**Event Sourced Entity**
- Extends `EventSourcedEntity<State, Event>`, has `@Component(id = "...")`
- Command handlers accept 0 or 1 parameter and return `Effect<T>`
- Events: sealed interface with `@TypeName` per event
- State/events in `domain`, entity in `application`

**Key Value Entity**
- Extends `KeyValueEntity<State>`, has `@Component(id = "...")`
- Command handlers accept 0 or 1 parameter
- Simpler than Event Sourced (direct state updates)

**View**
- Extends `View`, has `@Query` methods returning `QueryEffect<T>`
- Query methods accept 0 or 1 parameter
- **CRITICAL**: ESE views use `onEvent(Event)`, KVE views use `onUpdate(State)`
- TableUpdater uses `effects().updateRow()`, access current with `rowState()`

**Workflow**
- Extends `Workflow<State>`, has `@Component(id = "...")`
- Command handlers accept 0 or 1 parameter and return `Effect<T>`
- Step methods accept 0 or 1 parameter and return `StepEffect`
- Steps use `@StepName`, `stepEffects()` in steps, `effects()` in commands
- Compensation via `thenTransitionTo(compensationStep)` on failure

**Consumer**
- Extends `Consumer`, has `@Component(id = "...")`
- Annotated with `@Consume.From*` (EventSourcedEntity, KeyValueEntity, Workflow, Topic, ServiceStream)
- Handlers return `Effect` with `effects().done()` or `effects().ignore()`
- Produce with `@Produce.ToTopic` or `@Produce.ServiceStream`
- Use `@DeleteHandler` for KVE/Workflow deletions

**Timed Action**
- Extends `TimedAction`, has `@Component(id = "...")`
- Stateless, methods return `Effect<Done>`
- Schedule via `TimerScheduler.createSingleTimer(name, delay, deferred)`

**HTTP Endpoint**
- Has `@HttpEndpoint(path)`, NO `@Component`
- Use `@Get`, `@Post`, `@Put`, `@Delete`
- Inject `ComponentClient`, return API-specific types

**gRPC Endpoint**
- Has `@GrpcEndpoint`, NO `@Component`
- Implements interface from `.proto` (in `src/main/proto`)
- Class name with `Impl` suffix (e.g., `CustomerGrpcEndpointImpl`)
- Return protobuf types, use private `toApi()` converters

## Critical Patterns & Rules

### Domain Logic Pattern

```java
public record CreditCard(...) {
  public CreditCard charge(int amount) {
    if (!isActive() || amount <= 0 || !hasCredit(amount)) {
      throw new IllegalStateException("Invalid charge");
    }
    return withCurrentBalance(currentBalance + amount);
  }

  public boolean isActive() {
    return active;
  }

  public boolean hasCredit(int amount) {
    return currentBalance + amount <= creditLimit;
  }
}
```

### Event Sourced Entity Command Handler

```java
public Effect<Done> charge(ChargeCommand command) {
  // Validate first
  if (amount <= 0) return effects().error("Invalid amount");
  if (!currentState().isActive()) return effects().error("Card not active");
  if (!currentState().hasCredit(amount)) return effects().error("Insufficient credit");

  // Persist event, then reply
  var newState = currentState().charge(command.amount());
  var event = new CreditCardEvent.CardCharged(command.amount(), newState.currentBalance());
  return effects().persist(event).thenReply(state -> Done.getInstance());
}
```

### View Event Handler (from ESE)

```java
public Effect<MyRow> onEvent(MyEvent event) {
  var entityId = updateContext().eventSubject().orElse("");
  return switch (event) {
    case MyEvent.Created created ->
        effects().updateRow(new MyRow(entityId, created.data()));
    case MyEvent.Updated updated ->
        effects().updateRow(rowState().withData(updated.newData()));
  };
}
```

### Workflow with Compensation

```java
@Override
public WorkflowSettings settings() {
  return WorkflowSettings.builder()
    .defaultStepTimeout(ofSeconds(2))
    .stepRecovery(
      TransferWorkflow::depositStep,
      maxRetries(2).failoverTo(TransferWorkflow::compensateWithdrawStep))
    .build();
}

private StepEffect withdrawStep() {
  return stepEffects()
    .updateState(currentState().withStatus(WITHDRAW_SUCCEEDED))
    .thenTransitionTo(TransferWorkflow::depositStep);
}

private StepEffect compensateWithdrawStep() {
  return stepEffects()
    .updateState(currentState().withStatus(COMPENSATION_COMPLETED))
    .thenEnd();
}
```

### Agent with Tools

```java
@Component(id = "activity-agent")
public class ActivityAgent extends Agent {
  public Effect<String> query(String message) {
    return effects()
        .systemMessage("You are an activity agent...")
        .userMessage(message)
        .thenReply();
  }

  @FunctionTool(description = "Return current date in yyyy-MM-dd format")
  private String getCurrentDate() {
    return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
  }
}
```

### Consumer Producing to Topic

```java
@Component(id = "counter-to-topic")
@Consume.FromEventSourcedEntity(CounterEntity.class)
@Produce.ToTopic("counter-events")
public class CounterToTopicConsumer extends Consumer {
  public Effect onEvent(CounterEvent event) {
    var counterId = messageContext().eventSubject().get();
    Metadata metadata = Metadata.EMPTY.add("ce-subject", counterId);
    return effects().produce(event, metadata);
  }
}
```

### Entity Unit Tests

```java
var testKit = EventSourcedTestKit.of("entity-id", MyEntity::new);
var result = testKit.method(MyEntity::myCommand).invoke(command);
assertThat(result.isReply()).isTrue();
assertThat(testKit.getState()).satisfies(...);
```

### View Integration Tests

```java
public class MyViewIntegrationTest extends TestKitSupport {
  @Override
  protected TestKit.Settings testKitSettings() {
    return TestKit.Settings.DEFAULT
        .withEventSourcedEntityIncomingMessages(MyEntity.class);
  }

  @Test
  public void testView() {
    var events = testKit.getEventSourcedEntityIncomingMessages(MyEntity.class);
    events.publish(myEvent, "entity-id");

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          var result = componentClient.forView()
              .method(MyView::query)
              .invoke(queryParam);
          assertThat(result).isNotNull();
        });
  }
}
```

### Endpoint Integration Tests

```java
public class MyEndpointIntegrationTest extends TestKitSupport {
  @Test
  public void testEndpoint() {
    var response = httpClient // Use httpClient, NOT componentClient
        .POST("/counter/" + counterId + "/increase")
        .withRequestBody(request)
        .responseBodyAs(Counter.class)
        .invoke();

    assertThat(response.status().isSuccess()).isTrue();
    assertThat(response.body().value).isEqualTo(3);
  }
}
```

### Agent Testing Pattern

```java
public class MyAgentTest extends TestKitSupport {

  private final TestModelProvider agentModel = new TestModelProvider();

  @Override
  protected TestKit.Settings testKitSettings() {
    return TestKit.Settings.DEFAULT
        .withModelProvider(MyAgent.class, agentModel);
  }

  @Test
  public void testAgent() {
    var mockResponse = new MyAgent.Response("mocked result");
    agentModel.fixedResponse(JsonSupport.encodeToString(mockResponse));

    var result = componentClient
        .forAgent()
        .inSession("test-session-id")
        .method(MyAgent::myMethod)
        .invoke(request);

    assertThat(result).isEqualTo(mockResponse);
  }
}
```

**Agent Testing Key Points:**
- Create `TestModelProvider` instance as field
- Register in `testKitSettings()` with `.withModelProvider(AgentClass.class, modelProvider)`
- Use `.fixedResponse()` with `JsonSupport.encodeToString()` for structured responses
- Always use `.inSession(sessionId)` when calling agents from tests
- Use `.whenMessage(predicate).reply(response)` for conditional responses

## Critical Gotchas

### PROHIBITED Patterns (MUST NOT appear in code)

| Category | DON'T | DO Instead |
|----------|-------|------------|
| **Imports** | Use `io.akka.*` | Use `akka.*` |
| **Imports** | Import `WorkflowSettings` | It's inner class of `Workflow` |
| **Domain** | Put Akka dependencies in domain package | Keep domain pure Java |
| **Domain** | Return protobuf types from domain | Use domain records |
| **Entity** | Put business logic in entities | Put in domain objects |
| **Entity** | Use try-catch for validation | Return `effects().error()` |
| **Entity** | Use `commandContext().entityId()` in `emptyState()` | Inject context |
| **Entity** | Return `null` in event handler | Only `emptyState()` may return null |
| **Entity** | Perform side effects in `.thenReply()` | Use Consumer for side effects |
| **View** | Use `onUpdate(State)` for ESE views | Use `onEvent(Event)` |
| **View** | Put `@Consume` on View class | Put on TableUpdater subclass |
| **View** | Return `QueryEffect<List<Row>>` | Wrap in record with `List<Row> items` |
| **View** | Use `SELECT *` for multi-row | Use `SELECT * AS items` |
| **Workflow** | Use `definition()` | Use `settings()` + step methods |
| **Workflow** | Use string step names | Use method references (`::`) |
| **Endpoint** | Return domain objects | Create API-specific types |
| **Endpoint** | Return `CompletionStage` | Use synchronous style |
| **Endpoint** | Use `.invokeAsync()` | Use `.invoke()` |
| **Endpoint** | Omit `@Acl` | Always add `@Acl` annotation |
| **Endpoint** | Add `@Component` to endpoints | Only use `@HttpEndpoint`/`@GrpcEndpoint` |
| **Agent** | Create multiple command handlers | Exactly ONE handler per agent |
| **Component** | Inject `ComponentClient` into Entities/Views | Only in Endpoints, Agents, Consumers, TimedActions, Workflows |
| **Component** | Create empty command records without fields | Make method parameterless |
| **Bootstrap** | Create a `Main` class | Use `Bootstrap` implementing `ServiceSetup` |
| **Deprecated** | Use `@ComponentId` | Use `@Component(id = "...")` |
| **Deprecated** | Use `@AgentDescription` | Use `@Component(id = "...")` and `@AgentRole` |
| **Testing** | Use `testKit.call` | Use `testKit.method(...).invoke(...)` |
| **Testing** | Use `componentClient` in endpoint tests | Use `httpClient` |

### REQUIRED Patterns (MUST appear in code)

- Use Java records for immutable data
- Add `@TypeName` to all events
- Validate in domain objects, return effects in entities
- Use `with*` methods for immutable updates
- Inject `EventSourcedEntityContext` if accessing entity ID in `emptyState()`
- Use `Awaitility.await()` for view tests
- Follow package structure strictly
- Use `TestModelProvider` to mock AI in agent tests
- Add `ce-subject` metadata when producing to topics
- Handle errors in Timed Actions to avoid infinite rescheduling
- Define `.proto` files in `src/main/proto` for gRPC
- Use private `toApi()` converters in gRPC endpoints

## Self-Review Checklist

Before presenting code, verify:

**Imports**
- [ ] Using `akka.*` not `io.akka.*`
- [ ] Using `akka.javasdk.*`

**Agent**
- [ ] Only ONE command handler
- [ ] Rich descriptions in `@FunctionTool`
- [ ] Use `responseConformsTo()` for structured responses (not `responseAs()`)
- [ ] Handle errors from JSON parsing, tool calls, or model with `.onFailure()`
- [ ] Session ID strategy defined (UUID, workflow ID, etc.)

**Events & State**
- [ ] Events have `@TypeName` annotations
- [ ] State uses immutable records

**Entity**
- [ ] Inject `EventSourcedEntityContext` if needed
- [ ] `emptyState()` uses injected context (not `commandContext()`)

**View**
- [ ] `@Consume` on TableUpdater, not View class
- [ ] Multi-row queries use wrapper record with `AS` clause
- [ ] ESE views use `onEvent()` not `onUpdate()`

**Workflow**
- [ ] **STOP: Read `workflows.html.md` BEFORE writing code** (first workflow in session)
- [ ] Uses `settings()` with `WorkflowSettings` (NOT `definition()`)
- [ ] Command handlers accept 0 or 1 parameter and return `Effect<T>`
- [ ] Step methods accept 0 or 1 parameter and return `StepEffect` (NOT `Effect`)
- [ ] Uses method references for step names (e.g., `TransferWorkflow::withdrawStep`)
- [ ] Uses `stepEffects()` in steps, `effects()` in command handlers

**Endpoint**
- [ ] Has `@Acl` annotation
- [ ] Synchronous style, uses `.invoke()`
- [ ] Returns API-specific types

**Tests**
- [ ] Entity tests use `EventSourcedTestKit`
- [ ] View tests use event publishing + `Awaitility`
- [ ] Endpoint tests use `httpClient` not `componentClient`
- [ ] Agent tests use `TestModelProvider` with `.fixedResponse()` or `.whenMessage()`

## Governance

This constitution is derived from AGENTS.md and supersedes all other development
practices. When AGENTS.md is updated, this constitution MUST be updated to match.

**Amendment Process:**
1. Update AGENTS.md first (source of truth)
2. Run `/speckit.constitution` to sync constitution
3. Version bump follows semantic versioning:
   - MAJOR: Structural reorganization or backward-incompatible rule changes
   - MINOR: New rules or materially expanded guidance
   - PATCH: Clarifications, wording, typo fixes

**Compliance:**
- All code reviews MUST verify adherence to Critical Gotchas
- Self-Review Checklist MUST be completed before code presentation
- Violations require justification in plan.md Complexity Tracking section

**Version**: 3.0.0 | **Ratified**: 2026-03-04 | **Last Amended**: 2026-03-04
