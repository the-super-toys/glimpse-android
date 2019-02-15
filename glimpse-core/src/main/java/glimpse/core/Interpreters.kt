package glimpse.core

import glimpse.core.Glimpse.client
import org.tensorflow.lite.Interpreter

val rawModel by lazy { IOUtils.loadModel(Glimpse.client.applicationContext, "saliency_fast_3.tflite") }

internal val interpreter by lazy {
    Interpreter(IOUtils.loadModel(client.applicationContext, "saliency.tflite"), Interpreter.Options().apply {
        setNumThreads(4)
    })
}

internal val interpreterLite by lazy {
    Interpreter(IOUtils.loadModel(client.applicationContext, "saliency_fast_3.tflite"), Interpreter.Options().apply {
        setNumThreads(4)
    })
}