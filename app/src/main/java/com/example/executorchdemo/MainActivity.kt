package com.example.executorchdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.executorchdemo.databinding.ActivityMainBinding
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Volatile
    private var classifier: EdgeImpulseClassifier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.runButton.setOnClickListener { runInference() }
    }

    private fun runInference() {
        binding.runButton.isEnabled = false
        binding.outputText.text = getString(R.string.status_running)

        Thread {
            val message = try {
                val clf = classifier ?: EdgeImpulseClassifier(this).also { classifier = it }

                // TODO: replace this all-zeros placeholder with real, preprocessed input
                // (e.g. a normalized camera frame or a window of sensor samples) that
                // matches EdgeImpulseClassifier.INPUT_SHAPE.
                val input = FloatArray(clf.inputElementCount()) { 0f }

                val prediction = clf.classify(input)
                buildString {
                    appendLine("Top prediction:")
                    appendLine("  ${prediction.label}  (${"%.4f".format(prediction.confidence)})")
                    appendLine()
                    appendLine("All scores:")
                    prediction.scores.forEachIndexed { i, score ->
                        val name = clf.labels.getOrElse(i) { "class_$i" }
                        appendLine("  $name: ${"%.4f".format(score)}")
                    }
                }
            } catch (e: FileNotFoundException) {
                """
                Model or labels not found.

                Add your Edge Impulse export as:
                  app/src/main/assets/model.pte
                  app/src/main/assets/labels.txt

                Then set INPUT_SHAPE in EdgeImpulseClassifier.kt.
                """.trimIndent()
            } catch (t: Throwable) {
                "Inference failed: ${t.message}"
            }

            runOnUiThread {
                binding.outputText.text = message
                binding.runButton.isEnabled = true
            }
        }.start()
    }
}
