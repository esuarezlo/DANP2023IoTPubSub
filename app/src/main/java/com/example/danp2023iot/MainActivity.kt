package com.example.danp2023iot

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.danp2023iot.pages.HomePage
import com.example.danp2023iot.pubsub.MqttPubSubHelper
import com.example.danp2023iot.ui.theme.DANP2023IoTTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DANP2023IoTTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainComposable()
                }
            }
        }
    }

    @Composable
    private fun MainComposable() {
        val callbackStateMsg = remember { mutableStateOf("") }
        val commandMsg = remember { mutableStateOf("") }
        val mqttPubSubHelper = remember { mutableStateOf<MqttPubSubHelper?>(null) }

        //var mqttPubSubHelper: MqttPubSubHelper? = null

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                mqttPubSubHelper.value = MqttPubSubHelper(this@MainActivity) {
                    callbackStateMsg.value = it
                }
                /*val aws=AWSMqttHelper()
                aws.mainMqtt(this@MainActivity)*/

                /*val basic=BasicDiscovery()
                basic.mainConnect(this@MainActivity)*/

            }) {
                Text(text = "Connect")
            }

            Spacer(modifier = Modifier.height(40.dp))
            TextField(value = commandMsg.value, onValueChange = { commandMsg.value = it })
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick =
                {
                    if(mqttPubSubHelper==null){
                        Log.d("TAG", "mqttPubSubHelper==null")
                    }
                    mqttPubSubHelper.value?.publishMessage(commandMsg.value)

                }) {
                Text(text = "Enviar comando")
            }

            /*            val text = remember { mutableStateOf("") }
                        Button(onClick = {
                            mqttPubSubHelper?.publishMessage()

                        }) {
                            Text(text = "Enviar comando 2")
                        }*/

            /*            Button(onClick = {
                            val n = Random.nextInt(100)
                            text.value = n.toString()

                        }) {
                            Text(text = "Random")
                        }*/

            Spacer(modifier = Modifier.height(20.dp))
            callbackStateMsg.value?.let {
                HomePage(text = callbackStateMsg.value)
            }

        }
    }
}
