package com.msisuzney.pm25demo;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivityIOT";
    private static final String UART_DEVICE_NAME = "UART0";
    final int data_len = 32;//bytes
    byte[] buffer = new byte[data_len];
    private UartDevice uartDevice;

    private UartDeviceCallback mUartCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            try {
                readUartBuffer(uart);
            } catch (IOException e) {
                Log.w(TAG, "Unable to access UART device", e);
            }
            // Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        try {
            new MulticastThread().start();
            PeripheralManagerService manager = new PeripheralManagerService();
            List<String> deviceList = manager.getUartDeviceList();
            if (deviceList.isEmpty()) {
                Log.v(TAG, "No UART port ");
            } else {
                Log.v(TAG, "List if UART port: " + deviceList);
            }
            uartDevice = manager.openUartDevice(UART_DEVICE_NAME);
            if (uartDevice != null)
                configUartFrame(uartDevice);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configUartFrame(UartDevice uart) throws IOException {
        uart.setBaudrate(9600);
//        uart.setDataSize(data_len);
        uart.setParity(UartDevice.PARITY_NONE);
        uart.setStopBits(1);
    }

    private void readUartBuffer(UartDevice uart) throws IOException {
        uart.read(buffer, buffer.length);
        if (buffer[0] == 66 && buffer[1] == 77) {
            int pm1_0 = (buffer[4] << 8) | buffer[5];
            int pm2_5 = (buffer[6] << 8) | buffer[7];
            String pmData = String.valueOf(pm1_0) + "," + String.valueOf(pm2_5);
            Log.d(TAG, "pmData: " + pmData);
            PMDataHolder.setData(pmData);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uartDevice != null) {
            try {
                uartDevice.close();
                uartDevice = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Begin listening for interrupt events
        try {
            uartDevice.registerUartDeviceCallback(mUartCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Interrupt events no longer necessary
        uartDevice.unregisterUartDeviceCallback(mUartCallback);
    }



}

