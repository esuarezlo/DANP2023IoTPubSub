package com.example.danp2023iot.pubsub

import android.app.Service
import android.content.Intent
import android.os.IBinder


class MqttHelperService() : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(p0: Intent?): IBinder? {
        val a= arrayOf("","")
        TODO("Not yet implemented")
    }

}