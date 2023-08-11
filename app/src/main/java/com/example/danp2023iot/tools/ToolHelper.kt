package com.example.danp2023iot.tools

import android.content.Context
import com.example.danp2023iot.MainActivity
import com.example.danp2023iot.model.MqttMessageWrapper
import com.example.danp2023iot.model.IoTMessage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date


class ToolHelper {
    val PREF_PUBLISH_END = "publish_end"
    val PREF_PUBLISH_BEGIN = "publish_begin"
    val PREFS_NAME = "app_settings"

    @Throws(IOException::class, ClassNotFoundException::class)
    fun deserialize(data: ByteArray?): Any? {
        val input = ByteArrayInputStream(data)
        val stream = ObjectInputStream(input)
        return stream.readObject()
    }

    @Throws(IOException::class)
    fun serialize(obj: Any?): ByteArray? {
        val out = ByteArrayOutputStream()
        val os = ObjectOutputStream(out)
        os.writeObject(obj)
        return out.toByteArray()
    }

    fun getData(size: Int): ArrayList<MqttMessageWrapper>? {
        val lst = ArrayList<MqttMessageWrapper>()
        try {
            var loc: IoTMessage
            var mqttMessageWrapper: MqttMessageWrapper
            for (i in 0 until size) {
                loc = IoTMessage(i.toString())
                mqttMessageWrapper = MqttMessageWrapper(i.toString(),loc.toString().toByteArray())
                lst.add(mqttMessageWrapper)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lst
    }

    fun setPublishEnd(context: Context, msgCounter: Int) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(PREF_PUBLISH_END, msgCounter)
        editor.commit()
    }

    fun getPublishEnd(context: Context): Int {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.getInt(PREF_PUBLISH_END, -1)
    }

    fun setPublishBegin(context: Context, datetime: String?) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(PREF_PUBLISH_BEGIN, datetime)
        editor.commit()
    }

    fun getPublishBegin(context: Context): String? {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.getString(PREF_PUBLISH_BEGIN, "")
    }

    fun getDateTime(): String? {
        val currentDateTime = System.currentTimeMillis()
        val currentDate = Date(currentDateTime)
        val df: DateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        return df.format(currentDate)
    }
}