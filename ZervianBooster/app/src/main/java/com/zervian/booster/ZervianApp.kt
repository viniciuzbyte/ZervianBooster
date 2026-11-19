package com.zervian.booster

import android.app.Application

class ZervianApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Tempat inisialisasi global (crash reporting, dsb) jika nanti ditambah.
    }
}
