# Training Planner --- Product & Domain Direction

**Status:** Agreed product direction  
**Purpose:** Canonical source for the Android Tech Lab / Training Planner redesign before auditing the existing codebase.

## 1. Project goal

The project is a **local-first workout planning and execution application** that also serves as a practical Android/Kotlin engineering laboratory.

The primary goal is **learning senior-level Android/Kotlin/KMP engineering through a realistic application**, not building a production-ready fitness platform.

The application should therefore be:

- useful enough to exercise realistic engineering problems;
- small enough that the domain does not overwhelm the learning objectives;
- local-first and usable without a backend connection;
- progressively extended with backend communication and Android platform capabilities;
- based on the existing Training Planner codebase rather than rewritten from scratch;
- structured so individual features provide opportunities to learn, implement, test, debug, and explain Android technologies.

The resulting application is a **side effect of the learning process**.

---

## 2. Core product concept

The application helps a user:

1. discover and manage exercises;
2. create and customize training plans;
3. schedule planned training;
4. execute a training session;
5. record what actually happened;
6. review training history and statistics;
7. optionally synchronize data with the backend;
8. optionally consume community-provided exercises and plans;
9. communicate through a simple global chat.

The application is **not intended to become a social fitness platform**.

---

## 3. Main domain concepts

The central distinction is between **what the user intends to do** and **what actually happened**.

### Exercise

An exercise describes an individual exercise.

It contains things such as:

- name;
- description;
- media;
- one or more body parts;
- creator-provided recommendations.

Recommendations can target different experience levels:

- beginner;
- intermediate;
- advanced.

A recommendation can include:

- sets;
- repetitions;
- rest time;
- recommended weight.

Exercises can come from:

- bundled/default content;
- community/backend content;
- the user.

Community/backend exercises can have **versions**.

### TrainingPlan

A training plan describes **what the user intends to do**.

A plan contains exercises together with the user's configured training parameters, such as:

- sets;
- repetitions;
- weight;
- rest time.

The plan contains the user's **planned values**, rather than the values actually achieved during a training session.

A predefined/community plan can be customized by the user without destroying the original predefined plan. The original predefined plan remains available.

### Exercise versioning

A plan referencing a community exercise should retain the **specific exercise version** it was created/configured against.

If the backend later publishes a newer version:

```text
Exercise v1
    ↓
Plan references v1

Backend publishes Exercise v2
    ↓
User is informed that an update exists
    ↓
User can inspect the update
    ↓
User chooses whether to update
```

An existing plan should **not silently change** because a community exercise was updated.

The backend therefore stores available versions, while the plan retains its selected version.

### ScheduledTraining

A scheduled training represents an **intention to perform a plan at a particular time**.

It can contain:

- training plan;
- date/time;
- location;
- one-off or recurring schedule.

A scheduled training is **not a training session** and does not represent something that already happened.

Conceptually:

```text
TrainingPlan
    │
    │ scheduled for
    ▼
ScheduledTraining
    │
    │ eventually starts
    ▼
TrainingSession
```

### TrainingSession --- proposed terminology

**Proposed, not yet frozen:** use `TrainingSession` for the actual execution of training.

The term `Workout` is intentionally not being frozen because it is ambiguous: in normal fitness language it can mean either the process of exercising or the historical record of a completed exercise session.

`TrainingSession` more clearly describes the actual execution.

A session can have a lifecycle such as:

```text
NOT_STARTED
    ↓
IN_PROGRESS
    ↓
PAUSED
    ↓
COMPLETED

or

IN_PROGRESS → CANCELLED
```

The same persisted `TrainingSession` can therefore represent the actual training event while it is in progress and after it has completed.

This is a **suggestion for the domain terminology** and should be validated against the existing project during the architecture audit.

### Actual training data

The actual training session contains data such as:

- exercises performed;
- actual sets;
- actual repetitions;
- actual weights;
- exercise/workout timing;
- rest periods;
- difficulty feedback;
- whether the planned work was doable.

The session may retain relevant planned values so the application can compare:

```text
planned
   vs
actual
```

The key domain distinction is:

> **TrainingPlan = what I intend to do.**  
> **TrainingSession = what actually happens.**

---

## 4. Active training session

The active training session is primarily a **local/device concern**.

While the user is exercising, the application needs to maintain transient state such as:

- current exercise;
- current set;
- current repetitions;
- current weight;
- elapsed exercise time;
- rest timer;
- pause/resume state;
- training progress.

This state should not depend on a backend connection.

The backend should **not** be responsible for the live training timer or current execution state.

The completed session is persisted locally and can subsequently be synchronized.

The existing foreground `session-service` in the Training Planner is potentially useful here and should be evaluated during the audit rather than discarded automatically.

---

## 5. Synchronization model

The application is **local-first**.

The user should be able to use the core application without an internet connection.

Room/local persistence should be the normal source of truth for persistent user-facing data.

The backend provides:

- community content;
- remote persistence;
- synchronization between devices;
- authentication;
- chat;
- selected communication experiments.

### Completed session upload

When a training session finishes:

