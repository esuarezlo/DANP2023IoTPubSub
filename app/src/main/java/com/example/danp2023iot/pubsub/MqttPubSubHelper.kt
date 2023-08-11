package com.example.danp2023iot.pubsub

import android.content.Context
import android.util.Log


class MqttPubSubHelper(val context: Context, val callback: (String) -> Unit) : MqttPubSubInf {
    private val TAG = "MqttHelper"
    private val HOST = "ssl://a3brjmyw4c304l-ats.iot.us-east-2.amazonaws.com:8883"
    private val TOPIC_PUBLISHER = "sensor/command"
    private val TOPIC_SUBSCRIBE = "sensor/mensajes"
    private val QOS = 1
    private var mqttPubSub: MqttPubSub

    init {

        mqttPubSub = MqttPubSub(
            context,
            HOST,
            TOPIC_PUBLISHER,
            TOPIC_SUBSCRIBE,
            QOS,
            QOS,
            this
        )

    }

    override fun messageNotification(message: String) {
        Log.d(TAG, "payload: " + message)
        callback(message)
    }

    fun publishMessage(command: String) {
        Log.d(TAG, "publishMessage")
        mqttPubSub!!.publish(command)
    }

}

