package com.njp.robotloomo.manager

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.emoji.HeadControlHandler
import com.segway.robot.sdk.locomotion.head.Head

class HeadControlManager(context: Context) : HeadControlHandler,LifecycleObserver {
    private val mHead: Head
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

    init {
        mHead = Head.getInstance()
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

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unbind(){
        if (mIsBindSuccess) {
            mHead.unbindService()
        }
    }

}
