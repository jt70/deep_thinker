package org.deep_thinker.dl.math

import java.util.*

object SharedRnd {
    private var rnd = Random()

    fun getRnd(): Random {
        return rnd
    }

    fun setRnd(rnd: Random) {
        SharedRnd.rnd = rnd
    }
}
