package com.njp.robotloomo.manager

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import com.njp.robotloomo.event.BaseEvent
import com.segway.robot.sdk.base.action.RobotAction
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.emoji.BaseControlHandler
import com.segway.robot.sdk.locomotion.sbv.Base
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BaseControlManager(context: Context) : BaseControlHandler, LifecycleObserver {

    private val mBase: Base
    private var mIsBindSuccess = false

    private val mBindStateListener = object : ServiceBinder.BindStateListener {
        override fun onBind() {
            mIsBindSuccess = true
        }

        override fun onUnbind(reason: String) {
            mIsBindSuccess = false
        }
    }

    init {
        mBase = Base.getInstance()
        mBase.bindService(context.applicationContext, mBindStateListener)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun setLinearVelocity(velocity: Float) {
        if (mIsBindSuccess) {
            mBase.setLinearVelocity(velocity)
        }

    }

    override fun setAngularVelocity(velocity: Float) {
        if (mIsBindSuccess) {
            mBase.setAngularVelocity(velocity)
        }
    }

    override fun stop() {
        if (mIsBindSuccess) {
            mBase.stop()
        }
    }

    override fun getTicks(): BaseControlHandler.Ticks? {
        return null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unbind() {
        if (mIsBindSuccess) {
            mBase.unbindService()
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(event: BaseEvent) {
        setLinearVelocity(event.linearVelocity)
        setAngularVelocity(event.sngularVelocity)
    }

}