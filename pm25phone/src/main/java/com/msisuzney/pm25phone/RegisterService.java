package com.msisuzney.pm25phone;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RegisterService extends IntentService {

    private static final int REGISTER_CODE = 1;
    private static final String ACTION = "REGISTER";
    private static final int UNREGISTER_CODE = 2;
    private static final String TAG = "RegisterService";

    //发送广播的socket
    private MulticastSocket ms;

    public RegisterService() {
        super("RegisterService");
    }

    public static void registerMine(Context context) {
        Intent intent = new Intent(context, RegisterService.class);
        intent.putExtra(ACTION, REGISTER_CODE);
        context.startService(intent);
    }

    public static void unregisterMine(Context context) {
        Intent intent = new Intent(context, RegisterService.class);
        intent.putExtra(ACTION, UNREGISTER_CODE);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            ms = new MulticastSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final int code = intent.getIntExtra(ACTION, -1);
            if (code == REGISTER_CODE) {
                handleRegisterMine();
            } else if (code == UNREGISTER_CODE) {
                handleUnRegisterMine();
            }
        }
    }


    private void handleUnRegisterMine() {

        ReceiveDataService.stopReceiveData(this);

        //发送的数据包，局网内的所有地址都可以收到该数据包
        DatagramPacket dataPacket = null;
        try {
            ms.setTimeToLive(4);
            byte[] data = Constants.sStopContent.getBytes();
            //224.0.0.1为广播地址
            InetAddress address = InetAddress.getByName(Constants.multicastHost);

            dataPacket = new DatagramPacket(data, data.length, address,
                    Constants.multicastHostPort);
            ms.send(dataPacket);
            Log.d(TAG, "send stop finish");
            ms.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRegisterMine() {
        ReceiveDataService.startReceiveData(this);

        //发送的数据包
        DatagramPacket dataPacket = null;
        try {
            ms.setTimeToLive(4);
            byte[] data = Constants.sSendContent.getBytes();
            //224.0.0.1为广播地址
            InetAddress address = InetAddress.getByName(Constants.multicastHost);

            dataPacket = new DatagramPacket(data, data.length, address,
                    Constants.multicastHostPort);
            ms.send(dataPacket);
            Log.d(TAG, "send finish");
            ms.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
