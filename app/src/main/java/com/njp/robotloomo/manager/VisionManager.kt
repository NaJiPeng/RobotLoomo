package com.njp.robotloomo.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.baseconnectivity.ByteMessage
import com.segway.robot.sdk.baseconnectivity.Message
import com.segway.robot.sdk.connectivity.BufferMessage
import com.segway.robot.sdk.connectivity.StringMessage
import com.segway.robot.sdk.vision.Vision
import com.segway.robot.sdk.vision.frame.Frame
import com.segway.robot.sdk.vision.stream.StreamType

/**
 * 视觉
 */
object VisionManager {

    private val mVision = Vision.getInstance()
    private var mIsBindSuccess = false
    private val mBindStateListener = object : ServiceBinder.BindStateListener {
        override fun onUnbind(reason: String?) {
            mIsBindSuccess = false
            Log.i("mmmm", "onUnbind")
        }

        override fun onBind() {
            mIsBindSuccess = true
            Log.i("mmmm", "onBind")
        }

    }

    fun init(context: Context) {
        Log.i("mmmm", "init")
        mVision.bindService(context, mBindStateListener)
    }

    fun unbind() {
        if (mIsBindSuccess) {
            mVision.unbindService()
        }
    }

}