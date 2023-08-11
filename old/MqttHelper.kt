package com.example.danp2023iot

import android.content.Context
import android.util.Log
import com.example.danp2023iot.model.MqttMessageWrapper
import com.example.danp2023iot.tools.FileHelper
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMDecryptorProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder
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
import java.io.BufferedInputStream
import java.io.InputStreamReader
import java.security.KeyPair
import java.security.KeyStore
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Random
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory



class MqttHelper(val context: Context) : MqttCallback {
    //private val mqttHelperListener: MqttHelperListener? = null
    private val TAG = "MqttHelper"
    private val HOST = "ssl://a3brjmyw4c304l-ats.iot.us-east-2.amazonaws.com:8883"

    //    private val USERNAME = "xxxxxxxxxxxxxxxx"
//    private val PASSWORD = ""
    private var mqttAndroidClient: MqttAsyncClient? = null
    private val instance: MqttHelper? = null
    private val TOPIC = "temperature/mobile"
    private val QOS = 1

    private val MAX_SIZE = 1000000
    private val mqttMessageWrapperArray: Array<MqttMessageWrapper?> =
        arrayOfNulls<MqttMessageWrapper>(MAX_SIZE)
    private var COUNTER = 0

    init {

        /*        if (TOPIC.trim { it <= ' ' } === "") try {
                    throw Exception("Topic was not defined")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (QOS < 0) try {
                    throw Exception("QOS was not defined")
                } catch (e: Exception) {
                    e.printStackTrace()
                }*/
        try {
            val clientId = MqttAsyncClient.generateClientId()
            mqttAndroidClient = MqttAsyncClient(HOST, clientId, MemoryPersistence())
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
            /*
            val msg = ToolHelper.deserialize(id.message.getPayload()) as MqttMessageWrapper
             msg.setTimeEnd(System.currentTimeMillis())
             msg.setOrderArrive(COUNTER)
             mqttMessageWrapperArray[COUNTER] = msg
             val msg2 =
                 COUNTER.toString() + ":" + msg.getOrderSend() + ":" + msg.getTimeInit() + ":" + msg.getTimeEnd() // + ":" + (new String(message.getPayload()));
             mqttHelperListener.displayMessage(msg2)
             COUNTER++
             */

            if (message != null) {
                Log.d(TAG, message.payload.toString(Charsets.UTF_8))
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        TODO("Not yet implemented")
    }

    private fun loadCertificates() {

    }

    private fun connect() {
        try {
            val socketFactory = getSocketFactory(
                "AmazonRootCA1.pem",
                "Device-certificate.pem.crt",
                "Mobile_private.pem.key",
                ""
            )
//            val socketFactory=SslUtil(context).getSocketFactory("Device-certificate.pem.crt")
            //val socketFactory=SslUtil(context).getSocketFactory("Mobile_private.pem.key")
            val mqttConnectOptions = MqttConnectOptions()
            mqttConnectOptions.isAutomaticReconnect = true
            mqttConnectOptions.isCleanSession = false
//            mqttConnectOptions.userName = USERNAME
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
                    //subscribeToTopic();
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.d(TAG, "Failed to connect to: $exception")
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    private fun getSocketFactoryFromFiles(): SSLSocketFactory? {
        var string: String? = ""

        val fileHelper = FileHelper(context)

        //val serverUrl = "ssl://serverip:1883"
        val caFilePath = fileHelper.readFile("AmazonRootCA1.pem") // "/your_ssl/cacert.pem"
        val clientCrtFilePath =
            fileHelper.readFile("Device-certificate.pem.crt") //  "/your_ssl/client.pem"
        val clientKeyFilePath =
            fileHelper.readFile("Mobile_private.pem.key") //  "/your_ssl/client.key"
//        val mqttUserName = "guest"
//        val mqttPassword = "123123"

        val socketFactory: SSLSocketFactory? = getSocketFactory(
            caFilePath,
            clientCrtFilePath, clientKeyFilePath, ""
        )
        //getSocketFactory("","","","")

        return socketFactory
    }

    private fun getSocketFactory(
        caCrtFile: String,
        crtFile: String,
        keyFile: String,
        password: String
    ): SSLSocketFactory? {

        Security.addProvider(BouncyCastleProvider())

        // load CA certificate
        var caCert: X509Certificate? = null

        val fis = context.assets.open(caCrtFile)// FileInputStream(caCrtFile)
        var bis = BufferedInputStream(fis)
        val cf = CertificateFactory.getInstance("X.509")
        Log.d("MqttHelper", "caCert.toString()-------------");
        while (bis.available() > 0) {
            caCert = cf.generateCertificate(bis) as X509Certificate
            Log.d("MqttHelper", caCert.toString());
        }

        // load client certificate
        // load client certificate
        bis =
            BufferedInputStream(context.assets.open(crtFile)) // BufferedInputStream(FileInputStream(crtFile))
        var cert: X509Certificate? = null
        Log.d("MqttHelper", "cert.toString()-------------");
        while (bis.available() > 0) {
            cert = cf.generateCertificate(bis) as X509Certificate
            Log.d("MqttHelper", cert.toString());
        }

        // load client private key
        // load client private key
        //val pemParser = PEMParser(FileReader(keyFile))
        val pemParser = PEMParser(InputStreamReader(context.assets.open(keyFile)))
        //val obj: Any = pemParser.readObject()
        val obj: PEMKeyPair  = pemParser.readObject() as PEMKeyPair
        Log.d("MqttHelper", obj.toString())

        val decProv: PEMDecryptorProvider = JcePEMDecryptorProviderBuilder()
            .build(password.toCharArray())

        //val converter: JcaPEMKeyConverter = JcaPEMKeyConverter().getKeyPair(obj)
            //.setProvider("BC")
        val key: KeyPair = JcaPEMKeyConverter().getKeyPair(obj)

/*        val key: KeyPair
        key = if (obj is PEMEncryptedKeyPair) {
            println("Encrypted key - we will use provided password")
            converter.getKeyPair(
                (obj as PEMEncryptedKeyPair).decryptKeyPair(decProv)
            )
        } else {
            println("Unencrypted key - no password needed")
            converter.getKeyPair(obj as PEMKeyPair)
        }*/

        //converter.getKeyPair(obj as PEMKeyPair)
        pemParser.close()

        // CA certificate is used to authenticate server
        val caKs = KeyStore.getInstance(KeyStore.getDefaultType())
        caKs.load(null, null)
        caKs.setCertificateEntry("ca-certificate", caCert)
        val tmf = TrustManagerFactory.getInstance("X509")
        tmf.init(caKs)

        // client key and certificates are sent to server so it can authenticate us
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, null)
        ks.setCertificateEntry("certificate", cert)
        ks.setKeyEntry(
            "private-key",
            key.private,
            password.toCharArray(),
            arrayOf<Certificate>(cert!!)
        )
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(ks, password.toCharArray())

        // finally, create SSL socket factory
        // finally, create SSL socket factory
        val context = SSLContext.getInstance("TLSv1.2")
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.socketFactory;
    }

    fun send(){
        if (mqttAndroidClient!!.isConnected) {
            val timestamp = System.currentTimeMillis()
//            val msg: MqttMessageWrapper = messages.get(MESSAGE_ID2)
//            msg.setTimeInit(timestamp)
            val rand = Random()
            val temp: Int = rand.nextInt(100)
            val jsonString = "{\"Temperatura (C°)\":$temp}"
            val json = JSONObject(jsonString)
            val objAsBytes = json.toString().toByteArray(charset("UTF-8"))
            mqttAndroidClient!!.publish(TOPIC, objAsBytes, 1, true)
            var mqttMessage = MqttMessage()
            mqttMessage.setPayload(objAsBytes)
            mqttMessage.setQos(QOS)
            Log.d(TAG, "Temperatura:$temp")
            //Toast.makeText(,"Temperatura:"+temp,Toast.LENGTH_SHORT).show();
        } else {
            mqttAndroidClient!!.connect()
            Log.d(TAG, " is running:")
        }
    }

}

