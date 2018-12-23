package com.njp.robotloomo.manager

import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * 发送IP广播的类
 */
object BroadcastSenderThread : Thread() {

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
                try {
                    multicastSocket.send(datagramPacket)
                } catch (e: Exception) {
                    //DO NOTHING
                }
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