package com.njp.robotloomo.event

import com.segway.robot.sdk.baseconnectivity.Message

/**
 * 向手机发送信息
 */
data class SendEvent(val data: Message<*>)

/**
 * 预设动作
 */
data class EmojiEvent(val id: Int)

/**
 * 车轮速度控制
 */
data class BaseRawEvent(val linearVelocity: Float, val angularVelocity: Float)

/**
 *  添加检查点
 */
data class BaseAddEvent(val name: String)

/**
 * 清空检查点并设置起点
 */
class BaseClearEvent

/**
 * 移动到制定路径点
 */
data class BasePointEvent(val id: Int)

/**
 * 获取所有路径点
 */
class BaseGetEvent