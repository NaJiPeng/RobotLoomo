package com.njp.robotloomo.manager

import android.content.Context
import com.google.gson.Gson
import com.njp.robotloomo.bean.Coor2D
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.connectivity.StringMessage
import com.segway.robot.sdk.emoji.BaseControlHandler
import com.segway.robot.sdk.locomotion.sbv.Base
import kotlin.collections.ArrayList

/**
 * 基座
 */
object BaseManager : BaseControlHandler {

    private val mBase = Base.getInstance()
    private var mIsBindSuccess = false
    private val mPoints = ArrayList<Coor2D>()
    private var mStartPoint = Coor2D(-1, "start", 0f, 0f)

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

    fun setMode(mode: Int) {
        if (mIsBindSuccess) {
            mBase.controlMode = mode
        }
    }

    fun reset() {
        if (mIsBindSuccess) {
            if (mBase.controlMode != Base.CONTROL_MODE_NAVIGATION) {
                mBase.controlMode = Base.CONTROL_MODE_NAVIGATION
            }
            mBase.cleanOriginalPoint()
            mPoints.clear()
            mBase.setOriginalPoint(mBase.getOdometryPose(-1))
            val point = mBase.getOdometryPose(-1)
            mStartPoint.x = point.x
            mStartPoint.y = point.y
            mBase.controlMode = Base.CONTROL_MODE_RAW
        }
    }

    fun sendPoints() {
        ConnectionManager.send(StringMessage("points|${Gson().toJson(mPoints)}"))
    }

    fun add(name: String) {
        val point = mBase.getOdometryPose(-1)
        mPoints.add(Coor2D(mPoints.size, name, point.x - mStartPoint.x, point.y - mStartPoint.y))
    }

    fun nav(id: Int) {
        if (mIsBindSuccess) {
            if (mBase.controlMode != Base.CONTROL_MODE_NAVIGATION) {
                mBase.controlMode = Base.CONTROL_MODE_NAVIGATION
            }
            mBase.addCheckPoint(mPoints[id].x, mPoints[id].y)
        }
    }

    fun openBarrier() {
        mBase.isUltrasonicObstacleAvoidanceEnabled = true
        mBase.ultrasonicObstacleAvoidanceDistance = 0.5f
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

    fun unbind() {
        if (mIsBindSuccess) {
            mBase.unbindService()
        }
    }

}