package com.msisuzney.pm25phone;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    //发送广播的socket
    private MulticastSocket ms;

    private TextView pm1_0;
    private TextView pm2_5;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        new ReceivePMDataThread().start();
    }

    public void init() {
        Button sendUDPBroadcast, stop;
        pm1_0 = (TextView) findViewById(R.id.pm1_0);
        pm2_5 = (TextView) findViewById(R.id.pm2_5);
        stop = (Button) findViewById(R.id.stop);
        sendUDPBroadcast = (Button) findViewById(R.id.send);
        sendUDPBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerPMData();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterPMData();
            }
        });
        try {
            ms = new MulticastSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void unregisterPMData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    private void registerPMData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterPMData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        ms.close();
    }

    private class ReceivePMDataThread extends Thread {

        DatagramSocket socket;
        DatagramPacket dp_receive;
        byte[] buf = new byte[1024];

        public ReceivePMDataThread() {
            init();
        }

        private void init() {
            try {
                socket = new DatagramSocket(Constants.sReceivePMPort);
                dp_receive = new DatagramPacket(buf, 1024);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    socket.receive(dp_receive);
                    Log.d(TAG, new String(dp_receive.getData(), 0, dp_receive.getLength()));
                    String[] pm = new String(dp_receive.getData(), 0,
                            dp_receive.getLength()).split(",");
                    Log.d(TAG, pm[0] + " ," + pm[1]);
                    final String pm1_0str = "当前PM1.0浓度: " + pm[0];
                    final String pm2_5str = "当前PM2.5浓度: " + pm[1];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pm1_0.setText(pm1_0str);
                            pm2_5.setText(pm2_5str);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

