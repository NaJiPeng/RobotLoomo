package com.njp.robotloomo.manager

import android.annotation.SuppressLint

import android.content.Context
import android.util.Log
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
                                Log.i("onClosed", error)
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
                                    when (it) {
                                        is StringMessage -> {
                                            val data = it.content.split("@")
                                            when (data[0]) {
                                                "mode" -> {
                                                    modeReceiver
                                                }
                                                else -> {
                                                    contentReceiver
                                                }
                                            }?.onNext(data[1])
                                        }
                                        else -> {

                                        }
                                    }
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
    private var modeReceiver: ObservableEmitter<String>? = null
    private var contentReceiver: ObservableEmitter<String>? = null
    private val messageSendListeners = HashMap<Int, ObservableEmitter<Int>>()

    @SuppressLint("StaticFieldLeak")
    private val messageRouter = RobotMessageRouter.getInstance()

    fun init(context: Context) {
        messageRouter.bindService(context, bindStateListener)
    }

    @SuppressLint("CheckResult")
    fun setModeReceiver(receiver: (String) -> Unit) {
        Observable.create(ObservableOnSubscribe<String> {
            modeReceiver = it
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    receiver.invoke(it)
                }
    }

    @SuppressLint("CheckResult")
    fun setContentReciver(receiver: (String) -> Unit) {
        Observable.create(ObservableOnSubscribe<String> {
            contentReceiver = it
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    receiver.invoke(it)
                }
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


}