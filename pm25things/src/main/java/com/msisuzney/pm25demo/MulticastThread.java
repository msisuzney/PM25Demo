package com.msisuzney.pm25demo;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by chenxin.
 * Date: 2017/7/12.
 * Time: 16:49.
 */

public class MulticastThread extends Thread {
    private static final String TAG = MulticastThread.class.getSimpleName();
    public static ConcurrentHashSet<String> addresses = new ConcurrentHashSet<>();
    private static Executor service = Executors.newCachedThreadPool();
    private MulticastSocket ds;


    public MulticastThread() {
        try {

            ds = new MulticastSocket(Constants.multicastHostPort);
            InetAddress receiveAddress = InetAddress.getByName(Constants.multicastHost);
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
                if (!addresses.contains(dp.getAddress().toString()) && content.matches(Constants.dataFormat + "\\d+$")) {
                    //hashmap保存client的ip地址
                    addresses.put(dp.getAddress().toString());
                    int port = Integer.valueOf(new String(buf, 0, dp.getLength()).split(":")[1]);
                    Log.d(TAG, "接收端口: " + port);
                    //线程池中启动一个SendPMDataThread，用于向这个client发送PM数据
                    service.execute(new SendPMDataThread(dp.getAddress(), port));
                }
                /*
                        如果hashmap中已经有了这个client ip地址,并且发送过来的数据为"PMClient,stop"，
                     从hashmap中移除这个ip，在线程池中的每一个线程会判断hashmap还有自己对应的client
                     的ip吗,如果没有说明被移除了，结束自己不发数据了
                 */
                if (addresses.contains(dp.getAddress().toString()) && content.equals(Constants.sStopContent)) {
                    addresses.remove(dp.getAddress().toString());
                    Log.d(TAG, dp.getAddress().toString() + " 断开连接");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
