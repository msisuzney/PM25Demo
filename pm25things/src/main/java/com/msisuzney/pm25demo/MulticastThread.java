package com.msisuzney.pm25demo;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by chenxin.
 * Date: 2017/7/12.
 * Time: 16:49.
 */

public class MulticastThread extends Thread {
    private static final String TAG = MulticastThread.class.getSimpleName();
    private static final String multicastHost = "224.0.0.1";//多播地址
    private static final String dataFormat = "PMClient,port:";
    private static final int multicastHostPort = 8003;//多播地址端口
    private static final String sStopContent = "PMClient,stop";
    public static ConcurrentHashMap<String, Boolean> addresses = new ConcurrentHashMap<String, Boolean>();//boolean没用。。。
    private static Executor service = Executors.newCachedThreadPool();
    private MulticastSocket ds;


    public MulticastThread() {
        try {

            ds = new MulticastSocket(multicastHostPort);
            InetAddress receiveAddress = InetAddress.getByName(multicastHost);
            ds.joinGroup(receiveAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        byte buf[] = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf, 1024);
        while (true) {
            try {
                Log.d(TAG, "MutilcastThread run()");
                ds.receive(dp);
                Log.d(TAG, "client address :" + dp.getAddress());
                Log.d(TAG, "client HostAddress :" + dp.getAddress().getHostAddress());
                Log.d(TAG, "client Port :" + dp.getPort());
                Log.d(TAG, "client content : " + new String(buf, 0, dp.getLength()));
                String content = new String(buf, 0, dp.getLength());
                //如果内容是我们自定的格式，并且是第一次请求
                if (!addresses.containsKey(dp.getAddress().toString()) && content.matches(dataFormat + "\\d+$")) {
                    addresses.put(dp.getAddress().toString(), true);
                    int port = Integer.valueOf(new String(buf, 0, dp.getLength()).split(":")[1]);
                    Log.d(TAG, "接收端口: " + port);
                    service.execute(new SendPMDataThread(dp.getAddress(), port));
                }
                if (addresses.containsKey(dp.getAddress().toString()) && content.equals(sStopContent)) {
                    addresses.remove(dp.getAddress().toString());
                    Log.d(TAG,dp.getAddress().toString() + " 断开连接");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
