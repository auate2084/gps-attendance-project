package com.gpsattendance.mobile

import android.app.Application
import android.os.Build
import android.util.Log
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GpsAttendanceApp : Application() {
    companion object {
        @Volatile
        var isKakaoMapAvailable: Boolean = true
            private set
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.SUPPORTED_ABIS.any { it.startsWith("x86") }) {
            isKakaoMapAvailable = false
            Log.w("GpsAttendanceApp", "KakaoMap disabled on x86/x86_64 ABI")
            return
        }

        try {
            KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
            isKakaoMapAvailable = true
        } catch (t: Throwable) {
            isKakaoMapAvailable = false
            Log.e("GpsAttendanceApp", "Failed to initialize KakaoMapSdk", t)
        }
    }
}
