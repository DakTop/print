package com.moria.print;

import android.bluetooth.BluetoothDevice;

/**
 * Author  moria
 * Date    2020/10/15
 * Time    18:12
 */
public class BluetoothDeviceBean {
    public boolean isConnect;
    public BluetoothDevice device;

    public BluetoothDeviceBean(boolean isConnect, BluetoothDevice device) {
        this.isConnect = isConnect;
        this.device = device;
    }
}
