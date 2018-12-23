package com.njp.robotloomo.manager

import android.content.Context

import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.emoji.TtsSpeakHnalder
import com.segway.robot.sdk.voice.Speaker
import com.segway.robot.sdk.voice.tts.TtsListener

/**
 * 扬声器
 */
object SpeakManager : TtsSpeakHnalder {

    private val mSpeaker = Speaker.getInstance()
    private var mIsBindSuccess = false

    private val listener = object : ServiceBinder.BindStateListener {
        override fun onUnbind(reason: String?) {
            mIsBindSuccess = false
        }

        override fun onBind() {
            mIsBindSuccess = true
        }

    }

    fun init(context: Context) {
        mSpeaker.bindService(context, listener)
    }

    override fun startSpeak(tts: String?) {
        if (mIsBindSuccess) {
            tts?.let {
                mSpeaker.speak(tts, object : TtsListener {
                    override fun onSpeechError(word: String?, reason: String?) {
                    }

                    override fun onSpeechStarted(word: String?) {
                    }

                    override fun onSpeechFinished(word: String?) {
                    }

                })
            }
        }
    }

    override fun stopSpeak() {
        if (mIsBindSuccess) {
            mSpeaker.stopSpeak()
        }
    }

    fun unbind() {
        if (mIsBindSuccess) {
            mSpeaker.unbindService()
        }
    }


}