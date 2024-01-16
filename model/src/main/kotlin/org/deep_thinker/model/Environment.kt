package org.deep_thinker.model

interface Environment<A, O> {
    fun reset(): O
    fun step(action: A): Step
}

