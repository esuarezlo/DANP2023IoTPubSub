package com.example.danp2023iot.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun HomePage(text:String) {
    Column() {
        Row() {
            Text(text = "Temperature:")
            Text(text = text)
        }
    }
}