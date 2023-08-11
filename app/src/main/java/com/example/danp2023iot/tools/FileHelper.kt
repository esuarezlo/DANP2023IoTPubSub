package com.example.danp2023iot.tools

import android.content.Context
import java.io.IOException
import java.io.InputStream

class FileHelper(val context: Context) {

    fun readFile(filename:String):String{
        var content:String?=null;
        try {
            val inputStream: InputStream =  context.assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            content = String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return content!!;
    }
}