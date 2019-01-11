package com.njp.robotloomo.manager

import android.annotation.SuppressLint
import com.segway.robot.sdk.emoji.*
import com.segway.robot.sdk.emoji.player.RobotAnimator
import com.segway.robot.sdk.emoji.player.RobotAnimatorFactory

/**
 * 表情
 */
object EmojiManager{

    @SuppressLint("StaticFieldLeak")
    private val emoji = Emoji.getInstance()

    private val listener = object : EmojiPlayListener {
        override fun onAnimationEnd(animator: RobotAnimator?) {
        }

        override fun onAnimationCancel(animator: RobotAnimator?) {
        }

        override fun onAnimationStart(animator: RobotAnimator?) {
        }

    }

    fun init(emojiView: EmojiView) {
        emoji.init(emojiView.context)
        emoji.setEmojiView(emojiView)
        emoji.setBaseControlHandler(BaseManager)
        emoji.setHeadControlHandler(HeadManager)
        emoji.setTtsSpeakHnalder(SpeakManager)
    }

    fun start(behavior: Int) {
        emoji.startAnimation(
                RobotAnimatorFactory.getReadyRobotAnimator(behavior),
                listener
        )
    }

}