package com.njp.robotloomo.manager

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.njp.robotloomo.event.EmojiEvent
import com.segway.robot.sdk.emoji.*
import com.segway.robot.sdk.emoji.player.RobotAnimator
import com.segway.robot.sdk.emoji.player.RobotAnimatorFactory
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class EmojiControlManager(
        emojiView: EmojiView,
        baseControlHandler: BaseControlHandler,
        headControlHandler: HeadControlHandler,
        speakHnalder: TtsSpeakHnalder
) : LifecycleObserver {

    private val emoji: Emoji

    private val listener = object : EmojiPlayListener {
        override fun onAnimationEnd(animator: RobotAnimator?) {
        }

        override fun onAnimationCancel(animator: RobotAnimator?) {
        }

        override fun onAnimationStart(animator: RobotAnimator?) {
        }

    }

    init {
        emoji = Emoji.getInstance()
        emoji.init(emojiView.context)
        emoji.setEmojiView(emojiView)
        emoji.setBaseControlHandler(baseControlHandler)
        emoji.setHeadControlHandler(headControlHandler)
        emoji.setTtsSpeakHnalder(speakHnalder)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun start(behavior: Int) {
        emoji.startAnimation(
                RobotAnimatorFactory.getReadyRobotAnimator(behavior),
                listener
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unbind() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(event: EmojiEvent) {
        start(event.id)
    }

}