1. the session is saved locally;
2. the user is asked whether to upload it;
3. the user can choose not to upload it;
4. the user can enable an option to automatically upload completed sessions in the future.

Conceptually:

```text
TrainingSession completed
        ↓
Save locally
        ↓
"Upload training?"
    ↙             ↘
Not now          Upload
                   ↓
             backend sync
```

There should be no requirement that the session reach the backend before the user can continue using the app.

### Synchronization conflicts

The initial synchronization strategy should favour **explicit user decisions rather than sophisticated automatic conflict merging**.

If the same user-owned data has diverged between devices, the application can present the conflicting versions and allow the user to decide which version to keep.

We do not need a sophisticated distributed conflict-resolution system for the first version.

---

## 6. Community content

The backend can provide additional:

- exercises;
- predefined training plans.

The application should retain local copies of content that the user needs.

A simplified flow is:

```text
Backend
   │
   │ download
   ▼
Room
   │
   ▼
Application
```

The UI should not need to fetch community content directly from the network every time it displays it.

This creates natural opportunities to practise:

- REST;
- serialization;
- caching;
- synchronization;
- versioning;
- pagination;
- authentication;
- error handling;
- offline behaviour.

---

## 7. Authentication

The backend should have a relatively conventional authentication model.

The Android application should practise:

- registration/login;
- access credentials;
- refresh;
- logout;
- authenticated requests;
- authentication failures;
- token refresh;
- concurrent refresh handling;
- secure local credential storage.

The authentication system should remain deliberately simple.

The goal is to learn Android/backend communication, not to build an identity platform.

---

## 8. Global chat

The application will contain **one global group chat**.

There will deliberately be no:

- private messaging;
- chat rooms;
- channels;
- social graph.

The purpose is to provide a natural environment for practising:

```text
REST
  +
pagination
  +
authentication
  +
WebSocket
```

For example:

```text
GET /chat/messages
        ↓
initial/history messages

WebSocket
        ↓
new incoming messages
```

This keeps WebSocket relevant without forcing real-time communication into the core training domain.

---

## 9. Push notifications

FCM can be used for appropriate remote events such as:

- community content updates;
- relevant training notifications;
- other server-originated notifications.

Local notifications remain appropriate for device-local scheduling.

The distinction should remain:

```text
Local scheduling
→ Android local notification mechanisms

Server-originated events
→ FCM
```

---

## 10. Wear OS

The watch is **execution-only**.

It is not intended to become a second full Training Planner application.

The phone remains the primary application.

A future flow is:

```text
Phone
  │
  │ selected plan/session
  ▼
Watch
  │
  │ executes training
  ▼
Watch
  │
  │ training result
  ▼
Phone
  │
  ▼
local persistence
  │
  ▼
optional backend synchronization
```

The watch therefore provides opportunities to practise:

- Wear OS;
- Android Data Layer;
- device-to-device communication;
- lifecycle/disconnection handling;
- synchronization of execution results.

It should not duplicate the application's entire domain or UI.

---

## 11. Android platform learning experiments

Some Android technologies should be used naturally in the product. Others should deliberately exist as **small experiments** rather than being forced into the domain.

### Natural product use

Examples:

- Room;
- ViewModel;
- Compose;
- Flow;
- WorkManager;
- foreground service;
- notifications;
- DataStore;
- networking;
- FCM;
- Wear OS;
- dependency injection.

### BroadcastReceiver experiment

A possible experiment:

```text
BOOT_COMPLETED
      ↓
BroadcastReceiver
      ↓
schedule/resume appropriate work
      ↓
WorkManager
```

The receiver should not perform long-running work itself.

### ContentProvider experiment

A small companion application could expose selected Training Planner data through a ContentProvider.

For example:

```text
Training Planner
       ↑
       │ ContentProvider
       │
Training Planner Inspector
```

This gives a controlled way to learn:

- URI matching;
- ContentResolver;
- Cursor;
- permissions;
- exported/non-exported components;
- IPC/security.

The provider should not be forced into the core application architecture.

### SSE experiment

SSE should be a separate communication experiment rather than a core dependency.

For example:

```text
GET /events
      ↓
server event stream
      ↓
Android Flow
```

Possible events could include:

- community exercise updated;
- community plan updated;
- announcement;
- other server-side events.

The goal is to learn:

- streaming;
- cancellation;
- lifecycle;
- reconnect;
- backoff;
- duplicate events.

We should not invent a business requirement just to make SSE central to the product.

---

## 12. Backend philosophy

The backend should remain **small and intentionally boring**.

Proposed technology:

```text
Kotlin
   +
Ktor
   +
PostgreSQL
```

The backend exists primarily to give the Android application something realistic to communicate with.

It should not evolve into:

- microservices;
- event-driven infrastructure;
- Kafka;
- Kubernetes;
- complex distributed architecture;
- elaborate cloud infrastructure.

The backend should provide enough functionality to exercise Android networking and synchronization properly.

---

## 13. Backend responsibilities

The backend should understand persistent/shared concepts such as:

