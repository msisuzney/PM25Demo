package com.msisuzney.pm25demo;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by chenxin.
 * Date: 2017/7/12.
 * Time: 16:27.
 */
public class SendPMDataThread extends Thread {
    private static final String TAG = SendPMDataThread.class.getSimpleName();
    private InetAddress address;
    private DatagramSocket ds;
    private int port;


    public SendPMDataThread(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            Log.d(TAG, Thread.currentThread() + " send start");
            byte[] bytes = PMDataHolder.getData().getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, address, port);
            try {
                ds.send(datagramPacket);
                Log.d(TAG, "send finish");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(20 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //判断hashmap还有自己对应的client
            //的ip吗,如果没有说明被移除了，结束自己不发数据了
            if (!MulticastThread.addresses.contains(address.toString())) {
                datagramPacket = null;
                ds = null;
                return;
            }
        }
    }
}
