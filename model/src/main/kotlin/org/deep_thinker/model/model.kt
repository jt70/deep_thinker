package org.deep_thinker.model

data class Step(val reward: Float, val done: Boolean, val state: FloatArray) {
  constructor() : this(0.0f, false, floatArrayOf())
}

data class GetAction(val envId: String, val state: FloatArray, val reward: Float) {
  constructor() : this("", floatArrayOf(), 0.0f)
}

data class GetFirstAction(val envId: String, val state: FloatArray) {
  constructor() : this("", floatArrayOf())
}

data class ActionPerformed(val envId: String, val state: FloatArray, val reward: Float, val action: Int) {
  constructor() : this("", floatArrayOf(), 0.0f, 0)
}

data class FirstActionPerformed(val envId: String, val state: FloatArray, val action: Int) {
  constructor() : this("", floatArrayOf(), 0)
}

data class EpisodeComplete(val envId: String, val state: FloatArray, val reward: Float, val episodeReward: Float)

data class StartEpisode(val episodeNumber: Int)

data class TakeAction(val action: Int)

data class SaveModel(val path: String)

