package com.msisuzney.pm25phone;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ReceiveDataService extends Service {

    private static final String TAG = "ReceiveDataService";
    private Notification notification;
    private Notification.Builder builder;
    private ReceivePMDataThread receiveThread;
    private DatagramSocket socket;

    public ReceiveDataService() {

    }

    public static void startReceiveData(Context context) {
        Intent intent = new Intent(context, ReceiveDataService.class);
        context.startService(intent);
    }

    public static void stopReceiveData(Context context) {
        Intent intent = new Intent(context, ReceiveDataService.class);
        context.stopService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(12, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PMDataMessageEvent event) {
        String pm1_0str = "PM1.0: " + event.getPm1_0() + " μg/m3";
        String pm2_5str = "PM2.5: " + event.getPm2_5() + " μg/m3";
        String data = pm1_0str + " , " + pm2_5str;
        Log.d(TAG, data);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentText(data);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        Notification notification = builder.build();
        Intent intent = new Intent(ReceiveDataService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ReceiveDataService.this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        startForeground(12, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        receiveThread = new ReceivePMDataThread();
        receiveThread.start();
        builder = new Notification.Builder(this);
        builder.setContentTitle("实时浓度信息");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        notification = builder.build();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (socket != null) {
            socket.close();
//            socket = null;
        }
        receiveThread.interrupt();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //可能存在内存泄露。。
    private class ReceivePMDataThread extends Thread {

        private static final String TAG = "ReceivePMDataThread";
        private byte[] buf = new byte[1024];

        private DatagramPacket dp_receive;

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
//                    Log.d(TAG, new String(dp_receive.getData(), 0, dp_receive.getLength()));
                    String[] pm = new String(dp_receive.getData(), 0,
                            dp_receive.getLength()).split(",");
//                    Log.d(TAG, pm[0] + " ," + pm[1]);
//                    String pm1_0str = "PM1.0: " + pm[0] + " μg/m3";
//                    String pm2_5str = "PM2.5: " + pm[1] + " μg/m3";

//                    String data = pm1_0str + " , " + pm2_5str;
                    EventBus.getDefault().post(new PMDataMessageEvent(Integer.valueOf(pm[0]),
                            Integer.valueOf(pm[1])));
                } catch (IOException e) {
                    e.printStackTrace();

                    /*
                        由于service的onDestroy（）方法将socket关闭了，而Thread还没有停止
                        堵塞的receive（）方法将会抛
                        java.net.SocketException: Socket closed，趁机break退出线程。。。

                        为什么要这样？
                        是因为防止用户在注册了自己后，注销了自己，又立即注册自己的情况
                        在这种情况下，socket没有关闭，可是Thread是死循环不能自己关闭，
                        怎么关闭呢，只能把socket置位全局变量，
                        内部Thread负责创建socket，而用户点击注销会导致Service注销，从而调用
                        onDestroy（）方法，在这个方法中socket.close();

                        不够优雅。。
                     */
                    break;
                }
            }
        }

    }
}
