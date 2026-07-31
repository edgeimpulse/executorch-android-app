Place your Edge Impulse ExecuTorch export here as `model.pte`.

How to get it:
  1. In Edge Impulse Studio, deploy your impulse using the ExecuTorch
     deployment block (or export the .pte from your custom block).
  2. Rename the exported program to `model.pte`.
  3. Drop it in this folder (app/src/main/assets/model.pte).
  4. Update `labels.txt` with one class label per line, in output order.
  5. Set INPUT_SHAPE in EdgeImpulseClassifier.kt to match the model input.

This placeholder file is ignored at runtime; only `model.pte` is loaded.
