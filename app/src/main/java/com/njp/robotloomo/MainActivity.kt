package com.njp.robotloomo

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.njp.robotloomo.databinding.ActivityMainBinding
import com.njp.robotloomo.manager.*
import com.segway.robot.sdk.connectivity.StringMessage

/**
 * 主程序
 */
class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initManager()

        ConnectionManager.setModeReceiver {
            when (it) {
                "control" -> {
                    startControlMode()
                }
                "chat" -> {
                    startChatMode()
                }
            }

        }

        BroadcastSenderThread.start()
    }

    /**
     * 对话模式
     */
    private fun startChatMode() {
        RecognizerManager.startWakeUp()
    }

    /**
     * 遥控模式
     */
    private fun startControlMode() {
        ConnectionManager.setContentReciver {
            val data = it.split(":")
            when (data[0]) {
                "base_raw" -> {
                    BaseManager.setVelocity(data[1].toFloat(), data[2].toFloat())
                }
                "base_clear" -> {
                    BaseManager.clear()
                }
                "base_add" -> {
                    BaseManager.add(data[1])
                }
                "base_get" -> {
                    ConnectionManager.send(StringMessage(BaseManager.getPoints()))
                }
                "base_point" -> {
                    BaseManager.navigate(data[1].toInt())
                }
                "speak_content" -> {
                    SpeakManager.startSpeak(data[1])
                }
            }
        }
    }

    private fun initManager() {
        BaseManager.init(this)
        HeadManager.init(this)
        SpeakManager.init(this)
        EmojiManager.init(mBinding.emojiView)
        VisionManager.init(this)
        SensorManager.init(this)
        ConnectionManager.init(this)
        RecognizerManager.init(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        BroadcastSenderThread.interrupt()
        BaseManager.unbind()
        HeadManager.unbind()
        ConnectionManager.unbind()
        SpeakManager.unbind()
        VisionManager.unbind()
        SensorManager.unbind()
        RecognizerManager.unbind()
    }

}
