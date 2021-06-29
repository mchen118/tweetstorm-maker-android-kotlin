package com.muchen.tweetstormmaker.androidui.mapper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

private typealias IATwitterUserAndTokens = com.muchen.tweetstormmaker.interfaceadapter.model.TwitterUserAndTokens
private typealias IAAccessTokens = com.muchen.tweetstormmaker.interfaceadapter.model.AccessTokens
private typealias IADraft = com.muchen.tweetstormmaker.interfaceadapter.model.Draft
private typealias IADraftContent = com.muchen.tweetstormmaker.interfaceadapter.model.DraftContent
private typealias IASentStatusEnum = com.muchen.tweetstormmaker.interfaceadapter.model.SentStatusEnum

private typealias UITwitterUserAndTokens = com.muchen.tweetstormmaker.androidui.model.TwitterUserAndTokens
private typealias UIAccessTokens = com.muchen.tweetstormmaker.androidui.model.AccessTokens
private typealias UIDraft = com.muchen.tweetstormmaker.androidui.model.Draft
private typealias UIDraftContent = com.muchen.tweetstormmaker.androidui.model.DraftContent
private typealias UISentStatusEnum = com.muchen.tweetstormmaker.androidui.model.SentStatusEnum

fun IADraft.toUIModel(): UIDraft {
    return UIDraft(timeCreated, content, sentStatus.toUIModel(), sentIds)
}

fun UIDraft.toIAModel(): IADraft {
    return IADraft(timeCreated, content, sentStatus.toIAModel(), sentIds)
}

fun List<UIDraft>.toIAModel(): List<IADraft> {
    val result = ArrayList<IADraft>()
    for (uiDraft in this) {
        result.add(uiDraft.toIAModel())
    }
    return result
}

fun UIDraftContent.toIAModel(): IADraftContent {
     return IADraftContent(timeCreated, content)
}

fun IATwitterUserAndTokens.toUIModel(): UITwitterUserAndTokens {
    return UITwitterUserAndTokens(userId, name, screenName, profileImageURLHttps, accessToken, accessTokenSecret)
}

fun UIAccessTokens.toIAModel(): IAAccessTokens {
    return IAAccessTokens(accessToken, accessTokenSecret)
}

fun IASentStatusEnum.toUIModel(): UISentStatusEnum {
    return when(this) {
        IASentStatusEnum.LOCAL -> UISentStatusEnum.LOCAL
        IASentStatusEnum.PARTIALLY_SENT -> UISentStatusEnum.PARTIALLY_SENT
        IASentStatusEnum.FULLY_SENT -> UISentStatusEnum.FULLY_SENT
    }
}

fun UISentStatusEnum.toIAModel(): IASentStatusEnum {
    return when(this) {
        UISentStatusEnum.LOCAL -> IASentStatusEnum.LOCAL
        UISentStatusEnum.PARTIALLY_SENT -> IASentStatusEnum.PARTIALLY_SENT
        UISentStatusEnum.FULLY_SENT -> IASentStatusEnum.FULLY_SENT
    }
}

@JvmName("toUIModelTwitterUserAndTokens")
fun Flow<IATwitterUserAndTokens?>.toUIModel(): Flow<UITwitterUserAndTokens?> {
    return transform {
        if (it != null) emit(it.toUIModel())
        else emit(null)
    }
}

@JvmName("toUIModelDraftList")
fun Flow<List<IADraft>>.toUIModel(): Flow<List<UIDraft>> {
    return transform {
        val result = ArrayList<UIDraft>()
        for (draft in it) {
            result.add(draft.toUIModel())
        }
        emit(result)
    }
}

@JvmName("toUIModelDraft")
fun Flow<IADraft?>.toUIModel(): Flow<UIDraft?> {
    return transform{
        if (it == null) emit(null)
        else emit(it.toUIModel())
    }
}