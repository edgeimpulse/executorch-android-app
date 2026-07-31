# ExecuTorch Android Demo

A minimal Kotlin Android app that runs an [ExecuTorch](https://docs.pytorch.org/executorch/stable/using-executorch-android.html)
`.pte` model on-device using the XNNPACK CPU backend. It is wired to load an
**Edge Impulse-exported** model.

## Project layout

```
app/
  src/main/
    assets/                    <- put model.pte + labels.txt here
    java/com/example/executorchdemo/
      MainActivity.kt          <- UI + runs one inference on a button tap
      EdgeImpulseClassifier.kt <- loads the .pte, runs forward(), reads scores
    res/                       <- layout, theme, launcher icon
```

## Wiring in your model

1. Export your model from Edge Impulse (ExecuTorch deployment block or custom
   block) and copy it to:
   - `app/src/main/assets/model.pte`
2. Fill `app/src/main/assets/labels.txt` with one class label per line, in the
   model's output order.
3. Set `INPUT_SHAPE` in
   [`EdgeImpulseClassifier.kt`](app/src/main/java/com/example/executorchdemo/EdgeImpulseClassifier.kt)
   to match your model input. Examples:
   - Image 96x96 RGB (NHWC): `longArrayOf(1, 96, 96, 3)`
   - Image 96x96 RGB (NCHW): `longArrayOf(1, 3, 96, 96)`
   - Tabular / time-series with N features: `longArrayOf(1, N)`
4. Replace the all-zeros placeholder input in `MainActivity.runInference()` with
   real, preprocessed data (a normalized camera frame, a sensor window, etc.).

## Dependency

ExecuTorch comes from Maven Central (see `app/build.gradle.kts`):

```kotlin
implementation("org.pytorch:executorch-android:1.0.0")
```

## Build & run

```bash
# from the project root
./gradlew :app:assembleDebug          # build a debug APK

# install on a connected device / emulator
./gradlew :app:installDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Requires the Android SDK (platform 35, build-tools 35) and JDK 17. The SDK
location is read from `local.properties` (`sdk.dir`).
