package com.njp.robotloomo.manager

import android.annotation.SuppressLint

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.util.Log
import com.njp.robotloomo.event.*
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.baseconnectivity.Message
import com.segway.robot.sdk.baseconnectivity.MessageConnection
import com.segway.robot.sdk.connectivity.RobotException
import com.segway.robot.sdk.connectivity.RobotMessageRouter
import com.segway.robot.sdk.connectivity.StringMessage
import com.segway.robot.sdk.emoji.configure.BehaviorList
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RobotConnectionManager(context: Context) : LifecycleObserver {
    private var mIsBindSuccess = false
    private val bindStateListener = object : ServiceBinder.BindStateListener {
        override fun onBind() {
            try {
                messageRouter.register {
                    messageConnection = it
                    try {
                        messageConnection?.setListeners(object : MessageConnection.ConnectionStateListener {
                            override fun onOpened() {
                                connectionStateListener?.onNext(true)
                                isConnect = true
                            }

                            override fun onClosed(error: String?) {
                                connectionStateListener?.onNext(false)
                                isConnect = false
                            }

                        }, object : MessageConnection.MessageListener {
                            override fun onMessageSentError(message: Message<*>?, error: String?) {
                                message?.let {
                                    messageSendListeners[message.id]?.onError(Throwable(error))
                                    messageSendListeners.remove(message.id)
                                }
                            }

                            override fun onMessageSent(message: Message<*>?) {
                                message?.let {
                                    messageSendListeners[message.id]?.onNext(it.id)
                                    messageSendListeners.remove(it.id)
                                }
                            }

                            override fun onMessageReceived(message: Message<*>?) {
                                message?.let {
                                    distributeEvent(it)
                                }
                            }
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: RobotException) {
                e.printStackTrace()
            }

        }

        override fun onUnbind(reason: String) {
        }
    }
    private var messageConnection: MessageConnection? = null
    private var isConnect = false
    private var connectionStateListener: ObservableEmitter<Boolean>? = null
    private val messageSendListeners = HashMap<Int, ObservableEmitter<Int>>()

    private val messageRouter: RobotMessageRouter

    init {
        messageRouter = RobotMessageRouter.getInstance()
        messageRouter.bindService(context, bindStateListener)
        EventBus.getDefault().register(this)

    }

    @SuppressLint("CheckResult")
    fun send(message: Message<*>, listener: ((Boolean) -> Unit)? = null) {
        if (!isConnect) {
            listener?.invoke(false)
            return
        }
        Observable.create(ObservableOnSubscribe<Int> {
            messageSendListeners[message.id] = it
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            listener?.invoke(true)
                        },
                        {
                            listener?.invoke(false)
                        }
                )
        messageConnection?.sendMessage(message)
    }

    @SuppressLint("CheckResult")
    fun setConnectStateListener(listener: (Boolean) -> Unit) {
        Observable.create(ObservableOnSubscribe<Boolean> {
            connectionStateListener = it
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it) {
                        listener.invoke(true)
                    } else {
                        listener.invoke(false)
                    }

                }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unBind() {
        if (mIsBindSuccess) {
            messageRouter.unbindService()
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    /**
     * 事件分发
     */
    private fun distributeEvent(message: Message<*>) {
        when (message) {
            is StringMessage -> {
                val messages = message.content.split(":")
                when (messages[0]) {
                    "emoji" -> {
                        EventBus.getDefault().post(EmojiEvent(when (messages[1]) {
                            "IDEA_BEHAVIOR_RANDOM" -> BehaviorList.IDEA_BEHAVIOR_RANDOM
                            "LOOK_AROUND" -> BehaviorList.LOOK_AROUND
                            "LOOK_COMFORT" -> BehaviorList.LOOK_COMFORT
                            "LOOK_CURIOUS" -> BehaviorList.LOOK_CURIOUS
                            "LOOK_NO_NO" -> BehaviorList.LOOK_NO_NO
                            "PHONE_CONNECT_SUCCESS" -> BehaviorList.PHONE_CONNECT_SUCCESS
                            "PHONE_CONNECT_FAIL" -> BehaviorList.PHONE_CONNECT_FAIL
                            "ROBOT_WAKE_UP" -> BehaviorList.ROBOT_WAKE_UP
                            "LOOK_UP" -> BehaviorList.LOOK_UP
                            "LOOK_DOWN" -> BehaviorList.LOOK_DOWN
                            "LOOK_LEFT" -> BehaviorList.LOOK_LEFT
                            "LOOK_RIGHT" -> BehaviorList.LOOK_RIGHT
                            "TURN_LEFT" -> BehaviorList.TURN_LEFT
                            "TURN_RIGHT" -> BehaviorList.TURN_RIGHT
                            "TURN_AROUND" -> BehaviorList.TURN_AROUND
                            "TURN_FULL" -> BehaviorList.TURN_FULL
                            "APPLE_WOW_EMOTION" -> BehaviorList.APPLE_WOW_EMOTION
                            "APPLE_LIKE_EMOTION" -> BehaviorList.APPLE_LIKE_EMOTION
                            "APPLE_LOVE_EMOTION" -> BehaviorList.APPLE_LOVE_EMOTION
                            "APPLE_LOSE_EMOTION" -> BehaviorList.APPLE_LOSE_EMOTION
                            "APPLE_HALO_EMOTION" -> BehaviorList.APPLE_HALO_EMOTION
                            "AVATAR_HELLO_EMOTION" -> BehaviorList.AVATAR_HELLO_EMOTION
                            "AVATAR_CURIOUS_EMOTION" -> BehaviorList.AVATAR_CURIOUS_EMOTION
                            "AVATAR_BLINK_EMOTION" -> BehaviorList.AVATAR_BLINK_EMOTION
                            "TTS_TEST" -> BehaviorList.TTS_TEST
                            else -> BehaviorList.IDEA_BEHAVIOR_RANDOM
                        }))
                    }
                    "base_raw" -> {
                        EventBus.getDefault().post(BaseRawEvent(messages[1].toFloat(), messages[2].toFloat()))
                    }
                    "base_clear" -> {
                        EventBus.getDefault().post(BaseClearEvent())
                    }
                    "base_add" -> {
                        EventBus.getDefault().post(BaseAddEvent(messages[1]))
                    }
                    "base_get" -> {
                        EventBus.getDefault().post(BaseGetEvent())
                    }

                    else -> {

                    }
                }
            }
            else -> {

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onSendEvent(event: SendEvent) {
        Log.i("mmmm", "messagesending")
        send(event.data)
    }


}