package org.deep_thinker.org.deep_thinker.codec

import io.vertx.core.buffer.Buffer
import org.deep_thinker.model.*

class GetFirstActionCodec() : io.vertx.core.eventbus.MessageCodec<GetFirstAction, GetFirstAction> {
    override fun encodeToWire(p0: Buffer?, p1: GetFirstAction) {
        TODO("Not yet implemented")
    }

    override fun decodeFromWire(p0: Int, p1: Buffer?): GetFirstAction {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        return "GetFirstActionCodec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun transform(a: GetFirstAction): GetFirstAction {
        return a
    }
}

class StartEpisodeCodec() : io.vertx.core.eventbus.MessageCodec<StartEpisode, StartEpisode> {
    override fun encodeToWire(p0: Buffer?, p1: StartEpisode) {
        TODO("Not yet implemented")
    }

    override fun decodeFromWire(p0: Int, p1: Buffer?): StartEpisode {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        return "StartEpisodeCodec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun transform(a: StartEpisode): StartEpisode {
        return a
    }
}

class GetActionCodec() : io.vertx.core.eventbus.MessageCodec<GetAction, GetAction> {
    override fun encodeToWire(p0: Buffer?, p1: GetAction) {
        TODO("Not yet implemented")
    }

    override fun decodeFromWire(p0: Int, p1: Buffer?): GetAction {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        return "GetActionCodec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun transform(a: GetAction): GetAction {
        return a
    }
}


class EpisodeCompleteCodec() : io.vertx.core.eventbus.MessageCodec<EpisodeComplete, EpisodeComplete> {
    override fun encodeToWire(p0: Buffer?, p1: EpisodeComplete) {
        TODO("Not yet implemented")
    }

    override fun decodeFromWire(p0: Int, p1: Buffer?): EpisodeComplete {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        return "EpisodeCompleteCodec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun transform(a: EpisodeComplete): EpisodeComplete {
        return a
    }
}

class TakeActionCodec() : io.vertx.core.eventbus.MessageCodec<TakeAction, TakeAction> {
    override fun encodeToWire(p0: Buffer?, p1: TakeAction) {
        TODO("Not yet implemented")
    }

    override fun decodeFromWire(p0: Int, p1: Buffer?): TakeAction {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        return "TakeActionCodec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun transform(a: TakeAction): TakeAction {
        return a
    }
}

class SaveModelCodec() : io.vertx.core.eventbus.MessageCodec<SaveModel, SaveModel> {
    override fun encodeToWire(p0: Buffer?, p1: SaveModel) {
        TODO("Not yet implemented")
    }

    override fun decodeFromWire(p0: Int, p1: Buffer?): SaveModel {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        return "SaveModelCodec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun transform(a: SaveModel): SaveModel {
        return a
    }
}

class ActionPerformedCodec() : io.vertx.core.eventbus.MessageCodec<ActionPerformed, ActionPerformed> {
    override fun encodeToWire(p0: Buffer?, p1: ActionPerformed) {
        TODO("Not yet implemented")
    }

    override fun decodeFromWire(p0: Int, p1: Buffer?): ActionPerformed {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        return "ActionPerformedCodec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun transform(a: ActionPerformed): ActionPerformed {
        return a
    }
}

class FirstActionPerformedCodec() : io.vertx.core.eventbus.MessageCodec<FirstActionPerformed, FirstActionPerformed> {
    override fun encodeToWire(p0: Buffer?, p1: FirstActionPerformed) {
        TODO("Not yet implemented")
    }

    override fun decodeFromWire(p0: Int, p1: Buffer?): FirstActionPerformed {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        return "FirstActionPerformedCodec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun transform(a: FirstActionPerformed): FirstActionPerformed {
        return a
    }
}

class UpdatePredictorCodec() : io.vertx.core.eventbus.MessageCodec<UpdatePredictor, UpdatePredictor> {
    override fun encodeToWire(p0: Buffer?, p1: UpdatePredictor) {
        TODO("Not yet implemented")
    }

    override fun decodeFromWire(p0: Int, p1: Buffer?): UpdatePredictor {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        return "UpdatePredictorCodec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun transform(a: UpdatePredictor): UpdatePredictor {
        return a
    }
}
