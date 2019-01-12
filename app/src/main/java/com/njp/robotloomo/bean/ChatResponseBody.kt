package com.njp.robotloomo.bean

data class ChatResponseBody(
        val emotion: Emotion,
        val intent: Intent,
        val results: List<Result>
)