# `:session-service` Module — Foreground Workout Service & Notifications

## 1. Current Reality (Scope & Key Files)
- **Namespace:** `com.lukasz.witkowski.training.planner.session.service`
- **Role:** Android `Service` (`SessionService`) intended to keep an active `TrainingSession` alive in the foreground with an ongoing notification.
- **Key Files:**
  - `SessionService.kt`: Bound + started `Service` calling `startForeground(NOTIFICATION_ID, notification)`.
  - `TrainingSessionController.kt`, `SessionServiceConnector.kt`.
  - `NotificationFactory.kt`, `NotificationChannelFactory.kt`, `TrainingSessionPendingIntentFactory.kt`.
  - `SessionServiceContainer.kt`: Manual singleton DI container (does not use Koin yet).

## 2. Critical Bugs & Current Status (Read Before Editing!)
1. **Not Wired to `:app`:** `:app/build.gradle.kts` and `AppModule.kt` do **not** currently depend on or wire `:session-service`.
2. **Inverted Lifecycle Calls in `SessionService.kt`:**
   - Line 51: `onCreate()` currently calls `trainingSessionController.onServiceDestroy()`.
   - Line 55: `onDestroy()` currently calls `trainingSessionController.onServiceCreate()`.
3. **No Unit/Instrumentation Tests Yet:** There is no `src/test` or `src/androidTest` directory in `:session-service`.

## 3. Target Direction (`REFACTOR` — Prime Android Learning Vehicle)
This module is a high-value learning laboratory for Senior Android mastery (`android-learning-lab` skill):
- Fix the inverted `onCreate`/`onDestroy` lifecycle calls.
- Migrate from manual `SessionServiceContainer` to Koin DI.
- Ensure Android 14+ (API 34+) `foregroundServiceType` compliance in `AndroidManifest.xml` (`specialUse` / `health` / `shortService` trade-offs) and runtime `POST_NOTIFICATIONS` permission handling.
- Connect `SessionService` to `:session`'s `TrainingSessionService` so live timers and ongoing notification actions (Pause/Resume/Next Set) stay synchronized when the user leaves the app.

## 4. Verification Commands
```bash
./gradlew :session-service:assembleDebug :session-service:ktlintCheck
```
