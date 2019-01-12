package com.njp.robotloomo.manager

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * 发送IP广播的类
 */
object BroadcastSenderThread : Thread() {

    private var ready = true
    private val mHost = "255.255.255.255"
    private val mPort = 10001
    private val mInterval = 1000L

    override fun run() {
        val multicastSocket = DatagramSocket()
        val buffer = "I am loomo".toByteArray()
        val datagramPacket = DatagramPacket(buffer, buffer.size).apply {
            address = InetAddress.getByName(mHost)
            port = mPort
        }
        while (!isInterrupted) {
            if (ready) {
                try {
                    multicastSocket.send(datagramPacket)
                    Thread.sleep(mInterval)
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    //DO NOTHING
                }
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