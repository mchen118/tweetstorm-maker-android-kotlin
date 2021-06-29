package com.muchen.tweetstormmaker.persistence.mapper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

private typealias IATwitterUserAndTokens = com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUserAndTokens
private typealias IATwitterUser = com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUser
private typealias IADraft = com.muchen.tweetstormmaker.interfaceadapter.model.Draft
private typealias IADraftContent = com.muchen.tweetstormmaker.interfaceadapter.model.DraftContent
private typealias IADraftSentStatus = com.muchen.tweetstormmaker.interfaceadapter.model.DraftSentStatus
private typealias IASentStatus = com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum

private typealias PTwitterUserAndTokens = com.muchen.tweetstormmaker.persistence.room.model.TwitterUserAndTokens
private typealias PTwitterUser = com.muchen.tweetstormmaker.persistence.room.model.TwitterUser
private typealias PDraft = com.muchen.tweetstormmaker.persistence.room.model.Draft
private typealias PDraftContent = com.muchen.tweetstormmaker.persistence.room.model.DraftContent
private typealias PDraftSentStatus = com.muchen.tweetstormmaker.persistence.room.model.DraftSentStatus
private typealias PSentStatusEnum = com.muchen.tweetstormmaker.persistence.model.SentStatusEnum
private typealias PSentStatus = Int

fun IASentStatus.toPModel(): PSentStatus {
    return when(this) {
        IASentStatus.LOCAL -> PSentStatusEnum.LOCAL.ordinal
        IASentStatus.PARTIALLY_SENT -> PSentStatusEnum.PARTIALLY_SENT.ordinal
        IASentStatus.FULLY_SENT -> PSentStatusEnum.FULLY_SENT.ordinal
    }
}

fun PSentStatus.toIAModel(): IASentStatus {
    return when(this) {
        PSentStatusEnum.LOCAL.ordinal -> IASentStatus.LOCAL
        PSentStatusEnum.PARTIALLY_SENT.ordinal -> IASentStatus.PARTIALLY_SENT
        PSentStatusEnum.FULLY_SENT.ordinal -> IASentStatus.FULLY_SENT
        else -> IASentStatus.LOCAL
    }
}

fun PDraft.toIAModel(): IADraft {
    return IADraft(timeCreated, content, sentStatus.toIAModel(), sentIds)
}

fun IADraft.toPModel(): PDraft {
    return PDraft(timeCreated, content, sentStatus.toPModel(), sentIds)
}

fun PTwitterUserAndTokens.toIAModel(): IATwitterUserAndTokens {
    return IATwitterUserAndTokens(userId, name, screenName, profileImageURLHttps, accessToken, accessTokenSecret)
}

fun IATwitterUserAndTokens.toPModel(): PTwitterUserAndTokens {
    return PTwitterUserAndTokens(userId, name, screenName, profileImageURLHttps, accessToken, accessTokenSecret)
}

fun IATwitterUser.toPModel(): PTwitterUser {
    return PTwitterUser(userId, name, screenName, profileImageURLHttps)
}

fun IADraftContent.toPModel(): PDraftContent {
    return PDraftContent(timeCreated, content)
}

fun IADraftSentStatus.toPModel(): PDraftSentStatus {
    return PDraftSentStatus(timeCreated, sentStatus.toPModel(), sentIds)
}

@JvmName("toIAModelTwitterUserAndTokens")
fun Flow<PTwitterUserAndTokens?>.toIAModel(): Flow<IATwitterUserAndTokens?> {
    return transform {
        if (it != null) emit(it.toIAModel())
        else emit(null)
    }
}

@JvmName("toIAModelDraftList")
fun Flow<List<PDraft>>.toIAModel(): Flow<List<IADraft>> {
    return transform {
        val result = ArrayList<IADraft>()
        for (draft in it) {
            result.add(draft.toIAModel())
        }
        emit(result)
    }
}

@JvmName("toIAModelDraft")
fun Flow<PDraft?>.toIAModel(): Flow<IADraft?> {
    return transform{
        if (it == null) emit(null)
        else emit(it.toIAModel())
    }
}