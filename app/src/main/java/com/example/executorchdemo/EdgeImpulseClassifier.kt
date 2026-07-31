package com.example.executorchdemo

import android.content.Context
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module
import org.pytorch.executorch.Tensor
import java.io.File
import java.io.FileOutputStream

/**
 * Thin wrapper around an ExecuTorch [Module] for an Edge Impulse-exported model.
 *
 * Drop your export into `app/src/main/assets/`:
 *   - `model.pte`   the ExecuTorch program exported from Edge Impulse
 *   - `labels.txt`  one class label per line, in output order
 *
 * Then set [INPUT_SHAPE] to match your model's input tensor.
 */
class EdgeImpulseClassifier(context: Context) {

    companion object {
        const val MODEL_ASSET = "model.pte"
        const val LABELS_ASSET = "labels.txt"

        /**
         * Input tensor shape expected by the model. Update this to match your export:
         *   - Image classifier 96x96 RGB (NHWC): longArrayOf(1, 96, 96, 3)
         *   - Image classifier 96x96 RGB (NCHW): longArrayOf(1, 3, 96, 96)
         *   - Time-series / tabular, N features:  longArrayOf(1, N)
         */
        val INPUT_SHAPE = longArrayOf(1, 96, 96, 3)
    }

    private val module: Module
    val labels: List<String>

    init {
        val modelPath = copyAssetToCache(context, MODEL_ASSET)
        module = Module.load(modelPath)
        labels = loadLabels(context, LABELS_ASSET)
    }

    /** Number of float values in a single flattened input tensor. */
    fun inputElementCount(): Int = INPUT_SHAPE.fold(1L) { acc, dim -> acc * dim }.toInt()

    data class Prediction(
        val label: String,
        val confidence: Float,
        val scores: FloatArray,
    )

    /**
     * Runs a single forward pass. [input] must contain exactly [inputElementCount]
     * float values laid out to match [INPUT_SHAPE].
     */
    fun classify(input: FloatArray): Prediction {
        require(input.size == inputElementCount()) {
            "Input has ${input.size} values but the model expects " +
                "${inputElementCount()} (shape ${INPUT_SHAPE.toList()})."
        }

        val inputTensor = Tensor.fromBlob(input, INPUT_SHAPE)
        val outputs = module.forward(EValue.from(inputTensor))
        val scores = outputs[0].toTensor().dataAsFloatArray

        val topIndex = scores.indices.maxByOrNull { scores[it] } ?: -1
        val label = labels.getOrElse(topIndex) { "class_$topIndex" }
        val confidence = if (topIndex >= 0) scores[topIndex] else 0f
        return Prediction(label, confidence, scores)
    }

    private fun copyAssetToCache(context: Context, name: String): String {
        val outFile = File(context.filesDir, name)
        context.assets.open(name).use { input ->
            FileOutputStream(outFile).use { output -> input.copyTo(output) }
        }
        return outFile.absolutePath
    }

    private fun loadLabels(context: Context, name: String): List<String> =
        context.assets.open(name).bufferedReader().useLines { lines ->
            lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
        }
}
