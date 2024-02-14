package org.deep_thinker.replay

data class ReplayBufferSample(val actions: IntArray, val rewards: FloatArray, val dones: FloatArray, val states: Array<FloatArray>, val nextStates: Array<FloatArray>)
