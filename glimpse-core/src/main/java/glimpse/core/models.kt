package glimpse.core

import org.tensorflow.lite.Interpreter

private val rawModel by lazy { IOUtils.loadModel(Glimpse.client.applicationContext, "model.tflite") }

internal val intpreter by lazy {
    Interpreter(rawModel, Interpreter.Options())
}