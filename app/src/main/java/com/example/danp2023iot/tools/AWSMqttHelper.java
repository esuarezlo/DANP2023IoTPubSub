package com.example.danp2023iot.tools;

import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

public class AWSMqttHelper {

    // When run normally, we want to exit nicely even if something goes wrong.
    // When run from CI, we want to let an exception escape which in turn causes the
    // exec:java task to return a non-zero exit code.
    static String ciPropValue = System.getProperty("aws.crt.ci");
    static boolean isCI = ciPropValue != null && Boolean.valueOf(ciPropValue);

    //static CommandLineUtils cmdUtils;

    /*
     * When called during a CI run, throw an exception that will escape and fail the exec:java task
     * When called otherwise, print what went wrong (if anything) and just continue (return from main)
     */
    static void onApplicationFailure(Throwable cause) {
        if (isCI) {
            throw new RuntimeException("BasicConnect execution failure", cause);
        } else if (cause != null) {
            System.out.println("Exception encountered: " + cause.toString());
        }
    }

    public void mainMqtt(Context context) {

        /**
         * cmdData is the arguments/input from the command line placed into a single struct for
         * use in this sample. This handles all of the command line parsing, validating, etc.
         * See the Utils/CommandLineUtils for more information.
         */
        //CommandLineUtils.SampleCommandLineData cmdData = CommandLineUtils.getInputForIoTSample("BasicConnect", args);

        MqttClientConnectionEvents callbacks = new MqttClientConnectionEvents() {
            @Override
            public void onConnectionInterrupted(int errorCode) {
                if (errorCode != 0) {
                    System.out.println("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode));
                }
            }

            @Override
            public void onConnectionResumed(boolean sessionPresent) {
                System.out.println("Connection resumed: " + (sessionPresent ? "existing session" : "clean session"));
            }
        };

        try {

            /**
             * Create the MQTT connection from the builder
             */
//            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder
//                    .newMtlsBuilderFromPath(cmdData.input_cert, cmdData.input_key);




            String Device_certificate_pem_crt="Device-certificate.pem.crt";
            InputStream inputStream = context.getAssets().open(Device_certificate_pem_crt);
            Integer size = inputStream.available();
            byte[] Device_certificate_pem_crt_buffer = new byte[size];
            inputStream.read(Device_certificate_pem_crt_buffer);
            String Device_certificate_pem_crt_content = new String(Device_certificate_pem_crt_buffer);

            String Mobile_private_pem_key="Mobile_private.pem.key";
            InputStream inputStream2 = context.getAssets().open(Mobile_private_pem_key);
            Integer size2 = inputStream2.available();
            byte[] Mobile_private_pem_key_buffer = new byte[size2];
            inputStream2.read(Mobile_private_pem_key_buffer);
            String Mobile_private_pem_key_content = new String(Mobile_private_pem_key_buffer);

            String AmazonRootCA1_pem="AmazonRootCA1.pem";
            InputStream inputStream3 = context.getAssets().open(AmazonRootCA1_pem);
            Integer size3 = inputStream3.available();
            byte[] AmazonRootCA1_pem_buffer = new byte[size3];
            inputStream3.read(AmazonRootCA1_pem_buffer);
            String AmazonRootCA1_pem_content = new String(AmazonRootCA1_pem_buffer);


            Log.d("MqttHelper", Device_certificate_pem_crt_content);
            Log.d("MqttHelper", Mobile_private_pem_key_content);
            Log.d("MqttHelper", AmazonRootCA1_pem_content);

//            String file = "file:///android_asset/Device-certificate.pem.crt";
//            BufferedReader reader = new BufferedReader(new FileReader(file));
//            String line = reader.readLine();


            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder
                    .newMtlsBuilder(Device_certificate_pem_crt_content,Mobile_private_pem_key_content);
                    //.newMtlsBuilder(Device_certificate_pem_crt_buffer,Mobile_private_pem_key_buffer);
                    //.newMtlsBuilderFromPath(Device_certificate_pem_crt, "//android_asset/Mobile_private.pem.key");

            String input_ca = "//android_asset/AmazonRootCA1.pem";
            String input_clientId = "mobile-01";
            String input_endpoint = "a3brjmyw4c304l-ats.iot.us-east-2.amazonaws.com";
            if (input_ca != "") {
                //builder.withCertificateAuthorityFromPath(null, input_ca);
                builder.withCertificateAuthority(AmazonRootCA1_pem_content);
            }

            builder.withConnectionEventCallbacks(callbacks)
                    .withClientId(input_clientId)
                    .withEndpoint(input_endpoint)
                    .withPort((short) 8883)
                    .withCleanSession(true)
                    .withProtocolOperationTimeoutMs(60000);
/*            if (cmdData.input_proxyHost != "" && cmdData.input_proxyPort > 0) {
                HttpProxyOptions proxyOptions = new HttpProxyOptions();
                proxyOptions.setHost(cmdData.input_proxyHost);
                proxyOptions.setPort(cmdData.input_proxyPort);
                builder.withHttpProxyOptions(proxyOptions);
            }*/
            MqttClientConnection connection = builder.build();
            builder.close();


            /**
             * Verify the connection was created
             */
            if (connection == null) {
                onApplicationFailure(new RuntimeException("MQTT connection creation failed!"));
            }

            /**
             * Connect and disconnect
             */
            CompletableFuture<Boolean> connected = connection.connect();
            try {
                boolean sessionPresent = connected.get();
                System.out.println("Connected to " + (!sessionPresent ? "new" : "existing") + " session!");
            } catch (Exception ex) {
                throw new RuntimeException("Exception occurred during connect", ex);
            }
            System.out.println("Disconnecting...");
            CompletableFuture<Void> disconnected = connection.disconnect();
            disconnected.get();
            System.out.println("Disconnected.");

            /**
             * Close the connection now that it is complete
             */
            connection.close();

        } catch (CrtRuntimeException | InterruptedException | ExecutionException ex) {
            onApplicationFailure(ex);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CrtResource.waitForNoResources();
        System.out.println("Complete!");
    }

}
