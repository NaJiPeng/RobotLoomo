package com.njp.robotloomo

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.njp.robotloomo.databinding.ActivityMainBinding
import com.njp.robotloomo.manager.*

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mBaseControlManager: BaseControlManager
    private lateinit var mHeadControlManager: HeadControlManager
    private lateinit var mSpeakControlManager: SpeakControlManager
    private lateinit var mEmojiControlManager: EmojiControlManager
    private lateinit var mConnectionManager: RobotConnectionManager
    private lateinit var mSenderThread: BroadcastSenderThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initManager()

        mSenderThread.start()
    }


    private fun initManager() {
        mSenderThread = BroadcastSenderThread()
        mBaseControlManager = BaseControlManager(this)
        mHeadControlManager = HeadControlManager(this)
        mSpeakControlManager = SpeakControlManager(this)
        mEmojiControlManager = EmojiControlManager(
                mBinding.emojiView,
                mBaseControlManager,
                mHeadControlManager,
                mSpeakControlManager
        )
        mConnectionManager = RobotConnectionManager(this)


        lifecycle.addObserver(mBaseControlManager)
        lifecycle.addObserver(mHeadControlManager)
        lifecycle.addObserver(mSpeakControlManager)
        lifecycle.addObserver(mEmojiControlManager)
        lifecycle.addObserver(mConnectionManager)
    }

}