- users;
- authentication;
- community exercises;
- exercise versions;
- community plans;
- user-owned exercises/plans;
- schedules;
- completed training history;
- synchronization;
- chat messages.

The backend should **not** own:

- current training timers;
- current exercise/set state;
- live rest timers;
- transient phone execution state;
- watch execution state.

Those remain on the device.

---

## 14. High-level architecture direction

The application should generally move toward:

```text
             ┌─────────────────┐
             │     Compose     │
             └────────┬────────┘
                      │
                 ViewModel
                      │
                State / Flow
                      │
                 Repository
                /           \
               /             \
            Room           Network
             │                │
             │             Backend
             │
        Local source
          of truth
```

The exact architecture is intentionally **not frozen yet**.

The final module structure, domain boundaries, repository interfaces, synchronization architecture, and other implementation details should be determined after auditing the existing Training Planner.

---

## 15. Relationship to the existing Training Planner

This project is **not a greenfield rewrite**.

The existing Training Planner already contains valuable engineering work, including domain concepts, multiple modules, Room persistence, Compose, ViewModels, StateFlow, statistics/session logic, a foreground service, and Wear OS communication.

The new project should therefore be treated as a:

> **migration and deliberate refactoring of an existing application into a modern Android engineering laboratory.**

For every existing component we should eventually decide:

```text
KEEP
REFACTOR
REWRITE
MOVE
REMOVE
DEFER
```

The existing implementation should not automatically determine the new architecture, but neither should useful working code be discarded simply because we want to learn something.

Existing functionality should become learning material where it naturally maps to the target architecture.

---

## 16. Learning principle

Every substantial feature should follow:

```text
Learn
  ↓
Design
  ↓
Implement
  ↓
Break / experiment
  ↓
Test
  ↓
Debug
  ↓
Explain
  ↓
Interview
```

The goal is not merely to make the application work.

For a topic to count as learned, the user should be able to:

- explain the underlying mechanism;
- implement it;
- test success and failure;
- reason about lifecycle, cancellation, and edge cases;
- debug it when it breaks;
- compare reasonable alternatives;
- defend the architectural choice in an interview.

---

## 17. Decisions already agreed

The following are considered agreed product requirements:

1. The application is local-first.
2. The existing Training Planner is the starting codebase; we modify and adjust it rather than creating a separate greenfield project.
3. Training plans contain intended/planned values.
4. Actual performed values belong to the actual training execution/session.
5. Community exercises are versioned.
6. A plan retains the exercise version it references.
7. Cloud exercise updates never silently change an existing plan.
8. The user can inspect and explicitly accept an available cloud update.
9. Completed training is saved locally first.
10. At completion, the user is asked whether to upload it.
11. The user can enable automatic upload for future completed training sessions.
12. The watch is execution-only; the phone remains the primary application.
13. The backend is intentionally small and primarily exists to provide realistic Android communication and synchronization scenarios.
14. Global chat is one group chat and exists mainly as a natural WebSocket/pagination/authentication exercise.
15. SSE, ContentProvider, and BroadcastReceiver are learning experiments and should not be forced into the core domain.
16. `TrainingSession` is the **proposed** terminology for actual training execution, replacing the ambiguous `Workout` terminology for now. This terminology remains open until the existing project is audited.

---

## 18. Deliberately not decided yet

The following should remain open until the existing-project audit and architecture workshop:

- final domain class names;
- final module structure;
- exact domain relationships;
- exact Room schema;
- exact backend PostgreSQL schema;
- exact REST endpoints;
- exact synchronization protocol;
- exact authentication implementation;
- DI structure/framework usage;
- navigation architecture;
- exact Wear OS communication protocol;
- exact cloud deployment;
- which existing classes/modules survive;
- which existing implementation patterns should be replaced;
- whether `TrainingSession` remains the final terminology.

---

## 19. Next step: existing-project audit

The next phase is to audit the current Training Planner against this target.

Each major existing module/subsystem should be classified as:

| Existing component | Decision | Reason |
| :--- | :--- | :--- |
| Domain | KEEP / REFACTOR / REWRITE / REMOVE / DEFER | |
| Exercise | KEEP / REFACTOR / REWRITE / REMOVE / DEFER | |
| Training | KEEP / REFACTOR / REWRITE / REMOVE / DEFER | |
| Statistics | KEEP / REFACTOR / REWRITE / REMOVE / DEFER | |
| Synchronization | KEEP / REFACTOR / REWRITE / REMOVE / DEFER | |
| Session service | KEEP / REFACTOR / REWRITE / REMOVE / DEFER | |
| Wearable | KEEP / REFACTOR / REWRITE / REMOVE / DEFER | |
| Room/persistence | KEEP / REFACTOR / REWRITE / REMOVE / DEFER | |
| Compose/UI | KEEP / REFACTOR / REWRITE / REMOVE / DEFER | |
| Gradle/modules | KEEP / REFACTOR / REWRITE / REMOVE / DEFER | |

The audit should then bridge:

**existing codebase → target domain → target architecture → implementation/learning roadmap.**

Only after this audit should the final architecture and detailed implementation plan be frozen.
