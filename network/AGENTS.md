# `:network` Module — HTTP Client, Auth Interceptors & Sync Contracts

## 1. Current Reality (Scope & Key Files)
- **Namespace:** `com.lukasz.witkowski.training.planner.network`
- **Role:** Provides shared HTTP client infrastructure (Retrofit 3 + OkHttp 5 + `kotlinx.serialization`), token refresh synchronization, and cross-module auth/sync contracts.
- **Key Files:**
  - `NetworkModule.kt`: Koin module providing `Json`, `OkHttpClient`, and `Retrofit` instances qualified by `NetworkQualifiers.UNAUTHENTICATED` and `NetworkQualifiers.AUTHENTICATED` (default `Retrofit` binding is `AUTHENTICATED`). Default emulator URL: `http://10.0.2.2:8080/`.
  - `AuthInterceptor.kt`: Attaches `Authorization: Bearer <accessToken>` header from `TokenStorage`.
  - `TokenAuthenticator.kt`: OkHttp `Authenticator` handling HTTP `401 Unauthorized` with a `Mutex` lock, double-checking whether another concurrent request already refreshed the token, calling `TokenRefreshRemoteDataSource`, and retrying up to `RESPONSE_COUNT_LIMIT = 3`.
  - **Cross-Module Interfaces:** `TokenStorage`, `AuthSessionProvider`, `AuthTokens`, `TokenRefreshRemoteDataSource`, `SyncScheduler`, and `SyncPreferencesStorage`.

## 2. Architecture & Coding Invariants
- **Qualifier Rule:** Authentication endpoints (`/api/v1/auth/login`, `/register`, `/refresh`) **must** inject `get<Retrofit>(NetworkQualifiers.UNAUTHENTICATED)` to avoid infinite 401 authenticator loops. Protected feature APIs inject the default (authenticated) `get<Retrofit>()`.
- **No Feature APIs Here:** Keep Retrofit `@GET`/`@POST` interfaces inside their owning feature modules (`:auth`, `:user`, `:exercise`, `:training`), not in `:network`.
- **Testing:** Test interceptors and authenticators with OkHttp `MockWebServer` (`AuthInterceptorTest`, `TokenAuthenticatorTest`).

## 3. Target Direction (`KEEP & EXPAND`)
- **KEEP:** `AuthInterceptor`, `TokenAuthenticator`, and Koin qualifiers.
- **EXPAND:**
  1. Add shared OkHttp WebSocket factory/client support for the **Global Chat** (`/ws/chat`).
  2. Add the **SSE (Server-Sent Events) learning experiment** client (`GET /events` $\to$ cold `Flow` with reconnect/backoff and lifecycle cancellation).

## 4. Verification Commands
```bash
./gradlew :network:testDebugUnitTest :network:ktlintCheck
```
