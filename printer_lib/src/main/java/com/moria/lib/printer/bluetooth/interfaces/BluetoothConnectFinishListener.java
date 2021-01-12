package com.moria.lib.printer.bluetooth.interfaces;

import android.bluetooth.BluetoothSocket;

/**
 * Author  moria
 * Date    2020/10/13
 * Time    10:56
 */
public interface BluetoothConnectFinishListener {
    void onFinishConnectListener(boolean isConnect, BluetoothSocket mSocket);
}
