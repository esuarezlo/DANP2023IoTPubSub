package com.example.danp2023iot.pubsub

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.util.Random

class MqttPubSub(
    private val context: Context,
    host: String,
    private val publisher_topic: String,
    private val subscriber_topic: String,
    private val publisher_qos: Int,
    private val subscriber_qos: Int,
    private val mqttPubSubInf: MqttPubSubInf
) : MqttCallback {

    private val TAG = "MqttHelper"
    private var mqttAndroidClient: MqttAsyncClient? = null
    private val instance: MqttPubSubHelper? = null

    init {
        try {
            val clientId = MqttAsyncClient.generateClientId()
            mqttAndroidClient = MqttAsyncClient(host, clientId, MemoryPersistence())
            connect()
            mqttAndroidClient!!.setCallback(this)
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    override fun connectionLost(cause: Throwable?) {

    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        try {
            val topic_name = topic?.let { it }
            val payload = message?.let { it.payload.toString(Charsets.UTF_8) }
            payload?.let { mqttPubSubInf.messageNotification(it) }
//            Log.d(TAG, "topic: " + topic_name)
//            Log.d(TAG, "payload: " + payload)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        TODO("Not yet implemented")
    }

    private fun connect() {
        try {
/*            val socketFactory = SSLSocketFactoryHelper(context).getSocketFactory(
                "AmazonRootCA1.pem",
                "Device-certificate.pem.crt",
                "Mobile_private.pem.key",
            )*/
            val socketFactory = SSLSocketFactoryHelper(context).getSocketFactory(
                "AmazonRootCA1.pem",
                "SensorLab-device-certificate.pem.crt",
                "SensorLab-private.pem.key",
            )
            val mqttConnectOptions = MqttConnectOptions()
            mqttConnectOptions.isAutomaticReconnect = true
            mqttConnectOptions.isCleanSession = false
            //mqttConnectOptions.userName = USERNAME
            mqttConnectOptions.socketFactory = socketFactory
            //mqttConnectOptions.setPassword(PASSWORD.toCharArray());
            mqttConnectOptions.maxInflight = 10

            mqttAndroidClient!!.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d(TAG, "connect to: onSuccess")
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient!!.setBufferOpts(disconnectedBufferOptions)
                    subscribeTopic();
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.d(TAG, "Failed to connect to: $exception")
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    private fun subscribeTopic() {
        mqttAndroidClient!!.subscribe(subscriber_topic, subscriber_qos)
        mqttAndroidClient!!.setCallback(this)
    }

    fun publish(message:String) {
        if (mqttAndroidClient!!.isConnected) {
            //val rand = Random()
            //val temp: Int = rand.nextInt(100)
            //val jsonString = "{\"Temperatura (C°)\":$temp}"
            val jsonString = "{\"command\":$message}"
            val json = JSONObject(jsonString)
            val objAsBytes = json.toString().toByteArray(charset("UTF-8"))

            mqttAndroidClient!!.publish(publisher_topic, objAsBytes, publisher_qos, true)
//            var mqttMessage = MqttMessage()
//            mqttMessage.setPayload(objAsBytes)
//            mqttMessage.setQos(publisher_qos)

            Log.d(TAG, "Temperatura:$message")
        } else {
            mqttAndroidClient!!.connect()
            Log.d(TAG, " is running:")
        }
    }

}
