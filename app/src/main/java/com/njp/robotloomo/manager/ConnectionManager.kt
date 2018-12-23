package com.njp.robotloomo.manager

import android.annotation.SuppressLint

import android.content.Context
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.baseconnectivity.Message
import com.segway.robot.sdk.baseconnectivity.MessageConnection
import com.segway.robot.sdk.connectivity.RobotException
import com.segway.robot.sdk.connectivity.RobotMessageRouter
import com.segway.robot.sdk.connectivity.StringMessage
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * 连接
 */
object ConnectionManager {
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

    @SuppressLint("StaticFieldLeak")
    private val messageRouter = RobotMessageRouter.getInstance()

    fun init(context:Context) {
        messageRouter.bindService(context, bindStateListener)
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

    fun unbind() {
        if (mIsBindSuccess) {
            messageRouter.unbindService()
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

                    }
                    "base_raw" -> {
                        BaseManager.setVelocity(messages[1].toFloat(),messages[2].toFloat())
                    }
                    "base_clear" -> {
                    }
                    "base_add" -> {
                    }
                    "base_get" -> {
                    }
                    "base_point" -> {

                    }
                    else -> {

                    }
                }
            }
            else -> {

            }
        }
    }



}