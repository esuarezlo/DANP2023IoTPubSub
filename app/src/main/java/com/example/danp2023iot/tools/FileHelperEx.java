package com.example.danp2023iot.tools;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class FileHelperEx {
    private Context context;

    public FileHelperEx(Context context){
        this.context=context;
    }
    public String getContentString(String AmazonRootCA1_pem) {
        String content = null;
        try {
            InputStream inputStream3 = context.getAssets().open(AmazonRootCA1_pem);
            Integer size3 = inputStream3.available();
            byte[] AmazonRootCA1_pem_buffer = new byte[size3];
            inputStream3.read(AmazonRootCA1_pem_buffer);
            content= new String(AmazonRootCA1_pem_buffer);
        }catch (Exception e){
            e.printStackTrace();
        }

        return content;

    }
}
