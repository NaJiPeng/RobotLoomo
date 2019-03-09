package com.njp.robotloomo.manager

import android.app.Activity
import android.content.Context
import android.hardware.usb.UsbManager
import android.support.v4.content.ContextCompat.getSystemService
import android.util.Log


object UsbSerialManager {

    private lateinit var mUsbManager: UsbManager

    fun init(activity: Activity) {
        mUsbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager

        getDevices()
    }

    fun getDevices() {
        val list = mUsbManager.accessoryList
        if (list.isNullOrEmpty()) {
            Log.i("mmmm", "empty")
            return
        }
        list.forEach {
            Log.i("mmmm", it.description)
        }
    }

}