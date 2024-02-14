package org.deep_thinker.replay

class ReplayBuffer(val capacity: Int, val observationLength: Int) {
  var total = 0
  var actions = IntArray(capacity)
  var rewards = FloatArray(capacity)
  var dones = FloatArray(capacity)
  var states = Array(capacity) { FloatArray(observationLength) }
  var nextStates = Array(capacity) { FloatArray(observationLength) }

  fun add(state: FloatArray, action: Int, reward: Float, done: Boolean, nextState: FloatArray) {
    val index = total % capacity

    actions[index] = action
    rewards[index] = reward
    dones[index] = if (done) 1.0f else 0.0f
    states[index] = state
    nextStates[index] = nextState
    total += 1
  }

  fun sample(batchSize: Int): ReplayBufferSample {
    val maxIndex = Math.min(total, capacity)
    val indicies = mutableListOf<Int>()
    var i = 0
    while (i < batchSize) {
      indicies.addLast((Math.random() * maxIndex).toInt())
      i += 1
    }

    return ReplayBufferSample(
      actions = actions.sliceArray(indicies),
      rewards = rewards.sliceArray(indicies),
      dones = dones.sliceArray(indicies),
      states = states.sliceArray(indicies),
      nextStates = nextStates.sliceArray(indicies)
    )
  }
}
