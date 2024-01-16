package org.deep_thinker.model

class TopicGenerator {
  fun getStartEpisodeTopic(envId: String): String {
    return "Environment.StartEpisode.${envId}"
  }

  fun getTakeActionTopic(envId: String): String {
    return "Environment.TakeAction.${envId}"
  }

  fun getActionPerformedTopic(): String {
    return "Agent.ActionPerformed"
  }

  fun getFirstActionPerformedTopic(): String {
    return "Agent.FirstActionPerformed"
  }

  fun getGetFirstActionTopic(envId: String): String {
    return "ActionSelector.GetFirstAction.${envId}"
  }

  fun getGetActionTopic(envId: String): String {
    return "ActionSelector.GetAction.${envId}"
  }

  fun getEpisodeCompleteTopic(envId: String): String {
    return "ActionSelector.EpisodeComplete.${envId}"
  }

  fun getEpisodeCompleteTopic(): String {
    return "EpisodeComplete"
  }
}
