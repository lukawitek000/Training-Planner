# `:synchronization` Module — Legacy Wearable Channel Sync (Read Before Using!)

## 1. CRITICAL REALITY WARNING
- **Namespace:** `com.lukasz.witkowski.training.planner.synchronization`
- **What Actually Lives Here:** Despite its generic name, `:synchronization` currently **ONLY** contains legacy **Wear OS Bluetooth `ChannelClient`** stream helpers (`WearableChannelClientSender`, `WearableChannelClientReceiver`, `StreamSuspendingOperations`, `SyncDataUtils`, `SynchronizationStatus`, `SynchronizationExceptions`) serialized with **`Gson`**.
- **Where Backend HTTP Sync Actually Lives:**
  - Client-server sync interfaces (`SyncScheduler`, `SyncPreferencesStorage`) live in **[`:network`](../network/AGENTS.md)**.
  - WorkManager outbox/delta sync implementation lives in **[`:exercise`](../exercise/AGENTS.md)** (`infrastructure/sync/`).

## 2. Architecture & Coding Invariants
- **Do NOT put Retrofit/WorkManager backend sync code here** unless executing an explicit module restructuring plan.
- **Do NOT copy the `Gson` pattern** from `SyncDataUtils.kt` into other modules; the rest of the project uses `kotlinx.serialization`.

## 3. Target Direction (`REWRITE / MOVE`)
- **Wear OS Direction:** The watch is **execution-only** (Phone sends selected plan/session $\to$ Watch executes training $\to$ Watch returns training result $\to$ Phone persists locally & optionally syncs to backend).
- **Refactoring Plan:**
  1. Move Wear OS Data Layer / `ChannelClient` communication into a dedicated `:wearable` or watch-transport boundary and migrate serialization from `Gson` to `kotlinx.serialization`.
  2. Either repurpose `:synchronization` into a shared local-first backend sync engine (abstracting the outbox/conflict patterns proven in `:exercise`) or remove this module once Wear OS transport is relocated.

## 4. Verification Commands
```bash
./gradlew :synchronization:assembleDebug :synchronization:ktlintCheck
```
