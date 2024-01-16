package org.deep_thinker.model

data class ReplayBufferSample(val actions: IntArray, val rewards: FloatArray, val dones: FloatArray, val states: Array<FloatArray>, val nextStates: Array<FloatArray>)
