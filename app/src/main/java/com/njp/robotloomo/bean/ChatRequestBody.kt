package com.njp.robotloomo.bean

data class ChatRequestBody(
        val perception: Perception,
        val reqType: Int,
        val userInfo: UserInfo
)