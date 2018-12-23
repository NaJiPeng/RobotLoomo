package com.njp.robotloomo.manager

import android.content.Context
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.emoji.HeadControlHandler
import com.segway.robot.sdk.locomotion.head.Head

/**
 * 头部
 */
object HeadManager : HeadControlHandler {
    private val mHead = Head.getInstance()
    private var mIsBindSuccess = false

    private val mBindStateListener = object : ServiceBinder.BindStateListener {
        override fun onBind() {
            mIsBindSuccess = true
            worldPitch = 0.6f
            mode = HeadControlHandler.MODE_EMOJI
        }

        override fun onUnbind(reason: String) {
            mIsBindSuccess = false
        }
    }

    fun init(context: Context) {
        mHead.bindService(context.applicationContext, mBindStateListener)
    }

    override fun getMode(): Int {
        return if (mIsBindSuccess) {
            mHead.mode
        } else 0
    }

    override fun setMode(mode: Int) {
        if (mIsBindSuccess) {
            mHead.mode = mode
        }
    }

    override fun setWorldPitch(angle: Float) {
        if (mIsBindSuccess) {
            mHead.setWorldPitch(angle)
        }
    }

    override fun setWorldYaw(angle: Float) {
        if (mIsBindSuccess) {
            mHead.setWorldYaw(angle)
        }
    }

    override fun getWorldPitch(): Float {
        return if (mIsBindSuccess) {
            mHead.worldPitch.angle
        } else 0f
    }

    override fun getWorldYaw(): Float {
        return if (mIsBindSuccess) {
            mHead.worldYaw.angle
        } else 0f
    }

    fun unbind() {
        if (mIsBindSuccess) {
            mHead.unbindService()
        }
    }

}
