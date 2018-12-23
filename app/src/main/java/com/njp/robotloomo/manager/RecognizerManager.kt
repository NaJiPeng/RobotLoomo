package com.njp.robotloomo.manager

import android.content.Context
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.voice.Recognizer

/**
 * 语音识别器
 */
object RecognizerManager {

    private val mRecognizer = Recognizer.getInstance()
    private val mBindStateListener = object :ServiceBinder.BindStateListener{
        override fun onUnbind(reason: String?) {
            mBindSuccess = false
        }

        override fun onBind() {
            mBindSuccess = true
        }

    }
    private var mBindSuccess = false

    fun init(context: Context){
        mRecognizer.bindService(context, mBindStateListener)
    }

    fun unbind(){
        if (mBindSuccess){
            mRecognizer.unbindService()
        }
    }

}