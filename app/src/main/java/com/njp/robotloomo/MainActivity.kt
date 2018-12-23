package com.njp.robotloomo

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.njp.robotloomo.databinding.ActivityMainBinding
import com.njp.robotloomo.manager.*

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initManager()

        BroadcastSenderThread.start()
    }


    private fun initManager() {
        BaseManager.init(this)
        HeadManager.init(this)
        SpeakManager.init(this)
        EmojiManager.init(mBinding.emojiView)
        VisionManager.init(this)
        RecognizerManager.init(this)
        SensorManager.init(this)
        ConnectionManager.init(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unBindManager()
    }

    private fun unBindManager() {
        BaseManager.unbind()
        HeadManager.unbind()
        ConnectionManager.unbind()
        SpeakManager.unbind()
        VisionManager.unbind()
        RecognizerManager.unbind()
        SensorManager.unbind()
    }

}
