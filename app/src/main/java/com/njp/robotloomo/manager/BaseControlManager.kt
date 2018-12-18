package com.njp.robotloomo.manager

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.util.Log
import com.njp.robotloomo.event.*
import com.segway.robot.algo.Pose2D
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.connectivity.StringMessage
import com.segway.robot.sdk.emoji.BaseControlHandler
import com.segway.robot.sdk.locomotion.sbv.Base
import com.segway.robot.sdk.locomotion.sbv.Base.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BaseControlManager(context: Context) : BaseControlHandler, LifecycleObserver {

    private val mBase: Base
    private var mIsBindSuccess = false
    private val mPoints = ArrayList<Pair<String, Pose2D>>()

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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onRawEvent(event: BaseRawEvent) {
        if (mBase.controlMode != CONTROL_MODE_RAW) {
            mBase.controlMode = CONTROL_MODE_RAW
        }
        setLinearVelocity(event.linearVelocity)
        setAngularVelocity(event.angularVelocity)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onClearPointEvent(event: BaseClearEvent) {
        Log.i("mmmm", "onClearPointEvent")
        mBase.cleanOriginalPoint()
        mPoints.clear()
        if (mIsBindSuccess) {
            mBase.setOriginalPoint(mBase.getOdometryPose(-1))
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onGetEvent(event: BaseGetEvent) {
        Log.i("mmmm", "onGetEvent")
        EventBus.getDefault().post(SendEvent(StringMessage(StringBuffer("points:").apply {
            mPoints.forEachIndexed { i, pair ->
                append("$i-${pair.first},")
            }
        }.deleteCharAt(-1).toString())))
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onAddEvent(event: BaseAddEvent) {
        Log.i("mmmm", "onAddPointEvent:${event.name}")
        mPoints.add(event.name to mBase.getOdometryPose(-1))
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onPointEvent(event: BasePointEvent) {
        Log.i("mmmm", "onPointEvent:${event.id}")
        if (mBase.controlMode != CONTROL_MODE_NAVIGATION) {
            mBase.controlMode = CONTROL_MODE_NAVIGATION
        }
        if (mIsBindSuccess) {
            val point = mPoints[event.id].second
            mBase.addCheckPoint(point.x, point.y)
        }
    }


}