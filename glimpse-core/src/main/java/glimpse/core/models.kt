package glimpse.core

internal val rawModel by lazy { IOUtils.loadModel(Glimpse.client.applicationContext, "model.tflite") }