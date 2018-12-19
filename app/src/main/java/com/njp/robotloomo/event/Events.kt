package com.njp.robotloomo.event

import com.segway.robot.sdk.baseconnectivity.Message

/**
 * 通讯控制
 */
data class SendEvent(val data: Message<*>)

/**
 * 表情控制
 */
data class EmojiEvent(val id: Int)

/**
 * 底座控制
 */
sealed class BaseEvent {
    /**
     * 车轮速度控制
     */
    data class BaseRawEvent(val linearVelocity: Float, val angularVelocity: Float) : BaseEvent()

    /**
     *  添加检查点
     */
    data class BaseAddEvent(val name: String) : BaseEvent()

    /**
     * 清空检查点并设置起点
     */
    class BaseClearEvent : BaseEvent()

    /**
     * 移动到制定路径点
     */
    data class BasePointEvent(val id: Int) : BaseEvent()

    /**
     * 获取所有路径点
     */
    class BaseGetEvent : BaseEvent()
}
