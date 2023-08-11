package com.example.danp2023iot.pubsub

import android.content.Context
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.BufferedInputStream
import java.io.InputStreamReader
import java.security.KeyPair
import java.security.KeyStore
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

class SSLSocketFactoryHelper(val context: Context) {

    fun getSocketFactory(
        caCrtFile: String,
        crtFile: String,
        keyFile: String
    ): SSLSocketFactory? {
        val password: String=""
        Security.addProvider(BouncyCastleProvider())

        // load CA certificate
        var caCert: X509Certificate? = null
        var bis = BufferedInputStream(context.assets.open(caCrtFile))
        val cf = CertificateFactory.getInstance("X.509")
        while (bis.available() > 0) {
            caCert = cf.generateCertificate(bis) as X509Certificate
        }

        // load client certificate
        var cert: X509Certificate? = null
        bis = BufferedInputStream(context.assets.open(crtFile))
        while (bis.available() > 0) {
            cert = cf.generateCertificate(bis) as X509Certificate
        }

        // load client private key
        val pemParser = PEMParser(InputStreamReader(context.assets.open(keyFile)))
        val pemKeyPair: PEMKeyPair = pemParser.readObject() as PEMKeyPair
        val key: KeyPair = JcaPEMKeyConverter().getKeyPair(pemKeyPair)
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
        val context = SSLContext.getInstance("TLSv1.2")
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.socketFactory;
    }
}