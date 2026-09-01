# ExecuTorch Android Demo

A minimal Kotlin Android app that runs an [ExecuTorch](https://docs.pytorch.org/executorch/stable/using-executorch-android.html)
`.pte` model on-device using the XNNPACK CPU backend. It is wired to load an
**Edge Impulse-exported** model.

<img width="2169" height="1360" alt="image" src="https://github.com/user-attachments/assets/5210cfa7-742f-4901-8f11-c50aff78fd9d" />

## Prebuilt sample APKs

Grab an APK from the [**Releases**](../../releases) page and sideload it — no
build required. Each ships a different Edge Impulse model and installs under its
own app id, so you can keep all three side by side:

| APK | Model | Input shape | Classes |
| --- | --- | --- | --- |
| `app-classification-debug.apk` | Image classifier | `[1, 3, 96, 96]` | lamp, plant, unknown |
| `app-fomo-debug.apk` | FOMO object detection | `[1, 3, 320, 320]` | coffee, lamp |
| `app-timeseries-debug.apk` | Motion (spectral) | `[1, 39]` | idle, snake, updown, wave |


## Compatible Learn Blocks Classification, Timeseries, Object Detection FOMO & KWS

| Name | link |
|------|---------|
| Timeseries | https://github.com/edgeimpulse/executorch-pytorch-timeseries-block |
| Classification | https://github.com/edgeimpulse/executorch-pytorch-classification-block |
| FOMO | https://github.com/edgeimpulse/executorch-pytorch-object-detection-fomo-block |
| KWS | https://github.com/edgeimpulse/executorch-pytorch-kws-block |


<img width="1892" height="1360" alt="image" src="https://github.com/user-attachments/assets/5d532056-9a49-455d-955f-78ebdb40f6cd" />

<img width="1892" height="1360" alt="image" src="https://github.com/user-attachments/assets/f1a2af36-7bed-421c-ae1e-d731560b8d77" />

<img width="1892" height="1360" alt="image" src="https://github.com/user-attachments/assets/a99ee113-4b7c-4876-95af-844759d301a3" />


```bash
adb install -r app-classification-debug.apk
```

Open the app and tap **Run inference**. It runs one forward pass on a placeholder
input and shows the output scores — proof the model loads and runs on-device.

## Project layout

```
app/
  src/
    main/                         <- shared code, UI, launcher icon (no model)
      java/com/example/executorchdemo/
        MainActivity.kt           <- UI + runs one inference on a button tap
        EdgeImpulseClassifier.kt  <- loads the .pte, runs forward(), reads scores
    classification/assets/        <- model.pte + labels.txt + input_shape.txt
    fomo/assets/                  <- model.pte + labels.txt + input_shape.txt
    timeseries/assets/            <- model.pte + labels.txt + input_shape.txt
```

Each model is a Gradle **product flavor** (`classification`, `fomo`,
`timeseries`). The bundled assets per flavor are:

- `model.pte` — the ExecuTorch program exported from Edge Impulse
- `labels.txt` — one class label per line, in output order
- `input_shape.txt` — the input tensor shape, e.g. `1,3,96,96` (NCHW)

The input shape is read at runtime from `input_shape.txt`, so adding a new model
is just a new flavor + assets — no code change.

## Dependency

ExecuTorch comes from Maven Central (see `app/build.gradle.kts`):

```kotlin
implementation("org.pytorch:executorch-android:1.0.0")
```

## Build & run

```bash
# build every flavor's debug APK
./gradlew assembleDebug
# -> app/build/outputs/apk/<flavor>/debug/app-<flavor>-debug.apk

# build/install a single flavor
./gradlew installClassificationDebug   # or installFomoDebug / installTimeseriesDebug
```

If a flavor has no bundled model you'll see this instead:

<img width="1310" height="1360" alt="image" src="https://github.com/user-attachments/assets/bf3ed1a9-635c-493e-9160-f2ea60300846" />

Requires the Android SDK (platform 35, build-tools 35) and JDK 17. The SDK
location is read from `local.properties` (`sdk.dir`).

## Using your own Edge Impulse model

1. Add a flavor asset folder, e.g. `app/src/<name>/assets/`, with `model.pte`,
   `labels.txt`, and `input_shape.txt`.
2. Register the flavor in `app/build.gradle.kts` under `productFlavors`.
3. Replace the all-zeros placeholder input in `MainActivity.runInference()` with
   real, preprocessed data (a normalized camera frame, a sensor window, etc.).
