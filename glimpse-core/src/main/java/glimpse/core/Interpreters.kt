package glimpse.core

import glimpse.core.Glimpse.client
import org.tensorflow.lite.Interpreter

internal val interpreter by lazy {
    Interpreter(IOUtils.loadModel(client.applicationContext, "saliency.tflite"), Interpreter.Options().apply {
        setNumThreads(4)
    })
}

internal val interpreterLite by lazy {
    Interpreter(IOUtils.loadModel(client.applicationContext, "saliency_light.tflite"), Interpreter.Options().apply {
        setNumThreads(4)
    })
}