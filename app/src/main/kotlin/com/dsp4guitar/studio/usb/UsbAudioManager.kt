package com.dsp4guitar.studio.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitors USB device attach/detach events and exposes the list of connected
 * USB Audio Class devices as a [StateFlow].
 */
class UsbAudioManager(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val _connectedDevices = MutableStateFlow<List<UsbDevice>>(emptyList())
    val connectedDevices: StateFlow<List<UsbDevice>> = _connectedDevices.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED,
                UsbManager.ACTION_USB_DEVICE_DETACHED -> refresh()
            }
        }
    }

    fun register() {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        refresh()
    }

    fun unregister() {
        runCatching { context.unregisterReceiver(receiver) }
    }

    private fun refresh() {
        _connectedDevices.value = usbManager.deviceList.values
            .filter { it.isAudioClass() }
    }

    /** Returns true if this USB device advertises the Audio Device Class (0x01). */
    private fun UsbDevice.isAudioClass(): Boolean {
        if (deviceClass == 0x01) return true
        for (i in 0 until interfaceCount) {
            if (getInterface(i).interfaceClass == 0x01) return true
        }
        return false
    }
}
