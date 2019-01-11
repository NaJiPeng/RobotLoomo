package com.njp.robotloomo.manager

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.iflytek.cloud.*
import com.iflytek.cloud.util.ResourceUtil
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.voice.Recognizer

/**
 * 语音识别器
 */
object RecognizerManager {

    private val mRecognizer = Recognizer.getInstance()
    private lateinit var mIat: SpeechRecognizer
    private var mBindSuccess = false
    private val mBindStateListener = object : ServiceBinder.BindStateListener {
        override fun onUnbind(reason: String?) {
            mBindSuccess = false
        }

        override fun onBind() {
            mBindSuccess = true
        }

    }
    private val mRecogListener = object : RecognizerListener {
        override fun onVolumeChanged(p0: Int, p1: ByteArray?) {
            Log.i("mmmm", "onVolumeChanged")
        }

        override fun onResult(p0: RecognizerResult?, p1: Boolean) {
            Log.i("mmmm", "onResult:${p0?.resultString}")
            mRecognizer.stopBeamFormingListen()

        }

        override fun onBeginOfSpeech() {
            Log.i("mmmm", "onBeginOfSpeech")

        }

        override fun onEvent(p0: Int, p1: Int, p2: Int, p3: Bundle?) {
            Log.i("mmmm", "onEvent")

        }

        override fun onEndOfSpeech() {
            Log.i("mmmm", "onEndOfSpeech")

        }

        override fun onError(p0: SpeechError?) {
            Log.i("mmmm", "onError:${p0?.errorCode}-${p0?.errorDescription}")
            mRecognizer.stopBeamFormingListen()
        }

    }

    fun init(context: Context) {
        mRecognizer.bindService(context, mBindStateListener)
        SpeechUtility.createUtility(context, "appid=5c381c02")
        mIat = SpeechRecognizer.createRecognizer(context) {
            if (it != ErrorCode.SUCCESS) {
                Log.i("mmmm", "讯飞模块初始化错误:$it")
            }
        }
        mIat.apply {
            mIat.setParameter(SpeechConstant.PARAMS, null)
            setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
            mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1")
            mIat.setParameter(SpeechConstant.RESULT_TYPE, "plain")
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn")
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin")
            mIat.setParameter(SpeechConstant.VAD_BOS, "4000")
            mIat.setParameter(SpeechConstant.VAD_EOS, "1000")
            mIat.setParameter(SpeechConstant.ASR_PTT, "0")
        }

    }

    fun recognize() {
        mIat.startListening(mRecogListener)
        mRecognizer.stopBeamFormingListen()
        mRecognizer.startBeamFormingListen { data, dataLength ->
            //                Log.i("mmmm", "onRawData:$dataLength")
            mIat.writeAudio(data, 0, dataLength)
        }
    }

    fun unbind() {
        if (mBindSuccess) {
            mRecognizer.unbindService()
        }
    }

}