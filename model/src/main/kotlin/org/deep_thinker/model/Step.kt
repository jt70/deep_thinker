package org.deep_thinker.model

data class Step(val reward: Float, val done: Boolean, val state: FloatArray) {
  constructor() : this(0.0f, false, floatArrayOf())
}

