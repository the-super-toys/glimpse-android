package glimpse.core

internal val rawModel by lazy { IOUtils.loadModel(Glimpse.client.applicationContext, "saliency_fast_3.tflite") }