package com.njp.robotloomo.manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.iflytek.cloud.*
import com.njp.robotloomo.network.NetworkManager
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.connectivity.StringMessage
import com.segway.robot.sdk.voice.Recognizer
import com.segway.robot.sdk.voice.recognition.WakeupListener
import com.segway.robot.sdk.voice.recognition.WakeupResult
import io.reactivex.schedulers.Schedulers

/**
 * 语音识别器
 */
object RecognizerManager {

    private val mRecognizer = Recognizer.getInstance()
    private lateinit var mIat: SpeechRecognizer
    private var mBindSuccess = false
    private var mCounter = 0

    private val mBindStateListener = object : ServiceBinder.BindStateListener {
        override fun onUnbind(reason: String?) {
            mBindSuccess = false
        }

        override fun onBind() {
            mBindSuccess = true
        }

    }

    private val mWakeupListener = object : WakeupListener {
        override fun onWakeupResult(wakeupResult: WakeupResult?) {
            Log.i("mmmm", "onWakeupResult")
            mCounter = 0
            recognize()
        }

        override fun onStandby() {
            Log.i("mmmm", "onStandby")
        }

        override fun onWakeupError(error: String?) {
            Log.i("mmmm", "onWakeupError")
        }

    }

    private val mRecognizerListener = object : RecognizerListener {
        override fun onVolumeChanged(p0: Int, p1: ByteArray?) {
//            Log.i("mmmm", "onVolumeChanged")
        }

        @SuppressLint("CheckResult")
        override fun onResult(p0: RecognizerResult?, p1: Boolean) {
            Log.i("mmmm", "onResult:${p0?.resultString}")
            mRecognizer.stopBeamFormingListen()
            if (!p0?.resultString.isNullOrEmpty()) {
                ConnectionManager.send(StringMessage("man:$p0"))
                send(p0!!)
            }
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
            mCounter++
            if (mCounter < 3) {
                recognize()
            } else {
                startWakeUp()
            }
        }

    }

    @SuppressLint("CheckResult")
    fun send(content: RecognizerResult) {
        mRecognizer.stopBeamFormingListen()
        NetworkManager.send(content.resultString)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            Log.i("mmmm", "onnext")
                            val text = it.results[0].values.text
                            SpeakManager.speak(text) {
                                recognize()
                            }
                        },
                        {
                            Log.i("mmmm", "onerror")
                            SpeakManager.startSpeak("网络错误")
                        },
                        {
                            mCounter = 0
                        }
                )
    }

    fun init(context: Context) {
        mRecognizer.bindService(context, mBindStateListener)
        SpeechUtility.createUtility(context, "appid=5c381c02")
        mIat = SpeechRecognizer.createRecognizer(context) {
            if (it != ErrorCode.SUCCESS) {
                Log.i("mmmm", "语音识别模块初始化错误:$it")
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

    fun startWakeUp() {
        if (mBindSuccess) {
            mRecognizer.stopBeamFormingListen()
            mRecognizer.startWakeupMode(mWakeupListener)
        }
    }

    fun stopWakeUp() {
        if (mBindSuccess) {
            mRecognizer.stopBeamFormingListen()
        }
    }

    fun recognize() {
        mIat.startListening(mRecognizerListener)
        mRecognizer.stopBeamFormingListen()
        mRecognizer.startBeamFormingListen { data, dataLength ->
            mIat.writeAudio(data, 0, dataLength)
        }
    }

    fun unbind() {
        if (mBindSuccess) {
            mRecognizer.unbindService()
        }
    }

}