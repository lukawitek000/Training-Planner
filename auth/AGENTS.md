# `:auth` Module — Authentication, Token Lifecycle & Keystore Security

## 1. Current Reality (Scope & Key Packages)
- **Namespace:** `com.lukasz.witkowski.training.planner.auth`
- **Domain (`domain/`):** `AuthenticationRepository`, `AuthenticationResult`, `AuthenticationFailure`, `SignInForm`, `SignUpForm`.
- **Infrastructure (`infrastructure/`):**
  - `local/Encryptor.kt` & `AndroidSecureTokenStorage.kt`: Hardware-backed encryption using Android Keystore (`AES/GCM/NoPadding`) persisted in Jetpack DataStore Preferences.
  - `local/DefaultTokenStorage.kt`: Implements `:network`'s `TokenStorage` and `AuthSessionProvider`.
  - `remote/AuthenticationApi.kt`, `RetrofitAuthenticationRemoteDataSource.kt`, `RetrofitTokenRefreshRemoteDataSource.kt` (implements `:network`'s `TokenRefreshRemoteDataSource`).
  - `DefaultAuthenticationRepository.kt`: Coordinates login/register, saves tokens, and triggers `SyncScheduler` on successful authentication.
- **Presentation (`presentation/`):** `AuthenticationScreen.kt`, `AuthenticationViewModel.kt`, `SessionManagerViewModel.kt`.

## 2. Architecture & Coding Invariants
- **Unauthenticated Retrofit Client:** `AuthenticationApi` in `AuthModule.kt` must always use `get<Retrofit>(NetworkQualifiers.UNAUTHENTICATED)`.
- **Security Invariant:** Never store plaintext JWT access or refresh tokens on disk; always route persistence through `SecureTokenStorage` (`Encryptor`).
- **Offline First:** Authentication is optional for core local workout planning and execution ("Continue as Guest" is always supported).

## 3. Target Direction & Known Tech Debt (`KEEP & REFACTOR`)
- **KEEP:** Complete login, registration, token refresh, and Keystore AES-GCM storage implementation.
- **REFACTOR:** `SessionManagerViewModel` is currently not registered in `AuthModule.kt`. Register and connect it when global session state / automatic re-authentication prompts (`ReauthBottomSheet` in `:user`) are wired at the app level.

## 4. Verification Commands
```bash
./gradlew :auth:testDebugUnitTest :auth:ktlintCheck
```
