package com.njp.robotloomo.manager

import android.content.Context
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.vision.Vision

/**
 * 视觉
 */
object VisionManager {

    private val mVision = Vision.getInstance()
    private var mIsBindSuccess = false
    private val mBindStateListener = object : ServiceBinder.BindStateListener {
        override fun onUnbind(reason: String?) {
            mIsBindSuccess = false
        }

        override fun onBind() {
            mIsBindSuccess = true
        }

    }

    fun init(context: Context) {
        mVision.bindService(context, mBindStateListener)
    }

    fun unbind(){
        if (mIsBindSuccess){
            mVision.unbindService()
        }
    }

}