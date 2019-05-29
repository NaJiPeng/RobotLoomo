package com.njp.robotloomo

import android.databinding.DataBindingUtil
import android.graphics.SurfaceTexture
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import com.njp.robotloomo.databinding.ActivityMainBinding
import com.njp.robotloomo.manager.*
import com.segway.robot.sdk.connectivity.StringMessage
import com.segway.robot.sdk.locomotion.head.Head
import com.segway.robot.sdk.locomotion.sbv.Base
import kotlinx.android.synthetic.main.activity_main.*

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
                "patrol" -> {
                    startPatrolMode()
                }
                "arm"->{
                    startArmMode()
                }
            }

        }

        emoji_view.setOnClickListener {
            ConnectionManager.send(StringMessage("robot:hello"))
        }

        BroadcastSenderThread.start()

    }

    /**
     * 机械手臂模式
     */
    private fun startArmMode() {
        VisionManager.isClassifier = false
        VisionManager.isSend = false

        UsbSerialManager.getDevices()

        RecognizerManager.stop()
        HeadManager.mode = Head.MODE_SMOOTH_TACKING
        HeadManager.worldPitch = 0.6f
        HeadManager.worldYaw = 0f
    }

    /**
     * 路径巡逻模式
     */
    private fun startPatrolMode() {
        VisionManager.isClassifier = false
        VisionManager.isSend = false

        RecognizerManager.stop()
        HeadManager.mode = Head.MODE_SMOOTH_TACKING
        HeadManager.worldPitch = 0.7f
        HeadManager.worldYaw = 0f
        BaseManager.setMode(Base.CONTROL_MODE_NAVIGATION)
        BaseManager.sendPoints()
        BaseManager.openBarrier()
        ConnectionManager.setContentReciver {
            val data = it.split(":")
            when (data[0]) {
                "base_patrol" -> {
                    BaseManager.patrol(data[1].split(",").map { it.toInt() }, data[2].toBoolean())
                }
            }
        }
    }

    /**
     * 对话模式
     */
    private fun startChatMode() {
        VisionManager.isClassifier = false
        VisionManager.isSend = false

        HeadManager.mode = Head.MODE_SMOOTH_TACKING
        HeadManager.worldPitch = 0f
        HeadManager.worldYaw = 0f
        RecognizerManager.start()
        HeadManager.mode = 0
        ConnectionManager.setContentReciver {
            val data = it.split(":")
            when (data[0]) {
                "chat" -> {
                    RecognizerManager.send(data[1])
                }
            }
        }
    }

    /**
     * 遥控模式
     */
    private fun startControlMode() {
        VisionManager.isClassifier = false
        VisionManager.isSend = true

        RecognizerManager.stop()
        HeadManager.mode = Head.MODE_SMOOTH_TACKING
        HeadManager.worldYaw = 0f
        HeadManager.worldPitch = 0f

        ConnectionManager.setContentReciver {
            val data = it.split(":")
            when (data[0]) {
                "base_velocity" -> {
                    BaseManager.setVelocity(data[1].toFloat(), data[2].toFloat())
                }
                "head_velocity" -> {
                    HeadManager.setVelocity(data[1].toFloat(), data[2].toFloat())
                }
                "base_reset" -> {
                    BaseManager.reset()
                }
                "base_add" -> {
                    BaseManager.add(data[1])
                }
                "speak" -> {
                    SpeakManager.speak(data[1])
                }
                "classify"->{
                    VisionManager.isClassifier = data[1].toBoolean()
                }
            }
        }
    }

    private fun initManager() {
        mBinding.textureView.surfaceTextureListener = object :TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                VisionManager.init(this@MainActivity, mBinding.textureView)
            }

        }
        BaseManager.init(this)
        HeadManager.init(this)
        SpeakManager.init(this)
        EmojiManager.init(mBinding.emojiView)
        SensorManager.init(this)
        ConnectionManager.init(this)
        RecognizerManager.init(this)
        UsbSerialManager.init(this)
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
