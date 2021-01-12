package com.moria.lib.printer.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.moria.lib.printer.bluetooth.interfaces.BluetoothConnectFinishListener;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothConnectListener;

import java.io.IOException;
import java.util.UUID;

/**
 * 蓝牙连接,连接在配对之后
 * Author  moria
 * Date    2020/10/14
 * Time    18:26
 */
public class BluetoothConnectTask extends Thread {
    private static final String uuid = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private BluetoothConnectFinishListener connectListener;

    public BluetoothConnectTask(BluetoothDevice device, BluetoothConnectFinishListener connectListener) {
        this.connectListener = connectListener;
        mDevice = device;
    }

    public void run() {
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
            mSocket.connect();
            if (mSocket.isConnected()) {
                connectListener.onFinishConnectListener(true, mSocket);
            } else {
                connectListener.onFinishConnectListener(false, mSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
            connectListener.onFinishConnectListener(false, mSocket);
            cancel();
        }
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
        } finally {

        }
    }
}