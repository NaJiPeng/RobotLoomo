package com.njp.robotloomo.manager

import android.content.Context
import com.segway.robot.algo.Pose2D
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.emoji.BaseControlHandler
import com.segway.robot.sdk.locomotion.sbv.Base
import java.lang.StringBuilder

/**
 * 基座
 */
object BaseManager : BaseControlHandler {

    private val mBase = Base.getInstance()
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

    fun init(context: Context) {
        mBase.bindService(context, mBindStateListener)
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

    fun setVelocity(lv: Float, av: Float) {
        if (mIsBindSuccess) {
            if (mBase.controlMode != Base.CONTROL_MODE_RAW) {
                mBase.controlMode = Base.CONTROL_MODE_RAW
            }
            mBase.setLinearVelocity(lv)
            mBase.setAngularVelocity(av)
        }
    }

    fun clear() {
        mPoints.clear()
        if (mIsBindSuccess) {
            mBase.cleanOriginalPoint()
            mBase.setOriginalPoint(mBase.getOdometryPose(-1))
        }
    }

    fun add(name: String) {
        if (mIsBindSuccess) {
            mPoints.add(name to mBase.getOdometryPose(-1))
        }
    }

    fun navigate(id: Int) {
        if (mIsBindSuccess) {
            if (mBase.controlMode != Base.CONTROL_MODE_NAVIGATION) {
                mBase.controlMode = Base.CONTROL_MODE_NAVIGATION
            }
            val pose2D = mPoints[id].second
            mBase.addCheckPoint(pose2D.x, pose2D.y)
        }
    }

    fun getPoints() = StringBuilder("points:")
            .append(mPoints.mapIndexed { i: Int, pair: Pair<String, Pose2D> ->
                "$i-${pair.first}"
            }.joinToString(","))
            .toString()


    fun unbind() {
        if (mIsBindSuccess) {
            mBase.unbindService()
        }
    }

}