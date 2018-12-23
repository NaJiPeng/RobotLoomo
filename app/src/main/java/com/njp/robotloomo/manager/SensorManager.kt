package com.njp.robotloomo.manager

import android.content.Context
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.perception.sensor.Sensor

/**
 * 传感器
 */
object SensorManager {

    private val mSensor = Sensor.getInstance()
    private var mIsBindSuccess = false
    private val mBindStateListener = object : ServiceBinder.BindStateListener {
        override fun onUnbind(reason: String?) {
            mIsBindSuccess = false
        }

        override fun onBind() {
            mIsBindSuccess = true
        }

    }

    fun init(context: Context){
        mSensor.bindService(context, mBindStateListener)
    }

    fun unbind(){
        if (mIsBindSuccess){
            mSensor.unbindService()
        }
    }

}