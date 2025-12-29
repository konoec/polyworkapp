package com.konoec.polyworkapp.platform

import android.os.Build

actual object DeviceInfo {
    actual fun getDeviceId(): String {
        return "Android ${Build.VERSION.RELEASE} - ${Build.MANUFACTURER} ${Build.MODEL}"
    }
}

