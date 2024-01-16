package org.deep_thinker.agent.dqn

import org.deep_thinker.model.EpisodeComplete
import org.deep_thinker.model.GetAction
import org.deep_thinker.model.GetFirstAction
import org.deep_thinker.model.SaveModel

interface DeepQLearning : AutoCloseable {
    fun getFirstAction(getFirstAction: GetFirstAction) : Int
    fun saveModel(saveModel: SaveModel)
    fun getAction(getAction: GetAction): Int
    fun episodeComplete(episodeComplete: EpisodeComplete)
}
