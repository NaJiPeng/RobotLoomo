package com.njp.robotloomo.manager

import android.util.Log
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * 发送IP广播的类
 */
class BroadcastSenderThread : Thread() {

    private var ready = true
    private val mHost = "239.0.0.1"
    private val mPort = 10001
    private val mInterval = 1000L

    override fun run() {
        val multicastSocket = MulticastSocket()
        val buffer = "I am loomo".toByteArray()
        val datagramPacket = DatagramPacket(buffer, buffer.size).apply {
            address = InetAddress.getByName(mHost)
            port = mPort
        }
        while (true) {
            if (ready) {
                multicastSocket.send(datagramPacket)
                Log.i("mmmm","send")
                Thread.sleep(mInterval)
            }
        }
    }

    fun beReady() {
        ready = true
    }

    fun notReady() {
        ready = false
    }


}