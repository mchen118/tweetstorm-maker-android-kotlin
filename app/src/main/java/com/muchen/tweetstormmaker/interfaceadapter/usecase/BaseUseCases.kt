package com.muchen.tweetstormmaker.interfaceadapter.usecase


interface InputOutputUseCase<Input, Output>{

    suspend fun execute(input: Input): Output
}

interface OutputUseCase<Output> {

    suspend fun execute(): Output
}

interface InputUseCase<Input> {

    suspend fun execute(input: Input)

}

interface NoInputNoOutputUseCase {

    suspend fun execute()
}