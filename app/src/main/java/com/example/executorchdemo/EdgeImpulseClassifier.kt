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
        const val SHAPE_ASSET = "input_shape.txt"

        // Used only when input_shape.txt is absent. Edge Impulse ExecuTorch
        // exports are NCHW, e.g. an image classifier is [1, 3, H, W].
        val DEFAULT_INPUT_SHAPE = longArrayOf(1, 3, 96, 96)
    }

    private val module: Module
    val labels: List<String>

    /**
     * Input tensor shape, read from the `input_shape.txt` asset (comma-separated,
     * e.g. `1,3,96,96`). Falls back to [DEFAULT_INPUT_SHAPE].
     */
    val inputShape: LongArray

    init {
        val modelPath = copyAssetToCache(context, MODEL_ASSET)
        module = Module.load(modelPath)
        labels = loadLabels(context, LABELS_ASSET)
        inputShape = loadInputShape(context, SHAPE_ASSET)
    }

    /** Number of float values in a single flattened input tensor. */
    fun inputElementCount(): Int = inputShape.fold(1L) { acc, dim -> acc * dim }.toInt()

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
                "${inputElementCount()} (shape ${inputShape.toList()})."
        }

        val inputTensor = Tensor.fromBlob(input, inputShape)
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

    private fun loadInputShape(context: Context, name: String): LongArray =
        try {
            val values = context.assets.open(name).bufferedReader().use { it.readText() }
                .split(',', ' ', '\n', '\r', '\t')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { it.toLong() }
                .toLongArray()
            if (values.isNotEmpty()) values else DEFAULT_INPUT_SHAPE
        } catch (e: java.io.FileNotFoundException) {
            DEFAULT_INPUT_SHAPE
        }
}
