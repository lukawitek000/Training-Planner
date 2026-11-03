# `:image` Module — Local Image Storage & Reference-Counted Room DB

## 1. Current Reality (Scope & Key Packages)
- **Namespace:** `com.lukasz.witkowski.training.planner.image`
- **Role:** Manages local image persistence on internal file storage with checksum deduplication and reference counting per owner entity.
- **Core & Domain (`domain/`, root package):**
  - `ImageStorage` (`DataReferenceSeparatedImageStorage`), `ImageId`, `ImageReference`, `ImageConfiguration`, `ImageBitmap`, `ImageByteArray`.
  - `ImageRepository`, `ImageReferenceRepository`, `ChecksumCalculator`.
- **Infrastructure (`infrastructure/`):**
  - `InternalStorageImageRepository`: Saves/reads binary files in Android internal storage (`TrainingPlannerImageStorage`).
  - `Adler32ChecksumCalculator`: Computes checksums to avoid storing duplicate image payloads.
  - **Room 3 (`ImageReferenceDatabase`):** `ImageReferenceDao`, `DbImageReferenceRepository`, `DbImageReference`, `DbImageOwner`, `DbImageReferenceWithOwners`.
- **Presentation (`presentation/`):** `Image.kt`, `ImageGallery.kt`, `ImageInputForm.kt`, `CarouselNavigation.kt` (uses Coil 3 `coil-compose`).

## 2. Architecture & Coding Invariants
- **Reference Counting Invariant:** Multiple domain owners (e.g., an Exercise and a TrainingPlan snapshot) can reference the same underlying image file via `DbImageOwner`. Deleting an owner reference only deletes the physical file when no owners remain.
- **Room 3:** Always use `androidx.room3.*` imports.

## 3. Target Direction (`KEEP & REFACTOR`)
- **KEEP:** `DataReferenceSeparatedImageStorage`, Adler32 deduplication, and reference-counted cleanup.
- **REFACTOR:** When updating `presentation/` composables (`ImageGallery`, `ImageInputForm`), align with `docs/composable_guidelines.md` (`App*` prefix, `Dimens.*` tokens, `@AppPreview`).

## 4. Verification Commands
```bash
./gradlew :image:testDebugUnitTest :image:ktlintCheck
```
