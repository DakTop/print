package com.moria.lib.printer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.moria.lib.printer.bluetooth.interfaces.BluetoothConnectFinishListener;
import com.moria.lib.printer.bluetooth.interfaces.BluetoothConnectListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Author  moria
 * Date    2020/10/12
 * Time    17:05
 */
public class BluetoothPrinterService {

    /**
     * 打开蓝牙并扫描蓝牙设备
     */
    public void openOrScanBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //判断设备是否支持蓝牙，如果mBluetoothAdapter为空则不支持，否则支持
        if (mBluetoothAdapter != null) {
            //判断蓝牙是否开启，如果蓝牙没有打开则打开蓝牙
            if (!mBluetoothAdapter.isEnabled()) {
                //打开蓝牙
                mBluetoothAdapter.enable();
            } else {
                if (!mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.startDiscovery();
                }
            }
        }
    }

    /**
     * 配对蓝牙设备，蓝牙设备的使用，需要先配对，后连接
     */
    public void bondDevice(BluetoothDevice device) {
        //在配对之前，停止搜索
        cancelDiscovery();
        if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED) {//没配对才配对
            device.createBond();
        }
    }


    /**
     * 取消配对（取消配对成功与失败通过广播返回 也就是配对失败）
     *
     * @param device
     */
    public void cancelBondDevice(BluetoothDevice device) {
        if (device == null)
            return;
        cancelDiscovery();
        try {
            Method removeBondMethod = device.getClass().getMethod("removeBond");
            removeBondMethod.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 蓝牙设备是否正在绑定
     */
    public boolean isBonding(BluetoothDevice device) {
        return device != null && device.getBondState() == BluetoothDevice.BOND_BONDING;
    }

    /**
     * 蓝牙设备是否已经绑定
     */
    public boolean isBonded(BluetoothDevice device) {
        return device != null && device.getBondState() == BluetoothDevice.BOND_BONDED;
    }


    /**
     * 连接设备，回调监听方法是在子线程中进行的
     *
     * @param device
     * @param connectListener
     */
    public void connectDevice(final BluetoothDevice device, final BluetoothConnectFinishListener connectListener) {
        BluetoothConnectTask task = new BluetoothConnectTask(device, new BluetoothConnectFinishListener() {
            @Override
            public void onFinishConnectListener(boolean isConnect, BluetoothSocket mSocket) {
                connectListener.onFinishConnectListener(isConnect, mSocket);
            }
        });
        task.start();
    }

    /**
     * 取消扫描
     */
    public void cancelDiscovery() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    /**
     * 获取已配对设备
     *
     * @return
     */
    public List<BluetoothDevice> getBondedList() {
        cancelDiscovery();
        List<BluetoothDevice> bondedList = new ArrayList<>();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.getBondedDevices() != null) {
            bondedList.addAll(mBluetoothAdapter.getBondedDevices());
        }
        return bondedList;
    }

    /**
     * 生成设备
     *
     * @param macAddress
     */
    public BluetoothDevice obtainDevice(String macAddress) {
        return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress);
    }

}
