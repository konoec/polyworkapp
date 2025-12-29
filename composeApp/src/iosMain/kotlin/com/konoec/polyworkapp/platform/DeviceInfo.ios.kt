package com.konoec.polyworkapp.platform

import platform.UIKit.UIDevice

actual object DeviceInfo {
    actual fun getDeviceId(): String {
        val device = UIDevice.currentDevice
        return "iOS ${device.systemVersion} - ${device.model}"
    }
}

