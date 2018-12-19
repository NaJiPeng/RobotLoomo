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
import java.lang.StringBuilder

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
    fun onBaseEvent(event: BaseEvent) {
        when (event) {
            is BaseEvent.BaseRawEvent -> {
                Log.i("mmmm", "BaseRawEvent")
                if (mBase.controlMode != CONTROL_MODE_RAW) {
                    mBase.controlMode = CONTROL_MODE_RAW
                }
                setLinearVelocity(event.linearVelocity)
                setAngularVelocity(event.angularVelocity)
            }
            is BaseEvent.BaseClearEvent -> {
                Log.i("mmmm", "BaseClearEvent")
                mPoints.clear()
                if (mIsBindSuccess) {
                    mBase.cleanOriginalPoint()
                    mBase.setOriginalPoint(mBase.getOdometryPose(-1))
                }
            }
            is BaseEvent.BaseGetEvent -> {
                Log.i("mmmm", "BaseGetEvent")
                EventBus.getDefault().post(SendEvent(StringMessage(
                        StringBuilder("points:").apply {
                            append(mPoints.mapIndexed { index, pair ->
                                "$index-${pair.first}"
                            }.joinToString(","))
                        }.toString()
                )))
            }
            is BaseEvent.BaseAddEvent -> {
                Log.i("mmmm", "BaseAddEvent")
                mPoints.add(event.name to mBase.getOdometryPose(-1))
            }
            is BaseEvent.BasePointEvent -> {
                Log.i("mmmm", "BasePointEvent")
                if (mBase.controlMode != CONTROL_MODE_NAVIGATION) {
                    mBase.controlMode = CONTROL_MODE_NAVIGATION
                }
                if (mIsBindSuccess) {
                    val point = mPoints[event.id].second
                    mBase.addCheckPoint(point.x, point.y, point.theta)
                }
            }

        }
    }

}